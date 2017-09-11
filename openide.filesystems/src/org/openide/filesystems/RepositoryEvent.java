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


/** Event describing adding a filesystem to, or removing a filesystem from, the filesystem pool.
*
* @author Jaroslav Tulach
* @version 0.10 November 4, 1997
*/
public class RepositoryEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5466690014963965717L;

    /** the modifying filesystem */
    private FileSystem fileSystem;

    /** added or removed */
    private boolean add;

    /** Create a new filesystem pool event.
    * @param fsp filesystem pool that is being modified
    * @param fs filesystem that is either being added or removed
    * @param add <CODE>true</CODE> if the filesystem is added,
    *    <CODE>false</CODE> if removed
    */
    public RepositoryEvent(Repository fsp, FileSystem fs, boolean add) {
        super(fsp);
        this.fileSystem = fs;
        this.add = add;
    }

    /** Getter for the filesystem pool that is modified.
    * @return the filesystem pool
    */
    public Repository getRepository() {
        return (Repository) getSource();
    }

    /** Getter for the filesystem that is added or removed.
    * @return the filesystem
    */
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /** Is the filesystem added or removed?
    * @return <CODE>true</CODE> if the filesystem is added, <code>false</code> if removed
    */
    public boolean isAdded() {
        return add;
    }
}
