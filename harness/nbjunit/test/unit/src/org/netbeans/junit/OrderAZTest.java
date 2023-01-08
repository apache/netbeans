/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.junit;

import java.lang.reflect.Method;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.junit.Test;

/** Check the a-z behaviour.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class OrderAZTest {
    static {
        System.setProperty("NbTestCase.order", "a-z");
    }
    
    @Test public void shuffleTest() throws ClassNotFoundException {
        Class<?> load = Class.forName("org.netbeans.junit.OrderHid");
        TestSuite ts = new TestSuite(load);
        TestResult res = junit.textui.TestRunner.run(ts);
        Assert.assertEquals("No errors", 0, res.errorCount());
        Assert.assertEquals("No failures: " + dumpMethod(load), 0, res.failureCount());
    }
    
    private static String dumpMethod(Class<?> c) {
        StringBuilder sb = new StringBuilder();
        for (Method m : c.getDeclaredMethods()) {
            sb.append(m.getName()).append('\n');
        }
        return sb.toString();
    }
}
