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

package org.netbeans.modules.xml.schema.completion;

import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SchemaBasedCompletionProvider implements CompletionProvider {
    
    /**
     * Creates a new instance of SchemaBasedCompletionProvider
     */
    public SchemaBasedCompletionProvider() {
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        BaseDocument doc = Utilities.getDocument(component);
	if ( typedText ==null || typedText.trim().length() ==0 ){
            return 0;
        }
        // do not pop up if the end of text contains some whitespaces.
        if (Character.isWhitespace(typedText.charAt(typedText.length() - 1) )) {
            return 0;
        }
        if(doc == null)
            return 0;
        XMLSyntaxSupport support = XMLSyntaxSupport.getSyntaxSupport(doc);
        if(support != null && CompletionUtil.noCompletion(component) || 
                !CompletionUtil.canProvideCompletion(doc)) {
            return 0;
        }
        
        return COMPLETION_QUERY_TYPE;
    }
        
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE || queryType == COMPLETION_ALL_QUERY_TYPE) {
            return new AsyncCompletionTask(new CompletionQuery(CompletionUtil.getPrimaryFile(component.getDocument())), component);
        }
        
        return null;
    }
    
        
}
