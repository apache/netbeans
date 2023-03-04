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
