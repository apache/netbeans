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

package org.netbeans.modules.groovy.editor.completion.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 *  HTML Parser. It retrieves sections of the javadoc HTML file.
 *
 * @author  Martin Roskanin
 */
final class HTMLJavadocParser {

    public HTMLJavadocParser() {
        super();
    }
    
    /** Gets the javadoc text from the given URL
     *  @param url nbfs protocol URL
     *  @param pkg true if URL should be retrieved for a package
     */
    public static String getJavadocText(URL url, boolean pkg, boolean isGDK) {
        if (url == null) return null;
        
        HTMLEditorKit.Parser parser;
        InputStream is = null;
        
        String charset = null;
        for (;;) {
            try{
                is = url.openStream();
                parser = new ParserDelegator();
                String urlStr = URLDecoder.decode(url.toString(), "UTF-8"); //NOI18N
                int offsets[] = new int[2];
                Reader reader = charset == null?new InputStreamReader(is): new InputStreamReader(is, charset);
                
                if (pkg){
                    // package description
                    offsets = parsePackage(reader, parser, charset != null);
                }else if (urlStr.indexOf('#')>0){
                    // member javadoc info
                    String memberName = urlStr.substring(urlStr.indexOf('#')+1);
                    if (memberName.length()>0) offsets = parseMember(reader, memberName, parser, charset != null, isGDK);
                }else{
                    // class javadoc info
                    offsets = parseClass(reader, parser, charset != null);
                }
                
                if (offsets !=null && offsets[0]!=-1 && offsets[1]>offsets[0]){
                    return getTextFromURLStream(url, offsets[0], offsets[1], charset);
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
            } catch(IOException ioe){
                ioe.printStackTrace();
                break;
            }finally{
                // Findbugs removed: parser = null;
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
        
        spec = spec.toLowerCase(Locale.ENGLISH);
        
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
    
    private static String getTextFromURLStream(URL url, int startOffset, int endOffset, String charset) throws IOException{
        
        if (url == null) return null;
        
        if (startOffset > endOffset) {
            throw new IOException();
        }
        InputStream fis = url.openStream();
        InputStreamReader fisreader = charset == null ? new InputStreamReader(fis) : new InputStreamReader(fis, charset);
        char buffer[];
        try {
            int len = endOffset - startOffset;
            int bytesAlreadyRead = 0;
            buffer = new char[len];
            int bytesToSkip = startOffset;
            long bytesSkipped = 0;
            do {
                bytesSkipped = fisreader.skip(bytesToSkip);
                bytesToSkip -= bytesSkipped;
            } while ((bytesToSkip > 0) && (bytesSkipped > 0));

            do {
                int count = fisreader.read(buffer, bytesAlreadyRead, len - bytesAlreadyRead);
                if (count < 0) {
                    break;
                }
                bytesAlreadyRead += count;
            } while (bytesAlreadyRead < len);

        } finally {
            fisreader.close();
        }
        
        return new String(buffer);
    }

    
    /** Retrieves the position (start offset and end offset) of class javadoc info
      * in the raw html file */
    private static int[] parseClass(Reader reader, final HTMLEditorKit.Parser parser, boolean ignoreCharset) throws IOException {
        final int INIT = 0;
        // javadoc HTML comment '======== START OF CLASS DATA ========'
        final int CLASS_DATA_START = 1;
        // start of the text we need. Located just after first P.
        final int TEXT_START = 2;

        final int state[] = new int[1];
        final int offset[] = new int[2];

        offset[0] = -1; //start offset
        offset[1] = -1; //end offset
        state[0] = INIT;

        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {

            int nextHRPos = -1;
            int lastHRPos = -1;

            @Override
            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR){
                    if (state[0] == TEXT_START){
                        nextHRPos = pos;
                    }
                    lastHRPos = pos;
                }
            }

            @Override
            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.P && state[0] == CLASS_DATA_START){
                    state[0] = TEXT_START;
                }
                if (t == HTML.Tag.A && state[0] == TEXT_START) {
                    String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                    if (attrName!=null && attrName.length()>0){
                        if (nextHRPos!=-1){
                            offset[1] = nextHRPos;
                        }else{
                            offset[1] = pos;
                        }
                        state[0] = INIT;
                    }
                }
            }

            @Override
            public void handleComment(char[] data, int pos){
                String comment = String.valueOf(data);
                if (comment!=null){
                    if (comment.indexOf("START OF CLASS DATA")>0){ //NOI18N
                        state[0] = CLASS_DATA_START;
                    } else if (comment.indexOf("NESTED CLASS SUMMARY")>0){ //NOI18N
                        if (lastHRPos!=-1){
                            offset[1] = lastHRPos;
                        }else{
                            offset[1] = pos;
                        }
                    }
                }
            }
            
            @Override
            public void handleText(char[] data, int pos) {
                if (state[0] == TEXT_START && offset[0] < 0)
                    offset[0] = pos;
            }
        };        

        parser.parse(reader, callback, ignoreCharset);
        // Findbugs-Removed: callback = null;
        return offset;
    }

    
    /** Retrieves the position (start offset and end offset) of member javadoc info
      * in the raw html file */
    private static int[] parseMember(Reader reader, final String name, final HTMLEditorKit.Parser parser, boolean ignoreCharset, final boolean isGDK) throws IOException {
        final int INIT = 0;
        // 'A' tag with the name we are looking for.
        final int A_OPEN = 1;
        // close tag of 'A'
        final int A_CLOSE = 2;
        // PRE close tag after the A_CLOSE
        final int PRE_CLOSE = 3;

        final int state[] = new int[1];
        final int offset[] = new int[2];

        offset[0] = -1; //start offset
        offset[1] = -1; //end offset
        state[0] = INIT;

        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {

            int hrPos = -1;

            String methodName(String signature){
                // System.out.println("methodName(signature): " + signature);
                
                if(signature ==  null){
                    return "<NULL>";
                }
                
                int idx = signature.indexOf("(");
                if(idx != -1) {
                    return signature.substring(0,idx);
                } else {
                    return signature;
                }
            }
            
            int countParameters(String signature){
                // System.out.println("methodName(signature): " + signature);
                
                int openSign  = signature.indexOf("(");
                int closeSign = signature.indexOf(")");
                
                if(openSign == -1 || closeSign == -1 || (closeSign <= openSign)){
                    return -1;
                }
            
                // "()" means no parameter
                if((closeSign - openSign) == 1){
                    return 0;
                } else {
                    // count the number of commas:
                    String paramList = signature.substring(openSign + 1, closeSign);
                    
                    int num = 0;
                    int idx = 0;
                    
                    while((idx = paramList.indexOf(",", idx)) != -1 ){
                        idx++;
                        num++;
                    }
                    
                    return num + 1;
                }
                
            }
            
            
            boolean checkSignatureLink(String signature, String attrName, final boolean isGDK) {
                
                // There's a difference in JavaDoc Link format. GDK comes with variable names:
                // JDK: String.html#endsWith(java.lang.String)
                // GDK: String.html#center(java.lang.Number%20numberOfChars,%20java.lang.String%20padding)
                
                if(signature == null && attrName == null){
                    return false;
                }
               
                if (isGDK) {
                    // FIXME: now we are a little bit smarter: We count the 
                    // number of parameters. Alas, if the count is equal, but 
                    // not of the same type we match the first one (which will
                    // be wrong sometimes [see Object.use(...)] )
                    
                    if(methodName(signature).equals(methodName(attrName))) {
                        if (countParameters(signature) == countParameters(attrName)) {
                            return true;
                        }

                    }
                    
                } else { // This is the standart case we are dealing with JDK JavaDocs
                    if (signature.equals(attrName)) {
                        return true;
                    }
                }
                return false;

            }
            
            
            @Override
            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR && state[0]!=INIT){
                    if (state[0] == PRE_CLOSE){
                        hrPos = pos;
                    }
                }
            }

