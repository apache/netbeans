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
package org.netbeans.modules.xml.lib;

import org.netbeans.modules.xml.util.Util;
import java.io.IOException;

import org.openide.loaders.DataObject;
import org.openide.filesystems.*;

/**
 * Main purpose it to unify handling of FileObject's isVirtual(), isValid(),
 * canRead() and canWrite() aspects.
 *
 * @author  Libor Kramolis
 * @author  Petr Kuzel, added support for empty extensions
 * @version 0.1
 */
public final class FileUtilities {

    /*
     * Create data object in given folder. Consider calling it from FS atomic action
     * as empty files may confuse loaders.
     * @return DataObject or null if the DataObject exist and should not be overwritten
     */
    public static DataObject createDataObject (final FileObject folder, final String name, final String ext, final boolean overwrite) throws IOException {
        DataObject dataObject = null;
        String normalized = ext == null ? "" : "".equals(ext) ? "" : "." + ext;  // NOI18N
        FileObject fileObject = createFileObject (folder, name, normalized, overwrite, false, true);
        if ( fileObject != null ) {
            dataObject = DataObject.find (fileObject);
        }
        return dataObject;
    }

    /**
     * Create DataObject at relative path to given folder. Consider calling it from FS atomic action
     * as empty files may confuse loaders.
     * @return DataObject or null if the DataObject exist and should not be overwritten
     */
    public static FileObject createFileObject (FileObject folder, String nameExt, boolean overwrite) throws IOException {
        // eliminate relative path
        
        while ( nameExt.startsWith ("../") ) {  // NOI18N
            nameExt = nameExt.substring (3);
            if ( folder.isRoot() == false ) {
                folder = folder.getParent();
            }
        }

        FileObject fo = null;

//         if ( ( overwrite == true ) ||
//              ( GuiUtil.confirmAction (Util.THIS.getString ("PROP_replaceMsg", nameExt)) ) ) {
//             fo = FileUtil.createData (folder, nameExt);
//         }

        String name, ext;
        int dotIndex = nameExt.lastIndexOf ('.');
        int slashIndex = nameExt.lastIndexOf ('/');
        if (dotIndex != -1 && dotIndex > slashIndex) {
            name  = nameExt.substring (0, dotIndex);
            ext   = "." + nameExt.substring (dotIndex + 1); // NOI18N
        } else {
            name = nameExt;
            ext = ""; // NOI18N
        }

        fo = createFileObject (folder, name, ext, overwrite, true, false);

        return fo;
    }


//     /*
//      * @return FileObject or null if the FileObject exist and should not be overwritten
//      */
//     public static FileObject createFileObject (final FileObject folder, String nameExt, final boolean overwrite) throws IOException {
//         int dotIndex = nameExt.lastIndexOf ('.');

//         String name = nameExt.substring (0, dotIndex);
//         String ext = nameExt.substring (dotIndex + 1);

//         return createFileObject (folder, name, ext, overwrite, true, false);
//     }

    /*
     * @return FileObject or null if the FileObject exist and should not be overwritten
     */
    private static FileObject createFileObject (final FileObject folder, final String name, final String ext, final boolean overwrite, boolean askForOverwrite, final boolean makeCopy) throws IOException {
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("[FileUtilities.createFileObject]"); // NOI18N
            Util.THIS.debug ("    folder = " + folder);  // NOI18N
            Util.THIS.debug ("    name   = " + name); // NOI18N
            Util.THIS.debug ("    .ext   = " + ext); //NOI18N
        }

        FileObject file = folder.getFileObject (name + ext);
        
        if (file == null) { // new one
            
            file = FileUtil.createData (folder, name + ext);
            
        } else if ( file.isVirtual() || overwrite) {
            // isVirtual:
            //     FileObject represents virtual file (not available),
            //     so it is important to delete such virtual file
            //     and create real one.
            // overwrite:
            //     make backup of original file
                        
            FileSystem fs = folder.getFileSystem();
            final FileObject tempFile = file;

            fs.runAtomicAction (new FileSystem.AtomicAction () {
                public void run () throws IOException {

                    if ( ( makeCopy == true ) &&
                         ( overwrite == true ) &&
                         ( tempFile.isVirtual() == false ) ) {
                        // Make copy of original not virtual file

                        for (int i = 1; true; i++) {
                            if (folder.getFileObject(name + ext + i) == null) {
                                tempFile.copy (folder, name + ext + i, "");  // NOI18N
                                break;
                            }
                        }
                    }
                    
                    if ( ( makeCopy == false ) &&
                         ( tempFile.isVirtual() ) ) { // do not create new file object
                        tempFile.delete();
                        FileUtil.createData (folder, name +  ext);
                    }
                }
            });

            file = folder.getFileObject (name + ext);
        } else if ( askForOverwrite ) {

            if (!!! GuiUtil.confirmAction (Util.THIS.getString (
                    FileUtilities.class, "PROP_replaceMsg", name + ext) ) ) {
                file = null;
            }

        } else {
            file = null;
        }
        
        return file;
    }

}
