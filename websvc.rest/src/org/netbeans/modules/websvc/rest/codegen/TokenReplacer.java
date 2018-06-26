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
package org.netbeans.modules.websvc.rest.codegen;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class TokenReplacer {

    TokenReplacer( ClientStubsGenerator clientStubsGenerator ) {
        this.clientStubsGenerator = clientStubsGenerator;
    }

    private Map<String, String> tokens = new HashMap<String, String>();

    public Map<String, String> getTokens() {
        return Collections.unmodifiableMap(tokens);
    }

    public void addToken(String name, String value) {
        tokens.put(name, value);
    }

    public void setTokens(Map<String, String> tokens) {
        this.tokens = tokens;
    }

    public void replaceTokens(FileObject fo) throws IOException {
        replaceTokens(fo, getTokens());
    }

    public void replaceTokens(FileObject fo, Map<String, String> tokenMap) throws IOException {
        FileLock lock = fo.lock();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader( new InputStreamReader( 
                    new FileInputStream((FileUtil.toFile(fo))), 
                    Charset.forName("UTF-8")));         // NOI18N
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                line = replaceTokens(line, "", "", tokenMap);
                sb.append(line);
                sb.append("\n");
            }
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), 
                    clientStubsGenerator.getBaseEncoding());
            try {
                writer.write(sb.toString());
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
            if ( reader!= null ){
                reader.close();
            }
        }
    }
    
    protected ClientStubsGenerator getGenerator(){
        return clientStubsGenerator;
    }

    private String replaceTokens(String line, String object, String pkg, Map<String, String> tokenMap) {
        String replacedLine = line;
        for(Map.Entry e:tokenMap.entrySet()) {
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            if(key != null && value != null)
                replacedLine = replacedLine.replaceAll(key, value);
        }
        return replacedLine;
    }
    
    private final ClientStubsGenerator clientStubsGenerator;
}