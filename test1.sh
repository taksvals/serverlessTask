#!/usr/bin/env bash

URL="https://6f33mdx1h2.execute-api.eu-west-1.amazonaws.com/calculator"


curl -XPOST $URL\
	 -H "Content-Type: application/json"\
	 -d @payload1.json