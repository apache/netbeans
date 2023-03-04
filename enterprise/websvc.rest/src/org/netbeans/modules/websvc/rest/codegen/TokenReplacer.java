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
package org.netbeans.modules.websvc.rest.codegen;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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
            reader = new BufferedReader(new InputStreamReader( 
                    new FileInputStream((FileUtil.toFile(fo))), StandardCharsets.UTF_8));
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