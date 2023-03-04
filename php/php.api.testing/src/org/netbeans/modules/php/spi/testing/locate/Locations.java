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
package org.netbeans.modules.php.spi.testing.locate;

import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * This class contains several representations of a location.
 */
public final class Locations {

    private Locations() {
    }

    //~ Inner classes

    /**
     * Location with a line number.
     */
    public static final class Line {

        private final FileObject file;
        private final int line;


        /**
         * Create new location.
         * @param file file
         * @param line line number, can be e.g. -1 if not known
         */
        public Line(@NonNull FileObject file, int line) {
            Parameters.notNull("file", file);
            this.file = file;
            this.line = line;
        }

        /**
         * Get the file of this location.
         * @return the file of this location
         */
        public FileObject getFile() {
            return file;
        }

        /**
         * Get the line of this location.
         * @return the line of this location, can be e.g. -1 if not known
         */
        public int getLine() {
            return line;
        }

        @Override
        public String toString() {
            return "Locations.Line{" + "file=" + file + ", line=" + line + '}'; // NOI18N
        }

    }

    /**
     * Location with an offset.
     */
    public static final class Offset {

        private final FileObject file;
        private final int offset;


        /**
         * Create new location.
         * @param file file
         * @param offset offset, can be e.g. -1 if not known
         */
        public Offset(@NonNull FileObject file, int offset) {
            Parameters.notNull("file", file);
            this.file = file;
            this.offset = offset;
        }

        /**
         * Get the file of this location.
         * @return the file of this location
         */
        public FileObject getFile() {
            return file;
        }

        /**
         * Get the offset of this location.
         * @return the offset of this location, can be e.g. -1 if not known
         */
        public int getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            return "Locations.Offset{" + "file=" + file + ", offset=" + offset + '}'; // NOI18N
        }

    }

}
