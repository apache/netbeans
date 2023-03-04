/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.junit;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

/** Checks behaviour of linear speed suite in case the
 * average time per one unit it lower than 1ms.
 *
 * @author Jaroslav Tulach
 */
public class LinearSpeedTest extends NbTestCase {
    public LinearSpeedTest(String s) {
        super(s);
    }

    public static Test suite() {
        System.setProperty("ignore.random.failures", "false");
        final Test t = NbTestSuite.linearSpeedSuite(LinearSpeedTest.class, 2,2);

        class ThisHasToFail extends TestCase {
            
            public int countTestCases() {
                return 1;
            }

            public String getName() {
                return "LinearSpeedTest";
            }

            public void run(TestResult testResult) {
                TestResult r = new TestResult();
                t.run(r);
                
                int count = r.errorCount() + r.failureCount();
                if (count == 0) {
                    testResult.startTest(this);
                    testResult.addFailure(this, new AssertionFailedError("LinearSpeedTest must fail: " + count));
                    testResult.endTest(this);
                } else {
                    testResult.startTest(this);
                    testResult.endTest(this);
                }
            }
        }
        return new ThisHasToFail();
    }
    
    public void testBasicSizeOf1000() throws Exception {
        Thread.sleep(100);
    }
    public void testShouldBeTenTimesSloweverButIsJustTwice10000() throws Exception {
        Thread.sleep(200);
    }
}
