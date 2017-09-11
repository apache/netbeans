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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
/** Methods for accessing an archive. Archive represents zip file or
 * folder.
 *
 * @author Petr Hrebejk
 */
public interface Archive {
    // New implementation Archive Interface ------------------------------------


    /** Gets all files in given folder
     *  @param folderName name of folder to list, path elements separated by / char
     *  @param entry owning ClassPath.Entry to check the excludes or null if everything should be included
     *  @param kinds to list, may be null => all types
     *  @param filter to filter the file content
     *  @param recursive if true content of subfolders is included
     *  @return the listed files
     */
    public Iterable<JavaFileObject> getFiles( String folderName, ClassPath.Entry entry, Set<JavaFileObject.Kind> kinds, JavaFileFilterImplementation filter, boolean recursive) throws IOException;

    /*
     * Returns a new {@link JavaFileObject} for given path.
     * May throw an UnsupportedOperationException if the operation is not supported (eg. zip archive).
     * @param relativePath path from the root, separated by '/' character (resource name)
     * @return the {@link JavaFileObject}
     */
    public JavaFileObject create (final String relativeName, JavaFileFilterImplementation filter) throws UnsupportedOperationException;

    /**
     * Cleans cached data
     */
    public void clear ();

    /**
     * Returns a {@link JavaFileObject} for given name or null
     * @param name of resource
     * @return a file
     */
    public JavaFileObject getFile(final @NonNull String name) throws IOException;

    /**
     * Checks if the {@link Archive} is represents a multi release archive.
     * @return true if the {@link Archive} is supports multiple releases.
     */
    public boolean isMultiRelease();
}
