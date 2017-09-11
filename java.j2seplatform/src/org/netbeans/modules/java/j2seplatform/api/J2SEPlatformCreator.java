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

package org.netbeans.modules.java.j2seplatform.api;

import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Creates a new platform definition.
 * @since 1.11
 */
public class J2SEPlatformCreator {

    private J2SEPlatformCreator() {}

    /**
     * Create a new J2SE platform definition.
     * @param installFolder the installation folder of the JDK
     * @return the newly created platform
     * @throws IOException if the platform was invalid or its definition could not be stored
     */
    @NonNull
    public static JavaPlatform createJ2SEPlatform(@NonNull final FileObject installFolder) throws IOException {
        Parameters.notNull("installFolder", installFolder); //NOI18N
        return J2SEPlatformFactory.getInstance().create(installFolder);
    }

    /**
     * Create a new J2SE platform definition with given display name.
     * @param installFolder the installation folder of the JDK
     * @param platformName  the desired display name
     * @return the newly created platform
     * @throws IOException if the platform was invalid or its definition could not be stored
     * @throws IllegalArgumentException if a platform of given display name already exists
     * @since 1.23
     */
    @NonNull
    public static JavaPlatform createJ2SEPlatform(
            @NonNull final FileObject installFolder,
            @NonNull final String platformName) throws IOException , IllegalArgumentException {
        Parameters.notNull("installFolder", installFolder);  //NOI18N
        Parameters.notNull("platformName", platformName);    //NOI18N
        return J2SEPlatformFactory.getInstance().create(installFolder, platformName, true);
    }    
}
