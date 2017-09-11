/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nbform;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.form.FormDesigner;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormLoaderSettings;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.assistant.AssistantModel;
import org.netbeans.modules.form.assistant.AssistantView;
import org.openide.actions.FileSystemAction;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

@MultiViewElement.Registration(
    displayName="#CTL_DesignTabCaption",
    iconBase=FormEditorSupport.iconURL,
    persistenceType=TopComponent.PERSISTENCE_NEVER,
    preferredID=FormEditorSupport.MV_FORM_ID,
    mimeType="text/x-form",
    position=2000
)
public class FormDesignerTC extends TopComponent implements MultiViewElement {

    private FormEditorSupport formEditorSupport;
    private FormDesigner formDesigner;
    private PropertyChangeListener designerListener;

    private FormDesignerLookup lookup;

    private AssistantView assistantView;
    private PreferenceChangeListener settingsListener;

    private MultiViewElementCallback multiViewObserver;

    private PreLoadTask preLoadTask;

    private static String iconURL =
        "org/netbeans/modules/form/resources/formDesigner.gif"; // NOI18N

    public FormDesignerTC(Lookup lkp) {
        this(lkp.lookup(FormEditorSupport.class));
    }
    
    FormDesignerTC(FormEditorSupport formEditorSupport) {
        this.formEditorSupport = formEditorSupport;
        lookup = new FormDesignerLookup();
        createDesigner();
        associateLookup(lookup);
        setIcon(ImageUtilities.loadImage(iconURL));
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(10, 10));
    }

    private void createDesigner() {
        formDesigner = new FormDesigner(formEditorSupport.getFormEditor(true));
        if (designerListener == null) {
            designerListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (FormDesigner.PROP_TOP_DESIGN_COMPONENT.equals(evt.getPropertyName())) {
                        formEditorSupport.updateMVTCDisplayName();
                    }
                }
            };
        }
        formDesigner.addPropertyChangeListener(designerListener);
        lookup.setLookupFromDesigner(formDesigner);
    }

    @Override
    protected String preferredID() {
        return formEditorSupport.getFormDataObject().getName();
    }

    // only MultiViewDescriptor is stored, not MultiViewElement
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.formeditor"); // NOI18N
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        closeDesigner();
    }

    private FormEditorSupport closeDesigner() {
        if (formDesigner != null) {
            formDesigner.removePropertyChangeListener(designerListener);
            formDesigner.close();
        }
        removeAll();
        if (preLoadTask != null) { // designer closing before form loading started
            StatusDisplayer.getDefault().setStatusText(""); // NOI18N
        }
        if (settingsListener != null) {
            FormLoaderSettings.getPreferences().removePreferenceChangeListener(settingsListener);
            settingsListener = null;
        }
        assistantView = null;
        formDesigner = null;
        FormEditorSupport fes = formEditorSupport;
        formEditorSupport = null;
        return fes;
    }

    void resetDesigner(boolean reload) {
        boolean selected;
        if (isShowing()) {
            selected = (FormDesigner.getSelectedDesigner() == formDesigner);
        } else {
            selected = false;
            reload = false;
        }

        JToolBar toolbar = formDesigner != null ? formDesigner.getToolBar() : null;
        formEditorSupport = closeDesigner();
        createDesigner();
        if (toolbar != null) { // need to reuse the previous toolbar component (toolbar of MultiViewElement)
            formDesigner.setToolBar(toolbar);
        }

        if (selected) {
            TopComponent activeTC = TopComponent.getRegistry().getActivated();
            if (activeTC == this || (activeTC != null && activeTC.isAncestorOf(this))) {
                formDesigner.componentActivated();
            } else {
                FormDesigner.setSelectedDesigner(formDesigner, true);
            }
        }
        if (reload) {
            componentShowing();
        }
    }

    @Override
    public void componentShowing() { // TODO also used as API to trigger loading or init after reload...
        formDesigner.componentShowing();

        if (!isAncestorOf(formDesigner.getDesignCanvas())) { // designer not yet displayed
            guiInit();
        }

        if (formEditorSupport.isOpened()) {
            FormEditorSupport.checkFormGroupVisibility();
        } else {
            // Form loading phase 1/3.
            if (preLoadTask == null && formEditorSupport.startFormLoading()) {
                preLoadTask = new PreLoadTask(formEditorSupport.getFormEditor());
                FormUtils.getRequestProcessor().post(preLoadTask);
                StatusDisplayer.getDefault().setStatusText(
                    FormUtils.getFormattedBundleString(
                        "FMT_PreparingForm", // NOI18N
                        new Object[] { formEditorSupport.getFormDataObject().getName() }));
            }
        }
    }

    private void guiInit() {
        removeAll();
        JScrollPane scrollPane = new JScrollPane(formDesigner.getDesignCanvas());
        scrollPane.setBorder(null); // disable border, winsys will handle borders itself
        scrollPane.setViewportBorder(null); // disable also GTK L&F viewport border 
        scrollPane.getVerticalScrollBar().setUnitIncrement(5); // Issue 50054
        scrollPane.getHorizontalScrollBar().setUnitIncrement(5);
        add(scrollPane); // if not yet loaded, the canvas shows "Loading"
        setupAssistant(); // if not yet loaded, a blank placeholder panel is used
    }

    @Override
    public UndoRedo getUndoRedo() {
        UndoRedo.Provider provider = getLookup().lookup(UndoRedo.Provider.class);
        return (provider == null) ? UndoRedo.NONE : provider.getUndoRedo();
    }

    private class PreLoadTask implements Runnable {
        private FormEditor formEditor;
        PreLoadTask(FormEditor formEditor) {
            this.formEditor = formEditor;
        }
        @Override
        public void run() {
            long ms = System.currentTimeMillis();
            // Form loading phase 2/3.
            formEditor.loadOnBackground();
            Logger.getLogger(FormEditor.class.getName()).log(Level.FINER, "Opening form time 2: {0}ms", (System.currentTimeMillis()-ms)); // NOI18N

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    preLoadTask = null; // set back to null in EDT
                    if (formEditorSupport != null) {
                        loadForm();
                    }
                }
            });
        }
    }

    private void loadForm() {
        assert !formEditorSupport.isOpened();
        long ms = System.currentTimeMillis();
        // Form loading phase 3/3.
        if (formEditorSupport.loadOpeningForm()) {
            FormEditorSupport.checkFormGroupVisibility();
            formDesigner.loadingComplete();
            guiInit();

            Logger.getLogger(FormEditor.class.getName()).log(Level.FINER, "Opening form time 3: {0}ms", (System.currentTimeMillis()-ms)); // NOI18N
        }
    }

    @Override
    public void componentHidden() {
        if (formDesigner != null) {
            formDesigner.componentHidden();
        }
        FormEditorSupport.checkFormGroupVisibility();
    }

    @Override
    public void componentActivated() {
        formDesigner.componentActivated();
    }

    @Override
    public void componentDeactivated() {
        if (formDesigner != null) {
            formDesigner.componentDeactivated();
        }
    }

    @Override
    public void requestVisible() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }
    }

    @Override
    public void requestActive() {
        if (multiViewObserver != null) {
            multiViewObserver.requestActive();
        } else {
            super.requestActive();
        }
    }

    @Override
    public CloseOperationState canCloseElement() {
        return formEditorSupport.canCloseElement(multiViewObserver.getTopComponent());
    }

    @Override
    public Action[] getActions() {
        Action[] actions = super.getActions();
        SystemAction fsAction = SystemAction.get(FileSystemAction.class);
        if (!Arrays.asList(actions).contains(fsAction)) {
            Action[] newActions = new Action[actions.length+1];
            System.arraycopy(actions, 0, newActions, 0, actions.length);
            newActions[actions.length] = fsAction;
            actions = newActions;
        }
        return actions;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return formDesigner.getToolBar();
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewObserver = callback;

        // add FormDesigner as a client property so it can be obtained
        // from multiview TopComponent (it is not sufficient to put
        // it into lookup - only content of the lookup of the active
        // element is accessible)
        callback.getTopComponent().putClientProperty("formDesigner", this); // NOI18N

        // needed for deserialization...
        if (formEditorSupport != null) {
            // this is used (or misused?) to obtain the deserialized multiview
            // topcomponent and set it to FormEditorSupport
            formEditorSupport.setTopComponent(callback.getTopComponent());
        }
    }

    private void setupAssistant() {
        if (formEditorSupport.isOpened()) {
            updateAssistant();
            settingsListener = new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    if (FormLoaderSettings.PROP_ASSISTANT_SHOWN.equals(evt.getKey())) {
                        updateAssistant();
                    }
                }
            };
            FormLoaderSettings.getPreferences().addPreferenceChangeListener(settingsListener);
        } else if (FormLoaderSettings.getInstance().getAssistantShown()) {
            // only placeholder space during loading
            Component c = new JPanel();
            c.setPreferredSize(new Dimension(10, 41));
            c.setBackground(FormLoaderSettings.getInstance().getFormDesignerBackgroundColor());
            add(c, BorderLayout.NORTH);
        }
    }

    private void updateAssistant() {
        if (FormLoaderSettings.getInstance().getAssistantShown()) {
            AssistantModel assistant = FormEditor.getAssistantModel(formDesigner.getFormModel());
            assistantView = new AssistantView(assistant);
            assistant.setContext("select"); // NOI18N
            add(assistantView, BorderLayout.NORTH);
        } else if (assistantView != null) {
            remove(assistantView);
            assistantView = null;
        }
        revalidate();
        repaint();
    }

    private static class FormDesignerLookup extends ProxyLookup {
        void setLookupFromDesigner(FormDesigner designer) {
            setLookups(designer.getLookup());
        }
    }
}
