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
package org.netbeans.modules.hibernate.completion;

import org.netbeans.modules.hibernate.editor.HibernateEditorUtil;
import org.netbeans.modules.hibernate.editor.DocumentContext;
import org.netbeans.modules.hibernate.editor.ContextUtilities;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.hibernate.cfg.Environment;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.hibernate.cfg.HibernateCfgProperties;
import org.netbeans.modules.hibernate.cfg.HibernateCfgXmlConstants;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.dom.TagElement;
import org.openide.util.NbBundle;
import org.w3c.dom.Node;

/**
 * This class figures out the completion items for various attributes
 * 
 * @author Dongmei Cao
 */
public final class HibernateCfgCompletionManager {
    
    private static Map<String, Completor> completors = new HashMap<String, Completor>();

    private HibernateCfgCompletionManager() {
        setupCompletors();
    }

    private void setupCompletors() {

        // Completion items for configuration properties
        String[] propertyNames = new String[]{
            Environment.AUTOCOMMIT, NbBundle.getMessage(HibernateCfgCompletionManager.class, "AUTOCOMMIT_DESC"), // NOI18N
            Environment.AUTO_CLOSE_SESSION, NbBundle.getMessage(HibernateCfgCompletionManager.class, "AUTO_CLOSE_SESSION_DESC"), // NOI18N
            Environment.BYTECODE_PROVIDER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "BYTECODE_PROVIDER_DESC"), // NOI18N
            Environment.BATCH_STRATEGY, NbBundle.getMessage(HibernateCfgCompletionManager.class, "BATCH_STRATEGY_DESC"), // NOI18N
            Environment.BATCH_VERSIONED_DATA, NbBundle.getMessage(HibernateCfgCompletionManager.class, "BATCH_VERSIONED_DATA_DESC"), // NOI18N
            Environment.C3P0_ACQUIRE_INCREMENT, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_ACQUIRE_INCREMENT_DESC"), // NOI18N
            Environment.C3P0_IDLE_TEST_PERIOD, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_IDLE_TEST_PERIOD_DESC"), // NOI18N
            Environment.C3P0_MAX_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_MAX_SIZE_DESC"), // NOI18N
            Environment.C3P0_MAX_STATEMENTS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_MAX_STATEMENTS_DESC"), // NOI18N
            Environment.C3P0_MIN_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_MIN_SIZE_DESC"), // NOI18N
            Environment.C3P0_TIMEOUT, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_TIMEOUT_DESC"), // NOI18N
//            Environment.CACHE_PROVIDER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CACHE_PROVIDER_DESC"), // NOI18N
            Environment.CACHE_REGION_PREFIX, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CACHE_REGION_PREFIX_DESC"), // NOI18N
            Environment.CACHE_PROVIDER_CONFIG, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CACHE_PROVIDER_CONFIG_DESC"), // NOI18N
            Environment.CACHE_NAMESPACE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CACHE_NAMESPACE_DESC"), // NOI18N
            Environment.CONNECTION_PROVIDER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CONNECTION_PROVIDER_DESC"), // NOI18N
            Environment.CONNECTION_PREFIX, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CONNECTION_PREFIX_DESC"), // NOI18N
            Environment.CURRENT_SESSION_CONTEXT_CLASS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CURRENT_SESSION_CONTEXT_CLASS_DESC"), // NOI18N
            Environment.DATASOURCE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DATASOURCE_DESC"), // NOI18N
            Environment.DEFAULT_BATCH_FETCH_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DEFAULT_BATCH_FETCH_SIZE_DESC"), // NOI18N
            Environment.DEFAULT_CATALOG, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DEFAULT_CATALOG_DESC"), // NOI18N
            Environment.DEFAULT_ENTITY_MODE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DEFAULT_ENTITY_MODE_DESC"), // NOI18N
            Environment.DEFAULT_SCHEMA, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DEFAULT_SCHEMA_DESC"), // NOI18N
            Environment.DIALECT, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DIALECT_DESC"), // NOI18N
            Environment.DRIVER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DRIVER_DESC"), // NOI18N
            Environment.FLUSH_BEFORE_COMPLETION, NbBundle.getMessage(HibernateCfgCompletionManager.class, "FLUSH_BEFORE_COMPLETION_DESC"), // NOI18N
            Environment.FORMAT_SQL, NbBundle.getMessage(HibernateCfgCompletionManager.class, "FORMAT_SQL_DESC"), // NOI18N
            Environment.GENERATE_STATISTICS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "GENERATE_STATISTICS_DESC"), // NOI18N
            Environment.HBM2DDL_AUTO, NbBundle.getMessage(HibernateCfgCompletionManager.class, "HBM2DDL_AUTO_DESC"), // NOI18N
            Environment.ISOLATION, NbBundle.getMessage(HibernateCfgCompletionManager.class, "ISOLATION_DESC"), // NOI18N
//            Environment.JACC_CONTEXTID, NbBundle.getMessage(HibernateCfgCompletionManager.class, "JACC_CONTEXTID_DESC"), // NOI18N
            Environment.JNDI_CLASS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "JNDI_CLASS_DESC"), // NOI18N
            Environment.JNDI_URL, NbBundle.getMessage(HibernateCfgCompletionManager.class, "JNDI_URL_DESC"), // NOI18N
            Environment.JPAQL_STRICT_COMPLIANCE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "JPAQL_STRICT_COMPLIANCE_DESC"), // NOI18N
            Environment.MAX_FETCH_DEPTH, NbBundle.getMessage(HibernateCfgCompletionManager.class, "MAX_FETCH_DEPTH_DESC"), // NOI18N
            Environment.ORDER_UPDATES, NbBundle.getMessage(HibernateCfgCompletionManager.class, "ORDER_UPDATES_DESC"), // NOI18N
            Environment.OUTPUT_STYLESHEET, NbBundle.getMessage(HibernateCfgCompletionManager.class, "OUTPUT_STYLESHEET_DESC"), // NOI18N
            Environment.PASS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PASS_DESC"), // NOI18N
            Environment.POOL_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "POOL_SIZE_DESC"), // NOI18N
            Environment.PROXOOL_EXISTING_POOL, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PROXOOL_EXISTING_POOL_DESC"), // NOI18N
            Environment.PROXOOL_POOL_ALIAS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PROXOOL_POOL_ALIAS_DESC"), // NOI18N
            Environment.PROXOOL_PREFIX, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PROXOOL_PREFIX_DESC"), // NOI18N
            Environment.PROXOOL_PROPERTIES, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PROXOOL_PROPERTIES_DESC"), // NOI18N
            Environment.PROXOOL_XML, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PROXOOL_XML_DESC"), // NOI18N
            Environment.QUERY_CACHE_FACTORY, NbBundle.getMessage(HibernateCfgCompletionManager.class, "QUERY_CACHE_FACTORY_DESC"), // NOI18N
            Environment.QUERY_TRANSLATOR, NbBundle.getMessage(HibernateCfgCompletionManager.class, "QUERY_TRANSLATOR_DESC"), // NOI18N
            Environment.QUERY_SUBSTITUTIONS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "QUERY_SUBSTITUTIONS_DESC"), // NOI18N
            Environment.QUERY_STARTUP_CHECKING, NbBundle.getMessage(HibernateCfgCompletionManager.class, "QUERY_STARTUP_CHECKING_DESC"), // NOI18N
            Environment.RELEASE_CONNECTIONS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "RELEASE_CONNECTIONS_DESC"), // NOI18N
            Environment.SESSION_FACTORY_NAME, NbBundle.getMessage(HibernateCfgCompletionManager.class, "SESSION_FACTORY_NAME_DESC"), // NOI18N
            Environment.SHOW_SQL, NbBundle.getMessage(HibernateCfgCompletionManager.class, "SHOW_SQL_DESC"), // NOI18N
            Environment.SQL_EXCEPTION_CONVERTER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "SQL_EXCEPTION_CONVERTER_DESC"), // NOI18N
            Environment.STATEMENT_BATCH_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "STATEMENT_BATCH_SIZE_DESC"), // NOI18N
            Environment.STATEMENT_FETCH_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "STATEMENT_FETCH_SIZE_DESC"), // NOI18N
            Environment.TRANSACTION_STRATEGY, NbBundle.getMessage(HibernateCfgCompletionManager.class, "TRANSACTION_STRATEGY_DESC"), // NOI18N
