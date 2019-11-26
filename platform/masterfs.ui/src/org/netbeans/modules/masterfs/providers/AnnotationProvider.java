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

package org.netbeans.modules.masterfs.providers;

import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** Can provide status and actions for FileObjects. Register it using {@link org.openide.util.lookup.ServiceProvider}.
 *
 * @author Jaroslav Tulach
 */
public abstract class AnnotationProvider extends BaseAnnotationProvider {
    /** Annotate the icon of a file cluster.
     * <p>Please do <em>not</em> modify the original; create a derivative icon image,
     * using a weak-reference cache if necessary.
    * @param icon the icon suggested by default
    * @param iconType an icon type from {@link java.beans.BeanInfo}
    * @param files an immutable set of {@link FileObject}s belonging to this filesystem
    * @return the annotated icon or null if some other provider shall anotate the icon
    */
    public abstract java.awt.Image annotateIcon(java.awt.Image icon, int iconType, Set<? extends FileObject> files);
    
    /** Provides actions that should be added to given set of files.
     * @param files an immutable set of {@link FileObject}s belonging to this filesystem
     * @return null or array of actions for these files.
     * @deprecated Will be deleted in the future. Overwrite {@link #findExtrasFor(java.util.Set)}.
     */
    @Deprecated
    public javax.swing.Action[] actions(Set<? extends FileObject> files) {
        return findExtrasFor(files).lookupAll(javax.swing.Action.class).toArray(new javax.swing.Action[0]);
    }
    
    /** Provides various (usually UI related) information about 
     * the given set of files.
     * 
     * @param files the files to 
     * @return lookup to be exposed as {@link FileSystem#findExtrasFor}
     *   - may return <code>null</code>
     * @since 2.48
     */
    @SuppressWarnings("deprecated")
    @Override
    public Lookup findExtrasFor(Set<? extends FileObject> files) {
        Object[] arr = actions(files);
        return arr == null ? null : Lookups.fixed(arr);
    }
}
