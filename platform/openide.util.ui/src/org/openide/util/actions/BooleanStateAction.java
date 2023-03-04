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


/** An action that can be toggled on or off.
* The actual "performing" of the action is the toggle itself, so
* this action should be used by listening to the {@link #PROP_BOOLEAN_STATE} property.
* The default value of the state is <code>true</code> (on).
* <p>
* This action is not the most effective way to implement checkbox in
* a menu. Consider using more modern alternative:
* <a href="@org-openide-awt@/org/openide/awt/Actions.html#checkbox(java.lang.String,%20java.lang.String,%20java.lang.String,%20java.lang.String,%20boolean)">
* Actions.checkbox</a>, or declarative <a href="@org-openide-awt@/org/openide/awt/ActionState.html">ActionState annotation</a>.
*
* @author   Ian Formanek, Petr Hamernik
* @deprecated Use new support for stateful actions in <a href="@org-openide-awt@/org/openide/awt/Actions.html">Actions</a> or <a href="@org-openide-awt@/org/openide/awt/ActionState.html">ActionState annotation</a>
*
*/
@Deprecated
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
