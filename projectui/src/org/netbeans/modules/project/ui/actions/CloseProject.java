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

package org.netbeans.modules.project.ui.actions;

import java.awt.EventQueue;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Action for removing project from the open projects tab
 */
public class CloseProject extends ProjectAction {
    
    private static final String namePattern = NbBundle.getMessage( CloseProject.class, "LBL_CloseProjectAction_Name" ); // NOI18N
    private static final String namePatternPopup = NbBundle.getMessage( CloseProject.class, "LBL_CloseProjectAction_Popup_Name" ); // NOI18N
    
    
    /** Creates a new instance of CloseProject */
    public CloseProject() {
        this( null );        
    }
    
    public CloseProject( Lookup context ) {
        super( (String)null, namePattern, namePatternPopup, null, context );        
        refresh(getLookup(), true);
    }
        
    @Override
    protected void actionPerformed( final Lookup context ) {
        if (EventQueue.isDispatchThread()) {
            OpenProjectList.OPENING_RP.post(new Runnable() {
                @Override
                public void run() {
                    actionPerformed(context);
                }
            });
            return;
        }
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );        
        // show all modified documents, if an user cancel it then no project is closed        
        OpenProjectList.getDefault().close( projects, true );
    }
    
    @Override
    public void refresh(Lookup context, boolean immediate) {
        
        super.refresh(context, immediate);
        
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        // XXX make it work better for mutliple open projects
        if ( projects.length == 0 || !OpenProjectList.getDefault().isOpen( projects[0] ) ) {
            enable( false );
        }
        else {
            enable( true );
        }        
    }
    
    private void enable(final boolean enable) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setEnabled(enable);
                }
            });
        } else {
            setEnabled(enable);
        }
    }
    
    @Override
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new CloseProject( actionContext );
    }
    
    
}
