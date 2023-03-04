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
package org.netbeans.modules.web.spi.webmodule;

import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 * Provides support for extending a web module with a web framework, that is,
 * it allows to modify the web module to make use of the framework.
 *
 * @author Andrei Badea
 *
 * @since 1.9
 */
public abstract class WebModuleExtender {

    /**
     * Attaches a change listener that is to be notified of changes
     * in the extender (e.g., the result of the {@link #isValid} method
     * has changed.
     *
     * @param  listener a listener.
     */
    public abstract void addChangeListener(ChangeListener listener);

    /**
     * Removes a change listener.
     *
     * @param  listener a listener.
     */
    public abstract void removeChangeListener(ChangeListener listener);

    /**
     * Returns a UI component used to allow the user to customize this extender.
     *
     * @return a component or null if this extender does not provide a configuration UI.
     *         This method might be called more than once and it is expected to always
     *         return the same instance.
     */
    public abstract JComponent getComponent();

    /**
     * Returns a help context for {@link #getComponent}.
     *
     * @return a help context; can be null.
     */
    public abstract HelpCtx getHelp();

    /**
     * Called when the component returned by {@link #getComponent} needs to be filled
     * with external data.
     */
    public abstract void update();

    /**
     * Checks if this extender is valid (e.g., if the configuration set
     * using the UI component returned by {@link #getComponent} is valid).
     *
     * @return true if the configuration is valid, false otherwise.
     */
    public abstract boolean isValid();

    /**
     * Called to extend the given web module with the web framework
     * corresponding to this extender.
     *
     * @param  webModule the web module to be extended; never null.
     * @return the set of newly created files in the web module.
     */
    public abstract Set<FileObject> extend(WebModule webModule);

    /**
     * Interface that represents ability to save {@code WebModuleExtender}
     * properties for a {@link WebModule}.
     *
     * @since 1.26
     */
    public interface Savable {

        /**
         * Called to save extender configuration for given web module in cases that
         * the web module was already extended by the {@code WebModuleExtender}.
         *
         * @param webModule the web module to store extender settings; never null.
         */
        void save(@NonNull WebModule webModule);

    }

}
