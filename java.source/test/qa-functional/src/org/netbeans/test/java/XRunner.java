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

package org.netbeans.test.java;

import org.openide.cookies.SaveCookie;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;

//import org.netbeans.modules.java.settings.JavaSynchronizationSettings;

/** Runner
 * @author Jan Becicka
 */
public abstract class XRunner extends LogTestCase implements Go {
    
    protected String packageName;
    protected String name;
    
    /** golden file
     */
    /*protected File passFile;
     
    private String result="";*/
    
    private static boolean disabled = false;
    
    public XRunner(java.lang.String testName) {
        super(testName);
    }
    
    /** "main" of the TestCase
     */
    public void testRun() throws DataObjectNotFoundException {
        boolean ok = true;
        
        String result="";
        
        FileObject artefact=null;
        try {
            artefact=FileUtil.toFileObject(classPathWorkDir);
        } catch (Exception ex) {
            ex.printStackTrace(log);
            assertTrue(ex.toString(), false);
        }
        FileObject fo = artefact.getFileObject((packageName + "." + name).replace(".","/"));
        
        if (fo == null) {
            try {
                fo = Common.createClass(artefact, packageName, name);
            } catch (Exception e) {
                e.printStackTrace(log);
                assertTrue(e.toString(), false);
            }
        }
        //clazz.getSource().prepare().waitFinished();
        DataObject DO = DataObject.find(fo);
        try {
            ok&= go(fo, log );
            if (!ok) {
                System.out.println("go() failed");
            }
        } catch (Exception e) {
            ok = false;
            e.printStackTrace(log);
        }
        ok&= writeResult(DO);
        try {
            if (DO.getCookie(SaveCookie.class) != null) {
                ((SaveCookie) DO.getCookie(SaveCookie.class)).save();
            }
            DO.delete();
        } catch (Exception e){
            assertTrue(e.toString(), false);
        }
        assertTrue("See .log file for details", ok);
    }
    
    private static void disable() {
        if (!disabled) {
            disabled = true;
//            JavaSynchronizationSettings jss = (JavaSynchronizationSettings) JavaSynchronizationSettings.findObject(JavaSynchronizationSettings.class, true);
            //jss.setEnabled(false);
            
/*            try {
                org.netbeans.test.oo.gui.jello.JelloOKOnlyDialog ok = new org.netbeans.test.oo.gui.jello.JelloOKOnlyDialog("Warning");
                ok.ok();
            } catch (Exception texc) {
                // it's OK no error
                // texc.printStackTrace();
            }
 */
        }
    }
    
    protected boolean writeResult(DataObject DO) {
        String result="";
        try {
            EditorCookie ec=(EditorCookie)(DO.getCookie(EditorCookie.class));
            javax.swing.text.StyledDocument doc=ec.openDocument();
            result=doc.getText(0, doc.getLength());
            result=Common.unify(result);
        } catch (Exception e){
            e.printStackTrace(log);
            return false;
        }        
        ref(result);
        return true;
    }
}
