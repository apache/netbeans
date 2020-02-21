/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.utils;

import java.io.File;
import org.netbeans.modules.cnd.utils.FileFilterFactory.AbstractFileAndFileObjectFilter;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;

/**
 *
 */
/* A combination of FileFilter and FileObjectFilter */
public abstract class FileAndFileObjectFilter extends AbstractFileAndFileObjectFilter {

    @Override
    public final boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }
            if (accept(f.getName())) {
                return true;
            }
            return mimeAccept(f);
        }
        return false;
    }

    protected boolean mimeAccept(File f) {
        return false;
    }

    @Override
    public final boolean accept(FileObject f) {
        if (f != null) {
            if (f.isFolder()) {
                return true;
            }
            if (accept(f.getNameExt())) {
                return true;
            }
            return mimeAccept(f);
        }
        return false;
    }

    protected boolean mimeAccept(FileObject f) {
        return false;
    }

    protected final boolean accept(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index >= 0) {
            // Match suffix
            String suffix = fileName.substring(index + 1);
            if (amongSuffixes(suffix, getSuffixes())) {
                return true;
            }
        } else {
            // Match entire name
            if (amongSuffixes(fileName, getSuffixes())) {
                return true;
            }
        }
        return false;
    }

    protected abstract String[] getSuffixes();

    @Override
    public String getSuffixesAsString() {
        String[] suffixes = getSuffixes();
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < suffixes.length; i++) {
            if (0 < i) {
                ret.append(' '); // NOI18N
            }
            ret.append('.').append(suffixes[i]); // NOI18N
        }
        return ret.toString();
    }

    private boolean amongSuffixes(String suffix, String[] suffixes) {
        for (int i = 0; i < suffixes.length; i++) {
            if (areFilenamesEqual(suffixes[i], suffix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    protected boolean areFilenamesEqual(String firstFile, String secondFile) {
        return CndFileUtils.isSystemCaseSensitive() ? firstFile.equals(secondFile) : firstFile.equalsIgnoreCase(secondFile);
    }
}
