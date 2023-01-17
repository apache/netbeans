/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Martin
 */
public class DOMBreakpoint extends AbstractBreakpoint {
    
    public static final String PROP_URL = "url";            // NOI18N
    public static final String PROP_NODE = "node";          // NOI18N
    public static final String PROP_TYPES = "types";        // NOI18N

    public enum Type {
        
        SUBTREE_MODIFIED(Debugger.DOM_BREAKPOINT_SUBTREE),
        ATTRIBUTE_MODIFIED(Debugger.DOM_BREAKPOINT_ATTRIBUTE),
        NODE_REMOVED(Debugger.DOM_BREAKPOINT_NODE);
        
        private String typeString;
        
        private Type(String typeString) {
            this.typeString = typeString;
        }
        
        public String getTypeString() {
            return typeString;
        }
    }
    
    private boolean onSubtreeModification;
    private boolean onAttributeModification;
    private boolean onNodeRemoval;
    private Set<Type> types;
    private URL url;
    private DOMNode node;
    
    public DOMBreakpoint(URL url, DOMNode node) {
        this.url = url;
        this.node = node;
        if (url != null) {
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                fo.addFileChangeListener(new DOMFileChange());
            }
        }
    }
    
    public URL getURL() {
        return url;
    }
    
    private void setURL(URL url) {
        URL oldURL = this.url;
        this.url = url;
        firePropertyChange(PROP_URL, oldURL, url);
    }

    public DOMNode getNode() {
        return node;
    }
    
    public void setNode(DOMNode node) {
        DOMNode oldNode = this.node;
        this.node = node;
        firePropertyChange(PROP_NODE, oldNode, node);
    }

    public synchronized boolean isOnSubtreeModification() {
        return onSubtreeModification;
    }

    public void setOnSubtreeModification(boolean onSubtreeModification) {
        Set<Type> oldTypes;
        Set<Type> newTypes;
        synchronized (this) {
            oldTypes = types;
            this.onSubtreeModification = onSubtreeModification;
            if (types == null) {
                newTypes = null;
            } else {
                types = createTypes();
                newTypes = types;
            }
        }
        firePropertyChange(PROP_TYPES, oldTypes, newTypes);
    }

    public synchronized boolean isOnAttributeModification() {
        return onAttributeModification;
    }

    public void setOnAttributeModification(boolean onAttributeModification) {
        Set<Type> oldTypes;
        Set<Type> newTypes;
        synchronized (this) {
            oldTypes = types;
            this.onAttributeModification = onAttributeModification;
            if (types == null) {
                newTypes = null;
            } else {
                types = createTypes();
                newTypes = types;
            }
        }
        firePropertyChange(PROP_TYPES, oldTypes, newTypes);
    }

    public synchronized boolean isOnNodeRemoval() {
        return onNodeRemoval;
    }

    public void setOnNodeRemoval(boolean onNodeRemoval) {
        Set<Type> oldTypes;
        Set<Type> newTypes;
        synchronized (this) {
            oldTypes = types;
            this.onNodeRemoval = onNodeRemoval;
            if (types == null) {
                newTypes = null;
            } else {
                types = createTypes();
                newTypes = types;
            }
        }
        firePropertyChange(PROP_TYPES, oldTypes, newTypes);
    }
    
    public synchronized Set<Type> getTypes() {
        if (types == null) {
            types = createTypes();
        }
        return types;
    }
    
    public boolean addType(Type type) {
        boolean added = false;
        if (Type.SUBTREE_MODIFIED == type) {
            added = !isOnSubtreeModification();
            setOnSubtreeModification(true);
        }
        if (Type.ATTRIBUTE_MODIFIED == type) {
            added = !isOnAttributeModification();
            setOnAttributeModification(true);
        }
        if (Type.NODE_REMOVED == type) {
            added = !isOnNodeRemoval();
            setOnNodeRemoval(true);
        }
        return added;
    }
    
    public synchronized boolean removeType(Type type) {
        boolean removed = false;
        if (Type.SUBTREE_MODIFIED == type) {
            removed = isOnSubtreeModification();
            setOnSubtreeModification(false);
        }
        if (Type.ATTRIBUTE_MODIFIED == type) {
            removed = isOnAttributeModification();
            setOnAttributeModification(false);
        }
        if (Type.NODE_REMOVED == type) {
            removed = isOnNodeRemoval();
            setOnNodeRemoval(false);
        }
        return removed;
    }
    
    private Set<Type> createTypes() {
        Set<Type> ts = EnumSet.noneOf(Type.class);
        if (isOnSubtreeModification()) {
            ts.add(Type.SUBTREE_MODIFIED);
        }
        if (isOnAttributeModification()) {
            ts.add(Type.ATTRIBUTE_MODIFIED);
        }
        if (isOnNodeRemoval()) {
            ts.add(Type.NODE_REMOVED);
        }
        return Collections.unmodifiableSet(ts);
    }
    
    void setValidity(DOMNode.PathNotFoundException pnfex) {
        if (pnfex == null) {
            setValidity(VALIDITY.VALID, null);
        } else {
            setValidity(VALIDITY.INVALID, pnfex.getLocalizedMessage());
        }
    }
    
    final void setValid(String message) {
        setValidity(VALIDITY.VALID, message);
    }

    final void setInvalid(String message) {
        setValidity(VALIDITY.INVALID, message);
    }
    
    final void resetValidity() {
        setValidity(VALIDITY.UNKNOWN, null);
    }

    private class DOMFileChange implements FileChangeListener {

        public DOMFileChange() {
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {}

        @Override
        public void fileDataCreated(FileEvent fe) {}

        @Override
        public void fileChanged(FileEvent fe) {}

        @Override
        public void fileDeleted(FileEvent fe) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(DOMBreakpoint.this);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            setURL(URLMapper.findURL(fe.getFile(), URLMapper.EXTERNAL));
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {}
    }

}
