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
package org.netbeans.modules.python.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public class PythonFileEncodingQuery extends FileEncodingQueryImplementation {
    
    private final ThreadLocal<Boolean> callingFEQ = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    
    private static final Logger LOGGER = Logger.getLogger(PythonFileEncodingQuery.class.getName());
    private static final Pattern pattern = Pattern.compile("coding[:=]\\s*([-\\w.]+)"); // NOI18N
    private static final Map<String, String> encodingsAliases = new HashMap<>();
    
    private static final String ENCODING_ISO8859_1 = "ISO-8859-1"; //NOI18N
    private static final String ENCODING_UTF_8 = "utf-8"; //NOI18N

    static {
        // Convert encoding names only supported by Python to ones that are
        // supported by Python/Jython/Java. 
        // I've only added the ones I believed are the most popular ones, so a 
        // lot more must be added.
        // See https://docs.python.org/2/library/codecs.html
        encodingsAliases.put("utf_8", ENCODING_UTF_8); // NOI18N
        encodingsAliases.put("latin_1", ENCODING_ISO8859_1); // NOI18N
        encodingsAliases.put("latin-1", ENCODING_ISO8859_1); // NOI18N
        encodingsAliases.put("latin", ENCODING_ISO8859_1); // NOI18N
        encodingsAliases.put("latin1", ENCODING_ISO8859_1); // NOI18N
        encodingsAliases.put("8859", ENCODING_ISO8859_1); // NOI18N
    } 
    
    // Can be called directly by code that just wants to know the encoding
    // as specified in the file header. FileEncodingQueryImplementation's
    // getEncoding() method forces the use of CharsetDecoder/CharsetEncoder.
    public String getPythonFileEncoding(final InputStream in) throws IOException {
        String encoding = null;
        try (Scanner sc = new Scanner(in)) {
            for (int i=0; i <= 1; i++) {
                if (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    encoding = extractEncoding(line);
                    if (encoding != null) {
                        break;
                    }
                }
            }
        }
        return recognizedEncoding(encoding);
    }
    
    
    // TODO: This is currently used by the Parser, but should ideally be removed.
    public String getPythonFileEncoding(String[] lines) {
        String encoding = null;
        for (int i=0; i <= (lines.length > 1 ? 1 : 0); i++) {
            String line = lines[i];
            encoding = extractEncoding(line);
            if (encoding != null) {
                break;
            }
        }
        return recognizedEncoding(encoding);          
    }

    
    private String extractEncoding(String line) {
        if (line == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String res = matcher.group(1);
            return res;
        }
        return null;
    }    
    
    
    private String recognizedEncoding(String inputEncoding) {
        // Returns the encoding if it's recognized by Charset.forName
        // method. When not recognized, an alternative for the specified
        // is returned if one is defined. Returns null when not recognized 
        // at all.
        
        // PEP-0263 https://www.python.org/dev/peps/pep-0263/
        // suggests to silently ignore unrecognized encodings.
        String encoding = inputEncoding;
        if (encoding != null) {
            try {
                Charset temp = Charset.forName(encoding);
                LOGGER.log(Level.FINE, "Encoding recognized: {0}", encoding); //NOI18N
            } catch(Exception e) {
                // When using Python it makes sense to see if one of the 
                // recognized aliases is used (name of codecs that are not
                // recognized by Java/Jython, but are recognized by Python)
                // There's probably a better way to do this.
                encoding = encodingsAliases.get(encoding);
                if (encoding != null) {
                    LOGGER.log(Level.FINE, "Encoding alias: {0}", encoding); // NOI18N
                } else {
                    LOGGER.log(Level.FINE, "Encoding not recognized"); // NOI18N
                }
            }
        }        
        return encoding;
    }
            
    
    @Override
    public Charset getEncoding(FileObject file) {
        if(callingFEQ.get()) {
            //we are calling to the FEQ from within this method so
            //we must not return anything to prevent cycling
            return null;
        }
        //get the encoding from the FEQ excluding this FEQ implementation
        //so the proxy charset can default to appropriate encoding.
        //@todo Caching?
        callingFEQ.set(true);
        try {
            Charset charset = FileEncodingQuery.getEncoding(file);
            return new ProxyCharset(charset);
        } finally {
            callingFEQ.set(false);
        }
    }
    
    private class ProxyCharset extends Charset {

        public ProxyCharset (Charset charset) {
            super (charset.name(), new String[0]);         //NOI18N
        }

        public boolean contains(Charset c) {
            return false;
        }

        public CharsetDecoder newDecoder() {
            return new PythonDecoder(this);
        }

        public CharsetEncoder newEncoder() {
            return new PythonEncoder(this);
        }
    }

    
    private class PythonEncoder extends CharsetEncoder {
        private CharBuffer buffer = CharBuffer.allocate(4*1024);
        private CharBuffer remainder;
        private CharsetEncoder encoder;

        public PythonEncoder(Charset cs) {
            super(cs, 1,2);
        }


        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            if (buffer == null) {
                assert encoder != null;
                if (remainder!=null) {
                    CoderResult result = encoder.encode(remainder,out,false);
                    if (!remainder.hasRemaining()) {
                        remainder = null;
                    }
                }
                CoderResult result = encoder.encode(in, out, false);
                return result;
            }
           if (buffer.remaining() == 0 || (buffer.position() > 0 && in.limit() == 0)) {
               return handleHead (in,out);
           }
           else if (buffer.remaining() < in.remaining()) {
               int limit = in.limit();
               in.limit(in.position()+buffer.remaining());
               buffer.put(in);
               in.limit(limit);
               return handleHead (in, out);
           }
           else {
               buffer.put(in);
               return CoderResult.UNDERFLOW;
           }
        }

        private CoderResult handleHead (CharBuffer in, ByteBuffer out) {
            String encoding = null;
            try {
                encoding = getEncoding ();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            if (encoding == null) {
                buffer = null;
                throwUnknownEncoding();
                return null;
            }
            else {
                Charset c;
                try {
                    c = Charset.forName(encoding);
                } catch (UnsupportedCharsetException | IllegalCharsetNameException e) {
                    buffer = null;
                    throwUnknownEncoding();
                    return null;
                }
                encoder = c.newEncoder();
                return flushHead(in, out);
            }
        }

        private CoderResult flushHead (CharBuffer in , ByteBuffer out) {
            buffer.flip();
            CoderResult r = encoder.encode(buffer,out, in==null);
            if (r.isOverflow()) {
                remainder = buffer;
                buffer = null;
                return r;
            }
            else {
                buffer = null;
                if (in == null) {
                    return r;
                }
                return encoder.encode(in, out, false);
            }
        }

        private String getEncoding () throws IOException {
            String text = buffer.asReadOnlyBuffer().flip().toString();
            InputStream in = new ByteArrayInputStream(text.getBytes());
            try {
                return getPythonFileEncoding(in);
            } finally {
                in.close();
            }
        }

        @Override
        protected CoderResult implFlush(ByteBuffer out) {
            CoderResult res;
            if (buffer != null) {
                res = handleHead(null, out);
                return res;
            }
            else if (remainder != null) {
                encoder.encode(remainder, out, true);
            }
            else {
                CharBuffer empty = (CharBuffer) CharBuffer.allocate(0).flip();
                encoder.encode(empty, out, true);
            }
            res = encoder.flush(out);
            return res;
        }

        @Override
        protected void implReset() {
            if (encoder != null) {
                encoder.reset();
            }
        }
    }
    

    private class PythonDecoder extends CharsetDecoder {

        private ByteBuffer buffer = ByteBuffer.allocate(4*1024);
        private ByteBuffer remainder;
        private CharsetDecoder decoder;

        public PythonDecoder(Charset cs) {
            super (cs,1,2);
        }

        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            if (buffer == null) {
                assert decoder != null;
                if (remainder!=null) {
                    ByteBuffer tmp = ByteBuffer.allocate(remainder.remaining() + in.remaining());
                    tmp.put(remainder);
                    tmp.put(in);
                    tmp.flip();
                    CoderResult result = decoder.decode(tmp,out,false);
                    if (tmp.hasRemaining()) {
                        remainder = tmp;
                    }
                    else {
                        remainder = null;
                    }
                    return result;
                }
                else {
                    return decoder.decode(in, out, false);
                }
           }
           if (buffer.remaining() == 0) {
               return handleHead (in,out);
           }
           else if (buffer.remaining() < in.remaining()) {
               int limit = in.limit();
               in.limit(in.position()+buffer.remaining());
               buffer.put(in);
               in.limit(limit);
               return handleHead (in, out);
           }
           else {
               buffer.put(in);
               return CoderResult.UNDERFLOW;
           }
        }

        private CoderResult handleHead (ByteBuffer in, CharBuffer out) {
            String encoding = null;
            try {
                encoding = getEncoding ();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            if (encoding == null) {
                buffer = null;
                throwUnknownEncoding();
                return null;
            }
            else {
                Charset c;
                try {
                    //c = cache(Charset.forName(encoding));
                    c = Charset.forName(encoding);
                } catch (UnsupportedCharsetException e) {
                    buffer = null;
                    throwUnknownEncoding();
                    return null;
                } catch (IllegalCharsetNameException e) {
                    buffer = null;
                    throwUnknownEncoding();
                    return null;
                }
                decoder = c.newDecoder();
                return flushHead(in, out);
            }
        }

        private CoderResult flushHead (ByteBuffer in , CharBuffer out) {
            buffer.flip();
            CoderResult r = decoder.decode(buffer,out, in==null);
            if (r.isOverflow()) {
                remainder = buffer;
                buffer = null;
                return r;
            }
            else {
                buffer = null;
                if (in == null) {
                    return r;
                }
                return decoder.decode(in, out, false);
            }
        }

        private String getEncoding () throws IOException {
            byte[] arr = buffer.array();
            ByteArrayInputStream in = new ByteArrayInputStream (arr);
            try {
                return getPythonFileEncoding(in);
            }
            finally {
                in.close();
            }
        }

        @Override
        protected CoderResult implFlush(CharBuffer out) {
            CoderResult res;
            if (buffer != null) {
                res = handleHead(null, out);
                return res;
            }
            else if (remainder != null) {
                decoder.decode(remainder, out, true);
            }
            else {
                ByteBuffer empty = (ByteBuffer) ByteBuffer.allocate(0).flip();
                decoder.decode(empty, out, true);
            }
            res = decoder.flush(out);
            return res;
        }

        @Override
        protected void implReset() {
            if (decoder != null) {
                decoder.reset();
            }
        }
    }
}
