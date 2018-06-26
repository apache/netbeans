/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
