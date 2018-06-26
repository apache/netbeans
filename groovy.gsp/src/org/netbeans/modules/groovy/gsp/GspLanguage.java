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

package org.netbeans.modules.groovy.gsp;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.lexer.Language;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import static org.netbeans.modules.groovy.gsp.GspLanguage.ACTIONS;
import static org.netbeans.modules.groovy.gsp.GspLanguage.GSP_ICON;
import static org.netbeans.modules.groovy.gsp.GspLanguage.GSP_MIME_TYPE;
import org.netbeans.modules.groovy.gsp.lexer.GspLexerLanguage;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


@LanguageRegistration(
    mimeType = GSP_MIME_TYPE,
    useMultiview = true
)
@NbBundle.Messages("GspResolver=Gsp Files")
@MIMEResolver.ExtensionRegistration(
    mimeType = GSP_MIME_TYPE,
    displayName = "#GspResolver",
    extension = "gsp",
    position = 255
)
@ActionReferences(value = {
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"), path = ACTIONS, position = 100),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"), path = ACTIONS, position = 200, separatorAfter = 300),
    @ActionReference(id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"), path = ACTIONS, position = 400),
    @ActionReference(id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"), path = ACTIONS, position = 500, separatorAfter = 600),
    @ActionReference(id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), path = ACTIONS, position = 700),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"), path = ACTIONS, position = 800, separatorAfter = 900),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"), path = ACTIONS, position = 1000, separatorAfter = 1100),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"), path = ACTIONS, position = 1200),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), path = ACTIONS, position = 1300)
})
public class GspLanguage extends DefaultLanguageConfig {

    public static final String GSP_MIME_TYPE = "text/x-gsp"; // NOI18N
    public static final String GSP_ICON = "org/netbeans/modules/groovy/gsp/resources/GspFile16x16.png"; // NOI18N
    public static final String ACTIONS = "Loaders/" + GSP_MIME_TYPE + "/Actions"; // NOI18N


    public GspLanguage() {
        super();
    }

    @Override
    public String getLineCommentPrefix() {
        return "//"; // NOI18N
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c) || (c == '$');
    }

    @Override
    public Language getLexerLanguage() {
        return GspLexerLanguage.getLanguage();
    }

    @Override
    public String getDisplayName() {
        return "GSP"; // NOI18N
    }

    @Override
    public String getPreferredExtension() {
        return "gsp"; // NOI18N
    }

    @Override
    public boolean isUsingCustomEditorKit() {
        return true;
    }

    @Override
    public Parser getParser() {
        return new GspParser();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new GspStructureScanner();
    }

    @Override
    public Set<String> getSourcePathIds() {
        return Collections.singleton(ClassPath.SOURCE);
    }

    @NbBundle.Messages("CTL_SourceTabCaption=&Source")
    @MultiViewElement.Registration(
        iconBase = GSP_ICON,
        mimeType = GSP_MIME_TYPE,
        position = 1,
        preferredID = "groovy.gsp",
        displayName = "#CTL_SourceTabCaption",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
    )
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }
}
