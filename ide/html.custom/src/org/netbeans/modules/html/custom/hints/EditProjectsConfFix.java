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
package org.netbeans.modules.html.custom.hints;

import java.util.Objects;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.html.custom.conf.Configuration;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
@NbBundle.Messages(value = "editProjectConfiguration=Edit project's editor custom elements configuration file")
public final class EditProjectsConfFix implements HintFix {
    private final Snapshot snapshot;

    public EditProjectsConfFix(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public String getDescription() {
        return Bundle.editProjectConfiguration();
    }

    @Override
    public void implement() throws Exception {
        Configuration conf = Configuration.get(snapshot.getSource().getFileObject());
        FileObject projectsConfigurationFile = conf.getProjectsConfigurationFile();
        if(projectsConfigurationFile != null) {
            DataObject dobj = DataObject.find(projectsConfigurationFile);
            OpenCookie oc = dobj.getLookup().lookup(OpenCookie.class);
            oc.open();
        }
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.snapshot);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EditProjectsConfFix other = (EditProjectsConfFix) obj;
        if (!Objects.equals(this.snapshot, other.snapshot)) {
            return false;
        }
        return true;
    }
    
    
    
}
