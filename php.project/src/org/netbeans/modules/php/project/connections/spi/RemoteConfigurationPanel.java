/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.spi;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.connections.ConfigManager;

/**
 * @author Tomas Mysik
 */
public interface RemoteConfigurationPanel {

    /**
     * Attach a {@link ChangeListener change listener} that is to be notified of changes
     * in the configration panel (e.g., the result of the {@link #isValidConfiguration} method
     * has changed).
     *
     * @param listener a listener.
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Remove a {@link ChangeListener change listener}.
     *
     * @param listener a listener.
     */
    void removeChangeListener(ChangeListener listener);

    /**
     * Return a UI component used to allow the user to customize this {@link RemoteConfiguration remote configuration}.
     *
     * @return a component which provides a configuration UI, never <code>null</code>.
     *         This method might be called more than once and it is expected to always
     *         return the same instance.
     */
    JComponent getComponent();

    /**
     *
     * @param configuration {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration} to read data from.
     */
    void read(ConfigManager.Configuration configuration);

    /**
     *
     * @param configuration {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration} to store data to.
     */
    void store(ConfigManager.Configuration configuration);

    /**
     * Check whether this {@link RemoteConfiguration configuration} is valid, it means it contains no errors.
     * @return <code>true</code> if this {@link RemoteConfiguration remote configuration} contains no errors, <code>false</code> otherwise.
     */
    boolean isValidConfiguration();

    /**
     * Get the error messsage if this {@link RemoteConfiguration remote configuration} is not valid.
     * @return error messsage if this {@link RemoteConfiguration remote configuration} is not valid.
     * @see #isValidConfiguration()
     * @see #getWarning()
     */
    String getError();

    /**
     * Get the warning messsage. Please notice that this warning message is not related
     * to panel {@link #isValidConfiguration() validity}.
     * @return warning messsage.
     * @see #isValidConfiguration()
     * @see #getError()
     */
    String getWarning();
}
