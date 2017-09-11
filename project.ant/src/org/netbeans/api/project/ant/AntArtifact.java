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

package org.netbeans.api.project.ant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;

// XXX may also need displayName field (any default? or only in SimpleAntArtifact?)

/**
 * Represents one artifact of an Ant build.
 * For example, if a build script is known to generate a JAR of a certain name
 * as a result of running a certain target, this object will name that JAR
 * and point to the script and target responsible for creating it. You can use
 * this information to add an <samp>&lt;ant&gt;</samp> task to another project
 * which will generate that JAR as a dependency before using it.
 * @see org.netbeans.spi.project.support.ant.SimpleAntArtifact
 * @author Jesse Glick
 */
public abstract class AntArtifact {

    private final Properties PROPS = new Properties();
    
    /**
     * Empty constructor for use from subclasses.
     */
    protected AntArtifact() {
        try {
            if (getClass().getMethod("getArtifactLocation").getDeclaringClass() == AntArtifact.class && // NOI18N
                    getClass().getMethod("getArtifactLocations").getDeclaringClass() == AntArtifact.class) { // NOI18N
                // #72308: at least one must be overridden
                throw new IllegalStateException(getClass().getName() + ".getArtifactLocations() must be overridden"); // NOI18N
            }
        } catch (NoSuchMethodException x) {
            throw new AssertionError(x);
        }
    }
    
    /**
     * Get the type of the build artifact.
     * This can refer to both the physical content type or format;
     * and to the intended category of usage.
     * Typically a given client (e.g. superproject) will be interested
     * in only a certain artifact type for a certain purpose, e.g.
     * inclusion in a Java classpath.
     * <p>
     * Particular type identifiers should be agreed upon between
     * providers and clients.
     * For example, <a href="@org-netbeans-modules-java-project@/org/netbeans/api/java/project/JavaProjectConstants.html#ARTIFACT_TYPE_JAR"><code>JavaProjectConstants.ARTIFACT_TYPE_JAR</code></a>
     * is defined for JAR outputs.
     * Others may be defined as needed; for example, tag library JARs,
     * WARs, EJB JARs, deployment descriptor fragments, etc.
     * <p>
     * Since the type will be stored in XML, avoid whitespace.
     * @return the type (format or usage) of the build artifact
     */
    public abstract String getType();
    
    /**
     * Get a location for the Ant script that is able to produce this artifact.
     * The name <samp>build.xml</samp> is conventional.
     * @return the location of an Ant project file (might not currently exist)
     */
    public abstract File getScriptLocation();
    
    /**
     * Get the name of the Ant target that is able to produce this artifact.
     * E.g. <samp>jar</samp> would be conventional for JAR artifacts.
     * @return an Ant target name
     */
    public abstract String getTargetName();
    
    /**
     * Get the name of an Ant target that will delete this artifact.
     * Typically this should be <samp>clean</samp>.
     * The target may delete other build products as well.
     * @return an Ant target name
     */
    public abstract String getCleanTargetName();
    
    /**
     * Get the location of the build artifact relative to the Ant script.
     * See {@link #getArtifactLocations}.
     * @return a URI to the build artifact, resolved relative to {@link #getScriptLocation};
     *         may be either relative, or an absolute <code>file</code>-protocol URI
     * @deprecated use {@link #getArtifactLocations} instead
     */
    @Deprecated
    public URI getArtifactLocation() {
        return getArtifactLocations()[0];
    }

    private static final Set<String> warnedClasses = Collections.synchronizedSet(new HashSet<String>());
    /**
     * Get the locations of the build artifacts relative to the Ant script.
     * For example, <samp>dist/mylib.jar</samp>. The method is not defined 
     * as abstract only for backward compatibility reasons. <strong>It must be
     * overridden.</strong> The order is important and should stay the same
     * unless the artifact was changed.
     * @return an array of URIs to the build artifacts, resolved relative to {@link #getScriptLocation};
     *         may be either relative, or an absolute <code>file</code>-protocol URI
     * @since 1.5
     */
    public URI[] getArtifactLocations() {
        String name = getClass().getName();
        if (warnedClasses.add(name)) {
            Logger.getLogger(AntArtifact.class.getName()).warning(name + ".getArtifactLocations() must be overridden");
        }
        return new URI[]{getArtifactLocation()};
    }

