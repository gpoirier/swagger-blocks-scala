package fixtures

object PetstoreAPI {

  import swaggerblocks._
  import swaggerblocks.Implicits.stringToSomeString

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

  lazy val path = swaggerPath("/pets")(
    operations = List(
      operation(GET)(
        description = "Returns a list of pet objects",

        tags = List("pet"),

        parameters = List(
          parameter(
            name = "tags",
            in = Query,
            required = false,
            typ = s.String,
            description = "tags to filter by",
            schema = None
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
      typ = s.Int
    ),

    property("name")(
      typ = s.String,
      description = "Name of the pet"
    ),

    property("tag")(
      typ = s.String,
      required = false
    )
  )

  lazy val paths = List(path)
  lazy val schemas = List(petSchema)

}