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

package org.netbeans.modules.php.project.api;

import java.util.Collections;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.openide.filesystems.FileObject;

/**
 * @since 2.17
 * @author Tomas Mysik
 */
public final class PhpEditorExtender {
    private static final EditorExtender EMPTY_EDITOR_EXTENDER = new EmptyEditorExtender();

    private PhpEditorExtender() {
    }

    public static EditorExtender forFileObject(FileObject fo) {
        if (!isCurrentlyOpened(fo)) {
            return EMPTY_EDITOR_EXTENDER;
        }
        PhpProject phpProject = org.netbeans.modules.php.project.util.PhpProjectUtils.getPhpProject(fo);
        if (phpProject == null) {
            return EMPTY_EDITOR_EXTENDER;
        }
        EditorExtender editorExtender = phpProject.getLookup().lookup(EditorExtender.class);
        assert editorExtender != null : "Editor extender must be found for " + phpProject;
        return editorExtender;
    }

    private static boolean isCurrentlyOpened(FileObject fo) {
        JTextComponent component = EditorRegistry.lastFocusedComponent();
        if (component == null) {
            return false;
        }
        FileObject opened = NbEditorUtilities.getFileObject(component.getDocument());
        if (opened == null) {
            return false;
        }
        return opened.equals(fo);
    }

    private static final class EmptyEditorExtender extends EditorExtender {
        @Override
        public List<PhpBaseElement> getElementsForCodeCompletion(FileObject fo) {
            return Collections.emptyList();
        }
    }
}
