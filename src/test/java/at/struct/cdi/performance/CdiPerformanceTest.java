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
import at.struct.cdi.performance.beans.SimpleApplicationScopedBeanWithoutInterceptor;
import at.struct.cdi.performance.beans.SimpleRequestScopedBeanWithoutInterceptor;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A few micro benchmarks for various CDI stuff
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

    @Test
    public void testApplicationScopedBeanPerformance() throws InterruptedException
    {

        // we do this all in one method to make sure we don't kick off those methods in parallel

        final SimpleApplicationScopedBeanWithoutInterceptor underTest = getInstance(cdiContainer.getBeanManager(), SimpleApplicationScopedBeanWithoutInterceptor.class);
        underTest.theMeaningOfLife(); // warmup;

        executeInParallel("invocation on ApplicationScoped bean", new Runnable()
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

    @Test
    public void testApplicationScopedBeanInjectedIntoAnotherAppScopedBeanPerformance() throws InterruptedException

    {
        ApplicationScopedHolder applicationScopedHolder = getInstance(cdiContainer.getBeanManager(), ApplicationScopedHolder.class);
        final SimpleApplicationScopedBeanWithoutInterceptor underTest = applicationScopedHolder.getSimpleBeanWithoutInterceptor();
        underTest.theMeaningOfLife(); // warmup;

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

    @Test
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

    private void executeInParallel(String testName, Runnable runnable) throws InterruptedException
    {
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
