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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.w3c.dom.Element;


/**
 * The resource-bundle element inside the application element
 * references a java.util.ResourceBundle instance by name
 * using the var element.  ResourceBundles referenced in this
 * manner may be returned by a call to
 * Application.getResourceBundle() passing the current
 * FacesContext for this request and the value of the var
 * element below.
 * 
 * @author Petr Pisl, ads
 */

public class ResourceBundleImpl extends IdentifiableDescriptionGroupImpl implements ResourceBundle {

    protected static final List<String> RESOURCE_BUNDLE_SORTED_ELEMENTS 
        = new ArrayList<String>( DESCRIPTION_GROUP_SORTED_ELEMENTS.size() +2 );
    static {
        RESOURCE_BUNDLE_SORTED_ELEMENTS.addAll(DESCRIPTION_GROUP_SORTED_ELEMENTS);
        RESOURCE_BUNDLE_SORTED_ELEMENTS.add(JSFConfigQNames.BASE_NAME.getLocalName());
        RESOURCE_BUNDLE_SORTED_ELEMENTS.add(JSFConfigQNames.VAR.getLocalName());
    }
    
    public ResourceBundleImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public ResourceBundleImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.RESOURCE_BUNDLE));
    }
            
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Gets base-name of the faces-config-application-resource-bundleType.
     * @return trimmed base-name if any, {@code null} otherwise
     */
    public String getBaseName() {
        String baseName = getChildElementText(JSFConfigQNames.BASE_NAME.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(baseName);
    }

    public void setBaseName(String baseName) {
        setChildElementText(BASE_NAME, baseName, JSFConfigQNames.BASE_NAME.getQName(getNamespaceURI()));
    }

    /**
     * Gets var of the faces-config-application-resource-bundleType.
     * @return trimmed var if any, {@code null} otherwise
     */
    public String getVar() {
        String var = getChildElementText(JSFConfigQNames.VAR.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(var);
    }

    public void setVar(String var) {
        setChildElementText(VAR, var, JSFConfigQNames.VAR.getQName(getNamespaceURI()));
    }

    @Override
    protected List<String> getSortedListOfLocalNames() {
        return RESOURCE_BUNDLE_SORTED_ELEMENTS;
    }
}
