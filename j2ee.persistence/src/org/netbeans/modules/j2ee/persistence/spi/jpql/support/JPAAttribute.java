/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.spi.jpql.support;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.eclipse.persistence.jpa.jpql.tools.spi.IMappingType;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embedded;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EmbeddedId;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToOne;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToOne;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Version;

/**
 *
 * @author sp153251
 */
public class JPAAttribute {
    
    private Object attr;
    private int mType = IMappingType.TRANSIENT;
    private String name;
    private PersistentObject parent;
    private TypeElement typeElement;
    private Class<?> cl;
    private String typeName;
    
    public JPAAttribute(PersistentObject parent, ManyToMany attr){
        this.attr = attr;
        name = attr.getName();
        mType = IMappingType.MANY_TO_MANY;
        this.parent = parent;
    }
    
    public JPAAttribute(PersistentObject parent, ManyToOne attr){
        this.attr = attr;
        name = attr.getName();
        mType = IMappingType.MANY_TO_ONE;
        this.parent = parent;
    }
    
    public JPAAttribute(PersistentObject parent, OneToMany attr){
        this.attr = attr;
        name = attr.getName();
        mType = IMappingType.ONE_TO_MANY;
        this.parent = parent;
    }    
    
    public JPAAttribute(PersistentObject parent, OneToOne attr){
        this.attr = attr;
        name = attr.getName();
        mType = IMappingType.ONE_TO_ONE;
        this.parent = parent;
    } 
    
    public JPAAttribute(PersistentObject parent, Basic attr){
        this.attr = attr;
        name = attr.getName();
        mType = IMappingType.BASIC;
        this.parent = parent;
    } 
    
    public JPAAttribute(PersistentObject parent, Id attr){
        this.attr = attr;
        name = attr.getName();
        mType = IMappingType.ID;
        this.parent = parent;
    } 
    
    public JPAAttribute(PersistentObject parent, Embedded attr){
        this.attr = attr;
        name = attr.getName();
        mType = IMappingType.EMBEDDED;
        this.parent = parent;
    }    
    
    public JPAAttribute(PersistentObject parent, EmbeddedId attr){
        this.attr = attr;
        name = attr.getName();
        mType = IMappingType.EMBEDDED_ID;
        this.parent = parent;
    }   
    
    public JPAAttribute(PersistentObject parent, Version attr){
        this.attr = attr;
        name = attr.getName();
        mType = IMappingType.VERSION;
        this.parent = parent;
    }        
    //
    public int getMappingType(){
        return mType;
    }
    
    public String getName(){
        return name;
    }

    public TypeElement getType() {
        if(typeElement==null){
            buildType();
        }
        return typeElement;
    }
    
    public String getTypeName(){
        if(typeName == null){
            buildType();
        }
        return typeName;
    }
    
    public Class<?> getClass1() {
        if(cl == null){
            buildType();
        }
        return cl;
    }
    
    private void buildType(){
            TypeMirror tm = null;
            VariableElement var = Utils.getField(parent.getTypeElement(), name);
            if(var == null){
                ExecutableElement acc = Utils.getAccesor(parent.getTypeElement(), name);
                if(acc != null){
                    tm = acc.getReturnType();
                }
            } else {
                tm = var.asType();
            }
            if( tm!=null ){
                if(tm.getKind() == TypeKind.DECLARED){
                    DeclaredType declaredType = (DeclaredType) tm;
                    if(declaredType.getTypeArguments()!=null && declaredType.getTypeArguments().size()>0) {//it's some generic type
                        if(mType == IMappingType.ONE_TO_MANY || mType == IMappingType.MANY_TO_MANY) {//we suppose it should be for relationship mapping only
                            tm = declaredType.getTypeArguments().get(0);
                            if(tm.getKind() == TypeKind.DECLARED){
                                declaredType = (DeclaredType) tm;
                            }
                        }
                    }
                    typeElement =  (TypeElement) declaredType.asElement();
                    typeName = typeElement.getQualifiedName().toString();
                } else if (TypeKind.BOOLEAN == tm.getKind()) {
                    typeName = ("boolean");//NOI18N
                    cl = boolean.class;
                } else if (TypeKind.BYTE == tm.getKind()) {
                    typeName = "byte";//NOI18N
                    cl = byte.class;
                } else if (TypeKind.CHAR == tm.getKind()) {
                    typeName = "char";//NOI18N
                    cl = char.class;
                } else if (TypeKind.DOUBLE == tm.getKind()) {
                    typeName = "double";//NOI18N
                    cl = double.class;
                } else if (TypeKind.FLOAT == tm.getKind()) {
                    typeName = "float";//NOI18N
                    cl = float.class;
                } else if (TypeKind.INT == tm.getKind()) {
                    typeName = "int";//NOI18N
                    cl = int.class;
                } else if (TypeKind.LONG == tm.getKind()) {
                    typeName = "long";//NOI18N
                    cl = long.class;
                } else if (TypeKind.SHORT == tm.getKind()) {
                    typeName = "short";//NOI18N
                    cl = short.class;
                }
            }        
    }
}
/*
		// Element Collection
		if (type == MappingKeys2_0.ELEMENT_COLLECTION_ATTRIBUTE_MAPPING_KEY) {
			return IMappingType.ELEMENT_COLLECTION;
		}
*/