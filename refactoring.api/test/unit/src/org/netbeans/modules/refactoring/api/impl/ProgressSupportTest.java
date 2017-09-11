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

package org.netbeans.modules.refactoring.api.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import static org.junit.Assert.*;
import org.netbeans.modules.refactoring.api.ProgressListener;

/**
 *
 * @author Jan Pokorsky
 */
public class ProgressSupportTest {

    public ProgressSupportTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDeterministicProgress() {
        System.out.println("testDeterministicProgress");
        PL listener = new PL();
        ProgressSupport instance = new ProgressSupport();
        instance.addProgressListener(listener);

        // start indeterminate progress
        instance.fireProgressListenerStart(this,ProgressEvent.START, 4);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.START, listener.event.getEventId());
        assertEquals("progress", 4, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 0, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 1, listener.event.getCount());
        listener.reset();

        // step to 3
        instance.fireProgressListenerStep(this, 3);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 3, listener.event.getCount());
        listener.reset();

        // stop
        instance.fireProgressListenerStop(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STOP, listener.event.getEventId());
    }

    @Test
    public void testIndeterministicProgress() {
        System.out.println("testIndeterministicProgress");
        PL listener = new PL();
        ProgressSupport instance = new ProgressSupport();
        instance.addProgressListener(listener);

        // start indeterminate progress
        instance.fireProgressListenerStart(this,ProgressEvent.START, -1);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.START, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // stop
        instance.fireProgressListenerStop(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STOP, listener.event.getEventId());
    }

    @Test
    public void testIndeterministicToDeterministicProgress1() {
        System.out.println("testIndeterministicToDeterministicProgress1");
        PL listener = new PL();
        ProgressSupport instance = new ProgressSupport();
        instance.addProgressListener(listener);

        // start indeterminate progress
        instance.fireProgressListenerStart(this,ProgressEvent.START, -1);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.START, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // switch to deterministic progress of size 10
        instance.fireProgressListenerStep(this, 10);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 10, listener.event.getCount());
        listener.reset();
        
        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 0, listener.event.getCount());
        listener.reset();

        // stop
        instance.fireProgressListenerStop(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STOP, listener.event.getEventId());
    }

    @Test
    public void testIndeterministicToDeterministicProgress2() {
        System.out.println("testIndeterministicToDeterministicProgress2");
        PL listener = new PL();
        ProgressSupport instance = new ProgressSupport();
        instance.addProgressListener(listener);

        // start indeterminate progress
        instance.fireProgressListenerStart(this,ProgressEvent.START, -1);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.START, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", -1, listener.event.getCount());
        listener.reset();

        // switch to deterministic progress of size 10
        instance.fireProgressListenerStep(this, 10);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 10, listener.event.getCount());
        listener.reset();

        // step
        instance.fireProgressListenerStep(this, 5);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STEP, listener.event.getEventId());
        assertEquals("progress", 5, listener.event.getCount());
        listener.reset();

        // stop
        instance.fireProgressListenerStop(this);
        assertNotNull(listener.event);
        assertEquals("event ID", ProgressEvent.STOP, listener.event.getEventId());
    }
    
    private static class PL implements ProgressListener {
        private ProgressEvent event;

        @Override
        public void start(ProgressEvent event) {
            this.event = event;
        }

        @Override
        public void step(ProgressEvent event) {
            this.event = event;
        }

        @Override
        public void stop(ProgressEvent event) {
            this.event = event;
        }
        
        public void reset() {
            this.event = null;
        }
    }

}
