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
