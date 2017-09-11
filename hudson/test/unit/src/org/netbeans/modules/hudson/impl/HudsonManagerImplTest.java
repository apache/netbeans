/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hudson.impl;

import java.util.Collection;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertNull;
import org.openide.util.Exceptions;

/**
 *
 * @author jhavlin
 */
public class HudsonManagerImplTest {

    /**
     * Clear manager's list of instances before each test.
     */
    @Before
    public void setUp() {
        Collection<HudsonInstanceImpl> instances
                = HudsonManagerImpl.getDefault().getInstances();
        for (HudsonInstanceImpl instance : instances) {
            HudsonManagerImpl.getDefault().removeInstance(instance);
        }
    }

    /**
     * Test for bug 245529 - ConcurrentModificationException from
     * HudsonManagerImpl.getInstances.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testSynchronization() throws InterruptedException {

        final Throwable[] thrown = new Throwable[1];
        Thread addInstancesThread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        delay();
                        HudsonManagerImpl.getDefault().addInstance(
                                HudsonInstanceImpl.createHudsonInstance(
                                        "TestHudsonInstance" + i,
                                        "http://testHudsonInstance" + i + "/",
                                        "0"));
                    } catch (Throwable e) {
                        thrown[0] = e;
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }, "AddHudsonInstances");
        addInstancesThread.start();

        for (int i = 0; i < 10; i++) {
            try {
                delay();
                HudsonManagerImpl.getDefault().getInstances();
            } catch (Throwable e) {
                thrown[0] = e;
                e.printStackTrace();
                break;
            }
        }

        addInstancesThread.join();
        assertNull("No exception should be thrown", thrown[0]);
    }

    private static void delay() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
