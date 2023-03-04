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

package org.netbeans.modules.lexer.nbbridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.LanguageManager;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.openide.filesystems.FileObject;

/**
 *
 * @author vita
 */
@MimeLocation(subfolderName="languagesEmbeddingMap", instanceProviderClass=MimeLookupFolderInfo.class)
public class MimeLookupFolderInfo implements InstanceProvider {
    
    private static final Logger LOG = Logger.getLogger(MimeLookupFolderInfo.class.getName());
    
    public MimeLookupFolderInfo() {
    }

    public Object createInstance(List fileObjectList) {
        HashMap<String, LanguageEmbedding<?>> map
                = new HashMap<String, LanguageEmbedding<?>>();
        
        for(Object o : fileObjectList) {
            assert o instanceof FileObject : "fileObjectList should contain FileObjects and not " + o; //NOI18N
            
            FileObject f = (FileObject) o;
            try {
                Object [] info = parseFile(f);
                String mimeType = (String) info[0];
                int startSkipLength = (Integer) info[1];
                int endSkipLength = (Integer) info[2];
                
                if (isMimeTypeValid(mimeType)) {
                    Language<?> language = LanguageManager.getInstance().findLanguage(mimeType);
                    if (language != null) {
                        map.put(f.getName(), LanguageEmbedding.create(language, startSkipLength, endSkipLength));
                    } else {
                        LOG.warning("Can't find Language for mime type '" + mimeType + "', ignoring."); //NOI18N
                    }
                } else {
                    LOG.log(Level.WARNING, "Ignoring invalid mime type '" + mimeType + "' from: " + f.getPath()); //NOI18N
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, "Can't read language embedding definition from: " + f.getPath()); //NOI18N
            }
        }
        
        return new LanguagesEmbeddingMap(map);
    }
    
    private boolean isMimeTypeValid(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        int slashIndex = mimeType.indexOf('/'); //NOI18N
        if (slashIndex == -1) { // no slash
            return false;
        }
        if (mimeType.indexOf('/', slashIndex + 1) != -1) { //NOI18N
            return false;
        }
        return true;
    }
    
    private Object [] parseFile(FileObject f) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(f.getInputStream()));
        try {
            String line;
            
            while (null != (line = r.readLine())) {
                line.trim();
                if (line.length() != 0) {
                    String [] parts = line.split(","); //NOI18N
                    return new Object [] { 
                        parts[0], 
                        parts.length > 1 ? toInt(parts[1], "Ignoring invalid start-skip-length ''{0}'' in " + f.getPath()) : 0, //NOI18N
                        parts.length > 2 ? toInt(parts[2], "Ignoring invalid end-skip-length ''{0}'' in " + f.getPath()) : 0 //NOI18N
                    };
                }
            }
            
            return null;
        } finally {
            r.close();
        }
    }
    
    private int toInt(String s, String errorMsg) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, MessageFormat.format(errorMsg, s), e);
            return 0;
        }
    }
    
}
