# petfinder

REST backend for lost pets announcements application.

### Run locally
1) create directory structure `data/db`
2) run mongoDb with command `mongod --dbpath=data/db`
3) open root directory of `petfinder` project
4) run `./gradlew bootRun`
To play with API install Postman and import postman collection(from `postman` directory) into Postman app.

## Heroku deployment

### Deploy to Heroku staging
run `./heroku-staging.sh`

### Get app's logs from Heroku
run `heroku logs -a petfinder-yy`

### Get inside container
run `heroku run bash -a petfinder-yy`

### Debug performance
Install heroku plugin `~~heroku plugins:install heroku-cli-java~~`
and then you can run following commands:
```$xslt
heroku java:jconsole -a petfinder-yy
heroku java:visualvm -a petfinder-yy
heroku java:jmap -a petfinder-yy
heroku java:jstack -a petfinder-yy
```
It may be the case that for visualvm this command is needed:
```
jvisualvm -J-DsocksProxyHost=localhost -J-DsocksProxyPort=1080 --openjmx=:1099
```
fucking visualvm: `jvisualvm -J-DsocksProxyHost=localhost -J-DsocksProxyPort=1080 --openjmx=:1098`

### Instance management
https://devcenter.heroku.com/articles/dynos#cli-commands-for-dyno-management

## TODO
0. check that sorting uses index when geo search request is made
0. unstable test SpotAdControllerTest.testSpotAdCreationNotifiesPetAdsOwners
1. to think about id in db
4. logging
5. password validation(min length for example)
6. lombok @NonNull accepts empty string. Validate for empty string where needed.
