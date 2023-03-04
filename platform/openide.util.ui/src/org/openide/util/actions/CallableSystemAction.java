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

package org.openide.util.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.logging.Logger;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;

/** Not preferred anymore, use <a href="@org-openide-awt@/org/openide/awt/Actions.html#alwaysEnabled-java.awt.event.ActionListener-java.lang.String-java.lang.String-boolean-">Actions.alwaysEnabled</a>
* instead. To migrate your
* <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">
* layer definition</a> use:
* <pre>
* &lt;file name="your-pkg-action-id.instance"&gt;
*   &lt;attr name="instanceCreate" methodvalue="org.openide.awt.Actions.alwaysEnabled"/&gt;
*   &lt;attr name="delegate" methodvalue="your.pkg.YourAction.factoryMethod"/&gt;
*   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
*   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
*   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="false"/&gt; --&gt;
* &lt;/file&gt;
* </pre>
*
* @author   Ian Formanek, Jaroslav Tulach, Jan Jancura, Petr Hamernik
*/
public abstract class CallableSystemAction extends SystemAction implements Presenter.Menu, Presenter.Popup,
    Presenter.Toolbar {
    /** serialVersionUID */
    static final long serialVersionUID = 2339794599168944156L;

    // ASYNCHRONICITY
    // Adapted from org.netbeans.core.ModuleActions by jglick

    /**
     * Set of action classes for which we have already issued a warning that
     * {@link #asynchronous} was not overridden to return false.
     */
    private static final Set<Class> warnedAsynchronousActions = new WeakSet<Class>(); 
    private static final boolean DEFAULT_ASYNCH = !Boolean.getBoolean(
            "org.openide.util.actions.CallableSystemAction.synchronousByDefault"
        );

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a MenuBar.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getMenuPresenter() {
        return org.openide.util.actions.ActionPresenterProvider.getDefault().createMenuPresenter(this);
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a Popup Menu.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getPopupPresenter() {
        return org.openide.util.actions.ActionPresenterProvider.getDefault().createPopupPresenter(this);
    }

    /* Returns a Component that presents the Action, that implements this
    * interface, in a ToolBar.
    * @return the Component representation for the Action
    */
    public java.awt.Component getToolbarPresenter() {
        return org.openide.util.actions.ActionPresenterProvider.getDefault().createToolbarPresenter(this);
    }

    /** Actually perform the action.
    * This is the method which should be called programmatically.
    * Presenters in <a href="@org-openide-awt@/org/openide/awt/Actions.html">Actions</a> use this.
    * <p>See {@link SystemAction#actionPerformed} for a note on
    * threading usage: in particular, do not access GUI components
    * without explicitly asking for the AWT event thread!
    */
    public abstract void performAction();

    /* Implementation of method of javax.swing.Action interface.
    * Delegates the execution to performAction method.
    *
    * @param ev the action event
    */
    public void actionPerformed(ActionEvent ev) {
        if (isEnabled()) {
            org.openide.util.actions.ActionInvoker.invokeAction(
                this, ev, asynchronous(), new Runnable() {
                    public void run() {
                        performAction();
                    }
                }
            );
        } else {
            // Should not normally happen.
            Utilities.disabledActionBeep();
        }
    }

    /**
     * If true, this action should be performed asynchronously in a private thread.
     * If false, it will be performed synchronously as called in the event thread.
     * <p>The default value is true for compatibility reasons; subclasses are strongly
     * encouraged to override it to be false, and to either do their work promptly
     * in the event thread and return, or to somehow do work asynchronously (for example
     * using {@link org.openide.util.RequestProcessor#getDefault}).
     * <p class="nonnormative">You may currently set the global default to false
     * by setting the system property
     * <code>org.openide.util.actions.CallableSystemAction.synchronousByDefault</code>
     * to <code>true</code>.</p>
     * <p class="nonnormative">When true, the current implementation also provides for a wait cursor during
     * the execution of the action. Subclasses which override to return false should
     * consider directly providing a wait or busy cursor if the nature of the action
     * merits it.</p>
     * @return true if this action should automatically be performed asynchronously
     * @since 4.11
     */
    protected boolean asynchronous() {
        if (warnedAsynchronousActions.add(getClass())) {
            Logger.getLogger(CallableSystemAction.class.getName()).warning(
                "Warning - " + getClass().getName() +
                " should override CallableSystemAction.asynchronous() to return false"
            );
        }

        return DEFAULT_ASYNCH;
    }
}
