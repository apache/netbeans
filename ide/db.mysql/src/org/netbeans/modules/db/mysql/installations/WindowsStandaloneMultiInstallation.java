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
package org.netbeans.modules.db.mysql.installations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.db.mysql.impl.Installation;
import org.netbeans.modules.db.mysql.impl.MultiInstallation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Class for detection of installed versions of MySQL on Windows OS.
 *
 * @author jhavlin
 */
public class WindowsStandaloneMultiInstallation implements MultiInstallation {

    private static final WindowsStandaloneMultiInstallation DEFAULT =
            new WindowsStandaloneMultiInstallation();
    private Collection<Installation> installations = null;

    @Override
    public Collection<Installation> getInstallations() {
        if (!Utilities.isWindows()) {
            return Collections.<Installation>emptyList();
        }
        if (installations != null) {
            return installations;
        }
        FileObject fo = FileUtil.toFileObject(
                WindowsStandaloneInstallation.DEFAULT_BASE_PATH);

        if (fo != null) {
            List<Installation> found = new ArrayList<Installation>(3);
            for (FileObject child : fo.getChildren()) {
                if (child.getNameExt().startsWith(
                        WindowsStandaloneInstallation.FOLDER_NAME_PREFIX)
                        && child.isFolder()) {
                    found.add(
                            new WindowsStandaloneInstallation(
                            child.getNameExt()));
                }
            }
            installations = found;
        } else {
            installations = Collections.emptyList();
        }
        return installations;
    }

    @Override
    public void refresh() {
        installations = null;
    }

    public static WindowsStandaloneMultiInstallation getDefault() {
        return DEFAULT;
    }
}
