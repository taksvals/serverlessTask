#!/usr/bin/env bash

URL="https://7grq4506mk.execute-api.us-east-1.amazonaws.com/dev/calculator"


curl -XPOST $URL\
	 -H "Content-Type: application/json"\
	 -d @payload.json