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

package org.netbeans.spi.jumpto.symbol;

import javax.swing.Icon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.jumpto.support.Descriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * A SymbolDescriptor describes a symbol for display in the Go To Symbol dialog.
 * @since 1.7
 * @author Tomas Zezula
 */
public abstract class SymbolDescriptor extends Descriptor {

    private String matchedSymbolName;
    private SymbolProvider provider;

    /**
     * Return an icon that should be shown for this symbol descriptor. The icon
     * should give a visual indication of the type of match, e.g. method versus
     * field.  A default icon will be supplied if this method returns null.
     *
     * @return An Icon to be shown on the left hand side with the type entry
     */
    public abstract Icon getIcon();

    /**
     * Returns symbol display name
     * @return the symbol display name
     */
    public abstract String getSymbolName();

    /**
     * Returns display name of the owner in which the symbol is declared.
     * @return the owner display name
     */
    public abstract String getOwnerName();

    /**
     * Return the display name of the project owning the file containing the
     * symbol declaration.
     *
     * @return The display name of the project owning the file containing the
     * symbol declaration.
     */
    public abstract String getProjectName();

    /**
     * Return an icon that is applicable for the project owning the file containing the
     * symbol declaration.
     * Generally, this should be the same as the project icon.  This method will only
     * be called if {@link #getProjectName} returned a non-null value.
     *
     * @return A project icon corresponding to the project owning the file containing the
     * symbol declaration.
     */
    public abstract Icon getProjectIcon();

    /**
     * Return a FileObject for this symbol.
     * This will only be called when the dialog is opening the type or when
     * the user selects the file, so it does not have to be as fast as the other
     * descriptor attributes.
     *
     * @return The file object where the declared symbol
     */
    public abstract FileObject getFileObject();

    /**
     * Return the document offset corresponding to the symbol.
     * This will only be called when the dialog is opening the symbol, so
     * does not have to be as fast as the other descriptor attributes.
     *
     * @return The document offset of the type declaration in the declaration file
     */
    public abstract int getOffset();

    /**
     * Open the type declaration in the editor.
     */
    public abstract void open();

    /**
    * Returns a display name of the path to the file containing the symbol.
    * Default implementation uses {@code FileUtil.getFileDisplayName(getFileObject()) }
    * Could be overridden if a more efficient way could be provided.
    * Threading: This method is invoked in the EDT.
    *
    * @return The string representation of the path of the associated file.
    * @since 1.36
    */
    @NonNull
    public String getFileDisplayPath() {
       final FileObject fo = getFileObject();
       return fo == null ?
           "" : // NOI18N
           FileUtil.getFileDisplayName(fo);
    }

    /**
     * Returns the simple symbol name.
     * The simple symbol name is just a name without parameters or types.
     * @return the simple name or null
     * @since 1.48
     */
    @CheckForNull
    public String getSimpleName() {
        return null;
    }

    final void setHighlightText(@NonNull final String matchedSymbolName) {
        Parameters.notNull("matchedSymbolName", matchedSymbolName); //NOI18N
        this.matchedSymbolName = matchedSymbolName;
    }

    @NonNull
    final String getHighlightText() {
        return this.matchedSymbolName;
    }

    final void setSymbolProvider(@NonNull final SymbolProvider provider) {
        Parameters.notNull("provider", provider);   //NOI18N
        this.provider = provider;
    }

    @CheckForNull
    final SymbolProvider getSymbolProvider() {
        return this.provider;
    }
}
