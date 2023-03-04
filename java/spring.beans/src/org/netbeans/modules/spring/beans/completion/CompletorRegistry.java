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

package org.netbeans.modules.spring.beans.completion;

import org.netbeans.modules.spring.beans.completion.completors.InitDestroyMethodCompletor;
import org.netbeans.modules.spring.beans.completion.completors.PNamespaceBeanRefCompletor;
import org.netbeans.modules.spring.beans.completion.completors.ResourceCompletor;
import org.netbeans.modules.spring.beans.completion.completors.PropertyCompletor;
import org.netbeans.modules.spring.beans.completion.completors.FactoryMethodCompletor;
import org.netbeans.modules.spring.beans.completion.completors.JavaClassCompletor;
import org.netbeans.modules.spring.beans.completion.completors.GenericCompletorFactory;
import org.netbeans.modules.spring.beans.completion.completors.BeansRefCompletorFactory;
import org.netbeans.modules.spring.beans.completion.completors.AttributeValueCompletorFactory;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.spring.beans.BeansAttributes;
import org.netbeans.modules.spring.beans.BeansElements;
import org.netbeans.modules.spring.beans.completion.completors.BeanDependsOnCompletor;
import org.netbeans.modules.spring.beans.completion.completors.BeanIdCompletor;
import org.netbeans.modules.spring.beans.completion.completors.BeansRefCompletor;
import org.netbeans.modules.spring.beans.completion.completors.JavaPackageCompletor;
import org.netbeans.modules.spring.beans.completion.completors.PNamespaceCompletor;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.utils.StringUtils;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class CompletorRegistry {

    private static Map<String, CompletorFactory> completorFactories = new HashMap<String, CompletorFactory>();

    private CompletorRegistry() {
        setupCompletors();
    }

    private void setupCompletors() {
        String[] primaryItems = new String[] {
            "true", null, //XXX: Documentation // NOI18N
            "false", null, //XXX: Documentation // NOI18N
        };
        CompletorFactory completorFactory = new AttributeValueCompletorFactory(primaryItems);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.PRIMARY, completorFactory);
        
        String[] defaultLazyInitItems = new String[]{
            "true", null, //XXX: Documentation // NOI18N
            "false", null, //XXX: Documentation // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(defaultLazyInitItems);
        registerCompletorFactory(BeansElements.BEANS, BeansAttributes.DEFAULT_LAZY_INIT, completorFactory);
        
        String[] defaultMergeItems = new String[] {
            "true", null, //XXX: Documentation // NOI18N
            "false", null, //XXX: Documentation // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(defaultMergeItems);
        registerCompletorFactory(BeansElements.BEANS, BeansAttributes.DEFAULT_MERGE, completorFactory);
        
        String[] abstractItems = new String[] {
            "true", null, // XXX: documentation? // NOI18N
            "false", null, // XXX: documentation? // NOI18N
        };
        completorFactory = new AttributeValueCompletorFactory(abstractItems);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.ABSTRACT, completorFactory);
        
        registerCompletorFactory(BeansElements.IMPORT, BeansAttributes.RESOURCE, new GenericCompletorFactory(ResourceCompletor.class));

        GenericCompletorFactory javaClassCompletorFactory = new GenericCompletorFactory(JavaClassCompletor.class);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.CLASS, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.LIST, BeansAttributes.VALUE_TYPE, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.MAP, BeansAttributes.VALUE_TYPE, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.MAP, BeansAttributes.KEY_TYPE, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.SET, BeansAttributes.VALUE_TYPE, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.VALUE, BeansAttributes.TYPE, javaClassCompletorFactory);
        registerCompletorFactory(BeansElements.CONSTRUCTOR_ARG, BeansAttributes.TYPE, javaClassCompletorFactory);

        GenericCompletorFactory javaPackageCompletorFactory = new GenericCompletorFactory(JavaPackageCompletor.class);
        registerCompletorFactory(BeansElements.COMPONENT_SCAN, BeansAttributes.BASE_PACKAGE, javaPackageCompletorFactory);
        
        BeansRefCompletorFactory beansRefCompletorFactory = new BeansRefCompletorFactory(true, BeansRefCompletor.class);
        registerCompletorFactory(BeansElements.ALIAS, BeansAttributes.NAME, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.PARENT, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.FACTORY_BEAN, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.CONSTRUCTOR_ARG, BeansAttributes.REF, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.REF, BeansAttributes.BEAN, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.IDREF, BeansAttributes.BEAN, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.ENTRY, BeansAttributes.KEY_REF, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.ENTRY, BeansAttributes.VALUE_REF, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.PROPERTY, BeansAttributes.REF, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.LOOKUP_METHOD, BeansAttributes.BEAN, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.REPLACED_METHOD, BeansAttributes.REPLACER, beansRefCompletorFactory);
        
        beansRefCompletorFactory = new BeansRefCompletorFactory(false, BeansRefCompletor.class);
        registerCompletorFactory(BeansElements.REF, BeansAttributes.LOCAL, beansRefCompletorFactory);
        registerCompletorFactory(BeansElements.IDREF, BeansAttributes.LOCAL, beansRefCompletorFactory);
        
        GenericCompletorFactory javaMethodCompletorFactory = new GenericCompletorFactory(InitDestroyMethodCompletor.class);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.INIT_METHOD, javaMethodCompletorFactory);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.DESTROY_METHOD, javaMethodCompletorFactory);
        registerCompletorFactory(BeansElements.LOOKUP_METHOD, BeansAttributes.NAME, javaMethodCompletorFactory);
        registerCompletorFactory(BeansElements.REPLACED_METHOD, BeansAttributes.NAME, javaMethodCompletorFactory);
        
        javaMethodCompletorFactory = new GenericCompletorFactory(FactoryMethodCompletor.class);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.FACTORY_METHOD, javaMethodCompletorFactory);
        
        GenericCompletorFactory propertyCompletorFactory = new GenericCompletorFactory(PropertyCompletor.class);
        registerCompletorFactory(BeansElements.PROPERTY, BeansAttributes.NAME, propertyCompletorFactory);
        
        GenericCompletorFactory pNamespaceBeanRefCompletorFactory 
                = new GenericCompletorFactory(PNamespaceBeanRefCompletor.class);
        registerCompletorFactory(BeansElements.BEAN, null, pNamespaceBeanRefCompletorFactory);
        
        GenericCompletorFactory beanIdCompletorFactory = new GenericCompletorFactory(BeanIdCompletor.class);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.ID, beanIdCompletorFactory);
        
        BeansRefCompletorFactory dependsOnFactory = new BeansRefCompletorFactory(true, BeanDependsOnCompletor.class);
        registerCompletorFactory(BeansElements.BEAN, BeansAttributes.DEPENDS_ON, dependsOnFactory);
    }
    private static CompletorRegistry INSTANCE = new CompletorRegistry();

    public static CompletorRegistry getDefault() {
        return INSTANCE;
    }

    public Completor getCompletor(CompletionContext context) {
        switch (context.getCompletionType()) {
            case ATTRIBUTE_VALUE: return getAttributeValueCompletor(context);
            case ATTRIBUTE: return getAttributeCompletor(context);
            case TAG: return getElementCompletor(context);
            default: return null;
        }
    }
    
    private Completor getAttributeValueCompletor(CompletionContext context) {
        String tagName = extractVanilaTagName(context.getTag().getNodeName());
        Token<XMLTokenId> attrib = ContextUtilities.getAttributeToken(context.getDocumentContext());
        String attribName = attrib != null ? attrib.text().toString(): null;
        CompletorFactory completorFactory = locateCompletorFactory(tagName, attribName);
        if (completorFactory != null) {
            Completor completor = completorFactory.createCompletor(context.getCaretOffset());
            return completor;
        }
        
        return null;
    }
    
    private Completor getAttributeCompletor(final CompletionContext context) {
        String tagName = extractVanilaTagName(context.getTag().getNodeName());
        if(tagName.equals(BeansElements.BEAN) && ContextUtilities.isPNamespaceAdded(context.getDocumentContext())) {
            return new PNamespaceCompletor(context.getCaretOffset());
        }
        
        return null;
    }

    private static String extractVanilaTagName(String tagNameWithNs) {
        int offset = tagNameWithNs.indexOf(":"); //NOI18N
        return offset == -1 ? tagNameWithNs : tagNameWithNs.substring(offset + 1);
    }

    private Completor getElementCompletor(CompletionContext context) {
        // TBD
        return null;
    }

    private void registerCompletorFactory(String tagName, String attribName,
            CompletorFactory completorFactory) {
        completorFactories.put(createRegisteredName(tagName, attribName), completorFactory);
    }

    private static String createRegisteredName(String nodeName, String attributeName) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(nodeName)) {
            builder.append("/nodeName=");  // NOI18N
            builder.append(nodeName);
        } else {
            builder.append("/nodeName=");  // NOI18N
            builder.append("*");  // NOI18N
        }

        if (StringUtils.hasText(attributeName)) {
            builder.append("/attribute="); // NOI18N
            builder.append(attributeName);
        }

        return builder.toString();
    }

    private CompletorFactory locateCompletorFactory(String nodeName, String attributeName) {
        String key = createRegisteredName(nodeName, attributeName);
        if (completorFactories.containsKey(key)) {
            return completorFactories.get(key);
        }
        
        key = createRegisteredName(nodeName, null);
        if(completorFactories.containsKey(key)) {
            return completorFactories.get(key);
        }

        key = createRegisteredName("*", attributeName); // NOI18N
        if (completorFactories.containsKey(key)) {
            return completorFactories.get(key);
        }

        return null;
    }
}
