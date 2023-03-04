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
