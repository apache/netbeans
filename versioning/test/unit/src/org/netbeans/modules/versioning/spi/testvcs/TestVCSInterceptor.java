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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
