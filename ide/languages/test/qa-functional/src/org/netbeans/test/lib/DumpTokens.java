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

package org.netbeans.test.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyledDocument;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jindrich Sedek
 */
public class DumpTokens {

    private File file = null;
    private String str = null;
    private List<Token> tokens = null;

    public DumpTokens(File file) {
        this.file = file;
    }

    public String getTokenString() {
        if (str == null) {
        Logger.getLogger(DumpTokens.class.getName()).info("Getting token string");
            Iterator<Token> iterator = getTokens().iterator();
            while (iterator.hasNext()) {
                Token token = iterator.next();
                String next = token.id().name() + ":" + token.text().toString() + "\n";
                if (str == null) {
                    str = next;
                } else {
                    str = str.concat(next);
                }
            }
        }
        return str;
    }

    private List<Token> getTokens() {
        if (tokens == null) {
            try{
                tokens = dumpTokens();
            }catch(IOException e){
                AssertionError error = new AssertionError("Dumping error");
                error.initCause(e);
                throw error;
            }
        }
        return tokens;
    }

    @SuppressWarnings("unchecked")
    private List<Token> dumpTokens() throws IOException {
        Logger.getLogger(DumpTokens.class.getName()).info("Dumping tokens");
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(file));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);

        StyledDocument sDoc = ed.openDocument();
        BaseDocument doc = (BaseDocument) sDoc;
        TokenHierarchy th = null;
        TokenSequence ts = null;
        int roundCount = 0;
        while ((th == null) || (ts == null)){
            th = TokenHierarchy.get(doc);
            if (th != null){
                ts = th.tokenSequence();
            }
            roundCount++;
            if (roundCount > 50){
                throw new AssertionError("Impossible to get token hierarchy " +roundCount+ "times");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            
        }
        try{
            Logger.getLogger(DumpTokens.class.getName()).info("Parsing token sequence");
            List<Token> tok = dumpTokens(ts);
            return tok;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    private List<Token> dumpTokens(TokenSequence ts){
        List<Token> result = null;
        if (ts == null) {
            throw new AssertionError("No token sequence");
        }
        ts.move(0);

        if (result == null) {
            result = new ArrayList<Token>();
        }
        while (ts.moveNext()) {
            Token token = ts.token();
            if (ts.embedded()!= null){
                List<Token> emb = dumpTokens(ts.embedded());
                if (emb != null){
                    result.addAll(emb);
                }else{
                    result.add(token);
                }
            }else{
                result.add(token);
            }
        }
        return result;
    }
  
    
}
