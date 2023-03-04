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

package org.netbeans.modules.openide.filesystems.declmime;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.openide.filesystems.declmime.FileElement.Type;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.BaseUtilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * MIMEResolver implementation driven by an XML document instance
 * following PUBLIC "-//NetBeans//DTD MIME Resolver 1.0//EN".
 *
 * @author  Petr Kuzel
 */
public final class MIMEResolverImpl {
    
    // enable some tracing
    private static final Logger ERR = Logger.getLogger(MIMEResolverImpl.class.getName());
        
    static final boolean CASE_INSENSITIVE = BaseUtilities.getOperatingSystem() == BaseUtilities.OS_VMS;

    // notification limit in bytes for reading file content. It should not exceed 4192 (4kB) because it is read in one disk touch.
    private static final int READ_LIMIT = 4000;
    private static Set<String> readLimitReported = new HashSet<String>();

    // constants for user defined declarative MIME resolver
    private static final String MIME_RESOLVERS_PATH = "Services/MIMEResolver";  //NOI18N
    private static final String USER_DEFINED_MIME_RESOLVER = "user-defined-mime-resolver";  //NOI18N
    /** Position of user-defined mime resolver. Need to very low to override all other resolvers. */
    private static final int USER_DEFINED_MIME_RESOLVER_POSITION = 10;

    public static MIMEResolver forDescriptor(FileObject fo) throws IOException {
        if (fo.getSize() == 0 && !isUserDefined(fo)) {
            return create(fo);
        }
        return forDescriptor(fo, true);
    }
    static MIMEResolver forDescriptor(FileObject fo, boolean warn) {
        if (warn && !isUserDefined(fo)) {
            ERR.log(Level.WARNING, "Ineffective registration of resolver {0} use @MIMEResolver.Registration! See bug #191777.", fo.getPath());
            if (ERR.isLoggable(Level.FINE)) {
                try {
                    ERR.fine(fo.asText());
                } catch (IOException ex) {
                    ERR.log(Level.FINE, null, ex);
                }
            }
        }
        return new Impl(fo);
    }

    static MIMEResolver forStream(FileObject def, byte[] serialData) throws IOException {
        return new Impl(def, serialData);
    }
    
