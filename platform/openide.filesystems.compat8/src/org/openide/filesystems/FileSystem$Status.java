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
package org.openide.filesystems;

import java.awt.Image;
import java.util.Set;

/**
 * Allows a filesystem to annotate a group of files (typically comprising a data
 * object) with additional markers.
 * <p>
 * This could be useful, for example, for a filesystem supporting version
 * control. It could annotate names and icons of data nodes according to whether
 * the files were current, locked, etc.
 * <p/>
 * Formerly part of openide.filesystems module, replaced by {@link StatusDecorator}
 */
public interface FileSystem$Status {
    /**
     * Annotate the name of a file cluster.
     *
     * @param name the name suggested by default
     * @param files an immutable set of {@link FileObject}s belonging to this
     * filesystem
     * @return the annotated name (may be the same as the passed-in name)
     * @exception ClassCastException if the files in the set are not of valid
     * types
     */
    public String annotateName(String name, Set<? extends FileObject> files);

    /**
     * Annotate the icon of a file cluster.
     * <p>
     * Please do <em>not</em> modify the original; create a derivative icon
     * image, using a weak-reference cache if necessary.
     *
     * @param icon the icon suggested by default
     * @param iconType an icon type from {@link java.beans.BeanInfo}
     * @param files an immutable set of {@link FileObject}s belonging to this
     * filesystem
     * @return the annotated icon (may be the same as the passed-in icon)
     * @exception ClassCastException if the files in the set are not of valid
     * types
     */
    public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files);
}
