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

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;
import java.io.FileOutputStream;
import java.util.zip.CRC32;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import org.apache.tools.ant.BuildException;

/** This class represents module updates tracking
 *
 * @author  akemr
 */
class UpdateTracking {
    private static final String ELEMENT_MODULE = "module"; // NOI18N
    private static final String ATTR_CODENAME = "codename"; // NOI18N
    private static final String ELEMENT_VERSION = "module_version"; // NOI18N
    private static final String ATTR_VERSION = "specification_version"; // NOI18N
    private static final String ATTR_ORIGIN = "origin"; // NOI18N
    private static final String ATTR_LAST = "last"; // NOI18N
    private static final String ATTR_INSTALL = "install_time"; // NOI18N
    private static final String ELEMENT_FILE = "file"; // NOI18N
    private static final String ATTR_FILE_NAME = "name"; // NOI18N
    private static final String ATTR_CRC = "crc"; // NOI18N
    
    private static final String NBM_ORIGIN = "nbm"; // NOI18N
    private static final String INST_ORIGIN = "installer"; // NOI18N

    /** Platform dependent file name separator */
    private static final String FILE_SEPARATOR = System.getProperty ("file.separator");  // NOI18N           

    /** The name of the install_later file */
    public static final String TRACKING_DIRECTORY = "update_tracking"; // NOI18N
    
    private File trackingFile = null;
    
    private String origin = NBM_ORIGIN;
    private String nbPath = null;
    private Module module = null;
    protected InputStream is = null;
    protected OutputStream os = null;
   
    // for generating xml in build process
    public UpdateTracking( String nbPath ) {
        this.nbPath = nbPath;
        origin = INST_ORIGIN;
    }

    /**
     * Use this constructor, only when you want to use I/O Streams
     */
    public UpdateTracking () {
        this.nbPath = null;
        origin = INST_ORIGIN;
    }
    
    public Version addNewModuleVersion( String codename, String spec_version ) {
        module = new Module();
        module.setCodename( codename );
        Version version = new Version();        
        version.setVersion( spec_version );
        version.setOrigin( origin );
        version.setLast( true );
        version.setInstall_time( System.currentTimeMillis() );
        module.setVersion( version );
        return version;
    }
    
    public String getVersionFromFile (File utf) throws BuildException {
        this.setTrackingFile(utf.getParentFile(), utf.getName());
        read();
        if ( module.getVersions().size() != 1 ) 
            throw new BuildException ("Module described in update tracking file " + utf.getAbsolutePath() + " has got " + module.getVersions().size() + " specification versions. Correct number is 1.");
        return module.getVersions().get(0).getVersion();
    }
    
    public String getCodenameFromFile (File utf) throws BuildException {
        this.setTrackingFile(utf.getParentFile(), utf.getName());
        read();
        if ( module.getVersions().size() != 1 ) 
            throw new BuildException ("Module described in update tracking file " + utf.getAbsolutePath() + " has got " + module.getVersions().size() + " specification versions. Correct number is 1.");
        return module.getCodename();
    }
    
    public String getVersionForCodeName( String codeName ) throws BuildException {
        module = new Module();
        module.setCodename( codeName );
//        if (this.is == null) {
            File directory = new File( nbPath + FILE_SEPARATOR + TRACKING_DIRECTORY );
            setTrackingFile(directory, getTrackingFileName());
            if (!trackingFile.exists() || !trackingFile.isFile())
                throw new BuildException ("Tracking file " + trackingFile.getAbsolutePath() + " cannot be found for module " + module.getCodenamebase());
//        }
        read();
        if ( module.getVersions().size() != 1 ) 
            throw new BuildException ("Module with codenamebase " + codeName + " has got " + module.getVersions().size() + " specification versions. Correct number is 1.");
        return module.getVersions().get(0).getVersion();
    }
    
    public String[] getListOfNBM( String codeName ) throws BuildException {
        module = new Module();
        module.setCodename( codeName );
        if (this.is == null) {
            File directory = new File( nbPath + FILE_SEPARATOR + TRACKING_DIRECTORY );
            setTrackingFile(directory, getTrackingFileName());
            if (!trackingFile.exists() || !trackingFile.isFile())
                throw new BuildException ("Tracking file " + trackingFile.getAbsolutePath() + " cannot be found for module " + module.getCodenamebase());
        }
        
        read();
        
        if ( module.getVersions().size() != 1 ) 
            throw new BuildException ("Module with codenamebase " + codeName + " has got " + module.getVersions().size() + " specification versions. Correct number is 1.");
        
        List<ModuleFile> files = module.getVersions().get(0).getFiles();
        String [] listFiles = new String[ files.size() ];
        for (int i=0; i < files.size(); i++) {
            listFiles[i] = files.get(i).getName().replace(File.separatorChar,'/');
        }
        
        return listFiles;
    }

