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
package org.netbeans.modules.subversion.client.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.parser.ConflictDescriptionParser.ParserConflictDescriptor;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.xml.XMLUtil;
import org.tigris.subversion.svnclientadapter.SVNConflictDescriptor;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Tomas Stupka
 */
public class EntriesCache {

    private static final String SVN_THIS_DIR = "svn:this_dir"; // NOI18N
    private static final String DELIMITER = "\f";
    static final String ATTR_TREE_CONFLICTS = "tree-conflicts"; //NOI18N
    public static final String WC17FORMAT = "WC1_7Format";

    /**
     * The order as it is defined should be the same as how the Subvesion entries handler
     * expects to read the entries file.  This ordering is based on Subversion 1.4.0
     */
    static String[] entryFileAttributes = new String[] {
        "name",
        "kind",
        "revision",
        "url",
        "repos",
        "schedule",
        "text-time",
        "checksum",
        "committed-date",
        "committed-rev",
        "last-author",
        "has-props",
        "has-prop-mods",
        "cachable-props",
        "present-props",
        "prop-reject-file",
        "conflict-old",
        "conflict-new",
        "conflict-wrk",
        "copied",
        "copyfrom-url",
        "copyfrom-rev",
        "deleted",
        "absent",
        "incomplete",
        "uuid",
        "lock-token",
        "lock-owner",
        "lock-comment",
        "lock-creation-date",
        "changelist",
        "keep-local",
        "working-size",
        "depth",
        ATTR_TREE_CONFLICTS
    };

    private static final Set<String> BOOLEAN_ATTRIBUTES = new java.util.HashSet<String>();
    static {
        BOOLEAN_ATTRIBUTES.add("has-props");
        BOOLEAN_ATTRIBUTES.add("has-prop-mods");
        BOOLEAN_ATTRIBUTES.add("copied");
        BOOLEAN_ATTRIBUTES.add("deleted");
    }
    private static final Set<String> DATE_ATTRIBUTES = new java.util.HashSet<String>();
    static {
        DATE_ATTRIBUTES.add("committed-date");
        DATE_ATTRIBUTES.add("lock-creation-date");
        DATE_ATTRIBUTES.add("text-time");
    }

    private final static int MAX_SIZE;

    static {
        int ms;
        try {
            ms = Integer.parseInt(System.getProperty("org.netbeans.modules.subversion.entriescache.max_size", "200"));
        } catch (NumberFormatException e) {
            ms = -1;
        }
        MAX_SIZE = ms > 0 ? ms : -1;
    }

    private AttributePool attributePool = new AttributePool();

    private Entries entries;
    private static EntriesCache instance;
    private WeakHashMap<String, List<ConflictDescriptionParser.ParserConflictDescriptor>> cachedConflicts;

    private EntriesCache() {
        cachedConflicts = new WeakHashMap<String, List<ConflictDescriptionParser.ParserConflictDescriptor>>(5);
    }

    static EntriesCache getInstance() {
        if(instance == null) {
            instance = new EntriesCache();
        }
        return instance;
    }

    Map<String, String> getFileAttributes(File file) throws IOException, SAXException {
        return getFileAttributes(file, true);
    }

    private Map<String, String> getFileAttributes(File file, boolean mergeWithParent) throws IOException, SAXException {
        File entriesFile = file == null ? null : SvnWcUtils.getEntriesFile(file);
        if(entriesFile==null) {
            return null;
        }
        return getFileAttributes(entriesFile, file, mergeWithParent);
    }

    private synchronized Map<String, String> getFileAttributes(final File entriesFile, final File file, boolean mergeWithParent) throws IOException, SAXException {
        EntryAttributes ea = getEntryAttributes(entriesFile, file, mergeWithParent);
        return ea.get(file.isDirectory() ? SVN_THIS_DIR : file.getName());
    }

