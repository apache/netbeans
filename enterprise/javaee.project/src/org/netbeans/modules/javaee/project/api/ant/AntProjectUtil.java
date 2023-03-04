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
package org.netbeans.modules.javaee.project.api.ant;

import java.io.IOException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;

public class AntProjectUtil {

    public static void backupBuildImplFile(UpdateHelper updateHelper) throws IOException {
        final FileObject projectDir = updateHelper.getAntProjectHelper().getProjectDirectory();
        final FileObject buildImpl = projectDir.getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
        if (buildImpl != null) {
            final String name = buildImpl.getName();
            final String backupext = String.format("%s~", buildImpl.getExt());
            final FileObject oldBackup = buildImpl.getParent().getFileObject(name, backupext);
            if (oldBackup != null) {
                oldBackup.delete();
            }
            FileLock lock = buildImpl.lock();
            try {
                buildImpl.rename(lock, name, backupext);
            } finally {
                lock.releaseLock();
            }
        }
    }

    public static void updateDirsAttributeInCPSItem(org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item item, Element element) {
        String dirs = item.getAdditionalProperty(AntProjectConstants.DESTINATION_DIRECTORY);
        if (dirs == null) {
            dirs = AntProjectConstants.DESTINATION_DIRECTORY_LIB;
            if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT && !item.isBroken()) {
                if (item.getArtifact() != null && item.getArtifact().getProject() != null && item.getArtifact().getProject().getLookup().lookup(J2eeModuleProvider.class) != null) {
                    dirs = AntProjectConstants.DESTINATION_DIRECTORY_ROOT;
                }
            }
        }
        element.setAttribute("dirs", dirs);
    }

}
