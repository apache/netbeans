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

import java.beans.PropertyChangeSupport;
import java.util.Collections;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class MockPropertyChangeListenerTest extends TestCase {

    public MockPropertyChangeListenerTest(String n) {
        super(n);
    }

    Object source;
    PropertyChangeSupport pcs;
    MockPropertyChangeListener l;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        source = new Object();
        pcs = new PropertyChangeSupport(source);
        l = new MockPropertyChangeListener();
        pcs.addPropertyChangeListener(l);
    }

    // XXX test expect

    public void testBasicUsage() throws Exception {
        l.assertEvents();
        try {
            l.assertEvents("whatever");
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.firePropertyChange("foo", null, null);
        l.assertEvents("foo");
        try {
            l.assertEvents("foo");
            assert false;
        } catch (AssertionFailedError e) {}
        l.assertEventCount(0);
        pcs.firePropertyChange("bar", null, null);
        pcs.firePropertyChange("baz", null, null);
        l.assertEventCount(2);
        try {
            l.assertEventCount(2);
            assert false;
        } catch (AssertionFailedError e) {}
        assertEquals(0, l.allEvents().size());
        pcs.firePropertyChange("bar", null, null);
        pcs.firePropertyChange("baz", null, null);
        assertEquals(2, l.allEvents().size());
        assertEquals(0, l.allEvents().size());
        pcs.firePropertyChange("foo", "old", "new");
        l.assertEventsAndValues(null, Collections.singletonMap("foo", "new"));
        pcs.firePropertyChange("foo", "old2", "new2");
        l.assertEventsAndValues(Collections.singletonMap("foo", "old2"), Collections.singletonMap("foo", "new2"));
        try {
            l.assertEventsAndValues(null, Collections.singletonMap("foo", "new2"));
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.firePropertyChange("foo", null, null);
        l.reset();
        l.assertEvents();
        pcs.firePropertyChange("x", null, null);
        try {
            l.assertEvents();
            assert false;
        } catch (AssertionFailedError e) {}
        l.assertEvents();
    }

    public void testMessages() throws Exception {
        pcs.firePropertyChange("foo", null, null);
        try {
            l.assertEvents();
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.firePropertyChange("foo", null, null);
        try {
            l.msg("stuff").assertEvents();
            assert false;
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().contains("stuff"));
        }
        pcs.firePropertyChange("foo", null, null);
        try {
            l.assertEvents();
            assert false;
        } catch (AssertionFailedError e) {
            assertFalse(e.getMessage().contains("stuff"));
        }
    }

    public void testPropertyNameFiltering() throws Exception {
        l.ignore("irrelevant");
        l.blacklist("bad", "worse");
        pcs.firePropertyChange("relevant", null, null);
        l.assertEvents("relevant");
        pcs.firePropertyChange("irrelevant", null, null);
        l.assertEvents();
        try {
            pcs.firePropertyChange("bad", null, null);
            assert false;
        } catch (AssertionFailedError e) {}
        try {
            pcs.firePropertyChange("worse", null, null);
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.removePropertyChangeListener(l);
        l = new MockPropertyChangeListener("expected1", "expected2");
        pcs.addPropertyChangeListener(l);
        l.ignore("irrelevant");
        pcs.firePropertyChange("expected1", null, null);
        pcs.firePropertyChange("expected2", null, null);
        l.assertEvents("expected1", "expected2");
        pcs.firePropertyChange("irrelevant", null, null);
        l.assertEvents();
        try {
            pcs.firePropertyChange("other", null, null);
            assert false;
        } catch (AssertionFailedError e) {}
    }

}
