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
package org.netbeans.modules.java.hints.analyzer;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

public final class AnalyzeFolder extends AbstractAction implements ContextAwareAction {

    private final boolean def;
    private final Lookup context;

    public AnalyzeFolder() {
        context = Utilities.actionsGlobalContext();
        def = true;
        putValue(NAME, NbBundle.getMessage(AnalyzeFolder.class, "CTL_AnalyzeFolder"));
    }

    @Override
    public boolean isEnabled() {
        if (!def) {
            return super.isEnabled();
        }
        
        return Analyzer.normalizeLookup(context) != null;
    }

    public AnalyzeFolder(Lookup context) {
        this.context = context;
        def = false;
        setEnabled(Analyzer.normalizeLookup(context) != null);
        putValue(NAME, NbBundle.getMessage(AnalyzeFolder.class, "CTL_AnalyzeFolder"));
    }
    
    private static final Set<String> SUPPORTED_IDS = new HashSet<String>(Arrays.asList("create-javadoc", "error-in-javadoc"));
    
    public void actionPerformed(ActionEvent e) {
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                final HintsSettings hintsSettings = new HintsSettings() {
                    private final HintsSettings delegate = HintsSettings.getGlobalSettings();
                    @Override public boolean isEnabled(HintMetadata hint) {
                        return SUPPORTED_IDS.contains(hint.id);
                    }
                    @Override public void setEnabled(HintMetadata hint, boolean value) {
                        throw new UnsupportedOperationException("Not supported.");
                    }
                    @Override public Preferences getHintPreferences(HintMetadata hint) {
                        return delegate.getHintPreferences(hint);
                    }
                    @Override public Severity getSeverity(HintMetadata hint) {
                        return Severity.VERIFIER;
                    }
                    @Override public void setSeverity(HintMetadata hint, Severity severity) {
                        throw new UnsupportedOperationException("Not supported.");
                    }
                };

                final Lookup normalizedLookup = Analyzer.normalizeLookup(context);
                
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        Analyzer.process(normalizedLookup != null ? normalizedLookup : Lookup.EMPTY, hintsSettings);
                    }
                });
            }
            
        });
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new AnalyzeFolder(actionContext);
    }
    
    public static final class ToolsAction extends SystemAction implements ContextAwareAction {

        private Action delegate;
        
        public ToolsAction() {
            delegate = new AnalyzeFolder();
            putValue("noIconInMenu", Boolean.TRUE);
        }

        @Override
        public boolean isEnabled() {
            return delegate.isEnabled();
        }

        public void actionPerformed(ActionEvent e) {
            delegate.actionPerformed(e);
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            return new AnalyzeFolder(actionContext);
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(AnalyzeFolder.class, "CTL_AnalyzeFolder");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
        
    }

}