    public void removeLocalized( String locale ) {
        File updateDirectory = new File( nbPath, TRACKING_DIRECTORY );
        File[] trackingFiles = updateDirectory.listFiles( new FileFilter() { // Get only *.xml files
            public boolean accept( File file ) {
                return file.isFile() &&file.getName().endsWith(".xml"); //NOI18N
            }
        } );
        if (trackingFiles != null)
            for (int i = trackingFiles.length-1; i >= 0; i--) {
                trackingFile = trackingFiles[i];
                read();
                module.removeLocalized( locale );
                write();
            }
    }
    
    void write( ) throws BuildException{
        Document document = XMLUtil.createDocument(ELEMENT_MODULE);  
        Element e_module = document.getDocumentElement();
        e_module.setAttribute(ATTR_CODENAME, module.getCodename());
        for (Version ver : module.getVersions()) {
            Element e_version = document.createElement(ELEMENT_VERSION);
            e_version.setAttribute(ATTR_VERSION, ver.getVersion());
            e_version.setAttribute(ATTR_ORIGIN, ver.getOrigin());
            e_version.setAttribute(ATTR_LAST, "true");                          //NOI18N
            e_version.setAttribute(ATTR_INSTALL, Long.toString(ver.getInstall_time()));
            e_module.appendChild( e_version );
            for (ModuleFile file : ver.getFiles()) {
                Element e_file = document.createElement(ELEMENT_FILE);
                e_file.setAttribute(ATTR_FILE_NAME, file.getName().replace(File.separatorChar,'/'));
                e_file.setAttribute(ATTR_CRC, file.getCrc());
                e_version.appendChild( e_file );
            }
        }
        
        //document.getDocumentElement().normalize();
        if (this.os == null) {
            File directory = new File( nbPath + FILE_SEPARATOR + TRACKING_DIRECTORY );
            if (!directory.exists()) {
                directory.mkdirs();
            }
            setTrackingFile(directory, this.getTrackingFileName());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(directory,this.getTrackingFileName()));
            } catch (Exception e) {
                throw new BuildException("Could not get outputstream to write update tracking", e);
            }
            this.setTrackingOutputStream(fos);
        }
        try {
            try {
                XMLUtil.write(document, this.os);
            } finally {
                this.os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ((trackingFile != null) && (trackingFile.exists()))
                trackingFile.delete();
            throw new BuildException("Could not write update tracking", e);
        }        
    }

    protected void setTrackingFile (File dir, String tFname) throws BuildException {
        this.trackingFile = new File(dir,tFname);
//        this.trackingFile.mkdirs();
        try {
            //setTrackingOutputStream(new FileOutputStream(this.trackingFile));
            if (this.trackingFile.exists())
                setTrackingInputStream(new FileInputStream(this.trackingFile));
        } catch (java.io.FileNotFoundException fnf) {
            throw new BuildException("Unable to find tracking file "+this.trackingFile.getAbsolutePath(), fnf);
        }
    }
    
    public void setTrackingOutputStream(OutputStream tos) {
        this.os = tos;
    }
    
    public OutputStream getTrackingOutputStream() {
        return this.os;
    }
    
    public void setTrackingInputStream(InputStream tis) {
        this.is = tis;
    }
    
    public String getTrackingFileName() throws BuildException {
        String trackingFileName = module.getCodenamebase();
        if ( ( trackingFileName == null ) || ( trackingFileName.length() == 0 ) )
            throw new BuildException ("Empty codenamebase, unable to locate tracking file");
        trackingFileName = trackingFileName.replace('.', '-') + ".xml"; //NOI18N
        return trackingFileName;
    }

    /** Scan through Document document. */
    private void read() throws BuildException {
        /** Document document */
        Document document;
        if (this.is == null) {
            File directory = new File( nbPath + FILE_SEPARATOR + TRACKING_DIRECTORY );
            if (!directory.exists()) {
                directory.mkdirs();
            }
            setTrackingFile(directory,getTrackingFileName());
        }
        try {
            InputSource xmlInputSource = new InputSource( this.is );
            document = XMLUtil.parse( xmlInputSource, false, false, XMLUtil.rethrowHandler(), XMLUtil.nullResolver());
            if (is != null)
                is.close();
        } catch ( org.xml.sax.SAXException e ) {
            e.printStackTrace();
            if (trackingFile == null) {
                throw new BuildException ("Update tracking data in external InputStream is not well formatted XML document.", e);
            } else {
                throw new BuildException ("Update tracking file " + trackingFile.getAbsolutePath() + " is not well formatted XML document.", e);
            }
        } catch ( java.io.IOException e ) {
            e.printStackTrace();
            if (trackingFile == null) {
                throw new BuildException ("I/O error while accessing tracking data in InputStream", e);
            } else {
                throw new BuildException ("I/O error while accessing tracking file " + trackingFile.getAbsolutePath(), e);
            }
        }
            
        Element element = document.getDocumentElement();
        if ((element != null) && element.getTagName().equals(ELEMENT_MODULE)) {
            scanElement_module(element);
        }
    }    
    
    /** Scan through Element named module. */
    void scanElement_module(Element element) { // <module>
        module = new Module();        
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            if (attr.getName().equals(ATTR_CODENAME)) { // <module codename="???">
                module.setCodename( attr.getValue() );
            }
        }
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if ( node.getNodeType() == Node.ELEMENT_NODE ) {
                Element nodeElement = (Element) node;
                if (nodeElement.getTagName().equals(ELEMENT_VERSION)) {
                    scanElement_module_version(nodeElement, module);
                }
            }
        }
    }
    
    /** Scan through Element named module_version. */
    void scanElement_module_version(Element element, Module module) { // <module_version>
        Version version = new Version();        
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            if (attr.getName().equals(ATTR_VERSION)) { // <module_version specification_version="???">
                version.setVersion( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_ORIGIN)) { // <module_version origin="???">
                version.setOrigin( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_LAST)) { // <module_version last="???">                
                version.setLast( Boolean.getBoolean(attr.getValue() ));
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
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element nodeElement = (Element) node;
                if (nodeElement.getTagName().equals(ELEMENT_FILE)) {
                    scanElement_file(nodeElement, version);
                }
            }
        }
        module.addVersion( version );
    }
    
    /** Scan through Element named file. */
    void scanElement_file(Element element, Version version) { // <file>
        ModuleFile file = new ModuleFile();        
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr)attrs.item(i);
            if (attr.getName().equals(ATTR_FILE_NAME)) { // <file name="???">
                file.setName( attr.getValue().replace(File.separatorChar,'/') );
            }
            if (attr.getName().equals(ATTR_CRC)) { // <file crc="???">
                file.setCrc( attr.getValue() );
            }
        }
        version.addFile (file );
    }

    static CRC32 crcForFile(File inFile) throws FileNotFoundException, IOException {
        try (FileInputStream inFileStream = new FileInputStream(inFile)) {
            byte[] array = new byte[(int) inFile.length()];
            CRC32 crc = new CRC32();
            int len = inFileStream.read(array);
            if (len != array.length) {
                throw new BuildException("Cannot fully read " + inFile);
            }
            crc.update(array);
            return crc;
        }
    }
    
    class Module extends Object {        
        
        /** Holds value of property codename. */
        private String codename;
        
        /** Holds value of property versions. */
        private List<Version> versions = new ArrayList<>();
        
        /** Getter for property codenamebase.
         * @return Value of property codenamebase.
         */
        String getCodenamebase() {
	    String codenamebase = new String(codename);
            int idx = codenamebase.lastIndexOf ('/'); //NOI18N
            if (idx != -1) codenamebase = codenamebase.substring (0, idx);

            return codenamebase;
        }

         /** Getter for property codename.
         * @return Value of property codename.
         */
        String getCodename() {
            return codename;
        }
       
        /** Setter for property codename.
         * @param codename New value of property codename.
         */
        void setCodename(String codename) {
            this.codename = codename;
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
        
        void addVersion( Version version ) {
            versions = new ArrayList<>();
            versions.add( version );
        }

        void setVersion( Version version ) {
            versions = new ArrayList<>();
            versions.add( version );
        }
        
        void removeLocalized( String locale ) {
            for(Version ver: versions) {
                ver.removeLocalized( locale );
            }
        }
    }
    
    public class Version extends Object {        
        
        /** Holds value of property version. */
        private String version;
        
        /** Holds value of property origin. */
        private String origin;
        
        /** Holds value of property last. */
        private boolean last;
        
        /** Holds value of property install_time. */
        private long install_time = 0;
        
        /** Holds value of property files. */
        private List<ModuleFile> files = new ArrayList<>();
        
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
        void setFiles(List<ModuleFile> files) {
            this.files = files;
        }
        
        void addFile( ModuleFile file ) {
            files.add( file );
        }
        
        public void addFileWithCrc( String filename, String crc ) {
            ModuleFile file = new ModuleFile();
            file.setName( filename );
            file.setCrc( crc);
            files.add( file );
        }
        
        public void removeLocalized( String locale ) {
            List<ModuleFile> newFiles = new ArrayList<>();
            for (ModuleFile file : files) {
                if (file.getName().indexOf("_" + locale + ".") == -1 // NOI18N
                        && file.getName().indexOf("_" + locale + "/") == -1 // NOI18N
                        && !file.getName().endsWith("_" + locale) ) // NOI18N
                    newFiles.add ( file );
            }
            files = newFiles;
            
        }

        void addFileForRoot(File file) throws IOException {
            CRC32 crc = crcForFile(file);
            if (!file.getPath().startsWith(nbPath)) {
                throw new BuildException("File " + file + " needs to be under " + nbPath);
            }
            String rel = file.getPath().substring(nbPath.length()).replace(File.separatorChar, '/');
            if (rel.startsWith("/")) {
                rel = rel.substring(1);
            }
            addFileWithCrc(rel, "" + crc.getValue());
        }
    }
    
    class ModuleFile extends Object {        
        
        /** Holds value of property name. */
        private String name;
        
        /** Holds value of property crc. */
        private String crc;
        
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
            this.name = name.replace(File.separatorChar,'/');
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
        
    }

}
