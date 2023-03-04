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
package org.netbeans.modules.xml;

import java.io.*;
import java.util.*;
import java.text.DateFormat;

import org.netbeans.modules.xml.util.Util;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.*;


/** The DataLoader for XMLDataObjects.
 * This class is final only for performance reasons,
 * can be happily unfinaled if desired.
 */
public class XMLDataLoader extends UniFileLoader {
    // UID of F4J 1.0 build 501
    private static final long serialVersionUID = 3824119075670384804L;

    /** Extension constants */
    private static final String XML_EXT = "xml"; // NOI18N
    private static final String XMLINFO_EXT = "xmlinfo"; // NOI18N

    /** Creates a new XMLDataLoader */
    public XMLDataLoader () {
        super ("org.netbeans.modules.xml.XMLDataObject"); // NOI18N
    }

    /** Initialize XMLDataLoader: name, actions, ...
     */
    @Override
    protected void initialize () {
        super.initialize();
        
        ExtensionList ext = getExtensions();
        ext.addExtension (XML_EXT);
        ext.addMimeType (XMLKit.MIME_TYPE);
        ext.addMimeType ("application/xml"); // http://www.ietf.org/rfc/rfc3023.txt // NOI18N
//         ext.addMimeType (org.netbeans.modules.xml.XMLDataObject.XSLT_MIME_TYPE);
        setExtensions (ext);
    }    
     
    @Override
    protected String actionsContext() {
        return "Loaders/text/xml-mime/Actions/";
    }

    /**
     * Lazy init name.
     */
    @Override
    protected String defaultDisplayName () {
        return Util.THIS.getString (XMLDataLoader.class, "PROP_XmlLoader_Name");        
    }
    
    /** For a given file finds a primary file.
     * @param fo the file to find primary file for
     *
     * @return the primary file for the file or null if the file is not
     *   recognized by this loader
     */
    @Override
    protected FileObject findPrimaryFile (FileObject fo) {
        if (fo.isFolder()) {
            return null;
        }

        FileObject res = null;
        if ( super.findPrimaryFile (fo) != null ) {
            res = fo;
        } else if ( XMLINFO_EXT.equals (fo.getExt()) ) {
            res = FileUtil.findBrother (fo, XML_EXT);
        } else if ( fo.getMIMEType().endsWith ("+xml") ) { // NOI18N
            // recognize all XML flavours
            res = fo;
        }

        // give up for files on system file system that don't prefer us
        try {
            if ( ( res != null ) &&
                 ( res.getFileSystem().isDefault() == true ) ) { // system file system
                if ( ( DataLoaderPool.getPreferredLoader (res) != this ) && // the preferred DataLoader is not this loader
                     ( isTemplate (res) == false ) ) { // it is not template
                    res = null;
                }
            }
        } catch (FileStateInvalidException ex) {
            // ok, go on
        }
        
        return res;
    }

    
    /** Get the template status of this data object.
     * @return <code>true</code> if it is a template
     */
    private static boolean isTemplate (FileObject fo) {
        Object o = fo.getAttribute (DataObject.PROP_TEMPLATE);
        boolean ret = false;
        if ( o instanceof Boolean ) {
            ret = ((Boolean) o).booleanValue();
        }
        return ret;
    }


    /** Creates the right data object for given primary file.
     * It is guaranteed that the provided file is realy primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException {
        try {
//            System.err.println("Creating XML DO" + primaryFile);            
            return new org.netbeans.modules.xml.XMLDataObject (primaryFile, this);
        } catch (org.openide.loaders.DataObjectExistsException ex) {
//            System.err.println("Existing data object " + ex.getDataObject());
//            ex.printStackTrace();
            throw ex;
        }
    }

    /** Creates the right primary entry for given primary file.
     *
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     */
    @Override
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {            
        return new XMLFileEntry (obj, primaryFile);
    }

