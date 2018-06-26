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
package org.netbeans.modules.websvc.rest.codegen.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo.FieldInfo;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author PeterLiu
 * @author ads
 */
public class EntityResourceModelBuilder {

    private Map<String, EntityClassInfo> entitiesInRelationMap;
    private Map<String, EntityClassInfo> allEntitiesClassInfoMap;
    private EntityResourceBeanModel model;

    /** Creates a new instance of ModelBuilder */
    public EntityResourceModelBuilder(Project project, Collection<String> entities) {
        entitiesInRelationMap = new HashMap<String, EntityClassInfo>();
        allEntitiesClassInfoMap = new HashMap<String, EntityClassInfo>();
        for (String entity : entities) {
            try {
                EntityClassInfo info = null;

                ElementHandle<TypeElement> handle = SourceGroupSupport.
                    getHandleClassName(entity, project);
                if ( handle!= null) 
                {
                    info = new EntityClassInfo(entity, handle , project, this);
                }
                if (info != null ){
                    allEntitiesClassInfoMap.put(entity, info);
                    if ( !info.getFieldInfos().isEmpty()) {
                        entitiesInRelationMap.put(entity, info);
                    }
                }
                
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }


    public Set<EntityClassInfo> getEntityInfos() {
        return new HashSet<EntityClassInfo>(allEntitiesClassInfoMap.values());
    }

    public Set<String> getAllEntityNames() {
        return allEntitiesClassInfoMap.keySet();
    }

    public EntityClassInfo getEntityClassInfo(String type) {
        return allEntitiesClassInfoMap.get(type);
    }

    public EntityResourceBeanModel build() {
        model = new EntityResourceBeanModel(this);
        try {
            for (Entry<String, EntityClassInfo> entry : entitiesInRelationMap.entrySet()) {
                String fqn = entry.getKey();
                EntityClassInfo info = entry.getValue();
                model.addEntityInfo( fqn, info);
                computeRelationships( info );
            }

            model.setValid(true);

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            model.setValid(false);
        }

        entitiesInRelationMap.clear();
        return model;
    }

    private void computeRelationships(EntityClassInfo info) {      
        for (FieldInfo fieldInfo : info.getFieldInfos()) {
            if (fieldInfo.isRelationship()) {
                if (fieldInfo.isOneToMany() || fieldInfo.isManyToMany()) {
                    String typeArg = fieldInfo.getTypeArg();
                    EntityClassInfo classInfo = allEntitiesClassInfoMap.get( typeArg );
                    model.addEntityInfo( typeArg, classInfo);
                } else {
                    String type = fieldInfo.getType();
                    EntityClassInfo classInfo = allEntitiesClassInfoMap.get(type);
                    model.addEntityInfo( type, classInfo);
                }
            }
        }
    }
}
