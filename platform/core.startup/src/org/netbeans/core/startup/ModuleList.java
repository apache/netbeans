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

package org.netbeans.core.startup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.DuplicateException;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.Stamps;
import org.netbeans.Util;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.BaseUtilities;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakSet;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** Class responsible for maintaining the list of modules in the IDE persistently.
 * This class understands the "module status" XML format, and the list of modules
 * present in the Modules/ folder. And it can keep track of module histories.
 * Methods must be called from within appropriate mutex access.
 * @author Jesse Glick
 */
final class ModuleList implements Stamps.Updater {
    static final RequestProcessor RP = new RequestProcessor("Module List Updates"); // NOI18N
    /** The DTD for a module status. */
    public static final String PUBLIC_ID = "-//NetBeans//DTD Module Status 1.0//EN"; // NOI18N
    public static final String SYSTEM_ID = "http://www.netbeans.org/dtds/module-status-1_0.dtd"; // NOI18N
    
    /** Whether to validate module XML files.
     * Safer; only slows down startup in case quickie parse of XML statuses fails for some reason.
     */
    private static final boolean VALIDATE_XML = true;

    private static final Logger LOG = Logger.getLogger(ModuleList.class.getName());

    /** associated module manager */
    private final ModuleManager mgr;
    /** Modules/ folder containing XML data */
    private final FileObject folder;
    /** to fire events with */
    private final Events ev;
    /** map from code name (base)s to statuses of modules on disk */
    private final Map<String,DiskStatus> statuses = new HashMap<String,DiskStatus>(100);
    /** whether the initial round has been triggered or not */
    private boolean triggered = false;
    /** listener for changes in modules, etc.; see comment on class Listener */
    private final Listener listener = new Listener();
    private FileChangeListener weakListener;
    /** atomic actions I have used to change Modules/*.xml */
    private final Set<FileSystem.AtomicAction> myAtomicActions = Collections.<FileSystem.AtomicAction>synchronizedSet(new WeakSet<FileSystem.AtomicAction>(100));
    
    /** Create the list manager.
     * @param mgr the module manager which will actually control the modules at runtime
     * @param folder the Modules/ folder on the system file system to scan/write
     * @param ev the event logger
     */
    public ModuleList(ModuleManager mgr, FileObject folder, Events ev) {
        this.mgr = mgr;
        this.folder = folder;
        this.ev = ev;
        LOG.fine("ModuleList created, storage in " + folder);
    }
    
