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
