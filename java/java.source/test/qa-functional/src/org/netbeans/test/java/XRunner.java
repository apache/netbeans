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