    static byte[] toStream(MIMEResolver mime) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        ((Impl)mime).writeExternal(dos);
        dos.close();
        return os.toByteArray();
    }

    /** Check whether given resolver is declarative. */
    public static boolean isDeclarative(MIMEResolver resolver) {
        return resolver instanceof Impl;
    }

    /** Returns resolvable MIME Types for given declarative resolver. */
    public static String[] getMIMETypes(MIMEResolver resolver) {
        ((Impl) resolver).init();  // #171312 - resolver must be parsed
        return ((Impl)resolver).implResolvableMIMETypes;
    }

    /** Check whether given resolver's FileObject is user defined.
     * @param mimeResolverFO resolver's FileObject
     * @return true if specified FileObject is user defined MIME resolver, false otherwise
     */
    public static boolean isUserDefined(FileObject mimeResolverFO) {
        return 
            mimeResolverFO.getAttribute(USER_DEFINED_MIME_RESOLVER) != null || 
            mimeResolverFO.getName().equals(USER_DEFINED_MIME_RESOLVER);
    }

    /** Returns mapping of MIME type to set of extensions. It never returns null,
     * it can return empty set of extensions.
     * @param fo MIMEResolver FileObject
     * @return mapping of MIME type to set of extensions like
     * {@literal {image/jpeg=[jpg, jpeg], image/gif=[]}}.
     */
    public static Map<String, Set<String>> getMIMEToExtensions(FileObject fo) {
        Impl impl;
        if (!fo.hasExt("xml") || fo.getSize() == 0) { // NOI18N
            try {
                impl = (Impl) MIMEResolverImpl.create(fo);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                impl = null;
            } catch (IllegalArgumentException ex) {
                if (isUserDefined(fo)) {
                    File f = FileUtil.toFile(fo);
                    ERR.log(Level.INFO, "User-defined file association "//NOI18N
                            + "settings are corrupted. Delete file "    //NOI18N
                            + (f == null ? fo.getPath() : f.getPath()), ex);
                    impl = null;
                } else {
                    throw ex;
                }
            }
            if (impl == null) {
                return Collections.emptyMap();
            }
            impl.init();
        } else {
            impl = new Impl(fo);
            impl.parseDesc();
        }
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        FileElement[] elements = impl.smell;
        if (elements != null) {
            for (FileElement fileElement : elements) {
                String mimeType = fileElement.getMimeType();
                if (mimeType != null) {  // can be null if <exit/> element is used
                    String[] extensions = fileElement.getExtensions();
                    Set<String> extensionsSet = new HashSet<String>();
                    if (extensions != null) {
                        for (String extension : extensions) {
                            if (extension.length() > 0) {  // ignore empty extension
                                extensionsSet.add(extension);
                            }
                        }
                    }
                    Set<String> previous = result.get(mimeType);
                    if (previous != null) {
                        extensionsSet.addAll(previous);
                    }
                    result.put(mimeType, extensionsSet);
                }
            }
        }
        return result;
    }

    /** Returns FileObject representing declarative user defined MIME resolver
     * or null if not yet created.
     * @return FileObject representing declarative user defined MIME resolver
     * or null if not yet created.
     */
    public static FileObject getUserDefinedResolver() {
        FileObject resolversFolder = FileUtil.getConfigFile(MIME_RESOLVERS_PATH);
        if (resolversFolder != null) {
            FileObject[] resolvers = resolversFolder.getChildren();
            for (FileObject resolverFO : resolvers) {
                if (resolverFO.getAttribute(USER_DEFINED_MIME_RESOLVER) != null) {
                    return resolverFO;
                }
            }
        }
        return null;
    }

    /** Stores declarative resolver corresponding to specified mapping of MIME type
     * and set of extensions. This resolver has the highest priority. Usually
     * it resides in userdir/config/Servicer/MIMEResolver.
     * @param mimeToExtensions mapping of MIME type to set of extensions like
     * {@literal {image/jpeg=[jpg, jpeg], image/gif=[]}}.
     * @return true, if first user defined resolver was created
     */
    public static synchronized boolean storeUserDefinedResolver(final Map<String, Set<String>> mimeToExtensions) {
        Parameters.notNull("mimeToExtensions", mimeToExtensions);  //NOI18N
        final FileObject userDefinedResolverFO = getUserDefinedResolver();
        if (userDefinedResolverFO != null) {
            try {
                // delete previous resolver because we need to refresh MIMEResolvers
                userDefinedResolverFO.delete();
            } catch (IOException e) {
                ERR.log(Level.SEVERE, "Cannot delete resolver " + FileUtil.toFile(userDefinedResolverFO), e);  //NOI18N
                return false;
            }
        }
        if (mimeToExtensions.isEmpty()) {
            // nothing to write
            return false;
        }
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                Document document = XMLUtil.createDocument("MIME-resolver", null, "-//NetBeans//DTD MIME Resolver 1.1//EN", "http://www.netbeans.org/dtds/mime-resolver-1_1.dtd");  //NOI18N
                for (Map.Entry<String, Set<String>> entry : mimeToExtensions.entrySet()) {
                    String mimeType = entry.getKey();
                    Set<String> extensions = entry.getValue();
                    if (!extensions.isEmpty()) {
                        Element fileElement = document.createElement("file");  //NOI18N
                        for (String extension : mimeToExtensions.get(mimeType)) {
                            Element extElement = document.createElement("ext");  //NOI18N
                            extElement.setAttribute("name", extension);  //NOI18N
                            fileElement.appendChild(extElement);
                        }
                        Element resolverElement = document.createElement("resolver");  //NOI18N
                        resolverElement.setAttribute("mime", mimeType);  //NOI18N
                        fileElement.appendChild(resolverElement);
                        document.getDocumentElement().appendChild(fileElement);
                    }
                }
                if (!document.getDocumentElement().hasChildNodes()) {
                    // nothing to write
                    return;
                }
                OutputStream os = null;
                FileObject newUserDefinedFO = null;
                try {
                    FileObject resolversFolder = FileUtil.getConfigFile(MIME_RESOLVERS_PATH);
                    if (resolversFolder == null) {
                        resolversFolder = FileUtil.createFolder(FileUtil.getConfigRoot(), MIME_RESOLVERS_PATH);
                    }
                    newUserDefinedFO = resolversFolder.createData(USER_DEFINED_MIME_RESOLVER, "xml");  //NOI18N
                    newUserDefinedFO.setAttribute(USER_DEFINED_MIME_RESOLVER, Boolean.TRUE);
                    newUserDefinedFO.setAttribute("position", USER_DEFINED_MIME_RESOLVER_POSITION);  //NOI18N
                    os = newUserDefinedFO.getOutputStream();
                    XMLUtil.write(document, os, "UTF-8"); //NOI18N
                } catch (IOException e) {
                    ERR.log(Level.SEVERE, "Cannot write resolver " + (newUserDefinedFO == null ? "" : FileUtil.toFile(newUserDefinedFO)), e);  //NOI18N
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            ERR.log(Level.SEVERE, "Cannot close OutputStream of file " + (newUserDefinedFO == null ? "" : FileUtil.toFile(newUserDefinedFO)), e);  //NOI18N
                        }
                    }
                }
            }
        });
        return userDefinedResolverFO == null;
    }

    /** Lists registered MIMEResolver instances in reverse order,
     * i.e. first are ones with lower priority (position attribute higher)
     * and last are ones with highest prority (position attribute lower).
     * @return list of all registered MIMEResolver instances in reverse order
     */
    public static Collection<? extends FileObject> getOrderedResolvers() {
        // scan resolvers and order them to be able to assign extension to mime type from resolver with the lowest position
        FileObject[] resolvers = FileUtil.getConfigFile(MIME_RESOLVERS_PATH).getChildren();
        TreeMap<Integer, FileObject> orderedResolvers = new TreeMap<Integer, FileObject>(Collections.reverseOrder());
        for (FileObject mimeResolverFO : resolvers) {
            Integer position = (Integer) mimeResolverFO.getAttribute("position");  //NOI18N
            if (position == null) {
                position = Integer.MAX_VALUE;
            }
            while (orderedResolvers.containsKey(position)) {
                position--;
            }
            orderedResolvers.put(position, mimeResolverFO);
        }
        return orderedResolvers.values();
    }

    private static FileElement extensionElem(List<String> exts, String mimeType) {
        FileElement e = new FileElement();
        for (String ext : exts) {
            e.fileCheck.addExt(ext);
        }
        e.setMIME(mimeType);
        return e;
    }


    private static MIMEResolver forExts(FileObject def, String mimeType, List<String> exts) throws IOException {
        FileElement[] e = { extensionElem(exts, mimeType) };
        return new Impl(def, e, mimeType);
    }

    private static MIMEResolver forXML(FileObject def, 
        String mimeType, List<String> exts, List<String> acceptExts,
        String elem, List<String> namespace, List<String> dtds
    ) throws IOException {
        FileElement e = new FileElement();
        for (String ext : exts) {
            e.fileCheck.addExt(ext);
        }
        e.rule = new XMLMIMEComponent(elem, namespace, dtds);
        e.setMIME(mimeType);
        if (acceptExts.isEmpty()) {
            return new Impl(def, new FileElement[] { e }, mimeType);
        } else {
            FileElement direct = extensionElem(acceptExts, mimeType);
            return new Impl(def, new FileElement[] { e, direct }, mimeType);
        }
    }

    /** factory method for {@link MIMEResolver.Registration} */
    public static MIMEResolver create(FileObject fo) throws IOException {
        byte[] arr = (byte[]) fo.getAttribute("bytes");
        if (arr != null) {
            return forStream(fo, arr);
        }
        String mimeType = (String) fo.getAttribute("mimeType"); // NOI18Ns
        String element = (String) fo.getAttribute("element"); // NOI18N
        List<String> exts = readArray(fo, "ext."); // NOI18N
        if (element != null) {
            List<String> accept = readArray(fo, "accept."); // NOI18N
            List<String> nss = readArray(fo, "ns."); // NOI18N
            List<String> dtds = readArray(fo, "doctype."); // NOI18N
            return forXML(fo, mimeType, exts, accept, element, nss, dtds);
        }
        
        if (!exts.isEmpty()) {
            return forExts(fo, mimeType, exts);
        }
        throw new IllegalArgumentException("" + fo);
    }

    private static List<String> readArray(FileObject fo, final String prefix) {
        List<String> exts = new ArrayList<String>();
        int cnt = 0;
        for (;;) {
            String ext = (String) fo.getAttribute(prefix + cnt++);
            if (ext == null) {
                break;
            }
            exts.add(ext);
        }
        return exts;
    }
    

    // MIMEResolver ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final class Impl extends MIMEResolver
        implements MIMEResolverProcessor.FilterInfo {

        // This file object describes rules that drive ths instance
        private final FileObject data;

        private final FileChangeListener listener = new FileChangeAdapter() {
            public @Override void fileChanged(FileEvent fe) {
                synchronized (Impl.this) {
                    state = DescParser.INIT;
                    implResolvableMIMETypes = null;
                }
            }
        };

        // Resolvers in reverse order
        private FileElement[] smell;
                
        private short state;

        private String[] implResolvableMIMETypes;

        @SuppressWarnings("deprecation")
        Impl(FileObject obj) {
            if (ERR.isLoggable(Level.FINE)) ERR.log(Level.FINE, "MIMEResolverImpl.Impl.<init>({0})", obj);  // NOI18N
            state = DescParser.INIT;
            data = obj;
            data.addFileChangeListener(FileUtil.weakFileChangeListener(listener, data));
        }

        @SuppressWarnings("deprecation")
        private Impl(FileObject def, byte[] serialData) throws IOException {
            data = def;
            state = DescParser.LOAD;
            ByteArrayInputStream is = new ByteArrayInputStream(serialData);
            DataInputStream dis = new DataInputStream(is);
            readExternal(dis);

        }
        @SuppressWarnings("deprecation")
        private Impl(FileObject def, FileElement[] arr, String... mimeType) throws IOException {
            this.data = def;
            this.implResolvableMIMETypes = mimeType;
            this.smell = arr;
            this.state = DefaultParser.PARSED;
        }

        @Override
        public String findMIMEType(FileObject fo) {
            if (fo.hasExt("xml") && fo.getPath().startsWith(MIME_RESOLVERS_PATH)) { // NOI18N
                // do not try to check ourselves!
                return null;
            }

            init();
            if (state == DescParser.ERROR) {
                return null;
            }

            FileElement[] smell2 = smell;  //#163378, #157838 - copy to prevent concurrent modification and not synchronize to prevent deadlock
            // smell is filled in reverse order
            for (int i = smell2.length - 1; i >= 0; i--) {
                if (ERR.isLoggable(Level.FINE)) ERR.fine("findMIMEType - smell.resolve.");
                String s = smell2[i].resolve(fo);
                if (s != null) {
                    if (s.equals(FileElement.EXIT_MIME_TYPE)) {
                        // if file matches conditions and exit element is present, do not continue in loop and return null
                        return null;
                    }
                    if (ERR.isLoggable(Level.FINE)) ERR.log(Level.FINE, "MIMEResolverImpl.findMIMEType({0})={1}", new Object[]{fo, s});  // NOI18N
                    return s;
                }
            }
            
            return null;
        }

        private void init() {
            synchronized (this) {  // lazy init
                if (state == DescParser.INIT) {
                    state = parseDesc();
                }
            }
        }

        // description document is parsed in the same thread
        private short parseDesc() {
            smell = new FileElement[0];
            DescParser parser = new DescParser(data);
            parser.parse();
            smell = (parser.template != null) ? parser.template : smell;
            if (ERR.isLoggable(Level.FINE)) {
                if (parser.state == DescParser.ERROR) {
                    ERR.fine("MIMEResolverImpl.Impl parsing error!");
                } else {
                    StringBuilder buf = new StringBuilder();
                    buf.append("Parse: ");
                    for (int i = 0; i<smell.length; i++)
                        buf.append('\n').append(smell[i]);
                    ERR.fine(buf.toString());
                }
            }
            // fill resolvableMIMETypes array with available MIME types
            if(parser.state != DescParser.ERROR) {
                for (int i = 0; i < smell.length; i++) {
                    String mimeType = smell[i].getMimeType();
                    if(mimeType != null) {
                        implResolvableMIMETypes = Util.addString(implResolvableMIMETypes, mimeType);
                    }
                }
            }
            return parser.state;
        }
        
        /** For debug purposes. */
        @Override
        public String toString() {
            return "MIMEResolverImpl.Impl[" + data + "]";  // NOI18N
        }

        public void writeExternal(DataOutput out) throws IOException {
            init();
            if (state == DescParser.ERROR) {
                throw new IOException();
            }
            Util.writeStrings(out, implResolvableMIMETypes);
            out.writeInt(smell.length);
            for (FileElement fe : smell) {
                fe.writeExternal(out);
            }
        }

        private void readExternal(DataInput in) throws IOException {
            if (state != DescParser.LOAD) {
                throw new IOException();
            }
            try {
                implResolvableMIMETypes = Util.readStrings(in);
                smell = new FileElement[in.readInt()];
                for (int i = 0; i < smell.length; i++) {
                    smell[i] = new FileElement();
                    smell[i].readExternal(in);
                }
                state = DescParser.PARSED;
            } finally {
                if (state == DescParser.LOAD) {
                    state = DescParser.ERROR;
                }
            }
        }

        @Override
        public List<String> getExtensions() {
            if (smell == null) {
                return Collections.emptyList();
            } else {
                List<String> extensions = new LinkedList<String>();
                for (FileElement fe : smell) {
                    if (fe != null && fe.getExtensions() != null
                            && (fe.getNames() == null
                            || fe.getNames().isEmpty())) {
                        for (String ext : fe.getExtensions()) {
                            if (ext != null && !ext.isEmpty()) {
                                extensions.add(ext);
                            }
                        }
                    }
                }
                return extensions;
            }
        }

        @Override
        public List<String> getFileNames() {
            if (smell == null) {
                return Collections.emptyList();
            } else {
                List<String> fileNames = new LinkedList<String>();
                for (FileElement fe : smell) {
                    if (fe != null && fe.getNames() != null) {
                        for (Type.FileName name : fe.getNames()) {
                            String[] exts = fe.getExtensions();
                            if (exts == null || exts.length == 0) {
                                continue;
                            }
                            for (String ext : exts) {
                                fileNames.add(name.toString() + ext);
                            }
                        }
                    }
                }
                return fileNames;
            }
        }
    }

    
    // XML -> memory representation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Resonsible for parsing backend FileObject and filling resolvers
     * in memory structure according to it.
     */
    private static class DescParser extends DefaultParser {

        private FileElement[] template = null;
        
        // file state substates
        private short file_state = INIT;
        
        // references active resolver component
        private XMLMIMEComponent component = null;        
        private String componentDelimiter = null;
        // holds level of pattern element
        private int patternLevel = 0;
        // used to prohibit more pattern elements on the same level
        Set<Integer> patternLevelSet;


        DescParser(FileObject fo) {
            super(fo);
        }

        // pseudo validation states
        private static final short IN_ROOT = 1;
        private static final short IN_FILE = 2;
        private static final short IN_RESOLVER = 3;
        private static final short IN_COMPONENT = 4;
        private static final short IN_PATTERN = 5;

        // second state dimension
        private static final short IN_EXIT = INIT + 1;
        
        // grammar elements
        private static final String ROOT = "MIME-resolver";  // NOI18N
        private static final String FILE = "file"; // NOI18N
        private static final String MIME = "mime"; // NOI18N
        private static final String EXT  = "ext"; // NOI18N
        private static final String RESOLVER = "resolver"; // NOI18N
        private static final String FATTR = "fattr"; // NOI18N
        private static final String NAME = "name"; // NOI18N
        private static final String PATTERN = "pattern"; // NOI18N
        private static final String VALUE = "value"; // NOI18N
        private static final String RANGE = "range"; // NOI18N
        private static final String IGNORE_CASE = "ignorecase"; // NOI18N
        private static final String SUBSTRING = "substring"; // NOI18N
        private static final String MAGIC = "magic"; // NOI18N
        private static final String HEX = "hex"; // NOI18N
        private static final String MASK = "mask"; // NOI18N
        private static final String TEXT = "text"; // NOI18N
        private static final String EXIT = "exit"; // NOI18N
        private static final String XML_RULE_COMPONENT = "xml-rule";  // NOI18N

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

            String s;

            switch (state) {

                case INIT:

                    if (ROOT.equals(qName) ==  false) error();
                    state = IN_ROOT;
                    break;

                case IN_ROOT:
                    if (FILE.equals(qName) == false) error();

                    // prepare file element structure
                    // actual one is at index 0

                    if (template == null) {
                        template = new FileElement[] {new FileElement()};
                    } else {
                        FileElement[] n = new FileElement[template.length +1];
                        System.arraycopy(template, 0, n, 1, template.length);
                        n[0] = new FileElement();
                        template = n;
                    }

                    state = IN_FILE;                        
                    break;

                case IN_FILE:

                    if (file_state == IN_EXIT) error();
                    
                    if (EXT.equals(qName)) {

                        s = atts.getValue(NAME); if (s == null) error();
                        template[0].fileCheck.addExt(s);

                    } else if (MAGIC.equals(qName)) {

                        s = atts.getValue(HEX); if (s == null) error();
                        String mask = atts.getValue(MASK);                            
                        
                        char[] chars = s.toCharArray();
                        byte[] mask_bytes = null;  // mask is optional
                                                
                        try {
                        
                            if (mask != null) {
                                char[] mask_chars = mask.toCharArray();
                                mask_bytes = XMLUtil.fromHex(mask_chars, 0, mask_chars.length);
                            }
                        
                            byte[] magic = XMLUtil.fromHex(chars, 0, chars.length);
                            if (template[0].fileCheck.setMagic(magic, mask_bytes) == false) {
                                error();
                            }
                        } catch (IOException ioex) {
                            error();
                        }


                    } else if (MIME.equals(qName)) {

                        s = atts.getValue(NAME); if (s == null) error();
                        template[0].fileCheck.addMIME(s);

                    } else if (FATTR.equals(qName)) {

                        s = atts.getValue(NAME); if (s == null) error();
                        String val = atts.getValue(TEXT);
                        template[0].fileCheck.addAttr(s, val);                        

                    } else if (PATTERN.equals(qName)) {

                        s = atts.getValue(VALUE); if (s == null) error();
                        int range = Integer.valueOf(atts.getValue(RANGE));
                        assert range <= READ_LIMIT || !readLimitReported.add(fo.getPath()): "MIME resolver " + fo.getPath() + " should not exceed " + READ_LIMIT + " bytes limit for files content check.";  //NOI18N
                        boolean ignoreCase = Type.FilePattern.DEFAULT_IGNORE_CASE;
                        String ignoreCaseAttr = atts.getValue(IGNORE_CASE);
                        if (ignoreCaseAttr != null) {
                            ignoreCase = Boolean.valueOf(ignoreCaseAttr);
                        }
                        if (file_state == IN_PATTERN) {
                            if (patternLevelSet == null) {
                                patternLevelSet = new HashSet<Integer>();
                            }
                            if (!patternLevelSet.add(patternLevel)) {
                                error("Second pattern element on the same level not allowed");  //NOI18N
                            }
                            template[0].fileCheck.addInnerPattern(s, range, ignoreCase);
                        } else {
                            template[0].fileCheck.addPattern(s, range, ignoreCase);
                            file_state = IN_PATTERN;
                        }
                        patternLevel++;
                        break;

                    } else if (NAME.equals(qName)) {

                        s = atts.getValue(NAME); if (s == null) error();
                        String substringAttr = atts.getValue(SUBSTRING);
                        boolean substring = Type.FileName.DEFAULT_SUBSTRING;
                        if (substringAttr != null) {
                            substring = Boolean.valueOf(substringAttr);
                        }
                        boolean ignoreCase = Type.FileName.DEFAULT_IGNORE_CASE;
                        String ignoreCaseAttr = atts.getValue(IGNORE_CASE);
                        if (ignoreCaseAttr != null) {
                            ignoreCase = Boolean.valueOf(ignoreCaseAttr);
                        }
                        template[0].fileCheck.addName(s, substring, ignoreCase);
                        break;

                    } else if (RESOLVER.equals(qName)) {

                        if (!template[0].fileCheck.isValid()) {
                            error();  // at least one must be specified
                        }

                        s = atts.getValue(MIME); if (s == null) error();
                        template[0].setMIME(s);

                        state = IN_RESOLVER;
                        
                        break;

                    } else if (EXIT.equals(qName)) {
                        template[0].fileCheck.setExit();
                        file_state = IN_EXIT;
                        break;
                        
                        
                    } else {
                        String reason = "Unexpected element:  " + qName;
                        error(reason);
                    }
                    break;

                case IN_RESOLVER:
                    
                    // it is switch to hardcoded components
                    // you can smooth;y add new ones by entering them
                
                    // PLEASE update DTD public ID register it to XML Environment.Provider
                    // Let the DTD is backward compatible
                    
                    if (XML_RULE_COMPONENT.equals(qName)) {
                        enterComponent(XML_RULE_COMPONENT, new XMLMIMEComponent());
                        component.startElement(namespaceURI, localName, qName, atts);
                    }   
                    
                    break;

                case IN_COMPONENT:
                    
                    component.startElement(namespaceURI, localName, qName, atts);                    
                    break;

                default:

            }
        }

        private void enterComponent(String name, XMLMIMEComponent component) {
            this.component = component;
            componentDelimiter = name;

            component.setDocumentLocator(getLocator());           
            template[0].rule = component;
            state = IN_COMPONENT;            
        }
        
        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            switch (state) {
                case IN_FILE:
                    if (FILE.equals(qName)) {
                        state = IN_ROOT;
                        file_state = INIT;
                    }
                    if (PATTERN.equals(qName)) {
                        if (--patternLevel == 0) {
                            patternLevelSet = null;
                            file_state = INIT;
                        }
                    }
                    break;       
                    
                case IN_RESOLVER:
                    if (RESOLVER.equals(qName)) {
                        state = IN_FILE;
                    }
                    break;
                    
                case IN_COMPONENT:
                    component.endElement(namespaceURI, localName, qName);
                    if (componentDelimiter.equals(qName)) {
                        state = IN_RESOLVER;
                        component.setDocumentLocator(null);
                    }
                    break;
            }
        }

        @Override
        public void characters(char[] data, int offset, int len) throws SAXException {
            if (state == IN_COMPONENT) component.characters(data, offset, len);
        }
    }       
}
