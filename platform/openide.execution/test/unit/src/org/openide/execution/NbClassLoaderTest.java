/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.execution;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.Permission;
import java.util.Arrays;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Utilities;

/** Test NbClassLoader.
 * @author Jesse Glick
 */
public class NbClassLoaderTest extends NbTestCase {

    public NbClassLoaderTest(String name) {
        super(name);
    }
    
    /** Ensure that a user-mode class can at least use findResource() to access
     * resources in filesystems.
     * @see "#13038"
     */
    public void testUsingNbfsProtocol() throws Exception {
        System.setProperty("org.netbeans.core.Plain.CULPRIT", "true");
        File here = Utilities.toFile(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        assertTrue("Classpath really contains " + here,
                new File(new File(new File(new File(here, "org"), "openide"), "execution"), "NbClassLoaderTest.class").canRead());
        
        File dataDir = new File(new File(new File(new File(here, "org"), "openide"), "execution"), "data");
        if(!dataDir.exists()) {
            dataDir.mkdir();
        }
        File fooFile = new File(dataDir, "foo.xml");
        if(!fooFile.exists()) {
            fooFile.createNewFile();
        }
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(here);
        lfs.setReadOnly(true);
        ClassLoader cl = new NbClassLoader(new FileObject[] {lfs.getRoot()}, ClassLoader.getSystemClassLoader().getParent(), null);
        System.setSecurityManager(new MySecurityManager());
        // Ensure this class at least has free access:
        System.getProperty("foo");
        Class c = cl.loadClass("org.openide.execution.NbClassLoaderTest$User");
        assertEquals(cl, c.getClassLoader());
        try {
            c.getDeclaredConstructor().newInstance();
        } catch (ExceptionInInitializerError eiie) {
            Throwable t = eiie.getException();
            if (t instanceof IllegalStateException) {
                fail(t.getMessage());
            } else if (t instanceof Exception) {
                throw (Exception)t;
            } else {
                throw new Exception(t.toString());
            }
        }
    }
    
    public void testFastIsUsedForFileUrl() throws Exception {
        CharSequence log = Log.enable(NbClassLoader.class.getName(), Level.FINE);
        LocalFileSystem lfs = new LocalFileSystem();
        File here = Utilities.toFile(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        lfs.setRootDirectory(here);
        lfs.setReadOnly(true);
        ClassLoader cl = new NbClassLoader(new FileObject[]{lfs.getRoot()}, ClassLoader.getSystemClassLoader().getParent(), null);
        Class c = cl.loadClass("org.openide.execution.NbClassLoaderTest$User");
        assertFalse(log.toString().contains("NBFS used!"));
    }
    
    public static final class User {
        public User() throws Exception {
            URLClassLoader ncl = (URLClassLoader)getClass().getClassLoader();
            URL[] urls = ncl.getURLs();
            if (urls.length != 1) throw new IllegalStateException("Weird URLs: " + Arrays.asList(urls));
            URL manual = new URL(urls[0], "org/openide/execution/data/foo.xml");
            URLConnection uc = manual.openConnection();
            uc.connect();
            String ct = uc.getContentType();
            /* May now be a file: URL, in which case content type is hard to control:
            if (!"text/xml".equals(ct)) throw new IllegalStateException("Wrong content type (manual): " + ct);
             */
            URL auto = getClass().getResource("data/foo.xml");
            if (auto == null) throw new IllegalStateException("Could not load data/foo.xml; try uncommenting se.printStackTrace() in MySecurityManager.checkPermission");
            uc = auto.openConnection();
            uc.connect();
            ct = uc.getContentType();
            /* Ditto:
            if (!"text/xml".equals(ct)) throw new IllegalStateException("Wrong content type (auto): " + ct);
             */
            // Ensure this class does *not* have free access to random permissions:
            try {
                System.getProperty("foo");
                throw new IllegalStateException("Was permitted to access sys prop foo");
            } catch (SecurityException se) {
                // good
            }
        }
    }
    
    private static final class MySecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission p) {
            //System.err.println("cP: " + p);
            if (ok()) {/*System.err.println("ok");*/return;}
            try {
                super.checkPermission(p);
            } catch (SecurityException se) {
                //se.printStackTrace();
                //System.err.println("classes: " + Arrays.asList(getClassContext()));
                throw se;
            }
        }
        @Override
        public void checkPermission(Permission p, Object c) {
            if (ok()) {/*System.err.println("ok");*/return;}
            super.checkPermission(p, c);
        }
        @Override
        public void checkRead(String file) {
            // Do not honor file read checks. TopSecurityManager actually leaves
            // this blank for performance, but in fact very little would work if
            // we restricted reads. nbfs: protocols would be useless, meaning user
            // classes would not be able to load resources. This could be solved if
            // necessary by creating a special kind of FilePermission returned from
            // FileURL; it would be recognized by TSM.checkPermission and accepted.
        }
        private boolean ok() {
            Class[] cs = getClassContext();
            int i = 0;
            while (i < cs.length && cs[i] == MySecurityManager.class) i++;
            for (; i < cs.length; i++) {
                if (cs[i] == MySecurityManager.class) {
                    // avoid recursion
                    return true;
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
            new Exception().printStackTrace(new PrintStream(baos));
            if (baos.toString().indexOf("\tat java.security.AccessController.doPrivileged") != -1) {
                // Cheap check for privileged actions.
                // For some reason AccessController does not appear in the classContext
                // (perhaps because it is a native method?).
                return true;
            }
            for (int j = 0; j < cs.length; j++) {
                if (cs[j].getClassLoader() instanceof NbClassLoader) {
                    return false;
                }
            }
            return true;
        }
    }
    
}
