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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
