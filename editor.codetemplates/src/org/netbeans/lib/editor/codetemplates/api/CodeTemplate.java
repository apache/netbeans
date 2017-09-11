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

import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.CodeTemplateManagerOperation;
import org.netbeans.lib.editor.codetemplates.ParametrizedTextParser;

/**
 * Code template is represented by a parametrized text
 * that, after pre-processing, can be pasted into a given
 * text component.
 * <br/>
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
