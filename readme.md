# A small application to run a lightweight micro benchmark for CDI beans.

Currently it only fires up 100 concurrent Threads and does 10 Million invocations on a very simple bean.


## The numbers I've collected on my MBP15 so far:


Java8:
* OWB-1.2.6:         20.950 ms
* OWB-1.1.8:        288.030 ms
* Weld-1.1.9:       607.519 ms
* Weld-1.1.23:      619.518 ms
* Weld-2.2.5:       772.458 ms
* OWB-1.5.0:         13 ms (no this is NOT a hoax, it's due to our proxy caching [1]...)

All with Java8. 

Currently trying to run it on other boxes to rule out some misconfiguration 

[1] For @ApplicationScoped beans our proxies resolve the contextual instance only once. 
Thus you get the benefits of a Proxy (serializability, interceptors, decorators, cycle prevention, shield against scope differences)
for the costs of (almost) native invocation (Creating 'underTest' via new instead of the CDI bean will run the test in 8ms). 