# Location #

If you inspect most of _applicationContext_ files, you will find snippets like the following
```
        ...
        <context:property-placeholder
                properties-ref="dmsProperties" ignore-unresolvable="true" />

        <util:properties id="dmsProperties"
                location="file://${dms.config.home}/dms.properties" />
        ...
```

The first tag configures in the spring context a resolver for properties. It has **ignore-unresolvable** on, because the application uses the ability of the [Spring IoC](http://static.springsource.org/spring/docs/3.0.x/reference/beans.html) container to load multiple XML definitions at once, and as the DMS has different property files for some, then all must ignore properties that don't belong to them.

The second one tells that the required property file for that XML context definition resides in **dms.config.home**, which is a system property. This is the location of all properties files.

In the following sections we describe all the property files and the meaning of each property.

# dms.properties #

  1. DB settings: setting for the DMS datasource. Use the default values and configure username and password
```
database.url=jdbc\:mysql\://localhost\:3306/dms
database.username=root
database.password=*****
database.driverClassName=com.mysql.jdbc.Driver
```

  1. Database connections, pool settings; defaults here should be ok.
> _c3p0.idleConnectionTestPeriod_: how often (in seconds) we should check for idle connections; _c3p0.maxIdleTime_: how long (is seconds) connection can be idle before been closed
```
c3p0.idleConnectionTestPeriod=1200 
c3p0.maxIdleTime=1800
```

  1. JMS broker properties. The ActiveMQ instance is secured with username and password. Provide the same username and password as the one for !ActiveMQ.
```
dms.broker.url=tcp://localhost:61616
dms.broker.username=dmsclient
dms.broker.password=*********
```

  1. Apache Solr configuration
> _solr.serverUrl_: URL of the Solr instance. As seen, one can run Apache Solr in a different machine. The default works for development as probably ok for production too; _executor.poolSize_: size of thread pool for Solr.
{{
solr.serverUrl=http\://localhost\:8080/apache-solr-1.4.1
executor.poolSize=10
}}

  1. Tunnel URL for the applet.
> _dms.applet.tunnelUrl_: the web application injects this property into the applet to enable communication to the dms-httptunnel. One should use the proper protocol and domain name in a production environment. Again, the default is ok for development. Note that the tunnel is a different web application and, in principle, can be installed in another server (although we haven't tested this scenario); _dms.webapp.key.filename_: also, the web app needs to encrypt the jobId to avoid security attacks (see [here](TunnelSecurity.md) for quick overview of the security measures in place), so provide here the path to the corresponding public key.
```
dms.applet.tunnelUrl=https://<your-domain>:8443/dms-httptunnel/tunnel/app
dms.webapp.key.filename=<path-to-public-key-1>
```

  1. Publishing reminder email settings, most self-explanatory. The schedule follows quartz URL syntax. The _dms.email.dms.url_ is used to provide a link to the mentioned item in the email.
```
dms.email.server.url=smtp://localhost
dms.email.from=dms@localhost
dms.email.subject=Please publish your datasets
dms.email.schedule=0+0+0+1+1/6+?
dms.email.dms.url=https://localhost:8443/dms-web/catalogue/view?url=
```

# bookinggw.properties #

  1. datasource properties. Very similar to the ones configured for the DMS above. It is worth noticing that the hibernate dialect can be configured as well.
```
bookinggw.database.driverClassName=com.mysql.jdbc.Driver
bookinggw.database.username=root
bookinggw.database.password=*****
bookinggw.database.url=jdbc:mysql://localhost/ammrf
bookinggw.hibernate.dialect=org.hibernate.dialect.MySQL5Dialect
bookinggw.hibernate.showSql=true
```

  1. Database connections pool settings: same as for DMS database
```
bookinggw.c3p0.idleConnectionTestPeriod=1200 
bookinggw.c3p0.maxIdleTime=1800
```

# dms\_routing.properties #

The DMS service uses a simple routing implementation where workers are configured in this property file. Each worker is configured by the following pattern:
```
dms.worker.<num>=<queue-name>
dms.worker.<num>.<protocol>=<server-pattern>
```
where `<num>` starts at 1 and goes up in consecutive numbers; and `<protocol>` is a protocol identifier in that worker. Currently implemented are **local**, **ftp**, **hdd** (see [transport protocols](GenericArchitecture#Implemented_transport_protocols.md)). Server pattern allows even finer control if required. It is converted into a regular expression by replacing `*` with `.*`, so patterns like **`*`.domain.edu.au|`*`.other.edu.au** work as expected. A request can be sent to this worker if the server parameter matches the regular expression.
The first property dms.worker.`<num>` is the all important queue name, which must match one in the the worker. The default configuration works ok for development environment.
```
dms.worker.1=wn.request.node
dms.worker.1.ftp=*
dms.worker.1.hdd=*
dms.worker.1.local=*
```

# worker.properties #

Configures various worker settings, including the embedded transport protocols.

  1. broker configuration, similar to the dms.
```
dms.broker.url=tcp://localhost:61616
dms.broker.username=dmsclient
dms.broker.password=*********
```

  1. queue associated with worker instance. If you deploy multiple workers, these (across all workers) must match the queues configured in **dms\_routing.properties**
```
dms.wn.queue=wn.request.node
```

  1. pool configuration for transport connections;  _dms.wn.pool.minEvictableIdleTimeMillis_ : elapsed time when an idle connection inside the pool is scheduled for closing; _dms.wn.pool.timeBetweenEvictionRunsMillis_ : how often the eviction task is run (usually lot less than the preious one); both values in milliseconds.
```
dms.wn.pool.minEvictableIdleTimeMillis=300000
dms.wn.pool.timeBetweenEvictionRunsMillis=60000
```
> Note that in the case of FTP connections, the FTP server on the other end may close the connection before the timeout. The worker may attempt a reconnection on its own if the connection credentials are still known to the worker (see next)

  1. Active Connections cache configuration; configures for how long the worker can recall connections credentials and therefore will be able to reconnect. These parameters are the ones that clients of the worker see as timeouts for idle open connections. Also, there is a limit _maxElementsInMemory_ on this cache that can be extended depending on the number of concurrent users.
```
activeConnectionsCache.maxElementsInMemory=500
activeConnectionsCache.timeToIdleSeconds=600
```

> The following set of properties configure the transport factory beans.

  1. the tunnel URL for the HDD; default should work on development. This one, if the worker is deployed in the same machine can be localhost, otherwise needs to point to the location of the tunnel web app; _dms.wn.key.filename_ path to public key to provide security in the tunnel.
```
dms.wn.tunnelUrl=https://localhost:8443/dms-httptunnel/tunnel/hdd
dms.wn.key.filename=<path-to-public-key-1>
```

  1. the local protocol: _dms.wn.localRootPath_, root of folder hierarchy; top level folders there are treated as _server_ names, make sure this folder exists and has right permissions; _dms.wn.local.username_ and _dms.wn.local.password_ provide additional security, as indicate username and password that need to be provided in the open connection request;
```
dms.wn.localRootPath=<path-to-root>
dms.wn.local.username=admin
dms.wn.local.password=password
```

  1. MicroCT file poller: the Skyscan (or Micro-CT) trigger scans the source folder every _dms.wn.filepoller.periodMilliseconds_ looking for the .log file; when found it waits additional _dms.wn.filepoller.delayMilliseconds_ before starting the ingesiton
```
dms.wn.filepoller.periodMilliseconds=60000
dms.wn.filepoller.delayMilliseconds=10000
```