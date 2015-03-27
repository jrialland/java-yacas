
yacas
=====

A mavenized and cleaned-up version of the java port of the yaCAS computer algebra system, version 1.3.6 (http://yacas.sourceforge.net/homepage.html)

This project is published under the GNU GPL v2 License, as the work it is derived from.

[![Build Status](https://travis-ci.org/jrialland/java-yacas.svg)](https://travis-ci.org/jrialland/java-yacas)

Sample usage :

```java
    YacasInterpreter yacas = new YacasInterpreter();
    System.out.println(yacas.Evaluate("Taylor(x,0,5) Sin(x)")); // prints x-(x^3/6)+(x^5/120)

```
