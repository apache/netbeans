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
