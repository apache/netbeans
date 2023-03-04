/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
