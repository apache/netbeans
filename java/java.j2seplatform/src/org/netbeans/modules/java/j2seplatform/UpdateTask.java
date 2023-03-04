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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public final class UpdateTask implements Runnable {

    private static final Logger LOG = Logger.getLogger(UpdateLogger.class.getName());

    private static final UpdateTask instance = new UpdateTask();

    private final AtomicBoolean done = new AtomicBoolean();

    private UpdateTask() {}

    public static UpdateTask getDefault() {
        return instance;
    }

    public void run() {
        LOG.fine("Requesting update");  //NOI18N
        if (!done.getAndSet(true)) {
            boolean success = false;
            try {
                updateBuildProperties();
                success = true;
            } finally {
                LOG.fine("Update: " + (success ? "OK" : "Failed")); //NOI18N
            }
        }
    }

    private static boolean updateSourceLevel(EditableProperties ep) {
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        String ver = platform.getSpecification().getVersion().toString();
        if (!ver.equals(ep.getProperty("default.javac.source"))) { //NOI18N
            ep.setProperty("default.javac.source", ver); //NOI18N
            ep.setProperty("default.javac.target", ver); //NOI18N
            return true;
        } else {
            return false;
        }
    }


    private static boolean updateBuildProperties (EditableProperties ep) {
        boolean changed = false;
        JavaPlatform[] installedPlatforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification ("j2se",null));   //NOI18N
        for (int i=0; i<installedPlatforms.length; i++) {
            //Handle only platforms created by this module
            if (!installedPlatforms[i].equals (JavaPlatformManager.getDefault().getDefaultPlatform()) && installedPlatforms[i] instanceof J2SEPlatformImpl) {
                String systemName = ((J2SEPlatformImpl)installedPlatforms[i]).getAntName();
                String key = PlatformConvertor.createName(systemName,"home");   //NOI18N
                if (!ep.containsKey (key)) {
                    try {
                        PlatformConvertor.generatePlatformProperties(installedPlatforms[i], systemName, ep);
                        changed = true;
                    } catch (PlatformConvertor.BrokenPlatformException b) {
                        Logger.getLogger(J2SEPlatformModule.class.getName()).info("Platform: " + installedPlatforms[i].getDisplayName() +" is missing: " + b.getMissingTool());
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                  }
                }
            }
        }
        return changed;
    }

    
    private static void updateBuildProperties() {
        ProjectManager.mutex().postWriteRequest(
            new Runnable () {
                public void run () {
                    try {
                        final EditableProperties ep = PropertyUtils.getGlobalProperties();
                        boolean save = updateSourceLevel(ep);
                        save |= updateBuildProperties (ep);
                        if (save) {
                            PropertyUtils.putGlobalProperties (ep);
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            });
    }

}
