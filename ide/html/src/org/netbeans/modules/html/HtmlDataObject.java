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

package org.netbeans.modules.html;

import org.netbeans.modules.html.api.HtmlDataNode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.UserCancelException;
import org.openide.windows.TopComponent;
import org.xml.sax.InputSource;

/** Object that represents one html file.
 *
 * @author Ian Formanek
 * @author Marek Fukala
 */
@MIMEResolver.Registration(
    displayName="#HTMLResolver",
    position=300,
    resource="resolver.xml",
    showInFileChooser="#ResourceFiles"
)
public class HtmlDataObject extends MultiDataObject implements CookieSet.Factory {
    public static final String PROP_ENCODING = "Content-Encoding"; // NOI18N
    public static final String DEFAULT_ENCODING = new InputStreamReader(System.in).getEncoding();
    private static final Logger LOG = Logger.getLogger(HtmlDataObject.class.getName());
    static final long serialVersionUID =8354927561693097159L;
    
    //constants used when finding html document content type
    private static final String CHARSET_DECL = "CHARSET="; //NOI18N
    private static final String CHARSET_ATTRIBUTE = "charset"; //NOI18N
    private static final String META_TAG = "meta"; //NOI18N
    
    private HtmlEditorSupport htmlEditorSupport;
    
    private static final Pattern XML_PI_ENCODING_PATTERN = Pattern.compile(".*encoding=\"(.*)\".*");
    
    @MultiViewElement.Registration(
            displayName="#LBL_HTMLEditorTab",
            iconBase="org/netbeans/modules/html/htmlObject.png",
            persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID="html.source",
            mimeType=HtmlLoader.HTML_MIMETYPE,
            position=1
        )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    
    /** New instance.
     * @param pf primary file object for this data object
     * @param loader the data loader creating it
     * @exception DataObjectExistsException if there was already a data object for it
     */
    public HtmlDataObject(FileObject pf, UniFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        CookieSet set = getCookieSet();
        set.add(HtmlEditorSupport.class, this);
        set.add(ViewSupport.class, this);
        set.assign(SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs( FileObject folder, String fileName ) throws IOException {
                HtmlEditorSupport es = getCookie( HtmlEditorSupport.class );
                try {
                    es.updateEncoding();
                    es.saveAs( folder, fileName );
                } catch (UserCancelException e) {
                    //ignore, just not save anything
                }
            }
        });

        set.assign(FileEncodingQueryImplementation.class, new FileEncodingQueryImpl());
                
        //add check/validate xml cookies
        InputSource in = DataObjectAdapters.inputSource(this);
        set.add(new ValidateXMLSupport(in));
        set.add(new CheckXMLSupport(in));
        
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
    
    @Override
    protected org.openide.nodes.Node createNodeDelegate() {
        DataNode n = new HtmlDataNode(this, Children.LEAF);
        n.setIconBaseWithExtension("org/netbeans/modules/html/htmlObject.png"); // NOI18N
        return n;
    }

    @Override
    public void setModified(boolean modif) {
        super.setModified(modif);
        if(!isModified()) {
            HtmlEditorSupport support = getLookup().lookup(HtmlEditorSupport.class);
            support.removeSaveCookie();
        }
    }
    
    /** Creates new Cookie */
    public Node.Cookie createCookie(Class klass) {
        if (klass.isAssignableFrom(HtmlEditorSupport.class)) {
            return getHtmlEditorSupport();
        } else if (klass.isAssignableFrom(ViewSupport.class)) {
            return new ViewSupport(getPrimaryEntry());
        } else {
            return null;
        }
    }
    
    private synchronized HtmlEditorSupport getHtmlEditorSupport() {
        if (htmlEditorSupport == null) {
            htmlEditorSupport = new HtmlEditorSupport(this); 
        }
        return htmlEditorSupport;
    }
    
    // Package accessibility for HtmlEditorSupport:
    CookieSet getCookieSet0() {
        return getCookieSet();
    }
    
