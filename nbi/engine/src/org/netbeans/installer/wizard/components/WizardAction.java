/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.wizard.components;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.helper.NbiThread;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiProgressBar;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.progress.ProgressListener;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 * This class is a specialization of the {@link WizardComponent} which defines
 * behavior specific to actions.
 *
 * <p>
 * An action is best described by the following characteristics: it represents a
 * lengthy process (and hence displays a progress bar) and does not require any user
 * input - it just informs the user that something is happening and the wizard did
 * not hang.
 *
 * <p>
 * Optionally an action may provide means to cancel without waiting for it to
 * finish. This behavior is controlled by the {@link #isCancelable()} method.
 *
 * <p>
 * The derivative classes are expected to implement the {@link #execute()} and the
 * {@link #isCancelable()} methods. If the action is cancelable, then the code in
 * the {@link #execute()} method should check the cancellation status of the action
 * via {@link #isCanceled()}. The action will not be interrupted automatically -
 * canceling is a deliberate process.
 *
 * @author Kirill Sorokin
 * @since 1.0
 */
public abstract class WizardAction extends WizardComponent {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * UI of the action.
     */
    private WizardUi wizardUi;
    
    /**
     * Whether the action ahs finished execution.
     */
    private boolean finished;
    
    /**
     * Whether the action has been canceled. Note this field is simply a
     * recommendation to the code in {@link #execute()} that it should clean up and
     * return ASAP, it does not force any operation.
     */
    private boolean canceled;
    
    /**
     * Creates a new instance of {@link WizardAction}. This is the default
     * <code>protected</code> constructor which must be called by the concrete
     * implementations. It initializes the fields above.
     */
    protected WizardAction() {
        finished = false;
        canceled = false;
    }
    
    /**
     * Executes the action when it is read via a call to
     * {@link org.netbeans.installer.wizard.Wizard#next()}. This method runs the
     * {@link #execute()} method a new {@link NbiThread}.
     *
     * @see WizardComponent#executeForward()
     */
    public final void executeForward() {
        new NbiThread() {
            @Override
            public void run() {
                finished = false;
                execute();
                finished = true;
                
                if (!canceled) {
                    getWizard().next();
                }
            }
        }.start();
    }
    
    /**
     * This method has an empty implementation as {@link WizardAction} cannot be
     * executed when moving backward.
     *
     * @see WizardComponent#executeBackward()
     */
    public final void executeBackward() {
        // does nothing
    }
    
    /**
     * This method always returns <code>false</code>, as {@link WizardAction}s
     * cannot be executed when moving backward.
     *
     * @see WizardComponent#canExecuteBackward()
     */
    @Override
    public final boolean canExecuteBackward() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public WizardActionUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new WizardActionUi(this);
        }
        
        return (WizardActionUi) wizardUi;
    }
    
    /**
     * The default implementation of this method for {@link WizardAction} has an
     * empty body. Concrete implementations are expected to override this method
     * if they require any custom initialization.
     *
     * @see WizardComponent#initialize()
     */
    public void initialize() {
        // does nothing
    }
    
    /**
     * The main business-logic method of the action. It must be implemented by
     * concrete instances of {@link WizardAction}.
     *
     * <p>
     * The code in this method is expected to update the progress as it is being
     * executed. The {@link Progress} object for the action should be created by
     * this method and passed to the UI via the
     * {@link WizardActionUi#setProgress(Progress)} method.
     *
     * <p>
     * The implementing code is also expected to pay attention to the return value
     * of the {@link #isCanceled()} method. When the action receives a cancel signal
     * (if it supports cancelation) the return value of this method will change to
     * <code>true</code>.
     */
    public abstract void execute();
    
    /**
     * Whether this action can be canceled. The default value if <code>true</code>,
     * concrete implementations of {@link WizardAction} may override this method to
     * disable the possibility to cancel the action.
     *
     * @return <code>true</code> if the action can be canceled, <code>false</code>
     *      otherwise.
     */
    public boolean isCancelable() {
        return true;
    }
    
    /**
     * Whether this action has been canceled. This informational method is intended
     * to be called by the code in {@link WizardAction#execute()} in order to
     * correct its flow in case the action has been canceled.
     *
     * @return <code>true</code> is the action has been canceled, <code>false</code>
     *      otherwise.
     */
    public boolean isCanceled() {
        return canceled;
    }
    
    /**
     * Cancels the action. Note that this method does not explicitly "kill" the
     * execution of the action, but instead simply sets the cancellation marker and
     * waits till the action's execution finishes. In case of a not-very-well
     * behaved action, this can take a while.
     */
    public void cancel() {
        canceled = true;
        
        while (!finished) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                ErrorManager.notifyDebug(RESOURCE_INTERRUPTED_EXCEPTION, e);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    /**
     * Implementation of the {@link WizardUi} for {@link WizardAction}.
     * 
     * @author Kirill Sorokin
     * @since 1.0
     */
    public static class WizardActionUi extends WizardComponentUi
            implements ProgressListener {
        /**
         * Current {@link WizardAction} for this UI.
         */
        protected WizardAction action;
        
        /**
         * {@link Progress} object used by the action.
         */
        protected Progress progress;
        
        /**
         * Creates a new instance of {@link WizardActionUi}, initializing it with
         * the specified instance of {@link WizardAction}.
         *
         * @param action Instance of {@link WizardAction} which should be used
         *      by this UI.
         */
        public WizardActionUi(final WizardAction action) {
            super(action);
            
            
            this.action = action;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public SwingUi getSwingUi(final SwingContainer container) {
            if (swingUi == null) {
                swingUi = new WizardActionSwingUi(action, container);
            }
            
            return super.getSwingUi(container);
        }
        
        /**
         * Sets the current progress object for this action. It will be listened for
         * changes and the UI will be updated accordingly.
         *
         * @param progress Current {@link Progress} object for the action.
         */
        public void setProgress(final Progress progress) {
            if (this.progress != null) {
                this.progress.removeProgressListener(this);
            }
            
            this.progress = progress;
            this.progress.addProgressListener(this);
        }
        
        /**
         * This method is called when the progress updates. It performs the update
         * of all UIs that are created at the moment.
         *
         * @param progress {@link Progress} object which was updated.
         * @see ProgressListener#progressUpdated(Progress)
         */
        public void progressUpdated(final Progress progress) {
            if (swingUi != null) {
                ((WizardActionSwingUi) swingUi).progressUpdated(progress);
            }
        }
    }
    
    /**
     * Implementation of {@link SwingUi} for {@link WizardAction}.
     * 
     * @author Kirill Sorokin
     * @since 1.0
     */
    public static class WizardActionSwingUi extends WizardComponentSwingUi {
        /**
         * Current {@link WizardAction} for this UI.
         */
        private WizardAction action;
        
        /**
         * {@link NbiLabel} which represents the progress' title.
         */
        private NbiLabel titleLabel;
        
        /**
         * {@link NbiLabel} which represents the progress' detailed status.
         */
        private NbiLabel detailLabel;
        
        /**
         * {@link NbiProgressBar} which represents the progress' percentage.
         */
        private NbiProgressBar progressBar;
        
        /**
         * Creates a new instance of {@link WizardActionSwingUi}, initializing it
         * with the specified instances of {@link WizardAction} and
         * {@link SwingContainer}.
         *
         * @param action Instance of {@link WizardAction} which should be used
         *      by this UI.
         * @param container Instance of {@link SwingContainer} which should be used
         *      by this UI.
         */
        public WizardActionSwingUi(
                final WizardAction action,
                final SwingContainer container) {
            super(action, container);
            
            this.action = action;
            
            initComponents();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void initializeContainer() {
            super.initializeContainer();
            
            // set up the help button
            container.getHelpButton().setEnabled(false);
            container.getHelpButton().setVisible(false);
            
            // set up the back button
            container.getBackButton().setEnabled(false);
            container.getBackButton().setVisible(false);
            
            // set up the next (or finish) button
            container.getNextButton().setEnabled(false);
            container.getNextButton().setVisible(true);
            
            // set up the cancel button
            container.getCancelButton().setVisible(true);
            container.getCancelButton().setEnabled(action.isCancelable());
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void evaluateCancelButtonClick() {
            if (action.isCancelable()) {
                final String cancelDialogTitle = ResourceUtils.getString(
                        WizardAction.class,
                        RESOURCE_CANCEL_DIALOG_TITLE);
                final String canceldialogText = ResourceUtils.getString(
                        WizardAction.class,
                        RESOURCE_CANCEL_DIALOG_TEXT);
                
                if (!UiUtils.showYesNoDialog(cancelDialogTitle, canceldialogText)) {
                    return;
                }
                
                container.getCancelButton().setEnabled(false);
                titleLabel.setText(ResourceUtils.getString(
                        WizardAction.class,
                        RESOURCE_CANCELING_PROGRESS_TITLE));
                
                new NbiThread() {
                    public void run() {
                        ((WizardAction) action).cancel();
                        action.getWizard().getFinishHandler().cancel();
                    }
                }.start();
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public NbiButton getDefaultEnterButton() {
            return container.getCancelButton();
        }
        
        /**
         * This method is called from the corresponding {@link WizardActionUi} to
         * inform this class about the fact that the action's progress has been
         * updated. Thismethod updates the title, detail and progress bar components.
         *
         * @param progress {@link Progress} which has been updated.
         */
        public void progressUpdated(final Progress progress) {
            if (progress != null) {
                if (titleLabel != null) {
                    titleLabel.setText(progress.getTitle());
                }
                
                if (detailLabel != null) {
                    detailLabel.setText(progress.getDetail());
                }
                
                if (progressBar != null) {
                    progressBar.setValue(progress.getPercentage());
                }
            }
        }
        
        // private //////////////////////////////////////////////////////////////////
        /**
         * Initializes and lays out the swing components in this UI.
         */
        private void initComponents() {
            // titleLabel ///////////////////////////////////////////////////////////
            titleLabel = new NbiLabel();
            titleLabel.setFocusable(true);
            
            // progressBar //////////////////////////////////////////////////////////
            progressBar = new NbiProgressBar();
            
            // detailLabel //////////////////////////////////////////////////////////
            detailLabel = new NbiLabel(true);
            detailLabel.setFocusable(true);
            
            // this /////////////////////////////////////////////////////////////////
            add(titleLabel, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.SOUTH,         // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // ??? (padx, pady)
            add(progressBar, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.NORTH,         // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));                           // ??? (padx, pady)
            add(detailLabel, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));                           // ??? (padx, pady)
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_CANCEL_DIALOG_TITLE =
            "WA.cancel.dialog.title"; // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_CANCEL_DIALOG_TEXT =
            "WA.cancel.dialog.text"; // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_INTERRUPTED_EXCEPTION =
            "WA.error.interrupted.exception"; // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_CANCELING_PROGRESS_TITLE =
            "WA.canceling.progress.title"; // NOI18N
}
