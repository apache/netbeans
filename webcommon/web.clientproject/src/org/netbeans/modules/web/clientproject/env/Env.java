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
package org.netbeans.modules.web.clientproject.env;

import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;

/** Class providing indirect access to Ant project support services.
 */
public abstract class Env {
    public abstract CommonProjectHelper createProject(FileObject dirFO, String type) throws IOException;
    public abstract String getUsablePropertyName(String displayName);
    public abstract File resolveFile(File dir, String relative);
    public abstract Values createEvaluator(CommonProjectHelper h, FileObject dir);

    public abstract JComponent createLicenseHeaderCustomizerPanel(ProjectCustomizer.Category category, Licenses licenseSupport);

    public abstract String relativizeFile(File base, File relative);
    
    public abstract References newReferenceHelper(CommonProjectHelper helper, AuxiliaryConfiguration configuration, Values eval);

    public abstract Sources initSources(Project project, CommonProjectHelper h, Values e);

    public abstract Licenses newLicensePanelSupport(
        Values evaluator, CommonProjectHelper projectHelper, 
        String p1, String p2
    );
}
