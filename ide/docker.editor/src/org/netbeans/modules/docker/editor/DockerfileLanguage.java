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
package org.netbeans.modules.docker.editor;

import org.netbeans.api.lexer.Language;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.docker.editor.completion.DockerfileCompletion;
import org.netbeans.modules.docker.editor.lexer.DockerfileTokenId;
import org.netbeans.modules.docker.editor.parser.DockerfileParser;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Zezula
 */
@NbBundle.Messages("Dockerfile=Docker Build Files")
@LanguageRegistration(mimeType = DockerfileResolver.MIME_TYPE, useMultiview = true)
public final class DockerfileLanguage extends DefaultLanguageConfig {

    @Override
    public Language getLexerLanguage() {
        return DockerfileTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return Bundle.Dockerfile();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new DockerfileCompletion();
    }

    @Override
    public Parser getParser() {
        return new DockerfileParser();
    }

    @Override
    public String getLineCommentPrefix() {
        return "#"; //NOI18N
    }

    @NbBundle.Messages("Source=&Source")
    @MultiViewElement.Registration(
            displayName = "#Source",
            iconBase = "org/netbeans/modules/docker/editor/resources/docker_file.png",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            mimeType = DockerfileResolver.MIME_TYPE,
            preferredID = "dockerfile.source",
            position = 100
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

}
