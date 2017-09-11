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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.diff;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.openide.util.io.ReaderInputStream;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.queries.FileEncodingQuery;

/**
 * This class provides streams and information about them to be used by diff
 * and merge services.
 *
 * @author  Martin Entlicher
 */
public abstract class StreamSource extends Object {
    
    /**
     * Get the name of the source.
     */
    public abstract String getName();
    
    /**
     * Get the title of the source.
     */
    public abstract String getTitle();
    
    /**
     * Get the MIME type of the source.
     */
    public abstract String getMIMEType();
    
    /**
     * Hint for a diff visualizer about editability of this source. The source will only be made editable if it provides
     * some editable entity in its lookup (eg. FileObject) and this method returns true and the diff visualizer supports it.
     * 
     * @return true if this source can be editable in the diff visualizer, false otherwise
     * @since 1.17
     */ 
    public boolean isEditable() {
        return false;
    }

    /**
     * Source lookup that may define the content of this source. In case the lookup does not provide anything
     * usable, createReader() is used instead. Diff engines can process these inputs: 
     * <ul>
     * <li> instance of {@link org.openide.filesystems.FileObject} - in this case, the content of the source is defined 
     * by calling DataObject.find(fileObject).openDocument(). If the source is editable then it is
     * saved back via SaveCookie.save() when the Diff component closes.
     * <li> instance of {@link javax.swing.text.Document} - in this case, the content of the source is defined 
     * by this Document and the source will NOT be editable.
     * </ul>
     * 
     * For compatibility purposes, it is still adviced to fully implement createReader() as older Diff providers may
     * not use this method of obtaining the source.
     * 
     * @return an instance of Lookup
     * @since 1.17
     */ 
    public Lookup getLookup() {
        return Lookups.fixed();
    }
    
    /**
     * Create a reader, that reads the source.
     */
    public abstract Reader createReader() throws IOException ;
    
    /**
     * Create a writer, that writes to the source.
     * @param conflicts The list of conflicts remaining in the source.
     *                  Can be <code>null</code> if there are no conflicts.
     * @return The writer or <code>null</code>, when no writer can be created.
     */
    public abstract Writer createWriter(Difference[] conflicts) throws IOException ;
    
    /**
     * Close the stream source. This method, is called when this object
     * will never be asked for the streams any more and thus can
     * release it's resources in this method.
     */
    public void close() {
    }
    
    /**
     * Create the default implementation of <code>StreamSource</code>, that has
     * just reader and no writer.
     */
    public static StreamSource createSource(String name, String title, String MIMEType, Reader r) {
        return new Impl(name, title, MIMEType, r);
    }
    
    /**
     * Create the default implementation of <code>StreamSource</code>, that has
     * just reader and writer from/to a file.
     */
    public static StreamSource createSource(String name, String title, String MIMEType, File file) {
        return new Impl(name, title, MIMEType, file);
    }
    
    /**
     * Private implementation to be returned by the static methods.
     */
    private static class Impl extends StreamSource {
        
        private String name;
        private String title;
        private String MIMEType;
        private Reader r;
        private File readerSource;
        private Writer w;
        private File file;
        private Charset encoding;
        
        Impl(String name, String title, String MIMEType, Reader r) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
            this.r = r;
            this.readerSource = null;
            this.w = null;
            this.file = null;
            if (r instanceof InputStreamReader) {
                try {
                    encoding = Charset.forName(((InputStreamReader) r).getEncoding());
                } catch (UnsupportedCharsetException e) {
                    // ignore, encoding will be null
                }
            }
        }
        
        Impl(String name, String title, String MIMEType, File file) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
            this.readerSource = null;
            this.w = null;
            this.file = file;
            encoding = FileEncodingQuery.getEncoding(FileUtil.toFileObject(file));
        }
        
        private File createReaderSource(Reader r) throws IOException {
            File tmp = null;
            tmp = FileUtil.normalizeFile(File.createTempFile("sss", "tmp"));
            tmp.deleteOnExit();
            tmp.createNewFile();
            InputStream in = null;
            OutputStream out = null;
            try {
                if (encoding == null) {
                    in = new ReaderInputStream(r);
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    copyStreamsCloseAll(new OutputStreamWriter(baos, encoding), r);
                    in = new ByteArrayInputStream(baos.toByteArray());
                }
                org.openide.filesystems.FileUtil.copy(in, out = new FileOutputStream(tmp));
            } finally {
                if (in != null) in.close();
                if (out != null) out.close();
            }
            return tmp;
        }
        
        public String getName() {
            return name;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getMIMEType() {
            return MIMEType;
        }
        
        public Reader createReader() throws IOException {
            if (file != null) {
                return new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            } else {
                synchronized (this) {
                    if (r != null) {
                        readerSource = createReaderSource(r);
                        r = null;
                    }
                }
                if (encoding == null) {
                    return new BufferedReader(new FileReader(readerSource));
                } else {
                    return new BufferedReader(new InputStreamReader(new FileInputStream(readerSource), encoding));
                }
            }
        }
        
        public Writer createWriter(Difference[] conflicts) throws IOException {
            if (conflicts != null && conflicts.length > 0) return null;
            if (file != null) {
                if (encoding == null) {
                    return new BufferedWriter(new FileWriter(file));
                } else {
                    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
                }
            } else return w;
        }
        
    }
    
    private static void copyStreamsCloseAll(Writer writer, Reader reader) throws IOException {
        char [] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }
}