    /** Read an initial list of modules from disk according to their stored settings.
     * Just reads the XML files in the Modules/ directory, and adds those to
     * the manager's list of modules. Errors are handled internally.
     * Note that the modules encountered are not turned on at this point even if
     * the XML says they should be; but they are added to the list of modules to
     * enable as needed. All discovered modules are returned.
     * Write mutex only.
     */
    public Set<Module> readInitial() {
        ev.log(Events.START_READ);
        final Set<Module> read = new HashSet<Module>();
        try {
            folder.getFileSystem().runAtomicAction(new ReadInitial(read));
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
        return read;
    }
    
    final Module createModule(
        File jarFile, ModuleHistory hist, boolean reloadable, boolean autoload, 
        boolean eager, Integer startLevel
    ) throws IOException {
        Module m;
        try {
            if (startLevel != null) {
                m = mgr.createBundle(jarFile, hist, reloadable, autoload, eager, startLevel);
            } else {
                m = mgr.create(jarFile, hist, reloadable, autoload, eager);
            }
        } catch (DuplicateException dupe) {
            // XXX should this be tolerated somehow? In case the original is
            // in fact scheduled for deletion anyway?
            throw new IOException(dupe);
        }
        return m;
    }
    
    /**
     * Try to find a module JAR by an XML-supplied name.
     * @param jar the JAR name (relative to an install dir, or a full path)
     * @param name code name base of the module JAR
     * @return an actual JAR file
     * @throws FileNotFoundException if no such JAR file could be found on disk
     * @throws IOException if something else was wrong
     */
    private File findJarByName(String jar, String name) throws IOException {
        File f = new File(jar);
        if (f.isAbsolute()) {
            if (!f.isFile()) throw new FileNotFoundException(f.getAbsolutePath());
            return f;
        } else {
            Set<File> jars = InstalledFileLocator.getDefault().locateAll(jar, name, false);
            if (jars.isEmpty()) {
                throw new FileNotFoundException(jar);
            } else if (jars.size() == 1 || Boolean.getBoolean("org.netbeans.core.startup.ModuleList.firstModuleJarWins")) {
                return jars.iterator().next();
            } else {
                // Pick the newest one available.
                int major = -1;
                SpecificationVersion spec = null;
                File newest = null;
                for (File candidate : jars) {
                    int candidateMajor = -1;
                    SpecificationVersion candidateSpec = null;
                    JarFile jf = new JarFile(candidate);
                    try {
                        java.util.jar.Attributes attr = jf.getManifest().getMainAttributes();
                        String codename = attr.getValue("OpenIDE-Module");
                        if (codename != null) {
                            int slash = codename.lastIndexOf('/');
                            if (slash != -1) {
                                candidateMajor = Integer.parseInt(codename.substring(slash + 1));
                            }
                        }
                        String sv = attr.getValue("OpenIDE-Module-Specification-Version");
                        if (sv != null) {
                            candidateSpec = new SpecificationVersion(sv);
                        }
                    } finally {
                        jf.close();
                    }
                    if (newest == null || candidateMajor > major || (spec != null && candidateSpec != null && candidateSpec.compareTo(spec) > 0)) {
                        newest = candidate;
                        major = candidateMajor;
                        spec = candidateSpec;
                    }
                }
                return newest;
            }
        }
    }
    
    /** Actually go ahead and enable modules which were queued up by
     * reading methods. Should be done after as many modules
     * are collected as possible, in case they have odd mutual
     * dependencies. Also begins listening to further changes.
     * Pass in a list of boot modules which you would
     * like to also try to enable now.
     */
    public void trigger(Set<Module> boot) {
        ev.log(Events.PERF_START, "ModuleList.trigger"); // NOI18N
        if (triggered) throw new IllegalStateException("Duplicate call to trigger()"); // NOI18N
        Set<Module> maybeEnable = new HashSet<Module>(boot);
        for (DiskStatus status: statuses.values()) {
            if (status.pendingInstall) {
                // We are going to try to turn it on...
                status.pendingInstall = false;
                Module m = status.module;
                if (m.isEnabled() || m.isAutoload() || m.isEager()) throw new IllegalStateException();
                maybeEnable.add(m);
            }
        }
        ev.log(Events.PERF_TICK, "modules to enable prepared"); // NOI18N
	
        if (! maybeEnable.isEmpty()) {
            ev.log(Events.START_AUTO_RESTORE, maybeEnable);
            installNew(maybeEnable);
            ev.log(Events.FINISH_AUTO_RESTORE, maybeEnable);
        }
        LOG.fine("ModuleList.trigger: enabled new modules, flushing changes...");
        triggered = true;
        flushInitial();
        ev.log(Events.PERF_END, "ModuleList.trigger"); // NOI18N
    }
    // XXX is this method still needed? rethink...
    private void installNew(Set<Module> modules) {
        if (modules.isEmpty()) {
            return;
        }
        ev.log(Events.PERF_START, "ModuleList.installNew"); // NOI18N
        // First suppress all autoloads.
        Iterator<Module> it = modules.iterator();
        while (it.hasNext()) {
            Module m = it.next();
            if (m.isAutoload() || m.isEager()) {
                it.remove();
            } else if (m.isEnabled()) {
                // Can happen in obscure circumstances: old module A
                // now exists again but with dependency on new module B,
                // and a complete build was not done for A+B, so they have
                // no existing Modules/ *.xml. In such a case B will already
                // have been turned on when restoring A; harmless to remove
                // it from the list here.
                LOG.fine("#17295 fix active for " + m.getCodeNameBase());
                it.remove();
            } else if (!m.isValid()) {
                // Again can also happen if the user upgrades from one version
                // of a module to another. In this case ModuleList correctly removed
                // the old dead module from the manager's list, however it is still
                // in the set of modules to restore.
                LOG.fine("#17471 fix active for " + m.getCodeNameBase());
                it.remove();
            }
        }
        List<Module> toEnable = mgr.simulateEnable(modules);
	for (Module m: toEnable) {
            if (m.isAutoload() || m.isEager()) {
                continue;
            }
            // Quietly turn on others as well:
            if (! modules.contains(m)) {
                modules.add(m);
            }
        }
        Set<Module> missing = new HashSet<Module>(modules);
        missing.removeAll(toEnable);
        if (! missing.isEmpty()) {
            // Include also problematic autoloads and so on needed by these modules.
            Util.transitiveClosureModuleDependencies(mgr, missing);
            it = missing.iterator();
            while (it.hasNext()) {
                Module m = it.next();
                if (m.getProblems().isEmpty()) {
                    it.remove();
                }
            }
            ev.log(Events.FAILED_INSTALL_NEW, missing);
            modules.removeAll(missing);
        }
        try {
            mgr.enable(modules);
        } catch (InvalidException ie) {
            LOG.log(Level.INFO, null, ie);
            Module bad = ie.getModule();
            if (bad == null) throw new IllegalStateException();
            Set<Module> affectedModules = mgr.getModuleInterdependencies(bad, true, true, true);
            ev.log(Events.FAILED_INSTALL_NEW_UNEXPECTED, bad, affectedModules, ie);
            modules.removeAll (affectedModules);
            // Try again without it. Note that some other dependent modules might
            // then be in the missing list for the second round.
            installNew(modules);
        }
        ev.log(Events.PERF_END, "ModuleList.installNew"); // NOI18N
    }
    
    /** Read an XML file using an XMLReader and parse into a map of properties.
     * One distinguished property 'name' is the code name base
     * and is taken from the root element. Others are taken
     * from the param elements.
     * Properties are of type String, Boolean, Integer, or SpecificationVersion
     * according to the property name.
     * @param is the input stream
     * @param reader the XML reader to use to parse; may be null
     * @return a map of named properties to values of various types
     */
    private Map<String,Object> readStatus(InputSource is, XMLReader reader) throws IOException, SAXException {
        if (reader == null) {
            reader = XMLUtil.createXMLReader(VALIDATE_XML);
            reader.setEntityResolver(listener);
            reader.setErrorHandler(listener);
        }
        final Map<String,Object> m = new HashMap<String,Object>();

        DefaultHandler handler = new DefaultHandler() {
            private String modName;
            private String paramName;
            private StringBuffer data = new StringBuffer();
	    
            public @Override void startElement(String uri,
                                     String localname,
                                     String qname,
                                     Attributes attrs) throws SAXException {
                if ("module".equals(qname) ) { // NOI18N
                    modName = attrs.getValue("name"); // NOI18N
                    if( modName == null )
                        throw new SAXException("No module name"); // NOI18N
                    m.put("name", modName.intern()); // NOI18N
                }
                else if (modName != null && "param".equals(qname)) { // NOI18N
                    paramName = attrs.getValue("name");
                    if( paramName == null ) {
                        throw new SAXException("No param name"); // NOI18N
                    }
                    paramName = paramName.intern();
                    data.setLength(0);
                }
            }
	    
            public @Override void characters(char[] ch, int start, int len) {
                if(modName != null  && paramName != null)
                    data.append( ch, start, len );
            }
            
            public @Override void endElement (String uri, String localname, String qname)
                throws SAXException
            {
                if ("param".equals(qname)) { // NOI18N
                    if (modName != null && paramName != null) {
                        if (data.length() == 0)
                            throw new SAXException("No text contents in " + paramName + " of " + modName); // NOI18N
                        
                        try {
                            m.put(paramName, processStatusParam(paramName, data.toString()));
                        } catch (NumberFormatException nfe) {
                            // From either Integer or SpecificationVersion constructors.
                            throw (SAXException) new SAXException(nfe.toString()).initCause(nfe);
                        }

                        data.setLength(0);
                        paramName = null;
                    }
                }
                else if ("module".equals(qname)) { // NOI18N
                    modName = null;
                }
            }
        };
        
        reader.setContentHandler(handler);
        reader.parse(is);

        sanityCheckStatus(m);

        return m;
    }
    
    /** Parse a param value according to a natural type.
     * @param k the param name (must be interned!)
     * @param v the raw string value from XML
     * @return some parsed value suitable for the status map
     */
    private Object processStatusParam(String k, String v) throws NumberFormatException {
        if (k == "enabled" // NOI18N
                   || k == "autoload" // NOI18N
                   || k == "eager" // NOI18N
                   || k == "reloadable" // NOI18N
                   ) {
            return Boolean.valueOf(v);
        } else {
            if (k == "startlevel") { // NOI18N 
                return Integer.valueOf(v);
            }
            // Other properties are of type String.
            // Intern the smaller ones which are likely to be repeated somewhere.
            if (v.length() < 100) v = v.intern();
            return v;
        }
    }
    
    /** Just checks that all the right stuff is there.
     */
    private void sanityCheckStatus(Map<String,Object> m) throws IOException {
        String jar = (String) m.get("jar"); // NOI18N
        if (jar == null) {
            throw new IOException("Must define jar param"); // NOI18N
        }
        if (Boolean.TRUE.equals(m.get("autoload")) && m.get("enabled") != null) { // NOI18N
            throw new IOException("Autoload " + jar + " cannot specify enablement");
        }
        if (Boolean.TRUE.equals(m.get("eager")) && m.get("enabled") != null) { // NOI18N
            throw new IOException("Eager " + jar + " cannot specify enablement");
        }
    }

    // Encoding irrelevant for these getBytes() calls: all are ASCII...
    // (unless someone has their system encoding set to UCS-16!)
    private static final byte[] MODULE_XML_INTRO = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n<module name=\"".getBytes(); // NOI18N
//    private static final byte[] MODULE_XML_DIV1 = ">\n    <param name=\"".getBytes(); // NOI18N
    private static final byte[] MODULE_XML_INTRO_END = ">\n".getBytes(); // NOI18N
    private static final byte[] MODULE_XML_DIV2 = "   <param name=\"".getBytes(); // NOI18N
    private static final byte[] MODULE_XML_DIV3 = "/param>\n".getBytes(); // NOI18N
    private static final byte[] MODULE_XML_END = "/module>\n".getBytes(); // NOI18N
    /** Just like {@link #readStatus(InputSource,XMLReader)} but avoids using an XML parser.
     * If it does not manage to parse it this way, it returns null, in which case
     * you have to use a real parser.
     * @see "#26786"
     */
    private Map<String, Object> readStatus(InputStream is, boolean checkEOF) throws IOException {
        PushbackInputStream pbis = new PushbackInputStream(is, 1);
        Map<String,Object> m = new HashMap<String,Object>(15);
        if (!expect(pbis, MODULE_XML_INTRO)) {
            LOG.fine("Could not read intro");
            return null;
        }
        String name = readTo(pbis, '"');
        if (name == null) {
            LOG.fine("Could not read code name base");
            return null;
        }
        m.put("name", name.intern()); // NOI18N
        if (!expect(pbis, MODULE_XML_INTRO_END)) {
            LOG.fine("Could not read stuff after cnb");
            return null;
        }
        // Now we have <param>s some number of times, finally </module>.
    PARSE:
        while (true) {
            int c = pbis.read();
            switch (c) {
            case ' ':
                // <param>
                if (!expect(pbis, MODULE_XML_DIV2)) {
                    LOG.fine("Could not read up to param");
                    return null;
                }
                String k = readTo(pbis, '"');
                if (k == null) {
                    LOG.fine("Could not read param");
                    return null;
                }
                k = k.intern();
                if (pbis.read() != '>') {
                    LOG.fine("No > at end of <param> " + k);
                    return null;
                }
                String v = readTo(pbis, '<');
                if (v == null) {
                    LOG.fine("Could not read value of " + k);
                    return null;
                }
                if (!expect(pbis, MODULE_XML_DIV3)) {
                    LOG.fine("Could not read end of param " + k);
                    return null;
                }
                try {
                    m.put(k, processStatusParam(k, v));
                } catch (NumberFormatException nfe) {
                    LOG.fine("Number misparse: " + nfe);
                    return null;
                }
                break;
            case '<':
                // </module>
                if (!expect(pbis, MODULE_XML_END)) {
                    LOG.fine("Strange ending");
                    return null;
                }
                if (!checkEOF) {
                    break PARSE;
                }
                if (pbis.read() != -1) {
                    LOG.fine("Trailing garbage");
                    return null;
                }
                // Success!
                break PARSE;
            default:
                LOG.fine("Strange stuff after <param>s: " + c);
                return null;
            }
        }
        sanityCheckStatus(m);
        return m;
    }
    
    /** Read some stuff from a stream and skip over it.
     * Newline conventions are normalized to Unix \n.
     * @return true upon success, false if stream contained something else
     */
    private boolean expect(PushbackInputStream is, byte[] stuff) throws IOException {
        int len = stuff.length;
        boolean inNewline = false;
        for (int i = 0; i < len; ) {
            int c = is.read();
            if (c == 10 || c == 13) {
                // Normalize: s/[\r\n]+/\n/g
                if (inNewline) {
                    continue;
                } else {
                    inNewline = true;
                    c = 10;
                }
            } else {
                inNewline = false;
            }
            if (c != stuff[i++]) {
                return false;
            }
        }
        if (stuff[len - 1] == 10) {
            // Expecting something ending in a \n - so we have to
            // read any further \r or \n and discard.
            int c = is.read();
            if (c != -1 && c != 10 && c != 13) {
                // Got some non-newline character, push it back!
                is.unread(c);
            }
        }
        return true;
    }
    /** Read a maximal string until delim is encountered (which will be removed from stream).
     * This impl reads only ASCII, for speed.
     * Newline conventions are normalized to Unix \n.
     * @return the read string, or null if the delim is not encountered before EOF.
     */
    private String readTo(InputStream is, char delim) throws IOException {
        if (delim == 10) {
            // Not implemented - stream might have "foo\r\n" and we would
            // return "foo" and leave "\n" in the stream.
            throw new IOException("Not implemented"); // NOI18N
        }
        CharArrayWriter caw = new CharArrayWriter(100);
        boolean inNewline = false;
        while (true) {
            int c = is.read();
            if (c == -1) return null;
            if (c > 126) return null;
            if (c == 10 || c == 13) {
                // Normalize: s/[\r\n]+/\n/g
                if (inNewline) {
                    continue;
                } else {
                    inNewline = true;
                    c = 10;
                }
            } else if (c < 32 && c != 9) {
                // Random control character!
                return null;
            } else {
                inNewline = false;
            }
            if (c == delim) {
                return caw.toString();
            } else {
                caw.write(c);
            }
        }
    }

    final Map<String,Map<String,Object>> readCache() {
        InputStream is = Stamps.getModulesJARs().asStream("all-modules.dat"); // NOI18N
        if (is == null) {
            // schedule write for later
            writeCache();
            return null;
        }
        LOG.log(Level.FINEST, "Reading cache all-modules.dat");
        try {
            ObjectInputStream ois = new ObjectInputStream(is);

            Map<String,Map<String,Object>> ret = new HashMap<String, Map<String, Object>>(1333);
            while (is.available() > 0) {
                Map<String, Object> prop = readStatus(ois, false);
                if (prop == null) {
                    LOG.log(Level.CONFIG, "Cache is invalid all-modules.dat");
                    return null;
                }
                Set<?> deps;
                try {
                    deps = (Set<?>) ois.readObject();
                } catch (ClassNotFoundException ex) {
                    throw new IOException(ex);
                }
                prop.put("deps", deps);
                String cnb = (String)prop.get("name"); // NOI18N
                ret.put(cnb, prop);
            }


            is.close();
            return ret;
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Cannot read cache", ex);
            writeCache();
            return null;
        }
    }

    final void writeCache() {
        Stamps.getModulesJARs().scheduleSave(this, "all-modules.dat", false);
    }
    
    @Override
    public void cacheReady() {
    }

    @Override
    public void flushCaches(DataOutputStream os) throws IOException {
        ObjectOutputStream oss = new ObjectOutputStream(os);
        for (Module m : mgr.getModules()) {
            if (m.isFixed()) {
                continue;
            }
            Map<String, Object> prop = computeProperties(m);
            writeStatus(prop, oss);
            oss.writeObject(m.getDependencies());
        }
    }
    
    /** Write a module's status to disk in the form of an XML file.
     * The map of parameters must contain one named 'name' with the code
     * name base of the module.
     */
    private void writeStatus(Map<String, Object> m, OutputStream os) throws IOException {
        String codeName = (String)m.get("name"); // NOI18N
        if (codeName == null)
            throw new IllegalArgumentException("no code name present"); // NOI18N

        Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); // NOI18N
        w.write("<!DOCTYPE module PUBLIC \""); // NOI18N
        w.write(PUBLIC_ID);
        w.write("\"\n                        \""); // NOI18N
        w.write(SYSTEM_ID);
        w.write("\">\n"); // NOI18N
        w.write("<module name=\""); // NOI18N
        w.write(XMLUtil.toAttributeValue(codeName)); // NOI18N
        w.write("\">\n");       // NOI18N

        // Use TreeMap to sort the keys by name; since the module status files might
        // be version-controlled we want to avoid gratuitous format changes.
        for (Map.Entry<String, Object> entry: new TreeMap<String, Object>(m).entrySet()) {
            String name = entry.getKey();
            if (
                name.equals("name") || // NOI18N
                name.equals("deps") // NOI18N
            ) {
                // Skip this one, it is a pseudo-param.
                continue;
            }
            
            Object val = entry.getValue();

            w.write("    <param name=\""); // NOI18N
            w.write(XMLUtil.toAttributeValue(name)); // NOI18N
            w.write("\">");     // NOI18N
            w.write(XMLUtil.toElementContent(val.toString()));
            w.write("</param>\n"); // NOI18N
        }

        w.write("</module>\n"); // NOI18N
        w.flush();
    }
    
