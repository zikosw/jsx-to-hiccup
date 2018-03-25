# JSX to Hiccup

Convert JSX to Hiccup, You can use it here - [jsxtohiccup.jigko.net](https://jsxtohiccup.jigko.net)

Feel free to open an issue if you see something broken xD


## Setup

To bundle node modules

    yarn js

To get an interactive development environment run:

    lein figwheel

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2014 jigkoxsee

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
