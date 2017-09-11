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

import java.util.Map;
import org.eclipse.persistence.jpa.jpql.tools.spi.IEmbeddable;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeVisitor;
import org.eclipse.persistence.jpa.jpql.tools.spi.IMapping;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Attributes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EmbeddableAttributes;
import org.netbeans.modules.j2ee.persistence.spi.jpql.support.JPAAttribute;

/**
 *
 * @author sp153251
 */
public class Embeddable extends ManagedType implements IEmbeddable{

    public Embeddable(org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable element, IManagedTypeProvider provider){
        super((PersistentObject)element, provider);     
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(IManagedTypeVisitor visitor) {
            visitor.visit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
            return getType().getName();
    }

    @Override
    Map<String, IMapping> initMappings() {
        Map<String, IMapping> mappings = super.initMappings();
        org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable eb = (org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable) getPersistentObject();
        EmbeddableAttributes attrs = eb.getAttributes();
        if(attrs != null){
            Basic[] bs = attrs.getBasic();
            if(bs != null){
                for(Basic b1:bs){
                    mappings.put(b1.getName(), new Mapping(this, new JPAAttribute(getPersistentObject(), b1)));
                }
            }        
        }
        return mappings;
    }
    
    @Override
    Attributes getAttributes() {
        return null;//TODO embeddable attributes?
    }

}
