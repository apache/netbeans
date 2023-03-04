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
