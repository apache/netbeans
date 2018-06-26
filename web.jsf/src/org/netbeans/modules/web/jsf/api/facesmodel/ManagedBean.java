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

import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.ManagedProperty;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "managed-bean" element represents a JavaBean, of a
 * particular class, that will be dynamically instantiated
 * at runtime (by the default VariableResolver implementation)
 * if it is referenced as the first element of a value binding
 * expression, and no corresponding bean can be identified in
 * any scope.  In addition to the creation of the managed bean,
 * and the optional storing of it into the specified scope,
 * the nested managed-property elements can be used to
 * initialize the contents of settable JavaBeans properties of
 * the created instance.
 * @author Petr Pisl, ads
 */
public interface ManagedBean extends FacesConfigElement, DescriptionGroup, 
    IdentifiableElement , FacesManagedBean
{
    /**
     * Defines the legal values for the &lt;managed-bean-scope&gt;
     * element's body content, which includes all of the scopes
     * normally used in a web application, plus the "none" value
     * indicating that a created bean should not be stored into
     * any scope.
     */
    public enum Scope{
        REQUEST("request"),
        SESSION("session"),
        APPLICATION("application"),
        VIEW("view"),
        NONE("none");
        
        private String scope;
        
        Scope(String scope){
            this.scope = scope;
        }
        
        public String toString(){
            return scope;
        }
    }
    
    String MANAGED_BEAN_CLASS = JSFConfigQNames.MANAGED_BEAN_CLASS.getLocalName();
    String MANAGED_BEAN_SCOPE = JSFConfigQNames.MANAGED_BEAN_SCOPE.getLocalName();
    String MANAGED_BEAN_EXTENSION = JSFConfigQNames.MANAGED_BEAN_EXTENSION.getLocalName();
    String MANAGED_PROPERTY = JSFConfigQNames.MANAGED_PROPERTY.getLocalName();
    String MAP_ENTRIES = JSFConfigQNames.MAP_ENTRIES.getLocalName();
    String LIST_ENTRIES = JSFConfigQNames.LIST_ENTRIES.getLocalName();
    
    void setManagedBeanName(String name);
    
    void setManagedBeanClass(String beanClass);
    
    void setManagedBeanScope(ManagedBean.Scope scope);
    
    void setManagedBeanScope( String scope);
    
    List<ManagedBeanExtension> getManagedBeanExtensions();
    void addManagedBeanExtension( ManagedBeanExtension  extension );
    void removeManagedBeanExtension( ManagedBeanExtension extension );
    void addManagedBeanExtension( int index , ManagedBeanExtension extension );
    
    List<ManagedBeanProps> getManagedProps();
    void addManagedBeanProps( ManagedBeanProps props );
    void removeManagedBeanProps( ManagedBeanProps props );
    void addManagedBeanProps( int index , ManagedBeanProps props );
    
    void setEager( Boolean eager );
}
