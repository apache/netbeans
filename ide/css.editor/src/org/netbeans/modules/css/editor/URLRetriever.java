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
package org.netbeans.modules.css.editor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mfukala@netbeans.org
 */
public class URLRetriever {
    
    private static final WeakHashMap<String, String> PAGES_CACHE = new WeakHashMap<>();
    
    public static String getURLContentAndCache(URL url) {
        //strip off the anchor url part
        String path = url.getPath();

        //try to load from cache
        String file_content = PAGES_CACHE.get(path);
        if (file_content == null) {
            try {
                ByteArrayOutputStream baos;
                try (InputStream is = url.openStream()) {
                    byte buffer[] = new byte[8096];
                    baos = new ByteArrayOutputStream();
                    int count = 0;
                    do {
                        count = is.read(buffer);
                        if (count > 0) {
                            baos.write(buffer, 0, count);
                        }
                    } while (count > 0);
                }
                file_content = baos.toString("UTF-8"); //NOI18N
                baos.close();
            } catch (java.io.IOException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Cannot read css help file.", e); //NOI18N
            }

            PAGES_CACHE.put(path, file_content);
        }
        
        return file_content;
    }
    
}
