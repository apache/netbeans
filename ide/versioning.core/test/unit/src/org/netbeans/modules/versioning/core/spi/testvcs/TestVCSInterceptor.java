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
package org.netbeans.modules.versioning.core.spi.testvcs;

import java.io.*;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;

import java.util.*;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileUtil;

/**
 * @author Maros Sandor
 */
public class TestVCSInterceptor extends VCSInterceptor {
    public static final String REMOTE_LOCATION_PREFIX = "http://a.repository.far.far.away/";
    private final TestVCSInterceptor instance;
    
    public DeleteHandler deleteHandler;
    public MoveHandler moveHandler;
    public CopyHandler copyHandler;
    
    public static interface DeleteHandler {
        void delete(VCSFileProxy proxy) throws IOException;
    }
    
    public static interface MoveHandler {
        void move(VCSFileProxy from, VCSFileProxy to) throws IOException;
    }
    
    public static interface CopyHandler {
        void copy(VCSFileProxy from, VCSFileProxy to) throws IOException;
    }
    
    private final List<VCSFileProxy>    beforeCreateFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    doCreateFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    createdFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    beforeDeleteFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    doDeleteFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    deletedFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    beforeMoveFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    doMoveFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    afterMoveFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    beforeCopyFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    afterCopyFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    doCopyFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    beforeEditFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    beforeChangeFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    afterChangeFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    isMutableFiles = new ArrayList<VCSFileProxy>();
    private final List<VCSFileProxy>    refreshRecursivelyFiles = new ArrayList<VCSFileProxy>();

    public TestVCSInterceptor() {
        instance = this;
    }

    public boolean isMutable(VCSFileProxy file) {
        isMutableFiles.add(file);
        if(file.getName().startsWith(TestVCS.ALWAYS_WRITABLE_PREFIX)) {
            return true;
        }
        return super.isMutable(file);
    }

    @Override
    public Object getAttribute(VCSFileProxy file, String attrName) {
        if (attrName.equals("ProvidedExtensions.RemoteLocation")) {
            return REMOTE_LOCATION_PREFIX + file.getName();
        }
        return null;
    }

    public boolean beforeCreate(VCSFileProxy file, boolean isDirectory) {
        beforeCreateFiles.add(file);
        return true;
    }

    public void beforeChange(VCSFileProxy file) {
        beforeChangeFiles.add(file);
    }

    public void afterChange(VCSFileProxy file) {
        afterChangeFiles.add(file);
    }

    public void doCreate(VCSFileProxy file, boolean isDirectory) throws IOException {
        doCreateFiles.add(file);
        if (!file.exists()) {
            if (isDirectory) {
                file.toFile().mkdirs();
            } else {
                file.toFile().getParentFile().mkdirs();
                file.toFile().createNewFile();
            }
        }
    }

    public void afterCreate(VCSFileProxy file) {
        createdFiles.add(file);
    }

    public boolean beforeDelete(VCSFileProxy file) {
        beforeDeleteFiles.add(file);
        return true;
    }

    public void doDelete(VCSFileProxy file) throws IOException {
        doDeleteFiles.add(file);
        if (file.getName().endsWith("do-not-delete")) return;
        if(deleteHandler == null) {
            deleteHandler = new DefaultDeleteHandler();
        }
        deleteHandler.delete(file);
    }

    public void afterDelete(VCSFileProxy file) {
        deletedFiles.add(file);
    }

    public boolean beforeMove(VCSFileProxy from, VCSFileProxy to) {
        beforeMoveFiles.add(from);
        beforeMoveFiles.add(to);
        return true;
    }

    public void doMove(VCSFileProxy from, VCSFileProxy to) throws IOException {
        doMoveFiles.add(from);
        doMoveFiles.add(to);
        if(moveHandler == null) {
            moveHandler = new DefaultMoveHandler();
        }
        moveHandler.move(from, to); 
    }

    public void afterMove(VCSFileProxy from, VCSFileProxy to) {
        afterMoveFiles.add(from);
        afterMoveFiles.add(to);
    }

    public boolean beforeCopy(VCSFileProxy from, VCSFileProxy to) {
        beforeCopyFiles.add(from);
        beforeCopyFiles.add(to);
        return true;
    }

    public void doCopy(VCSFileProxy from, VCSFileProxy to) throws IOException {
        doCopyFiles.add(from);
        doCopyFiles.add(to);
        if(copyHandler == null) {
            copyHandler = new DefaultCopyHandler();
        }
        copyHandler.copy(from, to);
    }

