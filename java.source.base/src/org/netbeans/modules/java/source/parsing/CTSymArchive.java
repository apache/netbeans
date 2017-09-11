/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Specialized {@link CachingArchive} for ctsym file.
 * It provides API classes from ct.sym and non API classes from original archive file.
 * @author Tomas Zezula
 */
public class CTSymArchive extends CachingArchive {

    private static final Logger LOG = Logger.getLogger(CTSymArchive.class.getName());

    private final File ctSym;
    private final String pathToRootInCtSym;
    private ZipFile zipFile;
    private Map<String,Set<String>> pkgs;

    CTSymArchive(
        @NonNull final File archive,
        @NullAllowed final String pathToRootInArchive,
        @NonNull final File ctSym,
        @NullAllowed final String pathToRootInCtSym) {
        super(archive, pathToRootInArchive, true);
        this.ctSym = ctSym;
        this.pathToRootInCtSym = pathToRootInCtSym;
    }

    @Override
    protected void beforeInit() throws IOException {
        zipFile = new ZipFile(ctSym);
        pkgs = new HashMap<>();
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            final String name = entry.getName();
            String dirname;
            String basename;
            if (pathToRootInCtSym != null) {
                if (!name.startsWith(pathToRootInCtSym)) {
                    continue;
                }
                final int i = name.lastIndexOf(FileObjects.NBFS_SEPARATOR_CHAR);
                dirname = i < pathToRootInCtSym.length() ?
                    "" :    //NOI18N
                    name.substring(pathToRootInCtSym.length(), i);
                basename = name.substring(i+1);
            } else {
                final int i = name.lastIndexOf(FileObjects.NBFS_SEPARATOR_CHAR);
                dirname = i == -1 ? "" : name.substring(0, i);  //NOI18N
                basename = name.substring(i+1);
            }
            Set<String> content = pkgs.get(dirname);
            if (content == null) {
                pkgs.put(dirname, content = new HashSet<>());
            }
            content.add(basename);
        }
    }

    @Override
    protected short getFlags(@NonNull final String dirname) throws IOException {
        boolean isPublic = pkgs.containsKey(dirname);
        LOG.log(
            Level.FINE,
            "Package: {0} is public: {1}", //NOI18N
            new Object[]{
                dirname,
                isPublic
            });
        return (short) (isPublic ? 0 : 1);
    }

    @Override
    protected boolean includes(int flags, String folder, String name) {
        if (flags == 0) {
            final Set<String> content = pkgs.get(folder);
            return content == null ?
                    false :
                    content.contains(name);
        } else {
            return super.includes(flags, folder, name);
        }
    }

    @Override
    protected void afterInit(final boolean success) throws IOException {
        pkgs = null;
    }

    @Override
    protected ZipFile getArchive(final short flags) {
        return flags == 0 ?
            zipFile :
            super.getArchive(flags);
    }

    @Override
    protected String getPathToRoot(final short flags) {
        return flags == 0 ?
            pathToRootInCtSym :
            super.getPathToRoot(flags);
    }
}
