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

package org.openide.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ContextActionInjectTest extends NbTestCase {

    public ContextActionInjectTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public static final class Context implements ActionListener {
        private final int context;
        
        public Context(Integer context) {
            this.context = context;
        }

        static int cnt;

        public void actionPerformed(ActionEvent e) {
            cnt += context;
        }

    }

    public void testContextAction() throws Exception {
        Context.cnt = 0;
        
        FileObject fo = FileUtil.getConfigFile(
            "actions/support/test/testInjectContext.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(10);

        assertEquals("Number lover!", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 10, Context.cnt);

        ic.remove(10);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Global Action stays same", 10, Context.cnt);
    }
    
    private static final InstanceContent contextI = new InstanceContent();
    private static final Lookup context = new AbstractLookup(contextI);
    public static Lookup context() {
        return context;
    }
    
    public void testOwnContextAction() throws Exception {
        MultiContext.cnt = 0;
        
        FileObject fo = FileUtil.getConfigFile(
            "actions/support/test/testOwnContext.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is action", obj instanceof Action);
        assertFalse("It is not context aware action: " + obj, obj instanceof ContextAwareAction);
        Action a = (Action)obj;

        InstanceContent ic = contextI;
        ic.add(10);

        assertEquals("Number lover!", a.getValue(Action.NAME));
        a.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 10, MultiContext.cnt);

        ic.remove(10);
        a.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Global Action stays same", 10, MultiContext.cnt);
    }

    public static final class MultiContext implements ActionListener {
        private final List<Number> context;

        public MultiContext(List<Number> context) {
            this.context = context;
        }

        static int cnt;

        public void actionPerformed(ActionEvent e) {
            for (Number n : context) {
                cnt += n.intValue();
            }
        }

    }

    public void testMultiContextAction() throws Exception {
        MultiContext.cnt = 0;
        
        FileObject fo = FileUtil.getConfigFile(
            "actions/support/test/testInjectContextMulti.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(10);
        ic.add(3L);

        assertEquals("Number lover!", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 13, MultiContext.cnt);

        ic.remove(10);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Adds 3", 16, MultiContext.cnt);

        ic.remove(3L);
        assertFalse("It is disabled", clone.isEnabled());
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("No change", 16, MultiContext.cnt);
    }

   public static final class LookupContext extends AbstractAction 
   implements ContextAwareAction {
        private final Lookup context;

        public LookupContext() {
            this(Lookup.EMPTY);
        }

        private LookupContext(Lookup context) {
            this.context = context;
        }

        static int cnt;

        public void actionPerformed(ActionEvent e) {
            for (Number n : context.lookupAll(Number.class)) {
                cnt += n.intValue();
            }
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            return new LookupContext(actionContext);
        }
    }

    public void testMultiContextActionLookup() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "actions/support/test/testInjectContextLookup.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(10);
        ic.add(3L);

        assertEquals("Number lover!", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 13, LookupContext.cnt);

        ic.remove(10);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Adds 3", 16, LookupContext.cnt);

        ic.remove(3L);
        assertFalse("It is disabled", clone.isEnabled());
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("No change", 16, LookupContext.cnt);
    }
}
