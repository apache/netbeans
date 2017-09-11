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


/** Event used to listen on filesystem attribute changes.
*
* @author Petr Hamernik
*/
public class FileAttributeEvent extends FileEvent {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8601944809928093922L;

    /** Name of attribute. */
    private String name;

    /** Old value of attribute */
    private Object oldValue;

    /** New value of attribute */
    private Object newValue;

    /** Creates new <code>FileAttributeEvent</code>. The <code>FileObject</code> where the action occurred
    * is assumed to be the same as the source object.
    * @param src source file which sent this event
    * @param name name of attribute, or <code>null</code> (since 1.33 only) if any attributes may have changed
    * @param oldValue old value of attribute, or <code>null</code> if the name is
    * @param newValue new value of attribute, or <code>null</code> if the name is
    */
    public FileAttributeEvent(FileObject src, String name, Object oldValue, Object newValue) {
        this(src, src, name, oldValue, newValue);
    }

    /** Creates new <code>FileAttributeEvent</code>.
    * @param src source file which sent this event
    * @param file file object where the action occurred
    * @param name name of attribute, or <code>null</code> (since 1.33 only) if any attributes may have changed
    * @param oldValue old value of attribute, or <code>null</code> if the name is
    * @param newValue new value of attribute, or <code>null</code> if the name is
    */
    public FileAttributeEvent(FileObject src, FileObject file, String name, Object oldValue, Object newValue) {
        this(src, file, name, oldValue, newValue, false);
    }

    /** Creates new <code>FileAttributeEvent</code>.
    * @param src source file which sent this event
    * @param file file object where the action occurred
    * @param name name of attribute, or <code>null</code> (since 1.33 only) if any attributes may have changed
    * @param oldValue old value of attribute, or <code>null</code> if the name is
    * @param newValue new value of attribute, or <code>null</code> if the name is
    * @param expected sets flag whether the value was expected
    */
    public FileAttributeEvent(
        FileObject src, FileObject file, String name, Object oldValue, Object newValue, boolean expected
    ) {
        super(src, file, expected);
        this.name = name;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /** Gets the name of the attribute.
    * @return Name of the attribute, or <code>null</code> (since 1.33 only) if an unknown attribute changed
    */
    public String getName() {
        return name;
    }

    /** Gets the old value of the attribute.
    * @return Old value of the attribute, or <code>null</code> if the name is
    */
    public Object getOldValue() {
        return oldValue;
    }

    /** Gets the new value of the attribute.
    * @return New value of the attribute, or <code>null</code> if the name is
    */
    public Object getNewValue() {
        return newValue;
    }

    @Override
    void insertIntoToString(StringBuilder b) {
        b.append(",name=");
        b.append(name);
        b.append(",oldValue=");
        b.append(oldValue);
        b.append(",newValue=");
        b.append(newValue);
    }

}