//            Environment.TRANSACTION_MANAGER_STRATEGY, NbBundle.getMessage(HibernateCfgCompletionManager.class, "TRANSACTION_MANAGER_STRATEGY_DESC"), // NOI18N
            Environment.URL, NbBundle.getMessage(HibernateCfgCompletionManager.class, "URL_DESC"), // NOI18N
            Environment.USER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USER_DESC"), // NOI18N
            Environment.USE_GET_GENERATED_KEYS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_GET_GENERATED_KEYS_DESC"), // NOI18N
            Environment.USE_SCROLLABLE_RESULTSET, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_SCROLLABLE_RESULTSET_DESC"), // NOI18N
            Environment.USE_STREAMS_FOR_BINARY, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_STREAMS_FOR_BINARY_DESC"), // NOI18N
            Environment.USE_IDENTIFIER_ROLLBACK, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_IDENTIFIER_ROLLBACK_DESC"), // NOI18N
            Environment.USE_SQL_COMMENTS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_SQL_COMMENTS_DESC"), // NOI18N
            Environment.USE_MINIMAL_PUTS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_MINIMAL_PUTS_DESC"), // NOI18N
            Environment.USE_QUERY_CACHE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_QUERY_CACHE_DESC"), // NOI18N
            Environment.USE_SECOND_LEVEL_CACHE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_SECOND_LEVEL_CACHE_DESC"), // NOI18N
            Environment.USE_STRUCTURED_CACHE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_STRUCTURED_CACHE_DESC"), // NOI18N
