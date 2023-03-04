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


package org.netbeans.modules.i18n;


import java.awt.Dialog;
import java.io.IOException;
import java.util.concurrent.Future;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;

import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;


/**
 * Insert internationalized string at caret position (if it is not in guarded block).
 * <p>
 * Backported from <tt>prj40_prototype</tt> branch.
 *
 * @author   Petr Jiricka, Peter Zavadsky
 */
public class InsertI18nStringAction extends CookieAction {

    /** Generated serial version UID. */
    static final long serialVersionUID =-7002111874047983222L;

    // lifetime performAction:

    // Position where to insert the new i18n-string.
    private transient Position position;

    private transient I18nSupport support;

    private transient I18nPanel i18nPanel;

    private transient DataObject dataObject;

    public InsertI18nStringAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    /**
     * Open I18nPanel and grab user response then update Document.
     * @param activatedNodes currently activated nodes
     */
    protected void performAction (final Node[] activatedNodes) {
        try {
            final EditorCookie editorCookie = (activatedNodes[0]).getCookie(EditorCookie.class);
            if (editorCookie == null) {
                Util.debug(new IllegalArgumentException("Missing editor cookie!")); // NOI18N
                return;
            }

            // Set data object.
            dataObject = activatedNodes[0].getCookie(DataObject.class);
            if (dataObject == null) {
                Util.debug(new IllegalArgumentException("Missing DataObject!"));    // NOI18N
                return;
            }

            JEditorPane[] panes = editorCookie.getOpenedPanes();

            if (panes == null || panes.length == 0) {
                //??? should it be tools action at all, once launched
                // from node it may raise this exception or it inserts
                // string in latest caret position in possibly hidden source
                Util.debug(new IllegalArgumentException("Missing editor pane!"));   // NOI18N
                return;
            }

            // Set insert position.
            position = NbDocument.createPosition(panes[0].getDocument(), panes[0].getCaret().getDot(), Position.Bias.Backward);

            // If there is a i18n action in run on the same editor, cancel it.
            I18nManager.getDefault().cancel();

            try {
                showModalPanel();
            } catch(IOException ex) {
                String msg = "Document loading failure " + dataObject.getName();    // NOI18N
                Util.debug(msg, ex);
                return;
            }

            // Ensure caret is visible.
            panes[0].getCaret().setVisible(true);
        } catch (BadLocationException blex) {
            ErrorManager.getDefault().notify(blex);
        } finally {
            dataObject = null;
            support = null;
            i18nPanel = null;
            position = null;
        }
    }


    /**
     * Implementation
     */
    private void insertI18nString() {
        try {
            I18nString i18nString = i18nPanel.getI18nString();

            if(i18nString.key == null || "".equals(i18nString.key)) {
                return;
            }

            // Try to add key to bundle.
            support.getResourceHolder().addProperty(
                i18nString.getKey(),
                i18nString.getValue(),
                i18nString.getComment()
            );

            // Create field if necessary.
            // PENDING, should not be performed here -> capability moves to i18n wizard.
            if(support.hasAdditionalCustomizer())
                support.performAdditionalChanges();

            // Replace string.
            String code = i18nString.getReplaceString();
            support.getDocument().insertString(position.getOffset(), code, null);

        } catch (IllegalStateException e) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                I18nUtil.getBundle().getString("EXC_BadKey"),
                NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
        } catch (BadLocationException e) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(
                    I18nUtil.getBundle().getString("MSG_CantInsertInGuarded"),
                    NotifyDescriptor.INFORMATION_MESSAGE
                )
            );
        }
    }


    /**
     * Create panel used for specifying i18n string.
     */
    private JPanel createPanel() throws IOException {
        I18nSupport.Factory factory = FactoryRegistry.getFactory(dataObject.getClass());

        if(factory == null)
            throw new IllegalStateException("I18N: No factory registered for data object type="+dataObject.getClass().getName()); // NOI18N

        support = factory.create(dataObject);

        //If you decide for caching impl it must be invalidated on
        //dataobject and document instabce change and update properties keys regularly.

        i18nPanel = new I18nPanel(support.getPropertyPanel(), false, Util.getProjectFor(dataObject), dataObject.getPrimaryFile());
        i18nPanel.setI18nString(support.getDefaultI18nString());
        i18nPanel.setDefaultResource(dataObject);

        return i18nPanel;
    }


    /**
     * Basically I18nPanel wrapped by Ok, Cancel and Help buttons shown.
     * Handles OK button.
     */
    private void showModalPanel() throws IOException {
        DialogDescriptor dd = new DialogDescriptor(
            createPanel(),
            Util.getString("CTL_InsertI18nDialogTitle"),
            true,
            NotifyDescriptor.OK_CANCEL_OPTION,
            NotifyDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN,
            new HelpCtx(InsertI18nStringAction.class),
            null
        );
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() == NotifyDescriptor.OK_OPTION) {
            insertI18nString();
        }
    }


    /** Overrides superclass method. Adds additional test if i18n module has registered factory
     * for this data object to be able to perform i18n action. */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes)) return false;

        // if has an open editor pane must not be in a guarded block
        // PENDING>>
        // It causes StackOverflowError
        // I18nSupport.isGuardedPosittion() checks teh way it causes change cookies (remove add SaveCookie), what
        // in turn calls back enable method, it calls isGuardedPosition again etc. etc.
        /*final SourceCookie.Editor sec = (SourceCookie.Editor)(activatedNodes[0]).getCookie(SourceCookie.Editor.class);
        if (sec != null) {
            JEditorPane[] edits = sec.getOpenedPanes();
            if (edits != null && edits.length > 0) {
                int position = edits[0].getCaret().getDot();
                StyledDocument doc = sec.getDocument();
                DataObject obj = (DataObject)sec.getSource().getCookie(DataObject.class);
                if(I18nSupport.getI18nSupport(doc, obj).isGuardedPosition(position))
                    return false;
            }
        }*/
        // PENDING<<

        DataObject dataObject = activatedNodes[0].getCookie(DataObject.class);

        if (dataObject == null) return false;

        if (FactoryRegistry.hasFactory(dataObject.getClass()) == false) return false;

        EditorCookie sec = (activatedNodes[0]).getCookie(EditorCookie.class);
        if (sec == null || !hasOpenedPane(sec)) return false;
        

        Future<Project[]> openProjects = OpenProjects.getDefault().openProjects();
        if(!openProjects.isDone()) {
            return false;
        }
	// check that the node has project
	return FileOwnerQuery.getOwner(dataObject.getPrimaryFile()) != null;
    }

    /**
     * Checks whether at least one opened editor pane exists.
     * @param ec the {@code EditorCookie} associated to the editor pane.
     * @return (@code true} if the editor pane associated with the specified 
     * {@code EditorCookie} exists, otherwise (@code false}.
     * @see Bug 188430
     */
    private boolean hasOpenedPane(EditorCookie ec) {
        JEditorPane pane = NbDocument.findRecentEditorPane(ec);
        return pane != null;
    }

    /** Implements superclass abstract method.
     * @return MODE_EXACTLY_ONE.
     */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /** Implemenst superclass abstract method.
     * @return <code>EditorCookie<code>.class
     * #see org.openide.cookies.EditorCookie */
    protected Class[] cookieClasses () {
        return new Class [] {
            EditorCookie.class,
        };
    }

    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return I18nUtil.getBundle().getString("CTL_InsertI18nString");
    }

    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(I18nUtil.HELP_ID_MANINSERT);
    }

    @Override
    protected boolean asynchronous() {
      return false;
    }
}
