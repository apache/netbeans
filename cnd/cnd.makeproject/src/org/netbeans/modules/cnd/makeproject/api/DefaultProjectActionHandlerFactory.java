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

import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.PredefinedType;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 */
@ServiceProvider(service=ProjectActionHandlerFactory.class, position=9999)
public class DefaultProjectActionHandlerFactory implements ProjectActionHandlerFactory {

    /**
     * Default handler can handle anything except for debugging.
     *
     * @param type  action type
     * @return <code>false</code> if <code>action</code> is related to debugging,
     *          <code>true</code> otherwise
     */
    @Override
    public boolean canHandle(ProjectActionEvent.Type type, Lookup context, Configuration configuration) {
        if (type == PredefinedType.DEBUG ||
            type == PredefinedType.DEBUG_STEPINTO ||
            type == PredefinedType.DEBUG_TEST ||
            type == PredefinedType.DEBUG_STEPINTO_TEST) {
            return false;
        } else {
            return type instanceof PredefinedType;
        }
    }

    @Override
    public ProjectActionHandler createHandler() {
        return new DefaultProjectActionHandler();
    }

    @Override
    public boolean canHandle(ProjectActionEvent pae) {
        return canHandle(pae.getType(), pae.getContext(), pae.getConfiguration());
    }

}
