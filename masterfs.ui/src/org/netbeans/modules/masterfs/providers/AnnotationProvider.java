/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        return arr == null ? null : Lookups.fixed((Object[]) arr);
    }
}
