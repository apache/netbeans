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

package org.netbeans.lib.jsp.lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.jsp.lexer.JspParseData;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test utility class
 *
 * @author Marek.Fukala@Sun.COM
 */
public class Utils {

    /** Reads a FileObject's content into a string. */
    public static CharSequence readFileContentToString(File file) throws IOException {
        StringBuffer buff = new StringBuffer();
        BufferedReader rdr = new BufferedReader(new FileReader(file));

        String line;

        try{
            while ((line = rdr.readLine()) != null){
                buff.append(line + "\n");
            }
        } finally{
            rdr.close();
        }
        
        return buff;
    }
    
    public static void dumpTokens(FileObject file, Map<String, String> prefix2libraryMap, PrintStream out) throws IOException {
        String source = file.asText("UTF-8");
        
        
        JspParseData jspParseData = new JspParseData(prefix2libraryMap, true, true, true);

        InputAttributes inputAttributes = new InputAttributes();
        inputAttributes.setValue(JspTokenId.language(), JspParseData.class, jspParseData, false);
        
        TokenHierarchy th = TokenHierarchy.create(source, false, JspTokenId.language(), Collections.EMPTY_SET, inputAttributes);
        
        
        TokenSequence<JspTokenId> ts = th.tokenSequence(JspTokenId.language());
        dumpTokens(ts, out);
        
    }
    
    
    private static void dumpTokens(TokenSequence ts, PrintStream out){
        ts.moveStart();
        while (ts.moveNext()) {
            Token token = ts.token();
            TokenSequence embedded = ts.embedded();
            if (embedded != null){
                //weird logic, copied from the original source to preserve the golden files format
                embedded.moveStart();
                if(embedded.moveNext()) {
                    //there's something in there
                    dumpTokens(embedded, out);
                } else {
                    //nothing, dump the token itself
                    dumpToken(token, out);
                }
            } else {
                //no embedding at the token
                dumpToken(token, out);
            }
        }
    }
  
    private static void dumpToken(Token<?> token, PrintStream out) {
        out.append(token.id().name());
        out.append(':');
        out.append(token.text());
        out.append('\n');
    }
    

    
}
