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
package org.netbeans.modules.hudson.spi;

import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Extend Hudson Utilities class with UI-related methods.
 *
 * {@link Utilities} class contains several UI-related methods. To implement
 * them, register custom subclasses of this class using {@link ServiceProvider}.
 *
 * @author jhavlin
 */
public abstract class UIExtension {

    /**
     * Show a build in the UI.
     *
     * @param build Hudson Build to show.
     */
    public abstract void showInUI(HudsonJobBuild build);

    /**
     * Show a job in the UI.
     *
     * @param job Hudson Job to show.
     */
    public abstract void showInUI(HudsonJob job);
}
