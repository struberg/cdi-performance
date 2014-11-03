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
package at.struct.cdi.performance.interceptor;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * Interceptor which does just route straight through the intercepted instance.
 * this interceptor is just to bench the performance impact of the interceptor
 * framework of the CDI container itself.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
@Interceptor
@NopIntercepted
public class NopInterceptor
{

    @AroundInvoke
    public Object doNothing(InvocationContext ic) throws Exception
    {
        return ic.proceed();
    }
}
