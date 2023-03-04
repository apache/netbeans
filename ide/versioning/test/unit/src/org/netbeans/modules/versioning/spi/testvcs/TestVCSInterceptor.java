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
package org.netbeans.modules.versioning.spi.testvcs;

import org.netbeans.modules.versioning.spi.VCSInterceptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

/**
 * @author Maros Sandor
 */
public class TestVCSInterceptor extends VCSInterceptor {

    private final List<File>    beforeCreateFiles = new ArrayList<File>();
    private final List<File>    doCreateFiles = new ArrayList<File>();
    private final List<File>    createdFiles = new ArrayList<File>();
    private final List<File>    beforeDeleteFiles = new ArrayList<File>();
    private final List<File>    doDeleteFiles = new ArrayList<File>();
    private final List<File>    deletedFiles = new ArrayList<File>();
    private final List<File>    beforeMoveFiles = new ArrayList<File>();
    private final List<File>    afterMoveFiles = new ArrayList<File>();
    private final List<File>    beforeCopyFiles = new ArrayList<File>();
    private final List<File>    afterCopyFiles = new ArrayList<File>();
    private final List<File>    doCopyFiles = new ArrayList<File>();
    private final List<File>    beforeEditFiles = new ArrayList<File>();
    private final List<File>    beforeChangeFiles = new ArrayList<File>();
    private final List<File>    afterChangeFiles = new ArrayList<File>();
    private final List<File>    isMutableFiles = new ArrayList<File>();
    private final List<File>    refreshRecursivelyFiles = new ArrayList<File>();

    public TestVCSInterceptor() {
    }

    public boolean isMutable(File file) {
        isMutableFiles.add(file);
        return super.isMutable(file);
    }

    @Override
    public Object getAttribute(File file, String attrName) {
        if (attrName.equals("ProvidedExtensions.RemoteLocation")) {
            return "http://a.repository.far.far.away/" + file.getName();
        }
        return null;
    }

    public boolean beforeCreate(File file, boolean isDirectory) {
        beforeCreateFiles.add(file);
        return true;
    }

    public void beforeChange(File file) {
        beforeChangeFiles.add(file);
    }

    public void afterChange(File file) {
        afterChangeFiles.add(file);
    }

    public void doCreate(File file, boolean isDirectory) throws IOException {
        doCreateFiles.add(file);
        if (!file.exists()) {
            if (isDirectory) {
                file.mkdirs();
            } else {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        }
    }

    public void afterCreate(File file) {
        createdFiles.add(file);
    }

    public boolean beforeDelete(File file) {
        beforeDeleteFiles.add(file);
        return true;
    }

    public void doDelete(File file) throws IOException {
        doDeleteFiles.add(file);
        if (file.getName().endsWith("do-not-delete")) return;
        file.delete();
    }

    public void afterDelete(File file) {
        deletedFiles.add(file);
    }

    public boolean beforeMove(File from, File to) {
        beforeMoveFiles.add(from);
        return true;
    }

    public void doMove(File from, File to) throws IOException {
        from.renameTo(to);
    }

    public void afterMove(File from, File to) {
        afterMoveFiles.add(from);
    }

    public boolean beforeCopy(File from, File to) {
        beforeCopyFiles.add(from);
        beforeCopyFiles.add(to);
        return true;
    }

    public void doCopy(File from, File to) throws IOException {
        doCopyFiles.add(from);
        doCopyFiles.add(to);
        FileInputStream is = new FileInputStream(from);
        FileOutputStream os = new FileOutputStream(to);
        FileUtil.copy(is, os);
        is.close();
        os.close();
    }

    public void afterCopy(File from, File to) {
        afterCopyFiles.add(from);
        afterCopyFiles.add(to);
    }

    public void beforeEdit(File file) {
        beforeEditFiles.add(file);
    }

    public List<File> getIsMutableFiles() {
        return isMutableFiles;
    }

    public List<File> getBeforeCreateFiles() {
        return beforeCreateFiles;
    }

    public List<File> getDoCreateFiles() {
        return doCreateFiles;
    }

    public List<File> getCreatedFiles() {
        return createdFiles;
    }

    public List<File> getBeforeDeleteFiles() {
        return beforeDeleteFiles;
    }

    public List<File> getDoDeleteFiles() {
        return doDeleteFiles;
    }

    public List<File> getDeletedFiles() {
        return deletedFiles;
    }

    public List<File> getBeforeMoveFiles() {
        return beforeMoveFiles;
    }

    public List<File> getAfterMoveFiles() {
        return afterMoveFiles;
    }

    public List<File> getBeforeCopyFiles() {
        return beforeCopyFiles;
    }

    public List<File> getDoCopyFiles() {
        return doCopyFiles;
    }

    public List<File> getAfterCopyFiles() {
        return afterCopyFiles;
    }

    public List<File> getBeforeEditFiles() {
        return beforeEditFiles;
    }

    public List<File> getBeforeChangeFiles() {
        return beforeChangeFiles;
    }

    public List<File> getAfterChangeFiles() {
        return afterChangeFiles;
    }

    public List<File> getRefreshRecursivelyFiles() {
        return refreshRecursivelyFiles;
    }

    @Override
    public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
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
}
