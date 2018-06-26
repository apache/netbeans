/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
