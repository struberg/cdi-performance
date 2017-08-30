/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.struct.cdi.performance;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import java.util.concurrent.TimeUnit;

import at.struct.cdi.performance.beans.ApplicationScopedHolder;
import at.struct.cdi.performance.beans.ClassInterceptedBean;
import at.struct.cdi.performance.beans.MethodInterceptedBean;
import at.struct.cdi.performance.beans.SimpleApplicationScopedBeanWithoutInterceptor;
import at.struct.cdi.performance.beans.SimpleRequestScopedBeanWithoutInterceptor;
import at.struct.cdi.performance.events.MySimpleEvent;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A few micro benchmarks for various CDI stuff.
 *
 * <p>The {@link #executeInParallel(String, Runnable)} first does a single run() on the
 * Runnable as warmup and then spawns up {@link #NUM_THREADS} parallel Threads to do
 * the real benchmarking.</p>
 *
 * <p>Please note that Java microbenchmarks always vary to some degree as garbage collection
 * and threading is not 100% reproducible. It nonetheless gives a decent hint about
 * the performance of CDI containers.</p>
 *
 * <p>Please always keep in mind that we are dealing with 1 billion invocations on our test
 * objects and in practice most of the performance is lost on the database, remote calls, etc.<br>
 * Those numbers are much more interesting if you heavily deal with ExpressionLanguage invocations
 * on CDI beans. Due to the tree nature of EL you can quickly get a few 10.000 invocations per page
 * hit. On big pages I measured up to 300.000 EL invocations on beans
 * (big h:dataTable with many columns). In those situations the performance of the CDI container
 * might make the difference between 100ms and a few seconds rendering time of your page.</p>
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
@Test(singleThreaded = true)
public class CdiPerformanceTest
{
    private static int NUM_THREADS = 100;
    private static int NUM_ITERATION=1000000;

    private volatile CdiContainer cdiContainer;

    @BeforeClass
    public void init()
    {
        cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();
        System.out.println("\n\n");
    }

    @AfterClass
    public void shutdown()
    {
        cdiContainer.shutdown();

        System.out.println("\n\n");
    }

    @Test(priority = 1)
    public void testApplicationScopedBeanPerformance() throws InterruptedException
    {
        final SimpleApplicationScopedBeanWithoutInterceptor underTest = getInstance(cdiContainer.getBeanManager(), SimpleApplicationScopedBeanWithoutInterceptor.class);

        executeInParallel("invocation on @ApplicationScoped bean", new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 0; i < NUM_ITERATION; i++)
                {
                    // this line does the actual bean invocation.
                    underTest.theMeaningOfLife();
                }
            }
        });
    }

    @Test(priority = 2)
    public void testApplicationScopedBeanInjectedIntoAnotherAppScopedBeanPerformance() throws InterruptedException
    {
        ApplicationScopedHolder applicationScopedHolder = getInstance(cdiContainer.getBeanManager(), ApplicationScopedHolder.class);
        final SimpleApplicationScopedBeanWithoutInterceptor underTest = applicationScopedHolder.getSimpleBeanWithoutInterceptor();

        executeInParallel("invocation on @ApplicationScoped bean which got injected into another @ApplicationScoped bean", new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 0; i < NUM_ITERATION; i++)
                {
                    // this line does the actual bean invocation.
                    underTest.theMeaningOfLife();
                }
            }
        });
    }

    @Test(priority = 3)
    public void testRequestScopedBeanPerformance() throws InterruptedException
    {
        final SimpleRequestScopedBeanWithoutInterceptor underTest = getInstance(cdiContainer.getBeanManager(), SimpleRequestScopedBeanWithoutInterceptor.class);
        final ContextControl contextControl = cdiContainer.getContextControl();
        contextControl.startContext(RequestScoped.class);
        Assert.assertEquals(underTest.theMeaningOfLife(), 42);
        contextControl.stopContext(RequestScoped.class);

        executeInParallel("invocation on @RequestScoped bean", new Runnable()
        {

            @Override
            public void run()
            {
                contextControl.startContext(RequestScoped.class);
                for (int i = 0; i < NUM_ITERATION; i++)
                {
                    // this line does the actual bean invocation.
                    underTest.theMeaningOfLife();
                }
                contextControl.stopContext(RequestScoped.class);
            }
        });
    }

    @Test(priority = 4)
    public void testApplicationScopeClassInterceptedPerformance() throws InterruptedException
    {
        final ClassInterceptedBean underTest = getInstance(cdiContainer.getBeanManager(), ClassInterceptedBean.class);

        executeInParallel("invocation on ClassIntercepted bean", new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 0; i < NUM_ITERATION; i++)
                {
                    // this line does the actual bean invocation.
                    underTest.getMeaningOfLife();
                }
            }
        });
    }

    @Test(priority = 4)
    public void testApplicationScopeMethodInterceptedPerformance() throws InterruptedException
    {
        final MethodInterceptedBean underTest = getInstance(cdiContainer.getBeanManager(), MethodInterceptedBean.class);

        executeInParallel("invocation on intercepted method of MethodInterceptedBean", new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 0; i < NUM_ITERATION; i++)
                {
                    // this line does the actual bean invocation.
                    underTest.getMeaningOfLife();
                }
            }
        });
    }

    @Test(priority = 5)
    public void testApplicationScopeNonInterceptedMethodPerformance() throws InterruptedException
    {
        final MethodInterceptedBean underTest = getInstance(cdiContainer.getBeanManager(), MethodInterceptedBean.class);

        executeInParallel("invocation on NON-intercepted method of MethodInterceptedBean", new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 0; i < NUM_ITERATION; i++)
                {
                    // this line does the actual bean invocation.
                    underTest.getMeaningOfHalfLife();
                }
            }
        });
    }

    @Test(priority = 6)
    public void testEventPerformance() throws Exception
    {
        final MySimpleEvent simpleEvent = new MySimpleEvent();
        final BeanManager beanManager = cdiContainer.getBeanManager();

        executeInParallel("event observer", new Runnable()
        {
            @Override
            public void run()
            {
                int iterations = NUM_ITERATION/5;
                for (int i = 0; i < iterations; i++)
                {
                    beanManager.fireEvent(simpleEvent);
                }
            }
        });
    }


    private void executeInParallel(String testName, Runnable runnable) throws InterruptedException
    {
        // do the warmup
        runnable.run();

        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++)
        {
            threads[i] = new Thread(runnable);
        }

        long start = System.nanoTime();

        for (int i = 0; i < NUM_THREADS; i++)
        {
            threads[i].start();
        }

        for (int i = 0; i < NUM_THREADS; i++)
        {
            threads[i].join();
        }
        long end = System.nanoTime();

        System.out.println("Test " + testName + " TOOK: " + TimeUnit.NANOSECONDS.toMillis(end - start) + " ms");
    }


    private <T> T getInstance(BeanManager bm, Class<T> clazz) {
        Bean<?> bean = bm.resolve(bm.getBeans(clazz));
        return (T) bm.getReference(bean, clazz, bm.createCreationalContext(bean));
    }
}
