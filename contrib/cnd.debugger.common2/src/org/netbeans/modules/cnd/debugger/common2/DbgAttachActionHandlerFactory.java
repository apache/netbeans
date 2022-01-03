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

package org.netbeans.modules.cnd.debugger.common2;

import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandlerFactory;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.openide.util.Lookup;

@ServiceProvider(service=ProjectActionHandlerFactory.class, position=4000)
public class DbgAttachActionHandlerFactory implements ProjectActionHandlerFactory {

    @Override
    public boolean canHandle(ProjectActionEvent.Type type, Lookup context, Configuration conf) {
        if (type != ProjectActionEvent.PredefinedType.ATTACH) {
            return false;
        }
        if (conf instanceof MakeConfiguration) {
            MakeConfiguration mc = (MakeConfiguration)conf;
            final CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();
            if (compilerSet == null) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public ProjectActionHandler createHandler() {
	return new DbgAttachActionHandler();
    }

    @Override
    public boolean canHandle(ProjectActionEvent pae) {
        return canHandle(pae.getType(), pae.getContext(), pae.getConfiguration());
    }
}
