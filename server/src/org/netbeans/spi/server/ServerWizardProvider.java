/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.spi.server;

import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 * Provides the iterator and display name for addition of the new instance of
 * the server. Implementation of this class must be registered in
 * <code>Servers</code> folder in the system filesystem.
 *
 * @author Petr Hejl
 */
public interface ServerWizardProvider {

    /**
     * Returns the display name of the wizard. Usually same as the server name.
     *
     * @return the display name of the wizard
     */
    String getDisplayName();

    /**
     * Returns the iterator for adding the instance. {@link InstantiatingIterator#instantiate()}
     * should return the {@link org.netbeans.api.server.ServerInstance} created by the wizard.
     * <p>
     * Note that if the instance created by the wizard should be mentioned by
     * the infrastructure (as you usually want this to happen),
     * {@link ServerInstanceProvider} must fire change events on all registered
     * listeners.
     * <p>
     * The {@link java.util.Set} returned by {@link InstantiatingIterator#instantiate()}
     * should return the created {@link org.netbeans.api.server.ServerInstance}.
     *
     * @return iterator for adding the server instance
     */
    InstantiatingIterator getInstantiatingIterator();

}