    /** Write information about a module out to disk.
     * If the old status is given as null, this is a newly
     * added module; create an appropriate status and return it.
     * Else update the existing status and return it (it is
     * assumed properties are already updated).
     * Should write the XML and create/rewrite/delete the serialized
     * installer file as needed.
     */
    private DiskStatus writeOut(Module m, DiskStatus old) throws IOException {
        final DiskStatus nue;
        if (old == null) {
            nue = new DiskStatus();
            nue.module = m;
            nue.setDiskProps(computeProperties(m));
        } else {
            nue = old;
        }
        FileSystem.AtomicAction aa = new FileSystem.AtomicAction() {
            public void run() throws IOException {
                if (nue.file == null) {
                    nue.file = FileUtil.createData(folder, ((String)nue.diskProps.get("name")).replace('.', '-') + ".xml"); // NOI18N
                } else {
                    // Just verify that no one else touched it since we last did.
                    if (/*nue.lastApprovedChange != nue.file.lastModified().getTime()*/nue.dirty) {
                        // Oops, something is wrong. #156764 - log at lower level.
                        LOG.log(Level.INFO, null, new IOException("Will not clobber external changes in " + nue.file));
                        return;
                    }
                }
                LOG.fine("ModuleList: (re)writing " + nue.file);
                FileLock lock = nue.file.lock();
                try {
                    OutputStream os = nue.file.getOutputStream(lock);
                    try {
                        writeStatus(nue.diskProps, os);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
                //nue.lastApprovedChange = nue.file.lastModified().getTime();
            }
        };
        myAtomicActions.add(aa);
        folder.getFileSystem().runAtomicAction(aa);
        return nue;
    }
    
    /** Delete a module from disk.
     */
    private void deleteFromDisk(final Module m, final DiskStatus status) throws IOException {
        final String nameDashes = m.getCodeNameBase().replace('.', '-'); // NOI18N
        //final long expectedTime = status.lastApprovedChange;
        FileSystem.AtomicAction aa = new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject xml = folder.getFileObject(nameDashes, "xml"); // NOI18N
                if (xml == null) {
                    // Could be that the XML was already deleted externally, etc.
                    LOG.fine("ModuleList: " + m + "'s XML already gone from disk");
                    return;
                }
                //if (xml == null) throw new IOException("No such XML file: " + nameDashes + ".xml"); // NOI18N
                if (status.dirty) {
                    // Someone wrote to the file since we did. Don't delete it blindly!
                    // XXX should this throw an exception, or just warn??
                    throw new IOException("Unapproved external change to " + xml); // NOI18N
                }
                LOG.fine("ModuleList: deleting " + xml);
                /*
                if (xml.lastModified().getTime() != expectedTime) {
                    // Someone wrote to the file since we did. Don't delete it blindly!
                    throw new IOException("Unapproved external change to " + xml); // NOI18N
                }
                 */
                xml.delete();
                FileObject ser = folder.getFileObject(nameDashes, "ser"); // NOI18N
                if (ser != null) {
                    LOG.fine("(and also " + ser + ")");
                    ser.delete();
                }
            }
        };
        myAtomicActions.add(aa);
        folder.getFileSystem().runAtomicAction(aa);
    }
    
