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

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.universe.ClusterUtils;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleType;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;

/**
 * Info class about external cluster added to cluster.path.
 *
 * Contains cluster dir and locations of sources and javadoc.
 * @author Richard Michalsky
 */
public final class ClusterInfo {
    private File clusterDir;
    private boolean isPlatformCluster;
    private Project project;

    private boolean enabled;
    private URL[] sourceRoots;
    private URL[] javadocRoots;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClusterInfo other = (ClusterInfo) obj;
        if (this.clusterDir != other.clusterDir && (this.clusterDir == null || !this.clusterDir.equals(other.clusterDir))) {
            return false;
        }
        if (this.isPlatformCluster != other.isPlatformCluster) {
            return false;
        }
        if (this.project != other.project && (this.project == null || !this.project.equals(other.project))) {
            return false;
        }
        if (this.enabled != other.enabled) {
            return false;
        }
        if (! Arrays.deepEquals(this.sourceRoots, other.sourceRoots)) {
            return false;
        }
        if (! Arrays.deepEquals(this.javadocRoots, other.javadocRoots)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.clusterDir != null ? this.clusterDir.hashCode() : 0);
        hash = 47 * hash + (this.isPlatformCluster ? 1 : 0);
        hash = 47 * hash + (this.project != null ? this.project.hashCode() : 0);
        hash = 47 * hash + (this.enabled ? 1 : 0);
        hash = 47 * hash + Arrays.deepHashCode(this.sourceRoots);
        hash = 47 * hash + Arrays.deepHashCode(this.javadocRoots);
        return hash;
    }

    @Override
    public String toString() {
        return clusterDir.getAbsolutePath() +
                (isEnabled() ? "" : " (DISABLED)") +
                (project != null ? " (from " + project.getProjectDirectory() + ")" : "") +
                (isPlatformCluster ? " (PLATFORM)" : "") +
                (sourceRoots != null ? " (src=" + Arrays.toString(sourceRoots) + ")" : "") +
                (javadocRoots != null ? " (javadoc=" + Arrays.toString(javadocRoots) + ")" : "");
    }

    /**
     * True if cluster is enabled in Libraries customizer.
     * Meaningful only for external clusters.
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPlatformCluster() {
        return isPlatformCluster;
    }

    public boolean isExternalCluster() {
        return ! isPlatformCluster && project == null;
    }

    public File getClusterDir() {
        return clusterDir;
    }
    
    private ClusterInfo(File clusterDir) {
        this.clusterDir = clusterDir;
    }

    public static ClusterInfo createFromCP(File evaluatedPath, Project prj, 
            boolean isPlatformCluster, URL[] sourceRoots, URL[] javadocRoots, boolean enabled) {
        ClusterInfo ret = new ClusterInfo(evaluatedPath);
        ret.isPlatformCluster = isPlatformCluster;
        ret.project = prj;
        ret.enabled = enabled;
        ret.sourceRoots = sourceRoots;
        ret.javadocRoots = javadocRoots;
        return ret;
    }

    public static ClusterInfo create(File clusterDir, boolean isPlatformCluster, boolean enabled) {
        ClusterInfo ret = new ClusterInfo(clusterDir);
        ret.isPlatformCluster = isPlatformCluster;
        ret.enabled = enabled;
        return ret;
    }

    public static ClusterInfo createExternal(File clusterDir, URL[] sourceRoots, URL[] javadocRoots, boolean enabled) {
        ClusterInfo ret = new ClusterInfo(clusterDir);
        ret.isPlatformCluster = false;
        ret.enabled = enabled;
        ret.sourceRoots = sourceRoots;
        ret.javadocRoots = javadocRoots;
        return ret;
    }

    public enum QueryResult {
        OK("OK"),    // NOI18N
        NOT_A_NBM_PROJECT("Not a NetBeans Module project"),// NOI18N
        NBORG_PROJECT_NOT_ALLOWED("NB.org projects are not allowed"),// NOI18N
        SUITE_COMP_NOT_ALLOWED("Suite component projects are not allowed, call for suite project instead");// NOI18N

        QueryResult(String exceptionMsg) {
            msg = exceptionMsg;
        }
        private String msg;
        public String getExceptionMessage() {
            return msg;
        }
    }

    public static QueryResult canCreate(Project project) {
        NbModuleProvider nbmp = project.getLookup().lookup(NbModuleProvider.class);
        SuiteProvider sprv = project.getLookup().lookup(SuiteProvider.class);
        if (sprv != null) {
            if (nbmp == null) {
                return QueryResult.OK;
            } else {
                return QueryResult.SUITE_COMP_NOT_ALLOWED;
            }
        } else if (nbmp != null) {
            NbModuleProject nbm = project.getLookup().lookup(NbModuleProject.class);
            if (nbm == null || nbm.getModuleType() == NbModuleType.STANDALONE) {
                return QueryResult.OK;
            } else {
                return QueryResult.NBORG_PROJECT_NOT_ALLOWED;
            }
        } else {
            return QueryResult.NOT_A_NBM_PROJECT;
        }
    }

    public static ClusterInfo create(Project project, boolean enabled) {
        QueryResult res = canCreate(project);
        if (res != QueryResult.OK)
            throw new IllegalArgumentException(res.getExceptionMessage());

        NbModuleProvider nbmp = project.getLookup().lookup(NbModuleProvider.class);
        SuiteProvider sprv = project.getLookup().lookup(SuiteProvider.class);
        assert nbmp != null || sprv != null;
        File clusterDir = (nbmp != null) ?
            ClusterUtils.getClusterDirectory(project) : sprv.getClusterDirectory();
        ClusterInfo ret = new ClusterInfo(clusterDir);
        ret.project = project;
        ret.enabled = enabled;
        return ret;
    }

    public Project getProject() {
        return project;
    }

    public URL[] getSourceRoots() {
        return sourceRoots;
    }

    public URL[] getJavadocRoots() {
        return javadocRoots;
    }

}
