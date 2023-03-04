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

package org.netbeans.modules.j2ee.earproject.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * List of children of a containing node.
 * Each child node is represented by one key from some data model.
 * Remember to document what your permitted keys are!
 * Edit this template to work with the classes and logic of your data model.
 * @author vkraemer
 */
public class LogicalViewChildren extends Children.Keys<ClassPathSupport.Item>  implements AntProjectListener {
    
    private final AntProjectHelper model;
    private ClassPathSupport cs;
    private final UpdateHelper updateHelper;
    private EarProject project;
    
    public LogicalViewChildren(AntProjectHelper model, EarProject project, 
            UpdateHelper updateHelper, ClassPathSupport cs) {
        if (null == model) {
            throw new IllegalArgumentException("model cannot be null"); // NOI18N
        }
        this.model = model;
        this.project = project;
        this.updateHelper = updateHelper;
        this.cs = cs;
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        // there has been race condition here - incorrect order of listener & update
        // listen to changes in the model:
        model.addAntProjectListener(this);
        // set the children to use:
        updateKeys();
    }
    
    private void updateKeys() {
        List<ClassPathSupport.Item> vcpis = EarProjectProperties.getJarContentAdditional(project);
        List<ClassPathSupport.Item> keys = new ArrayList<ClassPathSupport.Item>();
        for (ClassPathSupport.Item item : vcpis) {
            if (item.getType() != ClassPathSupport.Item.TYPE_ARTIFACT || item.getArtifact() == null) {
                continue;
            }
            Project vcpiProject = item.getArtifact().getProject();
            J2eeModuleProvider jmp = vcpiProject.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                keys.add(item);
            }
        }
        setKeys(keys);
    }
    
    @Override
    protected void removeNotify() {
        model.removeAntProjectListener(this);
        setKeys(Collections.<ClassPathSupport.Item>emptySet());
        super.removeNotify();
    }
    
    protected Node[] createNodes(ClassPathSupport.Item item) {
        return new Node[] { new ModuleNode(item, model.getProjectDirectory(), project, updateHelper, cs) };
    }
    
    public void modelChanged(Object ev) {
        // your data model changed, so update the children to match:
        updateKeys();
    }
    
    public void configurationXmlChanged(AntProjectEvent ape) {
        // unsafe to call Children.setKeys() while holding a mutext
        // here the caller holds ProjectManager.mutex() read access
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateKeys();
            }
        });
    }
 
    public void propertiesChanged(final AntProjectEvent ape) {
        // unsafe to call Children.setKeys() while holding a mutext
        // here the caller holds ProjectManager.mutex() read access
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateKeys();
            }
        });
    }
}
