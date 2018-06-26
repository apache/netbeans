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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.netbeans.modules.web.jsf.api.facesmodel.FacesManagedProperty;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.ListEntries;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanExtension;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps;
import org.netbeans.modules.web.jsf.api.facesmodel.MapEntries;
import org.netbeans.modules.web.jsf.api.metamodel.ManagedProperty;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl, ads
 */
public class ManagedBeanImpl extends IdentifiableDescriptionGroupImpl implements ManagedBean {
    
    // caching properties
    private String beanName;
    private String beanClass;
    private ManagedBean.Scope beanScope;
    
    protected static final Map<String, Integer> SORTED_ELEMENTS = new HashMap<String
        , Integer>();
    static {
        int i=0;
        for( String name : DESCRIPTION_GROUP_SORTED_ELEMENTS ){
            SORTED_ELEMENTS.put( name, i);
            i++;
        }
        SORTED_ELEMENTS.put(JSFConfigQNames.MANAGED_BEAN_NAME.getLocalName(), i++);
        SORTED_ELEMENTS.put(JSFConfigQNames.MANAGED_BEAN_CLASS.getLocalName(), i++);
        SORTED_ELEMENTS.put(JSFConfigQNames.MANAGED_BEAN_SCOPE.getLocalName(), i++);
        SORTED_ELEMENTS.put(JSFConfigQNames.MANAGED_PROPERTY.getLocalName(), i);
        SORTED_ELEMENTS.put(JSFConfigQNames.MAP_ENTRIES.getLocalName(), i);
        SORTED_ELEMENTS.put(JSFConfigQNames.LIST_ENTRIES.getLocalName(), i++);
        SORTED_ELEMENTS.put(JSFConfigQNames.MANAGED_BEAN_EXTENSION.getLocalName(),i++);
    }
    
    /** Creates a new instance of ManagedBeanImpl */
    public ManagedBeanImpl(JSFConfigModelImpl model,Element element) {
        super(model, element);
        beanName = null;
        beanClass = null;
        beanScope = null;
        
        this.addPropertyChangeListener(new PropertyChangeListener () {
            
            public void propertyChange(PropertyChangeEvent event) {
                // The managed bean was changed -> reset all cache fields
                // When user modifies the source file by hand, then the property name
                // is "textContent", so it's easier to reset all fields, then 
                // parse the new value.
                beanName = null;
                beanClass = null;
                beanScope = null;
            }
            
        });
        
    }
    