    /** Checks the file for UTF-16 marks and calls findEncoding with properly loaded document content then. */
    String getFileEncoding() {
        InputStream is = null;
        try {
            FileObject pf = getPrimaryFile();
            if(!pf.isValid()) {
                return null;
            }
            is = pf.getInputStream();
            return getFileEncoding(is);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
        return null;
    }

    private String getFileEncoding(final InputStream in) throws IOException {
        //detect encoding from input stream
        String encoding = null;
            byte[] arr = new byte[4096];
        int len = in.read(arr);
            len = (len >= 0) ? len : 0;
            //check UTF-16 mark
            if (len > 1) {
                int mark = (arr[0]&0xff)*0x100+(arr[1]&0xff);
                if (mark == 0xfeff) {
                    encoding = "UTF-16"; // NOI18N
                } else if(mark == 0xfffe) {
                    encoding = "UTF-16LE";
                }
            }
            //try to read the file using some encodings
            String[] encodings = new String[]{encoding != null ? encoding : DEFAULT_ENCODING, "UTF-16LE", "UTF-16BE"};
            int i = 0;
            do {
                encoding = findEncoding(makeString(arr, 0, len, encodings[i++]));
            } while (encoding == null && i < encodings.length);
            
        if (encoding != null) {
            encoding = encoding.trim();
        }
        return encoding;
    }
    
    private String makeString(byte[] arr, int offset, int len, String encoding) throws UnsupportedEncodingException {
        return new String(arr, 0, len, encoding).toUpperCase();
    }
    
    /** Tries to guess the mime type from given input stream. Tries to find
     *   <em>&lt;meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"&gt;</em>
     * @param txt the string to search in (should be in upper case)
     * @return the encoding or null if no has been found
     */
    static String findEncoding(String txt) {
        TokenHierarchy hi = TokenHierarchy.create(txt, HTMLTokenId.language());
        TokenSequence<HTMLTokenId> ts = hi.tokenSequence(HTMLTokenId.language());
        ts.moveStart();
        boolean in_charset_attribute = false;
        boolean in_meta_tag = false;
        while (ts.moveNext()) {
            Token<HTMLTokenId> token = ts.token();
            switch (token.id()) {
                case XML_PI:
                    Matcher matcher = XML_PI_ENCODING_PATTERN.matcher(token.text());
                    if(matcher.matches()) {
                        return matcher.group(1);
                    }
                case TAG_OPEN:
                    in_meta_tag = LexerUtils.equals(META_TAG, token.text(), true, true);
                    break;
                case TAG_CLOSE_SYMBOL:
                    //always go out from meta tag if a close tag or close tag symbol '/>' is found 
                    //as the source may be broken
                    in_meta_tag = false;
                    break;
                case ARGUMENT:
                    if (in_meta_tag) {
                        in_charset_attribute = LexerUtils.equals(CHARSET_ATTRIBUTE, token.text(), true, true);
                    }
                    break;
                case VALUE:
                    if (in_meta_tag) {
                        if (in_charset_attribute) {
                            //<meta charset="UTF-8">
                            return WebUtils.unquotedValue(token.text()).toString();
                        } else {
                            //<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                            int charsetOffset = CharSequences.indexOf(token.text(), CHARSET_DECL); //find in uppercase
                            if (charsetOffset == -1) {
                                charsetOffset = CharSequences.indexOf(token.text(), CHARSET_DECL.toLowerCase(Locale.ENGLISH)); //find in lowercase
                            }
                            if (charsetOffset != -1) {
                                int charsetEndOffset = charsetOffset + CHARSET_DECL.length();
                                int endOffset = CharSequences.indexOf(token.text(), ";", charsetEndOffset);
                                if (endOffset == -1) {
                                    endOffset = CharSequences.indexOf(token.text(), "\"", charsetEndOffset);
                                }
                                if (endOffset == -1) {
                                    endOffset = CharSequences.indexOf(token.text(), "'", charsetEndOffset);
                                }
                                if (endOffset != -1 && charsetEndOffset < endOffset) {
                                    return token.text().subSequence(charsetEndOffset, endOffset).toString();
                                }
                            }
                        }
                    }
                    break;
            }
        }
        return null;
    }
    
    static final class ViewSupport implements ViewCookie {
        /** entry */
        private MultiDataObject.Entry primary;
        
        /** Constructs new ViewSupport */
        public ViewSupport(MultiDataObject.Entry primary) 
        {
            this.primary = primary;
        }
        
        @Override
        public void view() {
            HtmlBrowser.URLDisplayer.getDefault().showURL(primary.getFile().toURL());
        }
    }
    
    private class FileEncodingQueryImpl extends FileEncodingQueryImplementation {

        private volatile Charset cachedEncoding;
        private final AtomicBoolean listeningOnContentChange = new AtomicBoolean();
        private final ThreadLocal<Boolean> callingFEQ = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return false;
            }
        };

        @Override
        public Charset getEncoding(FileObject file) {
            assert file != null;
            if(callingFEQ.get()) {
                //we are calling to the FEQ from within this method so
                //we must not return anything to prevent cycling
                return null;
            }

            Charset encoding = cachedEncoding;
            if (encoding != null) {
                LOG.log(Level.FINEST, "HtmlDataObject.getFileEncoding cached {0}", new Object[] {encoding});   //NOI18N
                return encoding;
            } else {
                //get the encoding from the FEQ excluding this FEQ implementation
                //so the proxy charset can default to appropriate encoding
                callingFEQ.set(true);
                try {
                    Charset charset = FileEncodingQuery.getEncoding(file);
                    return new ProxyCharset(charset);
                } finally {
                    callingFEQ.set(false);
                }
            }
        }

        private Charset cache (final Charset encoding) {

            if (!listeningOnContentChange.getAndSet(true)) {
                final FileObject primaryFile = getPrimaryFile();
                primaryFile.addFileChangeListener(FileUtil.weakFileChangeListener(new FileChangeAdapter(){
                    @Override
                    public void fileChanged(FileEvent fe) {
                        cachedEncoding = null;
                    }
                },primaryFile));
            }
            cachedEncoding = encoding;
            LOG.log(Level.FINEST, "HtmlDataObject.getFileEncoding noncached {0}", new Object[] {encoding});   //NOI18N
            return encoding;
        }


        private class ProxyCharset extends Charset {

            public ProxyCharset (Charset charset) {
                super (charset.name(), new String[0]);         //NOI18N
            }
            
            public boolean contains(Charset c) {
                return false;
            }

            public CharsetDecoder newDecoder() {
                return new HtmlDecoder (this);
            }

            public CharsetEncoder newEncoder() {
                return new HtmlEncoder (this);
            }
        }

        private class HtmlEncoder extends CharsetEncoder {

            private CharBuffer buffer = CharBuffer.allocate(4*1024);
            private CharBuffer remainder;
            private CharsetEncoder encoder;

            public HtmlEncoder (Charset cs) {
                super(cs, 1,2);
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
                        c = cache(Charset.forName(encoding));
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
                    return getFileEncoding(in);
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

        private class HtmlDecoder extends CharsetDecoder {

            private ByteBuffer buffer = ByteBuffer.allocate(4*1024);
            private ByteBuffer remainder;
            private CharsetDecoder decoder;

            public HtmlDecoder (Charset cs) {
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
                        c = cache(Charset.forName(encoding));
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
                    return getFileEncoding(in);
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

}
