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

package org.netbeans.modules.openide.loaders;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.DocumentType;


import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.Lookup;
import org.openide.util.lookup.*;
import org.openide.xml.EntityCatalog;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileAttributeEvent;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;


/** 
 * Entity resolver which loads entities (typically DTDs) from fixed
 * locations in the system file system, according to public ID.
 * <p>
 * It expects that PUBLIC has at maximum three "//" parts 
 * (standard // vendor // entity name // language). It is basically
 * converted to <tt>"/xml/entities/{vendor}/{entity_name}"</tt> resource name.
 * <p>
 * It also attaches <tt>Environment</tt> according to registrations
 * at <tt>/xml/lookups/</tt> area. There can be registered:
 * <tt>Environment.Provider</tt> or deprecated <tt>XMLDataObject.Processor</tt>
 * and <tt>XMLDataObject.Info</tt> instances.
 * <p>
 * All above are core implementation features.
 *
 * @author  Jaroslav Tulach
 */
@ServiceProviders({@ServiceProvider(service=Environment.Provider.class), @ServiceProvider(service=EntityCatalog.class)})
public final class FileEntityResolver extends EntityCatalog implements Environment.Provider {
    private static final String ENTITY_PREFIX = "/xml/entities"; // NOI18N
    private static final String LOOKUP_PREFIX = "/xml/lookups"; // NOI18N

    static final Logger ERR = Logger.getLogger(FileEntityResolver.class.getName());
    
    /** Constructor
     */
    public FileEntityResolver() {
    }
    
    /** Tries to find the entity on system file system.
     */
    public InputSource resolveEntity(String publicID, String systemID) throws FileNotFoundException, SAXException {
        if (publicID == null) {
            return null;
        }


        String id = convertPublicId (publicID);
        
        StringBuffer sb = new StringBuffer (200);
        sb.append (ENTITY_PREFIX);
        sb.append (id);
        
        FileObject fo = FileUtil.getConfigFile (sb.toString ());
        if (fo != null) {
            
            // fill in InputSource instance, could possibly throw an error..
            InputSource in = new InputSource (fo.getInputStream());
            Object myPublicID = fo.getAttribute("hint.originalPublicID");  //NOI18N
            if (myPublicID instanceof String) {
                in.setPublicId((String)myPublicID);
            }                
            URL url = fo.toURL();
            in.setSystemId(url.toString());  // we get nasty nbfs: instead nbres: but it is enough                
            return in;
        } else {
            return null;
        }
    }
    
    /** A method that tries to find the correct lookup for given XMLDataObject.
     * @return the lookup
     */
    public Lookup getEnvironment(DataObject obj) {
        if (obj instanceof XMLDataObject) {
            XMLDataObject xml = (XMLDataObject)obj;
            
            String id = null;
            try {
                DocumentType domDTD = xml.getDocument ().getDoctype ();
                if (domDTD != null) id = domDTD.getPublicId ();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            } catch (org.xml.sax.SAXException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }

            if (id == null) {
                return null;
            }
            
            id = convertPublicId (id);
            
            return new Lkp (id, xml);
        } else if (obj instanceof InstanceDataObject) {
            return getEnvForIDO((InstanceDataObject) obj);
        }
        return null;
    }
    
    private Lookup getEnvForIDO(InstanceDataObject ido) {
        FileEntityResolver.DTDParser parser = new DTDParser(ido.getPrimaryFile());
        parser.parse();
        String id = parser.getPublicId();
        if (id == null) return null;
        id = convertPublicId (id);
        return new Lkp (id, ido);
    }
    
