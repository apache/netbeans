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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl, ads
 */
class ConverterImpl extends IdentifiableDescriptionGroupImpl implements Converter{
    
    protected static final List<String> CONVERTER_SORTED_ELEMENTS = new ArrayList<String>(9);
    static { 
        CONVERTER_SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS );
        CONVERTER_SORTED_ELEMENTS.add( CONVERTER_ID );
        CONVERTER_SORTED_ELEMENTS.add( CONVERTER_FOR_CLASS );
        CONVERTER_SORTED_ELEMENTS.add( CONVERTER_CLASS );
        CONVERTER_SORTED_ELEMENTS.add( ATTRIBUTE );
        CONVERTER_SORTED_ELEMENTS.add( PROPERTY );
        CONVERTER_SORTED_ELEMENTS.add( CONVERTER_EXTENSION );
    }
    
    /** Creates a new instance of CondverterImpl */
    ConverterImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    ConverterImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.CONVERTER));
    }

    /**
     * Gets converter-class of the faces-config-converterType.
     * @return trimmed converter-class if any, {@code null} otherwise
     */
    public String getConverterClass() {
        String className = getChildElementText(JSFConfigQNames.CONVERTER_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(className);
    }
    
    public void setConverterClass(String value) {
        setChildElementText(CONVERTER_CLASS, value, JSFConfigQNames.CONVERTER_CLASS.getQName(getNamespaceURI()));
    }

    /**
     * Gets converter-for-class of the faces-config-converterType.
     * @return trimmed converter-for-class if any, {@code null} otherwise
     */
    public String getConverterForClass() {
        String className = getChildElementText(JSFConfigQNames.CONVERTER_FOR_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(className);
    }
    
    public void setConverterForClass(String value) {
        setChildElementText(CONVERTER_FOR_CLASS, value, JSFConfigQNames.CONVERTER_FOR_CLASS.getQName(getNamespaceURI()));
    }

    /**
     * Gets converter-id of the faces-config-converterType.
     * @return trimmed converter-id if any, {@code null} otherwise
     */
    public String getConverterId() {
        String converterId = getChildElementText(JSFConfigQNames.CONVERTER_ID.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(converterId);
    }
    
    public void setConverterId(String value) {
        setChildElementText(CONVERTER_ID, value, JSFConfigQNames.CONVERTER_ID.getQName(getNamespaceURI()));
    }
        
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Converter#addConverterExtension(org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension)
     */
    public void addConverterExtension( ConverterExtension extension ) {
        appendChild( CONVERTER_EXTENSION, extension );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Converter#addConverterExtension(int, org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension)
     */
    public void addConverterExtension( int index, ConverterExtension extension )
    {
        insertAtIndex( CONVERTER_EXTENSION, extension, index, ConverterExtension.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Converter#getConverterExtensions()
     */
    public List<ConverterExtension> getConverterExtensions() {
        return getChildren( ConverterExtension.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Converter#removeConverterExtension(org.netbeans.modules.web.jsf.api.facesmodel.ConverterExtension)
     */
    public void removeConverterExtension( ConverterExtension extension ) {
        removeChild( CONVERTER_EXTENSION, extension);
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return CONVERTER_SORTED_ELEMENTS;
    }

}
