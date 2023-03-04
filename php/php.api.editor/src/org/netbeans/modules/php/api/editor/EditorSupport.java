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

package org.netbeans.modules.php.api.editor;

import java.util.Collection;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 * Helper editor class that can be found in the default lookup.
 * @author Tomas Mysik
 */
public interface EditorSupport {

    /**
     * Get {@link PhpType PHP types} from the given {@link FileObject file object}.
     * @param fo {@link FileObject file object} source file to investigate
     * @return collection of {@link PhpType PHP types}, never <code>null</code>
     * @since 0.28
     */
    Collection<PhpType> getTypes(FileObject fo);

    /**
     * Get {@link PhpClass PHP classes} from the given {@link FileObject file object}.
     * @param fo {@link FileObject file object} source file to investigate
     * @return collection of {@link PhpClass PHP classes}, never <code>null</code>
     */
    Collection<PhpClass> getClasses(FileObject fo);

    /**
     * Collects files (with offsets) containg the given {@link PhpClass PHP class}.
     * @param sourceRoot directory representing source root or test root
     * @param phpClass {@link PhpClass PHP class} to search for
     * @return collection of {@link FileObject file objects} (with offsets) containing the {@link PhpClass PHP class}, never <code>null</code>
     * @see #getClasses(FileObject)
     */
    Collection<Pair<FileObject, Integer>> filesForClass(FileObject sourceRoot, PhpClass phpClass);

    /**
     * Get {@link PhpElement PHP element} for the given file and offset.
     * @param fo file to search in
     * @param offset offset in the file
     * @return {@link PhpElement PHP element}, can be <code>null</code> if not in any
     */
    PhpBaseElement getElement(FileObject fo, int offset);
}
