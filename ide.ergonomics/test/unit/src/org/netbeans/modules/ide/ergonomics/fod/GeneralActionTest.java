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
 * Software is Sun Microsystems, Inc. Portions Copyright 2008 Sun
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

package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class GeneralActionTest extends NbTestCase {
    private FileObject actionFile;
    private FileObject root;
    
    public GeneralActionTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        if (true) return; // disabled right now

        clearWorkDir();
        
        Logger.getLogger("org.netbeans.core.startup").setLevel(Level.OFF);

        URI uri = ModuleInfo.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        File jar = new File(uri);
        System.setProperty("netbeans.home", jar.getParentFile().getParent());
        System.setProperty("netbeans.user", getWorkDirPath());
        StringBuffer sb = new StringBuffer();
        boolean found = false;
        for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (info.getCodeNameBase().equals("org.netbeans.modules.subversion")) {
                Method m = null;
                Class<?> c = info.getClass();
                Exception ex = null;
                for (;;) {
                    if (c == null) {
                        throw ex;
                    }
                    try {
                        m = c.getDeclaredMethod("setEnabled", Boolean.TYPE);
                    } catch (Exception ex2) {
                        ex = ex2;
                    }
                    if (m != null) {
                        break;
                    }
                    c = c.getSuperclass();
                }
                m.setAccessible(true);
                m.invoke(info, false);
                assertFalse("Module is now disabled", info.isEnabled());
                found = true;
            }
            sb.append(info.getCodeNameBase()).append('\n');
        }
        if (!found) {
            fail("No module found:\n" + sb);
        }

        File dbp = new File(getWorkDir(), "dbproject");
        File db = new File(dbp, "project.properties");
        dbp.mkdirs();
        db.createNewFile();

        root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("fileobject found", root);
        
        FoDLayersProvider.getInstance().refreshForce();

        actionFile = FileUtil.getConfigFile("Actions/System/org-netbeans-modules-autoupdate-ui-actions-PluginManagerAction.instance");
        assertNotNull("testing layer is loaded: ", actionFile);
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testIconIsCorrect() throws Exception {
        if (true) return; // disabled right now
        
        myListenerCounter = 0;
        myIconResourceCounter = 0;
        Action a = readAction();
        
        assertTrue("Always enabled", a.isEnabled());
        a.setEnabled(false);
        assertTrue("Still Always enabled", a.isEnabled());
        
        
        assertEquals("No icon in menu", Boolean.TRUE, a.getValue("noIconInMenu"));
        
        if (a instanceof ContextAwareAction) {
            fail("Should not be context sensitive, otherwise it would have to implement equal correctly: " + a);
        }
        
        a.actionPerformed(null);
    }
    
    private static int myListenerCounter;
    private static ActionListener myListener() {
        myListenerCounter++;
        return null;
    }
    private static int myIconResourceCounter;
    private static String myIconResource() {
        myIconResourceCounter++;
        return "/org/netbeans/modules/ide/ergonomics/api/TestIcon.png";
    }
    
    
    private Action readAction() throws Exception {
        FileObject fo = this.actionFile;
        assertNotNull("file " + actionFile, fo);
        
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("File object has not null instanceCreate attribute", obj);
        
        if (!(obj instanceof Action)) {
            fail("Object needs to be action: " + obj);
        }
        
        return (Action)obj;
    }
}
