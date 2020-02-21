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

package org.netbeans.modules.mercurial.remote;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.mercurial.remote.config.HgConfigFiles;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgRepositoryContextCache;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class AttributeInterceptorTest extends  RemoteVersioningTestBase {

    public AttributeInterceptorTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, AttributeInterceptorTest.class, "getAttributeRefreh");
        addTest(suite, AttributeInterceptorTest.class, "getAttributeWrong");
        addTest(suite, AttributeInterceptorTest.class, "getAttributeNotCloned");
        addTest(suite, AttributeInterceptorTest.class, "getAttributeClonedRoot");
        addTest(suite, AttributeInterceptorTest.class, "getAttributeCloned");
        addTest(suite, AttributeInterceptorTest.class, "getAttributeClonedOnlyPush");
        addTest(suite, AttributeInterceptorTest.class, "getAttributeClonedPull");
        addTest(suite, AttributeInterceptorTest.class, "getAttributeClonedPullWithCredentials");
        addTest(suite, AttributeInterceptorTest.class, "isModifiedAttributeFile");
        return(suite);
    }
    
    public void getAttributeRefreh() throws HgException, IOException {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        FileObject fo = file.toFileObject();
        Runnable attr = (Runnable) fo.getAttribute("ProvidedExtensions.Refresh");
        assertNotNull(attr);

        attr.run();
        // XXX check status
    }

    public void getAttributeWrong() throws HgException, IOException {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        FileObject fo = file.toFileObject();
        String attr = (String) fo.getAttribute("peek-a-boo");
        assertNull(attr);
    }

    public void getAttributeNotCloned() throws HgException, IOException {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        FileObject fo = file.toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNull(attr);
    }

    public void getAttributeClonedRoot() throws HgException, IOException {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        VCSFileProxy cloned = clone(getWorkTreeDir());

        FileObject fo = cloned.toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(getWorkTreeDir().getPath(), attr);
    }

    public void getAttributeCloned() throws HgException, IOException {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        VCSFileProxy cloned = clone(getWorkTreeDir());

        FileObject fo = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(cloned, folder.getName()), file.getName()).toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(getWorkTreeDir().getPath(), attr);
    }

    public void getAttributeClonedOnlyPush() throws HgException, IOException {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        VCSFileProxy cloned = clone(getWorkTreeDir());

        String defaultPush = "http://a.repository.far.far/away";
        new HgConfigFiles(cloned).removeProperty(HgConfigFiles.HG_PATHS_SECTION, HgConfigFiles.HG_DEFAULT_PULL);
        new HgConfigFiles(cloned).removeProperty(HgConfigFiles.HG_PATHS_SECTION, HgConfigFiles.HG_DEFAULT_PULL_VALUE);
        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PUSH, defaultPush);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(cloned, folder.getName()), file.getName()).toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPush, attr);
    }

    public void getAttributeClonedPull() throws HgException, IOException {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        VCSFileProxy cloned = clone(getWorkTreeDir());

        String defaultPull = "http://a.repository.far.far/away";
        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PULL, defaultPull);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(cloned, folder.getName()), file.getName()).toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPull, attr);
    }

    public void getAttributeClonedPullWithCredentials() throws HgException, IOException {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");

        commit(folder);
        VCSFileProxy cloned = clone(getWorkTreeDir());

        String defaultPull = "http://so:secure@a.repository.far.far/away";
        String defaultPullReturned = "http://a.repository.far.far/away";

        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PULL, defaultPull);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(cloned, folder.getName()), file.getName()).toFileObject();
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPullReturned, attr);
    }

    public void isModifiedAttributeFile () throws Exception {
        if (skipTest()) {
            return;
        }
        // file is outside of versioned space, attribute should be unknown
        VCSFileProxy file = VCSFileProxy.createFileProxy(getWorkTreeDir().getParentFile(), "testIsModifiedAttributeFile.txt");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.normalizeFile().toFileObject();
        String attributeModified = "ProvidedExtensions.VCSIsModified";
        
        Object attrValue = fo.getAttribute(attributeModified);
        assertNull(attrValue);
        
        // file inside a git repo
        file = VCSFileProxy.createFileProxy(getWorkTreeDir(), "file");
        write(file, "init");
        fo = file.normalizeFile().toFileObject();
        // new file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        HgCommand.doAdd(getWorkTreeDir(), file, NULL_LOGGER);
        // added file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        commit(file);
        
        // unmodified file, returns FALSE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
        
        write(file, "modification");
        // modified file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        write(file, "init");
        // back to up to date
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
    }

}
