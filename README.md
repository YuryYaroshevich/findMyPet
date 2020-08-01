# petfinder

REST backend for lost pets announcements application.

## Run locally
1) create directory structure `data/db`
2) run mongoDb with command `mongod --dbpath=data/db`
3) open root directory of `petfinder` project
4) run `./gradlew bootRun`
To play with API install Postman and import postman collection(from `postman` directory) into Postman app.

## Deploy to Heroku
run `./heroku.sh`

## Get app's logs from Heroku
run `heroku logs -a petfinder-yy`

### TODO
1. to think about id in db
4. logging
5. password validation(min length for example)
