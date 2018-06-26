/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.netbeans.api.queries.VisibilityQuery;

/**
 *
 * @author Petr Pisl
 */
public class FileUtils {
    
    final static String GO_UP = "../"; //NOI18N
    private static final String SLASH = "/"; //NOI18N
    private static final String FILE = "file"; // URI scheme
    
    private static final Logger LOG = Logger.getLogger(FileUtils.class.getName());
    
    public static FileObject findFileObject(final FileObject fromFO, final String path, boolean filesOnly) {
        FileObject parent = fromFO.getParent();
        FileObject targetFO;
        Project project = FileOwnerQuery.getOwner(fromFO);
        String projectDirectoryPath = ""; //NOI18N
        if (project != null) {
            FileObject projectDirectory = project.getProjectDirectory();
            projectDirectoryPath = projectDirectory.getPath();
        }
        if (parent != null && !path.isEmpty()) {
            while (parent != null && parent.getPath().contains(projectDirectoryPath)) {
                targetFO = parent.getFileObject(path);
                if (targetFO != null && (!filesOnly || (filesOnly && !targetFO.isFolder()))) {
                    return targetFO;
                }
                targetFO = parent.getFileObject(path + ".js");  // NOI18N
                if (targetFO != null) {
                    return targetFO;
                }
                parent = parent.getParent();
            }
        }

        return null;
    }
    
    public interface FileObjectFilter {

        boolean accept(FileObject file);

    }
    
    public static List<CompletionProposal> computeRelativeItems(
            Collection<? extends FileObject> relativeTo,
            final String prefix,
            int anchor,
            boolean addExtensions,
            boolean addRelativePrefix,
            FileObjectFilter filter) throws IOException {

        assert relativeTo != null;

        List<CompletionProposal> result = new LinkedList();

        int lastSlash = prefix.lastIndexOf('/');
        String pathPrefix;
        String filePrefix;

        if (lastSlash != (-1)) {
            pathPrefix = prefix.substring(0, lastSlash);
            filePrefix = prefix.substring(lastSlash + 1);
        } else {
            pathPrefix = null;
            filePrefix = prefix;
        }

        Set<FileObject> directories = new HashSet();
        File prefixFile = null;
        if (pathPrefix != null && !pathPrefix.startsWith(".")) { //NOI18N
            if (pathPrefix.length() == 0 && prefix.startsWith(SLASH)) {
                prefixFile = new File(SLASH); //NOI18N
            } else {
                prefixFile = new File(pathPrefix);
            }
        }
        if (prefixFile != null && prefixFile.exists()) {
            //absolute path
            File normalizeFile = FileUtil.normalizeFile(prefixFile);
            FileObject fo = FileUtil.toFileObject(normalizeFile);
            if (fo != null) {
                directories.add(fo);
            }
        } else {
            //relative path
            for (Iterator<? extends FileObject> it = relativeTo.iterator(); it.hasNext();) {
                FileObject f = it.next();
                if (pathPrefix != null) {
                    File toFile = FileUtil.toFile(f);
                    if (toFile != null) {
                        URI resolve = null;
                        try {
                            resolve = Utilities.toURI(toFile).resolve(pathPrefix).normalize();
                        } catch (IllegalArgumentException ex) {
                            resolve = null;
                        }
                        if (resolve != null && (resolve.getScheme() == null || FILE.equals(resolve.getScheme()))) {
                            try {
                                File normalizedFile = FileUtil.normalizeFile(Utilities.toFile(resolve));
                                f = FileUtil.toFileObject(normalizedFile);
                            } catch (IllegalArgumentException e) {
                                 LOG.log(Level.FINE, "could not convert " + resolve + " to File", resolve);
                            }
                        }
                    } else {
                        f = f.getFileObject(pathPrefix);
                    }
                }

                if (f != null) {
                    if (!f.isFolder()) {
                        if (f.getParent() != null)
                        directories.add(f.getParent());
                    } else {
                        directories.add(f);
                    }
                }
            }
        }

        for (FileObject dir : directories) {
            FileObject[] children = dir.getChildren();

            for (int cntr = 0; cntr < children.length; cntr++) {
                FileObject current = children[cntr];

                if (VisibilityQuery.getDefault().isVisible(current) && current.getNameExt().toLowerCase().startsWith(filePrefix.toLowerCase()) && filter.accept(current)) {
                    int newAnchor = anchor - prefix.length();

                    result.add(new FSCompletionItem(current, pathPrefix != null ? pathPrefix + SLASH : ((filePrefix.isEmpty() && addRelativePrefix) ? "./" : ""), addExtensions, newAnchor)); //NOI18N
                }
            }
        }


        return result;
    }
}
