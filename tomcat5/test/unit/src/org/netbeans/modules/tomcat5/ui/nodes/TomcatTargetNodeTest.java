/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.tomcat5.ui.nodes;

import org.netbeans.modules.tomcat5.ui.nodes.TomcatTargetNode;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class TomcatTargetNodeTest extends NbTestCase {

    private static RequestProcessor processor = new RequestProcessor("Deadlock Test", 2);

    public TomcatTargetNodeTest(String name) {
        super(name);
    }

    public void testDeadlock191535() {
        final CountDownLatch latch = new CountDownLatch(2);
        final CountDownLatch finish = new CountDownLatch(1);

        Runnable blocking = new Runnable() {

            @Override
            public void run() {
                Children.MUTEX.postWriteRequest(new Runnable() {
                    public void run() { 
                        latch.countDown();
                        try {
                            latch.await();
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        };

        Runnable testing = new Runnable() {

            @Override
            public void run() {
                latch.countDown();
                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                try {
                    TomcatTargetNode node = new TomcatTargetNode(Lookup.EMPTY);
                } finally {
                    finish.countDown();
                }
            }
        };
        
        processor.post(blocking);
        processor.post(testing);

        try {
            assertTrue("Deadlock detected", finish.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            fail("Test interrupted"); 
        }
        
    }
}
