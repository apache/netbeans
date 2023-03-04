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

package org.netbeans.lib.editor.codetemplates.spi;

import java.util.List;
import javax.swing.JTextArea;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Filter accepting code templates being displayed in a code completion popup.
 * It is also used for editor hints (code templates) over a text selection.
 *
 * @author Dusan Balek
 */
public interface CodeTemplateFilter {
  
    /**
     * Accept or reject the given code template.
     * 
     * @param template non-null template to accept or reject.
     * @return true to accept the given code template or false to reject it.
     */
    boolean accept(CodeTemplate template);
    
    /**
     * Factory for producing of the code template filters.
     * <br/>
     * It should be registered in the MimeLookup for a given mime-type.
     */
    @MimeLocation(subfolderName="CodeTemplateFilterFactories")
    public interface Factory {
        
        /**
         * Create code template filter for the given context.
         * 
         * @param component non-null component for which the filter is being created.
         * @param offset &gt;=0 offset for which the filter is being created.
         * @return non-null code template filter instance.
         */
        CodeTemplateFilter createFilter(JTextComponent component, int offset);

        /**
         * Create code template filter for the given context.
         *
         * @param doc non-null document for which the filter is being created.
         * @param startOffset &gt;=0 start offset for which the filter is being created.
         * @param endOffset &gt;=startOffset end offset for which the filter is being created.
         * @return non-null code template filter instance.
         * @since 1.57
         */
        default CodeTemplateFilter createFilter(Document doc, int startOffset, int endOffset) {
            JTextArea component = new JTextArea(doc);
            component.setSelectionStart(startOffset);
            component.setSelectionEnd(endOffset);
            return createFilter(component, startOffset);
        }
    }
    
    /**
     * Factory for producing of the code template filters that filter templates
     * based on their contexts.
     * <br/>
     * It should be registered in the MimeLookup for a given mime-type.
     * 
     * @since 1.34
     */
    @MimeLocation(subfolderName="CodeTemplateFilterFactories")
    public interface ContextBasedFactory extends Factory {
        
        /**
         * Get the list of all code template contexts supported by filters
         * created by the factory.
         * @return non-null list of supported contexts.
         */
        List<String> getSupportedContexts();
    }
}
