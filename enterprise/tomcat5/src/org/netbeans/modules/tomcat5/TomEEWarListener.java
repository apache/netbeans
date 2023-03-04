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
package org.netbeans.modules.tomcat5;

import java.io.File;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author Petr Hejl
 */
public class TomEEWarListener implements FileChangeListener {

    private final TomcatProperties tp;

    private final RefreshHook refresh;

    private File currentTomEEJar;

    public TomEEWarListener(TomcatProperties tp, RefreshHook refresh) {
        this.tp = tp;
        this.refresh = refresh;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        checkAndRefresh();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        checkAndRefresh();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        checkAndRefresh();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        checkAndRefresh();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        checkAndRefresh();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    public void checkAndRefresh() {
        File jar = TomcatFactory.getTomEEWebAppJar(tp.getCatalinaHome(), tp.getCatalinaBase());
        if (this.currentTomEEJar != jar && (this.currentTomEEJar == null || !this.currentTomEEJar.equals(jar))) {
            currentTomEEJar = jar;
            TomcatManager.TomEEVersion version = TomcatFactory.getTomEEVersion(jar);
            TomcatManager.TomEEType type = version == null ? null : TomcatFactory.getTomEEType(jar.getParentFile());
            refresh.refresh(version, type);
        }
    }

    public static interface RefreshHook {
        void refresh(TomcatManager.TomEEVersion version, TomcatManager.TomEEType type);
    }

}
