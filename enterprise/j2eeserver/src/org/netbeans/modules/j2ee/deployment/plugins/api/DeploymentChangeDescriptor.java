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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.impl.ChangeDescriptorAccessor;
import org.netbeans.modules.j2ee.deployment.impl.ServerFileDistributor;

/**
 * This class allows the plugin to ask for changes which happened
 * in the application. This is compatible replacement for {@link AppChangeDescriptor}.
 *
 * @author Petr Hejl
 * @since 1.47
 */
public final class DeploymentChangeDescriptor implements AppChangeDescriptor {

    private final ServerFileDistributor.AppChanges desc;

    private final boolean serverResourcesChanged;
    
    static {
        ChangeDescriptorAccessor.setDefault(new ChangeDescriptorAccessor() {
            @Override
            public DeploymentChangeDescriptor newDescriptor(ServerFileDistributor.AppChanges desc) {
                return new DeploymentChangeDescriptor(desc, false);
            }

            @Override
            public DeploymentChangeDescriptor withChangedServerResources(DeploymentChangeDescriptor desc) {
                return new DeploymentChangeDescriptor(desc.desc, true);
            }
        });
    }

    private DeploymentChangeDescriptor(ServerFileDistributor.AppChanges desc,
            boolean serverResourcesChanged) {
        this.desc = desc;
        this.serverResourcesChanged = serverResourcesChanged;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getChangedEjbs() {
        return desc.getChangedEjbs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ejbsChanged() {
        return desc.ejbsChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean serverDescriptorChanged() {
        return desc.serverDescriptorChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean manifestChanged() {
        return desc.manifestChanged();
    }

    /**
     * {@inheritDoc}<p>
     * 
     * Returns all changed files (locations where they are deployed).
     */
    @Override
    public File[] getChangedFiles() {
        return desc.getChangedFiles();
    }

    /**
     * {@inheritDoc}
     * <p>
     *
     * Returns all removed files (locations where they are deployed).
     */
    @Override
    public File[] getRemovedFiles() {
        return desc.getRemovedFiles();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean descriptorChanged() {
        return desc.descriptorChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean classesChanged() {
        return desc.classesChanged();
    }

    /**
     * Returns <code>true</code> if the resources intended to be delivered to
     * server (such as connection pools) were changed.
     *
     * @return <code>true</code> if server side resources were changed
     * @since 1.63
     */
    public boolean serverResourcesChanged() {
        return serverResourcesChanged;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("classesChanged: ").append(classesChanged());
        builder.append(", ");
        builder.append("descriptorChanged: ").append(descriptorChanged());
        builder.append(", ");
        builder.append("ejbsChanged: ").append(ejbsChanged());
        builder.append(", ");
        builder.append("manifestChanged: ").append(manifestChanged());
        builder.append(", ");
        builder.append("serverDescriptorChanged: ").append(serverDescriptorChanged());
        builder.append(", ");
        builder.append("serverResourcesChanged: ").append(serverResourcesChanged());
        return builder.toString();
    }

}
