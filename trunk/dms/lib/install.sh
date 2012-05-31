#!/bin/bash
sudo mkdir /var/dms
mvn install:install-file -DgroupId=com.drew -DartifactId=metadata-extractor -Dversion=2.5.0-RC2 -Dpackaging=jar -Dfile=lib/metadata-extractor-2.5.0-RC2.jar
mvn install:install-file -DgroupId=netscape -DartifactId=javascript -Dversion=1.6.0 -Dpackaging=jar -Dfile=lib/javascript-1.6.0.jar


