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
package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.spi.project.ProjectConfigurationProvider;

public class MakeProjectConfigurationProvider implements ProjectConfigurationProvider<Configuration>, PropertyChangeListener {

    /**
     * Property name of the set of configurations.
     * Use it when firing a change in the set of configurations.
     */
    static final String PROP_CONFIGURATIONS_BROKEN = "brokenConfigurations"; // NOI18N
    private final Project project;
    private final ConfigurationDescriptorProviderImpl projectDescriptorProvider;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public MakeProjectConfigurationProvider(Project project, ConfigurationDescriptorProviderImpl projectDescriptorProvider, PropertyChangeListener info) {
        this.project = project;
        this.projectDescriptorProvider = projectDescriptorProvider;
        this.pcs.addPropertyChangeListener(info);
        projectDescriptorProvider.addConfigurationDescriptorListener(this);
        projectDescriptorProvider.getConfigurationDescriptorImpl().getConfs().addPropertyChangeListener(this);
    }

    @Override
    public Collection<Configuration> getConfigurations() {
        return projectDescriptorProvider.getConfigurationDescriptorImpl().getConfs().getConfigurations();
    }

    @Override
    public Configuration getActiveConfiguration() {
        return projectDescriptorProvider.getConfigurationDescriptorImpl().getConfs().getActive();
    }

    @Override
    public void setActiveConfiguration(Configuration configuration) throws IllegalArgumentException, IOException {
        projectDescriptorProvider.getConfigurationDescriptorImpl().getConfs().setActive(configuration);
    }

    public void registerPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        pcs.removePropertyChangeListener(lst);
    }

    @Override
    public boolean hasCustomizer() {
        if (projectDescriptorProvider.gotDescriptor() && projectDescriptorProvider.getConfigurationDescriptor() != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void customize() {
        MakeCustomizerProvider makeCustomizer = project.getLookup().lookup(MakeCustomizerProvider.class);
        makeCustomizer.showCustomizer("Build"); // NOI18N
    }

    @Override
    public boolean configurationsAffectAction(String command) {
        return false;
    /*
    return command.equals(ActionProvider.COMMAND_RUN) ||
    command.equals(ActionProvider.COMMAND_BUILD) ||
    command.equals(ActionProvider.COMMAND_CLEAN) ||
    command.equals(ActionProvider.COMMAND_DEBUG);
     */
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        assert pcs != null;
        pcs.firePropertyChange(evt);
    }
}
