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

package org.netbeans.api.progress;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.modules.progress.spi.ExtractedProgressUIWorker;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.ProgressEvent;
import org.netbeans.modules.progress.spi.ProgressUIWorkerProvider;
import org.netbeans.modules.progress.spi.SwingController;
import org.netbeans.modules.progress.spi.UIInternalHandle;
import org.netbeans.progress.module.TrivialProgressUIWorkerProvider;
import org.netbeans.progress.module.UIInternalHandleAccessor;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 * Factory to create various ProgressHandle instances that allow long lasting
 * tasks to show their progress using various progress UIs. This class adds the 
 * ability to provide a custom Swing UI (JPanel) and manipulate with Swing components 
 * to the {@link org.netbeans.api.progress.ProgressHandle}.
 * @author Milos Kleint (mkleint@netbeans.org)
 * @author Svata Dedic
 */
public final class ProgressHandleFactory {

    /** Creates a new instance of ProgressIndicatorFactory */
    private ProgressHandleFactory() {
    }

    /**
     * Create a progress ui handle for a long lasting task.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @deprecated Use {@link ProgressHandle#createHandle(java.lang.String)}
     */
    @Deprecated
    public static ProgressHandle createHandle(String displayName) {
        return createHandle(displayName, (Action)null);
    }
    
     /**
      * Create a progress ui handle for a long lasting task.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @deprecated Use {@link ProgressHandle#createHandle(java.lang.String, org.openide.util.Cancellable)}.
     */
    @Deprecated
    public static ProgressHandle createHandle(String displayName, Cancellable allowToCancel) {
        return createHandle(displayName, allowToCancel, null);
    }

    /**
     * Create a progress ui handle for a long lasting task. Since {@code 1.59}, this method was
     * migrated to {@link ProgressHandle basic Progress API}.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @deprecated Please use {@link ProgressHandle#createHandle(java.lang.String, org.openide.util.Cancellable, javax.swing.Action)}
     */
    @Deprecated
    public static ProgressHandle createHandle(String displayName, Action linkOutput) {
        return createHandle(displayName, null, linkOutput);
    }
    
    /**
     * Create a progress UI handle for a long lasting task. Since {@code 1.59}, this method was
     * migrated to {@link ProgressHandle basic Progress API}. This implementation delegates to
     * {@link ProgressHandle#createHandle(java.lang.String, org.openide.util.Cancellable, javax.swing.Action)}
     * to enable smooth transition of older Progress API clients.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the appropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @deprecated Please use {@link ProgressHandle#createHandle(java.lang.String, org.openide.util.Cancellable, javax.swing.Action)}
     */
    @Deprecated
    public static ProgressHandle createHandle(String displayName, Cancellable allowToCancel, Action linkOutput) {
        return ProgressHandle.createHandle(displayName, allowToCancel, linkOutput);
    }
    
    /**
     * Create a progress UI handle for a long lasting task.
     * This call creates a Swing-based implementation. Use only when the Handle is directly used in
     * Swing UIs. These handles will never work in other presenters.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the appropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @since 1.59
     */
    public static ProgressHandle createUIHandle(String displayName, Cancellable allowToCancel, Action linkOutput) {
        return new UIInternalHandle(displayName, allowToCancel, true, linkOutput).createProgressHandle();
    }
    
    /**
     * Get the progress bar component for use in custom dialogs, the task won't 
     * show in the progress bar anymore.
     * @return the component to use in custom UI.
     */
    public static JComponent createProgressComponent(ProgressHandle handle) {
        return ihextract(handle).getProgressComponent();
    }

    /**
     * Get the task title component for use in custom dialogs, the task won't 
     * show in the progress bar anymore. The text of the label is changed by calls to <code>ProgressHandle.setDisplayName()</code> method
     * @return the component to use in custom UI.
     * @since org.netbeans.api.progress 1.8
     */
    public static JLabel createMainLabelComponent(ProgressHandle handle) {
        return ihextract(handle).getMainLabelComponent();
    }
    
    /**
     * Get the detail messages component for use in custom dialogs, the task won't 
     * show in the progress bar anymore. The text of the label is changed by calls to <code>ProgressHandle.progress(String)</code> method.
     * @return the component to use in custom UI.
     * @since org.netbeans.api.progress 1.8
     */
    public static JLabel createDetailLabelComponent(ProgressHandle handle) {
        return ihextract(handle).getDetailLabelComponent();
    }
    
    /**
     * Create a handle for a long lasting task that is not triggered by explicit user action.
     * Such tasks have lower priority in the UI.
     * Since {@code 1.59}, the functionality moves to basic {@link ProgressHandle Progress API}; this method is retained for smooth transition of older API clients.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @deprecated Use {@link ProgressHandle#createSystemHandle(java.lang.String, org.openide.util.Cancellable)}.
     */
    @Deprecated
    public static ProgressHandle createSystemHandle(String displayName) {
        return createSystemHandle(displayName, null, null);
    }

