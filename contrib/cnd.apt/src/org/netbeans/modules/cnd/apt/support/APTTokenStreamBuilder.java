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

package org.netbeans.modules.cnd.apt.support;

import java.io.Reader;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.impl.support.generated.APTLexer;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;

/**
 * Creates token stream for input path
 * 
 */
public final class APTTokenStreamBuilder {

    private APTTokenStreamBuilder() {
    }   
    
//    public static TokenStream buildTokenStream(File file) throws FileNotFoundException {  
//        String path = file.getAbsolutePath();
//        // could be problems with closing this stream
//        InputStream stream = new BufferedInputStream(new FileInputStream(file), TraceFlags.BUF_SIZE);        
//        return buildTokenStream(path, stream);
//    }
    
    public static TokenStream buildTokenStream(String text, String lang) {
        return buildTokenStream(text, APTDriver.langFlavorToAPTFileKind(lang));
    }
    public static TokenStream buildTokenStream(String text, APTFile.Kind aptKind) {
        char[] buf = new char[text.length()];
        text.getChars(0, text.length(), buf, 0);
        APTLexer lexer = new APTLexer(buf);
        lexer.init(text, 0, aptKind);
        return lexer;
    }  

    public static TokenStream buildTokenStream(char[] buf, APTFile.Kind aptKind) {
        APTLexer lexer = new APTLexer(buf);
        lexer.init("", 0, aptKind); //NOI18N
        return lexer;
    }
    
    public static TokenStream buildTokenStream(CharSequence name, Reader in, APTFile.Kind aptKind) {
        APTLexer lexer = new APTLexer(in);
        lexer.init(name.toString(), 0, aptKind);
        return lexer;
    }     

    public static TokenStream buildTokenStream(CharSequence name, char[] buf, APTFile.Kind aptKind) {
        APTLexer lexer = new APTLexer(buf);
        lexer.init(name.toString(), 0, aptKind);
        return lexer;
    }
    
    public static TokenStream buildLightTokenStream(CharSequence name, Reader in, APTFile.Kind aptKind) {
        APTLexer lexer = new APTLexer(in);
        lexer.init(name.toString(), 0, aptKind);
        lexer.setOnlyPreproc(true);
        return lexer;
    }    

    public static TokenStream buildLightTokenStream(CharSequence name, char[] buf, APTFile.Kind aptKind) {
        APTLexer lexer = new APTLexer(buf);
        lexer.init(name.toString(), 0, aptKind);
        lexer.setOnlyPreproc(true);
        return lexer;
    }
}
