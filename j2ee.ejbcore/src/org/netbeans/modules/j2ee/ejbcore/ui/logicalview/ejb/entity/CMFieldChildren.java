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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
