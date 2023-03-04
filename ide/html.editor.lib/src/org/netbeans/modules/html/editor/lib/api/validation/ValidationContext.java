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

package org.netbeans.modules.html.editor.lib.api.validation;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.openide.filesystems.FileObject;

/**
 *
 * Possible features: filter.foreign.namespaces
 *
 * @author marekfukala
 */
public final class ValidationContext {

    private Reader source;
    private FileObject file;
    private HtmlVersion version;
    private SyntaxAnalyzerResult result;
    private Map<String, Boolean> features = new HashMap<>();

    public ValidationContext(Reader source, HtmlVersion version, FileObject file, SyntaxAnalyzerResult result) {
        this.source = source;
        this.file = file;
        this.version = version;
        this.result = result;
    }

    public FileObject getFile() {
        return file;
    }

    public Reader getSourceReader() {
        return source;
    }

    public HtmlVersion getVersion() {
        return version;
    }

    public SyntaxAnalyzerResult getSyntaxAnalyzerResult() {
        return result;
    }

    public boolean isFeatureEnabled(String featureName) {
        Boolean val = features != null ? features.get(featureName) : null;
        return val != null ? val : false;
    }

    public void enableFeature(String featureName, boolean enabled) {
        features.put(featureName, enabled);
    }

}