            @Override
            public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {

                if (t == HTML.Tag.A) {
                    String attrName = (String)a.getAttribute(HTML.Attribute.NAME);
                    if (checkSignatureLink(name, attrName, isGDK)){
                        // we have found desired javadoc member info anchor
                        state[0] = A_OPEN;
                    } else {
                        if (state[0] == PRE_CLOSE && attrName!=null){
                            // reach the end of retrieved javadoc info
                            state[0] = INIT;
                            offset[1] = (hrPos!=-1) ? hrPos : pos;
                        }
                    }
                } else if (t == HTML.Tag.DD && state[0] == PRE_CLOSE && offset[0] < 0){
                    offset[0] = pos;
                }

            }

            @Override
            public void handleEndTag(HTML.Tag t, int pos){
                if (t == HTML.Tag.A && state[0] == A_OPEN){
                    state[0] = A_CLOSE;
                } else if (t == HTML.Tag.PRE && state[0] == A_CLOSE){
                    state[0] = PRE_CLOSE;
                }
            }

        };

        parser.parse(reader, callback, ignoreCharset);
        // Findbugs-Removed: callback = null;
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

            @Override
            public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
                if (t == HTML.Tag.HR && state[0]!=INIT){
                    if (state[0] == A_OPEN){
                        hrPos = pos;
                        offset[1] = pos;
                    }
                }
            }

            @Override
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
        // Findbugs-Removed: callback = null;
        return offset;
    }
    
}
