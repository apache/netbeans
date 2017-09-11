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

package org.netbeans.modules.versioning.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Should be implemented by a particular VCS system and returned on a {@link FileObject#getAttribute(java.lang.String)}
 * call with the {@link #PROVIDED_EXTENSIONS_SEARCH_HISTORY} argument.
 * 
 * @author Tomas Stupka
 */
public abstract class SearchHistorySupport {

    public static final String PROVIDED_EXTENSIONS_SEARCH_HISTORY = "ProvidedExtensions.SearchHistorySupport";

    private static final Logger LOG = Logger.getLogger(SearchHistorySupport.class.getName());

    private final File file;

    protected SearchHistorySupport(File file) {
        this.file = file;
    }

    public static SearchHistorySupport getInstance(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if(fo == null) {
            return null;
        }
        SearchHistorySupport support = (SearchHistorySupport) fo.getAttribute(PROVIDED_EXTENSIONS_SEARCH_HISTORY);
        return support;
    }

    protected File getFile() {
        return file;
    }

    /**
     * @see org.netbeans.modules.bugtracking.spi.VCSAccessor#searchHistory(File, int)
     */
    public boolean searchHistory(int line) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in  awt!";
        if (!file.exists()) {
            LOG.log(Level.WARNING, "Trying to show history for non-existent file {0}", file.getAbsolutePath());
            return false;
        }
        if (!file.isFile()) {
            LOG.log(Level.WARNING, "Trying to show history for a folder {0}", file.getAbsolutePath());
            return false;
        }
        return searchHistoryImpl(line);
    }

    protected abstract boolean searchHistoryImpl(int line) throws IOException;

}
