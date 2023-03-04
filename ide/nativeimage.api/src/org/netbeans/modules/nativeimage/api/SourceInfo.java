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
package org.netbeans.modules.nativeimage.api;

import java.util.Objects;

/**
 * A source file information in the debuggee program.
 *
 * @since 0.2
 */
public final class SourceInfo {

    private final String fileName;
    private final String fullName;

    private SourceInfo(String fileName, String fullName) {
        this.fileName = fileName;
        this.fullName = fullName;
    }

    /**
     * Get the file name, or <code>null</code> when unknown. May return a relative path.
     *
     * @since 0.2
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the full file name, or <code>null</code> when unknown. Returns an absolute path, if any.
     *
     * @since 0.2
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Creates a builder to build a new {@link SourceInfo}.
     *
     * @since 0.2
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.fileName);
        hash = 17 * hash + Objects.hashCode(this.fullName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SourceInfo other = (SourceInfo) obj;
        if (!Objects.equals(this.fileName, other.fileName)) {
            return false;
        }
        if (!Objects.equals(this.fullName, other.fullName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SourceInfo{" + "fileName=" + fileName + ", fullName=" + fullName + '}';
    }

    /**
     * Symbol's builder.
     *
     * @since 0.2
     */
    public static final class Builder {

        private String fileName;
        private String fullName;

        Builder() {}

        /**
         * Set a file name. It may be a relative path.
         *
         * @since 0.2
         */
        public void fileName(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Set a full file name. It needs to be an absolute path.
         *
         * @since 0.2
         */
        public void fullName(String fullName) {
            this.fullName = fullName;
        }

        /**
         * Build the {@link Symbol} object.
         *
         * @since 0.2
         */
        public SourceInfo build() {
            return new SourceInfo(fileName, fullName);
        }
    }
}
