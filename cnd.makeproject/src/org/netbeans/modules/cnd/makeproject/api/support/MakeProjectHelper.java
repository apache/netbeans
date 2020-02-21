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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.api.support;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.project.NativeProjectType;
import org.netbeans.modules.cnd.makeproject.MakeBasedProjectFactorySingleton;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;

/**
 *
 */
public interface MakeProjectHelper {

    /**
     * Relative path from project directory to the customary shared properties file.
     */
    public static final String PROJECT_PROPERTIES_PATH = "nbproject/project.properties"; // NOI18N
    /**
     * Relative path from project directory to the customary private properties file.
     */
    public static final String PRIVATE_PROPERTIES_PATH = "nbproject/private/private.properties"; // NOI18N
    
    public static final String PROJECT_LICENSE_NAME_PROPERTY = "project.license"; // NOI18N
    public static final String PROJECT_LICENSE_PATH_PROPERTY = "project.licensePath"; // NOI18N

    /**
     * Relative path from project directory to the required shared project metadata file.
     */
    public static final String PROJECT_XML_PATH = MakeBasedProjectFactorySingleton.PROJECT_XML_PATH;

    public static final String PROJECT_NS = MakeBasedProjectFactorySingleton.PROJECT_NS;

    /**
     * Relative path from project directory to the required private project metadata file.
     */
    public static final String PRIVATE_XML_PATH = "nbproject/private/private.xml"; // NOI18N
    /**
     * Get the corresponding Ant-based project type factory.
     */
    NativeProjectType getType();

    /**
     * Add a listener to changes in the project configuration.
     * <p>Thread-safe.
     * @param listener a listener to add
     */
    void addMakeProjectListener(MakeProjectListener listener);

    /**
     * Remove a listener to changes in the project configuration.
     * <p>Thread-safe.
     * @param listener a listener to remove
     */
    void removeMakeProjectListener(MakeProjectListener listener);

    /**
     * Get the top-level project directory.
     * @return the project directory beneath which everything in the project lies
     */
    FileObject getProjectDirectory();

    /**Notification that this project has been deleted.
     * @see org.netbeans.spi.project.ProjectState#notifyDeleted
     *
     * @since 1.8
     */
    void notifyDeleted();

    /**
     * Get the primary configuration data for this project.
     * The returned element will be named according to
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementName} and
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementNamespace}.
     * The project may read this document fragment to get custom information
     * from <code>nbproject/project.xml</code> and <code>nbproject/private/private.xml</code>.
     * The fragment will have no parent node and while it may be modified, you must
     * use {@link #putPrimaryConfigurationData} to store any changes.
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @return the configuration data that is available
     */
    Element getPrimaryConfigurationData(final boolean shared);

    /**
     * Store the primary configuration data for this project.
     * The supplied element must be named according to
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementName} and
     * {@link AntBasedProjectType#getPrimaryConfigurationDataElementNamespace}.
     * The project may save this document fragment to set custom information
     * in <code>nbproject/project.xml</code> and <code>nbproject/private/private.xml</code>.
     * The fragment will be cloned and so further modifications will have no effect.
     * <p>Acquires write access from {@link ProjectManager#mutex}. However, you are well
     * advised to explicitly enclose a <em>complete</em> operation within write access,
     * starting with {@link #getPrimaryConfigurationData}, to prevent race conditions.
     * @param data the desired new configuration data
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @throws IllegalArgumentException if the element is not correctly named
     */
    void putPrimaryConfigurationData(Element data, boolean shared) throws IllegalArgumentException;

    /**
     * Create an object permitting this project to store auxiliary configuration.
     * Would be placed into the project's lookup.
     * @return an auxiliary configuration provider object suitable for the project lookup
     */
    AuxiliaryConfiguration createAuxiliaryConfiguration();

    /**
     * Create an implementation of the file sharability query.
     * You may specify a list of source roots to include that should be considered sharable,
     * as well as a list of build directories that should not be considered sharable.
     * <p>
     * The project directory itself is automatically included in the list of sharable directories
     * so you need not explicitly specify it.
     * Similarly, the <code>nbproject/private</code> subdirectory is automatically excluded
     * from VCS, so you do not need to explicitly specify it.
     * </p>
     * <p>
     * Any file (or directory) mentioned (explicitly or implicity) in the source
     * directory list but not in any of the build directory lists, and not containing
     * any build directories inside it, will be given as sharable. If a directory itself
     * is sharable but some directory inside it is not, it will be given as mixed.
     * A file or directory inside some build directory will be listed as not sharable.
     * A file or directory matching neither the source list nor the build directory list
     * will be treated as of unknown status, but in practice such a file should never
     * have been passed to this implementation anyway - {@link org.netbeans.api.queries.SharabilityQuery} will
     * normally only call an implementation in project lookup if the file is owned by
     * that project.
     * </p>
     * <p>
     * Each entry in either list should be a string evaluated first for Ant property
     * escapes (if any), then treated as a file path relative to the project directory
     * (or it may be absolute).
     * </p>
     * <p>
     * It is permitted, and harmless, to include items that overlap others. For example,
     * you can have both a directory and one of its children in the include list.
     * </p>
     * <p>
     * Whether or not you use this method, all files named <code>*-private.properties</code>
     * outside the project are marked unsharable, as are such files inside the project if currently referenced
     * as project libraries. (See {@link #getProjectLibrariesPropertyProvider}.)
     * </p>
     * <div class="nonnormative">
     * <p>
     * Typical usage would be:
     * </p>
     * <pre>
     * helper.createSharabilityQuery(helper.getStandardPropertyEvaluator(),
     *                               new String[] {"${src.dir}", "${test.src.dir}"},
     *                               new String[] {"${build.dir}", "${dist.dir}"})
     * </pre>
     * <p>
     * A quick rule of thumb is that the include list should contain any
     * source directories which <em>might</em> reside outside the project directory;
     * and the exclude list should contain any directories which you would want
     * to add to a <samp>.cvsignore</samp> file if using CVS (for example).
     * </p>
     * <p>
     * Note that in this case <samp>${src.dir}</samp> and <samp>${test.src.dir}</samp>
     * may be relative paths inside the project directory; relative paths pointing
     * outside of the project directory; or absolute paths (generally outside of the
     * project directory). If they refer to locations inside the project directory,
     * including them does nothing but is harmless - since the project directory itself
     * is always treated as sharable. If they refer to external locations, you will
     * need to also make sure that {@link org.netbeans.api.project.FileOwnerQuery} actually maps files in those
     * directories to this project, or else {@link org.netbeans.api.queries.SharabilityQuery} will never find
     * this implementation in your project lookup and may return <code>UNKNOWN</code>.
     * </p>
     * </div>
     * @param eval a property evaluator to interpret paths with
     * @param sourceRoots a list of additional paths to treat as sharable
     * @param buildDirectories a list of paths to treat as not sharable
     * @return a sharability query implementation suitable for the project lookup
     * @see Project#getLookup
     */
    SharabilityQueryImplementation2 createSharabilityQuery(String[] sourceRoots, String[] buildDirectories);
    
    FileObject resolveFileObject(String filename);
    
    FSPath resolveFSPath(String filename);
}
