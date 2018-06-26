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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.refactoring;

import java.util.Collection;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.refactoring.RefactoringTask.NodeToElementTask;
import org.netbeans.modules.groovy.refactoring.RefactoringTask.TextComponentTask;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.groovy.refactoring.move.MoveFileRefactoringUI;
import org.netbeans.modules.groovy.refactoring.rename.RenameRefactoringUI;
import org.netbeans.modules.groovy.refactoring.ui.WhereUsedQueryUI;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Janicek
 */
public final class RefactoringTaskFactory {

    public enum RefactoringType {
        FIND_USAGES, RENAME, MOVE
    }

    private RefactoringTaskFactory() {
    }


    public static RefactoringTask createRefactoringTask(Lookup lookup, RefactoringType type) {
        final EditorCookie ec = lookup.lookup(EditorCookie.class);

        if (GroovyProjectUtil.isFromEditor(ec)) {
            return getTextComponentTask(lookup, type);
        } else {
            return getNodeToElementTask(lookup, type);
        }
    }

    private static RefactoringTask getTextComponentTask(Lookup lookup, RefactoringType type) {
        final EditorCookie ec = lookup.lookup(EditorCookie.class);
        final FileObject fileObject = getFileObject(lookup);

        switch (type) {
            case FIND_USAGES:
                return new FindUsagesTextComponentTask(ec, fileObject);
            case RENAME:
                return new RenameRefactoringTextComponentTask(ec, fileObject);
            case MOVE:
                return new MoveRefactoringTextComponentTask(ec, fileObject);
            default:
                return null;
        }
    }

    private static RefactoringTask getNodeToElementTask(Lookup lookup, RefactoringType type) {
        final FileObject fileObject = lookup.lookup(FileObject.class);
        final Collection<? extends Node> nodes = lookup.lookupAll(Node.class);

        switch (type) {
            case FIND_USAGES:
                return new FindUsagesNodeToElementTask(nodes, fileObject);
            case RENAME:
                // TODO: Implement as well
                break;
            case MOVE:
                return new MoveNodeToElementTask(nodes, fileObject);
            default:
                return null;
        }
        return null;
    }

    private static FileObject getFileObject(Lookup lookup) {
        final Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        final Node node;
        if (nodes.size() == 1) {
            node = nodes.iterator().next();
        } else {
            node = null;
        }

        final DataObject dataObject;
        if (node != null) {
            dataObject = node.getLookup().lookup(DataObject.class);
        } else {
            dataObject = null;
        }

        return (dataObject != null) ? dataObject.getPrimaryFile() : null;
    }


    private static class FindUsagesTextComponentTask extends TextComponentTask {

        public FindUsagesTextComponentTask(EditorCookie ec, FileObject fileObject) {
            super(ec, fileObject);
        }

        @Override
        protected RefactoringUI createRefactoringUI(RefactoringElement selectedElement, int startOffset, int endOffset, GroovyParserResult info) {
            if (selectedElement != null && selectedElement.getName() != null) {
                return new WhereUsedQueryUI(selectedElement);
            }
            return null;
        }
    }

    private static class FindUsagesNodeToElementTask extends NodeToElementTask {

        public FindUsagesNodeToElementTask(Collection<? extends Node> nodes, FileObject fileObject) {
            super(fileObject);
        }

        @Override
        protected RefactoringUI createRefactoringUI(RefactoringElement selectedElement, GroovyParserResult info) {
            if (selectedElement != null && selectedElement.getName() != null) {
                return new WhereUsedQueryUI(selectedElement);
            }
            return null;
        }
    }

    private static class RenameRefactoringTextComponentTask extends TextComponentTask {

        public RenameRefactoringTextComponentTask(EditorCookie ec, FileObject fileObject) {
            super(ec, fileObject);
        }

        @Override
        protected RefactoringUI createRefactoringUI(RefactoringElement selectedElement, int startOffset, int endOffset, GroovyParserResult info) {
            if (selectedElement != null && selectedElement.getName() != null) {
                return new RenameRefactoringUI(selectedElement);
            }
            return null;
        }
    }

    private static class MoveRefactoringTextComponentTask extends TextComponentTask {

        public MoveRefactoringTextComponentTask(EditorCookie ec, FileObject fileObject) {
            super(ec, fileObject);
        }

        @Override
        protected RefactoringUI createRefactoringUI(RefactoringElement selectedElement, int startOffset, int endOffset, GroovyParserResult info) {
            if (selectedElement != null && selectedElement.getFileObject() != null) {
                return new MoveFileRefactoringUI(selectedElement);
            }
            return null;
        }
    }

    private static class MoveNodeToElementTask extends NodeToElementTask {

        public MoveNodeToElementTask(Collection<? extends Node> nodes, FileObject fileObject) {
            super(fileObject);
        }

        @Override
        protected RefactoringUI createRefactoringUI(RefactoringElement selectedElement, GroovyParserResult info) {
            if (selectedElement != null && selectedElement.getFileObject() != null) {
                return new MoveFileRefactoringUI(selectedElement);
            }
            return null;
        }
    }
}
