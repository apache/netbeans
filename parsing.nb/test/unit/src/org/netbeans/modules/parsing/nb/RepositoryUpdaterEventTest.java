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

package org.netbeans.modules.parsing.nb;

import org.netbeans.modules.parsing.nb.EventSupport;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import static junit.framework.Assert.assertNotNull;
import junit.framework.TestSuite;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdaterTest;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
public class RepositoryUpdaterEventTest extends RepositoryUpdaterTest {

    public RepositoryUpdaterEventTest(String name) {
        super(name);
    }
    
    public static TestSuite suite() {
        TestSuite s = new TestSuite();
        s.addTest(new RepositoryUpdaterEventTest("testAWTIndexAndWaitDeadlock"));
        
        return s;
    }
    
    public void testAWTIndexAndWaitDeadlock() throws Exception {
        final Class<EventSupport.EditorRegistryListener> erlc = EventSupport.EditorRegistryListener.class;        
        final Field k24Field = erlc.getDeclaredField("k24");   //NOI18N
        assertNotNull (k24Field);
        k24Field.setAccessible(true);
        final AtomicBoolean cond = (AtomicBoolean) k24Field.get(null);
        
        final Source source = Source.create(f3);
        assertNotNull(source);

        Runnable action = new Runnable() {
            public void run() {
                try {
                    TaskProcessor.resetState(source, false, true);
                    cond.set(true);
                } catch (/*ReflectiveOperation*/Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        }
        else {
            SwingUtilities.invokeAndWait(action);
        }

        action = new Runnable() {
            public void run() {
                try {
                    IndexingManager.getDefault().refreshIndexAndWait(srcRootWithFiles1.getURL(), null);
                } catch (FileStateInvalidException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        }
        else {
            SwingUtilities.invokeAndWait(action);
        }


        action = new Runnable() {
            public void run() {
                try {
                    cond.set(false);
                    TaskProcessor.resetStateImpl(source);
                } catch (/*ReflectiveOperation*/Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        }
        else {
            SwingUtilities.invokeAndWait(action);
        }

    }

}
