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
package org.netbeans.modules.nativeexecution.support.filesearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

public final class FileSearchParams {

    private final ExecutionEnvironment execEnv;
    private final List<String> searchPaths;
    private final String filename;
    private final boolean searchInUserPaths;

    public FileSearchParams(ExecutionEnvironment execEnv, List<String> searchPaths, String filename, boolean searchInUserPaths) {
        if (execEnv == null || searchPaths == null || filename == null) {
            throw new NullPointerException("FileSearchParams cannot be null"); // NOI18N
        }

        this.execEnv = execEnv;
        this.searchPaths = Collections.unmodifiableList(new ArrayList<>(searchPaths));
        this.filename = filename;
        this.searchInUserPaths = searchInUserPaths;
    }

    public ExecutionEnvironment getExecEnv() {
        return execEnv;
    }

    public String getFilename() {
        return filename;
    }

    public List<String> getSearchPaths() {
        return searchPaths;
    }

    public boolean isSearchInUserPaths() {
        return searchInUserPaths;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileSearchParams)) {
            return false;
        }

        final FileSearchParams that = (FileSearchParams) obj;

        return this.searchInUserPaths == that.searchInUserPaths &&
                this.execEnv.equals(that.execEnv) &&
                this.filename.equals(that.filename) &&
                this.searchPaths.equals(that.searchPaths);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.execEnv != null ? this.execEnv.hashCode() : 0);
        hash = 29 * hash + (this.searchPaths != null ? this.searchPaths.hashCode() : 0);
        hash = 29 * hash + (this.filename != null ? this.filename.hashCode() : 0);
        hash = 29 * hash + (this.searchInUserPaths ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("File to search: " + filename + "; "); // NOI18N
        sb.append("Search env: " + execEnv.toString() + "; "); // NOI18N
        sb.append("Search paths: " + Arrays.toString(searchPaths.toArray(new String[0])) + "; "); // NOI18N
        sb.append("Search in PATH: " + (searchInUserPaths ? "yes" : "no")); // NOI18N
        return sb.toString();
    }
}
