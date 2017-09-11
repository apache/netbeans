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

package org.netbeans.modules.csl.navigation.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.modules.csl.navigation.ClassMemberFilters;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/** 
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 * 
 * "Radio button" type action, base class designed for subclassing
 *
 * @author Dafe Simonek
 */
public abstract class SortActionSupport extends AbstractAction implements Presenter.Popup {
    
    private JRadioButtonMenuItem menuItem;
    protected ClassMemberFilters filters;
    
    /** Creates a new instance of SortByNameAction */
    public SortActionSupport ( ClassMemberFilters filters ) {
        this.filters = filters;
    }
    
    public final JMenuItem getPopupPresenter() {
        JMenuItem result = obtainMenuItem();
        updateMenuItem();
        return result;
    }
    
    protected final JRadioButtonMenuItem obtainMenuItem () {
        if (menuItem == null) {
            menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
        return menuItem;
    }
    
    protected abstract void updateMenuItem ();
    
    
    /** Enables sorting by names when selected
     */
    public static final class SortByNameAction extends SortActionSupport {
        
        public SortByNameAction ( ClassMemberFilters filters) {
            super(filters);
            putValue(Action.NAME, NbBundle.getMessage(SortByNameAction.class, "LBL_SortByName")); //NOI18N
        }
    
        public void actionPerformed (ActionEvent e) {
            filters.setNaturalSort(false);
            updateMenuItem();
        }

        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(!filters.isNaturalSort());
        }
    } // end of SortByNameAction

    /** Enables sorting by names when selected
     */
    public static final class SortBySourceAction extends SortActionSupport {
        
        public SortBySourceAction ( ClassMemberFilters filters ) {
            super(filters);
            putValue(Action.NAME, NbBundle.getMessage(SortBySourceAction.class, "LBL_SortBySource")); //NOI18N
        }
    
        public void actionPerformed (ActionEvent e) {
            filters.setNaturalSort(true);
            updateMenuItem();
        }

        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(filters.isNaturalSort());
        }
    } // end of SortBySourceAction
    
    
}
