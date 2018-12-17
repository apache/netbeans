/**
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

package org.netbeans.installer.products.nb.javafx;

import java.io.File;
import java.io.IOException;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String JAVAFX_CLUSTER = 
            "{javafx-cluster}"; // NOI18N
    private static final String ID = 
            "FX"; // NOI18N
    private static final String EULA_ACCEPTED_MARKER =
            ".javafx_eula_accepted";
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            JAVAFX_CLUSTER}, ID);
    }

    @Override
    public void install(Progress progress) throws InstallationException {
        super.install(progress);
        final File eula_accepted = new File(SystemUtils.getUserHomeDirectory(), EULA_ACCEPTED_MARKER);
        
        if (!FileUtils.exists(eula_accepted)) {
            try {
                getProduct().getInstalledFiles().add(FileUtils.writeFile(eula_accepted, ""));
            } catch (IOException e){
                LogManager.log(e);
            }
        }
    }    
}
