/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.filesystems;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;
import org.openide.util.BaseUtilities;
import org.openide.util.io.NbMarshalledObject;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** Implementation of <code>AbstractFileSystem.Attr</code> using a special file
 * in each folder for holding attributes.
 * It needs to hide
 * the file from the rest of system, so it also implements
 * <code>AbstractFileSystem.List</code> to exclude the file from the children list
 * (it can then serve to filter a plain list implementation).
 *
 *Description of format of special file ilustrates best DTD file that is showed in next lines:
 * <pre>{@code 
 * <!ELEMENT attributes (fileobject)*>
 * <!ATTLIST attributes version CDATA #REQUIRED>
 * <!ELEMENT fileobject (attr)*>
 * <!ATTLIST fileobject name CDATA #REQUIRED>
 * <!ELEMENT attr EMPTY>
 * <!ATTLIST attr name CDATA #REQUIRED>
 * <!ATTLIST attr bytevalue CDATA #IMPLIED>
 * <!ATTLIST attr shortvalue CDATA #IMPLIED>
 * <!ATTLIST attr intvalue CDATA #IMPLIED>
 * <!ATTLIST attr longvalue CDATA #IMPLIED>
 * <!ATTLIST attr floatvalue CDATA #IMPLIED>
 * <!ATTLIST attr doublevalue CDATA #IMPLIED>
 * <!ATTLIST attr boolvalue CDATA #IMPLIED>
 * <!ATTLIST attr charvalue CDATA #IMPLIED>
 * <!ATTLIST attr stringvalue CDATA #IMPLIED>
 * <!ATTLIST attr methodvalue CDATA #IMPLIED>
 * <!ATTLIST attr serialvalue CDATA #IMPLIED>
 * <!ATTLIST attr urlvalue CDATA #IMPLIED>
 * }
 * </pre>
 * @author Jaroslav Tulach
 */
@SuppressWarnings("unchecked")
public class DefaultAttributes extends Object implements AbstractFileSystem.Attr, AbstractFileSystem.List {
    static final long serialVersionUID = -5801291358293736478L;

    /** File name of special file in each folder where attributes are saved.
     * @deprecated does not handle XML attributes
     */
    @Deprecated
    public static final String ATTR_NAME = "filesystem"; // NOI18N

    /** Extension of special file in each folder where attributes are saved.
     * @deprecated does not handle XML attributes
     */
    @Deprecated
    public static final String ATTR_EXT = "attributes"; // NOI18N

    /** Name with extension of special file in each folder where attributes are saved.
     * @deprecated does not handle XML attributes
     */
    @Deprecated
    public static final String ATTR_NAME_EXT = ATTR_NAME + '.' + ATTR_EXT;
    private static final String ATTR_NAME_EXT_XML = System.getProperty(
            "org.openide.filesystems.DefaultAttributes.ATTR_NAME_EXT_XML", ".nbattrs"
        ); // NOI18N

    /**  readOnlyAttrs is name of virtual attribute. This name of virtual attribute
     * is shared between classes (and cannot be changed without breaking compatibility):
     * - org.openide.filesystems.DefaultAttributes
     * - org.openide.loaders.ExecutionSupport
     * - org.openide.loaders.CompilerSupport
     * - org.netbeans.core.ExJarFileSystem
     */
    private static final String READONLY_ATTRIBUTES = "readOnlyAttrs"; //NOI18N

    // <?xml version="1.0"?>
    // <!DOCTYPE filesystem PUBLIC "-//NetBeans//DTD DefaultAttributes 1.0//EN" "http://www.netbeans.org/dtds/attributes-1_0.dtd">
    // <attributes>...</attributes>
    private static final String PUBLIC_ID = "-//NetBeans//DTD DefaultAttributes 1.0//EN"; // NOI18N
    private static final String DTD_PATH = "org/openide/filesystems/attributes.dtd"; // NOI18N    

    /** description of the fs to work on - info about files */
    private AbstractFileSystem.Info info;

    /** description of the fs to work on - work with files */
    private AbstractFileSystem.Change change;

    /** description of the fs to work on - listing of files */
    private AbstractFileSystem.List list;

    /** file name of attributes (default value corresponds to ATTR_NAME_EXT_XML) */
    private String fileName;

    /** Cache of attributes.
    * For name of folder gives map of maps of attibutes
    * (String, Reference (Table))
    */
    private transient Map<String, SoftReference<Table>> cache;

    /** Constructor.
    * @param info file object information to use
    * @param change file change hooks to use
    * @param list list to filter (can be <code>null</code>, but then this object cannot work as a list)
    */
    public DefaultAttributes(
        AbstractFileSystem.Info info, AbstractFileSystem.Change change, AbstractFileSystem.List list
    ) {
        this.info = info;
        this.change = change;
        this.list = list;
        fileName = ATTR_NAME_EXT_XML;
    }

    /** Constructor.
     *
     * @param info file object information to use
     * @param change file change hooks to use
     * @param list list to filter (can be <code>null</code>, but then this object cannot work as a list)
     * @param fileName
     * @since 4.35
     */
    protected DefaultAttributes(
        AbstractFileSystem.Info info, AbstractFileSystem.Change change, AbstractFileSystem.List list, String fileName
    ) {
        this(info, change, list);
        this.fileName = fileName;
    }

