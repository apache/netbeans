/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
