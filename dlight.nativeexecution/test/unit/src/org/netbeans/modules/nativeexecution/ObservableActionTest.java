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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution;

import org.netbeans.modules.nativeexecution.support.ObservableAction;
import org.netbeans.modules.nativeexecution.support.ObservableActionListener;
import javax.swing.Action;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class ObservableActionTest {

    public ObservableActionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of invokeAndWait method, of class ObservableAction.
     */
    @Test
    public void doTest() {
        try {
            System.out.println("invokeAndWait"); // NOI18N
            ObservableAction<Integer> action = new ObservableAction<Integer>("My Action") { // NOI18N

                @Override
                protected Integer performAction() {
                    System.out.println("Performed!"); // NOI18N
                    return 10;
                }
            };
            action.addObservableActionListener(new ObservableActionListener<Integer>() {

                public void actionStarted(Action source) {
                    System.out.println("Started!"); // NOI18N
                }

                public void actionCompleted(Action source, Integer result) {
                    System.out.println("Finished ! " + result); // NOI18N
                }
            });

            Thread.sleep(100);
//            action.invokeAndWait();

            action.actionPerformed(null);
            Thread.sleep(100);
            action.actionPerformed(null);
            action.actionPerformed(null);
            action.actionPerformed(null);
            try {
//            Thread.sleep(100);
//            action.actionPerformed(null);
//            Thread.sleep(100);
//            assertEquals(new Integer(10), action.getLastResult());

//                assertEquals(new Integer(10), action.actionPerformed(null));
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
