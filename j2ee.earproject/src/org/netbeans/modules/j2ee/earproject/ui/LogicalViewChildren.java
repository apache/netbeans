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
