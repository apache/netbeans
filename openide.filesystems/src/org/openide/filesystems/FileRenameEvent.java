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


/** Event indicating a file rename.
*
* @author Petr Hamernik
*/
public class FileRenameEvent extends FileEvent {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = -3947658371806653711L;

    /** Original name of the file. */
    private String name;

    /** Original extension of the file. */
    private String ext;

    /** Creates new <code>FileRenameEvent</code>. The <code>FileObject</code> where the action took place
    * is assumed to be the same as the source object.
    * @param src source file which sent this event
    * @param name original file name
    * @param ext original file extension
    */
    public FileRenameEvent(FileObject src, String name, String ext) {
        this(src, src, name, ext);
    }

    /** Creates new <code>FileRenameEvent</code>, specifying an event location.
    * @param src source file which sent this event
    * @param file file object where the action took place
    * @param name original file name
    * @param ext original file extension
    */
    public FileRenameEvent(FileObject src, FileObject file, String name, String ext) {
        this(src, file, name, ext, false);
    }

    /** Creates new <code>FileRenameEvent</code>, specifying an event location
    * and whether the event was expected by the system.
    * @param src source file which sent this event
    * @param file file object where the action took place
    * @param name original file name
    * @param ext original file extension
    * @param expected whether the value was expected
    */
    public FileRenameEvent(FileObject src, FileObject file, String name, String ext, boolean expected) {
        super(src, file, expected);
        this.name = name;
        this.ext = ext;
    }

    /** Get original name of the file.
    * @return old name of the file
    */
    public String getName() {
        return name;
    }

    /** Get original extension of the file.
    * @return old extension of the file
    */
    public String getExt() {
        return ext;
    }

    @Override
    void insertIntoToString(StringBuilder b) {
        b.append(",name.ext=");
        b.append(name);
        b.append('.');
        b.append(ext);
    }

}
