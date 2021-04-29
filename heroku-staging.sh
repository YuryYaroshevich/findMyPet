#!/usr/bin/env bash

heroku container:login
docker buildx build --platform linux/amd64 -t petfinder-yy .
docker tag petfinder-yy registry.heroku.com/petfinder-yy/web
docker push registry.heroku.com/petfinder-yy/web
heroku container:release web -a petfinder-yy
