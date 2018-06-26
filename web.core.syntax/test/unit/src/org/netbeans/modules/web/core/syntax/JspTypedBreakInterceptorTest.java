/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
