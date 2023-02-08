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
        if(tm != null && tm.getKind() != null) {
            switch (tm.getKind()) {
                case DECLARED:
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
                    break;
                case BOOLEAN:
                    typeName = ("boolean");//NOI18N
                    cl = boolean.class;
                    break;
                case BYTE:
                    typeName = "byte";//NOI18N
                    cl = byte.class;
                    break;
                case CHAR:
                    typeName = "char";//NOI18N
                    cl = char.class;
                    break;
                case DOUBLE:
                    typeName = "double";//NOI18N
                    cl = double.class;
                    break;
                case FLOAT:
                    typeName = "float";//NOI18N
                    cl = float.class;
                    break;
                case INT:
                    typeName = "int";//NOI18N
                    cl = int.class;
                    break;
                case LONG:
                    typeName = "long";//NOI18N
                    cl = long.class;
                    break;
                case SHORT:
                    typeName = "short";//NOI18N
                    cl = short.class;
                    break;
                default:
                    break;
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