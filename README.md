# bullet-record

[![Build Status](https://travis-ci.org/yahoo/bullet-record.svg?branch=master)](https://travis-ci.org/yahoo/bullet-record) [![Coverage Status](https://coveralls.io/repos/github/yahoo/bullet-record/badge.svg?branch=master)](https://coveralls.io/github/yahoo/bullet-record?branch=master) [![Download](https://api.bintray.com/packages/yahoo/maven/bullet-record/images/download.svg) ](https://bintray.com/yahoo/maven/bullet-record/_latestVersion)


This is a wrapper for the Record object that interfaces with the Bullet
query engine. Users wishing their data to be queryable by Bullet insert
their data into a BulletRecord.

The BulletRecord is serializable and serializes itself using Avro.


## Installation

The artifacts is available through JCenter if you need to depend on it in code directly, so you will need to add the repository.

```xml
    <repositories>
        <repository>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <id>central</id>
            <name>bintray</name>
            <url>http://jcenter.bintray.com</url>
        </repository>
    </repositories>
```

```xml
    <dependency>
      <groupId>com.yahoo.bullet</groupId>
      <artifactId>bullet-record</artifactId>
      <version>${bullet.version}</version>
    </dependency>
```

If you just need the jar artifact, you can download it directly from [JCenter](http://jcenter.bintray.com/com/yahoo/bullet/bullet-record/).

Code licensed under the Apache 2 license. See LICENSE file for terms.
