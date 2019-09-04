/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.fish.payara.micro.plugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.TemplateRegistration;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.POM_TEMPLATE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROJECT_ICON;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROJECT_TYPE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_AUTO_BIND_HTTP;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_PAYARA_MICRO_VERSION;
import static org.netbeans.modules.fish.payara.micro.plugin.TemplateUtil.expandTemplate;
import static org.netbeans.modules.fish.payara.micro.plugin.TemplateUtil.loadResource;
import org.netbeans.modules.fish.payara.micro.project.MicroApplication;
import org.netbeans.modules.fish.payara.micro.project.MicroProjectHook;
import org.netbeans.modules.fish.payara.micro.project.ui.PayaraMicroDescriptor;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_CONTEXT_ROOT;

@TemplateRegistration(
        folder = "PayaraResources",
        displayName = "#Templates/PayaraResources/PayaraMicroMavenPlugin", 
        iconBase = PROJECT_ICON,
        description = "resources/PayaraMicroPluginDescription.html", 
        category = {"j2ee-types", "web-types"}
)
public final class MicroPluginWizardDescriptor implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private int index;

    private WizardDescriptor descriptor;
    
    private Project project;

    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    
    public static void updateMicroMavenPlugin(Project project, String payaraMicroVersion, String autoBindHttp, String contextRoot) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("autoBindHttp", autoBindHttp);
        params.put("contextRoot", contextRoot);
        params.put("payaraMicroVersion", payaraMicroVersion);

        try (Reader sourceReader = new InputStreamReader(loadResource(POM_TEMPLATE))) {
            try (Reader targetReader = new StringReader(expandTemplate(sourceReader, params))) {
                POMManager pomManager = new POMManager(targetReader, project);
                pomManager.setExtensionOverrideFilter((source, target) -> {
                    if ("option".equalsIgnoreCase(source.getName())) {
                        for (POMExtensibilityElement element : target.getExtensibilityElements()) {
                            if ("key".equals(element.getQName().getLocalPart())) {
                                return source.getChild("key").getValue().equals(element.getElementText());
                            }
                        }
                    }
                    return true;
                });
                pomManager.commit();
                pomManager.reload();
            }
        }
    }

    @Override
    public void initialize(WizardDescriptor descriptor) {
        this.descriptor = descriptor;
        index = 0;
        if (project == null) {
            project = Templates.getProject(descriptor);
        }
        panels = new ArrayList<>();
        panels.add(new PayaraMicroDescriptor(PROJECT_TYPE));
        String[] names = new String[]{
                NbBundle.getMessage(MicroPluginWizardDescriptor.class, "LBL_PayaraMicroPlugin")
            };
          
        descriptor.putProperty("NewFileWizard_Title", 
                NbBundle.getMessage(MicroPluginWizardDescriptor.class, "Templates/PayaraResources/PayaraMicroMavenPlugin"));
        Wizards.mergeSteps(descriptor, panels.toArray(new WizardDescriptor.Panel[0]), names);
    
    }

    @Override
    public Set instantiate() throws IOException {
        String payaraMicroVersion = (String) descriptor.getProperty(PROP_PAYARA_MICRO_VERSION);
        String autoBindHttp = (String) descriptor.getProperty(PROP_AUTO_BIND_HTTP);
        String contextRoot = (String) descriptor.getProperty(PROP_CONTEXT_ROOT);

        updateMicroMavenPlugin(project, payaraMicroVersion, autoBindHttp, contextRoot);
        MicroApplication.registerInstance(project);
        new MicroProjectHook(project).projectOpened();
        NbMavenProject.fireMavenProjectReload(project);
        return Collections.emptySet();
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels.get(index);
    }

    @Override
    public String name() {
        return NbBundle.getMessage(MicroPluginWizardDescriptor.class, "Templates/PayaraResources/PayaraMicroMavenPlugin");
    }

    @Override
    public boolean hasNext() {
        return index < panels.size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

}
