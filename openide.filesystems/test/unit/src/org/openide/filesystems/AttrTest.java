/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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
