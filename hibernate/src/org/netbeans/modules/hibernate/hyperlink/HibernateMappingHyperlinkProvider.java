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
