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
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyContainer;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.spi.grammar.DialogFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Milos Kleint
 */
@NbBundle.Messages("NAME_Dependency=Dependency...")
public class DependencyGenerator extends AbstractGenerator<POMModel> {

    @MimeRegistration(mimeType=Constants.POM_MIME_TYPE, service=CodeGenerator.Factory.class, position=100)
    public static class Factory implements CodeGenerator.Factory {
        
        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> toRet = new ArrayList<CodeGenerator>();
            POMModel model = context.lookup(POMModel.class);
            JTextComponent component = context.lookup(JTextComponent.class);
            if (model != null) {
                toRet.add(new DependencyGenerator(model, component));
            }
            return toRet;
        }
    }

    private DependencyGenerator(POMModel model, JTextComponent component) {
        super(model, component);
    }

    @Override
    public String getDisplayName() {
        return NAME_Dependency();
    }

    @Override
    protected void doInvoke() {
        FileObject fo = model.getModelSource().getLookup().lookup(FileObject.class);
        assert fo != null;
        org.netbeans.api.project.Project prj = FileOwnerQuery.getOwner(fo);
        assert prj != null;
        final int carretPos = component.getCaretPosition();
        final DocumentComponent componentAtCarret = model.findComponent(carretPos);
        boolean dm = false;
        if (componentAtCarret != null) {
            String xpath = model.getXPathExpression(componentAtCarret);
            dm = xpath.contains("dependencyManagement"); //NOI18N
        }
        String[] ret = DialogFactory.showDependencyDialog(prj, !dm);
        if (ret != null) {
            final String groupId = ret[0];
            final String artifactId = ret[1];
            final String version = ret[2];
            final String scope = ret[3];
            final String type = ret[4];
            final String classifier = ret[5];
            writeModel(new ModelWriter() {
                @Override
                public int write() {
                    int pos = component.getCaretPosition();
                    DependencyContainer dependencyContainer = findContainer(pos, model);
                    Dependency dep = dependencyContainer.findDependencyById(groupId, artifactId, classifier);
                    if (dep == null) {
                        dep = model.getFactory().createDependency();
                        dep.setGroupId(groupId);
                        dep.setArtifactId(artifactId);
                        dep.setVersion(version);
                        dep.setScope(scope);
                        dep.setType(type);
                        dep.setClassifier(classifier);
                            
                        if(!addAtPosition(componentAtCarret, model.getPOMQNames().DEPENDENCIES.getName(), dependencyContainer::getDependencies, dep)) {
                            dependencyContainer.addDependency(dep);
                        }
                    }
                    return dep.getModel().getAccess().findPosition(dep.getPeer());
                }                             
            });
        }
    }

    private DependencyContainer findContainer(int pos, POMModel model) {
        Component dc = model.findComponent(pos);
        while (dc != null) {
            if (dc instanceof DependencyContainer) {
                return (DependencyContainer) dc;
            }
            dc = dc.getParent();
        }
        return model.getProject();
    }
}
