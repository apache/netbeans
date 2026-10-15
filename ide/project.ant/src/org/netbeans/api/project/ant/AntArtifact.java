/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * this information to add an <em>&lt;ant&gt;</em> task to another project
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
     * The name <em>build.xml</em> is conventional.
     * @return the location of an Ant project file (might not currently exist)
     */
    public abstract File getScriptLocation();
    
    /**
     * Get the name of the Ant target that is able to produce this artifact.
     * E.g. <em>jar</em> would be conventional for JAR artifacts.
     * @return an Ant target name
     */
    public abstract String getTargetName();
    
    /**
     * Get the name of an Ant target that will delete this artifact.
     * Typically this should be <em>clean</em>.
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
     * For example, <em>dist/mylib.jar</em>. The method is not defined 
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
            artifact = BaseUtilities.normalizeURI(BaseUtilities.toURI(getScriptLocation()).resolve(artifactLocation)).toURL();
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
        return l.toArray(new FileObject[0]);
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
