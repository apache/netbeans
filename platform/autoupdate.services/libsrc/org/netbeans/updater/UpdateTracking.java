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

package org.netbeans.updater;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.CRC32;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** This class represents module updates tracking
 *
 * @author  Ales Kemr
 */
public final class UpdateTracking {
    
    /** Platform dependent file name separator */
    public static final String FILE_SEPARATOR = System.getProperty ("file.separator");
    public static final String PATH_SEPARATOR = System.getProperty ("path.separator");
    
    public static final String ELEMENT_MODULES = "installed_modules"; // NOI18N
    public static final String ELEMENT_MODULE = "module"; // NOI18N
    public static final String ATTR_CODENAMEBASE = "codename"; // NOI18N
    public static final String ELEMENT_VERSION = "module_version"; // NOI18N
    public static final String ATTR_VERSION = "specification_version"; // NOI18N
    public static final String ATTR_LAST = "last"; // NOI18N
    public static final String ATTR_INSTALL = "install_time"; // NOI18N
    public static final String ELEMENT_FILE = "file"; // NOI18N
    public static final String ATTR_FILE_NAME = "name"; // NOI18N
    public static final String ATTR_ORIGIN = "origin"; // NOI18N
    public static final String UPDATER_ORIGIN = "updater"; // NOI18N
    public static final String INSTALLER_ORIGIN = "installer"; // NOI18N
    
    private static final String ATTR_CRC = "crc"; // NOI18N    
    private static final String NBM_ORIGIN = "nbm"; // NOI18N
    
    public static final String ELEMENT_ADDITIONAL = "module_additional"; // NOI18N
    public static final String ELEMENT_ADDITIONAL_MODULE = "module"; // NOI18N
    public static final String ATTR_ADDITIONAL_NBM_NAME = "nbm_name"; // NOI18N
    public static final String ATTR_ADDITIONAL_SOURCE = "source-display-name"; // NOI18N
    
    public static final String EXTRA_CLUSTER_NAME = "extra";
    
    private static final String LOCALE_DIR = FILE_SEPARATOR + "locale" + FILE_SEPARATOR; // NOI18N

    public static final String TRACKING_FILE_NAME = "update_tracking"; // NOI18N
    public static final String ADDITIONAL_INFO_FILE_NAME = "additional_information.xml"; // NOI18N
    private static final String XML_EXT = ".xml"; // NOI18N
    private static final String FORBID_AUTOUPDATE = ".noautoupdate"; // NOI18N

    /** maps root of clusters to tracking files. (File -> UpdateTracking) */
    private static final Map<File, UpdateTracking> trackings = new HashMap<File, UpdateTracking> ();
    private static final Map<File, UpdateTracking.AdditionalInfo> infos = new HashMap<File, UpdateTracking.AdditionalInfo> ();
    
    /** Mapping from files defining modules to appropriate modules objects.
     */
    private LinkedHashMap<File, Module> installedModules = new LinkedHashMap<File, Module> ();

    private final File directory;
    private final File trackingFile;
    private String origin = NBM_ORIGIN;
    private final UpdatingContext context;
    
    /** Private constructor.
     */
    private UpdateTracking( File nbPath, UpdatingContext context ) {
        assert nbPath != null : "Path cannot be null";
        
        trackingFile = new File( nbPath + FILE_SEPARATOR + TRACKING_FILE_NAME);
        directory = nbPath;
        origin = UPDATER_ORIGIN;
        this.context = context;
    }
    
    //
    // Various factory and utility methods
    //
    
    /** Finds update tracking for given cluster root.
     * @path root of a cluster
     * @param createIfDoesNotExists should new tracking be created if it does not exists
     * @return the tracking for that cluster
     */    
    static UpdateTracking getTracking (File path, boolean createIfDoesNotExists, UpdatingContext context) {
        synchronized (trackings) {
            UpdateTracking track = trackings.get (path);
            if (track == null) {
                File utFile = new File (path, TRACKING_FILE_NAME);
                if (!createIfDoesNotExists && !utFile.isDirectory ()) {
                    // if the update_tracking directory is missing
                    // do not allow creation at all (only in userdir)
                    return null;
                }
                File noAU = new File(path, FORBID_AUTOUPDATE); // NOI18N
                if (noAU.exists()) {
                    // ok, this prevents autoupdate from accessing this 
                    // directory completely
                    return null;
                }
                
                track = new UpdateTracking (path, context);
                trackings.put (path, track);
                track.read ();
                track.scanDir ();
            }
            return track;
        }
    }
    

