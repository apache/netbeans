/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

