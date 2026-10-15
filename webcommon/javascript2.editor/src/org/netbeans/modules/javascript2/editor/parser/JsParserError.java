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
package org.netbeans.modules.javascript2.editor.parser;

import java.util.Collection;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.css.lib.api.FilterableError;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsParserError implements FilterableError, Error.Badging {

    private final JsErrorManager.SimpleError error;
    private final FileObject file;
    private final Severity severity;
    private final Object[] parameters;
    private final boolean showExplorerBadge;
    private final boolean showInEditor;

    private final Collection<SetFilterAction> enableFilter;
    private final SetFilterAction disableFilter;

    public JsParserError(JsErrorManager.SimpleError error, FileObject file,
            Severity severity, Object[] parameters,
            boolean showExplorerBadge, boolean showInEditor,
            Collection<FilterableError.SetFilterAction> enableFilter, FilterableError.SetFilterAction disableFilter) {

        this.error = error;
        this.file = file;
        this.severity = severity;
        this.parameters = parameters != null ? parameters.clone() : new Object[] {};
        this.showExplorerBadge = showExplorerBadge;
        this.showInEditor = showInEditor;
        this.disableFilter = disableFilter;
        this.enableFilter = enableFilter;
    }

    @Override
    public String getDisplayName() {
        return error.getMessage();
    }

    @Override
    public String getDescription() {
        return error.getMessage();
    }

    @Override
    public String getKey() {
        int position = error.getStartPosition();
        return "[" + position + "," + position + "]-" + error.getMessage();
    }

    @Override
    public FileObject getFile() {
        return file;
    }

    @Override
    public int getStartPosition() {
        return error.getStartPosition();
    }

    @Override
    public int getEndPosition() {
        return error.getEndPosition();
    }

    @Override
    public boolean isLineError() {
        return error.isLineError();
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public boolean showExplorerBadge() {
        return showExplorerBadge;
    }

    public boolean showInEditor() {
        return showInEditor;
    }

    @Override
    public boolean isFiltered() {
        return disableFilter != null;
    }

    @Override
    public Collection<SetFilterAction> getEnableFilterActions() {
        return enableFilter;
    }

    @Override
    public SetFilterAction getDisableFilterAction() {
        return disableFilter;
    }
}