    /**
     * Returns identifier of the AntArtifact which must be <strong>unique within
     * one project</strong>. By default it is target name which produces the
     * artifact, but if your target produces more that one artifact then
     * you must override this method and uniquely identify each artifact.
     */
    public String getID() {
        return getTargetName();
    }

    /**
     * Convenience method to find the actual artifact, if it currently exists.
     * See {@link #getArtifactFiles}.
     * @return the artifact file on disk, or null if it could not be found
     * @deprecated use {@link #getArtifactFiles} instead
     */
    @Deprecated
    public final FileObject getArtifactFile() {
        FileObject fos[] = getArtifactFiles();
        if (fos.length > 0) {
            return fos[0];
        } else {
            return null;
        }
    }
    
    private FileObject getArtifactFile(URI artifactLocation) {
        assert !artifactLocation.isAbsolute() ||
            (!artifactLocation.isOpaque() && "file".equals(artifactLocation.getScheme())) // NOI18N
            : artifactLocation;
        URL artifact;
        try {
            // XXX this should probably use something in PropertyUtils?
            artifact = BaseUtilities.toURI(getScriptLocation()).resolve(artifactLocation).normalize().toURL();
        } catch (MalformedURLException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
            return null;
        }
        FileObject fo = URLMapper.findFileObject(artifact);
        if (fo != null) {
            assert FileUtil.toFile(fo) != null : fo;
            return fo;
        } else {
            return null;
        }
    }
    
    /**
     * Convenience method to find the actual artifacts, if they currently exist.
     * Uses {@link #getScriptFile} or {@link #getScriptLocation} and resolves {@link #getArtifactLocations} from it.
     * Note that a project which has been cleaned more recently than it has been built
     * will generally not have the build artifacts on disk and so this call may easily
     * return empty array. If you do not rely on the actual presence of the file but just need to
     * refer to it abstractly, use {@link #getArtifactLocations} instead.
     * @return the artifact files which exist on disk, or empty array if none could be found
     * @since 1.5
     */
    public final FileObject[] getArtifactFiles() {
        List<FileObject> l = new ArrayList<FileObject>();
        for (URI artifactLocation : getArtifactLocations()) {
            FileObject fo = getArtifactFile(artifactLocation);
            if (fo != null) {
                l.add(fo);
            }
        }
        return l.toArray(new FileObject[l.size()]);
    }
    
    /**
     * Convenience method to find the actual script file, if it currently exists.
     * Uses {@link #getScriptLocation}.
     * The script must exist on disk (Ant cannot run scripts from NetBeans
     * filesystems unless they are represented on disk).
     * @return the Ant build script file, or null if it could not be found
     */
    public final FileObject getScriptFile() {
        FileObject fo = FileUtil.toFileObject(getScriptLocation());
        assert fo == null || FileUtil.toFile(fo) != null : fo;
        return fo;
    }
    
    /**
     * Find the project associated with this script, if any.
     * The default implementation uses {@link #getScriptLocation} and {@link FileOwnerQuery},
     * but subclasses may override that to return something else.
     * @return the associated project, or null if there is none or it could not be located
     */
    public Project getProject() {
        return FileOwnerQuery.getOwner(BaseUtilities.toURI(getScriptLocation()));
    }

    /**
     * Optional properties which are used for Ant target execution. Only
     * properties necessary for customization of Ant target execution should
     * be used. These properties are stored in project.xml of project using 
     * this artifact so care should be taken in defining what properties
     * are used, e.g. never use absolute path like values
     * @since 1.5
     */
    public Properties getProperties() {
        return PROPS;
    }
    
}
