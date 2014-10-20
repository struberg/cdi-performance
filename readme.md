A small application to run a lightweight micro benchmark for CDI beans.

Currently it only fires up 100 concurrent Threads and does 10 Million invocations on a very simple bean.


The numbers I've collected on my MBP15 so far:


Java8:
OWB-1.2.6: 		 20.950 ms
OWB-1.1.8:		288.030 ms
Weld-1.1.9:		607.519 ms

Currently trying to run it on other boxes to rule out some misconfiguration 
