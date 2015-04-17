# Compile command line #

For those with a hurry, the steps to get the source compiling command line are quite simple:
  1. Get and install [Apache Maven](http://maven.apache.org/)
  1. [Checkout](http://code.google.com/p/ammrf-dms/source/checkout) the source code
  1. Open a command line shell and into the checked out directory do:
```
    $ cd ammrf-dm
    $ mvn clean install
```
> This compiles all sources command line and should work out of the box.

# Getting into the IDE #

The AMMRF-DMS was developed using the eclipse-based Spring source's _Integrated Development Environment_. Here the steps to get the DMS into STS.

  1. First, follow the steps above to get the source compiling command line.
  1. Get and install the Spring source Tool Suite (STS) [here](http://www.springsource.com/developer/sts)
  1. In STS use **Import** > **Existing maven projects**, and select the top folder
  1. STS cannot understand few generated folders that have been configured into the classpath; so one has to do it manually by selecting all projects, right-click and selecting **Maven** > **Update project configuration** and then same with **Maven** > **Update dependencies**
  1. Done. You should have the DMS compiled into STS.
  1. We recommend disabling all validations in STS.

# Few points on basic STS troubleshooting #

STS is a powerful IDE including support for Java, JSPX, javascript, AspectJ, Spring Roo, among others. But the DMS uses Roo and other features and it stretches it quite a bit; sometimes it just can't handle it. So, few tips are helpful:

  1. Ignore the javascript error reported on jquery.
  1. Sometimes, when using **auto build**, it just gets recompiling forever (in Linux). Disable **auto build** and build manually... at least for a while.
  1. Doing **Maven** > **Update project configuration**, **Maven** > **Update dependencies** then **Clean all projects** usually helps.
  1. Tomcat support in Eclipse is a pain. So, **clean server folder** in context menu usually helps so things get re-deployed to known state as they should.
  1. It is recommended to extend the Tomcat start up time out to at least 1.5 mins. (This is for your development machine, in a middle-end dedicated server the DMS starts in under 30 secs.)
  1. We believe most issues are created when one runs the command line maven build and then refresh STS. Unfortunately, doing a command line `mvn clean install` to get all the tests checked is a must for any experienced developer. Our work around, cleaning the project space as mentioned above usually helps.

# Running the DMS from STS #

Running the DMS inside STS with a single worker architecture requires more steps:

## Install required third party software ##

  1. Grab and install [MySQL](http://www.mysql.com/)
  1. Likewise for [ActiveMQ](http://activemq.apache.org/)
  1. and [Tomcat 6](http://tomcat.apache.org/download-60.cgi)
  1. In MySQL, create a database `ammrf` and populate it with the provided [sql](http://code.google.com/p/ammrf-dms/source/browse/trunk/dms/dms-bookinggw/src/main/sql/mysql/001_create.sql) in dms-booking/src/main/sql/mysql
  1. Also, create a database `specimendb` if you want to try the atom probe ingestion (please contact us for sample data)
  1. In STS, configure the !Tomcat instance
  1. Download [Apache Solr](http://lucene.apache.org/solr/) and put the apache-solr-`<version>`.war in the STS's Tomcat temporary folder as outlined in reference [R1](#R1.md).

> `*` **Note**: Tomcat, ActiveMQ and Solr are already in dms-install/src/main/resources/lib, but feel free to try a latest release if you want.

## Configure your profile ##

The system uses a bunch of java property files located in a configuration folder. A sample of those are in dms-install/src/main/resources/profiles/default.dir. Copy the contents of this folder into your home directory, under .ammrf/dms for example. Then follow this [guideline](DmsPropertyFiles.md) on how to configure those properties for your system.

## Launch in STS ##

Once the properties files are in place; one can run the application from within STS. Create a subdirectory where the property files are called _data_

  1. Start ActiveMQ
  1. Launch the worker using org.apache.camel.spring.Main and providing
    1. Arguments: -ac META-INF/applicationContext-worker**.xml
    1. VM arguments: -Ddms.config.home=`<`path-to-config-files`>` -Dsolr.solr.home=`<`path-to-data`>`
  1. Launch Tomcat, by adding the dms.config.home above as VM argument of the launcher.**

# References #
## [R1](https://code.google.com/p/ammrf-dms/source/detail?r=1) ##
(_Retrieved from [here](http://abtj.blogspot.com/2009/07/how-to-adddeploy-war-file-into-eclipse.html) 13/Jul/2011_)
In some scenarios, you wish to deploy a default web application into Eclipse Tomcat which is not part of your project, you may just add the war file into the below directory:

%your\_workspace%\.metadata\.plugins\org.eclipse.wst.server.core\tmpX\webapps

where tmpX could be tmp0, tmp1 etc