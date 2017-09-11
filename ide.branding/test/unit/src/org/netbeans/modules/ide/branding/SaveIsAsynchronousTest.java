/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ide.branding;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@RandomlyFails // NB-Core-Build #8077: Nothing in the registry expected:<null> but was:<displayname>
public class SaveIsAsynchronousTest extends NbTestCase {
    private ContextAwareAction saveAction;
    private Action saveAllAction;
    
    public SaveIsAsynchronousTest(String testName) {
        super(testName);
    }

    protected boolean runInEQ() {
        return true;
    }

    protected void setUp() throws Exception {
        saveAction = (ContextAwareAction) Actions.forID("System", "org.openide.actions.SaveAction");
        saveAllAction = Actions.forID("System", "org.openide.actions.SaveAllAction");
        assertNotNull("Save action found", saveAction);
        assertNotNull("Save All action found", saveAllAction);
        
        assertEquals("Nothing in the registry", null, MySavable.REGISTRY.lookup(MySavable.class));
    }
    
    public void testSaveAction() throws Exception {
        MySavable mySavable = new MySavable();
        Action a = saveAction.createContextAwareInstance(Lookups.singleton(mySavable));
        a.actionPerformed(new ActionEvent(this, 0, ""));
        mySavable.cdl.await();
        assertTrue("Handle save called", mySavable.called);
    }

    public void testSaveAllAction() throws Exception {
        MySavable mySavable = new MySavable();
        assertEquals("Is in the registry", mySavable, MySavable.REGISTRY.lookup(MySavable.class));
        saveAllAction.actionPerformed(new ActionEvent(this, 0, ""));
        mySavable.cdl.await();
        assertTrue("Handle save called", mySavable.called);
    }
    
    private static final class MySavable extends AbstractSavable {
        final CountDownLatch cdl = new CountDownLatch(1);
        volatile boolean called;

        public MySavable() {
            register();
        }
        
        
        
        protected String findDisplayName() {
            return "displayname";
        }

        protected void handleSave() throws IOException {
            called = true;
            assertFalse("No EDT", EventQueue.isDispatchThread());
            cdl.countDown();
        }

        public boolean equals(Object obj) {
            return obj == this;
        }

        public int hashCode() {
            return 555;
        }
    }
}
