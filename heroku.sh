#!/usr/bin/env bash

heroku container:login
heroku container:push web -a petfinder-yy
heroku container:release web -a petfinder-yy
