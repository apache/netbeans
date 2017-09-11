/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.spi;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.openide.util.HelpCtx;

/**
 * Provides access to a Queries UI.
 * <p>
 * Typically a Query UI should provide at least a query criteria 
 * editor available when creating new queries or modifying existing ones. 
 * In case it isn't possible to create or modify a query on the client it is 
 * possible to provide no QueryController and no UI at all - e.g. an immutable 
 * server defined query with no remote api to modify the criteria.
 * </p>
 * 
 * <p>
 * When editing or creating a Query, the UI is presented in an 
 * TopComponent in the editor area. Fire <code>PROP_CHANGED</code> to notify the Query 
 * TopComponent that the UI state changed, {@link #isChanged()} will be called 
 * accordingly to determine if the IDE-s general SaveAction should be enabled. 
 * On save or TopComponent close are then the <code>saveChanges()</code> 
 * and <code>discardUnsavedChanges()</code> methods called accordingly.
 * </p>
 * 
 * <p>
 * Please <b>note</b>, that the results of an query 
 * are always presented in the TaskDashboard, but eventually, in case the need appears,
 * it is also possible for the bugtracking plugin implementation to provide a
 * customized result view - e.g a table listing more attributes than then TasksDashboard does.
 * </p>
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public interface QueryController {

    /**
     * Fired when the data presented in the Query UI were changed by the user.
     * @since 1.85
     */
    public static String PROP_CHANGED = "bugtracking.query.changed";
    
    /**
     * The mode in which this controllers component is shown.
     * 
     * @see #providesMode(org.netbeans.modules.bugtracking.spi.QueryController.QueryMode) 
     * @since 1.85
     */
    public enum QueryMode {
        /**
         * Determines the Controller Component to create or edit a Query.
         * @since 1.85
         */
        EDIT,
        /**
         * Determines the Controller Component to view the Query results. 
         * @since 1.85
         */
        VIEW
    }

    /**
     * Determines if the Query provides an Editor or a Result view.
     * Depending on the returned value the Query Open (view) and Edit actions will be 
     * enabled on a query node in the TasksDashboard.
     * 
     * @param mode
     * @return <code>true</code> if the given mode is provided by the particular 
     * implementation, otherwise false</code>
     * @since 1.85
     */
    public boolean providesMode(QueryMode mode);

    /**
     * Returns a visual Query component.
     * 
     * @param mode
     * @return a visual component representing a bugtracking query
     * @since 1.85
     */
    public JComponent getComponent(QueryMode mode);
    
    /**
     * Returns the help context associated with this controllers visual component
     * @return help context
     * @since 1.85
     */
    public HelpCtx getHelpCtx();

    /**
     * Called when the component returned by this controller was opened.
     * @since 1.85
     */
    public void opened();

    /**
     * Called when the component returned by this controller was closed.
     * @since 1.85
     */
    public void closed();

    /**
     * This method is called when the general IDE Save button is pressed or when 
     * Save was chosen on close of an Query TopComponent.
     * 
     * @param name in case the Query wasn't saved yet a new name is provided. Otherwise might be null.
     * @return <code>true</code> in case the save worked, otherwise <code>false</code>
     * @since 1.85
     */
    public boolean saveChanges(String name);

    /**
     * This method is called when Discard was chosen on close of an Query TopComponent.
     * 
     * @return <code>true</code> in case the discard worked, otherwise <code>false</code>
     * @since 1.85
     */
    public boolean discardUnsavedChanges();

    /**
     * Determines whether the state of the UI has changed and is supposed to be saved.
     * 
     * @return <code>true</code> in case there are changes to be saved, otherwise <code>false</code>
     */
    public boolean isChanged();
    
    /**
     * Registers a PropertyChangeListener.
     * 
     * @param l a PropertyChangeListener
     * @since 1.85
     */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Unregisters a PropertyChangeListener.
     * 
     * @param l a PropertyChangeListener
     * @since 1.85
     */
    public void removePropertyChangeListener(PropertyChangeListener l);        

}
