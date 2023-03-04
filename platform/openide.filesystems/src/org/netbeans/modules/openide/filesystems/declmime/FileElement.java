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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
/**
 * Represents a resolving process made using a <tt>file</tt> element.
 * <p>
 * Responsible for pairing and performing fast check followed by optional
 * rules and if all matches returning MIME type.
 */
final class FileElement {
    FileElement() {
    }
    Type fileCheck = new Type();
    private String mime = null;
    XMLMIMEComponent rule = null;
    // unique string to mark exit condition
    static final String EXIT_MIME_TYPE = "mime-type-to-exit"; //NOI18N

    String[] getExtensions() {
        return fileCheck.exts;
    }

    List<Type.FileName> getNames() {
        return fileCheck.names;
    }

    String getMimeType() {
        return mime;
    }

    private boolean isExit() {
        return fileCheck.exit;
    }

    void setMIME(String mime) {
        if ("null".equals(mime)) {
            return; // NOI18N
        }
        this.mime = mime;
    }

    String resolve(FileObject file) {
        try {
            if (fileCheck.accept(file)) {
                if (rule != null && !rule.acceptFileObject(file)) {
                    return null;
                }
                if (isExit() || mime == null) {
                    // all matched but exit element was found or mime attribute of resolver element is null => escape this resolver
                    return EXIT_MIME_TYPE;
                }
                // all matched
                return mime;
            }
        } catch (IOException io) {
            Logger.getLogger(MIMEResolverImpl.class.getName()).log(Level.INFO, "IOException in resolver " + this, io);
        }
        return null;
    }

    /**
     * For debug puroses only.
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("FileElement(");
        buf.append(fileCheck).append(' ');
        buf.append(rule).append(' ');
        buf.append("Result:").append(mime);
        return buf.toString();
    }

    public void writeExternal(DataOutput out) throws IOException {
        Util.writeUTF(out, mime);
        fileCheck.writeExternal(out);
        if (rule != null) {
            out.writeBoolean(true);
            rule.writeExternal(out);
        } else {
            out.writeBoolean(false);
        }
    }

    public void readExternal(DataInput in) throws IOException {
        mime = Util.readUTF(in);
        fileCheck.readExternal(in);
        if (in.readBoolean()) {
            rule = new XMLMIMEComponent(in);
        } 
    }
        
    /**
     * Hold data from XML document and performs first stage check according to them.
     * <p>
     * The first stage check is responsible for filtering files according  to their 
     * attributes provided by lower layers.
     * <p>
     * We could generate hardwired class bytecode on a fly.
     */
    static class Type {
        private static final String EMPTY_EXTENSION = "";  //NOI18N

        Type() {}
        private String[] exts;
        private String[] mimes;
        private String[] fatts;
        private String[] vals;   // contains null or value of attribute at the same index
        private boolean exit;
        private byte[]   magic;
        private byte[]   mask;
        private List<FilePattern> patterns;
        private List<FileName> names;
        private transient FilePattern lastAddedPattern;
        
        /** Checks whether the type is valid. At least one of the fields has
         * to be specified.
         */
        final boolean isValid() {
            return exts != null ||
                   mimes != null ||
                   fatts != null ||
                   patterns != null ||
                   names != null ||
                   magic != null;
        }

        private void writeExternal(DataOutput out) throws IOException {
            Util.writeStrings(out, exts);
            Util.writeStrings(out, mimes);
            Util.writeStrings(out, fatts);
            Util.writeStrings(out, vals);
            out.writeBoolean(exit);
            Util.writeBytes(out, magic);
            Util.writeBytes(out, mask);
            if (patterns == null) {
                out.writeInt(-1);
            } else {
                out.writeInt(patterns.size());
                for (FilePattern p : patterns) {
                    p.writeExternal(out);
                }
            }
            if (names == null) {
                out.writeInt(-1);
            } else {
                out.writeInt(names.size());
                for (FileName n : names) {
                    n.writeExternal(out);
                }
            }
        }