    public ManagedBeanImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.MANAGED_BEAN));
    }

    /**
     * Gets managed-bean-name of the faces-config-managed-beanType.
     * @return trimmed managed-bean-name if any, {@code null} otherwise
     */
    public String getManagedBeanName() {
        if (beanName == null) {
            beanName = getChildElementText(JSFConfigQNames.MANAGED_BEAN_NAME.getQName(getNamespaceURI()));
            beanName = ElementTypeHelper.pickJavaIdentifierType(beanName);
        }
        return beanName;
    }
    
    public void setManagedBeanName(String name) {
        setChildElementText(MANAGED_BEAN_NAME, name, JSFConfigQNames.MANAGED_BEAN_NAME.getQName(getNamespaceURI()));
    }

    /**
     * Gets managed-bean-class of the faces-config-managed-beanType.
     * @return trimmed managed-bean-class if any, {@code null} otherwise
     */
    public String getManagedBeanClass() {
        if (beanClass ==  null) {
            beanClass = getChildElementText(JSFConfigQNames.MANAGED_BEAN_CLASS.getQName(getNamespaceURI()));
            beanClass = ElementTypeHelper.pickFullyQualifiedClassType(beanClass);
        }
        return beanClass;
    }
    
    public void setManagedBeanClass(String beanClass) {
        setChildElementText(MANAGED_BEAN_CLASS, beanClass, JSFConfigQNames.MANAGED_BEAN_CLASS.getQName(getNamespaceURI()));
    }
    
    public ManagedBean.Scope getManagedBeanScope() {
        if (beanScope == null) {
            String scopeText = getChildElementText(JSFConfigQNames.MANAGED_BEAN_SCOPE.getQName(getNamespaceURI()));
            scopeText = scopeText.trim().toUpperCase(Locale.ENGLISH);
            try{
                beanScope = ManagedBean.Scope.valueOf(scopeText);
            }
            catch (IllegalArgumentException exception){
                // do nothing. The value is wrong and the method should return null.
            }
        }
        return beanScope;
    }
    
    public void setManagedBeanScope(ManagedBean.Scope scope) {
        setManagedBeanScope(scope.toString());
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#addManagedBeanExtension(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanExtension)
     */
    public void addManagedBeanExtension( ManagedBeanExtension extension ) {
        appendChild( MANAGED_BEAN_EXTENSION, extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#addManagedBeanExtension(int, org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanExtension)
     */
    public void addManagedBeanExtension( int index,
            ManagedBeanExtension extension )
    {
        insertAtIndex( MANAGED_BEAN_EXTENSION, extension, index , 
                ManagedBeanExtension.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#addManagedBeanProps(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps)
     */
    public void addManagedBeanProps( ManagedBeanProps props ) {
        appendChild( getPropertyName(props), props);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#addManagedBeanProps(int, org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps)
     */
    public void addManagedBeanProps( int index, ManagedBeanProps props ) {
        insertAtIndex(getPropertyName(props), props, index , ManagedBeanProps.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#getManagedBeanExtensions()
     */
    public List<ManagedBeanExtension> getManagedBeanExtensions() {
        return getChildren( ManagedBeanExtension.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#getManagedBeanScopeString()
     */
    public String getManagedBeanScopeString() {
        String scopeText = getChildElementText(JSFConfigQNames.MANAGED_BEAN_SCOPE.getQName(getNamespaceURI()));
        return scopeText;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#getManagedProps()
     */
    public List<ManagedBeanProps> getManagedProps() {
        return getChildren( ManagedBeanProps.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#removeManagedBeanExtension(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanExtension)
     */
    public void removeManagedBeanExtension( ManagedBeanExtension extension ) {
        removeChild( MANAGED_BEAN_EXTENSION, extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#removeManagedBeanProps(org.netbeans.modules.web.jsf.api.facesmodel.ManagedBeanProps)
     */
    public void removeManagedBeanProps( ManagedBeanProps props ) {
        removeChild( getPropertyName(props), props);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#setEager(java.lang.Boolean)
     */
    public void setEager( Boolean eager ) {
        setAttribute( EAGER, FacesAttributes.EAGER,  eager == null? null: 
            eager.toString().toLowerCase() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#setManagedBeanScope(java.lang.String)
     */
    public void setManagedBeanScope( String scope ) {
        setChildElementText(MANAGED_BEAN_SCOPE, scope, 
                JSFConfigQNames.MANAGED_BEAN_SCOPE.getQName(getPeer().getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean#getEager()
     */
    public Boolean getEager() {
        String eager = getAttribute(FacesAttributes.EAGER);
        if ( eager == null ) {
            return null;
        }
        return Boolean.valueOf( eager );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean#getManagedProperties()
     */
    public List<ManagedProperty> getManagedProperties() {
        /*
         * This method is unneeded in this implementation.
         * But is should be implemented due presence in interface.
         * It present in interface as consequence of annotation interface.  
         */
        List<FacesManagedProperty> props = getChildren( FacesManagedProperty.class);
        return new ArrayList<ManagedProperty>( props );
    }
    
    @Override
    protected Map<String, Integer> getOrderedMapOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    private String getPropertyName( ManagedBeanProps props ){
        String propName = null;
        if ( props instanceof ManagedProperty ){
            propName = MANAGED_PROPERTY;
        }
        else if ( props instanceof ListEntries) {
            propName = LIST_ENTRIES;
        }
        else if ( props instanceof MapEntries ){
            propName = MAP_ENTRIES;
        }
        assert propName != null : props +" element has unknown type. " +
                "Add appropriate child for ManagedBean with " +
                ManagedBeanProps.class+ "  superclass";       // NOI18N
        return propName;
    }

}
