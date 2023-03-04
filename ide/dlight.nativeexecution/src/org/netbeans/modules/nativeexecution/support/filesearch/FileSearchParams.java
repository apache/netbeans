/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
