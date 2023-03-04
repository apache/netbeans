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
    
    private GsfCodeTemplateFilter(Document doc, int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
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
            return createFilter(component.getDocument(), offset, component.getSelectionStart() == offset ? component.getSelectionEnd() : -1);
        }

        @Override
        public CodeTemplateFilter createFilter(Document doc, int startOffset, int endOffset) {
            return new GsfCodeTemplateFilter(doc, startOffset, endOffset);
        }
    }

}
