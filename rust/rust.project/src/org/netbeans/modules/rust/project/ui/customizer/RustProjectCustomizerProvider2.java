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
package org.netbeans.modules.rust.project.ui.customizer;

import java.awt.Dialog;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.HelpCtx;

/**
 * Responsible for showing the RustProjectCustomizer in specific categories. You
 * can register customizers under the "/Projects/RUST_PROJECT_KEY/Customizer"
 * path in layer.xml.
 *
 * @see
 * <a href="https://bits.netbeans.org/dev/javadoc/org-netbeans-modules-projectuiapi/org/netbeans/spi/project/ui/CustomizerProvider2.html">CustomizerProvider2</a>
 */
public class RustProjectCustomizerProvider2 implements CustomizerProvider2 {

    private final RustProject project;

    public RustProjectCustomizerProvider2(RustProject project) {
        this.project = project;
    }

    @Override
    public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {
        Dialog customizerDialog = ProjectCustomizer.createCustomizerDialog(
                "Projects/" + RustProjectAPI.RUST_PROJECT_KEY + "/Customizer",
                project.getLookup(),
                preselectedCategory,
                evt -> {
                },
                HelpCtx.DEFAULT_HELP);
        customizerDialog.setVisible(true);
    }

    @Override
    public void showCustomizer() {
        showCustomizer(null, null);
    }

}
