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
