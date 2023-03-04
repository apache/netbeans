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
package org.netbeans.modules.web.clientproject.api;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.web.clientproject.CustomizerPanelAccessor;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;

/**
 * Provides support for configuration (typically via Project Properties dialog).
 * @since 1.71
 */
public final class CustomizerPanel {

    private final CustomizerPanelImplementation delegate;

    static {
        CustomizerPanelAccessor.setDefault(new CustomizerPanelAccessor() {
            @Override
            public CustomizerPanel create(CustomizerPanelImplementation customizerPanelImplementation) {
                return new CustomizerPanel(customizerPanelImplementation);
            }
        });
    }


    private CustomizerPanel(CustomizerPanelImplementation delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this panel.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}.
     */
    @NonNull
    public String getIdentifier() {
        return delegate.getIdentifier();
    }

    /**
     * Returns the display name of this panel. The display name can be used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    /**
     * Attaches a change listener that is to be notified of changes
     * in the panel (e.g., the result of the {@link #isValid} method
     * has changed.
     * @param listener a listener.
     */
    public void addChangeListener(@NonNull ChangeListener listener) {
        delegate.addChangeListener(listener);
    }

    /**
     * Removes a change listener.
     * @param listener a listener.
     */
    public void removeChangeListener(@NonNull ChangeListener listener) {
        delegate.removeChangeListener(listener);
    }

    /**
     * Returns a UI component used to allow the user to customize this panel.
     * @return a component that provides configuration UI.
     *         This method might be called more than once and it is expected to always
     *         return the same instance.
     */
    @NonNull
    public JComponent getComponent() {
        return delegate.getComponent();
    }

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
    public boolean isValid() {
        return delegate.isValid();
    }

    /**
     * Gets error message or {@code null} if the {@link #getComponent component} is {@link #isValid() valid}.
     * @return error message or {@code null} if the {@link #getComponent component} is {@link #isValid() valid}
     * @see #isValid()
     * @see #getWarningMessage()
     */
    @CheckForNull
    public String getErrorMessage() {
        return delegate.getErrorMessage();
    }

    /**
     * Gets warning message that can be not {@code null} even for {@link #isValid() valid} panel.
     * In other words, it is safe to customize project even if this method returns a message.
     * @return warning message or {@code null}
     * @see #isValid()
     * @see #getErrorMessage()
     */
    @CheckForNull
    public String getWarningMessage() {
        return delegate.getWarningMessage();
    }

    /**
     * Called to extend project. This method
     * is called in a background thread and only if user clicks on the OK button;
     * also, it cannot be called if {@link #isValid()} is {@code false}.
     * <p>
     * <b>Please notice that this method is called under project write lock
     * so it should finish as fast as possible.</b>
     * @see #isValid()
     */
    public void save() {
        delegate.save();
    }

}
