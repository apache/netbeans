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

package org.netbeans.modules.spring.beans.hyperlink;

import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.spring.beans.BeansAttributes;
import org.netbeans.modules.spring.beans.BeansElements;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.modules.spring.java.Public;
import org.netbeans.modules.spring.java.Static;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;

/**
 * Provides hyperlinking functionality for Spring XML Configuration files
 * 
 * @author Rohan Ranade
 */
@MimeRegistration(mimeType = "x-springconfig+xml", service = HyperlinkProvider.class, position = 1000)
public class SpringXMLConfigHyperlinkProvider implements HyperlinkProvider {

    private static final String P_NAMESPACE = "http://www.springframework.org/schema/p"; // NOI18N
    
    private Map<String, HyperlinkProcessor> attribValueProcessors = 
            new HashMap<String, HyperlinkProcessor>();
    
    private PHyperlinkProcessor pHyperlinkProcessor = new PHyperlinkProcessor();
    
    public SpringXMLConfigHyperlinkProvider() {
        JavaClassHyperlinkProcessor classHyperlinkProcessor = new JavaClassHyperlinkProcessor();
        registerAttribValueProcessor(BeansElements.BEAN, BeansAttributes.CLASS, classHyperlinkProcessor);
        
        JavaMethodHyperlinkProcessor methodHyperlinkProcessor 
                = new JavaMethodHyperlinkProcessor(Public.DONT_CARE, Static.NO, 0);
        registerAttribValueProcessor(BeansElements.BEAN, BeansAttributes.INIT_METHOD, methodHyperlinkProcessor);
        registerAttribValueProcessor(BeansElements.BEAN, BeansAttributes.DESTROY_METHOD, methodHyperlinkProcessor);
        
        FactoryMethodHyperlinkProcessor factoryMethodHyperlinkProcessor 
                = new FactoryMethodHyperlinkProcessor();
        registerAttribValueProcessor(BeansElements.BEAN, BeansAttributes.FACTORY_METHOD, factoryMethodHyperlinkProcessor);
        
        methodHyperlinkProcessor 
                = new JavaMethodHyperlinkProcessor(Public.DONT_CARE, Static.NO, 0);
        registerAttribValueProcessor(BeansElements.LOOKUP_METHOD, BeansAttributes.NAME, methodHyperlinkProcessor);
        
        methodHyperlinkProcessor 
                = new JavaMethodHyperlinkProcessor(Public.DONT_CARE, Static.NO, -1);
        registerAttribValueProcessor(BeansElements.REPLACED_METHOD, BeansAttributes.NAME, methodHyperlinkProcessor);
        
        ResourceHyperlinkProcessor resourceHyperlinkProcessor = new ResourceHyperlinkProcessor();
        registerAttribValueProcessor(BeansElements.IMPORT, BeansAttributes.RESOURCE, resourceHyperlinkProcessor);
        
        PropertyHyperlinkProcessor propertyHyperlinkProcessor = new PropertyHyperlinkProcessor();
        registerAttribValueProcessor(BeansElements.PROPERTY, BeansAttributes.NAME, propertyHyperlinkProcessor);
        
        BeansRefHyperlinkProcessor beansRefHyperlinkProcessor = new BeansRefHyperlinkProcessor(true);
        registerAttribValueProcessor(BeansElements.BEAN, BeansAttributes.FACTORY_BEAN, beansRefHyperlinkProcessor);
        registerAttribValueProcessor(BeansElements.BEAN, BeansAttributes.DEPENDS_ON, beansRefHyperlinkProcessor);
        registerAttribValueProcessor(BeansElements.BEAN, BeansAttributes.PARENT, beansRefHyperlinkProcessor);
        registerAttribValueProcessor(BeansElements.LOOKUP_METHOD, BeansAttributes.BEAN, beansRefHyperlinkProcessor);
        registerAttribValueProcessor(BeansElements.REPLACED_METHOD, BeansAttributes.REPLACER, beansRefHyperlinkProcessor);
        registerAttribValueProcessor(BeansElements.PROPERTY, BeansAttributes.REF, beansRefHyperlinkProcessor);
        registerAttribValueProcessor(BeansElements.ALIAS, BeansAttributes.NAME, beansRefHyperlinkProcessor);
        registerAttribValueProcessor(BeansElements.CONSTRUCTOR_ARG, BeansAttributes.REF, beansRefHyperlinkProcessor);
        registerAttribValueProcessor(BeansElements.REF, BeansAttributes.BEAN, beansRefHyperlinkProcessor);
        registerAttribValueProcessor(BeansElements.IDREF, BeansAttributes.BEAN, beansRefHyperlinkProcessor);
        
        beansRefHyperlinkProcessor = new BeansRefHyperlinkProcessor(false);
        registerAttribValueProcessor(BeansElements.IDREF, BeansAttributes.LOCAL, beansRefHyperlinkProcessor);
        registerAttribValueProcessor(BeansElements.REF, BeansAttributes.LOCAL, beansRefHyperlinkProcessor);
    }
    
