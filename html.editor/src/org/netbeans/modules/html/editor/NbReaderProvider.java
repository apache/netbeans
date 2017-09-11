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
package org.netbeans.modules.html.editor;

import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.lib.api.dtd.ReaderProvider;
import org.openide.filesystems.*;
import org.openide.util.Exceptions;

public class NbReaderProvider implements ReaderProvider {

    private static final String CATALOG_FILE_NAME = "catalog"; // NOI18N

    private Map<String, String> mapping = new HashMap<>();
    private FileObject dtdSetFolder;

    @Deprecated
    public static synchronized void setupReaders() {
        Logger.global.log(Level.INFO, "Please remove the call to NbReaderProvider.setupReaders(), it is not necessary anymore.", new Exception()); //NOI18N
    }

    public NbReaderProvider(FileObject folder) {
        dtdSetFolder = folder;
        initialize();
    }

    @Override
    public Collection<String> getIdentifiers() {
        return mapping.keySet();
    }

    @Override
    public Reader getReaderForIdentifier(String identifier, String filename) {
        FileObject file = getSystemId(identifier);
        if(file == null) {
            return null;
        }
        
        try {
            return new InputStreamReader(file.getInputStream());
        } catch (FileNotFoundException exc) {
            return null;
        }
        
    }

    @Override
    public boolean isXMLContent(String identifier) {
        return HtmlVersion.find(identifier, null).isXhtml();
    }

    @Override
    public FileObject getSystemId(String publicId) {
         String fileName = (String) mapping.get(publicId);
        if (fileName == null) {
            return null;
        }
        if (dtdSetFolder == null) {
            return null;
        }

        FileObject file = dtdSetFolder.getFileObject(fileName);
        return file;
    }

    private void initialize() {
        FileObject catalog = dtdSetFolder.getFileObject(CATALOG_FILE_NAME);
        if (catalog != null) {
            try {
                mapping.putAll(parseCatalog(new InputStreamReader(catalog.getInputStream())));
            } catch (FileNotFoundException exc) {
                Exceptions.printStackTrace(exc);
            }
        }
    }

    private Map<String, String> parseCatalog(Reader catalogReader) {
        HashMap hashmap = new HashMap();
        LineNumberReader reader = new LineNumberReader(catalogReader);

        for (;;) {
            String line;

            try {
                line = reader.readLine();
            } catch (IOException exc) {
                return null;
            }

            if (line == null) {
                break;
            }

            StringTokenizer st = new StringTokenizer(line);
            if (st.hasMoreTokens() && "PUBLIC".equals(st.nextToken()) && st.hasMoreTokens()) { // NOI18N
                st.nextToken("\""); // NOI18N
                if (!st.hasMoreTokens()) {
                    continue;
                }
                String id = st.nextToken("\""); // NOI18N

                if (!st.hasMoreTokens()) {
                    continue;
                }
                st.nextToken(" \t\n\r\f"); // NOI18N

                if (!st.hasMoreTokens()) {
                    continue;
                }
                String file = st.nextToken();
                hashmap.put(id, file);
            }
        }
        return hashmap;
    }

}
