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

package org.openide.loaders;

import java.io.*;
import java.util.List;
import org.netbeans.api.templates.FileBuilder;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;

/** Entry that works with plain files. Copies, moves,
* renames and deletes them without any modification.
*
* @author Jaroslav Tulach
*/
public class FileEntry extends MultiDataObject.Entry {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5972727204237511983L;

    /** Creates new file entry initially attached to a given file object.
    * @param obj the data object this entry belongs to
    * @param fo the file object for the entry
    */
    public FileEntry(MultiDataObject obj, FileObject fo) {
        obj.super (fo);
    }

    /* Makes a copy to given folder.
    * @param f the folder to copy to
    * @param suffix the suffix to add to the name of original file
    */
    public FileObject copy (FileObject f, String suffix) throws IOException {
        FileObject fo = getFile();
        String newName = fo.getName() + suffix;
        return fo.copy (f, newName, fo.getExt ());
    }

    @Override
    public FileObject copyRename(FileObject f, String name, String ext) throws IOException {
        FileObject fo = getFile();
        return fo.copy (f, name, ext);
    }
    
    /* Renames underlying fileobject. This implementation return the
    * same file.
    *
    * @param name new name
    * @return file object with renamed file
    */
    public FileObject rename (String name) throws IOException {
        boolean locked = isLocked ();
        
        FileLock lock = takeLock();
        try {
            getFile().rename(lock, name, getFile().getExt());
        } finally {
            if (!locked)
                lock.releaseLock();
        }
        return getFile ();
    }

    /* Moves file to another folder
    * @param f the folder
    * @param suffix the suffix to append to original name of the file
    * @return new file object for the file
    */
    public FileObject move (FileObject f, String suffix) throws IOException {
        boolean locked = isLocked ();
        
        FileObject fo = getFile();
        FileLock lock = takeLock ();
        try {
            String newName = fo.getName() + suffix;
            FileObject dest = fo.move (lock, f, newName, fo.getExt ());
            if (dest == null) {
                throw new IOException(fo + "move(" + lock + ", " + f + ", " + newName + ", " + fo.getExt() + " yields null!");
            }
            return dest;
        } finally {
            if (!locked)
                lock.releaseLock ();
        }
    }

    /* Deletes file object
    */
    public void delete () throws IOException {
        /* JST: This fixes bug 4660. But I am not sure whether this will not
        * create another or open some old bug.
            
            if (isLocked())
              throw new IOException(NbBundle.getBundle (FileEntry.class).getString ("EXC_SharedAccess"));
        */
        boolean locked = isLocked ();

        FileLock lock = takeLock();
        try {
            getFile().delete(lock);
        }
        finally {
            if (!locked)
                lock.releaseLock();
        }
    }

    /* Creates dataobject from template. Copies the file
    * @param f the folder to create instance in
    * @param name name of the file or null if it should be choosen automaticly
    */
    @Override
    public FileObject createFromTemplate (FileObject f, String name) throws IOException {
        FileObject fo = FileBuilder.createFromTemplate(getFile(), f, name, 
                DataObject.CreateAction.getCallParameters(name), 
                FileBuilder.Mode.COPY);
        // unmark template state
        DataObject.setTemplate (fo, false);

        return fo;
    }

    /** Specialized entry that simplifies substitution when a file entry
    * is created from template.
    * Subclasses must implement
    * {@link #createFormat} and return a valid text format that
    * will be used for converting the lines of the original file
    * to lines in the newly created one.
    */
    public abstract static class Format extends FileEntry {
        static final long serialVersionUID =8896750589709521197L;
        /** Create a new entry initially attached to a given file object.
        * @param obj the data object this entry belongs to
        * @param fo the file object for the entry
        */
        public Format (MultiDataObject obj, FileObject fo) {
            super (obj, fo);
        }

        /* Creates dataobject from template. Copies the file and applyes substitutions
        * provided by the createFormat method.
        *
        * @param f the folder to create instance in
        * @param name name of the file or null if it should be choosen automaticly
        */
        @Override
        @SuppressWarnings("AssignmentToMethodParameter")
        public FileObject createFromTemplate (FileObject f, String name) throws IOException {
            String ext = getFile ().getExt ();
            if (name == null) {
                name = FileUtil.findFreeFileName(
                           f,
                           getFile ().getName (), ext
                       );
            }
            java.text.Format frm = createFormat (f, name, ext);
            List<FileObject> fos = new FileBuilder(getFile(), f).name(name).
                    withParameters(DataObject.CreateAction.getCallParameters(name)).
                    useFormat(frm).build();
            FileObject fo = fos.get(0);
            // unmark template state
            DataObject.setTemplate (fo, false);

            return fo;
        }

        /** Provide a suitable format for
        * substitution of lines.
        *
        * @param target the target folder of the installation
        * @param n the name the file will have
        * @param e the extension the file will have
        * @return a format to use for formatting lines
        */
        protected abstract java.text.Format createFormat (FileObject target, String n, String e);

    }