    public void afterCopy(VCSFileProxy from, VCSFileProxy to) {
        afterCopyFiles.add(from);
        afterCopyFiles.add(to);
    }

    public void beforeEdit(VCSFileProxy file) {
        beforeEditFiles.add(file);
    }

    public List<VCSFileProxy> getIsMutableFiles() {
        return isMutableFiles;
    }

    public List<VCSFileProxy> getBeforeCreateFiles() {
        return beforeCreateFiles;
    }

    public List<VCSFileProxy> getDoCreateFiles() {
        return doCreateFiles;
    }

    public List<VCSFileProxy> getCreatedFiles() {
        return createdFiles;
    }

    public List<VCSFileProxy> getBeforeDeleteFiles() {
        return beforeDeleteFiles;
    }

    public List<VCSFileProxy> getDoDeleteFiles() {
        return doDeleteFiles;
    }

    public List<VCSFileProxy> getDeletedFiles() {
        return deletedFiles;
    }

    public List<VCSFileProxy> getBeforeMoveFiles() {
        return beforeMoveFiles;
    }

    public List<VCSFileProxy> getAfterMoveFiles() {
        return afterMoveFiles;
    }

    public List<VCSFileProxy> getBeforeCopyFiles() {
        return beforeCopyFiles;
    }

    public List<VCSFileProxy> getDoCopyFiles() {
        return doCopyFiles;
    }
    
    public List<VCSFileProxy> getDoMoveFiles() {
        return doMoveFiles;
    }

    public List<VCSFileProxy> getAfterCopyFiles() {
        return afterCopyFiles;
    }

    public List<VCSFileProxy> getBeforeEditFiles() {
        return beforeEditFiles;
    }

    public List<VCSFileProxy> getBeforeChangeFiles() {
        return beforeChangeFiles;
    }

    public List<VCSFileProxy> getAfterChangeFiles() {
        return afterChangeFiles;
    }

    public List<VCSFileProxy> getRefreshRecursivelyFiles() {
        return refreshRecursivelyFiles;
    }

    @Override
    public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
        refreshRecursivelyFiles.add(dir);
        if(dir.getName().equals("administrative")) {
            return 0;
        }
        return -1;
    }

    public void clearTestData() {
        beforeCreateFiles.clear();
        doCreateFiles.clear();
        createdFiles.clear();
        beforeDeleteFiles.clear();
        doDeleteFiles.clear();
        deletedFiles.clear();
        beforeMoveFiles.clear();
        afterMoveFiles.clear();
        beforeEditFiles.clear();
        beforeChangeFiles.clear();
        afterChangeFiles.clear();
        isMutableFiles.clear();
    }
    
    private class DefaultDeleteHandler implements DeleteHandler {
        @Override
        public void delete(VCSFileProxy proxy) throws IOException {
            deleteRecursively(proxy);
        }
        private void deleteRecursively(VCSFileProxy proxy) throws IOException {
            assertProxy(proxy);
            if(proxy.isFile()) proxy.toFile().delete();
            VCSFileProxy[] files = proxy.listFiles();
            if(files != null) {
                for (VCSFileProxy f : files) {
                    deleteRecursively(f);
                }
            } 
        }
    }
    
    private class DefaultMoveHandler implements MoveHandler {
        @Override
        public void move(VCSFileProxy from, VCSFileProxy to) throws IOException {
            assertProxy(from);
            assertProxy(to);
            if(!from.toFile().renameTo(to.toFile())) {
                throw new IOException("wasn't able t rename " + from + " to " + to);
            }
        }
    }
    
    private class DefaultCopyHandler implements CopyHandler {
        @Override
        public void copy(VCSFileProxy from, VCSFileProxy to) throws IOException {
            assertProxy(from);
            assertProxy(to);
            copy(from.toFile(), to.toFile());
        }
        
        private void copy(File fromFile, File toFile) throws IOException {
            if(fromFile.isFile()) {
                InputStream is = new FileInputStream (fromFile);
                OutputStream os = new FileOutputStream(toFile);
                FileUtil.copy(is, os);
                is.close();
                os.close();
            } else {
                toFile.mkdirs();
                File[] files = fromFile.listFiles();
                if( files == null || files.length == 0) {
                    return;
                }
                for(File f : files) {
                    copy(f, new File(toFile, f.getName()));
                }
            }
        }
    }
    
    private void assertProxy(VCSFileProxy proxy) {
        assert proxy != null;
        assert proxy.toFile() != null;
    }
}
