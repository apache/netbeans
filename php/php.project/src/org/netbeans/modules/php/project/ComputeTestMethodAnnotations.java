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
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


public class ComputeTestMethodAnnotations implements DocumentListener, PropertyChangeListener, Runnable {

    private static final Logger LOGGER = Logger.getLogger(ComputeTestMethodAnnotations.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(ComputeTestMethodAnnotations.class);
    private static final ComputeTestMethodAnnotations INSTANCE = new ComputeTestMethodAnnotations();
    private static final AtomicInteger USAGES_COUNT = new AtomicInteger(0);
    private final RequestProcessor.Task task = RP.create(this);
    private volatile Document handledDocument;

    public static ComputeTestMethodAnnotations getInstance() {
        return INSTANCE;
    }

    private ComputeTestMethodAnnotations() {
    }

    public void register() {
        if (USAGES_COUNT.getAndIncrement() == 0) {
            EditorRegistry.addPropertyChangeListener(this);
        }
    }

    public void unregister() {
        if (USAGES_COUNT.decrementAndGet() == 0) {
            EditorRegistry.removePropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        assert SwingUtilities.isEventDispatchThread() : "UI thread expected but is: " + Thread.currentThread().getName();

        String propertyName = event.getPropertyName();

        JTextComponent textComponent = EditorRegistry.lastFocusedComponent();
        if (textComponent != null) {
            Document document = textComponent.getDocument();
            if (document != null) {
                FileObject fileObject = NbEditorUtilities.getFileObject(document);
                if (fileObject != null) {
                    if (FileUtils.isPhpFile(fileObject)) {
                        if (propertyName.equals(EditorRegistry.FOCUS_GAINED_PROPERTY)) {
                            handleFileChange(document);
                            document.addDocumentListener(ComputeTestMethodAnnotations.this);
                        } else if (propertyName.equals(EditorRegistry.FOCUS_LOST_PROPERTY)) {
                            document.removeDocumentListener(this);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        handleFileChange(event.getDocument());
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        handleFileChange(event.getDocument());
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        handleFileChange(event.getDocument());
    }

    private void handleFileChange(Document doc) {
        handledDocument = doc;
        task.schedule(500);
    }

    @Override
    public void run() {
        List<TestMethod> testMethods = getTestMethods(handledDocument);

        /*
         * Apparently, this method should update the list of annotations at each call of this method,
         * when the passed collection of methods changes.
         * Вut I didn't manage to achieve correct work in this case.
         * So I made the method call first with the empty collection, to clear the annotation list,
         * then already with the method collection.
         */
        TestMethodController.setTestMethods(handledDocument, Collections.emptyList());
        if (!testMethods.isEmpty()) {
            TestMethodController.setTestMethods(handledDocument, testMethods);
        }
    }

    private List<TestMethod> getTestMethods(Document document) {
        List<TestMethod> testMethods = new ArrayList<>();
        FileObject fileObject = NbEditorUtilities.getFileObject(document);
        if (fileObject != null) {
            PhpProject project = PhpProjectUtils.getPhpProject(fileObject);
            assert project != null;
            PhpModule phpModule = project.getPhpModule();
            for (PhpTestingProvider testingProvider : project.getTestingProviders()) {

                if (!document.equals(handledDocument)) {
                    return Collections.emptyList();
                }

                if (testingProvider.isTestFile(phpModule, fileObject)) {
                    EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
                    assert editorSupport != null;
                    Collection<PhpClass> phpClasses = editorSupport.getClasses(fileObject);
                    for (PhpClass phpClass : phpClasses) {

                        if (!document.equals(handledDocument)) {
                            return Collections.emptyList();
                        }

                        for (PhpType.Method method : phpClass.getMethods()) {

                            if (!document.equals(handledDocument)) {
                                return Collections.emptyList();
                            }

                            if (!testingProvider.isTestCase(phpModule, method)) {
                                continue;
                            }
                            try {
                                testMethods.add(new TestMethod(
                                        phpClass.getName(),
                                        new SingleMethod(fileObject, CommandUtils.encodeMethod(method.getPhpType().getFullyQualifiedName(), method.getName())),
                                        document.createPosition(method.getOffset()),
                                        document.createPosition(method.getOffset() + method.getName().length())
                                ));
                            } catch (BadLocationException exception) {
                                LOGGER.log(Level.WARNING, "Unable to create position: offset: {0}; method: {1}; class: {2}.", new Object[] {exception.offsetRequested(), method.getName(), phpClass.getName()}); // NOI18N
                            }
                        }

                        if (!testMethods.isEmpty()) {
                            return testMethods;
                        }
                    }

                }
            }
        }

        return testMethods;
    }
}
