/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.j2ee.jpa.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Attributes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Table;

/**
 * Utility methods for discovering various facts
 * about JPA model
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class JPAHelper {
    
    /**
     * Utility method to find out if any member is annotated as Id or
     * EmbeddedId in this class?
     * It does not check any of the inheritted
     * members.
     *
     * @param javaClass JavaClass whose members will be inspected.
     * @return returns true if atleast one member is annotated as Id or EmbeddedId
     */
    public static boolean isAnyMemberAnnotatedAsIdOrEmbeddedId(Object modelObject) {
        Attributes attrs = null;
        
        if (modelObject instanceof Entity){
            attrs = ((Entity)modelObject).getAttributes();
        } else if (modelObject instanceof MappedSuperclass){
            attrs = ((MappedSuperclass)modelObject).getAttributes();
        }
        
        if (attrs != null){
            if (attrs.getEmbeddedId() != null){
                return true;
            }
            
            if (attrs.getId().length > 0){
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * @return name of the primary table that will be mapped to given entity class
     */
    public static String getPrimaryTableName(Entity entity){
        Table table = entity.getTable();
        return table!=null ? table.getName(): null;
    }
    
    public static AnnotationMirror getFirstAnnotationFromGivenSet(Element element,
            Collection<String> searchedAnnotations){
        
        for (AnnotationMirror ann : element.getAnnotationMirrors()){
            String annType = ann.getAnnotationType().toString();
            
            if (searchedAnnotations.contains(annType)){
                return ann;
            }
        }
        
        return null;
    }
    
    /**
     *
     */
    public static AccessType findAccessType(TypeElement entityClass, Object modelElement){
        AccessType accessType = AccessType.INDETERMINED;
        
        if (modelElement instanceof Entity){
            String accessDef = ((Entity)modelElement).getAccess();
            
            if (Entity.FIELD_ACCESS.equals(accessDef)){
                accessType = AccessType.FIELD;
            }
            else if (Entity.PROPERTY_ACCESS.equals(accessDef)){
                accessType = AccessType.PROPERTY;
            }
        } else if (modelElement instanceof MappedSuperclass) {
            String accessDef = ((MappedSuperclass)modelElement).getAccess();
            
            if (MappedSuperclass.FIELD_ACCESS.equals(accessDef)){
                accessType = AccessType.FIELD;
            }
            else if (MappedSuperclass.PROPERTY_ACCESS.equals(accessDef)){
                accessType = AccessType.PROPERTY;
            }            
        }
        
        if(!accessType.isDetermined()) {
        
            // look for the first element annotated with a JPA field annotation
            for (Element element : entityClass.getEnclosedElements()){
                if (element.getKind() == ElementKind.FIELD || element.getKind() == ElementKind.METHOD){
                    AnnotationMirror ann = getFirstAnnotationFromGivenSet(element, JPAAnnotations.MEMBER_LEVEL);

                    if (ann != null){
                        accessType = element.getKind() == ElementKind.FIELD ?
                            AccessType.FIELD : AccessType.PROPERTY;

                        break;
                    }
                }
            }
        }
        
        if (accessType.isDetermined()){
            // check if access type is consistent
            Collection<? extends Element> otherElems;
            String alllowedOpposite;
            if (accessType == AccessType.FIELD){
                otherElems = ElementFilter.methodsIn(entityClass.getEnclosedElements());
                alllowedOpposite = JPAAnnotations.ACCESS_TYPE_PROPERTY;
            } else{
                otherElems = ElementFilter.fieldsIn(entityClass.getEnclosedElements());
                alllowedOpposite = JPAAnnotations.ACCESS_TYPE_FIELD;
            }
            
            for (Element element : otherElems){
                AnnotationMirror ann = getFirstAnnotationFromGivenSet(element, JPAAnnotations.MEMBER_LEVEL);
                
                if (ann != null){
                    boolean valid = false;
                    ann = getFirstAnnotationFromGivenSet(element, Collections.singletonList(JPAAnnotations.ACCESS_TYPE));
                    if(ann != null) {
                        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = ann.getElementValues();
                        if(elementValues != null) {
                            for(ExecutableElement el : elementValues.keySet()) {
                                if(el.getSimpleName().toString().equals("value")) {//NO18N
                                    if (alllowedOpposite.equals(elementValues.get(el).toString())) {
                                        valid = true;
                                        accessType = AccessType.MIXED;
                                        break;
                                    }
                                }
                            }
                        } 
                    }
                    if(!valid) {
                        accessType = AccessType.INCONSISTENT;
                        break;
                    }
                }
            }
        }
        
        return accessType;
    }
}
