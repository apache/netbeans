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
     * <br/>
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
