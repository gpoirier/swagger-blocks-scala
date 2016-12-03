# swagger-blocks-scala [![Build Status](https://travis-ci.org/felixbr/swagger-blocks-scala.svg?branch=master)](https://travis-ci.org/felixbr/swagger-blocks-scala)

A library to express swagger specifications using a Scala DSL.

It is inspired mainly by [fotinakis/swagger-blocks](https://github.com/fotinakis/swagger-blocks) 
for ruby.

Currently this only supports a part of the full swagger-spec and only **Scala 2.11**

## Goals

* Express swagger specifications in a reasonably type-safe DSL.
* Don't clutter models and logic with annotations
* Provide bindings for common json-libs (and yaml)
* Avoid reflection
* Be reasonable to use in IDEs (e.g. IntelliJ)

## Getting Started

### Dependencies

The library is published on Sonatype:

    resolvers += Resolver.sonatypeRepo("releases")

There is a core modul available for the DSL and core data types:

    "io.github.felixbr" %% "swagger-blocks-scala" % "0.2.1"

The plan is to provide bindings for the most popular json libs, but right now 
only play-json and moultingyaml are supported. The extensions also include the core lib, so 
you only need to specify the extension you want to use:

#### play-json (2.5.9)

    "io.github.felixbr" %% "swagger-blocks-play" % "0.2.1"
    
#### moultingyaml (0.3.1)

    "io.github.felixbr" %% "swagger-blocks-yaml" % "0.2.1
    
### Writing Swagger Path and Schema specifications
    
Write a specification for your endpoint (e.g. in your controller's companion 
object):

```Scala
import swaggerblocks._
import swaggerblocks.Implicits._

object PetsController {

  lazy val petListPath = swaggerPath("/pets")(
    operations = List(
      operation(GET)(
        description = "Returns a list of pet objects",
        summary = "Returns a list of pet objects",

        tags = List("pet"),

        parameters = List(
          queryParameter(
            name = "tag",
            required = false,
            schema = t.string,
            description = "tag to filter by"
          )
        ),

        responses = List(
          response(200)(
            description = "pet response",
            schema = manyOf(petSchema)
          )
        )
      )
    )
  )

  lazy val petSchema = swaggerSchema("Pet")(
    property("id")(
      schema = t.integer
    ),

    property("name")(
      schema = t.string,
      description = "Name of the pet"
    ),

    property("tag")(
      schema = t.string,
      required = false
    )
  )
```

### Rendering the output for swagger-ui

Create a new endpoint which serves the json needed for the swagger-ui. I 
recommend to use the same controller which serves the ui. You also have to 
write the required root metadata for swagger:

```Scala
import javax.inject._
import play.api.mvc._

import swaggerblocks._
import swaggerblocks.Implicits._
import swaggerblocks.rendering.playJson.renderPretty

@Singleton
class SwaggerController @Inject()() extends Controller {

  lazy val petstoreRoot = swaggerRoot("2.0")(
    host = "petstore.swagger.wordnik.com",
    basePath = "/api",

    info(
      version = "1.0.0",
      title = "Swagger Petstore",
      description = "A sample API that uses a petstore as an example",

      contact = contact(
        name = "Wordnik API Team"
      ),

      license = license(
        name = "MIT"
      )
    )
  )

  val paths = List(
    PetsController.petListPath
  )
  val schemas = List(
    PetsController.petSchema
  )

  def json = Action {
    val swaggerJson = renderPretty(petstoreRoot, paths, schemas)

    Ok(swaggerJson)
  }
}
```

Now you only have to include the new action in your `routes` file and point 
a swagger-ui (possibly rendered by a standard html-view) at the url.


## Caveats

While for the most part the library API tries to stick to the swagger spec, 
there are some exceptions made either for technical reasons, type-safety or 
usability:

* Since `type` is a restricted keyword you'll have to use `schema` in its place. 
You can use primitive types (e.g. `schema = t.string`) as well as references to schemas 
(e.g. `schema = petSchema`) and it will be sorted out behind the scenes.
* Many values in swagger can either be a primitive type, an object or a list of 
the former two. In order to make this more type-safe 
and usable, there is a `manyOf` helper for lists, which accepts primitive types 
or schemas.
Because of type erasure you have to use `parameter.manyOf` instead of just `manyOf` for 
all non-body parameters.
* Types are currently namespaced by `t` (e.g. `t.string`, `t.integer`, `t.number`). 
Some types should only be used in certain places according to the swagger spec. 
For example `t.file` is not allowed in path-parameters, but currently this is not enforced. 
There are namespaces for the different parameters, so you can easily find the allowed 
ones using autocompletion (e.g. `t.parameter.path.string` or `t.parameter.formData.file`).
* The DSL uses a few implicit conversions to provide an easier and more uniform way to write things.
For example most optional text fields like `description` still accept a `String` literal 
due to an implicit `String => Option[String]`. The other conversions deal with differences 
in `schema` fields.
