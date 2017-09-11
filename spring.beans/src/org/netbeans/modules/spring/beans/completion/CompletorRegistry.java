/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        return offset == -1 ? tagNameWithNs : tagNameWithNs.substring(offset + 1, tagNameWithNs.length());
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
