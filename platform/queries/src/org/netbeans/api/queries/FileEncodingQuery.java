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
package org.netbeans.api.queries;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.queries.UnknownEncoding;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * The query is used for finding encoding of files.
 * The query should be used when reading or writing files to use the
 * correct encoding.
 * @since org.netbeans.modules.queries/1 1.9
 * @see FileEncodingQueryImplementation
 * @author Tomas Zezula
 */
public class FileEncodingQuery {

    private static final int BUFSIZ = 4*1024;
    private static final String DEFAULT_ENCODING = "default-encoding";    //NOI18N
    private static final String UTF_8 = "UTF-8";                          //NOI18N
    private static final Logger LOG = Logger.getLogger(FileEncodingQuery.class.getName());

    private FileEncodingQuery() {}

    /**
     * Returns encoding of given file.
     * A folder also may be passed to this method in an attempt to get the encoding
     * for all files in, for example, a Java source root.
     * @param file to find an encoding for
     * @return encoding which should be used for given file, never returns null.
     * @throws IllegalArgumentException if file parameter is null.
     */
    public static Charset getEncoding (FileObject file) {
        if (file == null) {
            throw new IllegalArgumentException();
        }
        List<Charset> delegates = new ArrayList<Charset>();
        for (FileEncodingQueryImplementation impl : Lookup.getDefault().lookupAll(FileEncodingQueryImplementation.class)) {
            Charset encoding = impl.getEncoding(file);
            if (encoding != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0}: received encoding {1} from {2}", new Object[] {file, encoding, impl});
                }
                delegates.add(encoding);
            } else {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "{0}: received no encoding from {1}", new Object[] {file, impl});
                }
            }
        }
        try {
            if (file.getFileSystem().isDefault()) {
                delegates.add(Charset.forName(UTF_8));
            } else {
                delegates.add(Charset.defaultCharset());
            }
        } catch (FileStateInvalidException ex) {
            delegates.add(Charset.defaultCharset());
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "{0}: using encodings {1}", new Object[] {file, delegates});
        }
        return new ProxyCharset (delegates);
    }

    /**
     * Returns the encoding which should be used for newly created projects.
     * The typical user of this method is a code generating new projects.
     * The returned value is a last used encoding set for project.
     * @return the default encoding
     *
     */
    public static Charset getDefaultEncoding () {
        Preferences prefs = NbPreferences.forModule(FileEncodingQuery.class);
        String defaultEncoding = prefs.get (DEFAULT_ENCODING,UTF_8);
        return Charset.forName(defaultEncoding);
    }

    /**
     * Sets the encoding which should be used for newly created projects.
     * The typical user of this method is a project customizer, when the
     * user sets a new encoding the customizer code should update the defaul
     * encoding by this method.
     * @param encoding the new default encoding
     * @throws IllegalArgumentException if encoding parameter is null.
     *
     */
    public static void setDefaultEncoding (final Charset encoding) {
        if (encoding == null) {
            throw new IllegalArgumentException();
        }
        Preferences prefs = NbPreferences.forModule(FileEncodingQuery.class);
        prefs.put(DEFAULT_ENCODING, encoding.name());
    }

    private static class ProxyCharset extends Charset {

        private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0);
        private static final CharBuffer EMPTY_CHAR_BUFFER = CharBuffer.allocate(0);

        private final List<? extends Charset> delegates;

        private ProxyCharset (final List<? extends Charset> delegates) {
            super (delegates.get(0).name(), delegates.get(0).aliases().toArray(new String[delegates.get(0).aliases().size()]));
            this.delegates = delegates;
        }

        @Override
        public boolean contains(Charset charset) {
            return this.delegates.get(0).contains(charset);
        }

        @Override
        public CharsetDecoder newDecoder() {
            return new ProxyDecoder (delegates.get(0).newDecoder());
        }

        @Override
        public CharsetEncoder newEncoder() {
            return new ProxyEncoder (delegates.get(0).newEncoder());
        }

        private class ProxyDecoder extends CharsetDecoder {

            private CharsetDecoder currentDecoder;
            private ByteBuffer buffer = ByteBuffer.allocate(BUFSIZ);
            private ByteBuffer remainder;
            private CodingErrorAction malformedInputAction;
            private CodingErrorAction unmappableCharAction;
            private String replace;
            private boolean initialized;
            private CharBuffer lastCharBuffer;

            private ProxyDecoder (final CharsetDecoder defaultDecoder) {
                super (ProxyCharset.this, defaultDecoder.averageCharsPerByte(), defaultDecoder.maxCharsPerByte());
                this.currentDecoder = defaultDecoder;
                initialized = true;
            }

            @Override
            protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
                lastCharBuffer = out;
                if (buffer == null) {
                    if (remainder!=null) {
                        ByteBuffer tmp = ByteBuffer.allocate(remainder.remaining() + in.remaining());
                        tmp.put(remainder);
                        tmp.put(in);
                        tmp.flip();
                        CoderResult result = currentDecoder.decode(tmp,out,false);
                        if (tmp.hasRemaining()) {
                            remainder = tmp;
                        }
                        else {
                            remainder = null;
                        }
                        return result;
                    }
                    else {
                        return currentDecoder.decode(in, out, false);
                    }
                }
                if (buffer.remaining() == 0) {
                    return decodeHead(in, out, false);
                } else if (buffer.remaining() < in.remaining()) {
                    int limit = in.limit();
                    in.limit(in.position()+buffer.remaining());
                    buffer.put(in);
                    in.limit(limit);
                    return decodeHead(in, out, false);
                } else {
                    buffer.put(in);
                    return CoderResult.UNDERFLOW;
                }
            }

            private CoderResult decodeHead (final ByteBuffer in, final CharBuffer out, final boolean flush) {
                buffer.flip();
                CoderResult result = null;
                for (int i=0; i<delegates.size(); i++) {
                    currentDecoder=delegates.get(i).newDecoder();
                    if (malformedInputAction != null) {
                        currentDecoder.onMalformedInput(malformedInputAction);
                    }
                    if (unmappableCharAction != null) {
                        currentDecoder.onUnmappableCharacter(unmappableCharAction);
                    }
                    if (replace != null) {
                        currentDecoder.replaceWith(replace);
                    }
                    int outPos = out.position();
                    try {
                        ByteBuffer view = buffer.asReadOnlyBuffer();
                        result = currentDecoder.decode(view, out, in==null);
                        if (view.hasRemaining()) {
                            //Should never happen for files stored by NB, but may be some
                            //broken file ending with a non complete mbyte char.
                            if (flush) {
                                currentDecoder.flush(out);
                            }
                            LOG.log (Level.FINEST,DECODER_SELECTED,currentDecoder);
                            remainder = view;
                            buffer = null;
                            return result;
                        }
                        else {
                            if (in != null) {
                                result = currentDecoder.decode(in, out, false);
                            }
                            if (flush) {
                                result = currentDecoder.flush(out);
                            }
                            LOG.log (Level.FINEST,DECODER_SELECTED,currentDecoder);
                            buffer = null;
                            return result;
                        }
                    } catch (UnknownEncoding e) {
                        //continue when there was no already an output
                        if (outPos != out.position()) {
                            buffer = null;
                            return result;
                        }
                    }
                }
                buffer = null;
                assert result != null;
                return result;
            }

            @Override
            protected CoderResult implFlush(CharBuffer out) {
                lastCharBuffer = null;
                if (buffer != null) {
                    return decodeHead(null, out, true);
                }
                else {
                    currentDecoder.decode(EMPTY_BYTE_BUFFER, out, true);
                    return currentDecoder.flush(out);
                }
            }

            @Override
            protected void implReset() {
                if (lastCharBuffer!=null) {
                    implFlush(lastCharBuffer);
                }
                //Do rather flush, the sun.nio.cs.StreamDecoder doesn't do it
                currentDecoder.reset();
            }

            @Override
            protected void implOnMalformedInput(CodingErrorAction action) {
                if (buffer != null || !initialized) {
                    this.malformedInputAction = action;
                }
                else {
                    currentDecoder.onMalformedInput(action);
                }
            }

            @Override
            protected void implOnUnmappableCharacter(CodingErrorAction action) {
                if (buffer != null || !initialized) {
                    this.unmappableCharAction = action;
                }
                else {
                    currentDecoder.onUnmappableCharacter(action);
                }
            }

            @Override
            protected void implReplaceWith(String replace) {
                if (buffer != null || !initialized) {
                    this.replace = replace;
                }
                else {
                    currentDecoder.replaceWith(replace);
                }
            }

        }

        private class ProxyEncoder extends CharsetEncoder {

            private CharsetEncoder currentEncoder;
            private CharBuffer buffer = CharBuffer.allocate(BUFSIZ);
            private CharBuffer remainder;
            private CodingErrorAction malformedInputAction;
            private CodingErrorAction unmappableCharAction;
            private byte[] replace;
            private boolean initialized;
            private ByteBuffer lastByteBuffer;

            private ProxyEncoder (final CharsetEncoder defaultEncoder) {
                super (ProxyCharset.this, defaultEncoder.averageBytesPerChar(), defaultEncoder.maxBytesPerChar(), defaultEncoder.replacement());
                this.currentEncoder = defaultEncoder;
                this.initialized = true;
            }

            @Override
            protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
                lastByteBuffer = out;
                if (buffer == null) {
                    if (remainder!=null) {
                        CoderResult result = currentEncoder.encode(remainder,out,false);
                        if (!remainder.hasRemaining()) {
                            remainder = null;
                        }
                        if (!result.isUnderflow()) {
                            // perhaps did not fit into 'out'
                            return result;
                        }
                    }
                    CoderResult result = currentEncoder.encode(in, out, false);
                    return result;
                }
                if (buffer.remaining() == 0 || (buffer.position()>0 && in.limit() == 0)) {  //Either buffer is full or no more chars will come
                    CoderResult result = encodeHead(in, out, false);
                    return result;
                } else if (buffer.remaining() < in.remaining()) {
                    int limit = in.limit();
                    in.limit(in.position()+buffer.remaining());
                    buffer.put(in);
                    in.limit(limit);
                    CoderResult result = encodeHead(in, out, false);
                    return result;
                } else {
                    buffer.put(in);
                    return CoderResult.UNDERFLOW;
                }
            }

            private CoderResult encodeHead (CharBuffer in, ByteBuffer out, boolean flush) {
                // if buffer is missing, get data from the remainder, case for repeated implFlush() call.
                CharBuffer b = buffer == null ? remainder : buffer;
                b.flip();
                CoderResult result = null;
                for (int i=0; i<delegates.size(); i++) {
                    currentEncoder=delegates.get(i).newEncoder();
                    if (malformedInputAction != null) {
                        currentEncoder.onMalformedInput(malformedInputAction);
                    }
                    if (unmappableCharAction != null) {
                        currentEncoder.onUnmappableCharacter(unmappableCharAction);
                    }
                    if (replace != null) {
                        currentEncoder.replaceWith(replace);
                    }
                    int outPos = out.position();
                    try {
                        CharBuffer view = b.asReadOnlyBuffer();
                        result = currentEncoder.encode(view, out, in==null);
                        if (result.isOverflow()) {
                            // the output is smaller, interrupt encoding the head.
                            LOG.log(Level.FINEST, ENCODER_SELECTED, currentEncoder);
                            remainder = view;
                            buffer = null;
                            return result;
                        } else if (result.isUnmappable()) {
                            // encoder is not able to encode the head, throw an exception
                            return result;
                        }
                        else {
                            if (in != null) {
                                result = currentEncoder.encode(in, out, false);
                            }
                            if (result.isUnderflow()) {
                                if (flush) {
                                    result = currentEncoder.flush(out);
                                }
                            }
                            LOG.log(Level.FINEST, ENCODER_SELECTED, currentEncoder);
                            buffer = null;
                            return result;
                        }
                    } catch (UnknownEncoding e) {
                        //continue when there was no already an output
                        if (outPos != out.position()) {
                            buffer = null;
                            return result;
                        }
                    }
                }
                buffer = null;
                assert result != null;
                return result;
            }

            @Override
            protected CoderResult implFlush(ByteBuffer out) {
                lastByteBuffer = null;
                // if the previous encodeHead overflew, the caller should probably call encodeLoop, but is also
                // permitted to call flush -> implFlush again.
                if (buffer != null || remainder != null) {
                    return encodeHead(null, out, true);
                }
                else {
                    currentEncoder.encode(EMPTY_CHAR_BUFFER, out, true);
                    return currentEncoder.flush(out);
                }
            }

            @Override
            protected void implReset() {
                if (lastByteBuffer!=null) {
                    implFlush(lastByteBuffer);
                }
                //Do rather flush, the sun.nio.cs.StreamDecoder doesn't do it
                currentEncoder.reset();
            }

            @Override
            protected void implOnMalformedInput(CodingErrorAction action) {
                if (buffer != null || !initialized) {
                    malformedInputAction = action;
                }
                else {
                    currentEncoder.onMalformedInput(action);
                }
            }

            @Override
            protected void implOnUnmappableCharacter(CodingErrorAction action) {
                if (buffer != null || !initialized) {
                    unmappableCharAction = action;
                }
                else {
                    currentEncoder.onUnmappableCharacter(action);
                }
            }

            @Override
            protected void implReplaceWith(byte[] replace) {
                if (buffer != null || !initialized) {
                    this.replace = replace;
                }
                else {
                    currentEncoder.replaceWith(replace);
                }
            }
        }
    }

    //Unit tests support
    static final String ENCODER_SELECTED = "encoder-selected";  //NOI18N
    static final String DECODER_SELECTED = "decoder-selected";  //NOI18N
}
