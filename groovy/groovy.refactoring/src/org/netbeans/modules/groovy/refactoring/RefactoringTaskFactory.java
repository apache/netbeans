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