    private void registerAttribValueProcessor(String tagName, String attribName, 
            HyperlinkProcessor processor) {
        attribValueProcessors.put(createRegisteredName(tagName, attribName), processor);
    }
    
    public boolean isHyperlinkPoint(Document document, int offset) {
        if (!(document instanceof BaseDocument)) {
            return false;
        }
        if (XMLSyntaxSupport.getSyntaxSupport(document) == null) {
            return false;
        }

        HyperlinkEnv env = new HyperlinkEnv(document, offset);
        HyperlinkProcessor processor = locateProcessor(env);
        return processor != null;
    }

    public int[] getHyperlinkSpan(Document document, int offset) {
        if (!(document instanceof BaseDocument)) {
            return null;
        }
        
        HyperlinkEnv env = new HyperlinkEnv(document, offset);
        HyperlinkProcessor processor = locateProcessor(env);
        if(processor == null) {
            return new int[] { -1, -1 };
        }
        
        return processor.getSpan(env);
    }

    public void performClickAction(Document document, int offset) {
        HyperlinkEnv env = new HyperlinkEnv(document, offset);
        HyperlinkProcessor processor = locateProcessor(env);
        if(processor != null) {
            processor.process(env);
        }
    }
    
    private HyperlinkProcessor locateProcessor(HyperlinkEnv env) {
        HyperlinkProcessor processor = null;
        if(env.getType().isValueHyperlink()) {
            processor = locateAttributeValueProcessor(env.getTagName(), env.getAttribName());
            if(processor == null && isPNamespaceName(env, env.getAttribName())) {
                processor = pHyperlinkProcessor;
            }
        } else if(env.getType().isAttributeHyperlink()) {
            if (isPNamespaceName(env, env.getAttribName())) {
                processor = pHyperlinkProcessor;
            }
        }
        
        return processor;
    }
    
    protected String createRegisteredName(String nodeName, String attributeName) {
        StringBuilder builder = new StringBuilder();
        if(StringUtils.hasText(nodeName)) {
            builder.append("/nodeName=");  // NOI18N
            builder.append(nodeName);
        } else {
            builder.append("/nodeName=");  // NOI18N
            builder.append("*");  // NOI18N
        }
        
        if(StringUtils.hasText(attributeName)) {
            builder.append("/attribute="); // NOI18N
            builder.append(attributeName);
        }
        
        return builder.toString();
    }
    
    private HyperlinkProcessor locateAttributeValueProcessor(String nodeName, 
            String attributeName) {
        String key = createRegisteredName(nodeName, attributeName);
        if(attribValueProcessors.containsKey(key)) {
            return attribValueProcessors.get(key);
        }
               
        key = createRegisteredName("*", attributeName); // NOI18N
        if(attribValueProcessors.containsKey(key)) {
            return attribValueProcessors.get(key);
        }
        
        return null;
    }
    
    private boolean isPNamespaceName(HyperlinkEnv env, String nodeName) {
        String prefix = ContextUtilities.getPrefixFromNodeName(nodeName);
        if (prefix != null) {
            String namespaceUri = env.lookupNamespacePrefix(prefix);
            if (P_NAMESPACE.equals(namespaceUri)) {
                return true;
            }
        }
        return false;
    }
}
