/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
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

        variables = new HashMap<String, TreePath>(variables);
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
                return new HintContext(info, settings, metadata, path, variables, multiVariables, variableNames, Collections.<String, TypeMirror>emptyMap(), new LinkedList<MessageImpl>(), false, new AtomicBoolean(), -1);
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