    /** Simple file entry variant. It does nearly nothing.
    * When a file is copied, it does nothing. If it is moved
    * or renamed it deletes the file.
    * <P>
    * Useful for representing useless files.
    */
    public static final class Numb extends MultiDataObject.Entry {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -6572157492885890612L;

        /**
         * Create a dummy entry.
         * @param obj the data object this entry belongs to
         * @param fo the file object to create an entry for
         */
        public Numb (MultiDataObject obj, FileObject fo) {
            obj.super (fo);
        }
        
        /** Is not important at all.
        * @return false
        */
        public boolean isImportant () {
            return false;
        }

        /** Does nothing.
        * @param f ignored
        * @param suffix ignored
        * @return <code>null</code>
        */
        public FileObject copy (FileObject f, String suffix) {
            return null;
        }

        /** Removes file.
         * @param name ignored
        * @return <code>null</code>
         * @throws IOException in case of problem
        */
        public FileObject rename (String name) throws IOException {
            stdBehaving();
            return null;
        }

        /** Removes file.
         * @param f ignored
         * @param suffix ignored
        * @return <code>null</code>
         * @throws IOException in case of problem
        */
        public FileObject move (FileObject f, String suffix) throws IOException {
            stdBehaving();
            return null;
        }

        /** Removes file.
         * @throws IOException in case of problem
         */
        public void delete () throws IOException {
            stdBehaving();
        }

        /** Removes file.
         * @throws IOException in case of problem
         */
        private void stdBehaving () throws IOException {
            if (getFile() == null)
                return;

            if (isLocked())
                throw new IOException (NbBundle.getBundle (FileEntry.class).getString ("EXC_SharedAccess"));

            FileLock lock = takeLock();
            try {
                getFile().delete(lock);
            } finally {
                if (lock != null)
                    lock.releaseLock();
            }
        }

        /** Does nothing.
         * @param f ignored
         * @param name ignored
         * @return <code>null</code>
         */
        public FileObject createFromTemplate (FileObject f, String name) {
            return null;
        }
    }
    /** Simple entry for handling folders, on copy, move and createFromTemplate
     * it creates new empty folder and copies attributes of source folder.
     * Operation on children should be performed explicitly by DataObject 
     * using this entry.
     * @since 1.13
     */
    public static final class Folder extends MultiDataObject.Entry {

        /** Creates new FolderEntry */
        public Folder (MultiDataObject obj, FileObject fo) {
            obj.super (fo);
        }

        /** Creates new folder and copies attributes.
         * @param f the folder to create this entry in
         * @param suffix suffix appended to the new name to use
         * @return the copied <code>FileObject</code> or <code>null</code> if it cannot be copied
         * @exception IOException when the operation fails
         */
        public FileObject copy (FileObject f, String suffix) throws IOException {
            String add = suffix + ((getFile ().getExt ().length () > 0) ? "." + getFile ().getExt () : "");

            FileObject fo = FileUtil.createFolder (f, getFile ().getName () + add);
            FileUtil.copyAttributes (getFile (), fo);

            return fo;
        }

        /** Nearly the same like {@link #copy (FileObject f, String suffix)}.
         * @param f the folder to move this entry to
         * @param suffix suffix appended to the new name to use
         * @return the moved <code>FileObject</code> or <code>null</code> if it has been deleted
         * @exception IOException when the operation fails
         */
        public FileObject move (FileObject f, String suffix) throws IOException {
            return copy (f, suffix);
        }

        /** Creates new folder and copies attributes, the template flag is cleared.
         * @param f the folder to create this entry in
         * @param name the new name to use
         * @return the copied <code>FileObject</code> or <code>null</code> if it cannot be copied
         * @exception IOException when the operation fails
         */
        public FileObject createFromTemplate (FileObject f, String name) throws IOException {
            if (name == null) {
                name = FileUtil.findFreeFileName(
                           f,
                           getFile ().getName (), getFile ().getExt ()
                       );
            }
            FileObject fo = FileUtil.createFolder (f, name);

            FileUtil.copyAttributes (getFile (), fo, (n, v) -> DataObject.PROP_TEMPLATE.equals(n) ? null : FileUtil.defaultAttributesTransformer().apply(n, v));
            return fo;
        }

        /** Renames folder.
         * @param name the new name
         * @return the renamed <code>FileObject</code> or <code>null</code> if it has been deleted
         * @exception IOException when the operation fails
         */
        public FileObject rename (String name) throws IOException {
            boolean locked = isLocked ();
            FileLock lock = takeLock ();
            try {
                getFile ().rename (lock, name, null);
            } finally {
                if (!locked)
                    lock.releaseLock ();
            }
            return getFile ();
        }

        /** Deletes folder associated with entry. Although filesystems delete
         * folders recursively, it is better to delete children DataObjects before
         * the {@link #FileEntry.Folder} entry is deleted.
         * @exception IOException when the operation fails
         */
        public void delete () throws IOException {
            boolean locked = isLocked ();
            FileLock lock = takeLock ();
            try {
                getFile ().delete (lock);
            } finally {
                if (!locked)
                    lock.releaseLock();
            }
        }

    }
}
