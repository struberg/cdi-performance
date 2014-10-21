# A small application to run a lightweight micro benchmark for CDI beans.

Currently it only fires up 100 concurrent Threads and does 10 Million invocations on a very simple bean.


## The numbers I've collected on my MBP15 so far:


Java8:
* OWB-1.1.8:           currently collecting  ms
* OWB-1.2.6:               5270 ms
* OWB-1.5.0:                 92 ms (no this is NOT a hoax, it's due to our proxy caching [1]...)
* Weld-1.1.9.Final     currently collecting  ms
* Weld-1.1.23.Final:   currently collecting  ms
* Weld-2.2.5.Final:    currently collecting  ms

All with Java8. 

Currently trying to run it on other boxes to rule out some misconfiguration 


# disk footprint

I've also collected numbers about the size of all the jars needed:

### OpenWebBeans-1.2.6

<pre>
$> mvn clean dependency:copy-dependencies -DincludeScope=compile
$> du -hs target/dependency/
   952K target/dependency/
</pre>

One can see that the flexible plugin structure of Apache OpenWebBeans really pays off.

### Weld-2.2.5.Final

<pre>
$> mvn clean dependency:copy-dependencies -DincludeScope=compile -PWeld -Dweld.version=2.2.5.Final
$> du -hs target/dependency/
9,7M    target/dependency/
</pre>

I think I need to get in touch with Weld folks because most probably not all the parts are really needed.


[1] For @ApplicationScoped beans our proxies resolve the contextual instance only once. 
Thus you get the benefits of a Proxy (serializability, interceptors, decorators, cycle prevention, shield against scope differences)
for the costs of (almost) native invocation (Creating 'underTest' via new instead of the CDI bean will run the test in 8ms). 