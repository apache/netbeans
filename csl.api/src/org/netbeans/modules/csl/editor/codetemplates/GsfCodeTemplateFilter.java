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

package org.netbeans.modules.csl.editor.codetemplates;

import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.modules.csl.editor.completion.GsfCompletionProvider;

/**
 * Code template filter for GSF: Delegates to the plugin to determine which
 * templates are applicable. Based on JavaCodeTemplateFilter.
 * 
 * @author Dusan Balek
 * @author Tor Norbye
 */
public class GsfCodeTemplateFilter implements CodeTemplateFilter {
    
    private int startOffset;
    private int endOffset;
    private Set<String> templates;
    
    private GsfCodeTemplateFilter(JTextComponent component, int offset) {
        this.startOffset = offset;
        this.endOffset = component.getSelectionStart() == offset ? component.getSelectionEnd() : -1;
        Document doc = component.getDocument();
        CodeCompletionHandler completer = doc == null ? null : GsfCompletionProvider.getCompletable(doc, startOffset);
            
        if (completer != null) {
            templates = completer.getApplicableTemplates(doc, startOffset, endOffset);
        }
    }

    @Override
    public boolean accept(CodeTemplate template) {
        // Selection templates are eligible for "Surround With" should be filtered
        // based on whether the surrounding code makes sense (computed by
        // the language plugins)
        if (templates != null && template != null && template.getParametrizedText().indexOf("${selection") != -1) { // NOI18N
            return templates.contains(template.getAbbreviation()) || (template.getParametrizedText().indexOf("allowSurround") != -1); // NOI18N
        }
    
        // Other templates are filtered for code completion listing etc.
        return true;
    }
    
    public static final class Factory implements CodeTemplateFilter.Factory {
        
        @Override
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new GsfCodeTemplateFilter(component, offset);
        }
    }

}
