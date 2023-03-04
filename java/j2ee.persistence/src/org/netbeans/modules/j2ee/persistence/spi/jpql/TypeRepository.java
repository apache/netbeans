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
package org.netbeans.modules.j2ee.persistence.spi.jpql;

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.eclipse.persistence.jpa.jpql.tools.TypeHelper;
import org.eclipse.persistence.jpa.jpql.tools.spi.IType;
import org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository;
import org.netbeans.api.project.Project;

/**
 *
 * @author sp153251
 */
public class TypeRepository implements ITypeRepository {
    private final Map<String, IType[]> types;
    private final Map<String, Boolean> packages;
    private final ManagedTypeProvider mtp;
    private final Elements elements;


    TypeRepository(Project project, ManagedTypeProvider mtp, Elements elements) {
        this.mtp = mtp;
        this.elements = elements;
        types = new HashMap<String, IType[]>();
        packages = new HashMap<String, Boolean>();
    }
    
    @Override
    public IType getEnumType(String fqn) {
        IType[] ret = types.get(fqn);
        if(ret == null){
            //get main type
            int lastPoint = fqn.lastIndexOf('.');
            String mainPart = lastPoint > 0 ? fqn.substring(0, lastPoint) : null;
            if(mainPart != null){
                IType[] mainType = types.get(mainPart);
                if(mainType == null){
                    //first check packages
                    int mainFirstPoint = mainPart.indexOf('.');
                    int mainLastPoint = mainPart.lastIndexOf('.');
                    
                    if(mainFirstPoint != mainLastPoint && mainFirstPoint>-1){
                        //we have at least 2 points and at least one for package (we may have nested enums)
                        for(int packagePartIndex = mainFirstPoint;packagePartIndex<mainLastPoint && packagePartIndex>-1;packagePartIndex = mainPart.indexOf('.', packagePartIndex+1)){
                            String packageStr = mainPart.substring(0,packagePartIndex);
                            Boolean exist = packages.get(packageStr);
                            if(exist == null){
                                packages.put(packageStr, elements.getPackageElement(packageStr)!=null);
                                exist = packages.get(packageStr);
                            }
                            if(Boolean.FALSE.equals(exist)){
                                mainType = new Type[]{null};
                                types.put(mainPart, mainType);
                                break;
                            }
                        }
                    } else if(mainFirstPoint == -1) {
                        mainType = new Type[]{null};
                        types.put(mainPart, mainType);
                    }
                    //
                    if(mainType == null){
                        fillTypeElement(mainPart);
                    }
                }
                mainType = types.get(mainPart);
                if(mainType[0] != null){
                    fillTypeElement(fqn);
                } else {
                    types.put(fqn, new Type[]{null});
                }
            } else {
                //shouldn't happens
                fillTypeElement(fqn);
            }
            ret = types.get(fqn);
        }
        return ret[0];
    }

    @Override
    public IType getType(Class<?> type) {
        String fqn = type.getCanonicalName();
        IType[] ret = types.get(fqn);
        if(ret == null){
            fillTypeElement(type);
            ret = types.get(fqn);
        }
        return ret[0];
    }

    @Override
    public IType getType(String fqn) {
        IType[] ret = types.get(fqn);
        if(ret == null && isValid()){
            if(IType.UNRESOLVABLE_TYPE.equals(fqn)){
                types.put(fqn, new Type[] {new Type(this, fqn)});
            } else {
                //try to find in managed
                int lastPnt = fqn.lastIndexOf('.');
                ManagedType mt = (ManagedType) (lastPnt > -1 ? mtp.getManagedType(fqn.substring(lastPnt+1)) :  mtp.getManagedType(fqn));
                if(mt != null  && mt.getPersistentObject() != null && mt.getPersistentObject().getTypeElement()!=null && mt.getPersistentObject().getTypeElement().getQualifiedName().contentEquals(fqn)) {
                    types.put(fqn, new Type[]{new Type(TypeRepository.this, mt.getPersistentObject())});
                } else {
                    //
                    fillTypeElement(fqn);
                }
            }
            ret = types.get(fqn);
        }
        if((ret == null || ret[0]==null)){//it cost almost nothing to create type here, can create even if provider isn't valid
            //it's still null/unresoved, create "null" type for unresoved fqn
            ret = new Type[] {new Type(this, (String)null)};
            types.put(fqn, ret);
        }
        return ret[0];
    }

    @Override
    public TypeHelper getTypeHelper() {
        return new TypeHelper(this);
    }
    
    private void fillTypeElement(final String fqn){
        types.put(fqn, new Type[]{null});
        if(isValid()){ 
            if(isValid()) {
                TypeElement te = elements.getTypeElement(fqn);
                if(te!=null) {
                    types.put(fqn, new Type[]{new Type(TypeRepository.this, te)});
                }
            }
        }
    }
    private void fillTypeElement(Class<?> type){
        types.put(type.getName(), new Type[]{new Type(TypeRepository.this, type)});
    }
   
    
    boolean isValid(){
        return mtp.isValid();
    }

    void invalidate() {
        
    }
}
