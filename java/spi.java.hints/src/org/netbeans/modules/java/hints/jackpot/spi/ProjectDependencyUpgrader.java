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
package org.netbeans.modules.java.hints.jackpot.spi;

import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author lahvac
 */
public abstract class ProjectDependencyUpgrader {

    public abstract boolean ensureDependency(Project p, FileObject dep, SpecificationVersion spec, boolean canShowUI);
    public abstract boolean ensureDependency(Project p, String specification, boolean b);

    protected final boolean showDependencyUpgradeDialog(Project p, String dep, SpecificationVersion currentDependency, SpecificationVersion spec, boolean newDepenency, boolean canShowUI) {
        if (!canShowUI) return true;
        
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation("New version: " + spec, "Update spec version.", NotifyDescriptor.YES_NO_OPTION);

        return DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION;
    }

}
