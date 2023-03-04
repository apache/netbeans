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

package org.netbeans.modules.spring.api.beans.model;

import java.io.File;
import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.beans.SpringConfigModelAccessor;
import org.netbeans.modules.spring.beans.SpringScopeAccessor;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelController.LockedDocument;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelManager;
import org.netbeans.modules.spring.beans.model.SpringConfigModelController;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionRef;

/**
 * Encapsulates a model of Spring configuration files.
 *
 * @author Andrei Badea
 */
public final class SpringConfigModel {

    private final SpringConfigModelController controller;

    static {
        SpringConfigModelAccessor.setDefault(new SpringConfigModelAccessor() {
            @Override
            public SpringConfigModel createSpringConfigModel(SpringConfigFileModelManager fileModelManager, ConfigFileGroup configFileGroup) {
                return new SpringConfigModel(fileModelManager, configFileGroup);
            }
            @Override
            public DocumentAccess createDocumentAccess(SpringBeans springBeans, File file, LockedDocument lockedDoc) {
                return new DocumentAccess(springBeans, file, lockedDoc);
            }
            @Override
            public ConfigFileGroup getConfigFileGroup(SpringConfigModel model) {
                return model.controller.getConfigFileGroup();
            }
        });
    }

    /**
     * Returns a Spring configuration model for the given file.
     *
     * @param  file a file; never null.
     * @return a Spring configuration model or null
     */
    public static SpringConfigModel forFileObject(FileObject file) {
        SpringScope scope = SpringScope.getSpringScope(file);
        if (scope != null) {
            return SpringScopeAccessor.getDefault().getConfigModel(scope, file);
        }
        return null;
    }

    private SpringConfigModel(SpringConfigFileModelManager fileModelManager, ConfigFileGroup configFileGroup) {
        controller = new SpringConfigModelController(fileModelManager, configFileGroup);
    }

    /**
     * Provides access to the model. This method expects an {@link Action}
     * whose run method will be passed an instance of {@link SpringBeans}.
     *
     * <p><strong>All clients must make sure that no objects obtained from
     * the {@code SpringBeans} instance "escape" the {@code run()} method, in the
     * sense that they are reachable when the {@code run()} method has
     * finished running.</strong></p>
     *
     * @param action the action to run.
     */
    public void runReadAction(final Action<SpringBeans> action) throws IOException {
        controller.runReadAction(action);
    }

    /**
     * Provides access to the model and the document for each underlying configuration file.
     * This method expects an {@link Action} which will be invoked sequentially for each configuration
     * file. This is useful for actions which need to process all files in the
     * model while also accessing the document for each file (for example, refactoring).
     *
     * <p><strong>All clients must make sure that no objects obtained from
     * the {@code SpringBeans} instance "escape" the {@code run()} method, in the
     * sense that they are reachable when the {@code run()} method has
     * finished running.</strong></p>
     *
     * @param  action the action to run.
     */
    public void runDocumentAction(Action<DocumentAccess> action) throws IOException {
        controller.runDocumentAction(action);
    }

    /**
     * Encapsulates access to the model and the document of one of the underlying
     * configuration files.
     */
    public static final class DocumentAccess {

        private final SpringBeans springBeans;
        private final LockedDocument lockedDoc;
        private final File file;

        private DocumentAccess(SpringBeans springBeans, File file, LockedDocument lockedDoc) {
            this.springBeans = springBeans;
            this.lockedDoc = lockedDoc;
            this.file = file;
        }

        public SpringBeans getSpringBeans() {
            return springBeans;
        }

        public Document getDocument() {
            return lockedDoc.getDocument();
        }

        public File getFile() {
            return file;
        }

        public FileObject getFileObject() {
            return NbEditorUtilities.getFileObject(lockedDoc.getDocument());
        }

        public PositionRef createPositionRef(int offset, Bias bias) {
            return lockedDoc.createPositionRef(offset, bias);
        }
    }
}