        private void readExternal(DataInput in) throws IOException {
            exts = Util.readStrings(in);
            mimes = Util.readStrings(in);
            fatts = Util.readStrings(in);
            vals = Util.readStrings(in);
            exit = in.readBoolean();
            magic = Util.readBytes(in);
            mask = Util.readBytes(in);
            int patternsSize = in.readInt();
            if (patternsSize >= 0) {
                patterns = new ArrayList<FilePattern>(patternsSize);
                for (int i = 0; i < patternsSize; i++) {
                    patterns.add(new FilePattern(in));
                }
            }
            int namesSize = in.readInt();
            if (namesSize >= 0) {
                names = new ArrayList<FileName>(namesSize);
                for (int i = 0; i < namesSize; i++) {
                    names.add(new FileName(in));
                }
            }
        }

        /** Used to search in the file for given pattern in given range. If there is an inner
         * pattern element, it is used only if outer is fulfilled. Searching starts
         * always from the beginning of the file. For example:
         * <p>
         * Pattern &lt;?php in first 255 bytes
         * <pre>
         *      &lt;pattern value="&lt;?php" range="255"/&gt;
         * </pre>
         * </p>
         * <p>
         * Pattern &lt;HTML&gt;> or &lt;html&gt; in first 255 bytes and pattern &lt;?php in first 4000 bytes.
         * <pre>
         *      &lt;pattern value="&lt;HTML&gt;" range="255" ignorecase="true"&gt;
         *          &lt;pattern value="&lt;?php" range="4000"/&gt;
         *      &lt;/pattern&gt;
         * </pre>
         * </p>
         */
        class FilePattern {
            // case sensitive by default
            static final boolean DEFAULT_IGNORE_CASE = false;
            private final String value;
            private final int range;
            private final boolean ignoreCase;
            private final byte[] bytes;
            private final int valueLength;
            private FilePattern inner;

            public FilePattern(String value, int range, boolean ignoreCase) {
                this.value = value;
                this.valueLength = value.length();
                if (ignoreCase) {
                    this.bytes = value.toLowerCase().getBytes();
                } else {
                    this.bytes = value.getBytes();
                }
                this.range = range;
                this.ignoreCase = ignoreCase;
            }
            
            public FilePattern(DataInput is) throws IOException {
                this(
                    Util.readUTF(is), is.readInt(), is.readBoolean()
                );
                if (is.readBoolean()) {
                    inner = new FilePattern(is);
                }
            }
            
            public void writeExternal(DataOutput os) throws IOException {
                Util.writeUTF(os, value);
                os.writeInt(range);
                os.writeBoolean(ignoreCase);
                if (inner != null) {
                    os.writeBoolean(true);
                    inner.writeExternal(os);
                } else {
                    os.writeBoolean(false);
                }
            }

            public void setInner(FilePattern inner) {
                this.inner = inner;
            }

            private boolean match(byte b, AtomicInteger pointer) {
                if (b == bytes[pointer.get()]) {
                    return pointer.incrementAndGet() >= valueLength;
                } else {
                    pointer.set(0);
                    return false;
                }
            }

            /** Read from given file and compare byte-by-byte if pattern
             * appers in given range.
             */
            public boolean match(FileObject fo) throws IOException {
                InputStream is = null;
                boolean matched = false;
                try {
                    is = fo.getInputStream();  // it is CachedInputStream, so you can call getInputStream and read more times without performance penalty
                    byte[] byteRange = new byte[range];
                    int read = is.read(byteRange);
                    AtomicInteger pointer = new AtomicInteger(0);
                    for (int i = 0; i < read; i++) {
                        byte b = byteRange[i];
                        if (ignoreCase) {
                            b = (byte) Character.toLowerCase(b);
                        }
                        if (match(b, pointer)) {
                            matched = true;
                            break;
                        }
                    }
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException ioe) {
                        // already closed
                    }
                }
                if (matched) {
                    if (inner == null) {
                        return true;
                    } else {
                        return inner.match(fo);
                    }
                }
                return false;
            }

            @Override
            public String toString() {
                return "[" + value + ", " + range + ", " + ignoreCase + (inner != null ? ", " + inner : "") + "]";
            }
        }

