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


/** An action that can be toggled on or off.
* The actual "performing" of the action is the toggle itself, so
* this action should be used by listening to the {@link #PROP_BOOLEAN_STATE} property.
* The default value of the state is <code>true</code> (on).
* <p>
* This action is not the most effective way to implement checkbox in
* a menu. Consider using more modern alternative:
* <a href="@org-openide-awt@/org/openide/awt/Actions.html#checkbox(java.lang.String,%20java.lang.String,%20java.lang.String,%20java.lang.String,%20boolean)">
* Actions.checkbox</a>.
*
* @author   Ian Formanek, Petr Hamernik
*/
public abstract class BooleanStateAction extends SystemAction implements Presenter.Menu, Presenter.Popup,
    Presenter.Toolbar {
    /** serialVersionUID */
    static final long serialVersionUID = 6394800019181426199L;

    /** Name of property hold the state of the action. */
    public static final String PROP_BOOLEAN_STATE = "booleanState"; // NOI18N

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a MenuBar.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getMenuPresenter() {
        return org.openide.util.actions.ActionPresenterProvider.getDefault().createMenuPresenter(this);
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a Popup Menu.
    * The default implmentation returns the same JMenuItem as the getMenuPresenter.
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

    /** Get the current state.
    * @return <code>true</code> if on
    */
    public boolean getBooleanState() {
        return getProperty(PROP_BOOLEAN_STATE).equals(Boolean.TRUE);
    }

    /** Set the current state.
    * Fires a change event, which should be used to affect other components when
    * its state is toggled.
    * @param value <code>true</code> to turn on, <code>false</code> to turn off
    */
    public void setBooleanState(boolean value) {
        Boolean newValue = value ? Boolean.TRUE : Boolean.FALSE;
        Boolean oldValue = (Boolean) putProperty(PROP_BOOLEAN_STATE, newValue);

        firePropertyChange(PROP_BOOLEAN_STATE, oldValue, newValue);
    }

    /* Initializes its own properties (and let superclass initialize
    * too).
    */
    protected void initialize() {
        putProperty(PROP_BOOLEAN_STATE, Boolean.TRUE);
        super.initialize();
    }

    /* Implementation of method of javax.swing.Action interface.
    * Changes the boolean state.
    *
    * @param ev ignored
    */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        setBooleanState(!getBooleanState());
    }
}
