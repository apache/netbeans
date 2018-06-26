/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