    /** Methods to ensure backward compatibility for storing and
    * loading classes.
    */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ObjectInputStream.GetField fields = ois.readFields();

        Object o1 = AbstractFileSystem.readImpl("change", fields); // NOI18N
        Object o2 = AbstractFileSystem.readImpl("info", fields); // NOI18N
        Object o3 = AbstractFileSystem.readImpl("list", fields); // NOI18N

        change = (AbstractFileSystem.Change) o1;
        info = (AbstractFileSystem.Info) o2;
        list = (AbstractFileSystem.List) o3;
    }

    /** Get the children list, filtering out the special attributes file.
    * You <em>must</em> have provided a non-<code>null</code> {@link AbstractFileSystem.List}
    * in the constructor for this to work. If you did not, the rest of the class will work
    * fine, but this method should not be called and this object should not be used
    * as a <code>List</code> implementation.
    *
    * @param f the folder, by name; e.g. <code>top/next/afterthat</code>
    * @return a list of children of the folder, as <code>file.ext</code> (no path)
    */
    public String[] children(String f) {
        String[] arr = list.children(f);
        int lookUpIndex = 0;

        if (arr == null) {
            return null;
        }

        int size = arr.length;

        if (size == 1) {
            // In NB 3.2.x for OpenVMS, we had to use  "_nbattrs." as a attribute file.
            // However, OpenVMS now supports a file name beginning with "."
            // So we now have to copy the existing "_nbattrs." file into ".nbattrs"
            //
            if ((BaseUtilities.getOperatingSystem() == BaseUtilities.OS_VMS) && (arr[0] != null) && (f != null)) {
                if (arr[0].equalsIgnoreCase("_nbattrs.")) {
                    try {
                        deleteFile(f + "/" + arr[0]); // NOI18N
                    } catch (IOException ioe) {
                    }

                    arr[0] = getFileName();
                }
            }

            if ((getFileName().equals(arr[0]) || ATTR_NAME_EXT_XML.equals(arr[0]) || ATTR_NAME_EXT.equals(arr[0]))) {
                try {
                    this.change.delete(f + "/" + arr[0]);
                } catch (IOException iox) {
                }

                return new String[] {  };
            }
        }

        for (int i = 0; i < size; i++) {
            // In NB 3.2.x for OpenVMS, we had to use  "_nbattrs." as a attribute file.
            // However, OpenVMS now supports a file name beginning with "."
            // So we now have to copy the existing "_nbattrs." file into ".nbattrs"
            //
            if ((BaseUtilities.getOperatingSystem() == BaseUtilities.OS_VMS) && (arr[i] != null) && (f != null)) {
                if (arr[i].equalsIgnoreCase("_nbattrs.")) {
                    try {
                        File fp = new File(f + "/" + ".nbattrs");

                        if (!fp.exists()) {
                            cache = null;
                            copyVMSAttrFile(f);
                        }
                    } catch (IOException ioe) {
                    }

                    arr[i] = getFileName();
                }
            }

            String safeNbAttrsCopy = getFileName() + "~"; //NOI18N

            if (
                getFileName().equals(arr[i]) || ATTR_NAME_EXT.equals(arr[i]) || ATTR_NAME_EXT_XML.equals(arr[i]) ||
                    safeNbAttrsCopy.equals(arr[i])
            ) {
                // exclude this index
                arr[i] = null;

                // there can be two files with attributes
                if (++lookUpIndex >= 2) {
                    break;
                }
            }
        }

        return arr;
    }

    /** Renames the attribute file for OpenVMS platform.
     *  The method renames "_nbattrs." into ".nbattrs".
     *  We cannot simply use the change.rename method
     *  because of the special property of OpenVMS having to do with
     *  a file name starting with "."
     *
     *  @param f the folder containg the attribute file
     */
    private void copyVMSAttrFile(String f) throws IOException {
        InputStream is = null;
        OutputStream os = null;

        try {
            change.createData(f + "/" + getFileName());
            is = info.inputStream(f + "/" + "_nbattrs.");
            os = info.outputStream(f + "/" + getFileName());

            byte[] buf = new byte[256];
            int readi;

            while ((readi = is.read(buf, 0, 256)) >= 0x0) {
                os.write(buf, 0, readi);
            }

            is.close();

            //change.delete (f+"/"+"_nbattrs.");
            is = null;
        } catch (IOException ie) {
        } finally {
            if (is != null) {
                is.close();
            }

            if (os != null) {
                os.close();
            }
        }
    }

    // JST: Description
    //
    //
    // The class should be written in such a way that the access to disk is
    // synchronized (this). But during the access nobody is allowed to
    // perform serialization and deserialization
    // of unknown objects, so all objects should be wrapped into NbMarshalledObject
    // serialized or in reverse target NbMarshalledObject should be deserialized
    // and then not holding the lock the object obtained from it by a call to
    // marshall.get ().
    //
    // JST: Got it?

    /* Get the file attribute with the specified name.
    * @param name the file
    * @param attrName name of the attribute
    * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
    */
    public Object readAttribute(String name, String attrName) {
        Table t;
        String[] arr = new String[2];
        split(name, arr);

        /** At the momement substitutes lack of API */
        if (attrName.equals(READONLY_ATTRIBUTES)) {
            return info.readOnly(arr[0]) ? Boolean.TRUE : Boolean.FALSE;
        }

        synchronized (this) {
            // synchronized so only one table for each folder
            // can exist
            t = loadTable(arr[0]);
        }

        // JST:
        // had to split the code to do getAttr out of synchronized block
        // because the attribute can be serialized FileObject and
        // so the code returns back to FileSystem (that is usually synchronized)
        //
        // this leads to deadlocks between FS & DefaultAttributes implementation
        //
        // I do not know if the table should not be somehow synchronized,
        // but it seems ok.
        return t.getAttr(arr[1], attrName);
    }

    /* Set the file attribute with the specified name.
    * @param name the file
    * @param attrName name of the attribute
    * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular filesystems may or may not use serialization to store attribute values.
    * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
    */
    public void writeAttribute(String name, String attrName, Object value)
    throws IOException {
        // create object that should be serialized
        //NbMarshalledObject marshall = new NbMarshalledObject (value);
        int objType;

        String[] arr = new String[2];
        split(name, arr);

        for (;;) {
            int version;
            Table t;

            synchronized (this) {
                t = loadTable(arr[0]);
                version = t.version;
            }

            // Tests if the attribute is changing
            Object prev = t.getAttr(arr[1], attrName);

            if (prev == value /*|| (value != null && value.equals (prev))*/    ) {
                return;
            }

            synchronized (this) {
                Table t2 = loadTable(arr[0]);

                if ((t == t2) && (version == t2.version)) {
                    // no modification between reading of the value =>
                    // save!
                    //Class cls = value.getClass();                    
                    if (value == null) {
                        t.setAttr(arr[1], attrName, null); // clear the attribute
                    } else {
                        if (
                            (objType = XMLMapAttr.Attr.distinguishObject(value)) == XMLMapAttr.Attr.isValid(
                                    "SERIALVALUE"
                                )
                        ) { // NOI18N
                            t.setAttr(arr[1], attrName, value); //change value instead of marshall
                        } else {
                            t.setAttr(arr[1], attrName, XMLMapAttr.createAttribute(objType, value.toString()));
                        }
                    }

                    saveTable(arr[0], t);

                    // ok, saved
                    return;
                }
            }

            // otherwise try it again
        }
    }

    /* Get all file attribute names for the file.
    * @param name the file
    * @return enumeration of keys (as strings)
    */
    public synchronized Enumeration<String> attributes(String name) {
        String[] arr = new String[2];
        split(name, arr);

        Table t = loadTable(arr[0]);

        return t.attrs(arr[1]);
    }

    /* Called when a file is renamed, to appropriatelly update its attributes.
    * <p>
    * @param oldName old name of the file
    * @param newName new name of the file
    */
    public synchronized void renameAttributes(String oldName, String newName) {
        try {
            String[] arr = new String[2];
            split(oldName, arr);

            Table t = loadTable(arr[0]);
            Map v = (Map) t.remove(arr[1]);

            //      System.out.println ("ARg[0] = " + arr[0] + " arr[1] = " + arr[1] + " value: " + v); // NOI18N
            if (v == null) {
                // no attrs no change
                return;
            }

            split(newName, arr);

            // Remove transient attributes:
            Iterator<Map.Entry> it = v.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry pair = it.next();

                if (FileUtil.transientAttributes.contains(pair.getKey())) {
                    it.remove();
                }
            }
            t.put(arr[1], v);

            //      System.out.println ("xyz[0] = " + arr[0] + " xyz[1] = " + arr[1] + " value: " + v); // NOI18N
            saveTable(arr[0], t);
        } catch (IOException e) {
            ExternalUtil.exception(e);
        }
    }

    /* Called when a file is deleted to also delete its attributes.
    *
    * @param name name of the file
    */
    public synchronized void deleteAttributes(String name) {
        try {
            String[] arr = new String[2];
            split(name, arr);

            Table t = loadTable(arr[0]);

            if (t.remove(arr[1]) != null) {
                // if there is a change
                saveTable(arr[0], t);
            }
        } catch (IOException e) {
            ExternalUtil.exception(e);
        }
    }

    /** Getter for the cache.
    */
    private Map<String, SoftReference<Table>> getCache() {
        if (cache == null) {
            cache = new HashMap<>(31);
        }

        return cache;
    }

    /** Splits name of a file to name of folder and to name of the file.
    * @param name of file
    * @param arr arr[0] will hold name of folder and arr[1] name of the file
    */
    private static void split(String name, String[] arr) {
        int i = name.lastIndexOf('/');

        if (i == -1) {
            arr[0] = ""; // NOI18N
            arr[1] = name;

            return;
        }

        // folder name
        arr[0] = name.substring(0, i);

        // increase the i to be beyond the length
        if (++i == name.length()) {
            arr[1] = ""; // NOI18N
        } else {
            // split it
            arr[1] = name.substring(i);
        }
    }

    /** Save attributes.
    * @param name name of folder to save attributes for
    * @param map map to save
    */
    private void saveTable(String name, Table map) throws IOException {
        String fullName = ((name.length() == 0) ? "" : (name + '/')) + getFileName(); // NOI18N

        /** OpenVMS now supports various special characters including "~"*/
        String safeName = fullName + "~"; // NOI18N        

        if (info.folder(fullName)) {
            if (map.size() == 0) {
                // ok no need to delete
                return;
            }

            // find parent
            change.createData(fullName);
        } else {
            if (map.size() == 0) {
                deleteFile(fullName);

                return;
            }
        }

        PrintWriter pw = null;
        IOException ioexc = null;

        try {
            pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(info.outputStream(safeName)), "UTF8")); // NOI18N            
            map.writeToXML(pw);
            pw.flush();
        } catch (IOException iex) {
            ioexc = iex;
        } finally {
            if (pw != null) {
                pw.close();
            }

            if (ioexc != null) {
                try {
                    deleteFile(safeName);
                } catch (IOException ioe) {
                    if (ioe.getCause() == null) {
                        ioe.initCause(ioexc);
                    }
                    throw ioe;
                }
                throw ioexc;
            } else {
                for (int counter = 0; ;counter++) {
                    try {
                        deleteFile(fullName);
                    } catch (IOException iex2) {
                        /** if delete fails, then also rename fails and exception will
                         * be fired
                         */
                        FileSystem.LOG.log(Level.INFO, "Cannot delete " + fullName, iex2); // NOI18N
                    }
                    try {
                        this.change.rename(safeName, fullName);
                        break;
                    } catch (IOException ex) {
                        FileSystem.LOG.log(Level.INFO, "Cannot rename " + fullName + " to " + safeName, ex); // NOI18N
                        if (counter > 10) {
                            throw ex;
                        }
                    }
                }
            }
        }
    }

    /** Load attributes from cache or
    * from disk.
    * @param name of folder to load data from
    */
    private Table loadTable(String name) { //throws IOException {

        SoftReference<Table> r = getCache().get(name);

        if (r != null) {
            Table m = r.get();

            if (m != null) {
                return m;
            }
        }

        // have to load new table
        Table t = load(name);
        t.attach(name, this);

        getCache().put(name, new SoftReference<Table>(t));

        return t;
    }

    /** Loads the table. Does no initialization.
    */
    private Table load(String name) {
        String[] acceptNames = {
            ((name.length() == 0) ? "" : (name + '/')) + getFileName(), // NOI18N
            ((name.length() == 0) ? "" : (name + '/')) + ATTR_NAME_EXT
        }; // NOI18N

        for (int i = 0; i < acceptNames.length; i++) {
            if (info.size(acceptNames[i]) > 0L) {
                try {
                    InputStream fis = info.inputStream(acceptNames[i]);

                    try {
                        return loadTable(fis, acceptNames[i]);
                    } finally {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            // ignore--who cares?
                        }
                    }
                } catch (FileNotFoundException ex) {
                    ExternalUtil.exception(ex);
                }
            }
        }

        return new Table();
    }

    /** Loads the Table of extended attributes for a input stream from binary serialized file or from XML.
    * @param is input stream
    * @param folderName name of file for better error message
    * @return the attributes table for this input stream
    */
    static Table loadTable(InputStream is, String folderName) {
        Table retTable = new Table();
        PushbackInputStream pbStream = null;
        boolean isSerialized = false;

        try {
            if (folderName.endsWith(ATTR_NAME_EXT)) {
                pbStream = new PushbackInputStream(is, 4); //is.available()
                isSerialized = isSerialized(pbStream);
            }

            if (isSerialized && (pbStream != null)) {
                BufferedInputStream fis = new BufferedInputStream(pbStream);
                ObjectInputStream ois = new org.openide.util.io.NbObjectInputStream(fis);
                Object o = ois.readObject();

                if (o instanceof Table) {
                    return (Table) o;
                }
            } else {
                BufferedInputStream bis = (pbStream != null) ? new BufferedInputStream(pbStream)
                                                             : new BufferedInputStream(is);
                retTable.readFromXML(bis, false);

                return retTable;
            }
        } catch (Exception e) {
            // [PENDING] use multi-arg getMessage (MessageFormat-style) properly here:
            IOException summaryEx = new IOException(
                    NbBundle.getMessage(DefaultAttributes.class, "EXC_DefAttrReadErr") + ": " + folderName
                );
            ExternalUtil.copyAnnotation(summaryEx, e);
            ExternalUtil.exception(summaryEx);
        }

        // create empty table, what else
        return new Table();
    }

    /** Tests whether InputStream contains serialized data
    * @param pbStream is pushback input stream; tests 4 bytes and then returns them back
    * @return true if the file has serialized form
    */
    private static final boolean isSerialized(PushbackInputStream pbStream)
    throws IOException {
        int[] serialPattern = { '\u00AC', '\u00ED', '\u0000', '\u0005' }; //NOI18N patern for serialized objects
        byte[] checkedArray = new byte[serialPattern.length];
        int unsignedConv = 0;

        pbStream.read(checkedArray, 0, checkedArray.length);
        pbStream.unread(checkedArray);

        for (int i = 0; i < checkedArray.length; i++) {
            unsignedConv = (checkedArray[i] < 0) ? (checkedArray[i] + 256) : checkedArray[i];

            if (serialPattern[i] != unsignedConv) {
                return false;
            }
        }

        return true;
    }

    /** Remove from cache */
    synchronized void removeTable(String name) {
        getCache().remove(name);
    }

    //
    // FileUtil.extractJar methods
    //

    /** Does the name seems like file with extended attributes?
    * @param name the name
    * @return true if so
    */
    static boolean acceptName(String name) {
        return (name.endsWith(ATTR_NAME_EXT) || name.endsWith(ATTR_NAME_EXT_XML));
    }

    private String getFileName() {
        if (fileName == null) {
            fileName = ATTR_NAME_EXT_XML;
        }

        return fileName;
    }
    
    private void deleteFile(String name) throws IOException {
        OutputStream os = null;
        this.info.lock(name);
        try {
            //added because of mutual exclusion of streams (waits a while until stream is closed)
            os = this.info.outputStream(name);
            os.close(); os = null;
            this.change.delete(name);
        } finally {
            if (os != null) {
                os.close();
            }            
            this.info.unlock(name);
        }
    }
    

    /** Table that hold mapping between files and attributes.
    * Hold mapping of type (String, Map (String, Object))
    */
    static final class Table extends HashMap implements Externalizable {
        static final long serialVersionUID = 2353458763249746934L;

        /** name of folder we belong to */
        private transient String name;

        /** attributes to belong to */
        private transient DefaultAttributes attrs;

        /** version counting */
        private transient int version = 0;

        /** Constructor */
        public Table() {
            super(11);
        }

        /** Attaches to file in attributes */
        public void attach(String name, DefaultAttributes attrs) {
            this.name = name;
            this.attrs = attrs;
        }

        /** Remove itself from the cache if finalized.
        */
        @Override
        protected void finalize() {
            //      System.out.println ("Finalizing table for: " + name); // NOI18N
            attrs.removeTable(name);
        }

        /** For given file finds requested attribute.
         * @param fileName name of the file
         * @param attrName name of the attribute
         * @return attribute or null (if not found)
         */
        public Object getAttr(String fileName, String attrName) {
            XMLMapAttr m = (XMLMapAttr) get(fileName);

            if (m != null) {
                Object o = null;

                try {
                    o = m.getAttribute(attrName);
                } catch (Exception e) {
                    ExternalUtil.annotate(e, "fileName = " + fileName); //NOI18N                                     
                    ExternalUtil.exception(e);
                }

                if (o == null) {
                    return null;
                }

                if (!(o instanceof NbMarshalledObject)) {
                    return o;
                }

                NbMarshalledObject mo = (NbMarshalledObject) o;

                try {
                    return (mo == null) ? null : mo.get();
                } catch (IOException e) {
                    ExternalUtil.log("Cannot load attribute " + attrName + " from " + fileName); // NOI18N
                    ExternalUtil.exception(e);
                } catch (ClassNotFoundException e) {
                    ExternalUtil.log("Cannot load attribute " + attrName + " from " + fileName); // NOI18N
                    ExternalUtil.exception(e);
                }
            }

            return null;
        }

        /** Sets an marshaled attribute to the table.
        */
        final void setMarshalledAttr(String fileName, String attrName, NbMarshalledObject obj) {
            setAttr(fileName, attrName, obj);
        }

        /**
         * Sets an attribute to the table.
         * New added - for Sandwich project (XML format instead of serialization) .
         * @param fileName - name of file
         * @param attrName - name of attribute
         * @param obj - attribute
         */
        final void setAttr(String fileName, String attrName, Object obj) {
            XMLMapAttr m = (XMLMapAttr) get(fileName);

            if (m == null) {
                m = new XMLMapAttr();
                put(fileName, m);
            }

            m.put(attrName, obj, false);

            if ((obj == null) && (m.size() == 1)) {
                remove(fileName);
            }

            // increments the version
            version++;
        }

        /** Enum of attributes for one file.
        */
        public Enumeration<String> attrs(String fileName) {
            Map<String, Map> m = (Map) get(fileName);

            if (m == null) {
                return Enumerations.empty();
            } else {
                Set<String> s = new HashSet<>(m.keySet());

                return Collections.enumeration(s);
            }
        }

        /**
         * Parses element:  <CODE><Attributes version="1.0"></CODE>
         * @return new instance of subclass (anonymous class)of ElementHandler
         */
        private ElementHandler parseFirstLevel() {
            ElementHandler elemService = new ElementHandler() {
                    private final String[] ELM_KEYS = { "ATTRIBUTES" }; // NOI18N
                    private final String[] MANDAT_ATTR_KEYS = { "VERSION" }; // NOI18N

                    @Override
                    public void internalStartElement(String elemName, Map mapMandatory, Map mapAllowed)
                    throws SAXException {
                        // later can check version
                    }

                    @Override
                    protected String[] getKeys() {
                        return ELM_KEYS;
                    }

                    @Override
                    protected String[] getMandatoryAttrs() {
                        return MANDAT_ATTR_KEYS;
                    }
                };

            return elemService;
        }

        /**
         * Parses element:  <CODE><fileobject name="fileName"></CODE>
         * @param fileName is parsed from XML
         * @return new instance of subclass (anonymous class)of ElementHandler
         */
        private ElementHandler parseSecondLevel(final StringBuffer fileName) {
            ElementHandler elemService = new ElementHandler() {
                    private final String[] ELM_KEYS = { "FILEOBJECT" }; // NOI18N
                    private final String[] MANDAT_ATTR_KEYS = { "NAME" }; // NOI18N

                    public void internalStartElement(String elemName, Map mapMandatory, Map mapAllowed)
                    throws SAXException {
                        String temp;
                        fileName.delete(0, fileName.length());
                        temp = (String) mapMandatory.get("NAME"); // NOI18N

                        if (temp == null) {
                            temp = (String) mapMandatory.get("name"); // NOI18N
                        }

                        if (temp != null) {
                            fileName.append(temp);
                        }
                    }

                    @Override
                    public void endElement(String elementName)
                    throws SAXException {
                    }

                    @Override
                    protected String[] getKeys() {
                        return ELM_KEYS;
                    }

                    @Override
                    protected String[] getMandatoryAttrs() {
                        return MANDAT_ATTR_KEYS;
                    }
                };

            return elemService;
        }

        /**
         * Parses element:  <CODE><attr StringValue="This is attribute"></CODE>
         * @param fileName is name of fileobject, which is assigned to attribute
         * @return new instance of subclass (anonymous class)of ElementHandler
         */
        private ElementHandler parseThirdLevel(final StringBuffer fileName) {
            ElementHandler elemService = new ElementHandler() {
                    private final String[] ELM_KEYS = { "ATTR" }; // NOI18N
                    private final String[] MANDAT_ATTR_KEYS = { "NAME" }; // NOI18N

                    public void internalStartElement(String elemName, Map mapMandatory, Map mapAllowed)
                    throws SAXException {
                        String attrName;

                        if (mapAllowed.isEmpty()) {
                            return;
                        }

                        attrName = (String) mapMandatory.get("NAME"); // NOI18N

                        if (attrName == null) {
                            attrName = (String) mapMandatory.get("name"); // NOI18N
                        }

                        if (attrName == null) {
                            return;
                        }

                        Iterator<Map.Entry<String, String>> it = mapAllowed.entrySet().iterator();

                        while (it.hasNext()) {
                            Map.Entry<String, String> pair = it.next();

                            if (XMLMapAttr.Attr.isValid(pair.getKey()) != -1) {
                                XMLMapAttr.Attr attr = XMLMapAttr.createAttributeAndDecode(
                                        pair.getKey(), pair.getValue()
                                    );
                                setAttr(fileName.toString(), attrName, attr);
                            }
                        }
                    }

                    @Override
                    protected String[] getKeys() {
                        return ELM_KEYS;
                    }

                    @Override
                    protected String[] getMandatoryAttrs() {
                        return MANDAT_ATTR_KEYS;
                    }

                    @Override
                    protected String[] getAllowedAttrs() {
                        return XMLMapAttr.Attr.getAttrTypes();
                    }
                     //ALLOWED_ATTR_KEYS
                };

            return elemService;
        }

        /** Writes itself to XML
         * @param pw is PrintWriter
         */
        public void writeToXML(PrintWriter pw) /*throws IOException */ {
            // list of names
            Iterator<String> it = new TreeSet<>(keySet()).iterator();
            XMLMapAttr.writeHeading(pw);

            while (it.hasNext()) {
                String file = it.next();
                XMLMapAttr attr = (XMLMapAttr) get(file);

                if ((attr != null) && !attr.isEmpty()) {
                    attr.write(pw, file, "    "); // NOI18N
                }
            }

            XMLMapAttr.writeEnding(pw);
        }

        /**
         * Reads itself from XML format
         * New added - for Sandwich project (XML format instead of serialization) .
         * @param is input stream (which is parsed)
         * @return Table
         */
        public void readFromXML(InputStream is, boolean validate)
        throws SAXException {
            StringBuffer fileName = new StringBuffer();
            ElementHandler[] elmKeyService = { parseFirstLevel(), parseSecondLevel(fileName), parseThirdLevel(fileName) }; //
            String dtd = getClass().getClassLoader().getResource(DTD_PATH).toExternalForm();
            InnerParser parser = new InnerParser(PUBLIC_ID, dtd, elmKeyService);

            try {
                parser.parseXML(is, validate);
            } catch (Exception ioe) {
                throw (SAXException) ExternalUtil.copyAnnotation(
                    new SAXException(NbBundle.getMessage(DefaultAttributes.class, "EXC_DefAttrReadErr")), ioe
                );
            } catch (FactoryConfigurationError fce) {
                // ??? see http://openide.netbeans.org/servlets/ReadMsg?msgId=340881&listName=dev
                throw (SAXException) ExternalUtil.copyAnnotation(
                    new SAXException(NbBundle.getMessage(DefaultAttributes.class, "EXC_DefAttrReadErr")), fce
                );
            }
        }

        /** Writes external.
         * @param oo
         * @throws IOException  */
        public void writeExternal(ObjectOutput oo) throws IOException {
            // list of names
            Iterator<String> it = keySet().iterator();

            while (it.hasNext()) {
                String file = it.next();
                Map attr = (Map) get(file);

                if ((attr != null) && !attr.isEmpty()) {
                    oo.writeObject(file);

                    Iterator<Map.Entry> entries = attr.entrySet().iterator();

                    while (entries.hasNext()) {
                        Map.Entry entry = entries.next();
                        String key = (String) entry.getKey();
                        Object value = entry.getValue();

                        if ((key != null) && (value != null)) {
                            oo.writeObject(key);
                            oo.writeObject(value);
                        }
                    }

                    oo.writeObject(null);
                }
            }

            oo.writeObject(null);
        }

        /** Reads external.
        */
        public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
            for (;;) {
                String file = (String) oi.readObject();

                if (file == null) {
                    break;
                }

                for (;;) {
                    String attr = (String) oi.readObject();

                    if (attr == null) {
                        break;
                    }

                    Object o = oi.readObject();

                    // backward compatibility
                    if (o instanceof java.rmi.MarshalledObject) {
                        o = ((java.rmi.MarshalledObject) o).get();
                        o = new NbMarshalledObject(o);
                    }

                    // end of backward compatibility
                    if (o instanceof NbMarshalledObject) {
                        setAttr(file, attr, o);
                    }
                }
            }
        }
    }

    /** Element handler should be used as superclass for future classes. These future classes should be passed
     * to constructor of InnerParser as Array. (ElementHandler[]). Each subclass of ElementHandler is responsible for
     * processing one or more elements in XML file.
     * Each subclass of ElementHandler should overwrite one or more of these methods:
     * - protected   String[] getKeys()
     * - protected   String[] getMandatoryAttrs()
     * - protected   String[] getAllowedAttrs()
     * - protected  void endElement(String name) throws SAXException {}
     * - protected  void characters(char[] ch, int start, int length) throws SAXException {}
     * - protected  void  internalStartElement(String elemName, HashMap mapMandatory,HashMap mapAllowed) throws SAXException {}
     */
    abstract static class ElementHandler {
        private static final String[] EMPTY = {  };
        private int mandatAttrCount;

        public void startElement(String elemName, Attributes attrs)
        throws SAXException {
            Map<String, String> mapAllowed   = new HashMap<>();
            Map<String, String> mapMandatory = new HashMap<>();

            if (checkAttributes(attrs, mapMandatory, mapAllowed) == false) {
                throw new SAXException(
                    NbBundle.getMessage(DefaultAttributes.class, "XML_InaccurateParam") + ": " + elemName
                ); // NOI18N 
            }

            internalStartElement(elemName, mapMandatory, mapAllowed);
        }

        /** Inner parser calls this method to notify this class that start element was parsed (<someelement>)
         * @param elemName  name of element
         * @param mapMandatory  map(String attributeName,String attributeValue) which holds pairs attributeName and attributeValue, which are mandatory for this element
         * @param mapAllowed map(String attributeName,String attributeValue) which holds pairs attributeName and attributeValue, which are optional for this element
         * @throws SAXException
         */
        protected void internalStartElement(String elemName, Map mapMandatory, Map mapAllowed)
        throws SAXException {
        }

        /** Inner parser calls this method to notify this class that there is content between start element and end element
         * @param ch[]  array of characters found between start and end element
         * @param start is start position in ch[]
         * @param length is length of content
         * @throws SAXException
         */
        protected void characters(char[] ch, int start, int length)
        throws SAXException {
        }

        /** Inner parser calls this method to notify this class that end element was parsed
         * @param elemName  name of element
         * @throws SAXException
         */
        protected void endElement(String elemName) throws SAXException {
        }

        /** @return names of elements which this class can process
         */
        protected String[] getKeys() {
            return EMPTY;
        }

        /** @return names of attributes which are checked and are mandatory
         */
        protected String[] getMandatoryAttrs() {
            return getKeys();
        }

        /** @return names of attributes which are allowed, are expected, but are not mandatory
         */
        protected String[] getAllowedAttrs() {
            return EMPTY;
        }

        private int isMyTag(String name) {
            return isInArray(name, getKeys());
        }

        private int isAllowedAttr(String name) {
            return isInArray(name, getAllowedAttrs());
        }

        private boolean isMandatOK() {
            return (mandatAttrCount == getMandatoryAttrs().length);
        }

        private int isMandatoryAttr(String name) {
            int retValue = isInArray(name, getMandatoryAttrs());

            if (retValue != -1) {
                mandatAttrCount++;
            }

            return retValue;
        }

        private int isInArray(String name, String[] arr) {
            if ((arr == null) || (name == null)) {
                return -1;
            }

            String correctStr = name.trim();

            for (int i = 0; i < arr.length; i++) {
                if (correctStr.equalsIgnoreCase(arr[i]) == true) {
                    return i;
                }
            }

            return -1;
        }

        private boolean checkAttributes(Attributes attrList, Map<String, String> mapMandatory, Map<String, String> mapAllowed) {
            String temp;
            mandatAttrCount = 0;

            if (attrList == null) {
                return false;
            }

            for (int i = 0; i < attrList.getLength(); i++) {
                if (isMandatoryAttr(attrList.getQName(i)) != -1) {
                    temp = attrList.getQName(i).toUpperCase(Locale.ENGLISH);
                    mapMandatory.put(temp, attrList.getValue(i));

                    continue;
                }

                if (isAllowedAttr(attrList.getQName(i)) != -1) {
                    temp = attrList.getQName(i).toUpperCase(Locale.ENGLISH);
                    mapAllowed.put(temp, attrList.getValue(i));

                    continue;
                }
            }

            return isMandatOK();
        }
    }

    /** Class that can be used to parse XML document (Expects array of ElementHandler clasess).  Calls handler methods of ElementHandler clasess.
     */
    static class InnerParser extends DefaultHandler {
        private ElementHandler[] elmKeyService; // = {fileSystemElement(attrStack),folderElement(attrStack),fileElement(attrStack),attrElement(attrStack)};        
        private String tagInProcess = ""; // NOI18N
        private String publicId;
        private String publicURL;

        InnerParser(String publicId, String publicURL, ElementHandler[] elmKeyService) {
            this.elmKeyService = elmKeyService;
            this.publicId = publicId;
            this.publicURL = publicURL;
        }

        /** Starts parsing document, that can be localized by means of uri parameter
         * @param validate
         * @param uri adress of document, that will be parsed
         * @throws ParserConfigurationException
         * @throws IOException
         * @throws SAXException  */
        public void parseXML(String uri, boolean validate)
        throws IOException, SAXException, ParserConfigurationException, FactoryConfigurationError {
            XMLReader parser = getParser(validate);
            parser.parse(uri);
        }

        /** Starts parsing document - if you have document`s InputStream
         * @param validate
         * @param is document`s InputStream
         * @throws ParserConfigurationException
         * @throws IOException
         * @throws SAXException  */
        public void parseXML(InputStream is, boolean validate)
        throws IOException, SAXException, ParserConfigurationException, FactoryConfigurationError {
            InputSource iSource = new InputSource(is);
            XMLReader parser = getParser(validate);
            parser.parse(iSource);
        }

        private XMLReader getParser(boolean validate)
        throws SAXException, ParserConfigurationException, FactoryConfigurationError {
            XMLReader parser = XMLUtil.createXMLReader(validate);

            // create document handler and register it
            parser.setEntityResolver(this);
            parser.setContentHandler(this);
            parser.setErrorHandler(this);

            return parser;
        }

        @Override
         public void error(SAXParseException exception)
        throws SAXException {
            throw exception;
        }

        @Override
        public void warning(SAXParseException exception)
        throws SAXException {
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception)
        throws SAXException {
            throw exception;
        }

        @Override
        public void startElement(String uri, String lname, String name, Attributes attrs)
        throws SAXException {
            tagInProcess = name = name.trim();

            for (int i = 0; i < elmKeyService.length; i++) {
                if (elmKeyService[i].isMyTag(name) != -1) {
                    elmKeyService[i].startElement(name, attrs);

                    return;
                }
            }

            throw new SAXException(NbBundle.getMessage(DefaultAttributes.class, "XML_UnknownElement") + " " + name); // NOI18N
        }

        @Override
        public void endElement(String uri, String lname, String name) throws SAXException {
            for (int i = 0; i < elmKeyService.length; i++) {
                if (elmKeyService[i].isMyTag(name.trim()) != -1) {
                    elmKeyService[i].endElement(name.trim());

                    return;
                }
            }

            throw new SAXException(NbBundle.getMessage(DefaultAttributes.class, "XML_UnknownElement") + " " + name); // NOI18N
        }

        @Override
        public void characters(char[] ch, int start, int length)
        throws SAXException {
            for (int i = 0; i < elmKeyService.length; i++) {
                if (elmKeyService[i].isMyTag(tagInProcess) != -1) {
                    elmKeyService[i].characters(ch, start, length);

                    return;
                }
            }

            throw new SAXException(
                NbBundle.getMessage(DefaultAttributes.class, "XML_UnknownElement") + " " + tagInProcess
            ); // NOI18N
        }

        @Override
        public InputSource resolveEntity(java.lang.String pid, java.lang.String sid)
        throws SAXException {
            if ((pid != null) && pid.equals(publicId)) {
                return new InputSource(publicURL);
            }

            return new InputSource(sid);
        }
    }
}
