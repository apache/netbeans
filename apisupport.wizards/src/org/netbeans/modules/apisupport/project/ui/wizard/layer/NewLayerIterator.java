/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * <p/>
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 * <p/>
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 * <p/>
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * <p/>
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 * <p/>
 * Contributor(s):
 * <p/>
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.layer;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.spi.LayerUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

@TemplateRegistration(folder = UIUtil.TEMPLATE_FOLDER, position = 1300, displayName = "#template_label", iconBase = LayerUtil.LAYER_ICON, description = "newLayer.html", category = UIUtil.TEMPLATE_CATEGORY)
public class NewLayerIterator extends BasicWizardIterator {

    private BasicDataModel data;
    private CreatedModifiedFiles cmf;

    @Override protected Panel[] createPanels(WizardDescriptor wiz) {
        data = new BasicDataModel(wiz);
        cmf = new CreatedModifiedFiles(data.getProject());
        cmf.add(cmf.layerModifications(new CreatedModifiedFiles.LayerOperation() {
            @Override public void run(FileSystem layer) throws IOException {
                // do nothing - just make sure it exists
            }
        }, Collections.<String>emptySet()));
        return new Panel[] {new LayerPanel(wiz, data, cmf)};
    }

    @Override public Set<?> instantiate() throws IOException {
        cmf.run();
        FileObject layerFile = LayerHandle.forProject(data.getProject()).getLayerFile();
        return layerFile != null ? Collections.singleton(layerFile) : Collections.emptySet();
    }

    @Override public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }

}
