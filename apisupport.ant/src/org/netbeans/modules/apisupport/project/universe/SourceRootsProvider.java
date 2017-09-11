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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Richard Michalsky
 */
public interface SourceRootsProvider {

    /**
     * Property name.
     */
    public static final String PROP_SOURCE_ROOTS = "sourceRoots"; // NOI18N

    /**
     * Add given source root to the current source root list and save the
     * result.
     */
    void addSourceRoot(URL root) throws IOException;

    /**
     * Find sources for a module JAR file contained in this destination directory.
     * @param jar a JAR file in the destination directory
     * @return the directory of sources for this module (a project directory), or null
     */
    File getSourceLocationOfModule(File jar);

    /**
     * Get associated source roots for this provider.
     * Each root could be a netbeans.org source checkout or a module suite project directory.
     * @return a list of source root URLs (may be empty but not null)
     */
    URL[] getSourceRoots();

    /**
     * When no source roots are explicitly given, this may return default ones.
     * @return default source roots or <tt>null</tt>
     */
    URL[] getDefaultSourceRoots();

    /**
     * Remove given source roots from the current source root list and save the
     * result.
     */
    void removeSourceRoots(URL[] urlsToRemove) throws IOException;

    /**
     * Set source roots for this provider.
     * Each root could be a netbeans.org source checkout or a module suite project directory.
     * @param roots an array of source root URLs (may be empty but not null)
     */
    void setSourceRoots(URL[] roots) throws IOException;

    /**
     * Moves entry one step up in the list of source roots and saves the result.
     * Does nothing if <tt>indexToUp</tt> is 0 or negative.
     * @param indexToUp index of entry to move
     * @throws java.io.IOException can be thrown when storing new roots.
     */
    void moveSourceRootUp(int indexToUp) throws IOException;
    
    /**
     * Moves entry one step down in the list of source roots and saves the result.
     * Does nothing if <tt>indexToDown</tt> exceeds number of source roots.
     * @param indexToDown index of entry to move
     * @throws java.io.IOException can be thrown when storing new roots.
     */
    void moveSourceRootDown(int indexToDown) throws IOException;
}
