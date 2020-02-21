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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.options.editor.spi.PreviewProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class EditorOptionsPanelController extends OptionsPanelController implements PreviewProvider {

    private JEditorPane previewPane;
    private final EditorPropertySheet panel;
    private final CodeStyle.Language language;
    private static final boolean TRACE = false;
    private static final PreviewPreferencesModel preferencesModel = new PreviewPreferencesModel();
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed = false;
    private final PreviewPreferencesModel.Filter filter;

    
    public EditorOptionsPanelController(CodeStyle.Language language, PreviewPreferencesModel.Filter filter){
        if (TRACE) {System.out.println("EditorOptionsPanelController.ctor()");} // NOI18N
        this.language = language;
        this.filter = filter;
        this.panel = new EditorPropertySheet(this, language, preferencesModel, filter);
    }

    @Override
    public void update() {
        if (TRACE) {System.out.println("EditorOptionsPanelController.update()");} // NOI18N
        panel.load();
        changed = false;
    }
    
    @Override
    public void applyChanges() {
        if (TRACE) {System.out.println("EditorOptionsPanelController.applyChanges()");} // NOI18N
        panel.store();
        changed = false;
    }
    
    @Override
    public void cancel() {
        if (TRACE) {System.out.println("EditorOptionsPanelController.cancel()");} // NOI18N
        panel.cancel();
        changed = false;
    }
    
    @Override
    public boolean isValid() {
        if (TRACE) {System.out.println("EditorOptionsPanelController.isValid()");} // NOI18N
        return true;
    }
    
    @Override
    public boolean isChanged() {
        if (TRACE) {System.out.println("EditorOptionsPanelController.isChanged()");} // NOI18N
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("netbeans.optionsDialog.advanced.formEditor"); // NOI18N
    }
    
    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (TRACE) {System.out.println("EditorOptionsPanelController.getComponent()");} // NOI18N
        return panel;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
        
    void changed(boolean isChanged) {
        if (!changed) {
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        changed = isChanged;
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    @Override
    public JComponent getPreviewComponent() {
        if (previewPane == null) {
            previewPane = new JEditorPane();
            previewPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(EditorOptionsPanelController.class, "AN_Preview")); //NOI18N
            previewPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EditorOptionsPanelController.class, "AD_Preview")); //NOI18N
            previewPane.putClientProperty("HighlightsLayerIncludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.SyntaxHighlighting$"); //NOI18N
            previewPane.setEditorKit(CloneableEditorSupport.getEditorKit(language.toMime()));
            previewPane.setEditable(false);
        }
        return previewPane;
    }

    @Override
    public void refreshPreview() {
        panel.repaintPreview();
    }

    public static OptionsPanelController getCController() {
        return new EditorOptionsPanelController(CodeStyle.Language.C, PreviewPreferencesModel.Filter.All);
    }
    public static OptionsPanelController getCControllerTabs() {
        return new EditorOptionsPanelController(CodeStyle.Language.C, PreviewPreferencesModel.Filter.TabsAndIndents);
    }
    public static OptionsPanelController getCControllerAlignment() {
        return new EditorOptionsPanelController(CodeStyle.Language.C, PreviewPreferencesModel.Filter.Alignment);
    }
    public static OptionsPanelController getCControllerBraces() {
        return new EditorOptionsPanelController(CodeStyle.Language.C, PreviewPreferencesModel.Filter.Braces);
    }
    public static OptionsPanelController getCControllerBlank() {
        return new EditorOptionsPanelController(CodeStyle.Language.C, PreviewPreferencesModel.Filter.BlankLines);
    }
    public static OptionsPanelController getCControllerSpaces() {
        return new EditorOptionsPanelController(CodeStyle.Language.C, PreviewPreferencesModel.Filter.Spaces);
    }

    public static OptionsPanelController getCCController() {
        return new EditorOptionsPanelController(CodeStyle.Language.CPP, PreviewPreferencesModel.Filter.All);
    }
    public static OptionsPanelController getCCControllerTabs() {
        return new EditorOptionsPanelController(CodeStyle.Language.CPP, PreviewPreferencesModel.Filter.TabsAndIndents);
    }
    public static OptionsPanelController getCCControllerAlignment() {
        return new EditorOptionsPanelController(CodeStyle.Language.CPP, PreviewPreferencesModel.Filter.Alignment);
    }
    public static OptionsPanelController getCCControllerBraces() {
        return new EditorOptionsPanelController(CodeStyle.Language.CPP, PreviewPreferencesModel.Filter.Braces);
    }
    public static OptionsPanelController getCCControllerBlank() {
        return new EditorOptionsPanelController(CodeStyle.Language.CPP, PreviewPreferencesModel.Filter.BlankLines);
    }
    public static OptionsPanelController getCCControllerSpaces() {
        return new EditorOptionsPanelController(CodeStyle.Language.CPP, PreviewPreferencesModel.Filter.Spaces);
    }

    public static OptionsPanelController getHController() {
        return new EditorOptionsPanelController(CodeStyle.Language.HEADER, PreviewPreferencesModel.Filter.All);
    }
    public static OptionsPanelController getHControllerTabs() {
        return new EditorOptionsPanelController(CodeStyle.Language.HEADER, PreviewPreferencesModel.Filter.TabsAndIndents);
    }
    public static OptionsPanelController getHControllerAlignment() {
        return new EditorOptionsPanelController(CodeStyle.Language.HEADER, PreviewPreferencesModel.Filter.Alignment);
    }
    public static OptionsPanelController getHControllerBraces() {
        return new EditorOptionsPanelController(CodeStyle.Language.HEADER, PreviewPreferencesModel.Filter.Braces);
    }
    public static OptionsPanelController getHControllerBlank() {
        return new EditorOptionsPanelController(CodeStyle.Language.HEADER, PreviewPreferencesModel.Filter.BlankLines);
    }
    public static OptionsPanelController getHControllerSpaces() {
        return new EditorOptionsPanelController(CodeStyle.Language.HEADER, PreviewPreferencesModel.Filter.Spaces);
    }

}
