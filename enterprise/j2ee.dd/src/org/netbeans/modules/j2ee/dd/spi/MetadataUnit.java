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

package org.netbeans.modules.j2ee.dd.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class MetadataUnit {

    // XXX need to listen on the DD file

    public static final String PROP_DEPLOYMENT_DESCRIPTOR = "deploymentDescriptor"; // NOI18N

    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);

    private final ClassPath bootPath;
    private final ClassPath compilePath;
    private final ClassPath sourcePath;

    private File deploymentDescriptor;

    public static MetadataUnit create(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath, File deploymentDescriptor) {
        return new MetadataUnit(bootPath, compilePath, sourcePath, deploymentDescriptor);
    }

    private MetadataUnit(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath, File deploymentDescriptor) {
        this.bootPath = bootPath;
        this.compilePath = compilePath;
        this.sourcePath = sourcePath;
        this.deploymentDescriptor = deploymentDescriptor;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(listener);
    }

    public ClassPath getBootPath() {
        return bootPath;
    }

    public ClassPath getCompilePath() {
        return compilePath;
    }

    public ClassPath getSourcePath() {
        return sourcePath;
    }

    public synchronized FileObject getDeploymentDescriptor() {
        return deploymentDescriptor != null ? FileUtil.toFileObject(FileUtil.normalizeFile(deploymentDescriptor)) : null;
    }

    public void changeDeploymentDescriptor(File deploymentDescriptor) {
        synchronized (this) {
            this.deploymentDescriptor = deploymentDescriptor;
        }
        propChangeSupport.firePropertyChange(PROP_DEPLOYMENT_DESCRIPTOR, null, null);
    }
}
