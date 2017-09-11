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

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.util.HelpCtx;

/**
 * Provides access to an Repository UI.
 * <p>
 * Every Repository is expected to provide an UI to create or change its attributes. 
 * </p>
 * <p>
 * When creating or editing a new Repository instance, the UI is presented in a modal dialog 
 * with the necessary Save button to trigger the {@link #applyChanges()} method.
 * </p>
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public interface RepositoryController {

    /**
     * Returns a visual component representing the repository this controller is meant for.
     * 
     * @return a visual component representing a repository
     * @since 1.85
     */
    public JComponent getComponent();

    /**
     * Returns the help context associated with this controllers visual component.
     * @return a help context
     * @since 1.85
     */
    public HelpCtx getHelpCtx();

    /**
     * Determines whether the data in this controllers visual component are valid or not.
     * @return <code>true</code> in case the the data are valid, otherwise <code>false</code>
     * @since 1.85
     */
    public boolean isValid();

    /**
     * Called when the Repository UI is about to be opened. 
     * Populate the controllers component.
     * @since 1.85
     */
    public void populate();
    
    /**
     * In case the controller isn't valid, then return an error message to be 
     * shown in Repository dialog.
     * 
     * @return error message
     * @since 1.85
     */
    public String getErrorMessage();
    
    /**
     * Is called when the "Save" button was pressed.
     */
    public void applyChanges(); 
    
    /**
     * Is called when the "Cancel" button was pressed.
     * @since 1.85
     */
    public void cancelChanges(); 

    /**
     * Registers a ChangeListener.
     * 
     * @param l a ChangeListener
     * @since 1.85
     */
    public void addChangeListener(ChangeListener l);

    /**
     * Unregisters a ChangeListener.
     * 
     * @param l a ChangeListener
     * @since 1.85
     */
    public void removeChangeListener(ChangeListener l);
    
}
