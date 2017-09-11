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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.languages.dataobject;

import java.io.BufferedReader;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

@MIMEResolver.ExtensionRegistration(
    mimeType="text/x-nbs",
    position=210,
    displayName="#NBSResolver",
    extension={ "nbs" }
)
public class LanguagesDataObject extends MultiDataObject {

    public LanguagesDataObject(FileObject pf, LanguagesDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }

    @Override
    protected Node createNodeDelegate() {
        return new LanguagesDataNode(this);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    @Override
    protected DataObject handleCreateFromTemplate(final DataFolder df, final String name) throws IOException {
        DataObject createdClass = super.handleCreateFromTemplate(df, name);
        FileObject createdClassFO = createdClass.getPrimaryFile();
        Map<String, String> tokens = new HashMap<String, String>();
        tokens.put("__CLASS_NAME__", name);
        tokens.put("__PACKAGE_NAME__", getPackageName(createdClassFO));
        createFileWithSubstitutions(this.getPrimaryFile(), createdClassFO, tokens);
        return createdClass;
    }

    static String getPackageName(final FileObject createdClass) {
        FileObject parent = createdClass.getParent();
        // XXX bad. Acquire source directory though 'src.dir' property or some Scala's ClassPath
        while (parent != null && !"src".equals(parent.getNameExt())) {
            parent = parent.getParent();
        }
        return (parent == null) ? "test" // fallback
                : createdClass.getParent().getPath().substring(parent.getPath().length() + 1).replace('/', '.');
    }

    static void createFileWithSubstitutions(
            final FileObject sourceFO,
            final FileObject targetFO,
            final Map<String,String> tokens) throws IOException {
        FileLock lock = targetFO.lock();
        try {
            copyAndSubstituteTokens(sourceFO.getURL(), lock, targetFO, tokens);
        } finally {
            lock.releaseLock();
        }
    }
     
    private static void copyAndSubstituteTokens(final URL content,
            final FileLock lock, final FileObject targetFO, final Map<String,String> tokens) throws IOException {
        OutputStream os = targetFO.getOutputStream(lock);
        try {
            PrintWriter pw = new PrintWriter(os);
            try {
                InputStream is = content.openStream();
                try {
                    Reader r = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(r);
                    String line;
                    while ((line = br.readLine()) != null) {
                        pw.println(tokens == null ? line : replaceTokens(tokens, line));
                    }
                } finally {
                    is.close();
                }
            } finally {
                pw.close();
            }
        } finally {
            os.close();
        }
    }
    
    private static String replaceTokens(final Map<String,String> tokens, String line) {
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            line = line.replaceAll(entry.getKey(), entry.getValue());
        }
        return line;
    }
  
}