    /** Finds update tracking for given cluster root.
     * @path root of a cluster
     * @return the additional information for that cluster
     */    
    static UpdateTracking.AdditionalInfo getAdditionalInformation (File path, UpdatingContext context) {
        synchronized (infos) {
            UpdateTracking.AdditionalInfo additionalInfo = infos.get (path);
            if (additionalInfo == null) {
                getTracking (path, false, context);
                File downloadDir = new File (path, ModuleUpdater.DOWNLOAD_DIR);
                if (downloadDir.exists () && downloadDir.isDirectory ()) {
                    File addInfo = new File (downloadDir, ADDITIONAL_INFO_FILE_NAME);
                    if (addInfo.exists ()) {
                        additionalInfo = new UpdateTracking.AdditionalInfo (addInfo);
                    }
                }
            }
            return additionalInfo;
        }
    }
    

    /** Returns the platform installatiion directory.
     * @return the File directory.
     */
    public static File getPlatformDir () {
        String platform = System.getProperty ("netbeans.home");
        return platform == null ? null : new File (platform); // NOI18N
    }
    
    public static File getUserDir () {
        // bugfix #50242: the property "netbeans.user" can return dir with non-normalized file e.g. duplicate //
        // and path and value of this property wrongly differs
        String user = System.getProperty ("netbeans.user");
        File userDir = null;
        if (user != null) {
            // XXX cannot use FileUtil.normalizeFile from here
            userDir = new File (user);
            if (userDir.getPath ().startsWith ("\\\\")) {
                // Could use URI.normalize but only on userDir.getPath().toUri() in JDK 7 (#4723726 breaks UNC for userDir.toURI())
                try {
                    userDir = userDir.getCanonicalFile ();
                } catch (IOException ex) {
                    // fallback when getCanonicalFile fails
                    userDir = userDir.getAbsoluteFile ();
                }
            } else {
                userDir = new File (userDir.toURI ().normalize ()).getAbsoluteFile ();
            }
        }
        
        return userDir;
    }
    
    /** Returns enumeration of Files that represent each possible install
     * directory.
     * @param includeUserDir whether to include also user dir
     * @return List<File>
     */
    public static List<File> clusters (boolean includeUserDir) {
        List<File> files = new ArrayList<File> ();
        
        if (includeUserDir) {
            File ud = getUserDir ();
            if (ud != null) {
                // this prevents autoupdate from accessing this 
                // directory completely
                File noAU = new File (ud, FORBID_AUTOUPDATE); // NOI18N
                if (! noAU.exists ()) {
                    files.add (ud);
                }
            }
        }
        
        String dirs = System.getProperty("netbeans.dirs"); // NOI18N
        if (dirs != null) {
            Enumeration<Object> en = new StringTokenizer (dirs, File.pathSeparator);
            while (en.hasMoreElements ()) {
                File f = new File ((String)en.nextElement ());
                // this prevents autoupdate from accessing this 
                // directory completely
                File noAU = new File (f, FORBID_AUTOUPDATE); // NOI18N
                if (! noAU.exists ()) {
                    files.add (f);
                }
            }
        }
        
        
        File id = getPlatformDir ();
        if (id != null) {
            // this prevents autoupdate from accessing this 
            // directory completely
            File noAU = new File (id, FORBID_AUTOUPDATE); // NOI18N
            if (! noAU.exists ()) {
                files.add (id);
            }
        }
        
        return java.util.Collections.unmodifiableList (files);
    }
    
    //
    // Useful search methods
    //
    
    /** Returns true if module with given code base is installed here
     * @param codeBase name of the module
     * @return true or false
     */
    public boolean isModuleInstalled (String codeBase) {
        for (Module m: installedModules.values ()) {
            String mm = m.codenamebase;
            int indx = mm.indexOf ('/');
            if (indx >= 0) {
                mm = mm.substring (0, indx);
            }
            if (codeBase.equals (mm)) {
                return true;
            }
        }
        return false;
    }
    
    //
    // Private impls
    //
    private static ErrorHandler DUMMY_ERROR_HANDLER = new ErrorHandler() {

                @Override
                public void warning(SAXParseException exception) throws SAXException {
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                }
            };
    