        /** Used to compare filename with given name.
         * For example:
         * <p>
         * Filename matches makefile, Makefile, MaKeFiLe, mymakefile, gnumakefile, makefile1, ....
         * <pre>
         *      &lt;name name="makefile" substring="true"/&gt;
         * </pre>
         * </p>
         * <p>
         * Filename exactly matches rakefile or Rakefile.
         * <pre>
         *      &lt;name name="rakefile" ignorecase="false"/&gt;
         *      &lt;name name="Rakefile" ignorecase="false"/&gt;
         * </pre>
         * </p>
         */
        class FileName {

            // case insensitive by default
            static final boolean DEFAULT_IGNORE_CASE = true;
            static final boolean DEFAULT_SUBSTRING = false;
            private final String name;
            private final boolean substring;
            private final boolean ignoreCase;

            public FileName(String name, boolean substring, boolean ignoreCase) {
                if (ignoreCase) {
                    this.name = name.toLowerCase();
                } else {
                    this.name = name;
                }
                this.substring = substring;
                this.ignoreCase = ignoreCase;
            }
            
            public FileName(DataInput is) throws IOException {
                this(
                    Util.readUTF(is), is.readBoolean(), is.readBoolean()
                );
            }
            
            public void writeExternal(DataOutput os) throws IOException {
                Util.writeUTF(os, name);
                os.writeBoolean(substring);
                os.writeBoolean(ignoreCase);
            }

            public boolean match(FileObject fo) {
                String nameAndExt = fo.getNameExt();
                if (ignoreCase) {
                    nameAndExt = nameAndExt.toLowerCase();
                }
                if (substring) {
                    return nameAndExt.contains(name);
                } else {
                    return nameAndExt.equals(name);
                }
            }

            @Override
            public String toString() {
                return "[" + name + ", " + substring + ", " + ignoreCase + "]";
            }
        }

        /**
         * For debug purposes only.
         */
        @Override
        public String toString() {
            int i = 0;
            StringBuffer buf = new StringBuffer();

            buf.append("fast-check(");
            
            if (exts != null) {
                buf.append("exts:");            
                for (i = 0; i<exts.length; i++)
                    buf.append(exts[i]).append(", ");
            }
            
            if (mimes != null) {
                buf.append("mimes:");
                for (i = 0; i<mimes.length; i++)
                    buf.append(mimes[i]).append(", ");
            }
            
            if (fatts != null) {
                buf.append("file-attributes:");
                for (i = 0; i<fatts.length; i++)
                    buf.append(fatts[i]).append("='").append(vals[i]).append("', ");
            }

            if (patterns != null) {
                buf.append("patterns:");
                for (FilePattern pattern : patterns) {
                    buf.append(pattern.toString()).append(", ");
                }
            }

            if (names != null) {
                buf.append("names:");
                for (FileName name : names) {
                    buf.append(name.toString()).append(", ");
                }
            }

            if (magic != null) {
                buf.append("magic:").append(XMLUtil.toHex(magic, 0, magic.length));
            }
            
            if (mask != null) {
                buf.append("mask:").append(XMLUtil.toHex(mask, 0, mask.length));
            }

            buf.append(')');
            
            return buf.toString();
        }
        
        final void addExt(String ext) {
            exts = Util.addString(exts, ext);
        }

        final void addMIME(String mime) {
            mimes = Util.addString(mimes, mime.toLowerCase());
        }
        
        final void addAttr(String name, String value) {
            fatts = Util.addString(fatts, name);
            vals = Util.addString(vals, value);
        }

        final void addPattern(String value, int range, boolean ignoreCase) {
            if (patterns == null) {
                patterns = new ArrayList<FilePattern>();
            }
            lastAddedPattern = new FilePattern(value, range, ignoreCase);
            patterns.add(lastAddedPattern);
        }

        final void addInnerPattern(String value, int range, boolean ignoreCase) {
            FilePattern inner = new FilePattern(value, range, ignoreCase);
            lastAddedPattern.setInner(inner);
            lastAddedPattern = inner;
        }

