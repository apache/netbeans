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
package org.netbeans.modules.spring.beans.jumpto;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 * 
 * @author Rohan Ranade
 */
public abstract class AbstractBeanTypeDescriptor extends TypeDescriptor {

    private static final Icon beanIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/spring/beans/resources/spring-bean.png", false); // NOI18N
    
    private final String simpleName;
    private final FileObject fileObject;
    private String projectName;
    private Icon projectIcon;

    public AbstractBeanTypeDescriptor(String simpleName, FileObject fileObject) {
        this.simpleName = simpleName;
        this.fileObject = fileObject;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public synchronized String getProjectName() {
        if (projectName == null) {
            initProjectInfo();
        }

        return projectName;
    }

    @Override
    public String getContextName() {
        return null;
    }

    public synchronized Icon getProjectIcon() {
        if (projectIcon == null) {
            initProjectInfo();
        }

        return projectIcon;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    @Override
    public int getOffset() {
        return -1;
    }

    @Override
    public String getOuterName() {
        return null;
    }

    @Override
    public String getTypeName() {
        return getSimpleName();
    }

    @Override
    public Icon getIcon() {
        return beanIcon;
    }

    private void initProjectInfo() {
        Project p = FileOwnerQuery.getOwner(fileObject);
        if (p != null) {
            ProjectInformation pi = ProjectUtils.getInformation(p);
            projectName = pi.getDisplayName();
            projectIcon = pi.getIcon();
        }
    }
}
