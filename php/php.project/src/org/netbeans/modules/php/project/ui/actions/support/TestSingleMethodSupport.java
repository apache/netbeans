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
package org.netbeans.modules.php.project.ui.actions.support;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.Mutex;

public final class TestSingleMethodSupport {

    private TestSingleMethodSupport() {
    }

    public static boolean isTestClass(Node activatedNode) {
        FileObject fileObject = CommandUtils.getFileObject(activatedNode);
        if (fileObject == null) {
            return false;
        }
        PhpProject project = PhpProjectUtils.getPhpProject(fileObject);
        if (project == null) {
            return false;
        }
        if(CommandUtils.isUnderTests(project, fileObject, false)) {
            return true;
        }
        return false;
    }

    public static boolean canHandle(Node activatedNode) {
        FileObject fileObject = CommandUtils.getFileObject(activatedNode);
        if (fileObject == null) {
            return false;
        }
        PhpProject project = PhpProjectUtils.getPhpProject(fileObject);
        if (project == null) {
            return false;
        }
        final EditorCookie editorCookie = activatedNode.getLookup().lookup(EditorCookie.class);
        if (editorCookie == null) {
            return false;
        }
        JEditorPane pane = Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane>() {
            @Override
            public JEditorPane run() {
                return NbDocument.findRecentEditorPane(editorCookie);
            }

        });
        if (pane == null) {
            return false;
        }
        return getTestMethod(pane.getDocument(), pane.getCaret().getDot()) != null;
    }

    public static SingleMethod getTestMethod(Document doc, int caret) {
        FileObject fileObject = NbEditorUtilities.getFileObject(doc);
        assert fileObject != null;
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        assert editorSupport != null;
        PhpBaseElement element = editorSupport.getElement(fileObject, caret);
        if (!(element instanceof PhpType.Method)) {
            return null;
        }
        PhpType.Method method = (PhpType.Method) element;
        PhpProject project = PhpProjectUtils.getPhpProject(fileObject);
        assert project != null;
        PhpModule phpModule = project.getPhpModule();
        for (PhpTestingProvider testingProvider : project.getTestingProviders()) {
            if (testingProvider.isTestFile(phpModule, fileObject)
                    && testingProvider.isTestCase(phpModule, method)) {
                return new SingleMethod(fileObject, CommandUtils.encodeMethod(method.getPhpType().getFullyQualifiedName(), method.getName()));
            }
        }
        return null;
    }

}
