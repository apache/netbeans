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

package org.netbeans.modules.css.visual;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.modules.css.visual.api.ViewMode;
import org.openide.util.ImageUtilities;
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
@NbBundle.Messages({
        "action.name.updated=Show set properties only",
        "action.name.categorized=Show categorized properties",
        "action.name.all=Show all properties"
})
public abstract class ViewActionSupport extends AbstractAction implements Presenter.Popup {
    
    private JRadioButtonMenuItem menuItem;
    protected RuleEditorViews views;
    
    /** Creates a new instance of SortByNameAction */
    public ViewActionSupport (RuleEditorViews views ) {
        this.views = views;
    }
    
    @Override
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
    
    public static final class UpdatedOnlyViewAction extends ViewActionSupport {
        
        public UpdatedOnlyViewAction ( RuleEditorViews filters) {
            super(filters);
            putValue(Action.NAME, Bundle.action_name_updated()); //NOI18N
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/css/visual/resources/viewByUpdated.png", false)); //NOI18N
        }
    
        @Override
        public void actionPerformed (ActionEvent e) {
            views.setViewMode(ViewMode.UPDATED_ONLY);
            updateMenuItem();
        }

        @Override
        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(views.getViewMode() == ViewMode.UPDATED_ONLY);
        }
        
    } 

    public static final class CategorizedViewAction extends ViewActionSupport {
        
        public CategorizedViewAction ( RuleEditorViews filters ) {
            super(filters);
            putValue(Action.NAME, Bundle.action_name_categorized()); //NOI18N
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/css/visual/resources/viewByCategory.png", false)); //NOI18N
        }
    
        @Override
        public void actionPerformed (ActionEvent e) {
            views.setViewMode(ViewMode.CATEGORIZED);
            updateMenuItem();
        }

        @Override
        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(views.getViewMode() == ViewMode.CATEGORIZED);
        }
    } 
   
    public static final class AllViewAction extends ViewActionSupport {
        
        public AllViewAction ( RuleEditorViews filters ) {
            super(filters);
            putValue(Action.NAME, Bundle.action_name_all()); //NOI18N
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/css/visual/resources/viewAll.png", false)); //NOI18N
        }
    
        @Override
        public void actionPerformed (ActionEvent e) {
            views.setViewMode(ViewMode.ALL);
            updateMenuItem();
        }

        @Override
        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(views.getViewMode() == ViewMode.ALL);
        }
    } 
   
    
}
