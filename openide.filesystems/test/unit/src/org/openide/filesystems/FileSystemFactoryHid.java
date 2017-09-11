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

package org.openide.filesystems;

import junit.framework.*;
import org.netbeans.junit.*;

import java.io.*;
import java.net.URL;
import java.util.*;



/**
 *
 * @author  vs124454, rm111737
 * @version
 */
public abstract class FileSystemFactoryHid extends NbTestSetup {
    private static Map<Test, List<FileSystemFactoryHid>> map =
            new HashMap<Test, List<FileSystemFactoryHid>> ();
    private static String className;


    /** Creates new FileSystemFactory
     * @param test  */
    public FileSystemFactoryHid(Test test) {
        super(test);        
        /**Adds */
        registerMap (test);
    }

    /**
     * Intended to allow prepare tested environment for each individual test.
     * Although this method is static, all subclasses must modify its behaviour by means of
     * createTestedFS.
     * @param testName name of test 
     * @return  array of FileSystems that should be tested in test named: "testName"*/    
    static FileSystem[] createFileSystem (String testName,String[] resources, Test test) throws IOException {
         return getInstance (test,false).createFileSystem(testName, resources);
    }

    /**
     * Intended to allow prepare tested environment for each individual test.
     * @param testName name of test
     * @return  array of FileSystems that should be tested in test named: "testName"*/
    static FileSystem createXMLSystem (String testName, Test test, URL... layers) throws IOException {
        FileSystemFactoryHid factory = getInstance(test, false);
        if (factory instanceof XMLFileSystemTestHid.Factory) {
            XMLFileSystemTestHid.Factory f = (XMLFileSystemTestHid.Factory) factory;
            return f.createLayerSystem(testName, layers);
        }
        throw new IllegalStateException("You need to implement XMLFileSystemTestHid.Factory to use the AttributesTestHidden!");
    }
    static boolean switchXMLSystem (FileSystem fs, Test test, URL... layers) throws IOException {
        FileSystemFactoryHid factory = getInstance(test, false);
        if (factory instanceof XMLFileSystemTestHid.Factory) {
            XMLFileSystemTestHid.Factory f = (XMLFileSystemTestHid.Factory) factory;
            return f.setXmlUrl(fs, layers);
        }
        throw new IllegalStateException("You need to implement XMLFileSystemTestHid.Factory to use the AttributesTestHidden!");
    }
      
    static void destroyFileSystem (String testName, Test test)  throws IOException  {
        getInstance (test,false).destroyFileSystem(testName);
    }    
    
    static String getResourcePrefix (String testName, Test test, String[] resources) {
        return getInstance (test,false).getResourcePrefix(testName, resources);
    }    

    
    
    private static  FileSystemFactoryHid getInstance (Test test, boolean delete) {
            FileSystemFactoryHid factory = getFromMap (test,delete);
            if (factory != null)
                className  =  factory.getClass().getName();
        return factory;
    }
    
    static  String getTestClassName () {
        return (className != null) ? className : "Unknown TestSetup";
    }

    static void setServices(Test test, Class<?>... classes) {
        getInstance(test, false).setServices(classes);
    }
    
    /**
     * @param resources that are required to run given test
     * @return  array of FileSystems that should be tested in test named: "testName"*/    
    protected abstract FileSystem[] createFileSystem(String testName, String[] resources) throws IOException;        
    
    protected abstract void destroyFileSystem(String testName) throws IOException;

    protected String getResourcePrefix (String testName, String[] resources) {
        return "";
    }

    /** Registers services into default lookup. By default it uses
     * MockServices.setServices. Subclasses may override and register
     * additional classes into the lookup as well.
     * 
     * @param services the classes that shall be accessible from Lookup.getDefault()
     */
    protected void setServices(Class<?>... services) {
        MockServices.setServices(services);
    }
    

    private void registerMap (Test test) {
        if (test instanceof TestSuite) {
            Enumeration en = ((TestSuite)test).tests ();
            while (en.hasMoreElements()) {                
                Test tst = (Test)en.nextElement();
                if (tst instanceof TestSuite) 
                    registerMap (tst);
                else {
                    addToMap (tst);
                }
            }
        } else {
            addToMap (test);                
        }
    }
    
    private   void addToMap (Test test) {    
        List<FileSystemFactoryHid> s = map.get (test);
        if (s == null) {
            s = new LinkedList<FileSystemFactoryHid>();
        } 
        s.add(this);
        map.put(test ,s );                                                        
        
    }
        

    private static FileSystemFactoryHid getFromMap (Test test, boolean delete) {    
        LinkedList s = (LinkedList)map.get (test);
        FileSystemFactoryHid  retVal;
        try {
            retVal = (FileSystemFactoryHid)s.getLast();
        } catch (NoSuchElementException x ) {
            System.out.println("exc: "+ test + " : " );
            throw x;
        }
        if (delete) {
            s.remove(retVal);
        }
        return retVal;         
    }            
}
