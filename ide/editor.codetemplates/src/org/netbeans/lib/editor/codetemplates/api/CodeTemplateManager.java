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

package org.netbeans.lib.editor.codetemplates.api;

import java.util.Collection;
import java.util.List;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.lib.editor.codetemplates.CodeTemplateApiPackageAccessor;
import org.netbeans.lib.editor.codetemplates.CodeTemplateManagerOperation;

/**
 * Code template manager maintains list of code templates
 * for a particular document type and allows temporary
 * code templates to be created.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateManager {
    
    static {
        CodeTemplateApiPackageAccessor.register(new ApiAccessor());
    }
    
    /**
     * Get an instance of the manager for the given document.
     *
     * @param doc document for which the instance of the manager should be obtained.
     * @return The code template manager for the document.
     */
    public static CodeTemplateManager get(Document doc) {
        return CodeTemplateManagerOperation.getManager(doc);
    }
    
    private CodeTemplateManagerOperation operation;
    
    private CodeTemplateManager(CodeTemplateManagerOperation operation) {
        this.operation = operation;
    }

    /**
     * Get unmodifiable collection of the code templates for this manager.
     * <br>
     * This method will block until the code templates will be loaded.
     *
     * @return non-null unmodifiable collection of the code templates.
     */
    public Collection<? extends CodeTemplate> getCodeTemplates() {
        waitLoaded(); // Wait for the manager to become loaded with the templates.
        return operation.getCodeTemplates();
    }
    
    /**
     * Create temporary code template for an ad hoc insertion.
     *
     * @param parametrizedText non-null parametrized text of the code template.
     * @return non-null code template that can be inserted into the document.
     */
    public CodeTemplate createTemporary(String parametrizedText) {
        return new CodeTemplate(operation, "", "", parametrizedText, null, operation.getMimePath()); // NOI18N
    }
    
    /**
     * Check whether asynchronous loading of the code templates into this manager
     * was already finished.
     *
     * @return true if this manager is already loaded with the code templates
     *  or false if the templates are still being loaded.
     * @see #registerLoadedListener(ChangeListener)
     */
    public boolean isLoaded() {
        return operation.isLoaded();
    }
    
    /**
     * Wait for this manager to become loaded with the code templates.
     * <br>
     * If this manager is already loaded this method returns immediately.
     */
    public void waitLoaded() {
        operation.waitLoaded();
    }

    /**
     * Register change listener waiting for the state when this manager
     * becomes loaded with the code templates.
     *
     * <p>
     * In case the manager is already loaded the registered listener
     * will be fired immediately in the same thread.
     *
     * <p>
     * There is no unregistration of the listeners because they are
     * forgotten automatically once the manager becomes loaded.
     *
     * @param listener change listener to be fired once this manager becomes loaded
     *  by the code templates.
     */
    public void registerLoadedListener(ChangeListener listener) {
        operation.registerLoadedListener(listener);
    }
    
    CodeTemplateManagerOperation getOperation() {
        return operation;
    }

    private static final class ApiAccessor extends CodeTemplateApiPackageAccessor {

        public CodeTemplateManager createCodeTemplateManager(CodeTemplateManagerOperation operation) {
            return new CodeTemplateManager(operation);
        }

        public CodeTemplateManagerOperation getOperation(CodeTemplateManager manager) {
            return manager.getOperation();
        }

        public CodeTemplateManagerOperation getOperation(CodeTemplate codeTemplate) {
            return codeTemplate.getOperation();
        }
        
        public CodeTemplate createCodeTemplate(
            CodeTemplateManagerOperation managerOperation,
            String abbreviation, 
            String description, 
            String parametrizedText,
            List<String> contexts,
            String mimePath
        ) {
            return new CodeTemplate(managerOperation, abbreviation, description, parametrizedText, contexts, mimePath);
        }

        public String getSingleLineText(CodeTemplate codeTemplate) {
            return codeTemplate.getSingleLineText();
        }
        
        public String getCodeTemplateMimePath(CodeTemplate codeTemplate) {
            return codeTemplate.getMimePath();
        }
    } // End of ApiAccessor class
}
