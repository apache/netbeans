/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
