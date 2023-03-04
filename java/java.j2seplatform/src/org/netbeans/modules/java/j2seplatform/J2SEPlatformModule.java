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
package org.netbeans.modules.java.j2seplatform;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileUtil;


public class J2SEPlatformModule extends ModuleInstall {
    
    private static final String DEFAULT_PLATFORM = "Services/Platforms/org-netbeans-api-java-Platform/default_platform.xml";    //NOI18N

    public void restored() {
        super.restored();
        ProjectManager.mutex().postWriteRequest(
            new Runnable () {
                public void run () {
                    recoverDefaultPlatform ();
                }
            }
        );

    }

    
    
    

    private static void recoverDefaultPlatform () {
        final FileObject defaultPlatform = FileUtil.getConfigFile(DEFAULT_PLATFORM);
        if (defaultPlatform != null) {
            try {
                DataObject dobj = DataObject.find(defaultPlatform);
                boolean valid = false;
                InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
                if (ic != null) {
                    try {
                        ic.instanceCreate();
                        valid = true;
                    } catch (Exception e) {
                        //Ignore it, logged bellow
                    }
                }
                if (!valid) {
                    Logger.getLogger("global").log(Level.WARNING,"default_platform.xml is broken, regenerating.");
                    defaultPlatform.revert();
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        else {
            Logger.getLogger("global").log(Level.WARNING,"The default platform is hidden.");  //NOI18N
        }
    }
}
