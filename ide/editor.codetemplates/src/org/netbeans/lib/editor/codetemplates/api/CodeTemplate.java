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

import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.CodeTemplateManagerOperation;
import org.netbeans.lib.editor.codetemplates.ParametrizedTextParser;

/**
 * Code template is represented by a parametrized text
 * that, after pre-processing, can be pasted into a given
 * text component.
 * <br>
 * Code template instances are either persistent (can be retrieved by
 * {@link CodeTemplateManager#getCodeTemplates()})
 * or temporary code templates that can be created
 * by {@link CodeTemplateManager#createTemporary(String)}.
 * 
 * @author Miloslav Metelka
 */
public final class CodeTemplate {
    
    private final CodeTemplateManagerOperation managerOperation;
    private final String abbreviation;
    private final String description;
    private final String parametrizedText;
    private final List<String> contexts;
    private final String mimePath;
    
    private String singleLineText = null;
    
    CodeTemplate(
        CodeTemplateManagerOperation managerOperation,
        String abbreviation, 
        String description, 
        String parametrizedText, 
        List<String> contexts, 
        String mimePath
    ) {
        
        assert (managerOperation != null);
        if (abbreviation == null) {
            throw new NullPointerException("abbreviation cannot be null"); // NOI18N
        }
        if (parametrizedText == null) {
            throw new NullPointerException("parametrizedText cannot be null"); // NOI18N
        }

        this.managerOperation = managerOperation;
        this.abbreviation = abbreviation;
        this.description = description;
        this.parametrizedText = parametrizedText;
        this.contexts = contexts;
        this.mimePath = mimePath;
    }

    /**
     * Insert this code template into the given text component
     * at the caret position.
     *
     * @param component non-null text component.
     */
    public void insert(JTextComponent component) {
        CodeTemplateManagerOperation.insert(this, component);
    }

    /**
     * Get abbreviation that triggers expansion of this code template.
     *
     * @return non-null abbreviation that expands to this template.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Get textual description of this code template.
     * <br>
     * It's being displayed e.g. in the code completion window.
     * 
     * @return The description text or <code>null</code> if this template does
     *   not have description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the parametrized text of this code template.
     * <br>
     * The parameters have form "${...}" and there can be arbitrary
     * number of them.
     * 
     * @return non-null parametrized text.
     */
    public String getParametrizedText() {
        return parametrizedText;
    }
    
    public List<String> getContexts() {
        return contexts;
    }

    /**
     * Api-package accessor's method.
     * @return CodeTemplateManagerOperation
     */
    /* package */ CodeTemplateManagerOperation getOperation() {
        return managerOperation;
    }

    /* package */ String getSingleLineText() {
        if (singleLineText == null) {
            String singleLine;
            int nlInd = parametrizedText.indexOf('\n'); //NOI18N
            if (nlInd != -1) {
                singleLine = parametrizedText.substring(0, nlInd) + "..."; // NOI18N
            } else {
                singleLine = parametrizedText;
            }
            
            singleLineText = ParametrizedTextParser.parseToHtml(new StringBuffer(), singleLine).toString();
        }
        return singleLineText;
    }
    
    /* package */ String getMimePath() {
        return mimePath;
    }
    
    @Override
    public String toString() {
        return "abbrev='" + getAbbreviation() + "', parametrizedText='" + getParametrizedText() + "'"; // NOI18N
    }
    
}
