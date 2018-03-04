FROM mhart/alpine-node:latest

MAINTAINER Your Name <you@example.com>

# Create app directory
RUN mkdir -p /jsx-to-hiccup
WORKDIR /jsx-to-hiccup

# Install app dependencies
COPY package.json /jsx-to-hiccup
RUN npm install pm2 -g
RUN npm install

# Bundle app source
COPY target/release/jsx-to-hiccup.js /jsx-to-hiccup/jsx-to-hiccup.js
COPY public /jsx-to-hiccup/public

ENV HOST 0.0.0.0

EXPOSE 3000
CMD [ "pm2-docker", "/jsx-to-hiccup/jsx-to-hiccup.js" ]
