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

package org.netbeans.modules.options.indentation;

import java.awt.Dimension;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.editor.spi.PreviewProvider;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class IndentationPanelController implements PreferencesCustomizer, PreviewProvider {

    private static final Logger LOG = Logger.getLogger(IndentationPanelController.class.getName());
    
    public IndentationPanelController(Preferences prefs) {
        this(MimePath.EMPTY, null, prefs, null, null);
    }
    
    public IndentationPanelController(MimePath mimePath, CustomizerSelector.PreferencesFactory prefsFactory, Preferences prefs, Preferences allLangPrefs, PreferencesCustomizer delegate) {
        assert mimePath != null;
        assert prefs != null;
        assert (allLangPrefs == null && delegate == null) || (allLangPrefs != null && delegate != null);
        assert delegate == null || delegate instanceof PreviewProvider;
        this.mimePath = mimePath;
        this.prefsFactory = prefsFactory;
        this.preferences = prefs;
        this.allLanguagesPreferences = allLangPrefs;
        this.delegate = delegate;
    }

    // ------------------------------------------------------------------------
    // PreviewProvider implementtaion
    // ------------------------------------------------------------------------

    public JComponent getComponent() {
        if (indentationPanel == null) {
            if (delegate != null) {
                indentationPanel = new JPanel();
                indentationPanel.setLayout(new BoxLayout(indentationPanel, BoxLayout.Y_AXIS));

                // initialize the delegate's component first
                JComponent delegateComp = delegate.getComponent();
                indentationPanel.setName(delegateComp.getName());

                // then create and initialize IndentationPanel
                indentationPanel.add(new IndentationPanel(mimePath, prefsFactory, preferences, allLanguagesPreferences, (PreviewProvider) delegate));
                indentationPanel.add(delegateComp);

                JPanel spacer = new JPanel();
                spacer.setPreferredSize(new Dimension(10, Integer.MAX_VALUE));
                indentationPanel.add(spacer);
            } else {
                indentationPanel = new IndentationPanel(mimePath, prefsFactory, preferences, null, null);
            }
        }
        return indentationPanel;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(IndentationPanelController.class, "indentation-customizer-display-name"); //NOI18N
    }

    public String getId() {
        return PreferencesCustomizer.TABS_AND_INDENTS_ID;
    }

    public HelpCtx getHelpCtx () {
        HelpCtx ctx = null;

        if (delegate != null) {
            ctx = delegate.getHelpCtx();
        }

        return ctx != null ? ctx : new HelpCtx ("netbeans.optionsDialog.editor.identation"); //NOI18N
    }
    
    // ------------------------------------------------------------------------
    // PreviewProvider implementtaion
    // ------------------------------------------------------------------------

    public JComponent getPreviewComponent() {
        if (delegate != null) {
            return ((PreviewProvider) delegate).getPreviewComponent();
        } else {
            return getIndentationPanel().getPreviewProvider().getPreviewComponent();
        }
    }

    public void refreshPreview() {
        // XXX: this is a workaround for the new view hierarchy, normally we
        // should not catch any exception here and just call refreshPreview().
        try {
            if (delegate != null) {
                ((PreviewProvider) delegate).refreshPreview();
            } else {
                getIndentationPanel().scheduleRefresh();
            }
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            // ignore
        }
    }

    // ------------------------------------------------------------------------
    // private implementtaion
    // ------------------------------------------------------------------------

    private final MimePath mimePath;
    private final CustomizerSelector.PreferencesFactory prefsFactory;
    private final Preferences allLanguagesPreferences;
    private final Preferences preferences;
    private final PreferencesCustomizer delegate;

    private JComponent indentationPanel;
    
    private IndentationPanel getIndentationPanel() {
        assert indentationPanel instanceof IndentationPanel;
        return (IndentationPanel) indentationPanel;
    }
}
