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

package threaddemo.views;

import java.io.IOException;
import java.util.Collections;
import javax.swing.Action;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.OpenAction;
import org.openide.actions.RenameAction;
import org.openide.actions.SaveAction;
import org.openide.actions.ToolsAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import threaddemo.data.PhadhailLookups;
import threaddemo.data.PhadhailNewType;
import threaddemo.model.Phadhail;
import threaddemo.model.PhadhailEvent;
import threaddemo.model.PhadhailListener;
import threaddemo.model.PhadhailNameEvent;

// XXX view of DOM tree too

/**
 * A plain node view of a phadhail tree.
 * @author Jesse Glick
 */
final class PhadhailNode extends AbstractNode implements PhadhailListener {
    
    private final Phadhail ph;
    
    public PhadhailNode(Phadhail ph) {
        super(ph.hasChildren() ? new PhadhailChildren(ph) : Children.LEAF, PhadhailLookups.getLookup(ph));
        this.ph = ph;
        ph.addPhadhailListener((PhadhailListener)WeakListeners.create(PhadhailListener.class, this, ph));
    }
    
    public String getName() {
        return ph.getName();
    }
    
    public String getDisplayName() {
        return ph.getPath();
    }
    
    public void childrenChanged(PhadhailEvent ev) {
        assert ev.getPhadhail().lock().canRead();
        ((PhadhailChildren)getChildren()).update();
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        assert ev.getPhadhail().lock().canRead();
        fireNameChange(ev.getOldName(), ev.getNewName());
        fireDisplayNameChange(null, ev.getPhadhail().getPath());
    }
    
    public boolean canRename() {
        return true;
    }
    
    public void setName(String s) {
        try {
            ph.rename(s);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public void destroy() throws IOException {
        ph.delete();
    }
    
    public NewType[] getNewTypes() {
        if (ph.hasChildren()) {
            return new NewType[] {
                new PhadhailNewType(ph, false),
                new PhadhailNewType(ph, true),
            };
        } else {
            return new NewType[0];
        }
    }
    
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(SaveAction.class),
            null,
            SystemAction.get(NewAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            SystemAction.get(ToolsAction.class),
        };
    }
    
    private static final class PhadhailChildren extends Children.Keys<Phadhail> {
        
        private final Phadhail ph;
        
        public PhadhailChildren(Phadhail ph) {
            this.ph = ph;
        }
        
        protected void addNotify() {
            update();
        }
        
        public void update() {
            setKeys(ph.getChildren());
        }
        
        protected void removeNotify() {
            setKeys(Collections.<Phadhail>emptySet());
        }
        
        protected Node[] createNodes(Phadhail ph) {
            return new Node[] {new PhadhailNode(ph)};
        }
        
    }
    
}
