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
