# A small application to run a lightweight micro benchmark for CDI beans.

Currently it only fires up 100 concurrent Threads and does 10 Million invocations on a very simple bean.


## The numbers I've collected on my MBP15 so far:

All tests are run with Java7.

Apache Maven 3.2.1 (ea8b2b07643dbb1b84b6d16e1f08391b666bc1e9; 2014-02-14T18:37:52+01:00)
Maven home: /opt/apache/maven
Java version: 1.7.0_51, vendor: Oracle Corporation
Java home: /Library/Java/JavaVirtualMachines/jdk1.7.0_51.jdk/Contents/Home/jre
Default locale: de_DE, platform encoding: UTF-8
OS name: "mac os x", version: "10.10", arch: "x86_64", family: "mac"
 
### OWB-1.2.6
$> mvn clean install
Test invocation on @ApplicationScoped bean which got injected into another @ApplicationScoped bean TOOK: 13 ms
Test invocation on ApplicationScoped bean TOOK: 20 ms
Test invocation on @RequestScoped bean TOOK: 648 ms

### OWB-1.2.0
$> mvn clean install -Dowb.version=1.2.0
Test invocation on @ApplicationScoped bean which got injected into another @ApplicationScoped bean TOOK: 40 ms
Test invocation on ApplicationScoped bean TOOK: 18 ms
Test invocation on @RequestScoped bean TOOK: 918 ms

### OWB-1.1.6
$> mvn clean install -Dowb.version=1.1.6
Test invocation on @ApplicationScoped bean which got injected into another @ApplicationScoped bean TOOK: 7138 ms
Test invocation on ApplicationScoped bean TOOK: 7008 ms
Test invocation on @RequestScoped bean TOOK: 11979 ms

### OWB-1.1.8
$> mvn clean install -Dowb.version=1.1.8
Test invocation on @ApplicationScoped bean which got injected into another @ApplicationScoped bean TOOK: 7192 ms
Test invocation on ApplicationScoped bean TOOK: 6914 ms
Test invocation on @RequestScoped bean TOOK: 12111 ms

### OWB-1.5.0-SNAPSHOT (CDI-1.2)
$> mvn clean install -POWB15 -Dowb.version=1.5.0-SNAPSHOT
Test invocation on @ApplicationScoped bean which got injected into another @ApplicationScoped bean TOOK: 23 ms
Test invocation on ApplicationScoped bean TOOK: 18 ms
Test invocation on @RequestScoped bean TOOK: 625 ms

### Weld-1.1.23.Final
$> mvn clean install -PWeld -Dweld.version=1.1.23.Final
Test invocation on @ApplicationScoped bean which got injected into another @ApplicationScoped bean TOOK: 16258 ms
Test invocation on ApplicationScoped bean TOOK: 15447 ms
Test invocation on @RequestScoped bean TOOK: 2592 ms

### Weld-2.2.6.Final
$> mvn clean install -PWeld
Test invocation on @ApplicationScoped bean which got injected into another @ApplicationScoped bean TOOK: 17751 ms
Test invocation on ApplicationScoped bean TOOK: 16916 ms
Test invocation on @RequestScoped bean TOOK: 2593 ms

### Weld-2.2.5.Final
$> mvn clean install -PWeld -Dweld.version=2.2.5.Final
Test invocation on @ApplicationScoped bean which got injected into another @ApplicationScoped bean TOOK: 18408 ms
Test invocation on ApplicationScoped bean TOOK: 17812 ms
Test invocation on @RequestScoped bean TOOK: 2552 ms



# disk footprint

I've also collected numbers about the size of all the jars needed:

### OpenWebBeans-1.2.6

<pre>
$> mvn clean dependency:copy-dependencies -DincludeScope=compile
$> du -hs target/dependency/
   952K target/dependency/
</pre>

One can see that the flexible plugin structure of Apache OpenWebBeans really pays off.

### Weld-2.2.6.Final

<pre>
$> mvn clean dependency:copy-dependencies -DincludeScope=compile -PWeld
$> du -hs target/dependency/
3.8M    target/dependency/
</pre>


[1] For @ApplicationScoped beans our proxies resolve the contextual instance only once. 
Thus you get the benefits of a Proxy (serializability, interceptors, decorators, cycle prevention, shield against scope differences)
for the costs of (almost) native invocation (Creating 'underTest' via new instead of the CDI bean will run the test in 8ms). 