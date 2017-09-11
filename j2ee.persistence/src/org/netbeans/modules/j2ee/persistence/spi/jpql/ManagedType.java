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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider;
import org.eclipse.persistence.jpa.jpql.tools.spi.IMapping;
import org.eclipse.persistence.jpa.jpql.tools.spi.IType;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Attributes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embedded;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EmbeddedId;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToOne;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToOne;
import org.netbeans.modules.j2ee.persistence.spi.jpql.support.JPAAttribute;

/**
 *
 * @author sp153251
 */
abstract public class ManagedType implements IManagedType {
    private final PersistentObject element;
    private final IManagedTypeProvider provider;
    private Map<String, IMapping> mappings;
    private IType type;

    public ManagedType(PersistentObject element, IManagedTypeProvider provider){
        this.element = element;
        this.provider = provider;
    }
    

    @Override
    public IMapping getMappingNamed(String val) {
        if(mappings == null) {
            mappings = initMappings();
        }
        return mappings.get(val);
    }

    @Override
    public IManagedTypeProvider getProvider() {
        return provider;
    }

    @Override
    public IType getType() {
        if (type == null) {
            if(((ManagedTypeProvider)provider).isValid()){
                type = provider.getTypeRepository().getType(element.getTypeElement().getQualifiedName().toString());
            }
        }
        return type;
    }

    @Override
    public Iterable<IMapping> mappings() {
        if(mappings == null) {
            mappings = initMappings();
        }
        return Collections.unmodifiableCollection(mappings.values());
    }

    @Override
    public int compareTo(IManagedType o) {
        return getType().getName().compareTo(o.getType().getName());
    }
    
    PersistentObject getPersistentObject(){
        return element;
    }
    
    Map<String, IMapping> initMappings() {
        mappings = new HashMap<String, IMapping>();
        Attributes atrs = getAttributes();
        if(atrs != null){
            ManyToMany[] mms = atrs.getManyToMany();
            if(mms != null){
                for(ManyToMany mm1:mms){
                    mappings.put(mm1.getName(), new Mapping(this, new JPAAttribute(element, mm1)));
                }
            }
            ManyToOne[] mos = atrs.getManyToOne();
            if(mos != null){
                for(ManyToOne mo1:mos){
                    mappings.put(mo1.getName(), new Mapping(this, new JPAAttribute(element, mo1)));
                }
            }
            OneToOne[] oos = atrs.getOneToOne();
            if(oos != null){
                for(OneToOne oo1:oos){
                    mappings.put(oo1.getName(), new Mapping(this, new JPAAttribute(element, oo1)));
                }
            }
            OneToMany[] oms = atrs.getOneToMany();
            if(oms != null){
                for(OneToMany om1:oms){
                    mappings.put(om1.getName(), new Mapping(this, new JPAAttribute(element, om1)));
                }
            }
            Basic[] bs = atrs.getBasic();
            if(bs != null){
                for(Basic b1:bs){
                    mappings.put(b1.getName(), new Mapping(this, new JPAAttribute(element, b1)));
                }
            }
            Id[] ids = atrs.getId();
            if(ids != null){
                for(Id id1:ids){
                    mappings.put(id1.getName(), new Mapping(this, new JPAAttribute(element, id1)));
                }
            }
            try {
                Embedded[] es = atrs.getEmbedded();
                if(es != null){
                    for(Embedded e1:es){
                        mappings.put(e1.getName(), new Mapping(this, new JPAAttribute(element, e1)));
                    }
                }
            } catch (UnsupportedOperationException ex){
                //TODO: implements embedded in attributes
            }
            try {
                EmbeddedId eds = atrs.getEmbeddedId();
                if(eds != null){
                    mappings.put(eds.getName(), new Mapping(this, new JPAAttribute(element, eds)));
                }
            } catch (UnsupportedOperationException ex){
                //TODO: implements embedded in attributes
            }
        }
        return mappings;
    }
    
    abstract Attributes getAttributes(); 
    
}
