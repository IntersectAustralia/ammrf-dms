# Introduction #

The DMS can be _wired_ in different ways. This page explains briefly the current ACMM setup, providing specifics in regards to deployment and current wiring of beans.

# Overall picture #

![http://ammrf-dms.googlecode.com/svn/wiki/images/dms-setup.png](http://ammrf-dms.googlecode.com/svn/wiki/images/dms-setup.png)

The DMS is deployed in one machine, running the following java processes:

  1. ActiveMQ, the JMS broker
  1. Worker process
    1. Transport implementations for **local**, **ftp** and **hdd**
    1. Triggers and harvesters for all instruments (dms-instrument)
  1. Tomcat, with three web applications
    1. Web application (dms-web)
      1. DMS service (dms-service)
      1. Booking gateway (dms-booking)
    1. Http tunnel
    1. Apache solr

# Changing the configuration #

## Relocating components ##

By modifying basic property files, one can achieve easily the following:

  * Scale horizontally by providing more workers. One will need to change dms\_routing.properties and the corresponding properties for each worker.

  * Deploy tunnel and/or solr in another server by relocating the war. One issue which hasn't been tested though is when the applet points to a different server.

  * Deploy multiple brokers, one in the server and one _slave_ for the worker. This configuration allows to deploy the worker machine inside an Intranet while the main server lives outside.