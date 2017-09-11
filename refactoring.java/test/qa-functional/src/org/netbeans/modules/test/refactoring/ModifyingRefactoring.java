/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
        Collections.sort(l, new Comparator<FileObject>() {
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
