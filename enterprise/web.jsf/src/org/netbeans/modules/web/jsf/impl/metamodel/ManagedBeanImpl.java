/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean.Scope;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.ManagedProperty;


/**
 * @author ads
 *
 */
class ManagedBeanImpl extends PersistentObject  implements FacesManagedBean, 
    Refreshable 
{

    ManagedBeanImpl( AnnotationModelHelper helper,
            TypeElement typeElement )
    {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean#getEager()
     */
    public Boolean getEager() {
        return eager;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean#getManagedBeanName()
     */
    public String getManagedBeanName() {
        return myName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.impl.metamodel.Refreshable#refresh(javax.lang.model.element.TypeElement)
     */
    public boolean refresh( TypeElement type ) {
        Map<String, ? extends AnnotationMirror> types = 
            getHelper().getAnnotationsByType(getHelper().getCompilationController()
                    .getElements().getAllAnnotationMirrors(type));
        AnnotationMirror annotationMirror = types.get("jakarta.faces.bean.ManagedBean"); // NOI18N
        if (annotationMirror == null) {
            annotationMirror = types.get("javax.faces.bean.ManagedBean"); // NOI18N
        }
        if (annotationMirror == null) {
            return false;
        }
        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectPrimitive("eager", Boolean.class,                   // NOI18N
                AnnotationParser.defaultValue( Boolean.FALSE));
        parser.expectString("name", null);                               // NOI18N
        ParseResult parseResult = parser.parse(annotationMirror);
        Boolean eagerAttr = parseResult.get( "eager" , Boolean.class );  // NOI18N
        if ( eagerAttr == null ){
            eager = false;
        }
        else {
            eager = eagerAttr;
        }
        myClass = type.getQualifiedName().toString();
        myName = parseResult.get("name", String.class);                 // NOI18N
        if ( myName== null || myName.length() == 0 ){
            myName = getConvertedClassName( myClass );
        }
        
        setScope( types , type );
        
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean#getManagedBeanClass()
     */
    public String getManagedBeanClass() {
        return myClass;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean#getManagedBeanScope()
     */
    public Scope getManagedBeanScope() {
        return myScope;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean#getManagedBeanScopeString()
     */
    public String getManagedBeanScopeString() {
        return myStringScope;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean#getManagedProperties()
     */
    public List<ManagedProperty> getManagedProperties() {
        // TODO take care about field inside class annotated with 
        // "javax.faces.bean.ManagedProperty" annotation.
        return null;
    }
    
    private void setScope( Map<String, ? extends AnnotationMirror> types, 
            TypeElement type ) 
    {
        boolean isCustom
                = getHelper().hasAnnotation(type.getAnnotationMirrors(), "jakarta.faces.bean.CustomScoped") // NOI18N
                || getHelper().hasAnnotation(type.getAnnotationMirrors(), "javax.faces.bean.CustomScoped"); // NOI18N
        if ( isCustom ){
            AnnotationMirror annotationMirror = types
                    .get("jakarta.faces.bean.CustomScoped"); // NOI18N
            if (annotationMirror == null) {
                annotationMirror = types
                        .get("javax.faces.bean.CustomScoped"); // NOI18N
            }
            AnnotationParser parser = AnnotationParser.create(getHelper());
            parser.expectString("value", AnnotationParser.defaultValue(""));
            ParseResult parseResult = parser.parse(annotationMirror);
            String value = parseResult.get("value", String.class);
            if (value == null) {
                value = "";
            }
            if ( myScope != null ){
                myScope = null;
            }
            myStringScope = value;
        }
        else {
            ManagedBean.Scope found = null;
            for ( Entry<String, ManagedBean.Scope> entry: SCOPES.entrySet() ){
                String annotation = entry.getKey();
                ManagedBean.Scope scope = entry.getValue();
                if ( getHelper().hasAnnotation(type.getAnnotationMirrors(), 
                        annotation) )
                {
                    found = scope;
                    break;
                }
            }
            if ( found == null){
                myScope = null;
                myStringScope = null;
            }
            else {
                myScope = found;
                myStringScope = myScope.toString();
            }
        }
    }
    
    public static String getConvertedClassName( String className ){
        String result = className.substring( className.lastIndexOf(".") +1 );
        if ( result.length() >1 ){
            StringBuilder builder = new StringBuilder();
            builder.append(  
                    Character.toLowerCase(result.charAt(0)));
            builder.append( result.substring(1));
            result = builder.toString();
        }
        return result;
    }
    
    private static final Map<String , ManagedBean.Scope> SCOPES 
                = new HashMap<String, Scope>();
    static {
        SCOPES.put("javax.faces.bean.ApplicationScoped",    // NOI18N
                ManagedBean.Scope.APPLICATION);
        SCOPES.put("javax.faces.bean.NoneScoped",           // NOI18N
                ManagedBean.Scope.NONE);
        SCOPES.put("javax.faces.bean.RequestScoped",        // NOI18N
                ManagedBean.Scope.REQUEST);
        SCOPES.put("javax.faces.bean.SessionScoped",        // NOI18N
                ManagedBean.Scope.SESSION);
        SCOPES.put("javax.faces.bean.ViewScoped",           // NOI18N
                ManagedBean.Scope.VIEW);
        SCOPES.put("jakarta.faces.bean.ApplicationScoped",    // NOI18N
                ManagedBean.Scope.APPLICATION);
        SCOPES.put("jakarta.faces.bean.NoneScoped",           // NOI18N
                ManagedBean.Scope.NONE);
        SCOPES.put("jakarta.faces.bean.RequestScoped",        // NOI18N
                ManagedBean.Scope.REQUEST);
        SCOPES.put("jakarta.faces.bean.SessionScoped",        // NOI18N
                ManagedBean.Scope.SESSION);
        SCOPES.put("jakarta.faces.bean.ViewScoped",           // NOI18N
                ManagedBean.Scope.VIEW);
    }
    
    private Boolean eager;
    private String myName;
    private String myClass;
    private ManagedBean.Scope myScope;
    private String myStringScope;
}
