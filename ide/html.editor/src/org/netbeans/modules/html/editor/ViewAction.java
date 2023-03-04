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
package org.netbeans.modules.html.editor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.actions.Viewable;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "ViewAction.displayName=View"
})
@ActionID(id = "org.netbeans.modules.html.editor.ViewAction", category = "View")
@ActionRegistration(displayName = "#ViewAction.displayName", lazy = false)
@ActionReference(path = "Editors/text/html/Popup", position = 1800)
public final class ViewAction extends AbstractAction implements ContextAwareAction {

//    private static final long serialVersionUID = 5687856454545L;
    public ViewAction() {
        setEnabled(false);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        DataObject dobj = actionContext.lookup(DataObject.class);
        if (dobj == null) {
            return this;
        }
        Project owner = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
        if(owner == null) {
            return this;
        }
        
        if ("org.netbeans.modules.web.clientproject.ClientSideProject".equals(owner.getClass().getName())) { //NOI18N
            return this;
        }
        final Viewable viewable = dobj.getLookup().lookup(Viewable.class);
        if (viewable == null) {
            return this;
        }

        return new InnerViewAction(viewable);
        
    }

    private static class InnerViewAction extends AbstractAction {

        private Viewable v;
        
        public InnerViewAction(Viewable v) {
            super(Bundle.ViewAction_displayName());
            this.v = v;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            v.view();
        }
    }
}
