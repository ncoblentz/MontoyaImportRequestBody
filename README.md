# Import Request Body to Repeater Burp Extension

_By [Nick Coblentz](https://www.linkedin.com/in/ncoblentz/)_

__The Burp Extension is made possible by [Virtue Security](https://www.virtuesecurity.com), the Application Penetration Testing consulting company I work for.__

## About

The __Import Request Body ot Repeater Extension__ provides a UI to define an HTTP request template and then list file locations that have the request body to import into Burp's repeater. I use it when developers provide SOAP requests bodies without the headers to then import them in mass to repeater.

## How to Use It

- Build it with `gradlew shadowJar`
- Add the extension in burp from the `build/libs/ImportRequestsToRepeater-x.y-all.jar` folder where `x.y` represents the build version