        final void addName(String name, boolean substring, boolean ignoreCase) {
            if (names == null) {
                names = new ArrayList<FileName>();
            }
            names.add(new FileName(name, substring, ignoreCase));
        }

        final boolean setMagic(byte[] magic, byte[] mask) {
            if (magic == null) return true;
            if (mask != null && magic.length != mask.length) return false;            
            this.magic = magic;
            if (mask != null) {
                this.mask = mask;
                for (int i = 0; i<mask.length; i++) {
                    this.magic[i] &= mask[i];
                }
            }
            return true;
        }

        final void setExit() {
            exit = true;
        }

        @SuppressWarnings("deprecation")
        private static String getMIMEType(String extension) {
            return FileUtil.getMIMEType(extension);
        }

        /** #26521, 114976 - ignore not readable and windows' locked files. */
        private static void handleIOException(FileObject fo, IOException ioe) throws IOException {
            if (fo.canRead()) {
                if (!BaseUtilities.isWindows() || !(ioe instanceof FileNotFoundException) || !fo.isValid() || !fo.getName().toLowerCase().contains("ntuser")) {//NOI18N
                    throw ioe;
                }
            }
        }

        private boolean accept(FileObject fo) throws IOException {
            // check for resource extension
            if (exts != null) {
                String ext = fo.getExt();
                if (ext == null) {
                    ext = EMPTY_EXTENSION;
                }
                if (!Util.contains(exts, ext, MIMEResolverImpl.CASE_INSENSITIVE)) {
                    return false;
                }
            }
            
            // check for resource mime type

            if (mimes != null) {
                boolean match = false;
                String s = getMIMEType(fo.getExt());  //from the very first implementation there is still question "how to obtain resource MIME type as classified by lower layers?"
                if (s == null) return false;

                // RFC2045; remove content type paramaters and ignore case
                int l = s.indexOf(';');
                if (l>=0) s = s.substring(0, l);
                s = s.toLowerCase();

                for (int i = mimes.length -1 ; i>=0; i--) {
                    if (s.equals(mimes[i])) {
                        match = true;
                        break;
                    }

                    // RFC3023; allows "+xml" suffix
                    if (mimes[i].length() > 0 && mimes[i].charAt(0) == '+' && s.endsWith(mimes[i])) {
                        match = true;
                        break;
                    }
                }
                if (!match) return false;
            }
            
            // check for magic
            
            if (magic != null) {
                byte[] header = new byte[magic.length];

                // fetch header

                InputStream in = null;
                try {
                    in = fo.getInputStream();
                    int read = in.read(header);
                    if (read < 0) {
                        return false;
                    }
                } catch (IOException openex) {
                    handleIOException(fo, openex);
                    return false;
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException ioe) {
                        // already closed
                    }
                }

                // compare it

                for (int i = 0; i < magic.length; i++) {
                    if (mask != null) {
                        header[i] &= mask[i];
                    }
                    if (magic[i] != header[i]) {
                        return false;
                    }
                }
            }
            
            // check for fileobject attributes

            if (fatts != null) {
                for (int i = fatts.length -1 ; i>=0; i--) {
                    Object attr = fo.getAttribute(fatts[i]);
                    if (attr != null) {
                        if (!attr.toString().equals(vals[i]) && vals[i] != null) return false;
                    } else {
                        return false;
                    }
                }
            }

            // check for patterns in file
            if (patterns != null) {
                try {
                    boolean matched = false;
                    for (FilePattern pattern : patterns) {
                        if(pattern.match(fo)) {
                            // at least one pattern matched => escape loop, otherwise continue
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        return false;
                    }
                } catch (IOException ioe) {
                    handleIOException(fo, ioe);
                    return false;
                }
            }

            // check file name
            if (names != null) {
                boolean matched = false;
                for (FileName name : names) {
                    if(name.match(fo)) {
                        // at least one matched => escape loop, otherwise continue
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
            }

            // all templates matched
            return true;
        }
    }
    
}
