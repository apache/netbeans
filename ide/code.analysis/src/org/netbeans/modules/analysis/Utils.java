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
package org.netbeans.modules.analysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.analysis.spi.Analyzer.MissingPlugin;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;

/**
 *
 * @author lahvac
 */
public class Utils {

    public static void installMissingPlugins(Collection<? extends MissingPlugin> inPlugins) {
        Set<MissingPlugin> plugins = new HashSet<MissingPlugin>(inPlugins);

        for (MissingPlugin missing : plugins) {
            PluginManager.installSingle(SPIAccessor.ACCESSOR.getCNB(missing), SPIAccessor.ACCESSOR.getDisplayName(missing));
        }
    }
}
