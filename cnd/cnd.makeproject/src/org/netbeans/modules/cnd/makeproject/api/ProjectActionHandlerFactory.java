/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.makeproject.api;

import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.openide.util.Lookup;

/**
 */
public interface ProjectActionHandlerFactory {

    /**
     * Tells if created handler will be able to handle given action type.
     *
     * @param type  action type
     * @param configuration  configuration for action
     * @return <code>true</code> if created handler will be able to handle
     *          given action <code>type</code>, <code>false</code> otherwise
     */
    boolean canHandle(ProjectActionEvent.Type type, Lookup context, Configuration configuration);

    /**
     * Tells if created handler will be able to handle given project action event.
     *
     * @param pae  project action event to handle
     * @return <code>true</code> if created handler will be able to handle
     *          given project action event <code>pae</code>, <code>false</code> otherwise
     */
    boolean canHandle(ProjectActionEvent pae);

    /**
     * Creates handler instances. New handler is created for each action.
     *
     * @return new handler instance
     */
    ProjectActionHandler createHandler();

}