    String[] getChildren (File file) throws IOException, SAXException {
        File entriesFile = SvnWcUtils.getSvnFile(file, SvnWcUtils.ENTRIES);
        String[] children = new String[0];
        if (entriesFile != null) {
            synchronized (this) {
                EntryAttributes ea = getEntryAttributes(entriesFile, file, false);
                if (ea.size() > 1) {
                    children = new String[ea.size() - 1];
                    int i = 0;
                    for (String child : ea.keySet()) {
                        if (!SVN_THIS_DIR.equals(child)) {
                            children[i++] = child;
                        }
                    }
                }
            }
        }
        return children;
    }

    private EntryAttributes getEntryAttributes (File entriesFile, File file, boolean mergeWithParent) throws IOException, SAXException {
        EntriesFile ef = getEntries().get(entriesFile.getAbsolutePath());
        long lastModified = entriesFile.lastModified();
        long fileLength = entriesFile.length();
        if(ef == null || ef.ts != lastModified || ef.size != fileLength) {
            EntryAttributes ea = getAttributesFromEntriesFile(entriesFile);
            ef = new EntriesFile(getMergedAttributes(ea), lastModified, fileLength);
            getEntries().put(entriesFile.getAbsolutePath(), ef);
        }
        boolean isDirectory = file.isDirectory();
        if(ef.attributes.get(file.getName()) == null && !isDirectory) { // do not keep directory itself among its entries - it's kept rather as svn:this_dir
            // file does not exist in the svn metadata and
            // wasn't added to the entires cache yet
            Map<String, String> attributes  = mergeThisDirAttributes(false, file.getName(), ef.attributes);
        }

        if (isDirectory && mergeWithParent) { // sadly, conflicts are always kept in parent's metadata, even for a folder
            mergeDirWithParent(ef.attributes.get(SVN_THIS_DIR), file);
        }

        return ef.attributes;
    }

    private void mergeDirWithParent (Map<String, String> folderAttributes, File folder) throws IOException, SAXException {
        Map<String, String> parentAttributes = getFileAttributes(folder.getParentFile(), false);
        if (parentAttributes != null) {
            String treeConflicts = parentAttributes.get(ATTR_TREE_CONFLICTS);
            String fileName = folder.getName();
            SVNConflictDescriptor desc = getConflictDescriptor(fileName, treeConflicts);
            if (desc != null) {
                folderAttributes.put(WorkingCopyDetails.ATTR_TREE_CONFLICT_DESCRIPTOR, treeConflicts); //NOI18
            } else {
                folderAttributes.remove(WorkingCopyDetails.ATTR_TREE_CONFLICT_DESCRIPTOR);
            }
        }
    }

    private EntryAttributes getMergedAttributes(EntryAttributes ea) throws SAXException {
        for(String fileName : ea.keySet()) {
            String kind = ea.get(fileName).get("kind");
            if (kind == null) {
                // missing svn node type (dir or file) - svn allows that and considers such node to be missing.
                Subversion.LOG.log(Level.INFO, "File " + fileName + " is missing - metadata: " + ea.get(fileName));
                Subversion.LOG.log(Level.WARNING, "File " + fileName + " probably does not exist on the hard drive, please check your working copy.");
                kind = "file";
                ea.get(fileName).put("kind", kind);
            }
            boolean isDirectory = kind.equals("dir");
            Map<String, String> attributes = mergeThisDirAttributes(isDirectory, fileName, ea);
            if(isDirectory) {
                attributes.put(WorkingCopyDetails.IS_HANDLED, (ea.get(SVN_THIS_DIR).get("deleted") == null) ? "true" : "false");  // NOI18N
            } else {
                if(ea.get(fileName) != null) {
                    for(Map.Entry<String, String> entry : ea.get(fileName).entrySet()) {
                        attributes.put(entry.getKey(), entry.getValue());
                    }
                }
                // it's realy a file
                attributes.put(WorkingCopyDetails.IS_HANDLED, (ea.containsKey(fileName) && ea.get("deleted") == null) ? "true" : "false");        // NOI18N
            }
        }
        return ea;
    }

