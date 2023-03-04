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
package org.netbeans.modules.j2ee.deployment.impl;

import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor;

public abstract class ChangeDescriptorAccessor {

    private static volatile ChangeDescriptorAccessor accessor;

    public static void setDefault(ChangeDescriptorAccessor accessor) {
        if (ChangeDescriptorAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor");
        }
        ChangeDescriptorAccessor.accessor = accessor;
    }

    public static ChangeDescriptorAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        } // that will assign value to the DEFAULT field above
        Class c = DeploymentChangeDescriptor.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        assert accessor != null : "The accessor field must be initialized";
        return accessor;
    }

    /** Accessor to constructor */
    public abstract DeploymentChangeDescriptor newDescriptor(ServerFileDistributor.AppChanges desc);

    public abstract DeploymentChangeDescriptor withChangedServerResources(DeploymentChangeDescriptor desc);
}
