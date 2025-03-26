/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.spi.jumpto.type;

import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.jumpto.support.Descriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * A TypeDescriptor describes a type for display in the Go To Type dialog.
 * Items should be comparable such that the infrastructure can return these.
 * 
 * @author Tor Norbye
 * @author Tomas Zezula
 */
public abstract class TypeDescriptor extends Descriptor {

    private String highlightText;

    /**
     * Return the simple name of the type (not including qualifiers). The entries
     * will typically be sorted by this key.
     * 
     * @return The name of this type, e.g. for java.util.List it would be "List"
     */
    public abstract String getSimpleName();

    /**
     * <p>Return the "outer" name of the type, if any. For Java for example, this would be
     * the outer class if this type is an inner class.</p>
     * <p>Do not confuse with {@link #getContextName}!</p> 
     * 
     * @return The name of the outer class of this type, if any, otherwise return null
     */
    public abstract String getOuterName();

    /**
     * Return the name of this type, along with the outer name. This might
     * for example be "Entry in Map" for java.util.Map.Entry
     * 
     * @return The outer and inner name of this type, e.g. for java.util.Map.Entry it would be "Entry in Map"
     */
    public abstract String getTypeName();
     
    /**
     * Provide additional context for the type name. This would typically be
     * the fully qualified name, minus the name part. Return null if there is
     * no applicable context. For example, "java.util.List" would return "java.util"
     * here.
     * 
     * @return A description of the context of the type, such as the fully qualified name
     *   minus the name part
     */
    public abstract String getContextName();

    /** 
     * Return an icon that should be shown for this type descriptor. The icon
     * should give a visual indication of the type of match, e.g. class versus
     * module.  A default icon will be supplied if this method returns null.
     * 
     * @return An Icon to be shown on the left hand side with the type entry
     */
     public abstract Icon getIcon();
     
    /**
     * Return the display name of the project containing this type (if any).
     * 
     * @return The display name of the project containing the type declaration
     */
    public abstract String getProjectName();
     
    /**
     * Return an icon that is applicable for the project defining the type.
     * Generally, this should be the same as the project icon.  This method will only
     * be calld if {@link #getProjectName} returned a non-null value.
     * 
     * @return A project icon corresponding to the project defining this type
     */
    public abstract Icon getProjectIcon();
     
    /**
     * Return a FileObject for this type.
     * This will only be called when the dialog is opening the type or when
     * the user selects the file, so it does not have to be as fast as the other
     * descriptor attributes.
     * 
     * @return The file object where the type is defined
     */
    public abstract FileObject getFileObject();

    /**
     * Return the document offset corresponding to the type.
     * This will only be called when the dialog is opening the type, so
     * does not have to be as fast as the other descriptor attributes.
     * 
     * XXX This method is intended to replace the open() call below.
     *
     * @return The document offset of the type declaration in the declaration file
     */
    public abstract int getOffset();
     
    /**
     * Open the type declaration in the editor. 
     * XXX Should we nuke this method and only have type declarations return
     *   their offsets? I looked at the Java implementation and it's leveraging 
     *   some utility methods to open the type declaration; I have similar methods
     *   in Ruby. It might be more convenient
     */
    public abstract void open();

   /**
    * Returns a display name of the path to the file containing the type.
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

    @NonNull
    final String getHighlightText() {
        return highlightText;
    }

    final void setHighlightText(@NonNull final String textToHighlight) {
        Parameters.notNull("textToHighlight", textToHighlight); //NOI18N
        this.highlightText = textToHighlight;
    }
}
