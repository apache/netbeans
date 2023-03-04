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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;



/**
 * @author Chris Webster
 */
public class CMFieldChildren extends Children.Keys<CommonDDBean> implements PropertyChangeListener {
    
    private final EntityMethodController controller;
    private final Entity model;
    private final EjbJar ejbJar;
    private final FileObject ddFile;
    
    public CMFieldChildren(EntityMethodController controller, Entity model, FileObject ddFile) throws IOException {
        this.model = model;
        this.controller = controller;
        this.ejbJar = DDProvider.getDefault().getDDRoot(ddFile); // EJB 2.1
        this.ddFile = ddFile;
    }
    
    protected void addNotify() {
        super.addNotify();
        updateKeys();
        model.addPropertyChangeListener(this);
        ejbJar.addPropertyChangeListener(this);
    }
    
    private void updateKeys() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (controller.getBeanClass() == null) {
                    setKeys(Collections.<CommonDDBean>emptySet());
                } else {
                    List<CommonDDBean> keys = getCmrFields(model.getEjbName());
                    CmpField[] cmpFields = model.getCmpField();
                    Arrays.sort(cmpFields, new Comparator<CmpField>() {
                        public int compare(CmpField cmpField1, CmpField cmpField2) {
                            String fieldName1 = cmpField1.getFieldName();
                            String fieldName2 = cmpField2.getFieldName();
                            if (fieldName1 == null) {
                                fieldName1 = "";
                            }
                            if (fieldName2 == null) {
                                fieldName2 = "";
                            }
                            return fieldName1.compareTo(fieldName2);
                        }
                    });
                    keys.addAll(Arrays.asList(cmpFields));
                    setKeys(keys);
                }
            }
        });
    }
    
    protected void removeNotify() {
        model.removePropertyChangeListener(this);
        ejbJar.removePropertyChangeListener(this);
        setKeys(Collections.<CommonDDBean>emptySet());
        super.removeNotify();
    }
     
    protected Node[] createNodes(CommonDDBean key) {
        Node[] nodes = null;
        if (key instanceof CmpField) {
            CmpField field = (CmpField) key;
            Node node = new CMPFieldNode(field, controller, ddFile);
            nodes = new Node[] { node };
        } else if (key instanceof CmrField) {
            CmrField field = (CmrField) key;
            Node node = new CMRFieldNode(field, controller, ddFile);
            nodes = new Node[] { node };
        }
        return nodes;
    }
    
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        updateKeys();
    }
    
    private CmrField getCmrField(EjbRelationshipRole role, String ejbName) {
        return (role != null &&
               role.getRelationshipRoleSource() != null &&
               ejbName.equals(role.getRelationshipRoleSource().getEjbName()) &&
               role.getCmrField() != null) ? role.getCmrField():null;
    }
    
    private void getFields(String ejbName, List<CommonDDBean> list) {
        Relationships relationships = ejbJar.getSingleRelationships();
        if (relationships != null) {
            EjbRelation[] relations = relationships.getEjbRelation();
            if (relations != null) {
                for (int i = 0; i < relations.length; i++) {
                    CmrField cmrField = getCmrField(relations[i].getEjbRelationshipRole(), ejbName); 
                    if (cmrField != null) {
                        list.add(cmrField);
                    }
                    cmrField = getCmrField(relations[i].getEjbRelationshipRole2(), ejbName);
                    if (cmrField != null) {
                        list.add(cmrField);
                    }
                }
            }
        }
    }
    
    private List<CommonDDBean> getCmrFields(String ejbName) {
        List<CommonDDBean> resultList = new LinkedList<CommonDDBean>();
        getFields(ejbName + "", resultList);
        return resultList;
    }
    
}
