# petfinder

REST backend for lost pets announcements application.

### TODO
1. to think about id in db
2. authentication:
    a) https://medium.com/@ard333/authentication-and-authorization-using-jwt-on-spring-webflux-29b81f813e78
    b) https://habr.com/ru/post/485498/
    Need to decide who does what: JWTReactiveAuthenticationManager, TokenAuthenticationConverter
3. com.mongodb.MongoWriteException: E11000 duplicate key error collection: pets.user index: uuid dup key: { : null } - should be bad request
4. update doesn't work properly
5. searcg pet access to all
