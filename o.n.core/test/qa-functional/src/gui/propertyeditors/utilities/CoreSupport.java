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

package gui.propertyeditors.utilities;

import java.io.PrintStream;
import java.io.IOException;
import java.awt.Component;
import javax.swing.JDialog;
import org.netbeans.jellytools.JellyTestCase;

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;

import org.netbeans.junit.NbTestCase;

// ide imports
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;

/** Utilities for core tests. From this class extends supports for each testsuite.
 *
 * @author  Marian.Mirilovic@Sun.Com
 * @version
 */

public class CoreSupport {
    
    /** Creates new utilities */
    public CoreSupport() {
    }
    
    /**
     * Find path to the Sample Project.
     * 
     * @return path to the Sample Project 
     * @param testCase 
     */
    public static String getSampleProjectPath(JellyTestCase testCase) {
        return new java.io.File(testCase.getDataDir(),"SampleProject").getPath();
    }
    
    
    /**
     * Find file system name.
     * 
     * @param exc 
     * @param err 
     */
/*    public static String getFS(String _package, String fileName, String fileExtension){
        FileObject f = findFileObject(_package, fileName, fileExtension);
        
        if(f==null)
            throw new JemmyException("Unable find file " + fileName + "." + fileExtension + " in package " + _package);
        
        String fs;
        try{
            fs = f.getFileSystem().getSystemName();
        }catch(FileStateInvalidException exc){
            throw new JemmyException("FileStateInvalidException during attempt get filesystem name for " + fileName + "." + fileExtension + " in package " + _package);
        }
        
        // hack for Win NT/2K , where in FileObject is bad file separator !!!
        char fileSeparator = System.getProperty("file.separator").charAt(0);
        String fsName = fs.replace('/',fileSeparator).replace('\\',fileSeparator);
        //String path = fsName+ ", " + _package + ", " + fileName;
        
        return fsName;
    }
    
    
    public static String getPath(String packageName, String fileName, String fileExtension, String delim){
        String FS_Name = getFS(packageName, fileName, fileExtension);
        return FS_Name + delim + packageName.replace('.',delim.charAt(0)) + delim + fileName;
    }
    
    public static String getSystemPath(String packageName, String fileName, String fileExtension){
        String fileSeparator = System.getProperty("file.separator");
        return getPath(packageName, fileName, fileExtension, fileSeparator)+ "." + fileExtension;
    }
*/    
    public static void writeExc(Exception exc, PrintStream err) {
        err.println("Test ERROR: ");
        exc.printStackTrace(err);
    }
    
    /**
     * 
     * @param testCase 
     */
    public static void makeIDEScreenshot(NbTestCase testCase) {
        try{
            testCase.getWorkDir();
            org.netbeans.jemmy.util.PNGEncoder.captureScreen(testCase.getWorkDirPath()+System.getProperty("file.separator")+"IDEscreenshot.png");
        }catch(Exception ioexc){
            testCase.log("Impossible make IDE screenshot!!! \n" + ioexc.toString());
        }
    }
    
    /**
     * 
     * @param testCase 
     * @param component 
     */
    public static void makeWindowScreenshot(NbTestCase testCase, Component component) {
        try{
            testCase.getWorkDir();
            if(component != null)
                org.netbeans.jemmy.util.PNGEncoder.captureScreen(component,testCase.getWorkDirPath()+System.getProperty("file.separator")+"ComponentScreenshot.png");
            else
                makeIDEScreenshot(testCase);
        }catch(Exception ioexc){
            testCase.log("Impossible make component screenshot!!! \n =========" +component.toString() + "\n ======== \n " + ioexc.toString());
        }
    }
    
    
    public static void closeAllModal() {                                                                                                                  
        JDialogOperator oper = null;                                                                                                               
        // find some JDialog                                                                                                                       
        JDialog jDialog = JDialogOperator.findJDialog(ComponentSearcher.getTrueChooser(""));                                                       
        // number of opened non-modal                                                                                                              
        int nonModal = 0;                                                                                                                          
        // until any modal dialog is opened                                                                                                        
        while(jDialog!=null) {                                                                                                                     
            oper = new JDialogOperator(jDialog);                                                                                                   
            if(oper.isModal()) {                                                                                                                   
                // close if modal                                                                                                                  
                oper.close();                                                                                                                      
            } else {                                                                                                                               
                // increment nonModal                                                                                                              
                nonModal++;                                                                                                                        
            }                                                                                                                                      
            // use nonModal variable as index to skip opened non-modal dialogs                                                                     
            jDialog = JDialogOperator.findJDialog(ComponentSearcher.getTrueChooser(""), nonModal);                                                 
        }                                                                                                                                          
    }                                                                                                                                              

}
