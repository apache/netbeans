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
package org.netbeans.modules.progress.spi;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.ui.NbProgressBar;
import org.openide.util.Cancellable;

/**
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 */
public class ProgressAPICompatTest extends NbTestCase {
    public ProgressAPICompatTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(ProgressAPICompatTest.class, null, null);
    }
    
    static class C implements Cancellable {
        @Override
        public boolean cancel() {
            return false;
        }
    }
    
    static class A extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
    
    /**
     * Check that if a Swing component is extracted, the progress
     * reports 'customPlaced' = true.
     * @throws Exception 
     */
    public void testHandleIsCustomPlaced() throws Exception {
        C c = new C();
        A a = new A();
        Constructor ctor = InternalHandle.class.getConstructor(
                String.class,
                Cancellable.class,
                Boolean.TYPE,
                Action.class
            );
        InternalHandle h = (InternalHandle)ctor.newInstance("Foobar", c, true, a);
        ProgressHandle p = h.createProgressHandle();
        
        Method m = InternalHandle.class.getMethod("extractComponent");
        JComponent comp = (JComponent)m.invoke(h);
        
        assertTrue(h.isCustomPlaced());
    }
    
    public void testExtractComponent() throws Exception {
        Method m = InternalHandle.class.getMethod("extractComponent");
        assertEquals("Returns JComponent", JComponent.class, m.getReturnType());
        InternalHandle ih = new InternalHandle("foo", null, true);
        JComponent jl = (JComponent)m.invoke(ih);
        assertTrue("NbProgressBar created", jl instanceof NbProgressBar);
    }

    public void testExtractMainLabel() throws Exception {
        Method m = InternalHandle.class.getMethod("extractMainLabel");
        assertEquals("Returns JLabel", JLabel.class, m.getReturnType());
        
        InternalHandle ih = new InternalHandle("foo", null, true);
        JLabel jl = (JLabel)m.invoke(ih);
        ih.createProgressHandle().start(1);
        // label gets initialized in AWT, wait for the pending EDT queue items
        SwingUtilities.invokeAndWait(new Runnable() { public void run() {}});
        assertEquals("foo", jl.getText());
    }

    public void testExtractDetailLabel() throws Exception {
        Method m = InternalHandle.class.getMethod("extractDetailLabel");
        assertEquals("Returns JLabel", JLabel.class, m.getReturnType());
        
        InternalHandle ih = new InternalHandle("foo", null, true);
        final JLabel jl = (JLabel)m.invoke(ih);
        final ProgressHandle ph = ih.createProgressHandle();
        ph.start(1);
        ph.progress("bar", 1);
        // the progress is not updated immediately; wait a little (no suitable event listener);
        // label gets initialized in AWT, wait for the pending EDT queue items

        Thread.sleep(1000);
        SwingUtilities.invokeAndWait(new Runnable() { public void run() {
            assertEquals("bar", jl.getText());
        }});
    }
    
    public void testControllerVisualComponent() throws Exception {
        Controller ctrl = new Controller(null);
        Method m = ctrl.getClass().getMethod("getVisualComponent");
        assertEquals(Component.class, m.getReturnType());
    }
    
    public void testControllerTimer() throws Exception {
        Controller ctrl = new Controller(null);
        Field f = ctrl.getClass().getSuperclass().getDeclaredField("timer");
        f.setAccessible(true);
        assertNotNull(f.get(ctrl));
    }
}
