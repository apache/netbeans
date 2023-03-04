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

package org.netbeans.api.java.source.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import org.netbeans.modules.java.source.JavadocHelper;

/**
 *  HTML Parser. It retrieves sections of the javadoc HTML file.
 *
 * @author  Martin Roskanin
 */
class HTMLJavadocParser {
    
    public static final Logger LOG = Logger.getLogger(HTMLJavadocParser.class.getName());
    
    /** Gets the javadoc text from the given URL
     *  @param url location of Javadoc
     *  @param pkg true if URL should be retrieved for a package
     */
    public static String getJavadocText(URL url, boolean pkg) {
        return getJavadocText(new JavadocHelper.TextStream(url), pkg);
    }

    /** Gets the javadoc text from the given URL
     *  @param page location of Javadoc
     *  @param pkg true if URL should be retrieved for a package
     */
    public static String getJavadocText(JavadocHelper.TextStream page, boolean pkg) {
        if (page == null) return null;

        HTMLEditorKit.Parser parser;
        InputStream is = null;
        
        String charset = null;
        for (;;) {
            try{
                is = page.openStream();
                parser = new ParserDelegator();
                String urlStr = URLDecoder.decode(page.getLocation().toString(), "UTF-8"); //NOI18N
                int offsets[] = null;
                Reader reader = charset == null?new InputStreamReader(is): new InputStreamReader(is, charset);
                
                if (pkg){
                    // package description
                    offsets = parsePackage(reader, parser, charset != null);
                }else if (urlStr.indexOf('#')>0){
                    // member javadoc info
                    final Collection<? extends URL> urls = page.getLocations();
                    final Collection<String> possibleNames = new HashSet<>(urls.size());
                    for (URL nameUrl : urls) {
                        urlStr = URLDecoder.decode(nameUrl.toString(), "UTF-8"); //NOI18N
                        final String memberName = urlStr.substring(urlStr.indexOf('#')+1);
                        if (!memberName.isEmpty()) {
                            possibleNames.add(memberName);
                        }
                    }
                    if (!possibleNames.isEmpty()) {
                        offsets = parseMember(reader, possibleNames, parser, charset != null);
                    }
                }else{
                    // class javadoc info
                    offsets = parseClass(reader, parser, charset != null);
                }
                
                if (offsets != null){
                    return getTextFromURLStream(page, offsets, charset);
                }
                break;
            } catch (ChangedCharSetException e) {
                if (charset == null) {
                    charset = getCharSet(e);
                    //restart with valid charset
                } else {
                    e.printStackTrace();
                    break;
                }
            } catch (FileNotFoundException x) {
                break; // e.g. missing com.sun.** class in network Javadoc; ignore
            } catch (InterruptedIOException x) {
                //Http javadoc timeout
                break;
            }catch(IOException ioe){
                ioe.printStackTrace();
                break;
            }finally{
                parser = null;
                if (is!=null) {
                    try{
                        is.close();
                    }catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
    
    private static String getCharSet(ChangedCharSetException e) {
        String spec = e.getCharSetSpec();
        if (e.keyEqualsCharSet()) {
            //charsetspec contains only charset
            return spec;
        }
        
        //charsetspec is in form "text/html; charset=UTF-8"
                
        int index = spec.indexOf(";"); // NOI18N
        if (index != -1) {
            spec = spec.substring(index + 1);
        }
        
        spec = spec.toLowerCase();
        
        StringTokenizer st = new StringTokenizer(spec, " \t=", true); //NOI18N
        boolean foundCharSet = false;
        boolean foundEquals = false;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(" ") || token.equals("\t")) { //NOI18N
                continue;
            }
            if (foundCharSet == false && foundEquals == false
                    && token.equals("charset")) { //NOI18N
                foundCharSet = true;
                continue;
            } else if (foundEquals == false && token.equals("=")) {//NOI18N
                foundEquals = true;
                continue;
            } else if (foundEquals == true && foundCharSet == true) {
                return token;
            }
            
            foundCharSet = false;
            foundEquals = false;
        }
        
        return null;
    }
    
    private static String getTextFromURLStream(JavadocHelper.TextStream page, int[] offsets, String charset) throws IOException {
        if (page == null)
            return null;

        InputStream fis = null;
        InputStreamReader fisreader = null;
        try {
            fis = page.openStream();
            fisreader = charset == null ? new InputStreamReader(fis) : new InputStreamReader(fis, charset);
            
            StringBuilder sb = new StringBuilder();
            int offset = 0;

            for (int i = 0; i < offsets.length - 1; i+=2) {
                int startOffset = offsets[i];
                int endOffset = offsets[i + 1];
                if (startOffset < 0 || endOffset < 0)
                    continue;
                if (startOffset > endOffset) {
                    LOG.log(Level.WARNING,
                            "Was not able to correctly parse javadoc: {0}, startOffset={1}, endOffset={2}.",
                            new Object[] {page.getLocation(), startOffset, endOffset});
                    return null;
                }

                int len = endOffset - startOffset;
                char buffer[] = new char[len];
                int bytesToSkip = startOffset - offset;
                long bytesSkipped = 0;
                do {
                    bytesSkipped = fisreader.skip(bytesToSkip);
                    bytesToSkip -= bytesSkipped;
                } while ((bytesToSkip > 0) && (bytesSkipped > 0));

                int bytesAlreadyRead = 0;
                do {
                    int count = fisreader.read(buffer, bytesAlreadyRead, len - bytesAlreadyRead);
                    if (count < 0){
                        break;
                    }
                    bytesAlreadyRead += count;
                } while (bytesAlreadyRead < len);
                sb.append(buffer);
                offset = endOffset;
            }
            return sb.toString();
        } finally {
            if (fisreader != null)
                fisreader.close();
        }
    }

    
    /** Retrieves the position (start offset and end offset) of class javadoc info
      * in the raw html file */
    private static int[] parseClass(Reader reader, final HTMLEditorKit.Parser parser, boolean ignoreCharset) throws IOException {
        final int INIT = 0;
        // javadoc HTML comment '======== START OF CLASS DATA ========'
        final int CLASS_DATA_START = 1;
        // start of the text we need. Located just after first P.
        final int TEXT_START = 2;
        // div tag after the CLASS_DATA_START
        final int INSIDE_DIV = 3;
        // div tag after the INSIDE_DIV
        final int AFTER_DIV = 4;

        final int state[] = new int[] {INIT};
        final int offset[] = new int[] {-1, -1, -1, -1};

        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {

            int div_counter = 0;
            int li_counter = 0;
            int section_counter = 0;
            int nextHRPos = -1;
            int lastHRPos = -1;

            @Override
            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR) {
                    if (state[0] == TEXT_START) {
                        nextHRPos = pos;
                    }
                    lastHRPos = pos;
                } else if (state[0] == AFTER_DIV
                        && t instanceof HTML.UnknownTag
                        && "section".equalsIgnoreCase(t.toString())) {
                    if (a.containsAttribute(HTML.Attribute.ENDTAG, "true")) {
                        if (--section_counter < 0) {
                            offset[3] = pos;
                            state[0] = INIT;
                        }
                    } else {
                        section_counter++;
                    }
                }
            }

            @Override
            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.P && state[0] == CLASS_DATA_START){
                    if (offset[0] != -1 && offset[1] == -1)
                        offset[1] = pos + 3;
                    else
                        state[0] = TEXT_START;
                } else if (t == HTML.Tag.DIV) {
                    if (state[0] == CLASS_DATA_START && a.containsAttribute(HTML.Attribute.CLASS, "block")) {
                        state[0] = INSIDE_DIV;
                        if (offset[2] == -1)
                            offset[2] = pos;
                    }
                    if (state[0] == INSIDE_DIV)
                        div_counter++;
                } else if (t == HTML.Tag.LI && state[0] == AFTER_DIV) {
                    li_counter++;
                } else if (t == HTML.Tag.A && state[0] == TEXT_START) {
                    String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                    if (attrName!=null && attrName.length()>0){
                        if (nextHRPos!=-1 && nextHRPos > offset[2]){
                            offset[3] = nextHRPos;
                        }else{
                            offset[3] = pos;
                        }
                        state[0] = INIT;
                    }
                }
            }

