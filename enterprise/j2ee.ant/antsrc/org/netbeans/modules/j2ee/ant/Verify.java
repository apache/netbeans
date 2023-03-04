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

package org.netbeans.modules.j2ee.ant;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Calling entry into J2EE platform verifier support by integration plugin.
 *
 * @author nn136682
 */

public class Verify extends Task {
    
    private String file;
    public void setFile(String file) {
        this.file = file;
    }
    public String getFile() {
        return file;
    }
    
    public void execute() throws BuildException { 
        File f = getProject().resolveFile(file);
        FileObject targetFO = FileUtil.toFileObject(f);
        if (targetFO == null) {
            log(NbBundle.getMessage(Verify.class, "MSG_FileNotFound", file));
        }
        try {
            FileObject fo = FileUtil.toFileObject(getProject().getBaseDir());
            Project project = FileOwnerQuery.getOwner(fo);
            J2eeModuleProvider jmp = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
            jmp.verify(targetFO, new LogOutputStream(this, 0));
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }    
}
