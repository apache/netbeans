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
package org.netbeans.modules.subversion.remote;

import java.io.OutputStream;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import static org.netbeans.modules.subversion.remote.RemoteVersioningTestBase.addTest;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class InterceptorModifyTest extends RemoteVersioningTestBase {

    public InterceptorModifyTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, InterceptorModifyTest.class, "modifyFileOnDemandLock");
        addTest(suite, InterceptorModifyTest.class, "isModifiedAttributeFile");
        return(suite);
    }
    
    public void modifyFileOnDemandLock () throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(file);
        commit(wc);
        getClient().propertySet(file, "svn:needs-lock", "true", false);
        commit(file);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());

        SvnModuleConfig.getDefault(fs).setAutoLock(true);
        // modify
        OutputStream os = file.toFileObject().getOutputStream();
        os.write(new byte[] { 'a', 0 });
        os.close();

        // test
        assertTrue(file.exists());
        assertEquals(SVNStatusKind.MODIFIED, getSVNStatus(file).getTextStatus());

        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY_CONTENT | FileInformation.STATUS_LOCKED);

        commit(wc);

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
    }
    
    public void isModifiedAttributeFile () throws Exception {
        if (skipTest()) {
            return;
        }
        // file is outside of versioned space, attribute should be unknown
        
        FileObject fo = fs.createTempFile(fs.getTempFolder(), "testIsModifiedAttributeFile", "txt", true);
        String attributeModified = "ProvidedExtensions.VCSIsModified";
        
        Object attrValue = fo.getAttribute(attributeModified);
        assertNull(attrValue);
        
        // file inside a svn repo
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        TestKit.write(file, "init");
        fo = file.toFileObject();
        // new file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        getClient().addFile(file);
        // added file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        commit(file);
        
        // unmodified file, returns FALSE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
        
        TestKit.write(file, "modification");
        // modified file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        TestKit.write(file, "init");
        // back to up to date
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
    }
}
