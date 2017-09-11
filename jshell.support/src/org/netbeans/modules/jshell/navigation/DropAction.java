/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.navigation;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.model.SnippetHandle;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "ACTION_DropSnippet=Drop snippet",
    "# {0} - snippet count",
    "ACTION_DropSnippet_Count=Drop {0} snippets"
})
public class DropAction extends AbstractAction implements ContextAwareAction {
    private final Collection<SnippetHandle> handles;
    private final JShellEnvironment env;
    
    public DropAction() {
        super(Bundle.ACTION_DropSnippet());
        handles = Collections.emptyList();
        this.env = null;
    }
    
    private DropAction(JShellEnvironment e, Collection<SnippetHandle> handles) {
        this.handles = handles;
        boolean enable = false;
        int cnt = 0;
        
        for (SnippetHandle theHandle : handles) {
            switch (theHandle.getStatus()) {
                case DROPPED:
                case NONEXISTENT:
                case OVERWRITTEN:
                case REJECTED:
                    break;
                default:
                    boolean x = e.getShell() == theHandle.getState();
                    if (x) {
                        cnt++;
                        enable = true;
                    }
                    break;
            }
        }
        setEnabled(enable);
        this.env = e;
        putValue(Action.NAME, enable && cnt > 1 ?
                            Bundle.ACTION_DropSnippet_Count(cnt) :
                            Bundle.ACTION_DropSnippet()
        );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (handles.isEmpty()) {
            return;
        }
        for (SnippetHandle h : handles) {
            if (env.getShell() == h.getState()) {
                switch (h.getStatus()) {
                    case DROPPED:
                    case NONEXISTENT:
                    case OVERWRITTEN:
                    case REJECTED:
                        break;
                    default:
                        env.getShell().drop(h.getSnippet());
                }
            }
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        Collection<? extends Node> nodes = actionContext.lookupAll(Node.class);
        Collection<SnippetHandle> handles = new ArrayList<>();
        for (Node n : nodes) {
            SnippetHandle h = n.getLookup().lookup(SnippetHandle.class);
            if (h != null) {
                handles.add(h);
            }
        }
        return new DropAction(actionContext.lookup(JShellEnvironment.class), handles);
    }
    
    
}
