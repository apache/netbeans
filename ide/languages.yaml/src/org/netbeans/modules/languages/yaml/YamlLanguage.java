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
package org.netbeans.modules.languages.yaml;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.MIMEResolver;

/**
 * GSF Configuration for YAML
 *
 * @author Tor Norbye
 */
@MIMEResolver.ExtensionRegistration(displayName = "#YAMLResolver",
extension = {"yml", "yaml"},
mimeType = "text/x-yaml",
position = 280)
@LanguageRegistration(mimeType = "text/x-yaml") //NOI18N
public class YamlLanguage extends DefaultLanguageConfig {

    @Override
    public Language getLexerLanguage() {
        return YamlTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "YAML";
    }

    @Override
    public String getLineCommentPrefix() {
        return "#"; // NOI18N
    }

    @Override
    public Parser getParser() {
        return new YamlParser();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new YamlScanner();
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new YamlSemanticAnalyzer();
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new YamlKeystrokeHandler();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new YamlCompletion();
    }

    @Override
    public InstantRenamer getInstantRenamer() {
        return null;
    }
}