    /** Flush the initial state of the module installer after startup to disk.
     * This means:
     * 1. Find all modules in the manager.
     * 2. Anything for which we have no status, write out its XML now
     *    and create a status object for it.
     * 3. Anything for which we have a status, compare the status we
     *    have to its current state (don't forget the installer
     *    serialization state--if this is nonnull, that counts as an
     *    automatic change because it means the module was loaded and
     *    needed to store something).
     * 4. For any changes found in 3., write out new XML (and if
     *    there is any installer state, a new installer ser).
     * 5. Attach listeners to the manager and all modules to catch further
     *    changes in the system so they may be flushed.
     * We could in principle start listening right after readInitial()
     * but it should be more efficient to wait and see what has really
     * changed. Also, some XML may say that a module is enabled, and in
     * fact trigger() was not able to turn it on. In that case, this will
     * show up as a change in step 3. and we will rewrite it as disabled.
     * Called within write mutex by trigger().
     */
    private void flushInitial() {
        LOG.fine("Flushing initial module list...");
        // Find all modules for which we have status already. Treat
        // them as possibly changed, and attach listeners.
        for (Module m : mgr.getModules()) {
            DiskStatus status = statuses.get(m.getCodeNameBase());
            if (status != null) {
                 moduleChanged(m, status);
                m.addPropertyChangeListener(listener);
            }
        }
        // Now find all new and deleted modules.
        moduleListChanged();
        // And listener for new or deleted modules.
        mgr.addPropertyChangeListener(listener);
    }
    