    /** Creates right secondary entry for given file. The file is said to
     * belong to an object created by this loader.
     *
     * @param secondaryFile secondary file for which we want to create entry
     * @return the entry
     */
    @Override
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
        return new FileEntry (obj, secondaryFile);
    }


    /** This entry defines the format for replacing the text during
     * instantiation the data object.
     */
    public static class XMLFileEntry extends FileEntry.Format {

        /** Serial Version UID */
        private static final long serialVersionUID = -7300320795693949470L;
        
        
        /**
         * If true, the Entry refuses to open InputStream to prevent races
         * between readers and attempts to delete the file.
         */
        boolean disableInputStream;
        
        /**
         * Holds a collection of readers that read the file.
         */
        private Collection<InputStream>  activeReaders;
        
        /** Creates new MakefileFileEntry */
        public XMLFileEntry (MultiDataObject obj, FileObject file) {
            super (obj, file);
        }

        /** Method to provide suitable format for substitution of lines.
         *
         * @param target the target folder of the installation
         * @param name the name the file will have
         * @param ext the extension the file will have
         * @return format to use for formating lines
         */
        protected java.text.Format createFormat (FileObject target, String name, String ext) {
            HashMap<String, String> map = new HashMap<String, String>();
            Date now = new Date();

            map.put ("NAME", name + "." + ext); // NOI18N

            //??? find replacement (inline TAX code)
            map.put ("ROOT", "root");  // NOI18N
//            map.put ("ROOT", name);
//            try {
//                TreeName tn = new TreeName (name);
//                map.put ("ROOT", TreeUtilities.isValidElementTagName(tn) ? name : "root");  //NOI18N
//            } catch (InvalidArgumentException ex) {
//                map.put ("ROOT", "root");  //NOI18N
//            }

            map.put ("DATE", DateFormat.getDateInstance (DateFormat.LONG).format (now)); // NOI18N
            map.put ("TIME", DateFormat.getTimeInstance (DateFormat.SHORT).format (now)); // NOI18N
            map.put ("USER", System.getProperty ("user.name")); // NOI18N

            MapFormat format = new MapFormat (map);
            format.setLeftBrace ("__"); // NOI18N
            format.setRightBrace ("__"); // NOI18N
            format.setExactMatch (false);
            return format;
        }
        
        /*### 
          this code is copied from FileEntry 
          TT EXPECTS that all XML templates are encoded as UTF-8
          FileEntry implementation used just platform default encoding
        ###*/

        
        /* Creates dataobject from template. Copies the file and applyes substitutions
        * provided by the createFormat method.
        *
        * @param f the folder to create instance in
        * @param name name of the file or null if it should be choosen automaticly
        */
        public FileObject createFromTemplate (FileObject f, String name) throws IOException {
            String ext = getFile ().getExt ();

            if (name == null) {
                name = FileUtil.findFreeFileName(
                           f,
                           getFile ().getName (), ext
                       );
            }
            FileObject fo = f.createData (name, ext);

            java.text.Format frm = createFormat (f, name, ext);

            BufferedReader r = new BufferedReader (new InputStreamReader (getFile ().getInputStream (), "UTF8")); // NOI18N
            try {
                FileLock lock = fo.lock ();
                try {
                    BufferedWriter w = new BufferedWriter (new OutputStreamWriter (fo.getOutputStream (lock), "UTF8")); // NOI18N

                    try {
                        String line = null;
                        String current;
                        while ((current = r.readLine ()) != null) {
                            line = frm.format (current);
                            w.write (line);
                            w.newLine ();                            
                        }
                    } finally {
                        w.close ();
                    }
                } finally {
                    lock.releaseLock ();
                }
            } finally {
                r.close ();
            }

            // copy attributes
            FileUtil.copyAttributes (getFile (), fo, (n, v) -> DataObject.PROP_TEMPLATE.equals(n) ? null : FileUtil.defaultAttributesTransformer().apply(n, v));
            return fo;
        }

        //!!!
        // bloody OpemIDE why must every one copy paste this code?

        private synchronized void addReader(InputStream r) {
            if (activeReaders == null) {
                activeReaders = new LinkedList<>();
            }
            activeReaders.add(r);
        }

        private synchronized void removeReader(InputStream r) {
            if (activeReaders == null)
                return;
            activeReaders.remove(r);
        }

        @Override
        public void delete() throws IOException {
            synchronized (this) {
                if (activeReaders != null && activeReaders.size() > 0) {
                    for (Iterator<InputStream> it = activeReaders.iterator(); it.hasNext(); ) {
                        InputStream r = it.next();
                        r.close();
                        it.remove();
                    }
                }
                activeReaders = null;
                disableInputStream = true;
            }
            super.delete();
        }

        public InputStream getInputStream() throws FileNotFoundException {
            FileObject fob = getFile();
            synchronized (this) {
                if (disableInputStream) {
                    // refuse to create the stream.
                    throw new FileNotFoundException("File is being deleted."); // NOI18N
                }
                InputStream s = new NotifyInputStream(fob.getInputStream());
                addReader(s);
                return s;
            }
        }

        private class NotifyInputStream extends FilterInputStream {
            public NotifyInputStream(InputStream is) {
                super(is);
            }

            @Override
            public void close() throws IOException {
                super.close();
                removeReader(this);
            }
        }

        // debug taking locks
        @Override
        public FileLock takeLock() throws IOException {
            FileLock lock = super.takeLock();
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("XMLDataLoader.XMLEntry.takeLock()/" + getFile() + "=" + lock); // NOI18N

            return lock;
        }

    }

}
