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
package org.netbeans.modules.autoupdate.ui;

import java.util.Set;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.spi.autoupdate.PluginInstallerImplementation;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.UserCancelException;
import org.openide.util.lookup.ServiceProvider;

/**
 * GUI implementation of the plugin installer, shows wizard etc. Register with a higher priority
 * than CLI implementation.
 * 
 * @author sdedic
 */
@ServiceProvider(service = PluginInstallerImplementation.class, position = 30000)
public class GuiPluginInstallerImpl implements PluginInstallerImplementation {

    @Override
    public Object install(Set<String> codenamebases, String displayName, Lookup context, Object... alternativeOptions) throws OperationException, UserCancelException {
        // copied from PluginManager, but displayName handling changed.
        Parameters.notNull("cnb", codenamebases); // NOI18N
        Parameters.notNull("alternativeOptions", alternativeOptions); // NOI18N
        if (codenamebases.isEmpty()) {
            throw new IllegalArgumentException("No plugins to install");
        }

        Object o = new ModuleInstallerSupport(alternativeOptions).installPlugins(displayName, codenamebases);
        if (o == NotifyDescriptor.CANCEL_OPTION) {
            throw new UserCancelException();
        } else {
            return o;
        }
    }
    
}
