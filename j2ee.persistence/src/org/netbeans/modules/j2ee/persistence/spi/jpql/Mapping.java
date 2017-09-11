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

import java.lang.annotation.Annotation;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType;
import org.eclipse.persistence.jpa.jpql.tools.spi.IMapping;
import org.eclipse.persistence.jpa.jpql.tools.spi.IMappingType;
import org.eclipse.persistence.jpa.jpql.tools.spi.IType;
import org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration;
import org.netbeans.modules.j2ee.persistence.spi.jpql.support.JPAAttribute;

/**
 *
 * @author sp153251
 */
public class Mapping implements IMapping {
    
    private final ManagedType parent;
    private IMappingType mappingType;
    private IType type;
    private JPAAttribute attribute;


    public Mapping(ManagedType parent, JPAAttribute attrib){
        this.parent = parent;
        this.attribute = attrib;
    }
    

    @Override
    public String getName() {
        return attribute.getName();
    }

    @Override
    public IManagedType getParent() {
        return parent;
    }

    @Override
    public IType getType() {
        if(type == null){
            if(attribute.getType() != null) {
               type = parent.getProvider().getTypeRepository().getType(attribute.getType().getQualifiedName().toString());
            } else if(attribute.getClass1() !=null) {
                type = parent.getProvider().getTypeRepository().getType(attribute.getClass1());
            } else {
                type = parent.getProvider().getTypeRepository().getType(attribute.getTypeName());
                if(type == null) {
                    //fall back to simplest definition.
                    type = new Type(parent.getProvider().getTypeRepository(), attribute.getTypeName());
                }
            }
        }
        return type;
    }

    @Override
    public ITypeDeclaration getTypeDeclaration() {
        return getType().getTypeDeclaration();
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(IMapping o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public int getMappingType() {
        return attribute.getMappingType();
    }

    @Override
    public boolean isCollection() {
        return (attribute.getMappingType() == IMappingType.ELEMENT_COLLECTION || attribute.getMappingType() == IMappingType.ONE_TO_MANY || attribute.getMappingType() == IMappingType.MANY_TO_MANY);
    }

    @Override
    public boolean isProperty() {
        return (attribute.getMappingType() == IMappingType.BASIC) || (attribute.getMappingType() == IMappingType.ID);
    }

    @Override
    public boolean isRelationship() {
        return (attribute.getMappingType() == IMappingType.MANY_TO_MANY) || (attribute.getMappingType() == IMappingType.MANY_TO_ONE) || (attribute.getMappingType() == IMappingType.ONE_TO_MANY) || (attribute.getMappingType() == IMappingType.ONE_TO_ONE);
        
    }

    @Override
    public boolean isTransient() {
        return (attribute.getMappingType() == IMappingType.TRANSIENT);
    }
    
}
