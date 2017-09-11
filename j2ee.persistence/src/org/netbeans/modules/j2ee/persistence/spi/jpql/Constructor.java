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

import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.eclipse.persistence.jpa.jpql.tools.spi.IConstructor;
import org.eclipse.persistence.jpa.jpql.tools.spi.IType;
import org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration;
import org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository;

/**
 *
 * @author sp153251
 */
public class Constructor implements IConstructor{
    private final ExecutableElement nbConstructor;
    private final java.lang.reflect.Constructor <?> jConstructor;
    private final IType owner;
    private ITypeDeclaration[] parameterTypes;

    public Constructor(IType owner, ExecutableElement constructor){
        assert constructor.getKind() == ElementKind.CONSTRUCTOR;
        this.nbConstructor = constructor;
        this.owner = owner;
        this.jConstructor = null;
    }
    
    public Constructor(IType owner, java.lang.reflect.Constructor<?> constructor) {
        this.owner = owner;
        this.jConstructor = constructor;
        this.nbConstructor = null;
    }
    
    @Override
    public ITypeDeclaration[] getParameterTypes() {
        if(parameterTypes == null){
            if(nbConstructor != null){
                List<? extends VariableElement> params = nbConstructor.getParameters();
                parameterTypes = new ITypeDeclaration[params.size()];
                for(int i=0; i<params.size(); i++){
                    VariableElement param = params.get(i);
                    parameterTypes[i] = typeToTypeDeclaration(param.asType());
                }
            } else {
                Class<?>[] types = jConstructor.getParameterTypes();
                java.lang.reflect.Type[] genericTypes = jConstructor.getGenericParameterTypes();
                parameterTypes = new ITypeDeclaration[types.length];
                ITypeRepository typeRepository = ((Type)owner).getTypeRepository();

                for (int index = 0, count = types.length; index < count; index++) {
                    IType type = typeRepository.getType(types[index]);
                    parameterTypes[index] = new TypeDeclaration(typeRepository, type, genericTypes[index], types[index].isArray());
                }
            }
        }
        return parameterTypes;
    }
 
    private TypeDeclaration typeToTypeDeclaration(TypeMirror tMirror){
        int dimension = 0;
        TypeMirror aType =  tMirror;
        ITypeDeclaration[] generics = null;
        if(tMirror.getKind() == TypeKind.ARRAY){
            for(;aType.getKind() == TypeKind.ARRAY; aType =  ((ArrayType)tMirror).getComponentType()) {
                dimension++;
            }
        }
        if(aType.getKind() == TypeKind.DECLARED){
            DeclaredType dType = (DeclaredType) aType;
            List<? extends TypeMirror> parameters = dType.getTypeArguments();
            if( parameters!=null && parameters.size()>0){
                generics = new ITypeDeclaration[parameters.size()];
                int i=0;
                for(TypeMirror gType: parameters){
                    generics[i] = typeToTypeDeclaration(gType);
                    i++;
                }
            }
        }
        return new TypeDeclaration(owner, generics, dimension);
    }
}
