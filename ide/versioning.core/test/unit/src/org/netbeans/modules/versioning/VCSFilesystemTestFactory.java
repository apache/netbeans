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

package org.netbeans.modules.versioning;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSetup;
import org.openide.filesystems.FileObject;

/**
 *
 * @author tomas
 */
public abstract class VCSFilesystemTestFactory extends NbTestSetup {

    public VCSFilesystemTestFactory(Test test) {
        super(test);
        registerMap (test);
    }

    /**
     * Determines the root folder under which the factory creates files.
     * 
     * @return 
     * @throws IOException 
     */
    protected abstract String getRootPath() throws IOException;
    
    /**
     * Creates a file with a full path determined by the factories common root
     * and the given relative path.
     * 
     * @param path
     * @return
     * @throws IOException 
     */
    protected abstract FileObject createFile(String path) throws IOException;
    
    /**
     * Creates a folder with a full path determined by the factories common root.
     * and the given relative path, 
     * 
     * @param path 
     * @return
     * @throws IOException 
     */
    protected abstract FileObject createFolder(String path) throws IOException;
    
    /**
     * Set the file with the given relative path as read-only.
     * 
     * @param path
     * @throws IOException 
     */
    protected abstract void setReadOnly(String path) throws IOException;

    /**
     * Deletes the file with the given relative path. 
     * 
     * @param path
     * @throws IOException 
     */
    public abstract void delete(String path) throws IOException;
    
    /**
     * Move the files with the given relative paths
     * 
     * @param path
     * @throws IOException 
     */
    public abstract void move(String from, String to) throws IOException;
    
    /**
     * Copy the files with the given relative paths
     * 
     * @param path
     * @throws IOException 
     */
    public abstract void copy(String from, String to) throws IOException;
    
    public static VCSFilesystemTestFactory getInstance (Test test) {
        VCSFilesystemTestFactory factory = getFromMap (test);
        return factory;
    }

    private static Map<Test, List<VCSFilesystemTestFactory>> map = new HashMap<Test, List<VCSFilesystemTestFactory>> ();

    private synchronized void registerMap (Test test) {
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

    private synchronized void addToMap (Test test) {
        List<VCSFilesystemTestFactory> s = map.get (test);
        if (s == null) {
            s = new LinkedList<VCSFilesystemTestFactory>();
        }
        s.add(this);
        map.put(test ,s );
    }

    private static synchronized VCSFilesystemTestFactory getFromMap (Test test) {
        LinkedList s = (LinkedList) map.get (test);
        VCSFilesystemTestFactory  retVal;
        try {
            retVal = (VCSFilesystemTestFactory) s.getLast();
        } catch (NoSuchElementException x ) {
            System.out.println("exc: "+ test + " : " );
            throw x;
        }
        return retVal;
    }

}
