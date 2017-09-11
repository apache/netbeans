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

package org.openide.filesystems;

import java.util.EventListener;

/** Listener for changes in <code>FileObject</code>s. Can be attached to any <code>FileObject</code>.
* <P>
* When attached to a file it listens for file changes (due to saving from inside NetBeans) and
* for deletes and renames.
* <P>
* When attached to a folder it listens for all actions taken on this folder.
* These include any modifications of data files or folders,
* and creation of new data files or folders.
*
* @see FileObject#addFileChangeListener
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public interface FileChangeListener extends EventListener {
    /** Fired when a new folder is created. This action can only be
     * listened to in folders containing the created folder up to the root of
     * filesystem.
      *
     * @param fe the event describing context where action has taken place
     */
    public abstract void fileFolderCreated(FileEvent fe);

    /** Fired when a new file is created. This action can only be
    * listened in folders containing the created file up to the root of
    * filesystem.
    *
    * @param fe the event describing context where action has taken place
    */
    public abstract void fileDataCreated(FileEvent fe);

    /** Fired when a file is changed.
    * @param fe the event describing context where action has taken place
    */
    public abstract void fileChanged(FileEvent fe);

    /** Fired when a file is deleted.
    * @param fe the event describing context where action has taken place
    */
    public abstract void fileDeleted(FileEvent fe);

    /** Fired when a file is renamed.
    * @param fe the event describing context where action has taken place
    *           and the original name and extension.
    */
    public abstract void fileRenamed(FileRenameEvent fe);

    /** Fired when a file attribute is changed.
    * @param fe the event describing context where action has taken place,
    *           the name of attribute and the old and new values.
    */
    public abstract void fileAttributeChanged(FileAttributeEvent fe);
}
