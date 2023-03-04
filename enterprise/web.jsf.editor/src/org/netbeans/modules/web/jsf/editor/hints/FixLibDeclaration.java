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

package org.netbeans.modules.web.jsf.editor.hints;

import javax.swing.text.Document;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class FixLibDeclaration implements HintFix{

    private final String nsPrefix;
    private final Library lib;
    private final Document doc;
    private final boolean isJsf22Plus;

    public FixLibDeclaration(Document doc, String nsPrefix, Library lib, boolean isJsf22Plus) {
        this.doc = doc;
        this.nsPrefix = nsPrefix;
        this.lib = lib;
        this.isJsf22Plus = isJsf22Plus;
    }

    @Override
    public String getDescription() {
        String namespace;
        if (isJsf22Plus || lib.getLegacyNamespace() == null) {
            namespace = lib.getNamespace();
        } else {
            namespace = lib.getLegacyNamespace();
        }
        return NbBundle.getMessage(FixLibDeclaration.class, "MSG_FixLibDeclaration", nsPrefix, namespace); //NOI18N
    }

    @Override
    public void implement() throws Exception {
        LibraryUtils.importLibrary(doc, lib, nsPrefix, isJsf22Plus);
    }

    @Override
    public boolean isSafe() {
        return true; // hope so...
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

}
