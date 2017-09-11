/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
