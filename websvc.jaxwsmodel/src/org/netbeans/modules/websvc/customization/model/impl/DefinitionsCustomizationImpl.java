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
/*
 * DefinitionsCustomizationImpl.java
 *
 * Created on February 2, 2006, 5:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

import org.netbeans.modules.websvc.api.customization.model.JAXWSQName;
import java.util.Collections;
import org.netbeans.modules.websvc.api.customization.model.DefinitionsCustomization;
import org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping;
import org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent;
import org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle;
import org.netbeans.modules.websvc.api.customization.model.JavaPackage;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class DefinitionsCustomizationImpl extends CustomizationComponentImpl
        implements DefinitionsCustomization{
    
    /**
     * Creates a new instance of DefinitionsCustomizationImpl
     */
    public DefinitionsCustomizationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public DefinitionsCustomizationImpl(WSDLModel model){
        this(model, createPrefixedElement(JAXWSQName.BINDINGS.getQName(), model));
    }
    
    public void setEnableAsyncMapping(EnableAsyncMapping async) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(EnableAsyncMapping.class,
                ENABLE_ASYNC_MAPPING_PROPERTY, async, classes);
    }
    
    public void setPackage(JavaPackage pack) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(JavaPackage.class, PACKAGE_PROPERTY, pack, classes);
    }
    
    public void setEnableWrapperStyle(EnableWrapperStyle wrapperStyle) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(EnableWrapperStyle.class, ENABLE_WRAPPER_STYLE_PROPERTY,
                wrapperStyle, classes);
    }
    
    public void setEnableMIMEContent(EnableMIMEContent mime) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(EnableMIMEContent.class,
                ENABLE_MIME_CONTENT_PROPERTY, mime, classes);
    }
    
    public JavaPackage getPackage() {
        return getChild(JavaPackage.class);
    }
    
    public EnableWrapperStyle getEnableWrapperStyle() {
        return getChild(EnableWrapperStyle.class);
    }
    
    public EnableMIMEContent getEnableMIMEContent() {
        return getChild(EnableMIMEContent.class);
    }
    
    public EnableAsyncMapping getEnableAsyncMapping() {
        return getChild(EnableAsyncMapping.class);
    }
    
     public void removeEnableAsyncMapping(EnableAsyncMapping async) {
        removeChild(ENABLE_ASYNC_MAPPING_PROPERTY, async);
    }

    public void removePackage(JavaPackage pack) {
        removeChild(PACKAGE_PROPERTY, pack);
    }

    public void removeEnableWrapperStyle(EnableWrapperStyle wrapperStyle) {
        removeChild(ENABLE_WRAPPER_STYLE_PROPERTY, wrapperStyle);
    }

    public void removeEnableMIMEContent(EnableMIMEContent mime) {
        removeChild(ENABLE_MIME_CONTENT_PROPERTY, mime);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
