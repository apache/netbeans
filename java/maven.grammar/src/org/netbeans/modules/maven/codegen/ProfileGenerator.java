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
package org.netbeans.modules.maven.codegen;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.maven.api.Constants;
import static org.netbeans.modules.maven.codegen.Bundle.*;
import org.netbeans.modules.maven.model.pom.Activation;
import org.netbeans.modules.maven.model.pom.ActivationFile;
import org.netbeans.modules.maven.model.pom.ActivationOS;
import org.netbeans.modules.maven.model.pom.ActivationProperty;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 *
 * @author Milos Kleint
 */
@Messages({"NAME_Profile=Profile...",
           "TIT_Add_profile=Add new profile"
})
public class ProfileGenerator extends AbstractGenerator<POMModel> {

    @MimeRegistration(mimeType=Constants.POM_MIME_TYPE, service=CodeGenerator.Factory.class, position=300)
    public static class Factory implements CodeGenerator.Factory {
        
        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> toRet = new ArrayList<CodeGenerator>();
            POMModel model = context.lookup(POMModel.class);
            JTextComponent component = context.lookup(JTextComponent.class);
            if (model != null) {
                toRet.add(new ProfileGenerator(model, component));
            }
            return toRet;
        }
    }
    
    /** Creates a new instance of ProfileGenerator */
    private ProfileGenerator(POMModel model, JTextComponent component) {
        super(model, component);
    }

    @Override
    public String getDisplayName() {
        return NAME_Profile();
    }
    
    @Override
    protected void doInvoke() {
        final NewProfilePanel panel = new NewProfilePanel(model);
        DialogDescriptor dd = new DialogDescriptor(panel, TIT_Add_profile());
        panel.attachDialogDisplayer(dd);
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret == DialogDescriptor.OK_OPTION) {
            final String id = panel.getProfileId();
            writeModel(new ModelWriter() {
                @Override
                public int write() {
                    Profile prof = model.getProject().findProfileById(id);
                    boolean pomPackaging = "pom".equals(model.getProject().getPackaging()); //NOI18N
                    if (prof == null) {
                        prof = model.getFactory().createProfile();
                        prof.setId(id);
                        if (panel.generateDependencies()) {
                            Dependency dep = model.getFactory().createDependency();
                            dep.setGroupId("foo"); //NOI18N
                            dep.setArtifactId("bar"); //NOI18N
                            dep.setVersion("1.0"); //NOI18N
                            if (pomPackaging) {
                                DependencyManagement dm = model.getFactory().createDependencyManagement();
                                prof.setDependencyManagement(dm);
                                dm.addDependency(dep);
                            } else {
                                prof.addDependency(dep);
                            }
                        }
                        if (panel.generatePlugins()) {
                            BuildBase base = model.getFactory().createBuildBase();
                            prof.setBuildBase(base);
                            Plugin plug = model.getFactory().createPlugin();
                            plug.setGroupId("foo"); //NOI18N
                            plug.setArtifactId("bar"); //NOI18N
                            plug.setVersion("1.0"); //NOI18N
                            if (pomPackaging) {
                                PluginManagement pm = model.getFactory().createPluginManagement();
                                base.setPluginManagement(pm);
                                pm.addPlugin(plug);
                            } else {
                                base.addPlugin(plug);
                            }
                        }
                        if (panel.isActivation()) {
                            Activation act = model.getFactory().createActivation();
                            prof.setActivation(act);
                            if (panel.isActiovationByProperty()) {
                                ActivationProperty prop = model.getFactory().createActivationProperty();
                                act.setActivationProperty(prop);
                                prop.setName("foo");//NOI18N
                                prop.setValue("bar");//NOI18N
                            }
                            if (panel.isActiovationByFile()) {
                                ActivationFile file = model.getFactory().createActivationFile();
                                act.setActivationFile(file);
                                file.setExists("${basedir}/foo.bar"); //NOI18N
                            }
                            if (panel.isActiovationByOS()) {
                                ActivationOS os = model.getFactory().createActivationOS();
                                if (Utilities.isMac()) {
                                    os.setFamily("MacOS");//NOI18N
                                } else if (Utilities.isUnix()) {
                                    os.setFamily("Linux");//NOI18N
                                } else {
                                    os.setFamily("Windows"); //NOI18N
                                }
                                act.setActivationOS(os);
                            }
                        }
                        
                        if(!addAtPosition(model.getPOMQNames().PROFILES.getName(), model.getProject()::getProfiles, prof)) {
                            model.getProject().addProfile(prof);
                        } 
                        
                        return prof.getModel().getAccess().findPosition(prof.getPeer());
                    }
                    return -1;
                }
            });
        }
    }

}
