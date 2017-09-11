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

package org.openide.loaders;

import java.util.EventObject;

import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;

/** Event that describes operations taken on
* a data object.
*
* @author Jaroslav Tulach
*/
public class OperationEvent extends EventObject {
    /** package private numbering of methods */
    static final int COPY = 1, MOVE = 2, DELETE = 3, RENAME = 4, SHADOW = 5, TEMPL = 6, CREATE = 7;

    /** data object */
    private DataObject obj;
    private static final DataLoaderPool pl = DataLoaderPool.getDefault();
    static final long serialVersionUID =-3884037468317843808L;
    OperationEvent(DataObject obj) {
        super (pl);
        this.obj = obj;
    }

    /** Get the data object that has been modified.
    * @return the data object
    */
    public DataObject getObject () {
        return obj;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append(":");
        sb.append(" for ");
        sb.append(obj);
        writeDebug(sb);
        return sb.toString();
    }

    /** For subclasses in this package to write debug info */
    void writeDebug(StringBuffer sb) {
    }

    /** Notification of a rename of a data object.
    */
    public static final class Rename extends OperationEvent {
        /** name */
        private String name;

        static final long serialVersionUID =-1584168503454848519L;
        /** @param obj renamed object
        * @param name original name
        */
        Rename (DataObject obj, String name) {
            super (obj);
            this.name = name;
        }

        /** Get the old name of the object.
         * @return the old name
        */
        public String getOriginalName () {
            return name;
        }

        final void writeDebug(StringBuffer sb) {
            sb.append(" originalname: ");
            sb.append(name);
        }
    }

    /** Notification of a move of a data object.
    */
    public static final class Move extends OperationEvent {
        /** original file */
        private FileObject file;

        static final long serialVersionUID =-7753279728025703632L;
        /** @param obj renamed object
        * @param file original primary file
        */
        Move (DataObject obj, FileObject file) {
            super (obj);
            this.file = file;
        }

        /** Get the original primary file.
        * @return the file
        */
        public FileObject getOriginalPrimaryFile () {
            return file;
        }
        
        final void writeDebug(StringBuffer sb) {
            sb.append(" originalfile: ");
            sb.append(file);
        }
    }

    /** Notification of a copy action of a data object, creation of a shadow,
    * or creation from a template.
    */
    public static final class Copy extends OperationEvent {
        /** original data object */
        private DataObject orig;

        static final long serialVersionUID =-2768331988864546290L;
        /** @param obj renamed object
        * @param orig original object
        */
        Copy (DataObject obj, DataObject orig) {
            super (obj);
            this.orig = orig;
        }


        /** Get the original data object.
        * @return the data object
        */
        public DataObject getOriginalDataObject () {
            return orig;
        }
        
        
        final void writeDebug(StringBuffer sb) {
            sb.append(" originalobj: ");
            sb.append(orig);
        }
    }
}
