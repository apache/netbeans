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

package org.netbeans.api.diff;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import javax.swing.JToolBar;

/**
 * Controller interface that allows external code to control the DIFF component.
 *
 * @author Maros Sandor, Martin Entlicher
 */
public interface DiffView {

    /**
     * This property is fired when the difference count has changed.
     */
    public static final String PROP_DIFF_COUNT = "diffCount"; // NOI18N

    /**
     * Gets the visual DIFF component that modules can include in their GUI.
     *
     * @return Component
     */ 
    public Component getComponent();
    
    /**
     * Gets the number of differences found in sources.
     * 
     * @return int
     */ 
    public int getDifferenceCount();
    
    /**
     * Test whether this view can change the current difference.
     * This is expected to be true when the view has a visual indication
     * of the current difference.
     */
    public boolean canSetCurrentDifference();

    /**
     * Instructs the DIFF view to navigate to the n-th difference.
     * 
     * @param diffNo The difference number (-1 means hide current difference visualization)
     * @throws UnsupportedOperationException iff {@link #canSetCurrentDifference}
     *         returns <code>false</code>.
     */ 
    public void setCurrentDifference(int diffNo) throws UnsupportedOperationException;
    
    /**
     * Get the current difference that is displayed in the view.
     *
     * @return The current difference number
     * @throws UnsupportedOperationException iff {@link #canSetCurrentDifference}
     *         returns <code>false</code>.
     */
    public int getCurrentDifference() throws UnsupportedOperationException;
    
    /**
     * Get a toolbar, that is adviced to be displayed together with the component
     * obtained from {@link #getComponent}.
     *
     * @return the toolbar or <code>null</code> when no toolbar is provided by
     *         this view.
     */
    public JToolBar getToolBar();
    
    /**
     * Add a property change listener.
     * @param l The property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Remove a property change listener.
     * @param l The property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener l);
    
}
