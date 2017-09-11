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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.search;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import static java.util.logging.Level.FINER;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Action which searches files in folders, packages and projects.
 * <p>
 * This action uses two different mechanisms of enabling/disabling,
 * depending on whether the action is available in the toolbar or not:
 * <ul>
 *     <li><u>if the action is in the toolbar:</u><br />
 *         The action is updated (enabled/disabled) continuously.
 *         </li>
 *     <li><u>if the action is <em>not</em> in the toolbar</u><br />
 *         The action state is not updated but it is computed on demand,
 *         i.e. when method <code>isEnabled()</code> is called.
 *         </li>
 * </ul>
 * Moreover, the first call of method <code>isEnabled()</code> returns
 * <code>false</code>, no matter whether some projects are open or not.
 * This is made so based on the assumption that the first call of
 * <code>isEnabled()</code> is done during IDE startup as a part of menu
 * creation. It reduces startup time as it does not force projects
 * initialization.
 *
 * @author  Marian Petras
 */
@ActionID(id = "org.netbeans.modules.search.FindInFilesAction", category = "Edit")
@ActionRegistration(lazy = false, displayName = "#LBL_Action_FindInProjects")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-F"),
    @ActionReference(path = "Menu/Edit", position = 2400, separatorBefore = 2300)
})
public class FindInFilesAction extends CallableSystemAction {

    static final long serialVersionUID = 4554342565076372611L;
    
    private static final Logger LOG = Logger.getLogger(
            "org.netbeans.modules.search.FindAction_state");            //NOI18N
    
    /**
     * name of a shared variable - reference to the toolbar presenter
     */
    private static final String VAR_TOOLBAR_COMP_REF
                                = "toolbar presenter ref";              //NOI18N

    /**
     * name of property &quot;replacing&quot;.
     * Value of the property determines whether the action should offer
     * replacing of found matching strings (if {@code true}) or not
     * (if {@code false}). Value {@code true} thus effectively modifies
     * action &quot;find in files&quot; to &quot;replace in files&quot;.
     */
    protected static final String REPLACING = "replacing";              //NOI18N

    /** name of property &quot;type Id of the last used search scope&quot; */
    private static final String VAR_LAST_SEARCH_SCOPE_TYPE
                                = "lastScopeType";                      //NOI18N

    private final String name;
    protected final boolean preferScopeSelection;

    public FindInFilesAction() {
        this(false);
    }

    private FindInFilesAction(boolean preferScopeSelection) {
        this("LBL_Action_FindInProjects", preferScopeSelection);        //NOI18N
    }

    /**
     * Constructor that initializes action name. See #214693.
     */
    protected FindInFilesAction(String nameKey, boolean preferScopeSelection) {
        this.name = NbBundle.getMessage(getClass(), nameKey);
        this.preferScopeSelection = preferScopeSelection;
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        putProperty(REPLACING, Boolean.FALSE, false);
    }

    @Override
    public Component getToolbarPresenter() {
        assert EventQueue.isDispatchThread();
        if (shouldLog(LOG)) {
            log("getMenuPresenter()");
        }

        Component presenter = getStoredToolbarPresenter();
        return presenter;
    }

    /**
     * Returns a toolbar presenter.
     * If the toolbar presenter already exists, returns the existing instance.
     * If it does not exist, creates a new toolbar presenter, stores
     * a reference to it to shared variable <code>VAR_TOOLBAR_BTN_REF</code>
     * and returns the presenter.
     *
     * @return  existing presenter; or a new presenter if it did not exist
     */
    private Component getStoredToolbarPresenter() {
        assert EventQueue.isDispatchThread();
        if (shouldLog(LOG)) {
            log("getStoredToolbarPresenter()");
        }

        Object refObj = getProperty(VAR_TOOLBAR_COMP_REF);
        if (refObj instanceof Reference<?>) {
            Reference<?> ref = (Reference<?>) refObj;
            Object presenterObj = ref.get();
            if (presenterObj instanceof Component) {
                return (Component) presenterObj;
            }
        }
        
        Component presenter = super.getToolbarPresenter();
        putProperty(VAR_TOOLBAR_COMP_REF,
                    new WeakReference<Component>(presenter));
        return presenter;
    }
    
    /**
     * Checks whether the stored toolbar presenter exists but does not create
     * one if it does not exist.
     *
     * @return  <code>true</code> if the reference to the toolbar presenter
     *          is not <code>null</code> and has not been cleared yet;
     *          <code>false</code> otherwise
     * @see  #getStoredToolbarPresenter
     */
    private boolean checkToolbarPresenterExists() {
        assert EventQueue.isDispatchThread();
        if (shouldLog(LOG)) {
            log("checkToolbarPresenterExists()");
        }

        Object refObj = getProperty(VAR_TOOLBAR_COMP_REF);
        if (refObj == null) {
            return false;
        }
        return ((Reference) refObj).get() != null;
    }

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/find.gif";                //NOI18N
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(
                "org.netbeans.modules.search.FindInFilesAction");       //NOI18N
    }

    /** Perform this action. */
    @Override
    public void performAction() {
        assert EventQueue.isDispatchThread();

        boolean replacing = (Boolean) getProperty(REPLACING);

        SearchPanel current = SearchPanel.getCurrentlyShown();
        if (current != null) {
            if (current.isSearchAndReplace() == replacing) {
                current.focusDialog();
            } else {
                current.close();
                showSearchDialog(replacing);
            }
        } else {
            showSearchDialog(replacing);
        }
    }

    private void showSearchDialog(boolean replacing) {
        SearchPanel sp = new SearchPanel(replacing);
        sp.setPreferScopeSelection(preferScopeSelection);
        sp.showDialog();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private final String shortClassName;

    {
        String clsName = getClass().getName();
        int lastDot = clsName.lastIndexOf('.');
        shortClassName = (lastDot != -1) ? clsName.substring(lastDot + 1)
                                         : clsName;
    }

    private boolean shouldLog(Logger logger) {
        return logger.isLoggable(FINER)
               && shortClassName.equals("FindInFilesAction"); // NOI18N
    }

    private void log(String msg) {
        LOG.log(FINER, "{0}: {1}", new Object[]{shortClassName, msg});  //NOI18N
    }

    public static class Selection extends FindInFilesAction {

        public Selection() {
            super(true);
        }
    }
}
