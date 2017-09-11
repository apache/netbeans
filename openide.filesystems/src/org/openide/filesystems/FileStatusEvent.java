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

import java.util.Collections;
import java.util.EventObject;
import java.util.Set;

/** Event describing a change in annotation of files.
*
* @author Jaroslav Tulach
*/
public final class FileStatusEvent extends EventObject {
    static final long serialVersionUID = -6428208118782405291L;

    /** changed files */
    private Set<? extends FileObject> files;

    /** icon changed? */
    private boolean icon;

    /** name changed? */
    private boolean name;

    /** Creates new FileStatusEvent
    * @param fs filesystem that causes the event
    * @param files set of FileObjects that has been changed
    * @param icon has icon changed?
    * @param name has name changed?
    */
    public FileStatusEvent(FileSystem fs, Set<? extends FileObject> files, boolean icon, boolean name) {
        super(fs);
        this.files = files;
        this.icon = icon;
        this.name = name;
    }

    /** Creates new FileStatusEvent
    * @param fs filesystem that causes the event
    * @param file file object that has been changed
    * @param icon has icon changed?
    * @param name has name changed?
    */
    public FileStatusEvent(FileSystem fs, FileObject file, boolean icon, boolean name) {
        this(fs, Collections.singleton(file), icon, name);
    }

    /** Creates new FileStatusEvent. This does not specify the
    * file that changed annotation, assuming that everyone should update
    * its annotation. Please notice that this can be time consuming
    * and should be fired only when really necessary.
    *
    * @param fs filesystem that causes the event
    * @param icon has icon changed?
    * @param name has name changed?
    */
    public FileStatusEvent(FileSystem fs, boolean icon, boolean name) {
        this(fs, (Set<FileObject>) null, icon, name);
    }

    /** Getter for filesystem that caused the change.
    * @return filesystem
    */
    public FileSystem getFileSystem() {
        return (FileSystem) getSource();
    }

    /** Is the change change of name?
    */
    public boolean isNameChange() {
        return name;
    }

    /** Do the files changed their icons?
    */
    public boolean isIconChange() {
        return icon;
    }

    /** Check whether the given file has been changed.
    * @param file file to check
    * @return true if the file has been affected by the change
    */
    public boolean hasChanged(FileObject file) {
        if (files == null) {
            // all files on source filesystem are said to change
            try {
                return file.getFileSystem() == getSource();
            } catch (FileStateInvalidException ex) {
                // invalid files should not be changed
                return false;
            }
        } else {
            // specified set of files, so check it
            return files.contains(file);
        }
    }
}
