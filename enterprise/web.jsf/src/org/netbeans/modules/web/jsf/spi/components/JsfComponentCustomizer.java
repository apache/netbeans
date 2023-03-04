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
package org.netbeans.modules.web.jsf.spi.components;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.HelpCtx;

/**
 * Provides support for extending a JSF framework with a JSF component library.
 * It allows to modify the web module to make use of the JSF suite in JSF framework.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 *
 * @since 1.27
 */
public interface JsfComponentCustomizer extends HelpCtx.Provider {

    /**
     * Attaches a change listener that is to be notified of changes
     * in the customizer (e.g., the result of the {@link #isValid} method
     * has changed).
     *
     * @param  listener a listener.
     */
    public void addChangeListener(@NonNull ChangeListener listener);

    /**
     * Removes a change listener.
     *
     * @param  listener a listener.
     */
    public void removeChangeListener(@NonNull ChangeListener listener);

    /**
     * Returns a UI component used to allow the user to update this customizer.
     *
     * @return a component. This method might be called more than once and it is
     * expected to always return the same instance.
     */
    @NonNull
    public JComponent getComponent();

    /**
     * Checks if this customizer is valid (e.g., if the configuration set
     * using the UI component returned by {@link #getComponent} is valid).
     * <p>
     *
     * @return {@code true} if the configuration is valid, {@code false} otherwise.
     * @see #getErrorMessage()
     * @see #getWarningMessage()
     */
    public boolean isValid();

    /**
     * Get error message or {@code null} if the {@link #getComponent component} is {@link #isValid() valid}.
     *
     * @return error message or {@code null} if the {@link #getComponent component} is {@link #isValid() valid}
     *
     * @see #isValid()
     * @see #getWarningMessage()
     */
    @CheckForNull
    public String getErrorMessage();

    /**
     * Get warning message that can be not {@code null} even for {@link #isValid() valid} customizer.
     * In other words, it is safe to extend web module even if this method returns a message.
     *
     * @return warning message or {@code null}
     *
     * @see #isValid()
     * @see #getErrorMessage()
     */
    @CheckForNull
    public String getWarningMessage();

    /**
     * Allow to save actual configuration of UI component.
     * <p>
     * This method is called after closing component by OK button.
     */
    public void saveConfiguration();

}
