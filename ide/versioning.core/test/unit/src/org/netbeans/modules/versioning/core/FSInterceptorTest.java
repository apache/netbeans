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

package org.netbeans.modules.versioning.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.spi.VersioningSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class FSInterceptorTest extends NbTestCase {

    public FSInterceptorTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }   
    
    @Override
    protected void setUp() throws Exception {
        File userdir = new File(getWorkDir() + "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        FileObject fo = FileUtil.toFileObject(getWorkDir());
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    /**
     * ther whether all methods are overriden or not
     * @throws IOException
     */
    public void testTestInterceptorComplete() throws IOException {
        Set<String> testInterceptorMethods = new HashSet<String>();
        Method[]  methods = TestInterceptor.class.getDeclaredMethods();
        for (Method method : methods) {
            if((method.getModifiers() & Modifier.PUBLIC) != 0) {
                System.out.println(" test interceptor method: " + method.getName());
                testInterceptorMethods.add(method.getName());
            }
        }

        methods = VCSInterceptor.class.getDeclaredMethods();
        for (Method method : methods) {
            if((method.getModifiers() & Modifier.PUBLIC) != 0) {
                System.out.println(" vcsinterceptor method: " + method.getName());
                if(!testInterceptorMethods.contains(method.getName())) {
                    fail("" + TestInterceptor.class.getName() + " should override method " + method.getName());
                }
            }
        }
    }  

    public static class TestInterceptor extends VCSInterceptor {
        Set<String> methodNames = new HashSet<String>();
        static TestInterceptor instance;
        public TestInterceptor() {
            instance = this;
        }

        @Override
        public void afterChange(VCSFileProxy file) {
            storeMethodName();
            super.afterChange(file);
        }

        @Override
        public void afterCreate(VCSFileProxy file) {
            storeMethodName();
            super.afterCreate(file);
        }

        @Override
        public void afterDelete(VCSFileProxy file) {
            storeMethodName();
            super.afterDelete(file);
        }

        @Override
        public void afterCopy(VCSFileProxy from, VCSFileProxy to) {
            storeMethodName();
            super.afterCopy(from, to);
        }

        @Override
        public void afterMove(VCSFileProxy from, VCSFileProxy to) {
            storeMethodName();
            super.afterMove(from, to);
        }

        @Override
        public void beforeChange(VCSFileProxy file) {
            storeMethodName();
            super.beforeChange(file);
        }

        @Override
        public boolean beforeCreate(VCSFileProxy file, boolean isDirectory) {
            storeMethodName();
            return true;
        }

        @Override
        public boolean beforeDelete(VCSFileProxy file) {
            storeMethodName();
            return true;
        }

        @Override
        public void beforeEdit(VCSFileProxy file) throws IOException {
            storeMethodName();
            super.beforeEdit(file);
        }

        @Override
        public boolean beforeCopy(VCSFileProxy from, VCSFileProxy to) {
            storeMethodName();
            return true;
        }

        @Override
        public boolean beforeMove(VCSFileProxy from, VCSFileProxy to) {
            storeMethodName();
            return true;
        }

        @Override
        public void doCreate(VCSFileProxy file, boolean isDirectory) throws IOException {
            storeMethodName();
            super.doCreate(file, isDirectory);
        }

        @Override
        public void doDelete(VCSFileProxy file) throws IOException {
            storeMethodName();
            super.doDelete(file);
        }

        @Override
        public void doCopy(VCSFileProxy from, VCSFileProxy to) throws IOException {
            storeMethodName();
            super.doMove(from, to);
        }

        @Override
        public void doMove(VCSFileProxy from, VCSFileProxy to) throws IOException {
            storeMethodName();
            super.doMove(from, to);
        }

        @Override
        public Object getAttribute(VCSFileProxy file, String attrName) {
            storeMethodName();
            return super.getAttribute(file, attrName);
        }

        @Override
        public boolean isMutable(VCSFileProxy file) {
            storeMethodName();
            return super.isMutable(file);
        }

        @Override
        public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
            storeMethodName();
            return super.refreshRecursively(dir, lastTimeStamp, children);
        }

        private void storeMethodName() {
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            for (int i = 0; i < st.length; i++) {
                StackTraceElement e = st[i];
                if(e.getClassName().equals(this.getClass().getName())) {
                    methodNames.add(st[i+1].getMethodName());
                    return;
                }
            }
        }

    }

    @VersioningSystem.Registration(actionsCategory="testvcs", displayName="FSInterceptorTest$TestVCS", menuLabel="FSInterceptorTest$TestVCS", metadataFolderNames="")
    public static class TestVCS extends VersioningSystem {

        private VCSInterceptor interceptor;
        private static TestVCS instance;
        VCSFileProxy file;

        public TestVCS() {
            instance = this;
            interceptor = new TestInterceptor();
        }

        public static TestVCS getInstance() {
            return instance;
        }

        @Override
        public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
            if(this.file == null) return null;
            if(file.equals(this.file.getParentFile())) {
                return file;
            }
            if(file.equals(this.file)) {
                return file.getParentFile();
            }
            return null;
        }

        @Override
        public VCSInterceptor getVCSInterceptor() {
            return interceptor;
        }

    }

    private static class LogHandler extends Handler {
        Set<String> methodNames = new HashSet<String>();
        @Override
        public void publish(LogRecord record) {            
            String msg = record.getMessage();
            if(msg == null || msg.trim().equals("")) {
                return;
            }
            if(msg.startsWith("refreshRecursively")) {
                methodNames.add("refreshRecursively");
            } else if(msg.startsWith("needsLocalHistory")) {
                methodNames.add((String) record.getParameters()[0]);
            }
        }

        void reset () {
            methodNames.clear();
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }


    }

}

