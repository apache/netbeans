/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
