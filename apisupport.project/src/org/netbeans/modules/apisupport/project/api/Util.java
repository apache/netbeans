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

package org.netbeans.modules.apisupport.project.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.Comparator;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.apisupport.project.layers.PathCompletions;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 * Utility methods for the module.
 *
 * @author Jesse Glick, Martin Krauskopf
 */
public final class Util {

    static {
        PathCompletions.register();
    }
    
    private Util() {}
    
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.project"); // NOI18N
    
    /**
     * Convenience method for loading {@link EditableProperties} from a {@link
     * FileObject}. New items will alphabetizied by key.
     *
     * @param propsFO file representing properties file
     * @exception FileNotFoundException if the file represented by the given
     *            FileObject does not exists, is a folder rather than a regular
     *            file or is invalid. i.e. as it is thrown by {@link
     *            FileObject#getInputStream()}.
     */
    public static EditableProperties loadProperties(FileObject propsFO) throws IOException {
        InputStream propsIS = propsFO.getInputStream();
        EditableProperties props = new EditableProperties(true);
        try {
            props.load(propsIS);
        } finally {
            propsIS.close();
        }
        return props;
    }
    
    /**
     * Convenience method for storing {@link EditableProperties} into a {@link
     * FileObject}.
     *
     * @param propsFO file representing where properties will be stored
     * @param props properties to be stored
     * @exception IOException if properties cannot be written to the file
     */
    public static void storeProperties(FileObject propsFO, EditableProperties props) throws IOException {
        OutputStream os = propsFO.getOutputStream();
        try {
            props.store(os);
        } finally {
            os.close();
        }
    }
    
    /**
     * Convenience method for loading {@link EditableManifest} from a {@link
     * FileObject}.
     *
     * @param manifestFO file representing manifest
     * @exception FileNotFoundException if the file represented by the given
     *            FileObject does not exists, is a folder rather than a regular
     *            file or is invalid. i.e. as it is thrown by {@link
     *            FileObject#getInputStream()}.
     */
    public static @NonNull EditableManifest loadManifest(@NonNull FileObject manifestFO) throws IOException {
        InputStream mfIS = manifestFO.getInputStream();
        try {
            return new EditableManifest(mfIS);
        } finally {
            mfIS.close();
        }
    }
    
    /**
     * Convenience method for storing {@link EditableManifest} into a {@link
     * FileObject}.
     *
     * @param manifestFO file representing where manifest will be stored
     * @param em manifest to be stored
     * @exception IOException if manifest cannot be written to the file
     */
    public static void storeManifest(FileObject manifestFO, EditableManifest em) throws IOException {
        OutputStream os = manifestFO.getOutputStream();
        try {
            em.write(os);
        } finally {
            os.close();
        }
    }

    public static @CheckForNull Manifest getManifest(@NullAllowed FileObject manifestFO) {
        if (manifestFO != null) {
            try {
                InputStream is = manifestFO.getInputStream();
                try {
                    return new Manifest(is);
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                Logger.getLogger(Util.class.getName()).log(Level.INFO, "Could not parse: " + manifestFO, e);
            }
        }
        return null;
    }
    
    /**
     * Order projects by display name.
     */
    public static Comparator<Project> projectDisplayNameComparator() {
        return new Comparator<Project>() {
            private final Collator LOC_COLLATOR = Collator.getInstance();
            public int compare(Project o1, Project o2) {
                ProjectInformation i1 = ProjectUtils.getInformation(o1);
                ProjectInformation i2 = ProjectUtils.getInformation(o2);
                int result = LOC_COLLATOR.compare(i1.getDisplayName(), i2.getDisplayName());
                if (result != 0) {
                    return result;
                } else {
                    result = i1.getName().compareTo(i2.getName());
                    if (result != 0) {
                        return result;
                    } else {
                        return System.identityHashCode(o1) - System.identityHashCode(o2);
                    }
                }
            }
        };
    }
    
    /**
     * when ever there is need for non-java files creation or lookup,
     * use this method to get the right location for all projects. 
     * Eg. maven places resources not next to the java files.
     * Please note that the method should not be used for 
     * checking file existence. There can be multiple resource roots, the returned one
     * is just the first in line. Use <code>getResource</code> instead in that case.
     */ 
    public static FileObject getResourceDirectory(Project prj) {
        Sources srcs = ProjectUtils.getSources(prj);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (grps != null && grps.length > 0) {
            return grps[0].getRootFolder();
        }
        // fallback to sources..
        NbModuleProvider prov = prj.getLookup().lookup(NbModuleProvider.class);
        assert prov != null;
        return prov.getSourceDirectory();
    }
    
    /**
     * check if resource of given path exists in the current project resources.
     * 
     * @param prj
     * @param path as in <code>FileObject.getFileObject(path)</code>
     * @return FileObject or null if not found.
     * @since 1.57
     */
    public static FileObject getResource(Project prj, String path) {
        Sources srcs = ProjectUtils.getSources(prj);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (grps != null && grps.length > 0) {
            for (SourceGroup sg : grps) {
                FileObject fo = sg.getRootFolder().getFileObject(path);
                if (fo != null) {
                    return fo;
                }
            }
        }
        // fallback to sources..
        NbModuleProvider prov = prj.getLookup().lookup(NbModuleProvider.class);
        assert prov != null;
        return prov.getSourceDirectory().getFileObject(path);
    }
}
