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

package org.netbeans.installer.utils.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Kirill Sorokin
 */
public class FilesList implements Iterable<FileEntry> {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File listFile;
    private File tempFile;
    
    private List<FileEntry> entries;
    
    private int size;
    
    // constructors /////////////////////////////////////////////////////////////////
    public FilesList() {
        entries = new ArrayList<FileEntry>(CACHE_SIZE);
    }
    
    public FilesList(final File xml) throws IOException, XMLException {
        this();
        
        loadXml(xml);
    }
    
    // add/remove ///////////////////////////////////////////////////////////////////
    public void add(
            final File file) throws IOException {
        add(new FileEntry(file));
    }
    
    public void add(
            final FileEntry entry) throws IOException {
        final String name = entry.getName();
        
        int index = 0;
        while (index < entries.size()) {
            final String current = entries.get(index).getName();
            
            if (current.length() < name.length()) {
                break;
            } else if (current.equals(name)) {
                return;
            }
            
            index++;
        }
        
        entries.add(index, entry);
        size++;
        
        if (entries.size() == CACHE_SIZE) {
            save();
        }
    }
    
    public void add(
            final List<File> list) throws IOException {
        for (File file: list) {
            add(file);
        }
    }
    
    public void add(
            final FilesList list) throws IOException {
        for (FileEntry entry: list) {
            add(entry);
        }
    }
    
    public void clear() throws IOException {
        if (listFile != null) {
            FileUtils.deleteFiles(listFile, tempFile);
        }
        entries.clear();
        size = 0;
    }
    
    // getters //////////////////////////////////////////////////////////////////////
    public int getSize() {
        return size;
    }
    
    // list <-> xml /////////////////////////////////////////////////////////////////
    public FilesList loadXml(final File xml) throws XMLException {
        return loadXml(xml, null);
    }
    
    public FilesList loadXml(final File xml, final File root) throws XMLException {
        try {
            InputStream in = new FileInputStream(xml);
            
            loadXml(in, root);
            in.close();
            
            return this;
        } catch (IOException e) {
            throw new XMLException("Cannot parse xml file", e);
        }
    }
    
    public FilesList loadXmlGz(final File xml) throws XMLException {
        return loadXmlGz(xml, null);
    }
    
    public FilesList loadXmlGz(final File xml, final File root) throws XMLException {
        try {
            InputStream in = new GZIPInputStream(new FileInputStream(xml));
            
            loadXml(in, root);
            in.close();
            
            return this;
        } catch (IOException e) {
            throw new XMLException("Cannot parse xml file", e);
        }
    }
    
    public void saveXml(final File xml) throws XMLException {
        try {
            OutputStream out = new FileOutputStream(xml);
            
            saveXml(out);
            
            out.close();
        } catch (UnsupportedEncodingException e) {
            throw new XMLException("Cannot save XML", e);
        } catch (IOException e) {
            throw new XMLException("Cannot save XML", e);
        }
    }
    
    public void saveXmlGz(final File xml) throws XMLException {
        try {
            OutputStream out = new GZIPOutputStream(new FileOutputStream(xml));
            
            saveXml(out);
            
            out.close();
        } catch (UnsupportedEncodingException e) {
            throw new XMLException("Cannot save XML", e);
        } catch (IOException e) {
            throw new XMLException("Cannot save XML", e);
        }
    }
    
    // list <-> list :) /////////////////////////////////////////////////////////////
    public List<File> toList() {
        final List<File> files = new ArrayList<File>(size);
        
        for (FileEntry entry: this) {
            files.add(entry.getFile());
        }
        
        return files;
    }
    
