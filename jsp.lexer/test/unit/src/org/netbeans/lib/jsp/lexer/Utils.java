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
