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

package org.openide.modules;

import org.openide.util.Exceptions;
import org.openide.util.SharedClassObject;

/**
 * Optional custom module lifecycle hooks.
 * Most modules should not need this. In case you believe you really have
 * to execute some code on start, consider using {@link OnStart} or its 
 * dual peer {@link OnStop}.
 * 
* <p>Specify this class in the manifest file with <code>OpenIDE-Module-Install</code>.
* @author Petr Hamernik, Jaroslav Tulach, Jesse Glick
*/
public class ModuleInstall extends SharedClassObject {
    private static final long serialVersionUID = -5615399519545301432L;

    /** Called when a module is being considered for loading.
     * (This would be before {@link #installed}, {@link #restored},
     * or {@link #updated} are called.) If something is critically
     * wrong with the module (missing ad-hoc dependency, missing
     * license key, etc.) then <code>IllegalStateException</code>
     * may be thrown to prevent it from being loaded (preferably
     * with a {@linkplain Exceptions#attachLocalizedMessage localized annotation}). The default implementation
     * does nothing. The module cannot assume much about when this
     * method will be called; specifically it cannot rely on layers
     * or manifest sections to be ready, nor for the module's classloader
     * to exist in the system class loader (so if loading bundles, icons,
     * and so on, specifically pass in the class loader of the install
     * class rather than relying on the default modules class loader).
     * @since 1.24
     */
    public void validate() throws IllegalStateException {
    }

    /**
     * @deprecated Use {@link #restored} instead.
    */
    @Deprecated
    public void installed() {
        restored();
    }

    /**
     * Called when an already-installed module is restored (during startup).
     * Should perform whatever initializations are required.
     * <p>Note that it is possible for module code to be run before this method
     * is called, and that code must be ready nonetheless. For example, data loaders
     * might be asked to recognize a file before the module is "restored". For this
     * reason, but more importantly for general performance reasons, modules should
     * avoid doing anything here that is not strictly necessary - often by moving
     * initialization code into the place where the initialization is actually first
     * required (if ever). This method should serve as a place for tasks that must
     * be run once during every startup, and that cannot reasonably be put elsewhere.
     * <p>Basic programmatic services are available to the module at this stage -
     * for example, its class loader is ready for general use, any objects registered
     * declaratively to lookup are ready to be
     * queried, and so on.
     * 
     * @see OnStart
     */
    public void restored() {
    }

    /**
     * @deprecated Use {@link #restored} instead.
     */
    @Deprecated
    public void updated(int release, String specVersion) {
        restored();
    }

    /**
     * Called when the module is disabled while the application is still running.
     * Should remove whatever functionality that it had registered in {@link #restored}.
     * <p><strong>Beware:</strong> in practice there is no way to ensure that this method will really be called.
     * The module might simply be deleted or disabled while the application is not running.
     * <span class="nonnormative">In fact this is always the case in NetBeans 6.0;
     * the Plugin Manager only uninstalls or disables modules between restarts.
     * This method will still be called if you reload a module during development.</span>
    */
    public void uninstalled() {
    }

    /**
     * Called when NetBeans is about to exit. The default implementation returns <code>true</code>.
     * The module may cancel the exit if it is not prepared to be shut down.
    * @return <code>true</code> if it is ok to exit
    * @see OnStop
    */
    public boolean closing() {
        return true;
    }

    /**
     * Called when all modules agreed with closing and NetBeans will be closed.
     * @see OnStop
    */
    public void close() {
    }

    @Override
    protected boolean clearSharedData() {
        return false;
    }
}
