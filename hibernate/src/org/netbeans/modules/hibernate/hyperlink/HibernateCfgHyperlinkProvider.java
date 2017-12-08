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
import org.netbeans.modules.hibernate.cfg.HibernateCfgXmlConstants;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;

/**
 * Provides hyperlinking functionality for HibernateConfiguration files
 * 
 * @author Dongmei Cao
 */
public class HibernateCfgHyperlinkProvider implements HyperlinkProvider {

    private BaseDocument lastDocument;

    private HyperlinkProcessor currentProcessor;

    private Map<String, HyperlinkProcessor> attribValueProcessors = 
            new HashMap<String, HyperlinkProcessor>();
    
    
    public HibernateCfgHyperlinkProvider() {
        this.lastDocument = null;
        
        ResourceHyperlinkProcessor resourceHyperlinkProcessor = new ResourceHyperlinkProcessor();
        registerAttribValueHyperlinkPoint(HibernateCfgXmlConstants.MAPPING_TAG, HibernateCfgXmlConstants.RESOURCE_ATTRIB, resourceHyperlinkProcessor);
        
        JavaClassHyperlinkProcessor classHyperlinkProcessor = new JavaClassHyperlinkProcessor();
        registerAttribValueHyperlinkPoint(HibernateCfgXmlConstants.MAPPING_TAG, HibernateCfgXmlConstants.CLASS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateCfgXmlConstants.CLASS_CACHE_TAG, HibernateCfgXmlConstants.CLASS_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateCfgXmlConstants.COLLECTION_CACHE_TAG, HibernateCfgXmlConstants.COLLECTION_ATTRIB, classHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(HibernateCfgXmlConstants.LISTENER_TAG, HibernateCfgXmlConstants.CLASS_ATTRIB, classHyperlinkProcessor);
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
        if (env.getType().isValueHyperlink()) {
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
