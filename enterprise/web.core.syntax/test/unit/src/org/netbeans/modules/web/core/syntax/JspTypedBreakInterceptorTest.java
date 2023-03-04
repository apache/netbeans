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
package org.netbeans.modules.web.core.syntax;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.test.web.core.syntax.TestBase2;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class JspTypedBreakInterceptorTest extends TestBase2 {

    public JspTypedBreakInterceptorTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testEndTag1() throws Exception {
        insertBreak("<a>^</a>", "<a>\n    ^\n</a>");
    }

    public void testEndTag2() throws Exception {
        insertBreak("<jsp:body>^</jsp:body>", "<jsp:body>\n    ^\n</jsp:body>");
    }

    public void testAWTWait() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                Source src = Source.create(getTestFile("testSingleJsps/JSPDeclaration.java"));
                try {
                    ParserManager.parse(Collections.singleton(src), new UserTask() {

                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            latch.countDown();
                            Thread.sleep(30000);
                        }
                    });
                } catch (ParseException ex) {
                    throw new RuntimeException();
                }
            }
        });

        latch.await();
        System.out.println("Testing");
        System.out.flush();
        long start = System.nanoTime();
        insertBreak("<a>^</a>", "<a>\n    ^\n</a>");
        long time = (((System.nanoTime() - start)) / 1000000);
        System.out.println("Finished in " + time + " ms");
        assertTrue(time < 5000);
    }
}
