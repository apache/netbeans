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

package org.netbeans.modules.spring.beans.wizards;

import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

public class SpringXMLConfigNamespacesPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    public static final String INCLUDED_NAMESPACES = "includedNamespaces"; // NOI18N
    public static final String ADD_SPRING_TO_CLASSPATH = "addSpringToClassPath"; // NOI18N
    public static final String SPRING_LIBRARY = "springLibrary"; // NOI18N

    private SpringXMLConfigNamespacesVisual component;

    public SpringXMLConfigNamespacesVisual getComponent() {
        if (component == null) {
            component = new SpringXMLConfigNamespacesVisual();
        }
        return component;
    }

    public HelpCtx getHelp() {
        return null;
    }

    public boolean isValid() {
        return true;
    }

    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }

    public void readSettings(WizardDescriptor settings) {
        Project project = Templates.getProject(settings);
        FileObject targetFolder = Templates.getTargetFolder(settings);
        FileObject artifact = NewSpringXMLConfigWizardIterator.getSourceGroupArtifact(project, targetFolder);
        getComponent().setClassPath(ClassPath.getClassPath(artifact, ClassPath.COMPILE));
    }

    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(INCLUDED_NAMESPACES, getComponent().getIncludedNamespaces());
        settings.putProperty(ADD_SPRING_TO_CLASSPATH, getComponent().getAddSpringToClassPath());
        settings.putProperty(SPRING_LIBRARY, getComponent().getSpringLibrary());
    }
}
