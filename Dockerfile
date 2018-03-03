FROM mhart/alpine-node:latest

MAINTAINER Your Name <you@example.com>

# Create app directory
RUN mkdir -p /jsx-parser
WORKDIR /jsx-parser

# Install app dependencies
COPY package.json /jsx-parser
RUN npm install pm2 -g
RUN npm install

# Bundle app source
COPY target/release/jsx-parser.js /jsx-parser/jsx-parser.js
COPY public /jsx-parser/public

ENV HOST 0.0.0.0

EXPOSE 3000
CMD [ "pm2-docker", "/jsx-parser/jsx-parser.js" ]
