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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import javax.swing.text.StyledDocument;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.util.PNGEncoder;
//import org.netbeans.modules.java.settings.JavaSynchronizationSettings;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.SharedClassObject;


/** Runner
 * @author Jan Becicka
 */
public abstract class XGUIRunner extends JellyTestCase implements Go {
    
    protected String name;
    
    protected String packageName;
    
    public XGUIRunner(java.lang.String testName) {
        super(testName);
    }
    
    public void waitEditorOpened() {
        new EditorWindowOperator().getEditor(name);
    }

    public void testRun() {
        DataObject DO = null;
        //JavaSynchronizationSettings ss = (JavaSynchronizationSettings) SharedClassObject.findObject(JavaSynchronizationSettings.class);
        //ss.setEnabled(false);
        String fullName = packageName + "." + name;
        
        boolean ok = true;
        
        try {
            ok = go(fullName, new PrintWriter(getLog()));
            if (!ok) {
                getLog().println("go() failed");
            }
            
            DO = DataObject.find(Repository.getDefault().findResource(fullName.replace('.','/') + ".java"));
            ((SaveCookie) DO.getCookie(SaveCookie.class)).save();
        } catch (Exception e) {
            ok = false;
            e.printStackTrace(getLog());
        }

        ok = writeResult(DO);
        try {
            DO.delete();
        } catch (IOException e){
            assertTrue(e.toString(), false);
        }

        assertTrue("See .log file for details", ok);
	compareReferenceFiles();
    }
    
     public File getGoldenFile(String filename) {
        String fullClassName = this.getClass().getName();
        String className = fullClassName;
        int lastDot = fullClassName.lastIndexOf('.');
        if (lastDot != -1) {
            className = fullClassName.substring(lastDot+1);
        }  
        String goldenFileName = className+".pass";
        URL url = this.getClass().getResource(goldenFileName);
        assertNotNull("Golden file "+goldenFileName+" cannot be found",url);
        String resString = convertNBFSURL(url);        
        File goldenFile = new File(resString);
        return goldenFile;
    }

    protected boolean writeResult(DataObject DO) {
        String result = "";
        try {
            EditorCookie ec=(EditorCookie)(DO.getCookie(EditorCookie.class));
            StyledDocument doc=ec.openDocument();
            result=doc.getText(0, doc.getLength());
            result=Common.unify(result);
        } catch (Exception e){
            e.printStackTrace(getLog());
            return false;
        }
        
        getRef().print(result);
        return true;
    }
    
}
