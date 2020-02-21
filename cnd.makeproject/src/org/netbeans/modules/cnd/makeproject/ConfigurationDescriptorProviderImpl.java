/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class ConfigurationDescriptorProviderImpl extends ConfigurationDescriptorProvider{
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    // for unit tests only
    public ConfigurationDescriptorProviderImpl(FileObject projectDirectory) {
        this(null, projectDirectory);
    }

    public ConfigurationDescriptorProviderImpl(Project project, FileObject projectDirectory) {
        super(project, projectDirectory);
    }

    @Override
    protected MakeConfigurationDescriptor getConfigurationDescriptorImpl() {
        return super.getConfigurationDescriptorImpl();
    }
    
    protected void addConfigurationDescriptorListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    protected void removeConfigurationDescriptorListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    @Override
    protected void fireConfigurationDescriptorLoaded() {
        MakeConfigurationDescriptor makeConfigurationDescriptor = getConfigurationDescriptorImpl();
        
        if (makeConfigurationDescriptor.getState() != ConfigurationDescriptor.State.BROKEN) {  // IZ 122372 // IZ 182321
            pcs.firePropertyChange(ConfigurationDescriptorProvider.PROP_CONFIGURATIONS_LOADED, null, makeConfigurationDescriptor);
            pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, makeConfigurationDescriptor.getConfs().getConfigurations());
            pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, makeConfigurationDescriptor.getConfs().getActive());
        } else {
            // notify problem
            pcs.firePropertyChange(MakeProjectConfigurationProvider.PROP_CONFIGURATIONS_BROKEN, null, ConfigurationDescriptor.State.BROKEN);
        }
    }

    @Override
    protected void opening(Interrupter interrupter) {
        super.opening(interrupter);
    }
}
