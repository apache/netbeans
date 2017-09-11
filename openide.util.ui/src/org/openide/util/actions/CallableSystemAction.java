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
