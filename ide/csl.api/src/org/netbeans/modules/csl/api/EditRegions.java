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

package org.netbeans.modules.csl.api;

import java.util.Set;
import javax.swing.text.BadLocationException;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.editor.EditRegionsImpl;
import org.openide.filesystems.FileObject;


/**
 * Provide access to initiate editing of synchronized regions in a document
 * for a file.
 *
 * @author <a href="mailto:tor.norbye@sun.com">Tor Norbye</a>
 */
public abstract class EditRegions {
    /**
     * Initiate editing. The document for the file must already been open in the editor.
     * @param file The file whose document we want to edit
     * @param regions The set of ranges in the document that we want to edit
     * @param caretOffset The initial location for the caret (which MUST be within
     *   one of the regions)
     */
    public abstract void edit (@NonNull FileObject file, @NonNull Set<OffsetRange> regions, int caretOffset) throws BadLocationException;

    private static EditRegions      editRegions;

    @NonNull
    public static synchronized EditRegions getInstance () {
        if (editRegions == null) {
            editRegions = new EditRegionsImpl ();
        }
        return editRegions;
    }
}