    /** Does the real work when the list of modules changes.
     * Finds newly added modules, creates XML and status for
     * them and begins listening for changes; finds deleted
     * modules, removes their listener, XML, and status.
     * May be called within read or write mutex; since it
     * could be in the read mutex, synchronize (on statuses).
     */
    final void moduleListChanged() {
        synchronized (statuses) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("ModuleList: moduleListChanged; statuses=" + statuses);
            }
            // Newly added modules first.
            for (Module m : mgr.getModules()) {
                if (m.isFixed() || m.getJarFile() == null) {
                    // No way, we don't manage these.
                    continue;
                }
                final String name = m.getCodeNameBase();
                if (statuses.get(name) == null) {
                    // Yup, it's new. Write it out.
                    LOG.fine("moduleListChanged: added: " + m);
                    try {
                        statuses.put(name, writeOut(m, null));
                        m.addPropertyChangeListener(listener);
                    } catch (IOException ioe) {
                        LOG.log(Level.WARNING, null, ioe);
                        // XXX Now what? Keep it in our list or what??
                    }
                }
            }
            // Now deleted & recreated modules.
            Iterator<DiskStatus> it = statuses.values().iterator();
            while (it.hasNext()) {
                DiskStatus status = it.next();
                if (! status.module.isValid()) {
                    status.module.removePropertyChangeListener(listener);
                    Module nue = mgr.get(status.module.getCodeNameBase());
                    if (nue != null) {
                        // Deleted, but a new module with the same code name base
                        // was created (#5922 e.g.). So change the module reference
                        // in the status and write out any changes to disk.
                        LOG.fine("moduleListChanged: recreated: " + nue);
                        nue.addPropertyChangeListener(listener);
                        status.module = nue;
                        moduleChanged(nue, status);
                    } else {
                        // Newly deleted.
                        LOG.fine("moduleListChanged: deleted: " + status.module);
                        it.remove();
                        try {
                            deleteFromDisk(status.module, status);
                        } catch (IOException ioe) {
                            LOG.log(Level.WARNING, null, ioe);
                        }
                    }
                }
            }
        }
    }
    
    /** Does the real work when one module changes.
     * Compares old and new state and writes XML
     * (and perhaps serialized installer state) as needed.
     * May be called within read or write mutex; since it
     * could be in the read mutex, synchronize (on status).
     */
    private void moduleChanged(Module m, DiskStatus status) {
        synchronized (status) {
            LOG.log(Level.FINE, "moduleChanged: {0}", m);
            Map<String,Object> newProps = computeProperties(m);
            int cnt = 0;
            for (Map.Entry<String, Object> entry : status.diskProps.entrySet()) {
                if (entry.getKey().equals("deps")) { // NOI18N
                    continue;
                }
                Object snd = newProps.get(entry.getKey());
                if (!entry.getValue().equals(snd)) {
                    cnt = -1;
                    break;
                }
                cnt++;
            }
            if (cnt != newProps.size()) {
                if (LOG.isLoggable(Level.FINE)) {
                    Set<Map.Entry<String,Object>> changes = new HashSet<Map.Entry<String,Object>>(newProps.entrySet());
                    changes.removeAll(status.diskProps.entrySet());
                    LOG.fine("ModuleList: changes are " + changes);
                }
                // We need to write changes.
                status.setDiskProps(newProps);
                try {
                    writeOut(m, status);
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, null, ioe);
                    // XXX now what? continue to manage it anyway?
                }
                writeCache();
            }
        }
    }
    
    /** Compute what properties we would want to store in XML
     * for this module. I.e. 'name', 'reloadable', etc.
     */
    private Map<String,Object> computeProperties(Module m) {
        if (m.isFixed() || ! m.isValid()) throw new IllegalArgumentException("fixed or invalid: " + m); // NOI18N
        Map<String,Object> p = new HashMap<String,Object>();
        p.put("name", m.getCodeNameBase()); // NOI18N
        if (!m.isAutoload() && !m.isEager()) {
            p.put("enabled", m.isEnabled()); // NOI18N
        }
        p.put("autoload", m.isAutoload()); // NOI18N
        p.put("eager", m.isEager()); // NOI18N
        p.put("reloadable", m.isReloadable()); // NOI18N
        if (m.getStartLevel() > 0) {
            p.put("startlevel", m.getStartLevel()); // NOI18N
        }
        if (m.getHistory() instanceof ModuleHistory) {
            ModuleHistory hist = (ModuleHistory) m.getHistory();
            p.put("jar", hist.getJar()); // NOI18N
        }
        return p;
    }
    
    final void init() {
        weakListener = FileUtil.weakFileChangeListener(listener, folder);
        folder.getChildren();
        folder.addFileChangeListener(weakListener);
    }

    final void shutDown() {
        folder.removeFileChangeListener(weakListener);
    }
    /** Listener for changes in set of modules and various properties of individual modules.
     * Also serves as a strict error handler for XML parsing.
     * Also listens to changes in the Modules/ folder and processes them in req proc.
     */
    private final class Listener implements PropertyChangeListener, ErrorHandler, EntityResolver, FileChangeListener, Runnable {
        private final RequestProcessor.Task task;
        
        Listener() {
            task = RP.create(this);
        }
        
        // Property change coming from ModuleManager or some known Module.
        
        private boolean listening = true;
        public void propertyChange(PropertyChangeEvent evt) {
            if (! triggered) throw new IllegalStateException("Property change before trigger()"); // NOI18N
            // REMEMBER this is inside *read* mutex, it is forbidden to even attempt
            // to get write access synchronously here!
            String prop = evt.getPropertyName();
            Object src = evt.getSource();
            if (!listening) {
                // #27106: do not react to our own changes while we are making them
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("ModuleList: ignoring own change " + prop + " from " + src);
                }
                return;
            }
            if (ModuleManager.PROP_CLASS_LOADER.equals(prop) ||
                    ModuleManager.PROP_ENABLED_MODULES.equals(prop) ||
                    Module.PROP_CLASS_LOADER.equals(prop) ||
                    Module.PROP_PROBLEMS.equals(prop) ||
                    Module.PROP_VALID.equals(prop)) {
                // Properties we are not directly interested in, ignore.
                // Note that rather than paying attention to PROP_VALID
                // we simply deal with deletions when PROP_MODULES is fired.
                return;
            } else if (ModuleManager.PROP_MODULES.equals(prop)) {
                moduleListChanged();
            } else if (src instanceof Module) {
                // enabled, manifest, reloadable, possibly other stuff in the future
                Module m = (Module)src;
                if (! m.isValid()) {
                    // Skip it. We will get PROP_MODULES sometime anyway.
                    return;
                }
                DiskStatus status = statuses.get(m.getCodeNameBase());
                if (status == null) {
                    throw new IllegalStateException("Unknown module " + m + "; statuses=" + statuses); // NOI18N
                }
                if (status.pendingInstall && Module.PROP_ENABLED.equals(prop)) {
                    throw new IllegalStateException("Got PROP_ENABLED on " + m + " before trigger()"); // NOI18N
                }
                moduleChanged(m, status);
            } else {
                LOG.fine("Unexpected property change: " + evt + " prop=" + prop + " src=" + src);
            }
        }
        
        // SAX stuff.
        
        public void warning(SAXParseException e) throws SAXException {
            LOG.log(Level.WARNING, null, e);
        }
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
        public InputSource resolveEntity(String pubid, String sysid) throws SAXException, IOException {
            if (pubid.equals(PUBLIC_ID)) {
                if (VALIDATE_XML) {
                    // We certainly know where to get this from.
                    return new InputSource(ModuleList.class.getResource("module-status-1_0.dtd").toExternalForm()); // NOI18N
                } else {
                    // Not validating, don't load any DTD! Significantly faster.
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                }
            } else {
                // Otherwise try the standard places.
                return EntityCatalog.getDefault().resolveEntity(pubid, sysid);
            }
        }
        
        // Changes in Modules/ folder.
        
        public void fileDeleted(FileEvent ev) {
            if (isOurs(ev)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("ModuleList: got expected deletion " + ev);
                }
                return;
            }
            FileObject fo = ev.getFile();
            fileDeleted0(fo.getName(), fo.getExt()/*, ev.getTime()*/);
        }
        
        public void fileDataCreated(FileEvent ev) {
            if (isOurs(ev)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("ModuleList: got expected creation " + ev);
                }
                return;
            }
            FileObject fo = ev.getFile();
            fileCreated0(fo.getName(), fo.getExt()/*, ev.getTime()*/);
        }
        
        public void fileRenamed(FileRenameEvent ev) {
            if (isOurs(ev)) {
                throw new IllegalStateException("I don't rename anything! " + ev); // NOI18N
            }
            FileObject fo = ev.getFile();
            fileDeleted0(ev.getName(), ev.getExt()/*, ev.getTime()*/);
            fileCreated0(fo.getName(), fo.getExt()/*, ev.getTime()*/);
        }

        private void fileCreated0(String name, String ext/*, long time*/) {
            if ("xml".equals(ext)) { // NOI18N
                String codenamebase = name.replace('-', '.');
                DiskStatus status = statuses.get(codenamebase);
                LOG.fine("ModuleList: outside file creation event for " + codenamebase);
                if (status != null) {
                    // XXX should this really happen??
                    status.dirty = true;
                }
                runme();
            } else if ("ser".equals(ext)) { // NOI18N
                // XXX handle newly added installers?? or not
            } // else ignore
        }
        
        private void fileDeleted0(String name, String ext/*, long time*/) {
            if ("xml".equals(ext)) { // NOI18N
                // Removed module.
                String codenamebase = name.replace('-', '.');
                DiskStatus status = statuses.get(codenamebase);
                LOG.fine("ModuleList: outside file deletion event for " + codenamebase);
                if (status != null) {
                    // XXX should this ever happen?
                    status.dirty = true;
                }
                runme();
            } else if ("ser".equals(ext)) { // NOI18N
                // XXX handle newly deleted installers?? or not
            } // else ignore
        }
        
        public void fileChanged(FileEvent ev) {
            if (isOurs(ev)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("ModuleList: got expected modification " + ev);
                }
                return;
            }
            FileObject fo = ev.getFile();
            String name = fo.getName();
            String ext = fo.getExt();
            if ("xml".equals(ext)) { // NOI18N
                // Changed module.
                String codenamebase = name.replace('-', '.');
                DiskStatus status = statuses.get(codenamebase);
                LOG.fine("ModuleList: outside file modification event for " + codenamebase + ": " + ev);
                if (status != null) {
                    status.dirty = true;
                } else {
                    // XXX should this ever happen?
                }
                runme();
            } else if ("ser".equals(ext)) { // NOI18N
                // XXX handle changes of installers?? or not
            } // else ignore
        }
        
        public void fileFolderCreated(FileEvent ev) {
            // ignore
        }
        public void fileAttributeChanged(FileAttributeEvent ev) {
            // ignore
        }
        
        /** Check if a given file event in the Modules/ folder was a result
         * of our own manipulations, as opposed to some other code (or polled
         * refresh) manipulating one of these XML files. See #15573.
         */
        private boolean isOurs(FileEvent ev) {
            for (FileSystem.AtomicAction action : myAtomicActions) {
                if (ev.firedFrom(action)) {
                    return true;
                }
            }
            return false;
        }
        
        // Dealing with changes in Modules/ folder and processing them.
        
        private void runme() {
            task.schedule(100);
        }
        @Override
        public void run() {
            LOG.fine("ModuleList: will process outstanding external XML changes");
            mgr.mutexPrivileged().enterWriteAccess();
            try {
                folder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        // 1. For any dirty XML for which status exists but reloadable differs from XML: change.
                        // 2. For any XML for which we have no status: create & create status, as disabled.
                        // 3. For all dirty XML which says enabled but status says disabled: batch-enable as possible.
                        //    (Where not possible, mark disabled in XML??)
                        // 4. For all dirty XML which says disabled but status says enabled: batch-disable plus others.
                        // 5. For all status for which no XML exists: batch-disable plus others, then delete.
                        // 6. For any dirty XML for which jar/autoload/eager/release/specversion differs from
                        //    actual state of module: warn but do nothing.
                        // 7. For now, ignore any changes in *.ser.
                        // 8. For any dirty XML for which status now exists: replace diskProps with contents of XML.
                        // 9. Mark all statuses clean.
                        // Code name to module XMLs found on disk:
                        Map<String,FileObject> xmlfiles = prepareXMLFiles();
                        // Code name to properties for dirty XML or XML sans status only.
                        Map<String,Map<String,Object>> dirtyprops = prepareDirtyProps(xmlfiles);
                        // #27106: do not listen to changes we ourselves produce.
                        // It only matters if statuses has not been updated before
                        // the changes are fired.
                        listening = false;
                        try {
                            stepCheckReloadable(dirtyprops);
                            stepCreate(xmlfiles, dirtyprops);
                            stepEnable(dirtyprops);
                            stepDisable(dirtyprops);
                            stepDelete(xmlfiles);
                            stepCheckMisc(dirtyprops);
                            stepCheckSer(xmlfiles, dirtyprops);
                        } finally {
                            listening = true;
                            stepUpdateProps(dirtyprops);
                            stepMarkClean();
                        }
                    }
                });
                LOG.fine("ModuleList: finished processing outstanding external XML changes");
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }
        // All the steps called from the run() method to handle disk changes:
        private Map<String,FileObject> prepareXMLFiles() {
            LOG.fine("ModuleList: prepareXMLFiles");
            Map<String,FileObject> xmlfiles = new HashMap<String,FileObject>(100);
            FileObject[] kids = folder.getChildren();
            for (int i = 0; i < kids.length; i++) {
                if (kids[i].hasExt("xml")) { // NOI18N
                    xmlfiles.put(kids[i].getName().replace('-', '.'), kids[i]);
                }
            }
            return xmlfiles;
        }
        private Map<String,Map<String,Object>> prepareDirtyProps(Map<String,FileObject> xmlfiles) throws IOException {
            LOG.fine("ModuleList: prepareDirtyProps");
            Map<String,Map<String,Object>> dirtyprops = new HashMap<String,Map<String,Object>>(100);
            for (Map.Entry<String,FileObject> entry : xmlfiles.entrySet()) {
                String cnb = entry.getKey();
                DiskStatus status = statuses.get(cnb);
                if (status == null || status.dirty) {
                    FileObject xmlfile = entry.getValue();
                    if (xmlfile == null || ! xmlfile.canRead ()) {
                        continue;
                    }
                    Random eth = null;
                    for (int repeats = 0; ; repeats++) {
                        InputStream is = xmlfile.getInputStream();
                        try {
                            InputSource src = new InputSource(is);
                            src.setSystemId(xmlfile.toURL().toString());
                            try {
                                dirtyprops.put(cnb, readStatus(src, null));
                            } catch (SAXException saxe) {
                                final String msg = "Parse error:\n---%<--- " + xmlfile.getPath() + "\n" +
                                    xmlfile.asText("UTF-8") + "\n---%<---\ngot: " + saxe;
                                if (repeats < 10) {
                                    int wait;
                                    if (eth == null) {
                                        eth = new Random();
                                    }
                                    wait = eth.nextInt(90) + 10;
                                    LOG.warning(msg);
                                    LOG.log(Level.INFO, "Retry: {0} after waiting {1}", new Object[] { repeats, wait });
                                    try {
                                        Thread.sleep(wait);
                                    } catch (InterruptedException ignore) {
                                        // not that important
                                    }
                                    continue;
                                }
                                throw new IOException(msg, saxe);
                            }
                        } finally {
                            is.close();
                        }
                        break;
                    }
                }
            }
            return dirtyprops;
        }
        private void stepCheckReloadable(Map<String,Map<String,Object>> dirtyprops) {
            LOG.fine("ModuleList: stepCheckReloadable");
            for (Map.Entry<String,Map<String,Object>> entry : dirtyprops.entrySet()) {
                String cnb = entry.getKey();
                DiskStatus status = statuses.get(cnb);
                if (status != null) {
                    Map<String,Object> props = entry.getValue();
                    Boolean diskReloadableB = (Boolean)props.get("reloadable"); // NOI18N
                    boolean diskReloadable = (diskReloadableB != null ? diskReloadableB.booleanValue() : false);
                    boolean memReloadable = status.module.isReloadable();
                    if (memReloadable != diskReloadable) {
                        LOG.fine("Disk change in reloadable for " + cnb + " from " + memReloadable + " to " + diskReloadable);
                        status.module.setReloadable(diskReloadable);
                    }
                }
            }
        }
        private void stepCreate(Map<String,FileObject> xmlfiles, Map<String,Map<String,Object>> dirtyprops) throws IOException {
            LOG.fine("ModuleList: stepCreate");
            for (Map.Entry<String,FileObject> entry : xmlfiles.entrySet()) {
                String cnb = entry.getKey();
                if (! statuses.containsKey(cnb)) {
                    FileObject xmlfile = entry.getValue();
                    Map<String, Object> props = dirtyprops.get(cnb);
                    if (! cnb.equals(props.get("name"))) throw new IOException("Code name mismatch"); // NOI18N
                    String jar = (String)props.get("jar"); // NOI18N
                    File jarFile;
                    try {
                        jarFile = findJarByName(jar, cnb);
                    } catch (FileNotFoundException fnfe) {
                        final File file = new File(fnfe.getMessage());
                        ev.log(Events.MISSING_JAR_FILE, file, true);
                        final File p = file.getParentFile();
                        File[] arr = p.listFiles();
                        LOG.log(Level.FINE, "Content of {0} is:", p); // NOI18N
                        int cnt = 0;
                        if (arr != null) {
                            for (File f : arr) {
                                LOG.log(Level.FINE, "{0}. = {1}", new Object[] { ++cnt, f }); // NOI18N
                            }
                            LOG.log(Level.FINE, "There was {0} files", cnt); // NOI18N
                        } else {
                            LOG.fine("Directory does not exist"); // NOI18N
                        }
                        dirtyprops.remove(cnb); // #159001
                        continue;
                    }
                    Boolean reloadableB = (Boolean)props.get("reloadable"); // NOI18N
                    boolean reloadable = (reloadableB != null ? reloadableB.booleanValue() : false);
                    Boolean autoloadB = (Boolean)props.get("autoload"); // NOI18N
                    boolean autoload = (autoloadB != null ? autoloadB.booleanValue() : false);
                    Boolean eagerB = (Boolean)props.get("eager"); // NOI18N
                    boolean eager = (eagerB != null ? eagerB.booleanValue() : false);
                    Integer startLevel = (Integer)props.get("startlevel"); // NOI18N
                    ModuleHistory hist = new ModuleHistory(jar, "created from " + xmlfile);
                    Module m = createModule(jarFile, hist, reloadable, autoload, eager, startLevel);
                    m.addPropertyChangeListener(this);
                    // Mark the status as disabled for the moment, so in step 3 it will be turned on
                    // if in dirtyprops it was marked enabled.
                    Map<String, Object> statusProps;
                    if (props.get("enabled") != null && ((Boolean)props.get("enabled")).booleanValue()) { // NOI18N
                        statusProps = new HashMap<String, Object>(props);
                        statusProps.put("enabled", Boolean.FALSE); // NOI18N
                    } else {
                        statusProps = props;
                    }
                    DiskStatus status = new DiskStatus();
                    status.module = m;
                    status.file = xmlfile;
                    status.setDiskProps(statusProps);
                    statuses.put(cnb, status);
                }
            }
        }
        private void stepEnable(Map<String,Map<String,Object>> dirtyprops) throws IOException {
            LOG.fine("ModuleList: stepEnable");
            if (LOG.isLoggable(Level.FINEST)) {
                for (Entry<String, Map<String, Object>> e : dirtyprops.entrySet()) {
                    LOG.log(Level.FINEST, "{0} = {1}", new Object[]{e.getKey(), e.getValue()}); // NOI18N
                }
            }
            Set<Module> toenable = new HashSet<Module>();
            for (Map.Entry<String,Map<String,Object>> entry : dirtyprops.entrySet()) {
                String cnb = entry.getKey();
                Map<String, Object> props = entry.getValue();
                if (props.get("enabled") != null && ((Boolean)props.get("enabled")).booleanValue()) { // NOI18N
                    DiskStatus status = statuses.get(cnb);
                    assert status != null : cnb; // #159001
                    if (status.diskProps.get("enabled") == null || ! ((Boolean)status.diskProps.get("enabled")).booleanValue()) { // NOI18N
                        if (status.module.isEnabled()) throw new IllegalStateException("Already enabled: " + status.module); // NOI18N
                        toenable.add(status.module);
                    }
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                for (Module m : toenable) {
                    LOG.log(Level.FINEST, "About to enable {0}", m); // NOI18N
                }
            }
            installNew(toenable);
        }
        private void stepDisable(Map<String,Map<String,Object>> dirtyprops) throws IOException {
            LOG.fine("ModuleList: stepDisable");
            Set<Module> todisable = new HashSet<Module>();
            for (Map.Entry<String,Map<String,Object>> entry: dirtyprops.entrySet()) {
                String cnb = entry.getKey();
                Map<String, Object> props = entry.getValue();
                if (props.get("enabled") == null || ! ((Boolean)props.get("enabled")).booleanValue()) { // NOI18N
                    DiskStatus status = statuses.get(cnb);
                    assert status != null : cnb; // #159001
                    if (Boolean.TRUE.equals(status.diskProps.get("enabled"))) { // NOI18N
                        if (! status.module.isEnabled()) throw new IllegalStateException("Already disabled: " + status.module); // NOI18N
                        todisable.add(status.module);
                    }
                }
            }
            if (todisable.isEmpty()) {
                return;
            }
            List<Module> reallydisable = mgr.simulateDisable(todisable);
	    for (Module m: reallydisable) {
                if (!m.isAutoload() && !m.isEager() && !todisable.contains(m)) {
                    todisable.add(m);
                }
            }
            mgr.disable(todisable);
        }
        private void stepDelete(Map<String,FileObject> xmlfiles) throws IOException {
            LOG.fine("ModuleList: stepDelete");
            Set<Module> todelete = new HashSet<Module>();
            Iterator<Map.Entry<String,DiskStatus>> it = statuses.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String,DiskStatus> entry = it.next();
                String cnb = entry.getKey();
                DiskStatus status = entry.getValue();
                if (! xmlfiles.containsKey(cnb)) {
                    Module m = status.module;
                    todelete.add(m);
                    it.remove();
                }
            }
            if (todelete.isEmpty()) {
                return;
            }
            Set<Module> todisable = new HashSet<Module>();
	    for (Module m: todelete) {
                if (m.isEnabled() && !m.isAutoload() && !m.isEager()) {
                    todisable.add(m);
                }
            }
            List<Module> reallydisable = mgr.simulateDisable(todisable);
	    for (Module m: reallydisable) {
                if (!m.isAutoload() && !m.isEager() && !todisable.contains(m)) {
                    todisable.add(m);
                }
            }
            mgr.disable(todisable);
            // In case someone tried to delete an enabled autoload/eager module:
            Iterator<Module> delIt = todelete.iterator();
            while (delIt.hasNext()) {
                Module m = delIt.next();
                if (m.isEnabled()) {
                    if (!m.isAutoload() && !m.isEager()) throw new IllegalStateException("Module " + m + " scheduled for deletion could not be disabled yet was not an autoload nor eager"); // NOI18N
                    // XXX is it better to find all regular module using it and turn all of those off?
                    ev.log(Events.CANT_DELETE_ENABLED_AUTOLOAD, m);
                    delIt.remove();
                } else {
                    mgr.delete(m);
                }
            }
        }
        private void stepCheckMisc(Map<String,Map<String,Object>> dirtyprops) {
            LOG.fine("ModuleList: stepCheckMisc");
            String[] toCheck = {"jar", "autoload", "eager"}; // NOI18N
            for (Map.Entry<String,Map<String,Object>> entry : dirtyprops.entrySet()) {
                String cnb = entry.getKey();
                Map<String,Object> props = entry.getValue();
                DiskStatus status = statuses.get(cnb);
                assert status != null : cnb; // #159001
                Map<String,Object> diskProps = status.diskProps;
                for (int i = 0; i < toCheck.length; i++) {
                    String prop = toCheck[i];
                    Object onDisk = props.get(prop);
                    Object inMem = diskProps.get(prop);
                    if (! BaseUtilities.compareObjects(onDisk, inMem)) {
                        ev.log(Events.MISC_PROP_MISMATCH, status.module, prop, onDisk, inMem);
                    }
                }
            }
        }

        private void stepCheckSer(Map<String,FileObject> xmlfiles, Map<String,Map<String,Object>> dirtyprops) {
            // There is NO step 7!
        }

        private void stepUpdateProps(Map<String,Map<String,Object>> dirtyprops) {
            LOG.fine("ModuleList: stepUpdateProps");
            for (Map.Entry<String,Map<String,Object>> entry: dirtyprops.entrySet()) {
                String cnb = entry.getKey();
                DiskStatus status = statuses.get(cnb);
                if (status != null) {
                    Map<String,Object> props = entry.getValue();
                    status.setDiskProps(props);
                }
            }
        }
        private void stepMarkClean() {
            LOG.fine("ModuleList: stepMarkClean");
            for (DiskStatus status : statuses.values()) {
                status.dirty = false;
            }
        }
        
    }
    
    /** Representation of the status of a module on disk and so on. */
    private static final class DiskStatus {
        /** Initialize as a struct, i.e. member by member: */
        public DiskStatus() {}
        /** actual module object */
        public Module module;
        /** XML file holding its status */
        public FileObject file;
        /** timestamp of last modification to XML file that this class did */
        //public long lastApprovedChange;
        /** if true, this module was scanned and should be enabled but we are waiting for trigger */
        public boolean pendingInstall = false;
        /** properties of the module on disk */
        public Map<String,Object /*String|Integer|Boolean|SpecificationVersion*/> diskProps;
        void setDiskProps(Map<String,Object> diskProps) {
            Parameters.notNull("diskProps", diskProps);
            this.diskProps = diskProps;
        }
        /** if true, the XML was changed on disk by someone else */
        public boolean dirty = false;
        /** for debugging: */
        public @Override String toString() {
            return "DiskStatus[module=" + module + // NOI18N
                ",valid=" + module.isValid() + // NOI18N
                ",file=" + file + /*",lastApprovedChange=" + new Date(lastApprovedChange) +*/ // NOI18N
                ",dirty=" + dirty + // NOI18N
                ",pendingInstall=" + pendingInstall + // NOI18N
                ",diskProps=" + diskProps + "]"; // NOI18N
        }
    }

    private class ReadInitial implements AtomicAction, Runnable {
        private final Set<Module> read;
        private volatile Task task;

        public ReadInitial(Set<Module> read) {
            this.read = read;
        }

        @Override
        public void run() {
            if (task != null) {
                init();
                return;
            }
            task = RP.create(this);
            task.schedule(0);
            
            Map<String, Map<String, Object>> cache = readCache();
            String[] names;
            if (cache != null) {
                names = cache.keySet().toArray(new String[cache.size()]);
            } else {
                FileObject[] children = folder.getChildren();
                List<String> arr = new ArrayList<String>(children.length);
                for (FileObject f : children) {
                    if (f.hasExt("ser")) { // NOI18N
                        // Fine, skip over.
                    } else if (f.hasExt("xml")) {
                        // NOI18N
                        // Assume this is one of ours. Note fixed naming scheme.
                        String nameDashes = f.getName(); // NOI18N
                        char[] badChars = {'.', '/', '>', '='};
                        for (int j = 0; j < 4; j++) {
                            if (nameDashes.indexOf(badChars[j]) != -1) {
                                throw new IllegalArgumentException("Bad name: " + nameDashes); // NOI18N
                            }
                        }
                        String name = nameDashes.replace('-', '.').intern(); // NOI18N
                        arr.add(name);
                    } else {
                        LOG.fine("Strange file encountered in modules folder: " + f);
                    }
                }
                names = arr.toArray(new String[0]);
            }
            ev.log(Events.MODULES_FILE_SCANNED, names.length);
            XMLReader reader = null;
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                FileObject f = null;
                try {
                    // OK, read it from disk.
                    Map<String, Object> props = cache == null ? null : cache.get(name);
                    if (props == null) {
                        // Now name is the code name base of the module we expect to find.
                        // Check its format (throws IllegalArgumentException if bad):
                        Dependency.create(Dependency.TYPE_MODULE, name);
                        LOG.log(Level.FINEST, "no cache for {0}", name);
                        f = folder.getFileObject(name.replace('.', '-') + ".xml");
                        InputStream is = f.getInputStream();
                        try {
                            props = readStatus(new BufferedInputStream(is), true);
                            if (props == null) {
                                LOG.warning("Note - failed to parse " + f + " the quick way, falling back on XMLReader");
                                is.close();
                                is = f.getInputStream();
                                InputSource src = new InputSource(is);
                                // Make sure any includes etc. are handled properly:
                                src.setSystemId(f.toURL().toExternalForm());
                                if (reader == null) {
                                    try {
                                        reader = XMLUtil.createXMLReader();
                                    } catch (SAXException e) {
                                        throw (IllegalStateException) new IllegalStateException(e.toString()).initCause(e);
                                    }
                                    reader.setEntityResolver(listener);
                                    reader.setErrorHandler(listener);
                                }
                                props = readStatus(src, reader);
                            }
                        } finally {
                            is.close();
                        }
                    }
                    if (!name.equals(props.get("name"))) {
                        throw new IOException("Code name mismatch: " + name + " vs. " + props.get("name")); // NOI18N
                    }
                    Boolean enabledB = (Boolean) props.get("enabled"); // NOI18N
                    String jar = (String) props.get("jar"); // NOI18N
                    File jarFile;
                    try {
                        jarFile = findJarByName(jar, name);
                    } catch (FileNotFoundException fnfe) {
                        //LOG.fine("Cannot find: " + fnfe.getMessage());
                        ev.log(Events.MISSING_JAR_FILE, new File(fnfe.getMessage()), enabledB);
                        if (f != null && !Boolean.FALSE.equals(enabledB)) {
                            try {
                                f.delete();
                            } catch (IOException ioe) {
                                LOG.log(Level.WARNING, null, ioe);
                            }
                        }
                        continue;
                    }
                    ModuleHistory history = new ModuleHistory(jar, "loaded from " + f); // NOI18N
                    Boolean reloadableB = (Boolean) props.get("reloadable"); // NOI18N
                    boolean reloadable = reloadableB != null ? reloadableB.booleanValue() : false;
                    boolean enabled = enabledB != null ? enabledB.booleanValue() : false;
                    Boolean autoloadB = (Boolean) props.get("autoload"); // NOI18N
                    boolean autoload = autoloadB != null ? autoloadB.booleanValue() : false;
                    Boolean eagerB = (Boolean) props.get("eager"); // NOI18N
                    boolean eager = eagerB != null ? eagerB.booleanValue() : false;
                    NbInstaller.register(name, props.get("deps")); // NOI18N
                    Integer startLevel = (Integer)props.get("startlevel"); // NOI18N
                    Module m = createModule(jarFile, history, reloadable, autoload, eager, startLevel);
                    NbInstaller.register(null, null);
                    read.add(m);
                    DiskStatus status = new DiskStatus();
                    status.module = m;
                    status.file = f;
                    //status.lastApprovedChange = children[i].lastModified().getTime();
                    status.pendingInstall = enabled;
                    // Will only really be flushed if mgr props != disk props, i.e
                    // if version changed or could not be enabled.
                    //status.pendingFlush = true;
                    status.setDiskProps(props);
                    statuses.put(name, status);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error encountered while reading " + name, e);
                }
                ev.log(Events.MODULES_FILE_PROCESSED, name);
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("read initial XML files: statuses=" + statuses);
            }
            ev.log(Events.FINISH_READ, read);
            // Handle changes in the Modules/ folder on disk by parsing & applying them.
            task.waitFinished();
        }
    }
    
}
