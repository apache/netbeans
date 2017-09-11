/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openide.util.test;

import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.ChangeSupport;

public class MockChangeListenerTest extends NbTestCase {

    public MockChangeListenerTest(String n) {
        super(n);
    }

    Object source;
    ChangeSupport cs;
    MockChangeListener l;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        source = new Object();
        cs = new ChangeSupport(source);
        l = new MockChangeListener();
        cs.addChangeListener(l);
    }

    public void testBasicUsage() throws Exception {
        l.assertNoEvents();
        l.assertEventCount(0);
        try {
            l.assertEvent();
            assert false;
        } catch (AssertionFailedError e) {}
        try {
            l.assertEventCount(1);
            assert false;
        } catch (AssertionFailedError e) {}
        cs.fireChange();
        l.assertEvent();
        l.assertNoEvents();
        l.assertEventCount(0);
        cs.fireChange();
        cs.fireChange();
        l.assertEventCount(2);
        cs.fireChange();
        l.assertEvent();
        l.assertNoEvents();
        l.assertNoEvents();
        cs.fireChange();
        l.reset();
        l.assertNoEvents();
        cs.fireChange();
        cs.fireChange();
        assertEquals(2, l.allEvents().size());
    }

    public void testMessages() throws Exception {
        try {
            l.assertEvent();
            assert false;
        } catch (AssertionFailedError e) {}
        try {
            l.msg("stuff").assertEvent();
            assert false;
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().contains("stuff"));
        }
        try {
            l.assertEvent();
            assert false;
        } catch (AssertionFailedError e) {
            assertFalse(String.valueOf(e.getMessage()).contains("stuff"));
        }
    }

    @RandomlyFails // NB-Core-Build #8154
    public void testExpect() throws Exception {
        l.expectNoEvents(1000);
        cs.fireChange();
        l.expectEvent(1000);
        l.assertNoEvents();
        new Thread() {
            @Override public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException x) {
                    assert false;
                }
                cs.fireChange();
            }
        }.start();
        try {
            l.expectEvent(1000);
            assert false;
        } catch (AssertionFailedError e) {}
        l.expectEvent(2000);
        l.assertNoEvents();
        l.expectNoEvents(1000);
    }

}
