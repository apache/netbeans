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
package org.netbeans.modules.j2ee.persistence.editor.completion;

import org.netbeans.modules.j2ee.persistence.editor.DocumentContext;
import org.netbeans.modules.j2ee.persistence.editor.ContextUtilities;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.j2ee.persistence.editor.*;
import org.netbeans.modules.j2ee.persistence.unit.PersistenceCfgProperties;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.dom.TagElement;
import org.w3c.dom.Node;

/**
 * This class figures out the completion items for various attributes
 * 
 * @author Dongmei Cao
 */
public final class PUCompletionManager {
    
    private static Map<String, PUCompletor> completors = new HashMap<>();

    private PUCompletionManager() {
        setupCompletors();
    }

    private void setupCompletors() {


        // Items for property names 
        PUCompletor.PersistencePropertyNameCompletor propertyNamesCompletor = new PUCompletor.PersistencePropertyNameCompletor(PersistenceCfgProperties.getAllKeyAndValues());
        registerCompletor(PersistenceCfgXmlConstants.PROPERTY_TAG, PersistenceCfgXmlConstants.NAME_ATTRIB, propertyNamesCompletor);
        PUCompletor.PersistencePropertyValueCompletor propertyValuesCompletor = new PUCompletor.PersistencePropertyValueCompletor(PersistenceCfgProperties.getAllKeyAndValues());
        registerCompletor(PersistenceCfgXmlConstants.PROPERTY_TAG, PersistenceCfgXmlConstants.VALUE_ATTRIB, propertyValuesCompletor);

        // Items for mapping xml files
        PUCompletor.PersistenceMappingFileCompletor mappingFilesCompletor = new PUCompletor.PersistenceMappingFileCompletor();
        registerCompletor(PersistenceCfgXmlConstants.MAPPING_FILE, null, mappingFilesCompletor);
        
        PUCompletor.EntityClassCompletor javaClassCompletor = new PUCompletor.EntityClassCompletor();
        registerCompletor(PersistenceCfgXmlConstants.CLASS, null, javaClassCompletor);
        
        PUCompletor.ProviderCompletor providerCompletor = new PUCompletor.ProviderCompletor();
        registerCompletor(PersistenceCfgXmlConstants.PROVIDER, null, providerCompletor);
        
        PUCompletor.ExUnlistedClassesCompletor exClassesCompletor = new PUCompletor.ExUnlistedClassesCompletor();
        registerCompletor(PersistenceCfgXmlConstants.EXCLUDE_UNLISTED_CLASSES, null, exClassesCompletor);

        PUCompletor.JtaDatasourceCompletor jtaDatasourceCompletor = new PUCompletor.JtaDatasourceCompletor();
        registerCompletor(PersistenceCfgXmlConstants.JTA_DATA_SOURCE, null, jtaDatasourceCompletor);
    }
    
    private static PUCompletionManager INSTANCE = new PUCompletionManager();

    public static PUCompletionManager getDefault() {
        return INSTANCE;
    }

    public int completeAttributeValues(CompletionContext context, List<JPACompletionItem> valueItems) {
        int anchorOffset = -1;
        
        if(context.getTag() == null) {
            return anchorOffset;
        }
        
        String tagName = context.getTag().getNodeName();
        Token<XMLTokenId> attrib = ContextUtilities.getAttributeToken(context.getDocumentContext());
        String attribName = attrib != null ? attrib.text().toString(): null;

        PUCompletor completor = locateCompletor(tagName, attribName);
        if (completor != null) {
            valueItems.addAll(completor.doCompletion(context));
             if (completor.getAnchorOffset() != -1) {
                anchorOffset = completor.getAnchorOffset();
            }
        }
        
        return anchorOffset;
    }

    public int completeValues(CompletionContext context, List<JPACompletionItem> valueItems) {
        int anchorOffset = -1;         
        DocumentContext docContext = context.getDocumentContext();
        SyntaxElement curElem = docContext.getCurrentElement();
        SyntaxElement prevElem = docContext.getCurrentElement().getPrevious();
        TagElement propTag;

        String tagName;
        
        if (curElem.getType() == Node.ELEMENT_NODE && ((TagElement)curElem).isStart()) {
            tagName = curElem.getNode().getNodeName();
        } else if (prevElem.getType() == Node.ELEMENT_NODE && ((TagElement)prevElem).isStart()) {
            tagName = prevElem.getNode().getNodeName();
        } else {
            tagName = null;
        }
        PUCompletor completor = locateCompletor(tagName, null);
        if (completor != null) {
            valueItems.addAll(completor.doCompletion(context));
             if (completor.getAnchorOffset() != -1) {
                anchorOffset = completor.getAnchorOffset();
            }
        } else {

            // If current element is a start tag and its tag is <property>
            // or the current element is text and its prev is a start <property> tag,
            // then do the code completion
            if (curElem.getType() == Node.ELEMENT_NODE && ((TagElement)curElem).isStart() && 
                PersistenceCfgXmlConstants.PROPERTY_TAG.equalsIgnoreCase(curElem.getNode().getNodeName())) {
                propTag = (TagElement)curElem;
            } else if (curElem.getType() == Node.TEXT_NODE && (prevElem.getType() == Node.ELEMENT_NODE && ((TagElement)prevElem).isStart()) &&
                    PersistenceCfgXmlConstants.PROPERTY_TAG.equalsIgnoreCase(prevElem.getNode().getNodeName())) {
                propTag = (TagElement)prevElem;
            } else {
                return anchorOffset;
            }

            String propName = JPAEditorUtil.getPersistencePropertyName(propTag.getNode());
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            Object possibleValue = PersistenceCfgProperties.getPossiblePropertyValue(null, propName);

            if (possibleValue instanceof String[]) {

                // Add the values in the String[] as completion items
                String[] values = (String[])possibleValue;

                for (int i = 0; i < values.length; i++) {
                    if (values[i].startsWith(typedChars.trim())
                            || values[i].startsWith( "org.hibernate.dialect." + typedChars.trim()) ) { // NOI18N
                        JPACompletionItem item = 
                                JPACompletionItem.createHbPropertyValueItem(caretOffset-typedChars.length(), values[i]);
                        valueItems.add(item);
                    }
                }

                try {
                    anchorOffset = context.getDocumentContext().
                            runWithSequence((TokenSequence s) -> {
                                if (!s.movePrevious()) {
                                    return -1;
                                }
                                return s.offset();
                            });
                } catch (BadLocationException ex) {
                    anchorOffset = -1;
                }
            }
        }
        return anchorOffset;
    }

    public int completeAttributes(CompletionContext context, List<JPACompletionItem> attributeItems) {
        return -1;
    }

    public int completeElements(CompletionContext context, List<JPACompletionItem> elementItems) {
        return -1;
    }

    

    private void registerCompletor(String tagName, String attribName,
            PUCompletor completor) {
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

    private PUCompletor locateCompletor(String nodeName, String attributeName) {
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
