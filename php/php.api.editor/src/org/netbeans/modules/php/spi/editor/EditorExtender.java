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

package org.netbeans.modules.php.spi.editor;

import java.util.List;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.openide.filesystems.FileObject;

/**
 * SPI for extending PHP editor.
 * <p>
 * <i>All the methods are called only for the {@link FileObject}
 * that is currently opened in the editor.</i>
 * @author Tomas Mysik
 */
public abstract class EditorExtender {

    /**
     * Get the list of {@link PhpElement PHP elements} to be added to the code completion.
     * <p>
     * Future changes to be more general are probable.
     * @param fo {@link FileObject file object} in which the code completion is invoked
     * @return list of {@link PhpElement PHP elements} to be added to the code completion.
     */
    public abstract List<PhpBaseElement> getElementsForCodeCompletion(FileObject fo);
}