    /**
     * Create a cancelable handle for a task that is not triggered by explicit user action.
     * Such tasks have lower priority in the UI.
     * Since {@code 1.59}, the functionality moves to basic {@link ProgressHandle Progress API}; this method is retained
     * for smooth transition of older API clients.
     *
     * @param displayName to be shown in the progress UI
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @deprecated Use {@link ProgressHandle#createSystemHandle(java.lang.String, org.openide.util.Cancellable)}.
     */
    @Deprecated
    public static ProgressHandle createSystemHandle(String displayName, Cancellable allowToCancel) {
        return createSystemHandle(displayName, allowToCancel, null);
    }
    
    /**
     * Create a progress UI handle for a task that is not triggered by explicit user action.
     * Starting from {@code 1.59}, this method is fully superseded by {@link ProgressHandle#createSystemHandle(java.lang.String, org.openide.util.Cancellable, javax.swing.Action)}.
     * Since {@code 1.59}, the functionality moves to basic {@link ProgressHandle Progress API}; this method is retained for smooth transition of older API clients.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @deprecated Please use {@link ProgressHandle#createSystemHandle(java.lang.String, org.openide.util.Cancellable, javax.swing.Action)}
     */
    @Deprecated
    public static ProgressHandle createSystemHandle(String displayName, Cancellable allowToCancel, Action linkOutput) {
        return ProgressHandle.createSystemHandle(displayName, allowToCancel, linkOutput);
    }    

    /**
     * Create a progress UI handle for a task that is not triggered by explicit user action.
     * This call creates a Swing-based implementation. Use only when the Handle is directly used in
     * Swing UIs. These handles will never work in other presenters.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @since 1.59
     */
    public static ProgressHandle createSystemUIHandle(String displayName, Cancellable allowToCancel, Action linkOutput) {
        return new UIInternalHandle(displayName, allowToCancel, false, linkOutput).createProgressHandle();
    }    

    private static ProgressUIWorkerProvider TRIVIAL_PROVIDER = new TrivialProgressUIWorkerProvider();
    
    private static ExtractedProgressUIWorker ihextract(ProgressHandle h) {
        InternalHandle ih = h.getInternalHandle();
        if (ih instanceof UIInternalHandle) {
            return new UIHandleExtractor((UIInternalHandle)ih);
        } else {
            // fallback for non-UIInternalHandles. Let the environment to create
            // a suitable component representation.
            ProgressUIWorkerProvider prov = Lookup.getDefault().lookup(ProgressUIWorkerProvider.class);
            if (prov == null) {
                prov = TRIVIAL_PROVIDER;
            }
            ExtractedProgressUIWorker worker = prov.extractProgressWorker(ih);
            if (worker != null) {
                return new ForeignExtractor(ih, worker);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Creates UI components for InternalHandles that are not UI ones. Even though the
     * handle is not implemented in the standard way, SWing JComponents can be created for it;
     * the handle can be also marked as {@link InternalHandle#isCustomPlaced()}, so potential displayed
     * can check and remove it from the originally intended display.
     * <p>
     * This path is mainly used if an alternate ProgressHandle display (@link ProgressEnvironment} implementation) is 
     * in place, but GUI code still wants to embed Swing components for the handle in the UI.
     */
    private static class ForeignExtractor implements ExtractedProgressUIWorker {
        private final InternalHandle ih;
        private final ExtractedProgressUIWorker del;

        public ForeignExtractor(InternalHandle ih, ExtractedProgressUIWorker del) {
            this.del = del;
            this.ih = ih;
        }
        
        void customPlaced() {
            boolean wasCustomPlaced = ih.isCustomPlaced();
            UIInternalHandleAccessor acc = UIInternalHandleAccessor.instance();
            acc.markCustomPlaced(ih);
            if (!wasCustomPlaced) {
                acc.setController(ih, new SwingController(del));
            }
        }

        @Override
        public JComponent getProgressComponent() {
            customPlaced();
            return del.getProgressComponent();
        }

        @Override
        public JLabel getMainLabelComponent() {
            customPlaced();
            return del.getMainLabelComponent();
        }

        @Override
        public JLabel getDetailLabelComponent() {
            customPlaced();
            return del.getDetailLabelComponent();
        }

        @Override
        public void processProgressEvent(ProgressEvent event) {
            del.processProgressEvent(event);
        }

        @Override
        public void processSelectedProgressEvent(ProgressEvent event) {
            del.processSelectedProgressEvent(event);
        }
    }
    
    private static class UIHandleExtractor implements ExtractedProgressUIWorker {
        private final UIInternalHandle uiih;

        public UIHandleExtractor(UIInternalHandle uiih) {
            this.uiih = uiih;
        }
        
        @Override
        public JComponent getProgressComponent() {
            return uiih.extractComponent();
        }

        @Override
        public JLabel getMainLabelComponent() {
            return uiih.extractMainLabel();
        }

        @Override
        public JLabel getDetailLabelComponent() {
            return uiih.extractDetailLabel();
        }

        @Override
        public void processProgressEvent(ProgressEvent event) {
            throw new UnsupportedOperationException("Never called.");
        }

        @Override
        public void processSelectedProgressEvent(ProgressEvent event) {
            throw new UnsupportedOperationException("Never called.");
        }
    }
}
