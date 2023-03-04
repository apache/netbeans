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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;

/**
 * This interface allows a development module to express what about a module
 * or application has changed since the last deployment.  This information
 * can be passed to the plugin via the ModuleChangeDescriptor interface.
 * An implementation of this interface should be provided in the build target
 * lookup adjacent to the J2eeModule implementation.
 * @author  George Finklang
 */
public interface ModuleChangeReporter {
    
    /* Get all the changes since the time indicated by the timestmap. */
    public EjbChangeDescriptor getEjbChanges(long timestamp);
    
    public boolean isManifestChanged(long timestamp);

}
