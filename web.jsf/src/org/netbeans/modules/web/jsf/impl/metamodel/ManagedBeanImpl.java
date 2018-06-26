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
        AnnotationMirror annotationMirror = types.get(
                "javax.faces.bean.ManagedBean");                         // NOI18N
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
        boolean isCustom = getHelper().hasAnnotation(type.getAnnotationMirrors(), 
                "javax.faces.bean.CustomScoped");           // NOI18N
        if ( isCustom ){
            AnnotationMirror annotationMirror = types
                    .get("javax.faces.bean.CustomScoped"); // NOI18N
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
    
    private final static Map<String , ManagedBean.Scope> SCOPES 
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
    }
    
    private Boolean eager;
    private String myName;
    private String myClass;
    private ManagedBean.Scope myScope;
    private String myStringScope;
}
