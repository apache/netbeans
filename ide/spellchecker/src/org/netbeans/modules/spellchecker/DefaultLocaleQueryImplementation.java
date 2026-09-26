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
package org.netbeans.modules.spellchecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.StringTokenizer;
import org.netbeans.modules.spellchecker.spi.LocaleQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.spellchecker.spi.LocaleQueryImplementation.class, position=2000)
public class DefaultLocaleQueryImplementation implements LocaleQueryImplementation {

    private static final String DEFAULT_LOCALE_FILE = "spellchecker-default-locale";

    /** Creates a new instance of DefaultLocaleQueryImplementation */
    public DefaultLocaleQueryImplementation() {
    }

    @Override
    public Locale findLocale(FileObject file) {
        return getDefaultLocale();
    }


    private static FileObject getDefaultLocaleFile() {
        return FileUtil.getConfigFile(DEFAULT_LOCALE_FILE);
    }

    public static Locale getDefaultLocale() {
        FileObject file = getDefaultLocaleFile();

        if (file == null)
            return Locale.getDefault();

        BufferedReader r = null;

        try {
            r = new BufferedReader(new InputStreamReader(file.getInputStream(), UTF_8));

            String localeLine = r.readLine();

            if (localeLine == null || localeLine.trim().isEmpty())
                return null;

            StringTokenizer stok = new StringTokenizer(localeLine, "_");

            String language = stok.nextToken();
            String country = "";
            String variant = "";

            if (stok.hasMoreTokens()) {
                country = stok.nextToken();

                if (stok.hasMoreTokens())
                    variant = stok.nextToken();
            }

            return Locale.of(language, country, variant);
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

        try {
            if (file == null) {
                file = FileUtil.getConfigRoot().createData(DEFAULT_LOCALE_FILE);
            }

            try (var lock = file.lock(); var pw = new PrintWriter(new OutputStreamWriter(file.getOutputStream(lock), UTF_8))) {
                pw.println(locale.toString());
                ComponentPeer.clearDoc2DictionaryCache();
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
}
