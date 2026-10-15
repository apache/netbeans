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
package org.netbeans.modules.test.refactoring;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.jemmy.EventTool;
import org.netbeans.modules.test.refactoring.operators.RefactoringResultOperator;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public class ModifyingRefactoring extends RefactoringTestCase {

    public ModifyingRefactoring(String name) {
        super(name);
    }

    /**
     * Gets list of files in give directory
     *
     * @param rootDir Root directory
     * @return List of filenames in given directory, including subdirectories
     */
        public List<String> getFiles(File rootDir) {
        List<String> res = new LinkedList<String>();
        getFiles(rootDir, res);
        return res;
    }

    private void getFiles(File dir, List<String> res) {
        File[] listFiles = dir.listFiles();
        for (File file : listFiles) {
            if (file.getName().startsWith(".")) {
                continue;
            } //ignoring hidden files
            if (file.isDirectory()) {
                getFiles(file, res);
            }
            res.add(file.getAbsolutePath());
        }
    }

    public void refModifiedFiles(Set<FileObject> modifiedFiles) {
        List<FileObject> l = new LinkedList<FileObject>(modifiedFiles);
        l.sort(new Comparator<FileObject>() {
            @Override
            public int compare(FileObject o1, FileObject o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (FileObject fileObject : l) {
            ref(fileObject);
        }

    }

    public void refFileChange(List<String> origFiles, List<String> newFiles) {
        Collections.sort(newFiles);
        Collections.sort(origFiles);
        for (String fileName : newFiles) {
            if (!origFiles.contains(fileName)) {
                File f = new File(fileName);
                if (!f.exists()) {
                    fail("File " + fileName + " does not exists");
                }
                if (f.isDirectory()) {
                    ref("Created directory:");
                    String rootDir = new File(getRefactroringTestFolder(), "src").getAbsolutePath();
                    ref(fileName.substring(rootDir.length()).replace('\\', '/'));
                } else {
                    ref("Created file:");
                    ref(new File(fileName));
                }
            }
        }

        for (String fileName : origFiles) {
            if (!newFiles.contains(fileName)) {
                ref("Deleted file:\n");
                String rootDir = new File(getRefactroringTestFolder(), "src").getAbsolutePath();
                getRef().print(fileName.substring(rootDir.length()).replace('\\', '/'));
            }
        }
    }

    protected void dumpRefactoringResults() {
        RefactoringResultOperator result = RefactoringResultOperator.getPreview();
        new EventTool().waitNoEvent(1000);
        
        Set<FileObject> involvedFiles = (Set<FileObject>) result.getInvolvedFiles();
        List<String> origfiles = getFiles(getRefactroringTestFolder());
        
        result.doRefactoring();
        new EventTool().waitNoEvent(8000);
        
        refModifiedFiles(involvedFiles);
        refFileChange(origfiles, getFiles(getRefactroringTestFolder()));
    }
    
    private File getRefactroringTestFolder(){
        return new File(getDataDir(), "projects" + File.separator + REFACTORING_TEST);
    }
}
