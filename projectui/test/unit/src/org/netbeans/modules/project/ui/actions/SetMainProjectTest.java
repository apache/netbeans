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

package org.netbeans.modules.project.ui.actions;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.actions.TestSupport.ActionCreator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class SetMainProjectTest extends NbTestCase {
    
    public SetMainProjectTest(String name) {
        super( name );
    }

    public void setUp() throws Exception {
        super.setUp();

        MockServices.setServices(TestSupport.TestProjectFactory.class);
        clearWorkDir ();
    }
    
    public void testAcceleratorsPropagated() {
        TestSupport.doTestAcceleratorsPropagated(new ActionCreator() {
            public LookupSensitiveAction create(Lookup l) {
                return new SetMainProject(l);
            }
        }, false);
    }
    
    public void test70368() {
        SetMainProject a = new SetMainProject();
        WeakReference<?> ref = new WeakReference<Object>(a);
        
        a = null;
        
        assertGC("SetMainProject action's instance can be freed:", ref);
    }
    
    public void test70835() throws IOException {
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        
        assertNotNull(workDir);
        
        FileObject f1 = TestSupport.createTestProject (workDir, "project1");
        FileObject f2 = TestSupport.createTestProject (workDir, "project2");
        
        assertNotNull(f1);
        assertNotNull(f2);
        
        Project p1 = ProjectManager.getDefault().findProject(f1);
        Project p2 = ProjectManager.getDefault().findProject(f2);
        
        assertNotNull(p1);
        assertNotNull(p2);
        
        OpenProjectList.getDefault().open(new Project[] {p1, p2}, false);
        
        SetMainProject a = new SetMainProject();
        
        JMenuItem item = a.getMenuPresenter();
        
        assertTrue(item instanceof JMenu);
        
        JMenu menu = (JMenu) item;
        
        item = null;
        
        assertEquals(4, menu.getItemCount());
        assertTrue(menu.isEnabled());
        
        WeakReference<?> menuRef = new WeakReference<Object>(menu);
        WeakReference<?> actionRef = new WeakReference<Object>(a);
        
        a = null;
        
        try {
            assertGC("", actionRef);
        } catch (Error e) {
            //ignore....
        }
        
        OpenProjectList.getDefault().close(new Project[] {p1}, false);
        waitForAWT();
        assertEquals(3, menu.getItemCount());
        assertTrue(menu.isEnabled());

        OpenProjectList.getDefault().close(new Project[] {p2}, false);
        waitForAWT();
        assertEquals(2, menu.getItemCount());
        assertFalse(menu.isEnabled());

        OpenProjectList.getDefault().open(new Project[] {p1}, false);
        waitForAWT();
        assertEquals(3, menu.getItemCount());
        assertTrue(menu.isEnabled());
        
        menu = null;
        
        assertGC("", menuRef);
        assertGC("", actionRef);
    }

    private void waitForAWT() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
