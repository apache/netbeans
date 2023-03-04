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
