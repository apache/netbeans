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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.awt;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/** Verifies asynchronous aspects of actions systems are close to the
 * original behaviour of SystemAction one.
 * Taken from org.openide.util.actions.AsynchronousTest
 * @author Jaroslav Tulach
 */
public class AsynchronousTest extends NbTestCase {

    private CharSequence err;
    
    public AsynchronousTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 5000;
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void setUp() {
        err = Log.enable("", Level.WARNING);
        AC.finished = false;
    }
    
    public void testExecutionOfActionsThatDoesNotDefineAsynchronousIsSynchronousNoWarningIsPrinted() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/none.instance").getAttribute("instanceCreate");
        
        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            assertTrue("The synchronous action is finished immediatelly", AC.finished);
        }
        
        if (err.length() > 0) {
            fail("There should be no warning about missing asynchronous: " + err);
        }
    }
    
    public void testExecutionCanBeAsynchronous() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/true.instance").getAttribute("instanceCreate");
        
        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Shall Not be finished yet", AC.finished);
            AsynchronousTest.class.wait();
            assertTrue("The asynchronous action is finished", AC.finished);
        }
        
        if (err.length() > 0) {
            fail("No warning about the class: " + err);
        }
    }

    public void testExecutionCanBeAsynchronousForAlways() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/true-always.instance").getAttribute("instanceCreate");

        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Shall Not be finished yet", AC.finished);
            AsynchronousTest.class.wait();
            assertTrue("The asynchronous action is finished", AC.finished);
        }

        if (err.length() > 0) {
            fail("No warning about the class: " + err);
        }
    }
    public void testExecutionCanBeSynchronous() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/false.instance").getAttribute("instanceCreate");
        
        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            assertTrue("The synchronous action is finished immediatelly", AC.finished);
        }
        
        if (err.length() > 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public void testExecutionCanBeForcedToBeSynchronous() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/true.instance").getAttribute("instanceCreate");
        
        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, "waitFinished"));
            assertTrue("When asked for synchronous the action is finished immediatelly", AC.finished);
        }
        
        if (err.length() > 0) {
            fail("No warning about the class: " + err);
        }
    }
    
    public void testExecutionCanBeAsynchronousForContext() throws Exception {
        Action action = (Action)FileUtil.getConfigFile("actions/async/true-context.instance").getAttribute("instanceCreate");

        synchronized (AsynchronousTest.class) {
            action.actionPerformed(new ActionEvent(this, 0, ""));
            Thread.sleep(500);
            assertFalse("Shall Not be finished yet", AC.finished);
            AsynchronousTest.class.wait();
            assertTrue("The asynchronous action is finished", AC.finished);
        }

        if (err.length() > 0) {
            fail("No warning about the class: " + err);
        }
    }

    public static class AC extends AbstractAction {
        static boolean finished;
        
        public void actionPerformed(ActionEvent ev) {
            synchronized (AsynchronousTest.class) {
                AsynchronousTest.class.notifyAll();
                finished = true;
            }
        }
    }

    public static class CAC extends AC implements ContextAwareAction {
        public Action createContextAwareInstance(Lookup actionContext) {
            return this;
        }
    }
}
