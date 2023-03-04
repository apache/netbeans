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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class ClassNameMismatch implements ErrorRule<Void> {

    private static final Set<String> CODES = new HashSet<>(Arrays.asList("compiler.err.class.public.should.be.in.file"));
    
    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        FileObject file = info.getFileObject();
        
        if (!file.getParent().canWrite()) return Collections.emptyList();

        //testcase unknown, bug #235033:
        if (!TreeUtilities.CLASS_TREE_KINDS.contains(treePath.getLeaf().getKind())) return Collections.emptyList();
        
        return Arrays.<Fix>asList(new RenameFile(file, ((ClassTree) treePath.getLeaf()).getSimpleName().toString()));
    }

    @Override
    public String getId() {
        return ClassNameMismatch.class.getName();
    }

    @Override
    @Messages("DN_ClassNameMismatch=Class Name not Matching File Name")
    public String getDisplayName() {
        return Bundle.DN_ClassNameMismatch();
    }

    @Override
    public void cancel() { }
    
    private static final class RenameFile implements Fix {
        private final FileObject toRename;
        private final String newName;

        public RenameFile(FileObject toRename, String newName) {
            this.toRename = toRename;
            this.newName = newName;
        }
        
        @Override
        @Messages("FIX_ChangeFileName=Rename file to {0}")
        public String getText() {
            return Bundle.FIX_ChangeFileName(newName + "." + toRename.getExt());
        }

        @Override
        public ChangeInfo implement() throws Exception {
            DataObject.find(toRename).rename(newName);
            return null;
        }
    }
    
}