//            Environment.USER_TRANSACTION, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USER_TRANSACTION_DESC"), // NOI18N
            Environment.USE_REFLECTION_OPTIMIZER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_REFLECTION_OPTIMIZER_DESC"), // NOI18N
            Environment.WRAP_RESULT_SETS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "WRAP_RESULT_SETS_DESC") // NOI18N
        };

        // Items for property names 
        Completor.HbPropertyNameCompletor propertyNamesCompletor = new Completor.HbPropertyNameCompletor(propertyNames);
        registerCompletor(HibernateCfgXmlConstants.PROPERTY_TAG, HibernateCfgXmlConstants.NAME_ATTRIB, propertyNamesCompletor);

        // Items for mapping xml files
        Completor.HbMappingFileCompletor mappingFilesCompletor = new Completor.HbMappingFileCompletor();
        registerCompletor(HibernateCfgXmlConstants.MAPPING_TAG, HibernateCfgXmlConstants.RESOURCE_ATTRIB, mappingFilesCompletor);
        
        Completor.JavaClassCompletor javaClassCompletor = new Completor.JavaClassCompletor(false);
        registerCompletor(HibernateCfgXmlConstants.CLASS_CACHE_TAG, HibernateCfgXmlConstants.CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateCfgXmlConstants.COLLECTION_CACHE_TAG, HibernateCfgXmlConstants.COLLECTION_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateCfgXmlConstants.LISTENER_TAG, HibernateCfgXmlConstants.CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateCfgXmlConstants.MAPPING_TAG, HibernateCfgXmlConstants.CLASS_ATTRIB, javaClassCompletor);
        
        Completor.JavaClassCompletor javaPkgCompletor = new Completor.JavaClassCompletor(true);
        registerCompletor(HibernateCfgXmlConstants.MAPPING_TAG, HibernateCfgXmlConstants.PACKAGE_ATTRIB, javaPkgCompletor);
    }
    
    private static HibernateCfgCompletionManager INSTANCE = new HibernateCfgCompletionManager();

    public static HibernateCfgCompletionManager getDefault() {
        return INSTANCE;
    }

    public int completeAttributeValues(CompletionContext context, List<HibernateCompletionItem> valueItems) {
        int anchorOffset = -1;
        
        if(context.getTag() == null)
            return anchorOffset;
        
        String tagName = context.getTag().getNodeName();
        Token<XMLTokenId> attrib = ContextUtilities.getAttributeToken(context.getDocumentContext());
        String attribName = attrib != null ? attrib.text().toString(): null;

        Completor completor = locateCompletor(tagName, attribName);
        if (completor != null) {
            valueItems.addAll(completor.doCompletion(context));
             if (completor.getAnchorOffset() != -1) {
                anchorOffset = completor.getAnchorOffset();
            }
        }
        
        return anchorOffset;
    }

    public int completeValues(CompletionContext context, List<HibernateCompletionItem> valueItems) {
        int anchorOffset = -1;         
        DocumentContext docContext = context.getDocumentContext();
        SyntaxElement curElem = docContext.getCurrentElement();
        SyntaxElement prevElem = docContext.getCurrentElement().getPrevious();
        TagElement propTag = null;

        // If current element is a start tag and its tag is <property>
        // or the current element is text and its prev is a start <property> tag,
        // then do the code completion
        if (curElem.getType() == Node.ELEMENT_NODE &&
            ((TagElement)curElem).isStart() && HibernateCfgXmlConstants.PROPERTY_TAG.equalsIgnoreCase(curElem.getNode().getNodeName())) {
            propTag = (TagElement) curElem;
        } else if (curElem.getType() == Node.TEXT_NODE && 
                (prevElem.getType() == Node.ELEMENT_NODE && ((TagElement)prevElem).isStart() &&
                HibernateCfgXmlConstants.PROPERTY_TAG.equalsIgnoreCase(prevElem.getNode().getNodeName()))) {
            propTag = (TagElement) prevElem;
        } else {
            return anchorOffset;
        }
        
        String propName = HibernateEditorUtil.getHbPropertyName(propTag.getNode());
        int caretOffset = context.getCaretOffset();
        String typedChars = context.getTypedPrefix();
        
        Object possibleValue = HibernateCfgProperties.getPossiblePropertyValue(propName);
        
        if (possibleValue instanceof String[]) {
            
            // Add the values in the String[] as completion items
            String[] values = (String[])possibleValue;
            
            for (int i = 0; i < values.length; i++) {
                if (values[i].startsWith(typedChars.trim())
                        || values[i].startsWith( "org.hibernate.dialect." + typedChars.trim()) ) { // NOI18N
                    HibernateCompletionItem item = 
                            HibernateCompletionItem.createHbPropertyValueItem(caretOffset-typedChars.length(), values[i]);
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
        
        return anchorOffset;
    }

    public int completeAttributes(CompletionContext context, List<HibernateCompletionItem> attributeItems) {
        return -1;
    }

    public int completeElements(CompletionContext context, List<HibernateCompletionItem> elementItems) {
        return -1;
    }

    

    private void registerCompletor(String tagName, String attribName,
            Completor completor) {
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

    private Completor locateCompletor(String nodeName, String attributeName) {
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
