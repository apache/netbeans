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