    private Map<String, String> mergeThisDirAttributes(final boolean isDirectory, final String fileName, final EntryAttributes ea) {
        Map<String, String> attributes = ea.get(fileName);
        if(attributes == null) {
           attributes = new HashMap<String, String>();
           ea.put(fileName, attributes);
        }
        if (!ea.containsKey(SVN_THIS_DIR)) {
            return attributes;
        }
        for(Map.Entry<String, String> entry : ea.get(SVN_THIS_DIR).entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (WorkingCopyDetails.ATTR_TREE_CONFLICT_DESCRIPTOR.equals(key)) { // do not inherit this flag
                continue;
            } else if(isDirectory) {
                attributes.put(key, value);
            } else {
                if(key.equals("url")) {
                    if( attributes.get(key) == null ) {
                        attributes.put(key, value + "/" + fileName);
                    }
                } else if( key.equals("uuid") ||
                           key.equals("repos") ||
                           key.equals("revision") ||
                           key.equals(WorkingCopyDetails.VERSION_ATTR_KEY)) {
                    if( attributes.get(key) == null ) {
                        attributes.put(key, value);
                    }
                } else if (ATTR_TREE_CONFLICTS.equals(key)) { //NOI18N
                    SVNConflictDescriptor desc = getConflictDescriptor(fileName, value);
                    if (desc != null) {
                        attributes.put(WorkingCopyDetails.ATTR_TREE_CONFLICT_DESCRIPTOR, value);
                        attributes.put(WorkingCopyDetails.IS_HANDLED, ea.containsKey(fileName) && ea.get("deleted") == null ? "true" : "false");
                    }
                }
            }
        }
        return attributes;
    }

