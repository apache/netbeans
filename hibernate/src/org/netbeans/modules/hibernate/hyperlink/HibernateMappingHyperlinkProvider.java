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

package org.netbeans.modules.hibernate.hyperlink;

import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.hibernate.mapping.HibernateMappingXmlConstants;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;

/**
 * Provides hyperlinking functionality for Hibernate mapping files
 * 
 * @author Dongmei Cao
 */
public class HibernateMappingHyperlinkProvider implements HyperlinkProvider {

    private BaseDocument lastDocument;

    private HyperlinkProcessor currentProcessor;

    private Map<String, HyperlinkProcessor> attribValueProcessors = 
            new HashMap<String, HyperlinkProcessor>();
    
    
    public HibernateMappingHyperlinkProvider() {
        this.lastDocument = null;
       
        JavaClassHyperlinkProcessor classHyperlinkProcessor = new JavaClassHyperlinkProcessor();
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.CLASS_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.SUBCLASS_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.SUBCLASS_TAG, HibernateMappingXmlConstants.EXTENDS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.JOINED_SUBCLASS_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.JOINED_SUBCLASS_TAG, HibernateMappingXmlConstants.EXTENDS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.JOINED_SUBCLASS_TAG, HibernateMappingXmlConstants.PERSISTER_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.UNION_SUBCLASS_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.UNION_SUBCLASS_TAG, HibernateMappingXmlConstants.EXTENDS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.UNION_SUBCLASS_TAG, HibernateMappingXmlConstants.PERSISTER_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.ONE_TO_MANY_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.COMPOSITE_ID_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.MANY_TO_ONE_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.ONE_TO_ONE_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.COMPONENT_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.IMPORT_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.MANY_TO_MANY_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, classHyperlinkProcessor);
    
        PropertyHyperlinkProcessor propertyHyperlinkProcessor = new PropertyHyperlinkProcessor();
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.PROPERTY_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.ID_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.SET_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.COMPOSITE_ID_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.KEY_PROPERTY_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.VERSION_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.TIMESTAMP_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.MANY_TO_ONE_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.ONE_TO_ONE_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.COMPONENT_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.ANY_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.MAP_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateMappingXmlConstants.LIST_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyHyperlinkProcessor);
    }
    
    private void registerAttribValueHyperlinkPoint(String tagName, String attribName, 
            HyperlinkProcessor processor) {
        attribValueProcessors.put(createRegisteredName(tagName, attribName), processor);
    }
    
    public boolean isHyperlinkPoint(Document document, int offset) {
        if (XMLSyntaxSupport.getSyntaxSupport(document) == null) {
            return false;
        }

        HyperlinkEnv env = new HyperlinkEnv(document, offset);
        if(env.getType().isValueHyperlink()) {
            currentProcessor = locateHyperlinkProcessor(env.getTagName(), env.getAttribName(), attribValueProcessors);
        } else {
            currentProcessor = null;
        }
        
        return currentProcessor != null;
    }

    public int[] getHyperlinkSpan(Document document, int offset) {
        if (!(document instanceof BaseDocument)) {
            return null;
        }
        
        if(currentProcessor == null) {
            return null;
        }

        HyperlinkEnv env = new HyperlinkEnv(document, offset);
        return currentProcessor.getSpan(env);
    }

    public void performClickAction(Document document, int offset) {
        HyperlinkEnv env = new HyperlinkEnv(document, offset);
        if(currentProcessor != null) {
            currentProcessor.process(env);
        }
    }
    
    protected String createRegisteredName(String nodeName, String attributeName) {
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
    
    private HyperlinkProcessor locateHyperlinkProcessor(String nodeName, 
            String attributeName, Map<String, HyperlinkProcessor> processors) {
        String key = createRegisteredName(nodeName, attributeName);
        if(processors.containsKey(key)) {
            return processors.get(key);
        }
               
        key = createRegisteredName("*", attributeName); // NOI18N
        if(processors.containsKey(key)) {
            return processors.get(key);
        }
        
        return null;
    }
}
