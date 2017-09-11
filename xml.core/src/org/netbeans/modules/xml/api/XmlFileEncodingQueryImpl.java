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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.api;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * This implementation of the FileEncodingQueryImplementation can be used
 * by any XML file: WSDL, Schema, BPEL, ...
 *
 * @author nk160297
 */
public final class XmlFileEncodingQueryImpl extends FileEncodingQueryImplementation {
    
    //Logger based unit testing
    static final String DECODER_SELECTED = "decoder-selected";      //NOI18N
    //Logger based unit testing
    static final String ENCODER_SELECTED = "encoder-selected";      //NOI18N
    
    private static final Logger LOG = Logger.getLogger(XmlFileEncodingQueryImpl.class.getName());

    private static final XmlFileEncodingQueryImpl singleton = new XmlFileEncodingQueryImpl();
    
    private XmlFileEncodingQueryImpl () {}
    
    public static XmlFileEncodingQueryImpl singleton() {
        return singleton;
    }
    
    public synchronized Charset getEncoding(FileObject file) {
        assert file != null;
        return new XMLCharset();
    }
    
    private static class XMLCharset extends Charset {
        
            public XMLCharset () {
                super ("UTF-8", new String[0]);         //NOI18N
            }
        
            public boolean contains(Charset c) {
                return false;
            }

            public CharsetDecoder newDecoder() {
                return new XMLDecoder (this);
            }

            public CharsetEncoder newEncoder() {
                return new XMLEncoder (this);
            }
    }
    
    private static class XMLEncoder extends CharsetEncoder {
            
        private CharBuffer buffer = CharBuffer.allocate(4*1024);
        private CharBuffer remainder;
        private CharsetEncoder encoder;

        public XMLEncoder (Charset cs) {
            super (cs, 1,2);
        }


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
                } catch (UnsupportedCharsetException e) {
                    buffer = null;
                    throwUnknownEncoding();
                    return null;
                } catch (IllegalCharsetNameException e) {
                    buffer = null;
                    throwUnknownEncoding();
                    return null;
                }
                encoder = c.newEncoder();
                LOG.log (Level.FINEST,ENCODER_SELECTED,encoder);
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
                return EncodingUtil.detectEncoding(in);
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

    private static class XMLDecoder extends CharsetDecoder {

        private ByteBuffer buffer = ByteBuffer.allocate(4*1024);
        private ByteBuffer remainder;
        private CharsetDecoder decoder;

        public XMLDecoder (Charset cs) {
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
                LOG.log (Level.FINEST,DECODER_SELECTED,decoder);
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
                if (buffer.remaining() > 0) {
                    remainder = buffer;
                    buffer = null;
                    return r;
                }
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
                return EncodingUtil.detectEncoding(in);
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
