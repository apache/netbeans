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

package org.netbeans.spi.java.hints;

import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.SPIAccessor;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint.Kind;

/**
 *
 * @author Jan Lahoda
 */
public class HintContext {

    private final CompilationInfo info;
    private final HintsSettings settings;
    private final Preferences preferences;
    private final Severity severity;
    private final HintMetadata metadata;
    private final TreePath path;
    private final Map<String, TreePath> variables;
    private final Map<String, Collection<? extends TreePath>> multiVariables;
    private final Map<String, String> variableNames;
    private final Collection<? super MessageImpl> messages;
    private final Map<String, TypeMirror> constraints;
    private final boolean bulkMode;
    private final AtomicBoolean cancel;
    private final int caret;

    private HintContext(CompilationInfo info, HintsSettings settings, HintMetadata metadata, TreePath path, Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variableNames, Map<String, TypeMirror> constraints, Collection<? super MessageImpl> problems, boolean bulkMode, AtomicBoolean cancel, int caret) {
        this.info = info;
        this.settings = settings;
        this.preferences = metadata != null ? settings.getHintPreferences(metadata) : null;
        this.severity = preferences != null ? settings.getSeverity(metadata) : Severity.ERROR;
        this.metadata = metadata;
        this.path = path;

        variables = new HashMap<>(variables);
        variables.put("$_", path);
        
        this.variables = variables;
        this.multiVariables = multiVariables;
        this.variableNames = variableNames;
        this.messages = problems;
        this.constraints = constraints;
        this.bulkMode = bulkMode;
        this.cancel = cancel;
        this.caret = caret;
    }

    public CompilationInfo getInfo() {
        return info;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public Severity getSeverity() {
        return severity;
    }

    public TreePath getPath() {
        return path;
    }

    public Map<String, TreePath> getVariables() {
        return variables;
    }

    public Map<String, Collection<? extends TreePath>> getMultiVariables() {
        return multiVariables;
    }

    public Map<String, String> getVariableNames() {
        return variableNames;
    }

    HintMetadata getHintMetadata() {
        return metadata;
    }

    //TODO: not sure it should be here:
    public Map<String, TypeMirror> getConstraints() {
        return constraints;
    }

    /**
     * Will be used only for refactoring(s), will be ignored for hints.
     * 
     * @param kind
     * @param text
     */
    public void reportMessage(MessageKind kind, String text) {
        messages.add(new MessageImpl(kind, text));
    }

    /**Returns {@code true} if the hint is being run in over many files, {@code false}
     * if only the file opened in the editor is being inspected.
     *
     * @return {@code true} if the hint is being run in over many files.
     */
    public boolean isBulkMode() {
        return bulkMode;
    }

    /**Returns {@code true} if the computation has been canceled.
     *
     * @return {@code true} if the computation has been canceled.
     */
    public boolean isCanceled() {
        return cancel.get();
    }

    /**For suggestions, returns the caret location for the editor
     * for which the suggestion is being computed. Returns -1 for hints.
     *
     * @return for suggestions, returns the caret location, -1 otherwise
     */
    public int getCaretLocation() {
        return metadata.kind == Kind.ACTION ? caret : -1;
    }
    
    public enum MessageKind {
        WARNING, ERROR;
    }
    
    static {
        SPIAccessor.setINSTANCE(new SPIAccessor() {
            @Override public HintContext createHintContext(CompilationInfo info, HintsSettings settings, HintMetadata metadata, TreePath path, Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variableNames, Map<String, TypeMirror> constraints, Collection<? super MessageImpl> problems, boolean bulkMode, AtomicBoolean cancel, int caret) {
                return new HintContext(info, settings, metadata, path, variables, multiVariables, variableNames, constraints, problems, bulkMode, cancel, caret);
            }
            @Override public HintContext createHintContext(CompilationInfo info, HintsSettings settings, HintMetadata metadata, TreePath path, Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variableNames) {
                return new HintContext(info, settings, metadata, path, variables, multiVariables, variableNames, Collections.<String, TypeMirror>emptyMap(), new LinkedList<>(), false, new AtomicBoolean(), -1);
            }
            @Override public HintMetadata getHintMetadata(HintContext ctx) {
                return ctx.getHintMetadata();
            }
            @Override public HintsSettings getHintSettings(HintContext ctx) {
                return ctx.settings;
            }
        });
    }
}