    private EntryAttributes getAttributesFromEntriesFile(File entriesFile) throws IOException, SAXException {
        //We need to check the first line of the File.
        //If it is a number, its the new format.
        //Otherwise, treat it as XML
        boolean isXml = false;
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(entriesFile), "UTF8"));
        try {
            String firstLine = fileReader.readLine();
            try {
                Integer.valueOf(firstLine);
                isXml = false;
            } catch (NumberFormatException ex) {
                isXml = true;
            }

            if (isXml) {
                return loadAttributesFromXml(entriesFile);
            } else {
                return loadAttributesFromPlainText(fileReader, entriesFile);
            }
        } finally {
            fileReader.close();
        }
    }

    private EntryAttributes loadAttributesFromXml(File entriesFile) throws IOException, SAXException {
        //Parse the entries file
        XMLReader saxReader = XMLUtil.createXMLReader();
        XmlEntriesHandler xmlEntriesHandler = new XmlEntriesHandler();
        saxReader.setContentHandler(xmlEntriesHandler);
        saxReader.setErrorHandler(xmlEntriesHandler);
        InputStream inputStream = new java.io.FileInputStream(entriesFile);

        try {
            saxReader.parse(new InputSource(inputStream));
        } catch (SAXException ex) {
            throw ex;
        } finally {
            inputStream.close();
        }
        return xmlEntriesHandler.getEntryAttributes();
    }

    //New entries file format, as of SVN 1.4.0
    private EntryAttributes loadAttributesFromPlainText(BufferedReader entriesReader, File entryFile) throws IOException {
        String entryFilePath = entryFile.getAbsolutePath();
        EntryAttributes returnValue = new EntryAttributes();

        int attrIndex = 0;

        String entryName = null;
        Map<String, String> attributes = new HashMap<String, String>();

        String nextLine = attributePool.get(entriesReader.readLine());
        if (nextLine == null && new File(entryFile.getParentFile().getParentFile(), SvnUtils.SVN_WC_DB).exists()) {
            throw new IOException(WC17FORMAT);
        }
        while (nextLine != null) {
            if (attrIndex == 0) {
                entryName = nextLine;
                if (entryName.equals("")) {
                    entryName = SVN_THIS_DIR;
                }
            }

            if (!("".equals(nextLine))) {
                if (isBooleanValue(entryFileAttributes[attrIndex])) {
                    nextLine = "true";
                }
                attributes.put(entryFileAttributes[attrIndex], nextLine);
            }
            attrIndex++;
            nextLine = attributePool.get(entriesReader.readLine());

            if(nextLine != null && attrIndex > entryFileAttributes.length - 1) {
                Subversion.LOG.fine("Skipping attribute from position " + attrIndex + " in entry file " + entryFilePath);  // NOI18N
                for( ; nextLine != null && !DELIMITER.equals(nextLine); nextLine = attributePool.get(entriesReader.readLine()));
            }

            if (DELIMITER.equals(nextLine)) {
                attributes.put(WorkingCopyDetails.VERSION_ATTR_KEY, WorkingCopyDetails.VERSION_14);
                returnValue.put(entryName, attributes);
                attributes = new HashMap<String, String>();
                attrIndex = 0;
                nextLine = attributePool.get(entriesReader.readLine());
                continue;
            }

        }
        return returnValue;
    }

    private static boolean isBooleanValue(String attribute) {
        return BOOLEAN_ATTRIBUTES.contains(attribute);
    }

    SVNConflictDescriptor getConflictDescriptor (String fileName, String conflictsDescription) {
        SVNConflictDescriptor desc = null;
        if (conflictsDescription != null) {
            ParserConflictDescriptor[] conflicts = getConflicts(conflictsDescription);
            for (ParserConflictDescriptor conflict : conflicts) {
                if (fileName.equals(conflict.getFileName())) {
                    desc = conflict;
                    break;
                }
            }
        }
        return desc;
    }

    private synchronized ParserConflictDescriptor[] getConflicts (String conflictsDescription) {
        List<ParserConflictDescriptor> conflicts = cachedConflicts.get(conflictsDescription);
        if (conflicts == null) {
            ConflictDescriptionParser cdp = ConflictDescriptionParser.parseDescription(conflictsDescription);
            conflicts = cdp.getConflicts();
            cachedConflicts.put(conflictsDescription, conflicts);
        }
        return conflicts.toArray(new ParserConflictDescriptor[conflicts.size()]);
    }

    private class XmlEntriesHandler extends DefaultHandler {

        private static final String ENTRY_ELEMENT_NAME = "entry";  // NOI18N
        private static final String NAME_ATTRIBUTE = "name";  // NOI18N
        private EntryAttributes entryAttributes;

        public void startElement(String uri, String localName, String qName, Attributes elementAttributes) throws SAXException {
            if (ENTRY_ELEMENT_NAME.equals(qName)) {
                Map<String, String> attributes = new HashMap<String, String>();
                for (int i = 0; i < elementAttributes.getLength(); i++) {
                    String name = attributePool.get(elementAttributes.getQName(i));
                    String value = attributePool.get(elementAttributes.getValue(i));
                    attributes.put(name, value);
                }

                String nameValue = attributes.get(NAME_ATTRIBUTE);
                if (nameValue == null || "".equals(nameValue)) {
                    nameValue = SVN_THIS_DIR;
                }
                if(entryAttributes == null) {
                    entryAttributes = new EntryAttributes();
                }
                attributes.put(WorkingCopyDetails.VERSION_ATTR_KEY, WorkingCopyDetails.VERSION_13);
                entryAttributes.put(nameValue, attributes);
            }
        }

        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        public EntryAttributes getEntryAttributes() {
            return entryAttributes;
        }
    }

    private Entries getEntries() {
        if(entries == null) {
            entries = new Entries();
        }
        return entries;
    }

    private class EntriesFile {
        long ts;
        long size;
        EntryAttributes attributes;
        EntriesFile(EntryAttributes attributes, long ts, long size) {
            this.ts = ts;
            this.size = size;
            this.attributes = attributes;
        }
    }

    private class Entries extends LinkedHashMap<String, EntriesFile> {
        @Override
        protected boolean removeEldestEntry(Entry<String, EntriesFile> eldest) {
            return MAX_SIZE > -1 && size() > MAX_SIZE;
        }
    };

    private class EntryAttributes extends HashMap<String, Map<String, String>> {
        public EntryAttributes() {}
        public EntryAttributes(int initialCapacity) {
            super(initialCapacity);
        }
    };

    private class AttributePool {        
        private Map<String, String> m = new WeakHashMap<String, String>();
        public String get(String str) {
            if(str == null ) return null;
            if(DELIMITER.equals(str)) {
                return DELIMITER;
            }
            String val = m.get(str);
            if (val == null) {
                m.put(str, str);
                return str;
            }
            return val;
        }        
    }
}