    /** Scan through org.w3c.dom.Document document. */
    private void read() {
        /** org.w3c.dom.Document document */
        org.w3c.dom.Document document;

        File file;
        InputStream is;
        int avail = 0;
        try {
            file = trackingFile;
            
            if ( ! file.isFile () ) {
                return;
            }
            
            is = new FileInputStream( file );
            avail = is.available();

            InputSource xmlInputSource = new InputSource( is );
            document = XMLUtil.parse(xmlInputSource, false, false, DUMMY_ERROR_HANDLER, XMLUtil.createAUResolver());
            if (is != null) {
                is.close();
            }
        }
        catch ( org.xml.sax.SAXException e ) {
            XMLUtil.LOG.log(Level.SEVERE, "Bad update_tracking: " + trackingFile + ", available bytes: " + avail, e); // NOI18N
            return;
        }
        catch ( java.io.IOException e ) {
            XMLUtil.LOG.log(Level.SEVERE, "Missing update_tracking: " + trackingFile + ", available bytes: " + avail, e); // NOI18N
            return;
        }

        org.w3c.dom.Element element = document.getDocumentElement();
        if ((element != null) && element.getTagName().equals(ELEMENT_MODULES)) {
            scanElement_installed_modules(element);
        }            
    }    
    
    /** Scan through org.w3c.dom.Element named installed_modules. */
    void scanElement_installed_modules(org.w3c.dom.Element element) { // <installed_modules>
        // element.getValue();
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getTagName().equals(ELEMENT_MODULE)) {
                    if (true) {
                        throw new IllegalStateException ("What now!?");
                    }
                    // XXX  - should put the module into installedModules but do not know the key
                    // modules.add( scanElement_module(nodeElement, fromuser) );
                }                
            }
        }
    }
    
    /** Scan through org.w3c.dom.Element named module. */
    Module scanElement_module(org.w3c.dom.Element element) { // <module>
        Module module = new Module ();
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().startsWith(ATTR_CODENAMEBASE)) { 
                // <module codename="???"> or old version <module codenamebase="???">
                module.setCodenamebase( attr.getValue() );
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getTagName().equals(ELEMENT_VERSION)) {
                    scanElement_module_version(nodeElement, module);
                }
            }
        }
        return module;
    }
    
    /** Scan through org.w3c.dom.Element named module_version. */
    private void scanElement_module_version(org.w3c.dom.Element element, Module module) { // <module_version>
        Version version = new Version(module);        
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals(ATTR_VERSION)) { // <module_version specification_version="???">
                version.setVersion( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_ORIGIN)) { // <module_version origin="???">
                version.setOrigin( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_LAST)) { // <module_version last="???">
                version.setLast( Boolean.valueOf(attr.getValue() ).booleanValue());
            }
            if (attr.getName().equals(ATTR_INSTALL)) { // <module_version install_time="???">
                long li = 0;
                try {
                    li = Long.parseLong( attr.getValue() );
                } catch ( NumberFormatException nfe ) {
                }
                version.setInstall_time( li );
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getTagName().equals(ELEMENT_FILE)) {
                    scanElement_file(nodeElement, version);
                }
            }
        }
        module.addOldVersion( version );
    }
    
    /** Scan through org.w3c.dom.Element named file. */
    void scanElement_file(org.w3c.dom.Element element, Version version) { // <file>
        ModuleFile file = new ModuleFile();        
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals(ATTR_FILE_NAME)) { // <file name="???">
                file.setName( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_CRC)) { // <file crc="???">
                file.setCrc( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_VERSION)) {
                file.setLocaleversion( attr.getValue() );
            }
        }
        version.addFile (file );
    }
    
    Module readModuleTracking (String codename, boolean create ) {
        new File(directory, TRACKING_FILE_NAME).mkdirs();
        File file = new File (
            new File(directory, TRACKING_FILE_NAME), 
            getTrackingName( codename ) + XML_EXT 
        );
        
        // fix for #34355
        try {
            if ( file.exists() && file.length()==0 ) {
                file.delete();
            }
        } catch (Exception e) {
            // ignore
        }
        
        if ( ! file.exists() ) {
            if ( create ) {
                return new Module( codename, file);
            } else {
                return null;
            }
        }

        return readModuleFromFile( file, codename, create );
    }
    
    Version createVersion(String specversion) {
        Version ver = new Version(null);
        ver.setVersion( specversion );
        return ver;
    }
    
    private Module readModuleFromFile( File file, String codename, boolean create ) {
        
        /** org.w3c.dom.Document document */
        org.w3c.dom.Document document;
        InputStream is;
        try {
            is = new FileInputStream( file );

            InputSource xmlInputSource = new InputSource( is );
            document = XMLUtil.parse(xmlInputSource, false, false, DUMMY_ERROR_HANDLER, XMLUtil.createAUResolver());
            if (is != null) {
                is.close();
            }
        } catch ( org.xml.sax.SAXException e ) {
            XMLUtil.LOG.log(Level.SEVERE, "Bad update_tracking", e); // NOI18N
            return null;
        }
        catch ( java.io.IOException e ) {
            if ( create ) {
                return new Module (codename, file);
            } else {
                return null;
            }
        }

        org.w3c.dom.Element element = document.getDocumentElement();
        if ((element != null) && element.getTagName().equals(ELEMENT_MODULE)) {
            
            Module m = scanElement_module (element);
            m.setFile( file );
            installedModules.put (file, m);
            return m;
        }
        if ( create ) {
            return new Module (codename, file);
        } else {
            return null;
        }
    }
    
    private static String getTrackingName(String codename) {
        String trackingName = codename;
        int pos = trackingName.indexOf('/');    // NOI18N
        if ( pos > -1 ) {
            trackingName = trackingName.substring( 0, pos );
        }
        return trackingName.replace( '.', '-' );       // NOI18N
    }
    
    void deleteUnusedFiles() {
        List<Module> newModules = new ArrayList<Module> (installedModules.values ());
        for (Module mod: newModules) {
            mod.deleteUnusedFiles();
        }
        scanDir ();
    }
    
    public static long getFileCRC(File file) throws IOException {
        BufferedInputStream bsrc = null;
        CRC32 crc = new CRC32();
        try {
            bsrc = new BufferedInputStream( new FileInputStream( file ) );
            byte[] bytes = new byte[1024];
            int i;
            while( (i = bsrc.read(bytes)) != -1 ) {
                crc.update(bytes, 0, i );
            }
        }
        finally {
            if ( bsrc != null ) {
                bsrc.close();
            }
        }
        return crc.getValue();
    }
    
    private void scanDir () {
        File dir = new File (directory, TRACKING_FILE_NAME);
        File[] files = dir.listFiles( new FileFilter() {
                               @Override
                               public boolean accept( File file ) {
                                   if ( !file.isDirectory() && file.getName().toUpperCase().endsWith(".XML") ) {
                                       return true;
                                   } else {
                                       return false;
                                   }
                               }
                           } );
                           
        if (files == null) {
            return;
        }
                           
        for ( int i = 0; i < files.length; i++ ) {
            if (!installedModules.containsKey (files[i])) {
                readModuleFromFile( files[i], null, true );
            }
                
        }
    }
    
    @Override
    public String toString() {
        return "UpdateTracing[" + this.directory + ", origin: " + this.origin + "]";
    }
    
    class Module extends Object {        
        
        /** Holds value of property codenamebase. */
        private String codenamebase;
        
        /** Holds value of property versions. */
        private List<Version> versions = new ArrayList<Version>();
        
        private File file = null;
        
        public Module() {
        }
        
        public Module(String codenamebase, File file) {
            this.codenamebase = codenamebase;
            this.file = file;
        }
        
        private Version lastVersion = null;
        private Version newVersion = null;
        private boolean osgi = false;
        
        /** Getter for property codenamebase.
         * @return Value of property codenamebase.
         */
        String getCodenamebase() {
            return codenamebase;
        }
        
        /** Setter for property codenamebase.
         * @param codenamebase New value of property codenamebase.
         */
        void setCodenamebase(String codenamebase) {
            this.codenamebase = codenamebase;
        }

        void setOSGi(boolean isOSGi) {
            this.osgi = isOSGi;
        }
        boolean isOSGi() {
            return osgi;
        }

        
        /** Getter for property versions.
         * @return Value of property versions.
         */
        List<Version> getVersions() {
            return versions;
        }
        
        /** Setter for property versions.
         * @param versions New value of property versions.
         */
        void setVersions(List<Version> versions) {
            this.versions = versions;
        }
        
        private Version getNewOrLastVersion() {
            if ( newVersion != null ) {
                return newVersion;
            } else {
                return lastVersion;
            }
        }
        
        boolean hasNewVersion() {
            return newVersion != null;
        }
        
        void setFile(File file) {
            this.file = file;
        }
        
        public Version addNewVersion( String spec_version, String origin ) {
            if ( lastVersion != null ) {
                lastVersion.setLast ( false );
            }
            Version version = new Version(this);        
            newVersion = version;
            version.setVersion( spec_version );
            version.setOrigin( origin );
            version.setLast( true );
            version.setInstall_time( System.currentTimeMillis() );
            versions.add( version );
            return version;
        }
        
        void addOldVersion( Version version ) {
            if ( version.isLast() ) {
                lastVersion = version;
            }
                    
            versions.add( version );
        }
        
        void addL10NVersion( Version l_version ) {
            if ( lastVersion != null ) {
                lastVersion.addL10NFiles( l_version.getFiles() );
            } else {
                l_version.setOrigin( origin );
                l_version.setLast( true );
                l_version.setInstall_time( System.currentTimeMillis() );
                versions.add( l_version );
            }
        }
        
        void writeConfigModuleXMLIfMissing () {
            File configDir = new File (new File (directory, ModuleDeactivator.CONFIG), ModuleDeactivator.MODULES); // NOI18N
            
            String candidate = null;
            String oldCandidate = null;
            String newCandidate = null;
            
            String name = codenamebase;
            int indx = name.indexOf ('/');
            if (indx > 0) {
                name = name.substring (0, indx);
            }
            
            // check module name from config file
            String replaced = name.replace ('.', '-'); // NOI18N
            String searchFor;
            
            if (replaced.indexOf (ModuleDeactivator.MODULES) > 0) { // NOI18N
                // standard module
                searchFor = replaced + ".jar"; // NOI18N
            } else if(osgi) {
                searchFor = replaced + ".jar"; // NOI18N
            } else {
                // core module
                searchFor = replaced.substring (replaced.lastIndexOf ('-') > 0 ? replaced.lastIndexOf ('-') + 1 : 0) + ".jar"; // NOI18N
            }
            
            String dash = name.replace ('.', '-');

            {
                boolean needInfoInUserDir = false;
                boolean afterNBMsCluster = false;
                for (File c : clusters(true)) {
                    File hidden = new File(new File(new File(c, "config"), "Modules"), dash + ".xml_hidden");
                    if (hidden.exists()) {
                        hidden.delete();
                        XMLUtil.LOG.info("File " + hidden + " deleted.");
                    }

                    if (directory.equals(c)) {
                        afterNBMsCluster = true;
                        continue;
                    }

                    if (afterNBMsCluster) {
                        continue;
                    }

                    File customConfigs = new File(new File(new File(c, "config"), "Modules"), dash + ".xml");
                    if (customConfigs.exists()) {
                        needInfoInUserDir = lastVersion == null;
                    }
                }

                if (needInfoInUserDir) {
                    // there is a definition for the same XML file in some cluster
                    // already and
                    File userConfig = new File(new File(new File(getUserDir(), "config"), "Modules"), dash + ".xml");
                    writeModulesConfig(userConfig, searchFor, candidate, newCandidate, oldCandidate, name);
                    return;
                }
            }

            File config = new File (configDir,  dash + ".xml"); // NOI18N
            if (config.isFile ()) {
                // already written
                return;
            }
            writeModulesConfig(config, searchFor, candidate, newCandidate, oldCandidate, name);
        }
        
        void write( ) {
            Document document = XMLUtil.createDocument(ELEMENT_MODULE);

            Element e_module = document.getDocumentElement();
            Element e_version;
            Element e_file;

            e_module.setAttribute(ATTR_CODENAMEBASE, getCodenamebase());

            for (Version ver : getVersions()) {
                e_version = document.createElement(ELEMENT_VERSION);
                if (ver.getVersion() != null) {
                    e_version.setAttribute(ATTR_VERSION, ver.getVersion());
                }
                e_version.setAttribute(ATTR_ORIGIN, ver.getOrigin());
                e_version.setAttribute(ATTR_LAST, Boolean.valueOf(ver.isLast()).toString());
                e_version.setAttribute(ATTR_INSTALL, Long.toString(ver.getInstall_time()));
                e_module.appendChild(e_version);
                
                for (ModuleFile moduleFile : ver.getFiles()) {
                    e_file = document.createElement(ELEMENT_FILE);
                    e_file.setAttribute(ATTR_FILE_NAME, moduleFile.getName());
                    e_file.setAttribute(ATTR_CRC, moduleFile.getCrc());
                    if (moduleFile.getLocaleversion() != null) {
                        e_file.setAttribute(ATTR_VERSION, moduleFile.getLocaleversion());
                    }
                    e_version.appendChild(e_file);
                }
            }

            document.getDocumentElement().normalize();

            OutputStream os = null;
            try {
                os = context.createOS(file);
            } catch (Exception e) {
                XMLUtil.LOG.log(Level.WARNING, "Cannot read " + file, e);
                //#154904
                if (!file.delete()) {
                    XMLUtil.LOG.log(Level.SEVERE, null, new IOException("Corresponding update would not be installed since it is not possible to modify or delete update tracking file " + file));
                } else {
                    XMLUtil.LOG.log(Level.SEVERE, null, new IOException("Update tracking file was deleted since permissions does not allow to modify it: " + file));
                    try {
                        os = context.createOS(file);
                    } catch (Exception ex) {
                        XMLUtil.LOG.log(Level.WARNING, "Cannot read", ex);
                    }
                }
            }

            if (os != null) {
                try {
                    XMLUtil.write(document, os);
                    XMLUtil.LOG.info("File " + file + " modified.");
                } catch (IOException e) {
                    XMLUtil.LOG.log(Level.WARNING, "Cannot write " + file, e);
                } finally {
                    try {
                        os.close();
                    } catch (IOException e) {
                        XMLUtil.LOG.log(Level.WARNING, "Cannot close " + file, e);
                    }
                }
            }
        }

        void deleteUnusedFiles() {
            if ( lastVersion == null || newVersion == null ) {
                return;
            }
            for (ModuleFile modFile : lastVersion.getFiles()) {
                if ( ! newVersion.containsFile( modFile ) && modFile.getName().indexOf( LOCALE_DIR ) == -1 ) {
                    safeDelete( modFile );
                }
            }
        }
        
        private void safeDelete(ModuleFile modFile) {
            // test file existence
            File f = new File( file.getParentFile().getParent() + FILE_SEPARATOR + modFile.getName() );
            if ( f.exists() ) {
                // test crc
                try {
                    if (! Long.toString(getFileCRC(f)).equals(modFile.getCrc())) {
                        return;
                    }
                } catch ( IOException ioe ) {
                    return;
                }

                // test if file is referenced from other module
                scanDir();
                boolean found = false;
                Iterator<Module> it = installedModules.values ().iterator();
                while ( !found && it.hasNext() ) {
                    Module mod = it.next();
                    if ( ! mod.equals( this ) ) {
                        Version v = mod.getNewOrLastVersion();
                        if ( v != null && v.containsFile( modFile ) ) {
                            found = true;
                        }
                    }
                }
                if ( ! found ) {
                    XMLUtil.LOG.info("Deleting file: " + f);
                    boolean deleted = f.delete();
                    XMLUtil.LOG.info(".... " + f + " was deleted? " + deleted);
                }
            }
        }
        
        String getL10NSpecificationVersion(String jarpath) {
            String localever;
            Collections.<Version>sort( versions );
            for (Version ver: versions) {
                localever = ver.getLocaleVersion( jarpath );
                if ( localever != null ) {
                    return localever;
                }
            }
            return null;
        }

        private void writeModulesConfig(File config, String searchFor, String candidate, String newCandidate, String oldCandidate, String name) {
            config.getParentFile().mkdirs();
            Boolean isAutoload = null;
            Boolean isEager = null;
            java.util.Iterator it = newVersion.getFiles().iterator();
            boolean needToWrite = false;
            while (it.hasNext()) {
                ModuleFile f = (ModuleFile) it.next ();
                String n = f.getName();
                String parentDir;
                {
                    File p = new File(f.getName()).getParentFile();
                    parentDir = p != null ? p.getName() : "";
                }
                needToWrite = needToWrite || n.indexOf(ModuleDeactivator.MODULES) >= 0 || osgi;
                if (n.endsWith(".jar") && ! parentDir.equals("ext")) { // NOI18N
                    // ok, module candidate
                    candidate = f.getName();
                    // the correct candidate looks as e.g. org.netbeans.modules.mymodule
                    // if no jar looks as codenamebase then the jar file will be found as module's jar
                    if (searchFor.endsWith(candidate) || candidate.endsWith(searchFor)) {
                        newCandidate = candidate;
                        oldCandidate = null;
                        // autoload and eager will set by module's jar
                        if ("autoload".equals(parentDir)) {
                            // NOI18N
                            isAutoload = Boolean.TRUE;
                        } else {
                            isAutoload = Boolean.FALSE;
                        }
                        if ("eager".equals(parentDir)) {
                            // NOI18N
                            isEager = Boolean.TRUE;
                        } else {
                            isEager = Boolean.FALSE;
                        }
                    } else {
                        if (newCandidate == null) {
                            oldCandidate = (oldCandidate == null ? "" : oldCandidate + ", ") + candidate; // NOI18N
                        }
                    }
                }
                // if no correct name found => set autoload/eager by the last jar file
                if (isAutoload == null && "autoload".equals(parentDir)) {
                    // NOI18N
                    isAutoload = Boolean.TRUE;
                }
                if (isEager == null && "eager".equals(parentDir)) {
                    // NOI18N
                    isEager = Boolean.TRUE;
                }
            }
            if (!needToWrite) {
                XMLUtil.LOG.log(Level.WARNING, "No config file written for module {0}. No jar file present in \"modules\" directory.", codenamebase);
                return;
            }
            assert newCandidate != null || oldCandidate != null : "No jar file present!";
            if (newCandidate == null) {
                // PENDING: should check but some NBM assumed wrong behaviour before bugfix 53316
                assert oldCandidate.equals(candidate) : "More files look as module: " + oldCandidate;
                // only temporary
                if (!oldCandidate.equals(candidate)) {
                    XMLUtil.LOG.log(Level.WARNING, "More files look as module: {0}", oldCandidate);
                    oldCandidate = candidate;
                }
                // end of temp
            }
            String moduleName = newCandidate == null ? oldCandidate : newCandidate;
            boolean autoload = isAutoload != null && isAutoload.booleanValue();
            boolean eager = isEager != null && isEager.booleanValue();
            boolean isEnabled = !autoload && !eager;
            String spec = newVersion.getVersion();
            OutputStream os;
            try {
                os = context.createOS(config);
                PrintWriter pw = new PrintWriter(new java.io.OutputStreamWriter(os, "UTF-8"));
                // Please make sure formatting matches what the IDE actually spits
                // out; it could matter.
                pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                pw.println("<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"");
                pw.println("                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">");
                pw.println("<module name=\"" + name + "\">");
                pw.println("    <param name=\"autoload\">" + autoload + "</param>");
                pw.println("    <param name=\"eager\">" + eager + "</param>");
                if (isEnabled) {
                    pw.println("    <param name=\"enabled\">" + isEnabled + "</param>");
                }
                pw.println("    <param name=\"jar\">" + moduleName + "</param>");
                pw.println("    <param name=\"reloadable\">false</param>");
                pw.println("    <param name=\"specversion\">" + spec + "</param>");
                pw.println("</module>");
                pw.flush();
                pw.close();
                XMLUtil.LOG.info("New config was written in " + config);
            } catch (IOException ex) {
                XMLUtil.LOG.log(Level.INFO, null, ex);
            }
        }
        
        @Override
        public String toString() {
            return "UpdateTracing.Module[" + this.codenamebase + "(" + this.file + "), OSGI? " + this.osgi + "]";
        }
    }
    
    public class Version extends Object implements Comparable<Version> {
        private final Module module;
        
        Version(Module m) {
            this.module = m;
        }
        
        /** Holds value of property version. */
        private String version;
        
        /** Holds value of property origin. */
        private String origin;
        
        /** Holds value of property last. */
        private boolean last;
        
        /** Holds value of property install_time. */
        private long install_time = 0;
        
        /** Holds value of property files. */
        private List<ModuleFile> files = new ArrayList<ModuleFile>();
        
        /** Getter for property version.
         * @return Value of property version.
         */
        String getVersion() {
            return version;
        }
        
        /** Setter for property version.
         * @param version New value of property version.
         */
        void setVersion(String version) {
            this.version = version;
        }
        
        /** Getter for property origin.
         * @return Value of property origin.
         */
        String getOrigin() {
            return origin;
        }
        
        /** Setter for property origin.
         * @param origin New value of property origin.
         */
        void setOrigin(String origin) {
            this.origin = origin;
        }
        
        /** Getter for property last.
         * @return Value of property last.
         */
        boolean isLast() {
            return last;
        }
        
        /** Setter for property last.
         * @param last New value of property last.
         */
        void setLast(boolean last) {
            this.last = last;
        }
        
        /** Getter for property install_time.
         * @return Value of property install_time.
         */
        long getInstall_time() {
            return install_time;
        }
        
        /** Setter for property install_time.
         * @param install_time New value of property install_time.
         */
        void setInstall_time(long install_time) {
            this.install_time = install_time;
        }
        
        /** Getter for property files.
         * @return Value of property files.
         */
        List<ModuleFile> getFiles() {
            return files;
        }
        
        /** Setter for property files.
         * @param files New value of property files.
         */
        void addL10NFiles(List<ModuleFile> l10nfiles) {
            for (ModuleFile lf : l10nfiles) {
                String lname = lf.getName();
                for ( int i = files.size() - 1; i >=0; i-- ) {
                    ModuleFile f = files.get( i );
                    if ( f.getName().equals( lname ) ) {
                        files.remove( i );
                    }
                }
            }
            files.addAll( l10nfiles );
        }
        
        void addFile( ModuleFile file ) {
            files.add( file );
        }
        
        public void addFileWithCrc( String filename, String crc ) {
            ModuleFile file = new ModuleFile();
            file.setName( filename );
            file.setCrc( crc );
            files.add( file );
        }
        
        public void addL10NFileWithCrc( String filename, String crc, String specver ) {
            ModuleFile file = new ModuleFile();
            file.setName( filename );
            file.setCrc( crc );
            file.setLocaleversion( specver );
            files.add( file );
        }
        
        boolean containsFile( ModuleFile file ) {
            for (ModuleFile f : files) {
                if ( f.getName().equals( file.getName() ) ) {
                    return true;
                }
            }
            return false;
        }
        
        ModuleFile findFile(String filename) {
            for (ModuleFile f : files) {
                if ( f.getName().equals( filename ) ) {
                    return f;
                }
            }
            return null;
        }
        
        String getLocaleVersion(String filename) {
            String locver = null;
            ModuleFile f = findFile( filename );
            if ( f != null ) {
                locver = f.getLocaleversion();
                if ( locver == null ) {
                    locver = version;
                }
            }
            return locver;
        }
        
        @Override
        public int compareTo (Version oth) {
            if ( install_time < oth.getInstall_time() ) {
                return 1;
            }
            else if ( install_time > oth.getInstall_time() ) {
                return -1;
            } else {
                return 0;
            }
        }
        
        @Override
        public String toString() {
            return "UpdateTracing.Version[" + this.module + "/" + this.version + ", last? " + this.isLast() + "]";
        }
    }
    
    class ModuleFile extends Object {        
        
        /** Holds value of property name. */
        private String name;
        
        /** Holds value of property crc. */
        private String crc;
        
        /** Holds value of property localeversion. */
        private String localeversion = null;
        
        /** Getter for property name.
         * @return Value of property name.
         */
        String getName() {
            return name;
        }
        
        /** Setter for property name.
         * @param name New value of property name.
         */
        void setName(String name) {
            this.name = name;
        }
        
        /** Getter for property crc.
         * @return Value of property crc.
         */
        String getCrc() {
            return crc;
        }
        
        /** Setter for property crc.
         * @param crc New value of property crc.
         */
        void setCrc(String crc) {
            this.crc = crc;
        }
        
        /** Getter for property localeversion.
         * @return Value of property localeversion.
         *
         */
        public String getLocaleversion() {
            return this.localeversion;
        }
        
        /** Setter for property localeversion.
         * @param localeversion New value of property localeversion.
         *
         */
        public void setLocaleversion(String localeversion) {
            this.localeversion = localeversion;
        }
        
        @Override
        public String toString() {
            return "UpdateTracing.ModuleFile[" + this.name + "(" + this.crc + ")" + "]";
        }
        
    }

    public static class AdditionalInfo extends Object {
        private Map<String, String> sources;
        
        private AdditionalInfo (File additionalInfoFile) {
            sources = readAdditionalInfoFile (additionalInfoFile);
        }
        
        public String getSource (String nbmFileName) {
            return sources != null ? sources.get (nbmFileName) : null;
        }
        
        private Map<String, String> readAdditionalInfoFile (File f) {
            if (f == null || ! f.exists ()) {
                throw new IllegalArgumentException ("AdditionalInfo file " + f + " must exists.");
            }

            Map<String, String> res = null;

            /** org.w3c.dom.Document document */
            org.w3c.dom.Document document;

            InputStream is = null;
            try {
                is = new FileInputStream (f);
                document = XMLUtil.parse (new InputSource (is), false, false, null, null);
            } catch (org.xml.sax.SAXException e) {
                XMLUtil.LOG.log (Level.WARNING,"Bad " + UpdateTracking.ADDITIONAL_INFO_FILE_NAME + f, e); // NOI18N
                return res;
            } catch (java.io.IOException e) {
                XMLUtil.LOG.log (Level.WARNING,"Missing " + UpdateTracking.ADDITIONAL_INFO_FILE_NAME + f, e); // NOI18N
                return res;
            } finally {
                if (is != null) {
                    try {
                        is.close ();
                    } catch (IOException ioe) {
                        XMLUtil.LOG.log (Level.INFO, "Cannot close stream for file " + f, ioe); // NOI18N
                        return res;
                    }
                }
            }

            org.w3c.dom.Element element = document.getDocumentElement ();
            if ((element != null) && element.getTagName ().equals (ELEMENT_ADDITIONAL)) {
                res = scanModuleAdditional (element);
            }         

            return res;
        }
        
        private Map<String, String> scanModuleAdditional (org.w3c.dom.Element element) {
            Map<String, String> res = new HashMap<String, String> ();
            org.w3c.dom.NodeList nodes = element.getChildNodes ();
            for (int i = 0; i < nodes.getLength (); i++) {
                org.w3c.dom.Node node = nodes.item (i);
                if (node.getNodeType () == org.w3c.dom.Node.ELEMENT_NODE) {
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName ().equals (ELEMENT_ADDITIONAL_MODULE)) {
                        String fileSpec = nodeElement.getAttribute (ATTR_ADDITIONAL_NBM_NAME);
                        String source = nodeElement.getAttribute (ATTR_ADDITIONAL_SOURCE);
                        res.put (fileSpec, source);
                    }                
                }
            }
            return res;
        }
    }
    
}
