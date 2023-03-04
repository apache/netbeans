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

package org.netbeans.installer.infra.build.ant.registries;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;
import org.netbeans.installer.infra.lib.registries.ManagerException;
import org.netbeans.installer.infra.lib.registries.impl.RegistriesManagerImpl;

/**
 *
 * @author ks152834
 */
public class GenerateComponentsJs extends Task {
    private File root;
    private File file;
    private String locale;
    private File bundlesList;
    
    public void setRoot(final File root) {
        this.root = root;
    }

    public void setFile(final File file) {
        this.file = file;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    public void setBundlesList(final File bundles) {
        this.bundlesList = bundles;
    }
    
    @Override
    public void execute() throws BuildException {
        try {
            final String contents = 
                    new RegistriesManagerImpl().generateComponentsJs(root, bundlesList, locale);
            
            Utils.write(file, contents);
        } catch (ManagerException e) {
            throw new BuildException(e);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
