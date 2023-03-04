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

package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.io.File;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

/**
 * <i>ThreadSafe</i>
 *
 * @author Petr Hejl
 * TODO perhaps this could be merged into WLDeploymentManager
 */
public class WLSharedState {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /** <i>GuardedBy("this")</i> */
    private DomainChangeListener domainListener;

    /* <i>GuardedBy("this")</i> */
    private boolean restartNeeded;

    public WLSharedState() {
        super();
    }

    public synchronized void configure(InstanceProperties ip) {
        if (domainListener != null) {
            return;
        }

        File domainConfig = WLPluginProperties.getDomainConfigFile(ip);
        if (domainConfig != null) {
            domainListener = new DomainChangeListener();
            // weak reference
            FileUtil.addFileChangeListener(domainListener, domainConfig);
        }
    }

    public void addDomainChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeDomainChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public synchronized boolean isRestartNeeded() {
        return restartNeeded;
    }

    public synchronized void setRestartNeeded(boolean restartNeeded) {
        this.restartNeeded = restartNeeded;
    }

    private class DomainChangeListener implements FileChangeListener {

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

        @Override
        public void fileChanged(FileEvent fe) {
            changeSupport.fireChange();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            // realistically this would not happen
            changeSupport.fireChange();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            // realistically this would not happen
            changeSupport.fireChange();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            // realistically this would not happen
            changeSupport.fireChange();
        }
    }
}