    // iterable /////////////////////////////////////////////////////////////////////
    public Iterator<FileEntry> iterator() {
        return new FilesListIterator();
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void save() throws IOException {
        if (entries.size() > 0) {
            if (listFile == null) {
                listFile = FileUtils.createTempFile();
                tempFile = FileUtils.createTempFile();
            }
            
            final BufferedReader reader;
            if (listFile.length() > 0) {
                reader =
                        new BufferedReader(
                        new InputStreamReader(
                        new GZIPInputStream(
                        new FileInputStream(listFile))));
            } else {
                reader =
                        new BufferedReader(
                        new FileReader(listFile));
            }
            final BufferedWriter writer =
                    new BufferedWriter(
                    new OutputStreamWriter(
                    new GZIPOutputStream(
                    new FileOutputStream(tempFile))));
            
            int index = 0;
            FileEntry saved = readEntry(reader);
            
            while ((index < entries.size()) && (saved != null)) {
                final String unsavedName = entries.get(index).getName();
                final String savedName   = saved.getName();
                
                if (savedName.equals(unsavedName)) {
                    if ((index < entries.size() - 1) &&
                            entries.get(index + 1).getName().equals(unsavedName)) {
                        index++;
                    } else {
                        saved = readEntry(reader);
                    }
                    size--;
                } else {
                    if (unsavedName.length() < savedName.length()) {
                        writeEntry(saved, writer);
                        saved = readEntry(reader);
                    } else {
                        writeEntry(entries.get(index), writer);
                        index++;
                    }
                }
            }
            
            while (index < entries.size()) {
                writeEntry(entries.get(index), writer);
                index++;
            }
            
            while (saved != null) {
                writeEntry(saved, writer);
                saved = readEntry(reader);
            }
            
            reader.close();
            
            writer.flush();
            writer.close();
            
            FileUtils.copyFile(tempFile, listFile);
            
            entries.clear();
        }
    }
    
    private FileEntry readEntry(
            final BufferedReader reader) throws IOException {
        final String name = reader.readLine();
        
        if (name != null) {
            final File file = new File(name);
            final boolean directory = Boolean.parseBoolean(reader.readLine());
            
            if (directory) {
                final boolean empty = Boolean.parseBoolean(reader.readLine());
                final long modified = Long.parseLong(reader.readLine());
                final int permissions = Integer.parseInt(reader.readLine(), 8);
                
                return new FileEntry(
                        file,
                        empty,
                        modified,
                        permissions);
            } else {
                final long size = Long.parseLong(reader.readLine());
                final String md5 = reader.readLine();
                final boolean jarFile = Boolean.parseBoolean(reader.readLine());
                final boolean packed = Boolean.parseBoolean(reader.readLine());
                final boolean signed = Boolean.parseBoolean(reader.readLine());
                final long modified = Long.parseLong(reader.readLine());
                final int permissions = Integer.parseInt(reader.readLine(), 8);
                
                return new FileEntry(
                        file,
                        size,
                        md5,
                        jarFile,
                        packed,
                        signed,
                        modified,
                        permissions);
            }
        }
        
        return null;
    }
    
    private void writeEntry(
            final FileEntry entry,
            final Writer writer) throws IOException {
        if (entry.getFile().exists() && !entry.isMetaDataReady()) {
            entry.calculateMetaData();
        }
        
        writer.write(entry.toString());
    }
    
    private void saveXml(
            final OutputStream out) throws IOException {
        final PrintWriter writer =
                new PrintWriter(new OutputStreamWriter(out, ENCODING));
        
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<files-list>");
        
        for (FileEntry entry: this) {
            if (entry.getFile().exists() && !entry.isMetaDataReady()) {
                entry.calculateMetaData();
            }
            
            writer.println("    " + entry.toXml());
        }
        
        writer.println("</files-list>");
        
        writer.flush();
    }
    
    private void loadXml(
            final InputStream in,
            final File root) throws IOException, XMLException {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final SAXParser parser;
        
        try {
            parser = factory.newSAXParser();
            
            parser.parse(new InputSource(in), new FilesListHandler(root));
        } catch (SAXException e) {
            throw new XMLException("Cannot load files list from xml", e);
        } catch (ParserConfigurationException e) {
            throw new XMLException("Cannot load files list from xml", e);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private class FilesListHandler extends DefaultHandler {
        private boolean entryElement;
        
        private File root;
        
        private String name;
        
        private boolean directory;
        private boolean empty;
        
        private long size;
        private String md5;
        private boolean jarFile;
        private boolean packed;
        private boolean signed;
        private long modified;
        private int permissions;
        
        public FilesListHandler(File root) {
            this.root = root;
        }
        
        public void startElement(
                final String uri,
                final String localName,
                final String qName,
                final Attributes attributes) throws SAXException {
            if (qName.equals("entry")) {
                entryElement = true;
                
                String type = attributes.getValue("type");
                if (type.equals("file")) {
                    directory = false;
                    
                    size = Long.parseLong(attributes.getValue("size"));
                    md5 = attributes.getValue("md5");
                    jarFile = Boolean.parseBoolean(attributes.getValue("jar"));
                    
                    if (jarFile) {
                        packed = Boolean.parseBoolean(attributes.getValue("packed"));
                        signed = Boolean.parseBoolean(attributes.getValue("signed"));
                    } else {
                        packed  = false;
                        signed  = false;
                    }
                    
                    modified = Long.parseLong(attributes.getValue("modified"));
                    permissions = Integer.parseInt(attributes.getValue("permissions"), 8);
                } else {
                    directory = true;
                    empty = Boolean.parseBoolean(attributes.getValue("empty"));
                    modified = Long.parseLong(attributes.getValue("modified"));
                    permissions = Integer.parseInt(attributes.getValue("permissions"), 8);
                }
            } else {
                entryElement = false;
            }
        }
        
        public void characters(
                final char[] characters,
                final int start,
                final int length) throws SAXException {
            if (entryElement) {
                final String value = new String(characters, start, length);
                
                if (name == null) {
                    name = value;
                } else {
                    name += value;
                }
            }
        }
        
        public void endElement(
                final String uri,
                final String localName,
                final String qName) throws SAXException {
            if (entryElement) {
                final File file;
                if (root == null) {
                    file = new File(name);
                } else {
                    file = new File(root, name);
                }
                
                name = null;
                
                FileEntry entry;
                
                if (directory) {
                    entry = new FileEntry(
                            file,
                            empty,
                            modified,
                            permissions);
                } else {
                    entry = new FileEntry(
                            file,
                            size,
                            md5,
                            jarFile,
                            packed,
                            signed,
                            modified,
                            permissions);
                }
                
                entryElement = false;
                
                try {
                    FilesList.this.add(entry);
                } catch (IOException e) {
                    throw new SAXException("Could not add an entry", e);
                }
            }
        }
    }
    
    private class FilesListIterator implements Iterator<FileEntry> {
        private int sizeAtConstruction;
        private boolean listInMemory;
        
        private int index;
        private BufferedReader reader;
        
        private FileEntry next;
        
        public FilesListIterator() {
            // if the list size is already bigger than can be reposited in memory -
            // make sure that all entries are present in the cache file; and set the
            // iteration mode (over memory or over cache file)
            if (FilesList.this.listFile != null) {
                try {
                    FilesList.this.save();
                } catch (IOException e) {
                    ErrorManager.notifyError("Cannot save list", e);
                }
                
                listInMemory = false;
                try {
                    reader = new BufferedReader(
                            new InputStreamReader(
                            new GZIPInputStream(
                            new FileInputStream(FilesList.this.listFile))));
                    
                } catch (IOException e) {
                    ErrorManager.notifyError("Cannot open reader to the list file", e);
                }
            } else {
                listInMemory = true;
                index = 0;
            }
            
            sizeAtConstruction = FilesList.this.size;
        }
        
        public boolean hasNext() {
            if (sizeAtConstruction != FilesList.this.size) {
                throw new ConcurrentModificationException("The list was changed, while iterating");
            }
            
            if (next == null) {
                next = next();
            }
            
            return next != null;
        }
        
        public FileEntry next() {
            if (next != null) {
                final FileEntry temp = next;
                next = null;
                
                return temp;
            } else {
                FileEntry entry = null;
                
                if (listInMemory) {
                    if (index < FilesList.this.entries.size()) {
                        entry = FilesList.this.entries.get(index++);
                    }
                } else {
                    try {
                        entry = FilesList.this.readEntry(reader);
                        
                        if (entry == null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        ErrorManager.notifyError("Cannot read next entry", e);
                    }
                }
                
                return entry;
            }
        }
        
        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported for files list");
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int CACHE_SIZE =
            2500;
    
    public static final String ENCODING =
            StringUtils.ENCODING_UTF8; // NOI18N
}
