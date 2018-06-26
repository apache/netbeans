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

package org.netbeans.modules.javaee.project.api.ear;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.spi.ear.EarDDGeneratorImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * A supplementary API for generation EAR project's deployment descriptor (<i>application.xml</i>).
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 *
 * @see EarDDGeneratorImpl
 * @since 1.6
 */
public final class EarDDGenerator {

    private EarDDGenerator() {
    }

    /**
     * Generate deployment descriptor (<i>application.xml</i>) if needed or forced (applies for Java EE 5).
     * <p>
     * For J2EE 1.4 or older the deployment descriptor is always generated if missing.
     * For Java EE 5 it is only generated if missing and forced as well.
     *
     * @param project EAR project instance.
     * @param force if {@code true} <i>application.xml</i> is generated even if it's not needed (applies only for Java EE 5).
     *
     * @return {@link FileObject} of the deployment descriptor or {@code null} if <i>application.xml</i> were not successfully created.
     *
     * @see EarDDGeneratorImpl#generate(Project, Profile, FileObject, boolean) 
     * @since 1.6
     */
    @CheckForNull
    public static FileObject setupDD(
            @NonNull Project project,
            boolean force) {

        Parameters.notNull("project", project); //NOI18N

        EarDDGeneratorImplementation ddGenerator = project.getLookup().lookup(EarDDGeneratorImplementation.class);
        if (ddGenerator != null) {
            return ddGenerator.setupDD(force);
        }

        // Maybe we should throw UnsupportedProjectTypeException here
        return null;
    }
}
