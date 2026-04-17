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
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
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
        assert SwingUtilities.isEventDispatchThread() : "UI thread expected but is: " + Thread.currentThread().getName(); // NOI18N
        Document document = getLastFocusedDocument();
        if (document == null) {
            return;
        }
        long startTime = 0;
        if (LOGGER.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
        }
        PhpTestingProvider testingProvider = getFirstTestingProvider(document);
        if (LOGGER.isLoggable(Level.FINE)) {
            long time = System.currentTimeMillis() - startTime;
            LOGGER.fine(String.format("getFirstTestingProvider() took %d ms", time)); // NOI18N
        }
        if (testingProvider == null) {
            return;
        }
        // don't add the listener if this document is not a test file
        String propertyName = event.getPropertyName();
        if (propertyName.equals(EditorRegistry.FOCUS_GAINED_PROPERTY)) {
            handleFileChange(document);
            document.addDocumentListener(this);
        } else if (propertyName.equals(EditorRegistry.FOCUS_LOST_PROPERTY)) {
            document.removeDocumentListener(this);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        processUpdate();
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        processUpdate();
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        processUpdate();
    }

    private void handleFileChange(Document doc) {
        handledDocument = doc;
        processUpdate();
    }

    private void processUpdate() {
        task.schedule(500);
    }

    @CheckForNull
    private Document getLastFocusedDocument() {
        JTextComponent textComponent = EditorRegistry.lastFocusedComponent();
        if (textComponent == null) {
            return null;
        }
        return textComponent.getDocument();
    }

    /**
     * Get the first testing provider that recognizes this document as a test
     * file.
     *
     * @param document
     * @return the first testing provider if this document is recognized as a
     * test file, {@code null} otherwise
     */
    @CheckForNull
    private PhpTestingProvider getFirstTestingProvider(Document document) {
        FileObject fileObject = NbEditorUtilities.getFileObject(document);
        if (fileObject == null) {
            return null;
        }
        PhpProject project = PhpProjectUtils.getPhpProject(fileObject);
        if (project == null) {
            return null;
        }
        PhpModule phpModule = project.getPhpModule();
        for (PhpTestingProvider testingProvider : project.getTestingProviders()) {
            if (testingProvider.isTestFile(phpModule, fileObject)) {
                return testingProvider;
            }
        }
        return null;
    }

    @Override
    public void run() {
        List<TestMethod> testMethods = getTestMethods(handledDocument);

        TestMethodController.setTestMethods(handledDocument, testMethods);
    }

    /**
     * Get test methods for the first available testing provider.
     *
     * @return test methods
     */
    private List<TestMethod> getTestMethods(Document document) {
        FileObject fileObject = NbEditorUtilities.getFileObject(document);
        if (fileObject == null) {
            return Collections.emptyList();
        }
        PhpProject project = PhpProjectUtils.getPhpProject(fileObject);
        if (project == null) {
            return Collections.emptyList();
        }
        PhpTestingProvider testingProvider = getFirstTestingProvider(document);
        if (testingProvider == null) {
            return Collections.emptyList();
        }
        return getTestMethods(document, fileObject, testingProvider, project.getPhpModule());
    }

    private List<TestMethod> getTestMethods(Document document, FileObject fileObject, PhpTestingProvider testingProvider, PhpModule phpModule) {
        List<TestMethod> testMethods = new ArrayList<>();
        Collection<PhpClass> phpClasses = getPhpClasses(fileObject);
        for (PhpClass phpClass : phpClasses) {
            // check whether the document tab has already been switched to another one
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
                TestMethod testMethod = createTestMethod(document, phpClass, method, fileObject);
                if (testMethod != null) {
                    testMethods.add(testMethod);
                }
            }
        }
        return testMethods;
    }

    private Collection<PhpClass> getPhpClasses(FileObject fileObject) {
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        assert editorSupport != null;
        return editorSupport.getClasses(fileObject);
    }

    @CheckForNull
    private TestMethod createTestMethod(Document document, PhpClass phpClass, PhpType.Method method, FileObject fileObject) {
        Position startPosition = null;
        Position endPosition = null;
        try {
            startPosition = document.createPosition(method.getOffset());
            endPosition = document.createPosition(method.getOffset() + method.getName().length());
        } catch (BadLocationException exception) {
            LOGGER.log(Level.WARNING, "Unable to create position: offset: {0}; method: {1}; class: {2}.", // NOI18N
                    new Object[]{exception.offsetRequested(), method.getName(), phpClass.getName()});
        }
        if (startPosition == null || endPosition == null) {
            return null;
        }
        String methodName = CommandUtils.encodeMethod(method.getPhpType().getFullyQualifiedName(), method.getName());
        return new TestMethod(phpClass.getName(), new SingleMethod(fileObject, methodName), startPosition, endPosition);
    }
}
