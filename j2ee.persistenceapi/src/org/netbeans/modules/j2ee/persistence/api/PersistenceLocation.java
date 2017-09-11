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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.persistence.api;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.openide.filesystems.FileObject;

/**
 * This class allows retrieving the default persistence location in a project
 * or creating this location if it doesn't exist.
 *
 * @author Andrei Badea
 */
public final class PersistenceLocation {

    private PersistenceLocation() {
    }

    /**
     * Returns the default persistence location in the given project.
     *
     * @param  project the project.
     * @return the persistence location or null if the project does not have
     *         a persistence location or the location does not exist.
     */
    public static FileObject getLocation(Project project) {
        PersistenceLocationProvider provider = (PersistenceLocationProvider)project.getLookup().lookup(PersistenceLocationProvider.class);
        if (provider != null) {
            return provider.getLocation();
        }
        return null;
    }

    /**
     * Returns the FileObject's persistence location in the given project.
     *
     * @param  project the project.
     * @param  fo the FileObject
     * @return the persistence location or null if the project does not have
     *         a persistence location or the location does not exist for
     *         the given FileObject.
     * @since 1.37
     */
    public static FileObject getLocation(Project project, FileObject fo) {
        PersistenceLocationProvider provider = (PersistenceLocationProvider)project.getLookup().lookup(PersistenceLocationProvider.class);
        if (provider != null) {
            return provider.getLocation(fo);
        }
        return null;
    }

    /**
     * Creates the default persistence location in the given project.
     *
     * @param  project the project.
     * @return the persistence location or null if the location could not have been
     *         created (for example, because the implementor could not determine
     *         a proper location).
     * @throws IOException if the persistence location could not be created
     *         or the project did not have an implementation of
     *         PersistenceLocationProvider in its lookup.
     */
    public static FileObject createLocation(Project project) throws IOException {
        PersistenceLocationProvider provider = (PersistenceLocationProvider)project.getLookup().lookup(PersistenceLocationProvider.class);
        if (provider != null) {
            return provider.createLocation();
        }
        throw new IOException("The project " + project + " does not have an implementation of PersistenceLocationProvider in its lookup"); // NOI18N
    }

    /**
     * Creates the FileObject's persistence location in the given project.
     *
     * @param  project the project.
     * @param fo the FileObject
     * @return the persistence location or null if the location could not have been
     *         created (for example, because the implementor could not determine
     *         a proper location).
     * @throws IOException if the persistence location could not be created
     *         or the project did not have an implementation of
     *         PersistenceLocationProvider in its lookup.
     * @since 1.37
     */
    public static FileObject createLocation(Project project, FileObject fo) throws IOException {
        PersistenceLocationProvider provider = (PersistenceLocationProvider)project.getLookup().lookup(PersistenceLocationProvider.class);
        if (provider != null) {
            return provider.createLocation(fo);
        }
        throw new IOException("The project " + project + " does not have an implementation of PersistenceLocationProvider in its lookup"); // NOI18N
    }
}
