/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.Charset;
import org.netbeans.installer.utils.progress.Progress;

/**
 * @author Danila Dugurov
 * @author Kirill Sorokin
 */
public class StreamUtils {
    public static void transferData(
            final InputStream in, 
            final OutputStream out) throws IOException {
        final byte[] buffer = new byte[FileUtils.BUFFER_SIZE];
        int length = 0;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        out.flush();
    }
    
    public static void transferData(
            final RandomAccessFile in, 
            final OutputStream out) throws IOException {
        transferData(in, out, in.length());
    }
    
    public static void transferData(
            final RandomAccessFile in, 
            final OutputStream out,
            final Progress progress) throws IOException {
        transferData(in, out, in.length(), progress);
    }
    
    public static void transferData(
            final RandomAccessFile in, 
            final OutputStream out, 
            final long max) throws IOException {
        transferData(in, out, max, new Progress());
    }
    
    public static void transferData(
            final RandomAccessFile in, 
            final OutputStream out, 
            final long max,
            final Progress progress) throws IOException {
        final byte[] buffer = new byte[FileUtils.BUFFER_SIZE];
        
        long total = 0;
        int length = 0;
        
        progress.setPercentage(Progress.START);
        while (((length = in.read(buffer)) != -1) && (total < max)) {
            total += length;
            out.write(
                    buffer, 
                    0, 
                    (int) (total < max ? length : length - (total - max)));
            
            if (total < max) {
                progress.setPercentage(Progress.COMPLETE * total / max);
            }
        }
        progress.setPercentage(Progress.COMPLETE);
        
        out.flush();
    }
    
    public static void transferFile(
            final File file, 
            final OutputStream out) throws IOException {
        transferFile(file, out, new Progress());
    }
    
    public static void transferFile(
            final File file, 
            final OutputStream out,
            final Progress progress) throws IOException {
        RandomAccessFile in = null;
        
        try {
            transferData(in = new RandomAccessFile(file, "r"), out, progress);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    ErrorManager.notifyDebug("Cannot close raf", e);
                }
            }
        }
    }
    
    public static CharSequence readStream(
            final InputStream input) throws IOException {
        return readStream(input, Charset.forName(StringUtils.ENCODING_UTF8));
    }
    
    public static CharSequence readStream(
            final InputStream input, 
            final Charset charset) throws IOException {
        final Reader reader = new BufferedReader(new InputStreamReader(input, charset));
        return readReader(reader);
    }
    
    public static CharSequence readReader(
            final Reader reader) throws IOException {
        final char[] buffer = new char[FileUtils.BUFFER_SIZE];
        final StringBuilder stringBuilder = new StringBuilder();
        int readLength;
        while ((readLength = reader.read(buffer)) != -1) {
            stringBuilder.append(buffer, 0, readLength);
        }
        return stringBuilder;
    }
    
    public static CharSequence readFile(
            final File file) throws IOException {
        return readFile(file, Charset.forName(StringUtils.ENCODING_UTF8));
    }
    
    public static CharSequence readFile(
            final File file, 
            final Charset charset) throws IOException {
        final InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            return readReader(new InputStreamReader(in, charset));
        } finally {
            try {
                in.close();
            } catch(IOException ignord) {}
        }
    }
    
    public static void writeChars(
            final OutputStream out, 
            final CharSequence chars) throws IOException {
        writeChars(out, chars, Charset.forName(StringUtils.ENCODING_UTF8));
    }
    
    public static void writeChars(
            final OutputStream out, 
            final CharSequence chars, 
            final Charset charset) throws IOException {
        out.write(chars.toString().getBytes(charset.name()));
    }
    
    public static void writeChars(
            final File file, 
            final CharSequence chars) throws IOException {
        writeChars(file, chars, Charset.forName(StringUtils.ENCODING_UTF8));
    }
    
    public static void writeChars(
            final File file, 
            final CharSequence chars, 
            final Charset charset) throws IOException {
        final OutputStream out = 
                new BufferedOutputStream(new FileOutputStream(file));
        
        try {
            writeChars(out, chars, charset);
        } finally {
            try {
                out.close();
            } catch(IOException ignord) {}
        }
    }
}
