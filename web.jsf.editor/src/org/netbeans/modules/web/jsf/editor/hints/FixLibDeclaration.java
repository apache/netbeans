/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
