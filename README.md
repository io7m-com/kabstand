kabstand
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.kabstand/com.io7m.kabstand.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.kabstand%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.kabstand/com.io7m.kabstand?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/kabstand/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/kabstand.svg?style=flat-square)](https://codecov.io/gh/io7m-com/kabstand)
![Java Version](https://img.shields.io/badge/11-java?label=java&color=5ce6e6)

![com.io7m.kabstand](./src/site/resources/kabstand.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/kabstand/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/kabstand/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/kabstand/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/kabstand/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/kabstand/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/kabstand/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/kabstand/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/kabstand/actions?query=workflow%3Amain.windows.temurin.lts)|

## Kabstand

A simple, correct, efficient [interval tree](https://en.wikipedia.org/wiki/Interval_tree) implementation.

Note: This is a Kotlin port of the [abstand](https://www.github.com/io7m-com/abstand) package
      for users who are stuck on Android and cannot use modern Java. You
      probably want that package instead of this one.