    /** A method that extracts a listener from data object.
     * 
     * @param obj the data object that we are looking for environment of
     * @param source the obj that provides the environment
     * @return lookup provided by the obj or null if none has been found
     */
    @SuppressWarnings("deprecation")
    private static Lookup findLookup (DataObject obj, DataObject source) {
        if (source == null) {
            return null;
        }
        
        try {
            InstanceCookie cookie = source.getCookie (InstanceCookie.class);
            
            if (cookie != null) {
                Object inst = cookie.instanceCreate ();
                if (inst instanceof Environment.Provider) {
                    return ((Environment.Provider)inst).getEnvironment (obj);
                }
                
                if (!(obj instanceof XMLDataObject)) return null;

                if (inst instanceof XMLDataObject.Processor) {
                    // convert provider
                    XMLDataObject.Info info = new XMLDataObject.Info ();
                    info.addProcessorClass (inst.getClass ());
                    inst = info;
                }

                if (inst instanceof XMLDataObject.Info) {
                    return createInfoLookup ((XMLDataObject)obj, ((XMLDataObject.Info)inst));
                }

            }
        } catch (IOException ex) {
            ERR.log(Level.INFO, "no environment for " + obj, ex); // NOI18N
        } catch (ClassNotFoundException ex) {
            ERR.log(Level.INFO, "no environment for " + obj, ex); // NOI18N
        }
        
        return null;
    }
        
    
    /** Ugly hack to get to openide hidden functionality.
     */
    private static java.lang.reflect.Method method;
    @SuppressWarnings("deprecation")
    private static Lookup createInfoLookup (XMLDataObject obj, XMLDataObject.Info info) {
        // well, it is a wormhole, but just for default compatibility
        synchronized (FileEntityResolver.class) {
            if (method == null) {
                try {
                    java.lang.reflect.Method m = XMLDataObject.class.getDeclaredMethod ("createInfoLookup", new Class[] { // NOI18N
                        XMLDataObject.class,
                        XMLDataObject.Info.class
                    });
                    m.setAccessible (true);
                    method = m;
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
        }
        try {
            return (Lookup)method.invoke (null, new Object[] { obj, info });
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    /** Converts the publicID into filesystem friendly name.
     * <p>
     * It expects that PUBLIC has at maximum three "//" parts 
     * (standard // vendor // entity name // language). It is basically
     * converted to "vendor/entity_name" resource name.
     *
     * @see EntityCatalog
     */
    @SuppressWarnings("fallthrough")
    private static String convertPublicId (String publicID) {
        char[] arr = publicID.toCharArray ();


        int numberofslashes = 0;
        int state = 0;
        int write = 0;
        OUT: for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];

            switch (state) {
            case 0:
                // initial state 
                if (ch == '+' || ch == '-' || ch == 'I' || ch == 'S' || ch == 'O') {
                    // do not write that char
                    continue;
                }
                // switch to regular state
                state = 1;
                // fallthru
            case 1:
                // regular state expecting any character
                if (ch == '/') {
                    state = 2;
                    if (++numberofslashes == 3) {
                        // last part of the ID, exit
                        break OUT;
                    }
                    arr[write++] = '/';
                    continue;
                }
                break;
            case 2:
                // previous character was /
                if (ch == '/') {
                    // ignore second / and write nothing
                    continue;
                }
                state = 1;
                break;
            }

            // write the char into the array
            if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9') {
                arr[write++] = ch;
            } else {
                arr[write++] = '_';
            }
        }

        return new String (arr, 0, write);
    }
        
    
    /** Finds a fileobject for given ID.
     * @param id string id
     * @param last[0] will be filled with last file object we should listen on
     * @return file object that should represent it
     */
    private static FileObject findObject (String id, FileObject[] last) {
        StringBuffer sb = new StringBuffer (200);
        sb.append (LOOKUP_PREFIX);
        sb.append (id);
        int len = sb.length ();
        // at least for now
        sb.append (".instance"); // NOI18N 

        String toSearch1 = sb.toString ();
        int indx = searchFolder (FileUtil.getConfigRoot(), toSearch1, last);
        if (indx == -1) {
            // not possible to find folders
            return null;
        }

        FileObject fo = last[0].getFileObject (toSearch1.substring (indx));
        
        if (fo == null) {
            // try to find a file with xml extension
            sb.setLength (len);
            sb.append (".xml"); // NOI18N
            
            fo = last[0].getFileObject (sb.toString ().substring (indx));
        }
        
        return fo;
    }
    
    /** Find last folder for resourceName.
     * @param fo file object to search from
     * @param resourceName name of file to find
     * @param last last[0] will be filled with the last found name
     * @return position of last / if everything has been searched, or -1 if some files are missing
     */
    private static int searchFolder (FileObject fo, String resourceName, FileObject[] last) {
        int pos = 0;
        
        for (;;) {
            int next = resourceName.indexOf('/', pos);
            if (next == -1) {
                // end of the search
                last[0] = fo;
                return pos;
            }
            
            if (next == pos) {
                pos++;
                continue;
            }
            
            FileObject nf = fo.getFileObject(resourceName.substring (pos, next));
            if (nf == null) {
                // not found a continuation
                last[0] = fo;
                return -1;
            }
            
            // proceed to next one
            pos = next + 1;
            fo = nf;
        }
    }
    
    // internally stops documet parsing when looking for public id
    private static class StopSaxException extends SAXException {
        public StopSaxException() { super("STOP"); } //NOI18N
    }

    private static final StopSaxException STOP = new StopSaxException();
    
    // DTDParser
    /** resolve the PUBLIC item from the xml header of .settings file */
    private static class DTDParser extends org.xml.sax.helpers.DefaultHandler
    implements org.xml.sax.ext.LexicalHandler {
        
        private String publicId = null;
        private FileObject src;
        
        public DTDParser(FileObject src) {
            this.src = src;
        }
        
        public String getPublicId() {
            return publicId;
        }
        
        public void parse() {
            InputStream in = null;
            try {
                org.xml.sax.XMLReader reader = org.openide.xml.XMLUtil.createXMLReader(false, false);
                reader.setContentHandler(this);
                reader.setErrorHandler(this);
                reader.setEntityResolver(this);
                in = new BufferedInputStream (src.getInputStream());
                InputSource is = new InputSource(in);
                try {
                    reader.setFeature("http://xml.org/sax/features/validation", false);  //NOI18N
                } catch (SAXException sex) {
                    ERR.warning(
                    "XML parser does not support validation feature."); //NOI18N
                }
                try {
                    reader.setProperty("http://xml.org/sax/properties/lexical-handler", this);  //NOI18N
                } catch (SAXException sex) {
                    ERR.warning(
                    "XML parser does not support lexical-handler feature.");  //NOI18N
                }
                reader.parse(is);
            } catch (StopSaxException ex) {
                ERR.log(Level.FINE, null, ex);
            } catch (Exception ex) { // SAXException, FileNotFoundException, IOException
                if ("org.openide.util.lookup.AbstractLookup$ISE".equals (ex.getClass ().getName ())) { // NOI18N
                    // this is covered by the FileEntityResolverDeadlock54971Test
                    throw (IllegalStateException)ex;
                }
                
                try {
                    // #25082: do not notify an exception if the file comes
                    // from other filesystem than the system filesystem
                    // #127117 - ignore failures for Windows2Local because they 
                    //are harmless. Files can be corrupted if their saving
                    //is interrupted but windows system can recover from this.
                    if (src.getFileSystem().isDefault() && !src.getPath().startsWith("Windows2Local")) {  //NOI18N
                        ERR.log(Level.WARNING, null, new IOException("Parsing " + src + ": " + ex.getMessage()).initCause(ex)); // NOI18N
                    }
                } catch (org.openide.filesystems.FileStateInvalidException fie) {
                    // ignore
                }
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException exc) {
                    ERR.log(Level.WARNING, "Closing stream for " + src, exc);
                }
            }
        }
        
        @Override
        public InputSource resolveEntity(String publicId, String systemID) {
            InputSource ret = new InputSource(new java.io.StringReader("")); // NOI18N
            ret.setSystemId("StringReader");  //NOI18N
            return ret;
        }
        
        public void endDTD() throws org.xml.sax.SAXException {
            throw STOP;
        }
        
        public void startDTD(String name, String publicId, String systemId) throws org.xml.sax.SAXException {
            this.publicId = publicId;
        }
        
        public void startEntity(String str) throws org.xml.sax.SAXException {}
        public void endEntity(String str) throws org.xml.sax.SAXException {}
        public void comment(char[] values, int param, int param2) throws org.xml.sax.SAXException {}
        public void startCDATA() throws org.xml.sax.SAXException {}
        public void endCDATA() throws org.xml.sax.SAXException {}
        
    }
    
    
    /** A special lookup associated with id.
     */
    private static final class Lkp extends ProxyLookup
    implements PropertyChangeListener, FileChangeListener {
        /** converted ID we are associated with */
        private String id;
        /** for this data object we initialized this lookup */
        private Reference<DataObject> xml;
        
        /** last file folder we are listening on. Initialized lazily */
        private volatile FileObject folder;
        /** a data object that produces values Initialized lazily */
        private volatile DataObject obj;
        
        /** @param id the id to work on */
        public Lkp (String id, DataObject xml) {
            super (new Lookup[0]);
            this.id = id;
            this.xml = new WeakReference<DataObject>(xml);
        }
     
        /** Check whether all necessary values are updated.
         */
        @Override
        protected void beforeLookup (Template t) {
            if (ERR.isLoggable(Level.FINE)) {
                ERR.fine("beforeLookup: " + t.getType() + " for " + getXml()); // NOI18N
            }
            
            if (folder == null && obj == null) {
                update ();
            }
        }
        
        /** Updates current state of the lookup.
         */
        private void update () {
            if (ERR.isLoggable(Level.FINE)) ERR.fine("update: " + id + " for " + getXml()); // NOI18N
            FileObject[] last = new FileObject[1];
            FileObject fo = findObject (id, last);
            if (ERR.isLoggable(Level.FINE)) ERR.fine("fo: " + fo + " for " + getXml()); // NOI18N
            DataObject o = null;
            
            if (fo != null) {
                try {
                    o = DataObject.find (fo);
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("object found: " + o + " for " + getXml()); // NOI18N
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        
            if (o == obj) {
                if (ERR.isLoggable(Level.FINE)) ERR.fine("same data object" + " for " + getXml()); // NOI18N
                // the data object is still the same as used to be
                // 
                Lookup l = findLookup (getXml(),o);
                if (o != null && l != null) {
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("updating lookups" + " for " + getXml()); // NOI18N
                    // just update the lookups
                    setLookups (new Lookup[] { l });
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("updating lookups done" + " for " + getXml()); // NOI18N
                    // and exit
                    return;
                } 
            } else {
                // data object changed
                Lookup l = findLookup(getXml(),o);
                
                if (o != null && l != null) {
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("change the lookup"); // NOI18N
                    // add listener to changes of the data object
                    o.addPropertyChangeListener (
                        org.openide.util.WeakListeners.propertyChange (this, o)
                    );
                    obj = o;
                    // update the lookups
                    setLookups (new Lookup[] { l });
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("change in lookup done" + " for " + getXml()); // NOI18N
                    // and exit
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("data object updated to " + obj + " for " + getXml()); // NOI18N
                    return;
                } else {
                    obj = o;
                    if (ERR.isLoggable(Level.FINE)) ERR.fine("data object updated to " + obj + " for " + getXml()); // NOI18N
                }
            }
            
            if (ERR.isLoggable(Level.FINE)) ERR.fine("delegating to nobody for " + obj + " for " + getXml()); // NOI18N
            // object is null => there are no lookups
            setLookups (new Lookup[0]);
            
            // and start listening on latest existing folder 
            // if we did not do it yet
            if (folder != last[0]) {
                folder = last[0];
                last[0].addFileChangeListener (
                    org.openide.filesystems.FileUtil.weakFileChangeListener (this, last[0])
                );
            }
        }
        
        /** Fired when a file is deleted.
         * @param fe the event describing context where action has taken place
         */
        public void fileDeleted(FileEvent fe) {
            update ();
        }
        
        /** Fired when a new folder is created. This action can only be
         * listened to in folders containing the created folder up to the root of
         * file system.
         *
         * @param fe the event describing context where action has taken place
         */
        public void fileFolderCreated(FileEvent fe) {
            update ();
        }
        
        /** Fired when a new file is created. This action can only be
         * listened in folders containing the created file up to the root of
         * file system.
         *
         * @param fe the event describing context where action has taken place
         */
        public void fileDataCreated(FileEvent fe) {
            update ();
        }
        
        /** Fired when a file attribute is changed.
         * @param fe the event describing context where action has taken place,
         *          the name of attribute and the old and new values.
         */
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent ev) {
            String name = ev.getPropertyName();
            
            if (
                DataObject.PROP_COOKIE.equals(name) ||
                DataObject.PROP_NAME.equals(name) ||
                DataObject.PROP_VALID.equals(name) ||
                DataObject.PROP_PRIMARY_FILE.equals(name)
            ) {
                update ();
            }
        }
        
        /** Fired when a file is renamed.
         * @param fe the event describing context where action has taken place
         *          and the original name and extension.
         */
        public void fileRenamed(FileRenameEvent fe) {
            update ();
        }
        
        /** Fired when a file is changed.
         * @param fe the event describing context where action has taken place
         */
        public void fileChanged(FileEvent fe) {
        }

        private DataObject getXml() {
            return xml.get();
        }
        
    }
}
