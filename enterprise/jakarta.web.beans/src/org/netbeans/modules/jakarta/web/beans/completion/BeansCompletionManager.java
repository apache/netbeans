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
package org.netbeans.modules.jakarta.web.beans.completion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.w3c.dom.Node;

/**
 * This class figures out the completion items for various attributes
 * 
 * @author Dongmei Cao
 */
public final class BeansCompletionManager {
    
    private static Map<String, BeansCompletor> completors = new HashMap<>();

    private BeansCompletionManager() {
        setupCompletors();
    }

    private void setupCompletors() {


        // Items for property names 
        BeansCompletor.JavaClassesCompletor javaClassCompletor = new BeansCompletor.JavaClassesCompletor(BeansCompletor.TAG.CLASS);
        registerCompletor(BeansXmlConstants.CLASS, null, javaClassCompletor);
        
        BeansCompletor.JavaClassesCompletor stereotypeClassCompletor = new BeansCompletor.JavaClassesCompletor(BeansCompletor.TAG.STEREOTYPE);
        registerCompletor(BeansXmlConstants.STEREOTYPE, null, stereotypeClassCompletor);
    }
    
    private static final BeansCompletionManager INSTANCE = new BeansCompletionManager();

    public static BeansCompletionManager getDefault() {
        return INSTANCE;
    }

    public int completeAttributeValues(CompletionContext context, List<BeansCompletionItem> valueItems) {
        int anchorOffset = -1;
        
        if(context.getTag() == null) {
            return anchorOffset;
        }
        
        String tagName = context.getTag().getNodeName();
        Token<XMLTokenId> attrib = ContextUtilities.getAttributeToken(context.getDocumentContext());
        String attribName = attrib != null ? attrib.text().toString(): null;

        BeansCompletor completor = locateCompletor(tagName, attribName);
        if (completor != null) {
            valueItems.addAll(completor.doCompletion(context));
             if (completor.getAnchorOffset() != -1) {
                anchorOffset = completor.getAnchorOffset();
            }
        }
        
        return anchorOffset;
    }

    public int completeValues(CompletionContext context, List<BeansCompletionItem> valueItems) {
        int anchorOffset = -1;         
        DocumentContext docContext = context.getDocumentContext();
        SyntaxElement curElem = docContext.getCurrentElement();
        SyntaxElement prevElem = docContext.getCurrentElement().getPrevious();

        String tagName = curElem.getType() == Node.ELEMENT_NODE ? curElem.getNode().getNodeName() : null;
        BeansCompletor completor = locateCompletor(tagName, null);
        if (completor != null) {
            valueItems.addAll(completor.doCompletion(context));
             if (completor.getAnchorOffset() != -1) {
                anchorOffset = completor.getAnchorOffset();
            }
        } 
        return anchorOffset;
    }

    public int completeAttributes(CompletionContext context, List<BeansCompletionItem> attributeItems) {
        return -1;
    }

    public int completeElements(CompletionContext context, List<BeansCompletionItem> elementItems) {
        return -1;
    }

    

    private void registerCompletor(String tagName, String attribName,
            BeansCompletor completor) {
        completors.put(createRegisteredName(tagName, attribName), completor);
    }

    private static String createRegisteredName(String nodeName, String attributeName) {
        StringBuilder builder = new StringBuilder();
        if (nodeName != null && nodeName.trim().length() > 0) {
            builder.append("/nodeName=");  // NOI18N
            builder.append(nodeName);
        } else {
            builder.append("/nodeName=");  // NOI18N
            builder.append("*");  // NOI18N
        }

        if (attributeName != null && attributeName.trim().length() > 0) {
            builder.append("/attribute="); // NOI18N
            builder.append(attributeName);
        }

        return builder.toString();
    }

    private BeansCompletor locateCompletor(String nodeName, String attributeName) {
        String key = createRegisteredName(nodeName, attributeName);
        if (completors.containsKey(key)) {
            return completors.get(key);
        }

        key = createRegisteredName("*", attributeName); // NOI18N
        if (completors.containsKey(key)) {
            return completors.get(key);
        }

        return null;
    }
}
