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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
