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

package org.netbeans.modules.j2ee.persistence.unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper.State;
import org.openide.util.Exceptions;

/**
 * Panel for adding entities to persistence unit.
 *
 * @author  Erno Mononen, Andrei Badea
 */
public class AddEntityPanel extends javax.swing.JPanel {
    
    private final MetadataModelReadHelper<EntityMappingsMetadata, List<String>> readHelper;
    
    public static AddEntityPanel create(EntityClassScope entityClassScope, Set<String> ignoreClassNames) {
        AddEntityPanel panel = new AddEntityPanel(entityClassScope, ignoreClassNames);
        panel.initialize();
        return panel;
    }
    
    private AddEntityPanel(EntityClassScope entityClassScope, final Set<String> ignoreClassNames) {
        initComponents();
        MetadataModel<EntityMappingsMetadata> model = entityClassScope.getEntityMappingsModel(true);
        readHelper = MetadataModelReadHelper.create(model, new MetadataModelAction<EntityMappingsMetadata, List<String>>() {
            public List<String> run(EntityMappingsMetadata metadata) {
                List<String> result = new ArrayList<String>();
                for (Entity entity : metadata.getRoot().getEntity()) {
                    String className = entity.getClass2();
                    if (!ignoreClassNames.contains(className)) {
                        result.add(className);
                    }
                }
                Collections.sort(result);
                return result;
            }
        });
    }
    
    private void initialize() {
        readHelper.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (readHelper.getState() == State.FINISHED) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                setEntityClassModel(readHelper.getResult());
                            } catch (ExecutionException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    });
                }
            }
        });
        readHelper.start();
    }
    
    private void setEntityClassModel(List<String> entityClassNames) {
        assert SwingUtilities.isEventDispatchThread();
        DefaultListModel model = new DefaultListModel();
        for (String each : entityClassNames) {
            model.addElement(each);
        }
        entityList.setModel(model);
    }
    
    /**
     * @return fully qualified names of the selected entities' classes.
     */
    public List<String> getSelectedEntityClasses(){
        List<String> result = new ArrayList<String>();
        for (Object elem : entityList.getSelectedValues()) {
            result.add((String)elem);
        }
        return result;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        entityList = new javax.swing.JList();

        jScrollPane1.setViewportView(entityList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList entityList;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
}
