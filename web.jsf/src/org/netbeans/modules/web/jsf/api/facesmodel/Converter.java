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

package org.netbeans.modules.web.jsf.api.facesmodel;

import java.util.List;

import org.netbeans.modules.web.jsf.api.metamodel.FacesConverter;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "converter" element represents a concrete Converter
 * implementation class that should be registered under the
 * specified converter identifier.  Converter identifiers must
 * be unique within the entire web application.
 * 
 * Nested "attribute" elements identify generic attributes that
 * may be configured on the corresponding UIComponent in order
 * to affect the operation of the Converter.  Nested "property"
 * elements identify JavaBeans properties of the Converter
 * implementation class that may be configured to affect the
 * operation of the Converter.  "attribute" and "property"
 * elements are intended to allow component developers to
 * more completely describe their components to tools and users.
 * These elements have no required runtime semantics.
 * @author Petr Pisl, ads
 */
public interface Converter  extends FacesConfigElement, DescriptionGroup, 
    FacesConverter, IdentifiableElement , AttributeContainer, PropertyContainer
{

    String CONVERTER_CLASS = JSFConfigQNames.CONVERTER_CLASS.getLocalName();
    
    String CONVERTER_FOR_CLASS = JSFConfigQNames.CONVERTER_FOR_CLASS.getLocalName();
    
    String CONVERTER_ID = JSFConfigQNames.CONVERTER_ID.getLocalName();
    
    String CONVERTER_EXTENSION = JSFConfigQNames.CONVERTER_EXTENSION.getLocalName();
    
    void setConverterClass(String value);
    
    void setConverterForClass(String value);
    
    void setConverterId(String value);
    
    List<ConverterExtension> getConverterExtensions();
    void addConverterExtension( ConverterExtension extension );
    void addConverterExtension( int index, ConverterExtension extension );
    void removeConverterExtension( ConverterExtension extension );
}
