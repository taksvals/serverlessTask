#!/usr/bin/env bash

URL="https://1ohzj5ctvg.execute-api.us-east-1.amazonaws.com/dev/calculator"


curl -XPOST $URL\
	 -H "Content-Type: application/json"\
	 -d @payload.json