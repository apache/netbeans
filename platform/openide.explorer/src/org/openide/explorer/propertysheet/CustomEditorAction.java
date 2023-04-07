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

package org.openide.explorer.propertysheet;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.nodes.Node.*;
import org.openide.util.NbBundle;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.beans.*;

import java.lang.ref.WeakReference;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;


/** Action to invoke the custom editor.
 *
 * @author  Tim Boudreau
 */
class CustomEditorAction extends AbstractAction {
    private Invoker invoker;
    private WeakReference<PropertyModel> modelRef = null;

    /** Creates a new instance of CustomEditorAction */
    public CustomEditorAction(Invoker invoker) {
        this.invoker = invoker;
        putValue(SMALL_ICON, PropUtils.getCustomButtonIcon());
    }

    public CustomEditorAction(Invoker invoker, PropertyModel mdl) {
        this(invoker);

        if (mdl != null) {
            //            System.err.println("Creating custom editor action for model " + mdl);
            modelRef = new WeakReference<PropertyModel>(mdl);
        }
    }

    public void actionPerformed(ActionEvent ae) {
        if (PropUtils.isLoggable(CustomEditorAction.class)) {
            PropUtils.log(CustomEditorAction.class, "CustomEditorAction invoked " + ae); //NOI18N
        }

        if (!invoker.allowInvoke()) {
            if (PropUtils.isLoggable(CustomEditorAction.class)) {
                PropUtils.log(
                    CustomEditorAction.class,
                    "Invoker (" + invoker.getClass() + " allowInvoke() returned false.  Aborting."
                ); //NOI18N
            }

            return;
        }

        PropertyModel refd = (modelRef != null) ? modelRef.get() : null;

        //get the feature descriptor in question
        FeatureDescriptor fd = invoker.getSelection();

        final Property p = (fd instanceof Property) ? (Property) fd : null;

        //if it's not a property...
        if (p == null) {
            if (PropUtils.isLoggable(CustomEditorAction.class)) {
                PropUtils.log(
                    CustomEditorAction.class,
                    "Cant invoke custom " + "editor on " + fd + " it is null or not a Property." + "Aborting."
                ); //NOI18N
            }

            //Somebody invoked it from the keyboard on an expandable set
            Utilities.disabledActionBeep();

            return;
        }

        final java.beans.PropertyEditor editor = PropUtils.getPropertyEditor(p);

        //Create a new PropertyEnv to carry the values of the Property to the editor
        PropertyEnv env = null;

        if (editor instanceof ExPropertyEditor) {
            if (PropUtils.isLoggable(CustomEditorAction.class)) {
                PropUtils.log(CustomEditorAction.class, "Editor is an " + "ExPropertyEditor, attaching a PropertyEnv"); //NOI18N
            }

            env = new PropertyEnv();
            env.setFeatureDescriptor(fd);

            if (invoker instanceof SheetTable) {
                if (PropUtils.isLoggable(CustomEditorAction.class)) {
                    PropUtils.log(
                        CustomEditorAction.class, "env.setBeans to " + invoker.getReusablePropertyEnv().getBeans()
                    ); //NOI18N
                }

                env.setBeans(invoker.getReusablePropertyEnv().getBeans());
            }

            //Set up the editor with any hints from the property
            ((ExPropertyEditor) editor).attachEnv(env);
        }

        //if there is no custom property editor...
        if (!editor.supportsCustomEditor()) {
            if (PropUtils.isLoggable(CustomEditorAction.class)) {
                PropUtils.log(
                    CustomEditorAction.class,
                    "Cant invoke custom " + "editor for editor " + editor + " - it returns false " +
                    "from supportsCustomEditor()."
                ); //NOI18N
            }

            //Somebody invoked it from the keyboard on an editor w/o custom editor
            Utilities.disabledActionBeep();

            return;
        }

        final Component curComp = invoker.getCursorChangeComponent();
        if (curComp != null) {
            Cursor cur = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            curComp.setCursor(cur);
        }
        try { //#64007 start - reset cursor in case of a runtime exception

        //            customEditing = true;
        Object partialValue = invoker.getPartialValue();

        //Okay, we can display a custom editor.
        //If the user has already typed something in a text field, pass it to the editor,
        //even if they haven't updated the value yet
        if (partialValue != null) {
            try {
                if ((editor.getValue() == null) // Fix #13339
                         ||!(partialValue.toString().equals(editor.getAsText()))) {
                    if (!(editor instanceof PropUtils.DifferentValuesEditor)) {
                        editor.setAsText(partialValue.toString());
                    }
                }
            } catch (ProxyNode.DifferentValuesException dve) {
                // old value will be set back
            } catch (Exception ite) {
                // old value will be set back
            }
        }

        //horrible, I need the nodes anyway
        final PropertyModel mdl = (refd == null) ? new NodePropertyModel(p, null) : refd;
        String fdName;

        if ((mdl instanceof ExPropertyModel && (((ExPropertyModel) mdl).getFeatureDescriptor() != null))) {
            fdName = ((ExPropertyModel) mdl).getFeatureDescriptor().getDisplayName();
        } else {
            fdName = null;
        }

        //Support hinting of the title
        String suppliedTitle = (String) p.getValue("title"); //NOI18N
        final String title = (suppliedTitle == null)
            ? ((fd.getDisplayName() == null)
            ? //XXX does this ever happen??
                NbBundle.getMessage(CustomEditorAction.class, "FMT_CUSTOM_DLG_NOPROPNAME_TITLE",
                fdName == null ? invoker.getBeanName() : fdName
            )
            : ((fd.getDisplayName().equals(invoker.getBeanName())) ? invoker.getBeanName() : 
                NbBundle.getMessage(CustomEditorAction.class, "FMT_CUSTOM_DLG_TITLE",
                invoker.getBeanName(), fd.getDisplayName())
            )) : suppliedTitle; //NOI18N

        final PropertyDialogManager pdm = new PropertyDialogManager(
                NbBundle.getMessage(
                    CustomEditorAction.class, "PS_EditorTitle", //NOI18N
                    (title == null) ? "" : title, // NOI18N
                    p.getValueType()
                ), true, editor, mdl, env
            );

        boolean shouldListen = !(pdm.getComponent() instanceof EnhancedCustomPropertyEditor) &&
            (p.canWrite() && (invoker.wantAllChanges() || ((env == null) || env.isChangeImmediate())));

        final PropertyChangeListener pcl = (!shouldListen) ? null
                                                           : (new PropertyChangeListener() {
                    private boolean updating = false;

                    public void propertyChange(PropertyChangeEvent pce) {
                        if (updating) {
                            return;
                        }

                        updating = true;

                        try {
                            boolean success = PropUtils.updateProp(mdl, editor, title);

                            if (success) {
                                invoker.valueChanged(editor);
                            } else if( !pdm.wasCancelled() ) { //don't bother if custom editor was cancelled
                                invoker.failed();
                            }
                        } finally {
                            updating = false;
                        }
                    }
                });

        if (pcl != null) {
            editor.addPropertyChangeListener(pcl);
        }

        final java.awt.Window w = pdm.getDialog();

        WindowListener wl = new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (pdm.getComponent() instanceof EnhancedCustomPropertyEditor) {
                        if (!pdm.wasCancelled() && !closedOption && pdm.wasOK() && !pdm.wasReset()) {
                            try {
                                invoker.valueChanged(pdm.getEditor());
                            } catch (Exception ex) {
                                //do nothing
                            }
                        }
                    }

                    invoker.editorClosed();
                    w.removeWindowListener(this);

                    if (pcl != null) {
                        editor.removePropertyChangeListener(pcl);
                    }

                    //                        customEditing=false;
                }

                @Override
                public void windowOpened(WindowEvent e) {
                    invoker.editorOpened();
                    if (curComp != null) {
                        curComp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }

                // MCF ISSUE 44366
                @Override
                public void windowClosing(WindowEvent ev) {
                    if (PropUtils.isLoggable(CustomEditorAction.class)) {
                        PropUtils.log(CustomEditorAction.class, "CustomerEditorAction windowClosing event");
                    }

                    closedOption = true;
                }

                //  MCF ISSUE 44366 
                boolean closedOption = false;
            };

        //Don't set customEditing for non-dialog custom property editors - another
        //editor can be opened at the same time
        if (w instanceof JDialog) {
            JDialog jd = (JDialog) w;
            jd.getAccessibleContext().setAccessibleName(title);

            if (fd.getShortDescription() != null) {
                jd.getAccessibleContext().setAccessibleDescription(fd.getShortDescription());
            }

            w.addWindowListener(wl);
        } else if (w instanceof Frame) {
            ((Frame) w).addWindowListener(wl);
        }

        invoker.editorOpening();

        try {
            PropUtils.addExternallyEdited(p);
            w.setVisible(true);
            PropUtils.removeExternallyEdited(p);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        
        } finally { //#64007 end - reset cursor in case of a runtime exception
            if (curComp != null) {
                curComp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    static interface Invoker {
        /** Get the selected object that should be edited  */
        public FeatureDescriptor getSelection();

        /** If the value displayed has been edited but not committed, get
         * the partial edit to set in the custom editor */
        public Object getPartialValue();

        /** Get the component which is invoking the custom editor, and
         * should have its cursor changed while the dialog is being opened
         * (the cursor change will actually happen on its ancestor root pane)*/
        public Component getCursorChangeComponent();

        /** Get the name of the bean, if any, that owns the property - this
         * value is used as part of the window title */
        public String getBeanName();

        /** Callback in case the invoker wants to do something before the
         * window is opened */
        public void editorOpening();

        /** Callback to notify the invoker that the window has opened */
        public void editorOpened();

        /** Callback to notify the invoker that the window has closed.  If
         * the invoker should behave differently in response to a failed
         * edit than in response to a successful one, set a flag to false
         * when editorOpening() is called, and set it to true if failed()
         * is called, and do your processing here.   */
        public void editorClosed();

        /** Called if the value is changed successfully */
        public void valueChanged(PropertyEditor editor);

        /** Allow an invoker to block invocation of the custom editor if it
         * is not in an appropriate state */
        public boolean allowInvoke();

        /** Called if an update was attempted but the value was illegal */
        public void failed();

        /** Should valueUpdated be called even if the editor is not ExPropertyEditor?
         * PropertyPanel will need this to update inline editors; the property
         * sheet will repaint anyway and doesn't. */
        public boolean wantAllChanges();

        public ReusablePropertyEnv getReusablePropertyEnv();
    }
}
