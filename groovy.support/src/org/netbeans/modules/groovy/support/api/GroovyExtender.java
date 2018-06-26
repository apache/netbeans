/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.support.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.support.spi.GroovyExtenderImplementation;
import org.openide.util.Parameters;

/**
 * An API providing ability to activate Groovy support in specified {@link Project}.
 * <p>
 * Client can use this interface to activate/deactivate Groovy support or to
 * find out if the support is already active for a certain {@link Project}.
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 *
 * @see GroovyExtenderImplementation
 * @since 1.31
 */
public final class GroovyExtender {

    private GroovyExtender() {
    }

    /**
     * Check if the Groovy support had been already activated for the given {@link Project}.
     *
     * @param project project
     * @return {@code true} if the Groovy support is already active,
     *         {@code false} if the Groovy support is not active yet
     *
     * @since 1.31
     */
    public static boolean isActive(@NonNull Project project) {
        Parameters.notNull("project", project); //NOI18N

        GroovyExtenderImplementation extender = project.getLookup().lookup(GroovyExtenderImplementation.class);
        if (extender != null) {
            return extender.isActive();
        }
        return false;
    }

    /**
     * Activates Groovy support for the given {@link Project}.
     *
     * @param project project
     * @return {@code true} if activation were successful, {@code false} otherwise
     *
     * @since 1.31
     */
    public static boolean activate(@NonNull Project project) {
        Parameters.notNull("project", project); //NOI18N

        GroovyExtenderImplementation extender = project.getLookup().lookup(GroovyExtenderImplementation.class);
        if (extender != null) {
            return extender.activate();
        }
        return false;
    }

    /**
     * Deactivates Groovy support for the given {@link Project}.
     *
     * @param project project
     * @return {@code true} if deactivation were successful, {@code false} otherwise
     *
     * @since 1.31
     */
    public static boolean deactivate(@NonNull Project project) {
        Parameters.notNull("project", project); //NOI18N

        GroovyExtenderImplementation extender = project.getLookup().lookup(GroovyExtenderImplementation.class);
        if (extender != null) {
            return extender.deactivate();
        }
        return false;
    }
}
