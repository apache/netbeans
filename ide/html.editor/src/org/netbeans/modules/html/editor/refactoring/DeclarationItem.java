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

package org.netbeans.modules.html.editor.refactoring;

import javax.swing.text.Document;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.css.refactoring.api.EntryHandle;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class DeclarationItem {

    private EntryHandle declaration;
    private FileObject source;

    DeclarationItem(EntryHandle declaration, FileObject source) {
        this.declaration = declaration;
        this.source = source;
    }

    public EntryHandle getDeclaration() {
        return declaration;
    }

    public FileObject getSource() {
        return source;
    }

    public Document getDocument() {
        return GsfUtilities.getDocument(source, true);
    }

    @Override
    public String toString() {
        return "DeclarationItem[entry=" + getDeclaration() +
                ", source=" + (getSource() != null ? getSource().getPath() : "no source") +
                "]"; //NOI18N
    }



}
