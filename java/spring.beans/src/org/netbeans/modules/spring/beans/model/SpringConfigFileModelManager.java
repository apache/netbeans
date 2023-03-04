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

package org.netbeans.modules.spring.beans.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.beans.model.impl.ConfigFileSpringBeanSource;
import org.netbeans.modules.spring.util.fcs.FileChangeSupport;
import org.netbeans.modules.spring.util.fcs.FileChangeSupportEvent;
import org.netbeans.modules.spring.util.fcs.FileChangeSupportListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class SpringConfigFileModelManager {

    // XXX improve MIME type checking.
    // XXX when to clear file2Controller?

    // @GuardedBy("file2Controller")
    private final Map<File, SpringConfigFileModelController> file2Controller = Collections.synchronizedMap(new HashMap<File, SpringConfigFileModelController>());

    // @GuardedBy("this")
    private FileListener fileListener;
    // @GuardedBy("this")
    private EditorRegistryListener editorListener;

    public SpringConfigFileModelManager() {
    }

    public SpringConfigFileModelController getFileModelController(File file) {
        synchronized (this) {
            // Initializing the listeners lazily especially in order to avoid
            // adding unnecessary listeners to EditorRegistry.
            if (fileListener == null) {
                fileListener = new FileListener();
                editorListener = new EditorRegistryListener();
                editorListener.initialize();
            }
        }
        synchronized (file2Controller) {
            SpringConfigFileModelController controller = file2Controller.get(file);
            if (controller == null) {
                controller = new SpringConfigFileModelController(file, new ConfigFileSpringBeanSource());
                FileChangeSupport.DEFAULT.addListener(fileListener, file);
                file2Controller.put(file, controller);
            }
            return controller;
        }
    }

    private void notifyFileChanged(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            return;
        }
        notifyFileChanged(fo, file);
    }

    private void notifyFileDeleted(File file) {
        // XXX probably in order to support repeatable read, we should remove
        // the controller under exclusive access
    }

    private void notifyFileChanged(FileObject fo, File file) {
        SpringConfigFileModelController fileController = file2Controller.get(file);
        if (fileController != null) {
            fileController.notifyChange(fo);
        }
    }

    /**
     * Listens on disk changes to the config files.
     */
    private final class FileListener implements FileChangeSupportListener {

        public void fileCreated(FileChangeSupportEvent event) {
            notifyFileChanged(event.getPath());
        }

        public void fileModified(FileChangeSupportEvent event) {
            notifyFileChanged(event.getPath());
        }

        public void fileDeleted(FileChangeSupportEvent event) {
            notifyFileDeleted(event.getPath());
        }
    }

    /**
     * Listens on editor changes.
     */
    private final class EditorRegistryListener implements PropertyChangeListener, DocumentListener {

        private Document currentDocument;

        public EditorRegistryListener() {
        }

        public synchronized void initialize() {
            EditorRegistry.addPropertyChangeListener(this);
            JTextComponent newComponent = EditorRegistry.lastFocusedComponent();
            currentDocument = newComponent != null ? newComponent.getDocument() : null;
            if (currentDocument != null) {
                currentDocument.addDocumentListener(this);
            }
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            assert SwingUtilities.isEventDispatchThread();
            JTextComponent newComponent = EditorRegistry.lastFocusedComponent();
            Document newDocument = newComponent != null ? newComponent.getDocument() : null;
            if (currentDocument == newDocument) {
                return;
            }
            if (currentDocument != null) {
                currentDocument.removeDocumentListener(this);
            }
            currentDocument = newDocument;
            if (currentDocument != null) {
                currentDocument.addDocumentListener(this);
            }
        }

        public void changedUpdate(DocumentEvent e) {
            notify(e.getDocument());
        }

        public void insertUpdate(DocumentEvent e) {
            notify(e.getDocument());
        }

        public void removeUpdate(DocumentEvent e) {
            notify(e.getDocument());
        }

        private void notify(Document document) {
            FileObject fo = NbEditorUtilities.getFileObject(document);
            if (fo == null){
                return;
            }
            if (!SpringConstants.CONFIG_MIME_TYPE.equals(fo.getMIMEType())) {
                return;
            }
            File file = FileUtil.toFile(fo);
            if (file == null) {
                return;
            }
            notifyFileChanged(fo, file);
        }
    }
}
