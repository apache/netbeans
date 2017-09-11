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

package org.netbeans.modules.j2ee.persistence.spi;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * This interface should be implemented in a context which supports
 * Java Persistence API, whether or not is contains a persistence.xml file. It
 * contains methods for creating/retrieving the default location for persistence.xml
 * files. For example it can be implemented by a project and can be used by 
 * a client which wants to create a persistence scope in that project.
 *
 * @author Andrei Badea
 */
public interface PersistenceLocationProvider {

    /**
     * Returns the default location for persistence.xml and related files.
     *
     * @return the default location or null if it does not exist.
     */
    FileObject getLocation();

    /**
     * Returns the location for persistence.xml and related files for the given
     * FileObject.
     *
     * @param fo the FileObject
     * @return the location or null if it does not exist.
     * @since 1.37
     */
    default FileObject getLocation(FileObject fo) {
        return getLocation();
    }

    /**
     * Creates (if it does not exist) and returns the default location for
     * persistence.xml and related files.
     *
     * @return the default location or null if the location could not have been
     *         created (for example, because the implementor could not determine
     *         a proper location).
     *
     * @throws IOException if an error occured while creating the location
     *         of persistence.xml
     */
    FileObject createLocation() throws IOException;

    /**
     * Creates (if it does not exist) and returns the location for
     * persistence.xml and related files for the given FileObject.
     *
     * @param fo the FileObject
     * @return the location or null if the location could not have been
     *         created (for example, because the implementor could not determine
     *         a proper location).
     *
     * @throws IOException if an error occured while creating the location
     *         of persistence.xml
     * @since 1.37
     */
    default FileObject createLocation(FileObject fo) throws IOException {
        return createLocation();
    }
}
