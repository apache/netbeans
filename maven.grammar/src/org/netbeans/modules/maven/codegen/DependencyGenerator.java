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
