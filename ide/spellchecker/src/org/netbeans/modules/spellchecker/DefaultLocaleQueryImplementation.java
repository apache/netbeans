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
package org.netbeans.modules.spellchecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.StringTokenizer;
import org.netbeans.modules.spellchecker.spi.LocaleQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.spellchecker.spi.LocaleQueryImplementation.class, position=2000)
public class DefaultLocaleQueryImplementation implements LocaleQueryImplementation {
    
    /** Creates a new instance of DefaultLocaleQueryImplementation */
    public DefaultLocaleQueryImplementation() {
    }

    public Locale findLocale(FileObject file) {
        return getDefaultLocale();
    }
    
    private static final String FILE_NAME = "spellchecker-default-locale";
    
    private static FileObject getDefaultLocaleFile() {
        return FileUtil.getConfigFile(FILE_NAME);
    }
    
    public static Locale getDefaultLocale() {
        FileObject file = getDefaultLocaleFile();
        
        if (file == null)
            return Locale.getDefault ();
        
        Charset UTF8 = StandardCharsets.UTF_8;
        
        BufferedReader r = null;
        
        try {
            r = new BufferedReader(new InputStreamReader(file.getInputStream(), UTF8));
            
            String localeLine = r.readLine();
            
            if (localeLine == null || localeLine.trim().isEmpty())
                return null;
            
            String language = "";
            String country = "";
            String variant = "";
            
            StringTokenizer stok = new StringTokenizer(localeLine, "_");
            
            language = stok.nextToken();
            
            if (stok.hasMoreTokens()) {
                country = stok.nextToken();
                
                if (stok.hasMoreTokens())
                    variant = stok.nextToken();
            }
            
            return new Locale(language, country, variant);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            try {
                if (r != null)
                    r.close();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        return null;
    }
    
    public static void setDefaultLocale(Locale locale) {
        FileObject file = getDefaultLocaleFile();
        Charset UTF8 = StandardCharsets.UTF_8;
        FileLock lock = null;
        PrintWriter pw = null;
        
        try {
            if (file == null) {
                file = FileUtil.getConfigRoot().createData(FILE_NAME);
            }
            
            lock = file.lock();
            pw = new PrintWriter(new OutputStreamWriter(file.getOutputStream(lock), UTF8));
            
            pw.println(locale.toString());

            ComponentPeer.clearDoc2DictionaryCache();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            if (pw != null)
                pw.close();
            
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }
}
