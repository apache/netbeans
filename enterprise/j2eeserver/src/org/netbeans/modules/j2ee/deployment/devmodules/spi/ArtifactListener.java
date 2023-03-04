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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import java.io.File;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 * @since 1.48
 */
public interface ArtifactListener {

    public void artifactsUpdated(Iterable<Artifact> artifacts);

    public static final class Artifact {

        private final File file;

        private final File distributionPath;

        private final boolean library;

        private final boolean relocatable;

        /*
         * True if the artifact is server related - not a part of application
         * and thus intended to be deployed in a different way. For example
         * data sources, message queues etc.
         */
        private final boolean serverResource;
        
        private final String relocation;

        private Artifact(File file, File distributionPath, boolean library,
                boolean relocatable, boolean resource, String relocation) {
            this.file = file;
            this.distributionPath = distributionPath;
            this.library = library;
            this.relocatable = relocatable;
            this.serverResource = resource;
            this.relocation = relocation;
        }

        public static Artifact forFile(File file) {
            return new Artifact(FileUtil.normalizeFile(file), null, false, false, false, null);
        }

        public File getFile() {
            return file;
        }

        public Artifact referencedLibrary() {
            return new Artifact(this.file, this.distributionPath, true,
                    this.relocatable, this.isServerResource(), this.relocation);
        }

        public boolean isReferencedLibrary() {
            return library;
        }

        public Artifact distributionPath(File distributionPath) {
            return new Artifact(this.file, distributionPath, this.library,
                    this.relocatable, this.isServerResource(), this.relocation);
        }

        public File getDistributionPath() {
            return distributionPath;
        }

        public Artifact relocatable() {
            return new Artifact(this.file, this.distributionPath, this.library,
                    true, this.isServerResource(), null);
        }
        
        /**
         * 
         * @param path
         * @return 
         * @since 1.75
         */
        public Artifact relocatable(String path) {
            return new Artifact(this.file, this.distributionPath, this.library,
                    true, this.isServerResource(), path);
        }        

        public boolean isRelocatable() {
            return relocatable;
        }
        
        public String getRelocation() {
            return relocation;
        }

        public Artifact serverResource() {
            return new Artifact(this.file, this.distributionPath, this.library,
                    this.relocatable, true, this.relocation);
        }

        public boolean isServerResource() {
            return serverResource;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Artifact other = (Artifact) obj;
            if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
                return false;
            }
            if (this.distributionPath != other.distributionPath && (this.distributionPath == null || !this.distributionPath.equals(other.distributionPath))) {
                return false;
            }
            if (this.library != other.library) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (this.file != null ? this.file.hashCode() : 0);
            hash = 59 * hash + (this.distributionPath != null ? this.distributionPath.hashCode() : 0);
            hash = 59 * hash + (this.library ? 1 : 0);
            return hash;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(getFile().toString());
            builder.append(", ");
            builder.append(getDistributionPath());
            builder.append(", ");
            builder.append("isReferencedLibrary: ").append(isReferencedLibrary());
            builder.append(", ");
            builder.append("isRelocatable: ").append(isRelocatable());
            builder.append(", ");
            builder.append("isServerResource: ").append(isServerResource());
            return builder.toString();
        }

    }
}
