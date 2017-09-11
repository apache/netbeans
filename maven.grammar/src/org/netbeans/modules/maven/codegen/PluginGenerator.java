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
