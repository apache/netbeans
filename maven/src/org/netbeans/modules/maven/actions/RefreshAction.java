/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import static org.netbeans.modules.maven.actions.Bundle.*;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@SuppressWarnings(value = "serial")
@ActionID(id = "org.netbeans.modules.maven.refresh", category = "Project")
@ActionRegistration(displayName = "#ACT_Reload_Project", lazy=false)
@ActionReference(position = 1700, path = "Projects/org-netbeans-modules-maven/Actions")
@Messages("ACT_Reload_Project=Reload POM")
public class RefreshAction extends AbstractAction implements ContextAwareAction {

    private final Lookup context;
    public RefreshAction() {
        this(Lookup.EMPTY);
    }

    @Messages({"# {0} - count", "ACT_Reload_Projects=Reload {0} POMs"})
    private RefreshAction(Lookup lkp) {
        context = lkp;
        Collection<? extends NbMavenProjectImpl> col = context.lookupAll(NbMavenProjectImpl.class);
        if (col.size() > 1) {
            putValue(Action.NAME, ACT_Reload_Projects(col.size()));
        } else {
            putValue(Action.NAME, ACT_Reload_Project());
        }
    }

    @Override public void actionPerformed(ActionEvent event) {
        // #166919 - need to run in RP to prevent RPing later in fireProjectReload()
        //since #227101 fireMavenProjectReload() always posts to the RP... 
                //#211217 in 3.x should not be necessary.. 
                //EmbedderFactory.resetCachedEmbedders();
                for (NbMavenProjectImpl prj : context.lookupAll(NbMavenProjectImpl.class)) {
                    NbMavenProject.fireMavenProjectReload(prj);
                }
    }

    @Override public Action createContextAwareInstance(Lookup actionContext) {
        return new RefreshAction(actionContext);
    }

}
