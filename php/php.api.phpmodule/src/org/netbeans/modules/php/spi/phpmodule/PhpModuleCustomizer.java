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
package org.netbeans.modules.php.spi.phpmodule;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Provides support for configuring given PHP module in Project Properties dialog.
 * @since 2.44
 */
public interface PhpModuleCustomizer {

    /**
     * Get name (identifier) of this customizer.
     * @return name (identifier) of this customizer
     * @see #getDisplayName()
     */
    String getName();

    /**
     * Get display name of this customizer.
     * @return display name of this customizer
     * @see #getName()
     */
    String getDisplayName();

    /**
     * Attaches a change listener that is to be notified of changes
     * in the panel (e.g., the result of the {@link #isValid} method
     * has changed.
     * @param listener a listener.
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Removes a change listener.
     * @param listener a listener.
     */
    void removeChangeListener(ChangeListener listener);

    /**
     * Returns a UI component used to allow the user to customize this panel.
     * @return a component that provides configuration UI.
     *         This method might be called more than once and it is expected to always
     *         return the same instance.
     */
    JComponent getComponent();

    /**
     * Checks if this panel is valid (e.g., if the configuration set
     * using the UI component returned by {@link #getComponent} is valid).
     * <p>
     * If it returns {@code false}, check {@link #getErrorMessage() error message}, it
     * should not be {@code null}.
     *
     * @return {@code true} if the configuration is valid, {@code false} otherwise.
     * @see #getErrorMessage()
     * @see #getWarningMessage()
     */
    boolean isValid();

    /**
     * Gets error message or {@code null} if the {@link #getComponent component} is {@link #isValid() valid}.
     * @return error message or {@code null} if the {@link #getComponent component} is {@link #isValid() valid}
     * @see #isValid()
     * @see #getWarningMessage()
     */
    @CheckForNull
    String getErrorMessage();

    /**
     * Gets warning message that can be not {@code null} even for {@link #isValid() valid} panel.
     * In other words, it is safe to customize project even if this method returns a message.
     * @return warning message or {@code null}
     * @see #isValid()
     * @see #getErrorMessage()
     */
    @CheckForNull
    String getWarningMessage();

    /**
     * Called to extend project. This method
     * is called in a background thread and only if user clicks on the OK button;
     * also, it cannot be called if {@link #isValid()} is {@code false}.
     * <p>
     * <b>Please notice that this method is called under project write lock
     * so it should finish as fast as possible.</b>
     * @see #isValid()
     */
    void save();

    /**
     * Called <b>always</b> when Project Properties panel is closed
     * using either <tt>OK</tt> or <tt>Cancel</tt> button.
     */
    void close();

}
