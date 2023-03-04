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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.maven.api.Constants;
import static org.netbeans.modules.maven.codegen.Bundle.*;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginContainer;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Milos Kleint
 */
@Messages({"NAME_Plugin=Plugin...",
           "TIT_Add_plugin=Add new plugin"})
public class PluginGenerator extends AbstractGenerator<POMModel> {

    @MimeRegistration(mimeType=Constants.POM_MIME_TYPE, service=CodeGenerator.Factory.class, position=200)
    public static class Factory implements CodeGenerator.Factory {
        
        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> toRet = new ArrayList<CodeGenerator>();
            POMModel model = context.lookup(POMModel.class);
            JTextComponent component = context.lookup(JTextComponent.class);
            if (model != null) {
                toRet.add(new PluginGenerator(model, component));
            }
            return toRet;
        }
    }

    /** Creates a new instance of PluginGenerator */
    private PluginGenerator(POMModel model, JTextComponent component) {
        super(model, component);
    }

    @Override
    public String getDisplayName() {
        return NAME_Plugin();
    }

    @Override
    protected void doInvoke() {
        FileObject fo = model.getModelSource().getLookup().lookup(FileObject.class);
        assert fo != null;
        org.netbeans.api.project.Project prj = FileOwnerQuery.getOwner(fo);
        assert prj != null;

        final NewPluginPanel pluginPanel = new NewPluginPanel();
        DialogDescriptor dd = new DialogDescriptor(pluginPanel,
                TIT_Add_plugin());
        
        pluginPanel.setNotificationLineSupport(dd.createNotificationLineSupport());
        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            final NBVersionInfo vi = pluginPanel.getPlugin();
            if (vi != null) {
                writeModel(new ModelWriter() {
                    @Override
                    public int write() {
                        int pos = component.getCaretPosition();
                        PluginContainer container = findContainer(pos, model);

                        Plugin plug = model.getFactory().createPlugin();
                        plug.setGroupId(vi.getGroupId());
                        plug.setArtifactId(vi.getArtifactId());
                        plug.setVersion(vi.getVersion());

                        if (pluginPanel.isConfiguration()) {
                            Configuration config = model.getFactory().createConfiguration();
                            //it would be nice to figure all mandatory parameters without a default value..
                            config.setSimpleParameter("foo", "bar");
                            plug.setConfiguration(config);
                        }

                        if (pluginPanel.getGoals() != null && pluginPanel.getGoals().size() > 0) {
                            PluginExecution ex = model.getFactory().createExecution();
                            String id = null;
                            for (String goal : pluginPanel.getGoals()) {
                                ex.addGoal(goal);
                                if (id == null) {
                                    id = goal;
                                }
                            }
                            if (id !=null) {
                                ex.setId(id);
                            }
                            plug.addExecution(ex);
                            //shall we add execution configuration if
                        }
                        
                        if(!addAtPosition(model.getPOMQNames().PLUGINS.getName(), container::getPlugins, plug)) {
                            container.addPlugin(plug);                                                  
                        }
                        
                        return model.getAccess().findPosition(plug.getPeer());                        
                    }                                        
                });
            }
        }
    }
 
    private PluginContainer findContainer(int pos, POMModel model) {
        Component dc = model.findComponent(pos);
        while (dc != null) {
            if (dc instanceof PluginContainer) {
                return (PluginContainer) dc;
            }
            dc = dc.getParent();
        }
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            bld = model.getFactory().createBuild();
            model.getProject().setBuild(bld);
        }
        return bld;
    }

}
