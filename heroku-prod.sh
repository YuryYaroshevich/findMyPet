#!/usr/bin/env bash

heroku container:login
docker buildx build --platform linux/amd64 -t petfinder-prod .
docker tag petfinder-prod registry.heroku.com/petfinder-prod/web
docker push registry.heroku.com/petfinder-prod/web
heroku container:release web -a petfinder-prod
