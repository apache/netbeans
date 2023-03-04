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

package org.netbeans.modules.php.project.classpath;

import java.io.File;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Tomas Mysik
 */
public abstract class BasePathSupport {

    public static final class Item {
        public static enum Type {
            CLASSPATH,
            FOLDER
        }
        private final Type type;
        private final String filePath;
        private final boolean broken;
        protected String property;

        private Item(Type type, String filePath, String property, boolean broken) {
            this.type = type;
            this.filePath = filePath;
            this.property = property;
            this.broken = broken;
        }

        // classpath
        public static Item create(String property) {
            if (property == null) {
                throw new IllegalArgumentException("property must not be null");
            }
            return new Item(Type.CLASSPATH, null, property, false);
        }

        // folder
        public static Item create(String filePath, String property) {
            if (filePath == null) {
                throw new IllegalArgumentException("filePath must not be null");
            }
            return new Item(Type.FOLDER, filePath, property, false);
        }

        // broken folder
        public static Item createBroken(String filePath, String property) {
            if (property == null) {
                throw new IllegalArgumentException("property must not be null in broken items");
            }
            return new Item(Type.FOLDER, filePath, property, true);
        }

        public Type getType() {
            return type;
        }

        /**
         * @return file path as is (can be relative)
         */
        public String getFilePath() {
            return filePath;
        }

        /**
         * @return absolute file path (possibly resolved to the given base folder)
         */
        public String getAbsoluteFilePath(FileObject baseFolder) {
            File file = new File(filePath);
            if (file.isAbsolute()) {
                return file.getAbsolutePath();
            }
            return PropertyUtils.resolveFile(FileUtil.toFile(baseFolder), filePath).getAbsolutePath();
        }

        /**
         * @return file object (possibly resolved to the given base folder) or {@code null} if not exists
         */
        public FileObject getFileObject(FileObject baseFolder) {
            return FileUtil.toFileObject(FileUtil.normalizeFile(new File(getAbsoluteFilePath(baseFolder))));
        }

        public String getReference() {
            return property;
        }

        public boolean isBroken() {
            return broken;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append("BaseIncludePathSupport.Item[ type: ");
            sb.append(type.name());
            sb.append(", filePath: ");
            sb.append(filePath);
            sb.append(", property: ");
            sb.append(property);
            sb.append(", broken: ");
            sb.append(broken);
            sb.append(" ]");
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Item other = (Item) obj;
            if (broken != other.broken) {
                return false;
            }
            switch (getType()) {
                case CLASSPATH:
                    if (property != other.property && (property == null || !property.equals(other.property))) {
                        return false;
                    }
                    break;
                default:
                    if (filePath != other.filePath && (filePath == null || !filePath.equals(other.filePath))) {
                        return false;
                    }
                    break;
            }
            return true;
        }

        @Override
        public int hashCode() {
            if (broken) {
                return 42;
            }
            int hash = getType().ordinal();
            switch (getType()) {
                case CLASSPATH:
                    hash += property.hashCode();
                    break;
                default:
                    hash = 41 * hash + (filePath != null ? filePath.hashCode() : 0);
                    break;
            }
            return hash;
        }
    }

    /**
     * Converts the ant reference to the name of the referenced property.
     * @param ant reference
     * @param the name of the referenced property
     */
    public static String getAntPropertyName(String property) {
        if (property != null
                && property.startsWith("${") // NOI18N
                && property.endsWith("}")) { // NOI18N
            return property.substring(2, property.length() - 1);
        }
        return property;
    }
}
