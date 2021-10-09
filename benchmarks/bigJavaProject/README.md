# Treasure Data Bulk Import Tool in Java

## Overview

Many web/mobile applications generate huge amount of event logs (c,f. login,
logout, purchase, follow, etc).  Analyzing these event logs can be quite
valuable for improving services.  However, analyzing these logs easily and
reliably is a challenging task.

Treasure Data solves the problem by having: easy installation, small
footprint, plugins reliable buffering, log forwarding, the log analyzing, etc.

  * Treasure Data website: [http://treasure-data.com/](http://treasure-data.com/)
  * Treasure Data GitHub: [https://github.com/treasure-data/](https://github.com/treasure-data/)

`td-import-java` is a Java library to access Treasure Data from a Java application.
The library includes a 'main' function and can therefore be used as a command-line
interface as well.
This library is based off the [`td-client-java` Java client library](https://github.com/treasure-data/td-client-java)
and it is also used with the [`td CLI`](https://github.com/treasure-data/td).

## Requirements

Java >= 1.6

## Install From GitHub repository

You can get latest source code using git.

    $ git clone https://github.com/treasure-data/td-import-java.git
    $ cd td-import-java
    $ mvn package

The td-import jar file will be built and stored inside the td-import-java/target/ folder.
The file name will be `td-import-${td-import.version}-jar-with-dependencies.jar`.
See the [`pom.xml` file](https://github.com/treasure-data/td-import-java/blob/master/pom.xml) for more details.

## Configuration

Please refer to the [td-client-java Java client library README](https://github.com/treasure-data/td-client-java/blob/master/README.md#configuration)
for information on how to configure the library.

## License

Apache License, Version 2.0

