# petfinder

REST backend for lost pets announcements application.

## How to start
1) create directory structure `data/db`
2) run mongoDb with command `mongod --dbpath=data/db`
3) open root directory of `petfinder` project
4) run `./gradlew bootRun`
To play with API install Postman and import postman collection(from `postman` directory) into Postman app.

### TODO
1. to think about id in db
2. logging
3. password validation(min length for example)
4. change to java 8 for Heroku deployment
