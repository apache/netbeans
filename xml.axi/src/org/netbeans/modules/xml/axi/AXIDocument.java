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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.axi;

import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.axi.impl.AXIModelBuilder;
import org.netbeans.modules.xml.axi.impl.Util;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * Root of the AXI tree.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AXIDocument extends AXIComponent {
    
    /**
     * Creates a new instance of AXIDocument.
     */
    public AXIDocument(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of AXIDocument
     */
    public AXIDocument(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
        
    /**
     * Allow visitor.
     */
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Convenient method to return the top level elements.
     */
    public List<Element> getElements() {
        return getChildren(Element.class);
    }
    
    /**
     * Convenient method to return the top level attributes.
     */
    public List<Attribute> getAttributes() {
        return getChildren(Attribute.class);
    }    
    
    /**
     * Returns a list of ContentModels used in this schema document.
     */
    public List<ContentModel> getContentModels() {
        return getChildren(ContentModel.class);
    }
    
        
    /**
     * Adds a ContentModel to the document.
     */
    public void addContentModel(ContentModel contentModel) {
        appendChild(ContentModel.PROP_CONTENT_MODEL, contentModel);
    }
        
    /**
     * Removes a ContentModel from the document.
     */
    public void removeContentModel(ContentModel contentModel) {
        removeChild(ContentModel.PROP_CONTENT_MODEL, contentModel);
    }
    
    /**
     * Adds an Element as its child.
     */
    public void addElement(Element element) {
        appendChild(Element.PROP_ELEMENT, element);
    }
        
    /**
     * Removes an Element.
     */
    public void removeElement(Element element) {
        removeChild(Element.PROP_ELEMENT, element);
    }    

    /**
     * Returns the namespace, this component belongs to.
     */
    public String getTargetNamespace() {
        return namespace;
    }
    
    /**
     * Sets the fixed value.
     */
    public void setTargetNamespace(String value) {
        String oldValue = getTargetNamespace();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.namespace = value;
        firePropertyChangeEvent(PROP_TARGET_NAMESPACE, oldValue, value);
    }
        
    public void setVersion(String value) {
        String oldValue = getVersion();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.version = value;
        firePropertyChangeEvent(PROP_VERSION, oldValue, value);
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setLanguage(String value) {
        String oldValue = getLanguage();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.language = value;
        firePropertyChangeEvent(PROP_LANGUAGE, oldValue, value);
    }
    
    public String getLanguage() {
        return language;
    }
    
    //TODO
//    public void setFinalDefault(Set<Final> value) {
//        Set<Final> oldValue = getFinalDefault();
//        if( (oldValue == null && value == null) ||
//                (oldValue != null && oldValue.equals(value)) ) {
//            return;
//        }
//        this.finalDefault = value;
//        firePropertyChangeEvent(PROP_FINAL_DEFAULT, oldValue, value);
//    }
//    
//    public Set<Final> getFinalDefault() {
//        return finalDefault;
//    }
    
    public void setElementFormDefault(Form value) {
        Form oldValue = getElementFormDefault();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.elementFormDefault = value;
        firePropertyChangeEvent(PROP_ELEMENT_FORM_DEFAULT, oldValue, value);		
    }
    
    public Form getElementFormDefault() {
        return elementFormDefault;
    }
    
    public void setAttributeFormDefault(Form value) {
        Form oldValue = getAttributeFormDefault();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.attributeFormDefault = value;
        firePropertyChangeEvent(PROP_ATTRIBUTE_FORM_DEFAULT, oldValue, value);		
    }
    
    public Form getAttributeFormDefault() {
        return attributeFormDefault;
    }
	
    public void setSchemaDesignPattern(SchemaGenerator.Pattern value) {
        SchemaGenerator.Pattern oldValue = getSchemaDesignPattern();
        if( (oldValue == null && value == null) ||
                (oldValue != null && oldValue == value) ) {
            return;
        }
        if(getModel() != null)
			getModel().setSchemaDesignPattern(value);
        firePropertyChangeEvent(PROP_SCHEMA_DESIGN_PATTERN, oldValue, value);
    }
    
    public SchemaGenerator.Pattern getSchemaDesignPattern() {
        return getModel()!=null?getModel().getSchemaDesignPattern():null;
    }	
	
    private String namespace;
    private String version;
    private String language;
    private Form attributeFormDefault;
    private Form elementFormDefault;	
    
    public static final String PROP_TARGET_NAMESPACE  = "targetNamespace"; // NOI18N
    public static final String PROP_LANGUAGE  = "language"; // NOI18N
    public static final String PROP_VERSION  = "version"; // NOI18N
    public static final String PROP_ATTRIBUTE_FORM_DEFAULT  = "attributeFormDefault"; // NOI18N
    public static final String PROP_ELEMENT_FORM_DEFAULT  = "elementFormDefault"; // NOI18N
    public static final String PROP_SCHEMA_DESIGN_PATTERN = "schemaDesignPattern"; // NOI18N
}