            @Override
            public void handleEndTag(Tag t, int pos) {
                if (t == HTML.Tag.DIV && state[0] == INSIDE_DIV) {
                    if (--div_counter == 0) {
                        if (offset[0] > -1 && offset[1] == -1) {
                            state[0] = CLASS_DATA_START;
                            offset[1] = pos;
                        } else {
                            state[0] = AFTER_DIV;
                        }
                    }
                } else if (t == HTML.Tag.LI && state[0] == AFTER_DIV) {
                    if (--li_counter < 0) {
                        offset[3] = pos;
                        state[0] = INIT;
                    }
                }
            }

            public void handleComment(char[] data, int pos){
                String comment = String.valueOf(data);
                if (comment!=null){
                    if (comment.indexOf("START OF CLASS DATA")>0){ //NOI18N
                        state[0] = CLASS_DATA_START;
                    } else if (comment.indexOf("NESTED CLASS SUMMARY")>0 //NOI18N
                            && offset[3] == -1){
                        if (lastHRPos!=-1 && lastHRPos > offset[2]){
                            offset[3] = lastHRPos;
                        }else{
                            offset[3] = pos;
                        }
                    }
                }
            }
            
            public void handleText(char[] data, int pos) {
                if (state[0] == CLASS_DATA_START && "Deprecated.".equals(new String(data))) { //NOI18N
                    offset[0] = lastHRPos + 4;
                } else if (state[0] == INSIDE_DIV && "Deprecated.".equals(new String(data))) { //NOI18N
                    offset[0] = offset[2];
                    offset[2] = -1;
                } else if (state[0] == TEXT_START && offset[2] < 0) {
                    offset[2] = pos;
                }
            }
        };        

        parser.parse(reader, callback, ignoreCharset);
        callback = null;
        return offset;
    }

    /** Retrieves the position (start offset and end offset) of member javadoc info
      * in the raw html file */
    private static int[] parseMember(Reader reader, final Collection<? extends String> names, final HTMLEditorKit.Parser parser, boolean ignoreCharset) throws IOException {
        final int INIT = 0;
        // 'A' tag with the name we are looking for.
        final int A_OPEN = 1;
        // close tag of 'A'
        final int A_CLOSE = 2;
        // PRE close tag after the A_CLOSE
        final int PRE_CLOSE = 3;
        // div tag after the PRE_CLOSE
        final int INSIDE_DIV = 4;

        final int state[] = new int[1];
        final int offset[] = new int[2];

        offset[0] = -1; //start offset
        offset[1] = -1; //end offset
        state[0] = INIT;

        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {

            int div_counter = 0;
            int dl_counter = 0;
            int li_counter = 0;
            int section_counter = 0;
            int hrPos = -1;
            boolean startWithNextText;

            @Override
            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR && state[0] == PRE_CLOSE) {
                    hrPos = pos;
                } else if (t instanceof HTML.UnknownTag
                        && "section".equalsIgnoreCase(t.toString())) {
                    if (a.containsAttribute(HTML.Attribute.ENDTAG, "true")) {
                        if (state[0] != INIT) {
                            if (--section_counter < 0) {
                                state[0] = INIT;
                                offset[1] = hrPos;
                            }
                        }
                    } else {
                        if (state[0] == INIT) {
                            String attrId = (String) a.getAttribute(HTML.Attribute.ID);
                            if (names.contains(attrId)) {
                                // we have found desired javadoc member info anchor
                                state[0] = A_OPEN;
                            }
                        } else {
                            section_counter++;
                        }
                    }
                }
            }

            @Override
            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.A) {
                    String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                    String attrId = (String)a.getAttribute(HTML.Attribute.ID);
                    if (names.contains(attrName) || names.contains(attrId)){
                        // we have found desired javadoc member info anchor
                        state[0] = A_OPEN;
                    } else {
                        if ((state[0] == PRE_CLOSE) && (attrName != null || attrId != null) && hrPos != -1){
                            // reach the end of retrieved javadoc info
                            state[0] = INIT;
                            offset[1] = hrPos;
                        }
                    }
                } else if (t == HTML.Tag.DL && state[0] == PRE_CLOSE) {
                    dl_counter++;
                } else if (t == HTML.Tag.LI && state[0] == PRE_CLOSE) {
                    li_counter++;
                } else if (t == HTML.Tag.DD && state[0] == PRE_CLOSE && offset[0] < 0){
                        offset[0] = pos;
                } else if (t == HTML.Tag.DIV && (state[0] == PRE_CLOSE || state[0] == A_CLOSE || state[0] == INSIDE_DIV)){
                    state[0] = INSIDE_DIV;
                    div_counter++;
                    if (offset[0] < 0) {
                        if (div_counter == 2) {
                          offset[0] = pos;
                        } else if (div_counter == 1 && a.containsAttribute(HTML.Attribute.CLASS, "block")) {
                            startWithNextText = true;
                        }
                    }
                }

            }

            @Override
            public void handleEndTag(HTML.Tag t, int pos){
                if (t == HTML.Tag.A && state[0] == A_OPEN){
                    state[0] = A_CLOSE;
                } else if (t == HTML.Tag.PRE && state[0] == A_CLOSE){
                    state[0] = PRE_CLOSE;
                } else if (t == HTML.Tag.DL && state[0] == PRE_CLOSE) {
                    if (--dl_counter == 0)
                        hrPos = pos;
                } else if (t == HTML.Tag.LI && state[0] == PRE_CLOSE) {
                    if (--li_counter < 0)
                        hrPos = pos;
                } else if (t == HTML.Tag.DIV && state[0] == INSIDE_DIV) {
                    if (--div_counter == 0) {
                        state[0] = PRE_CLOSE;
                        hrPos = pos;
                    }
                }
            }

            @Override
            public void handleText(char[] data, int pos) {
                if (startWithNextText) {
                    startWithNextText = false;
                    if (offset[0] < 0) {
                        offset[0] = pos;
                    }
                }
            }
            
            @Override
            public void handleComment(char[] data, int pos){
                String comment = String.valueOf(data);
                if (comment!=null){
                    if (comment.indexOf("END OF CLASS DATA")>0){ //NOI18N
                        if ((state[0] == PRE_CLOSE) && hrPos != -1){
                            // reach the end of retrieved javadoc info
                            state[0] = INIT;
                            offset[1] = hrPos;
                        }
                    }
                }
            }
        };

        parser.parse(reader, callback, ignoreCharset);
        callback = null;
        return offset;
    }

    /** Retrieves the position (start offset and end offset) of member javadoc info
      * in the raw html file */
    private static int[] parsePackage(Reader reader, final HTMLEditorKit.Parser parser, boolean ignoreCharset) throws IOException {
        final String name = "package_description"; //NOI18N
        final int INIT = 0;
        // 'A' tag with the name we are looking for.
        final int A_OPEN = 1;

        final int state[] = new int[1];
        final int offset[] = new int[2];

        offset[0] = -1; //start offset
        offset[1] = -1; //end offset
        state[0] = INIT;

        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {

            int hrPos = -1;

            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR && state[0]!=INIT){
                    if (state[0] == A_OPEN){
                        hrPos = pos;
                        offset[1] = pos;
                    }
                }
            }

            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {

                if (t == HTML.Tag.A) {
                    String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                    if (name.equals(attrName)){
                        // we have found desired javadoc member info anchor
                        state[0] = A_OPEN;
                        offset[0] = pos;
                    } else {
                        if (state[0] == A_OPEN && attrName!=null){
                            // reach the end of retrieved javadoc info
                            state[0] = INIT;
                            offset[1] = (hrPos!=-1) ? hrPos : pos;
                        }
                    }
                } 
            }
        };

        parser.parse(reader, callback, ignoreCharset);
        callback = null;
        return offset;
    }
    
}
