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

package org.openide.filesystems;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author  pz97949
 */
public class AttrTest extends NbTestCase {
//    File fileSystemFile;
    File fileSystemDir;
    LocalFileSystem fileSystem;
     public AttrTest(String testName) {
        super(testName);
    }
   
     /** tests set/get attribute to fileobject special named: 
      *  "\"
      * see to bug http://installer.netbeans.org/issues/show_bug.cgi?id=8976
      */
    public void testSpecialNamedAttr() throws IOException,PropertyVetoException {
            preprocess();
            FileObject fo = getAnyFileObject() ; 
            setAttribute(fo,"\"", "1");
            setAttribute(fo,"h&", "2");
            setAttribute(fo,"<","3");
            setAttribute(fo,">","4");
            setAttribute(fo,"-", "5");
            setAttribute(fo,"*","6");
            System.gc();

            getAttribute(fo,"\"","1");
            getAttribute(fo,"h&","2");
            getAttribute(fo,"<","3");
            getAttribute(fo,">","4");
            getAttribute(fo,"-","5");
            getAttribute(fo,"*","6");
    }
 
    /** set attribute to FileObject 
     */ 
    private void setAttribute(FileObject fo,String name,String value) {
        try {
          fo.setAttribute(name, value);
          log ("attribute (name = " + name + ", value = " + value + ") setted" );
        } catch (Exception e) {
            String msg = "failed on set attribute name = " + name + " , value = " + value;
            log (msg); 
            assertTrue(msg,false);
        }
    }
    /** read attribude from fileobject and tests if is correct
     */
    private String  getAttribute(FileObject fo,String name, String refValue) {
        String value = (String) fo.getAttribute(name);
        if (value == null ) {
            assertTrue("File object doesn't contain attribute (name = " + name + ", value = " + value + " ",false);
        } else {
            if (!value.equals(refValue)) {
                assertTrue("FileObject read wrong attr value ( name = " + name + 
                      ",correct value = " + refValue + " , read value = " + value, false );
            }
        }
        return value;
    }
    /** it mounts LocalFileSystem in temorary directory
     */
    private void preprocess() throws IOException,PropertyVetoException {
//        fileSystemFile.mkdir();
        clearWorkDir();
        fileSystemDir = new File(getWorkDir(), "testAtt123rDir");
        if(fileSystemDir.mkdir() == false || fileSystemDir.isDirectory() == false) {
            throw new IOException (fileSystemDir.toString() + " is not directory");
        }
        fileSystem = new LocalFileSystem();
        fileSystem.setRootDirectory(fileSystemDir);
    }
        
    private FileObject getAnyFileObject() {
        return fileSystem.getRoot();
    }
    
    /** test set "\\" attr value, see to :  8977 in Issuezila
     */
    public void testSetBackslashValue() throws IOException,  PropertyVetoException {
        preprocess();
        FileObject fo = getAnyFileObject();
        try {
             setAttribute(fo, "\\", "2");   
             getAttribute(fo, "\\",  "2");
        } catch(Exception e) {
            assertTrue(" failed:no  attribute setted " + e,false );
        }
    }
    
}
