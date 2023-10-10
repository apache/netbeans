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
package org.netbeans.modules.php.dbgp.ui.completion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.spi.editor.completion.CompletionProvider;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_QUERY_TYPE;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;

@MimeRegistration(mimeType = ExceptionClassNbDebugEditorKit.MIME_TYPE, service = CompletionProvider.class)
public class ExceptionCompletionProvider implements CompletionProvider {

    private static final Logger LOG = Logger.getLogger(ExceptionCompletionProvider.class.getName());

    private final Set<String> builtinErrors = new HashSet<>(Arrays.asList("Deprecated","Notice", "Warning")); // NOI18N

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }

        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                if (caretOffset < 0) {
                    caretOffset = 0;
                }
                String text;
                try {
                    text = doc.getText(0, caretOffset);
                } catch (BadLocationException ex) {
                    LOG.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested()); // NOI18N
                    text = ""; // NOI18N
                }

                for (String builtinError: builtinErrors) {
                    resultSet.addItem(new ExceptionCompletionItem.Builtin(builtinError));
                }

//https://stackoverflow.com/questions/16090681/retrieving-location-of-currently-opened-file-or-project-in-netbeans

                Set<ClassElement> items = new HashSet<>();

                for (Project project : OpenProjects.getDefault().getOpenProjects()) {
                    PhpModule module = PhpModule.Factory.lookupPhpModule(project);
                    if (module != null) {
                        FileObject source = module.getProjectDirectory();

                        ElementQuery.Index index = ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(source));

                        NameKind nameQuery = NameKind.create(text, Kind.CASE_INSENSITIVE_PREFIX);
                        Set<ClassElement> classes = index.getClasses(nameQuery);
                        for (ClassElement classElement : classes) {
                            if (CancelSupport.getDefault().isCancelled()) {
                                return;
                            }
                            if (isException(classElement)) {
                                items.add(classElement);
                                continue;
                            }
                            if (classElement.getSuperClassName() != null) {
                                Set<ClassElement> inheritedClasses = index.getInheritedClasses(classElement);
                                for (ClassElement inheritedClass : inheritedClasses) {
                                    if (CancelSupport.getDefault().isCancelled()) {
                                        return;
                                    }
                                    if (isException(inheritedClass)) {
                                        items.add(classElement);
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        LOG.log(Level.WARNING, "No php module found for the project {0}", project); // NOI18N
                    }
                }

                for (ClassElement item: items) {
                    resultSet.addItem(new ExceptionCompletionItem(item));
                }

                resultSet.finish();
            }
        }, component);
    }

    private boolean isException(ClassElement element) {
        return element.getFullyQualifiedName().toString().equals("\\Exception"); // NOI18N
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return COMPLETION_QUERY_TYPE;
    }

}
