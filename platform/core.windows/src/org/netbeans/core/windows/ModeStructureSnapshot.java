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


package org.netbeans.core.windows;


import org.netbeans.core.windows.model.ModelElement;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * Snapshot of split structure in model.
 *
 * @author  Peter Zavadsky
 */
public class ModeStructureSnapshot {

    private final ElementSnapshot splitRootSnapshot;

    private final Set<ModeSnapshot> separateModeSnapshots;

    private final Set<SlidingModeSnapshot> slidingModeSnapshots;

    /** Creates a new instance of ModesModelSnapshot. */
    public ModeStructureSnapshot(ElementSnapshot splitRootSnapshot, 
            Set<ModeSnapshot> separateModeSnapshots,
            Set<SlidingModeSnapshot> slidingModeSnapshots) {
        this.splitRootSnapshot = splitRootSnapshot;
        this.separateModeSnapshots = separateModeSnapshots;
        this.slidingModeSnapshots = slidingModeSnapshots;
    }

    public ElementSnapshot getSplitRootSnapshot() {
        return splitRootSnapshot;
    }
    
    public ModeSnapshot[] getSeparateModeSnapshots() {
        return separateModeSnapshots.toArray(new ModeSnapshot[0]);
    }
    
    public SlidingModeSnapshot[] getSlidingModeSnapshots() {
        return slidingModeSnapshots.toArray(new SlidingModeSnapshot[0]);
    }

    /** @param name name of mode */
    public ModeSnapshot findModeSnapshot(String name) {
        ModeSnapshot ma = findModeSnapshotOfName(splitRootSnapshot, name);
        if(ma != null) {
            return ma;
        }
        
        for(Iterator it = separateModeSnapshots.iterator(); it.hasNext(); ) {
            ma = (ModeSnapshot)it.next();
            if(name.equals(ma.getName())) {
                return ma;
            }
        }
        
        for(Iterator it = slidingModeSnapshots.iterator(); it.hasNext(); ) {
            ma = (SlidingModeSnapshot)it.next();
            if(name.equals(ma.getName())) {
                return ma;
            }
        }
        
        return null;
    }
    
    private static ModeSnapshot findModeSnapshotOfName(ElementSnapshot snapshot, String name) {
        if(snapshot instanceof ModeSnapshot) {
            ModeSnapshot ma = (ModeSnapshot)snapshot;
            if(name.equals(ma.getName())) {
                return ma;
            }
        } else if(snapshot instanceof SplitSnapshot) {
            SplitSnapshot split = (SplitSnapshot)snapshot; 
            for(Iterator it = split.getChildSnapshots().iterator(); it.hasNext(); ) {
                ElementSnapshot child = (ElementSnapshot)it.next();
                ModeSnapshot ma = findModeSnapshotOfName(child, name);
                if(ma != null) {
                    return ma;
                }
            }
        } else if(snapshot instanceof EditorSnapshot) {
            EditorSnapshot editorSnapshot = (EditorSnapshot)snapshot;
            ModeSnapshot ma = findModeSnapshotOfName(editorSnapshot.getEditorAreaSnapshot(), name);
            if(ma != null) {
                return ma;
            }
        }
        
        return null;
    }
    
    /** Superclass for snapshot of model element.
     * There are three types, split, mode, and editor (represents editor area) type. */
    public abstract static class ElementSnapshot {
        // PENDING revise
        /** Corresponding object in model (SplitNode or ModeNode or ModeImpl for separate mode). */
        private final ModelElement originator;
        
        private /*final*/ SplitSnapshot parent;

        
        public ElementSnapshot(ModelElement originator, SplitSnapshot parent) {
            this.originator = originator;
            setParent(parent);
        }

        /** Gets originator object. Used only in model. */
        public ModelElement getOriginator() {
            return originator;
        }
        
        public void setParent(SplitSnapshot parent) {
            if(this.parent == null) {
                this.parent = parent;
            } else {
                throw new IllegalStateException("Parent can be set only once," // NOI18N
                    + " this.parent=" + this.parent + ", parent=" + parent); // NOI18N
            }
        }
        
        public SplitSnapshot getParent() {
            return parent;
        }
        
        public boolean originatorEquals(ElementSnapshot o) {
            return getClass().equals(o.getClass()) // To prevent mismatch between split and mode snapshot.
                                                   // Split has now originator corresponding to first child.
                && ((ElementSnapshot)o).originator == originator;
        }

        public abstract double getResizeWeight();
        
        /** Indicates whether component represented by this element is visible or not. */
        public abstract boolean isVisibleInSplit();

        /** Indicates whether there is at least one visible descendant (split relevant). */
        public abstract boolean hasVisibleDescendant();
        
        public String toString() {
            return "Snapshot[originatorHash=" + (originator != null ? Integer.toHexString(originator.hashCode()) : "null" ) + "]"; // NOI18N
        }
    }
    
    /** */ 
    public static class SplitSnapshot extends ElementSnapshot {
        private final int orientation;
        private final List<ElementSnapshot> childSnapshots = new ArrayList<ElementSnapshot>();
        private final Map<ElementSnapshot, Double> childSnapshot2splitWeight = new HashMap<ElementSnapshot, Double>();
        private final double resizeWeight;
        
        public SplitSnapshot(ModelElement originator, SplitSnapshot parent, int orientation,
                List<ElementSnapshot> childSnapshots, 
                Map<ElementSnapshot, Double> childSnapshot2splitWeight, double resizeWeight) {
            super(originator, parent); // XXX PENDING originator corresponds to the first child model element.
            
            this.orientation = orientation;
            this.childSnapshots.addAll(childSnapshots);
            this.childSnapshot2splitWeight.putAll(childSnapshot2splitWeight);
            this.resizeWeight = resizeWeight;
        }

        public int getOrientation() {
            return orientation;
        }
        
        public List getVisibleChildSnapshots() {
            List<ElementSnapshot> l = getChildSnapshots();
            for(Iterator<ElementSnapshot> it = l.iterator(); it.hasNext(); ) {
                ElementSnapshot child = it.next();
                if(!child.hasVisibleDescendant()) {
                    it.remove();
                }
            }
            
            return l;
        }
        
        public List<ElementSnapshot> getChildSnapshots() {
            return new ArrayList<ElementSnapshot>(childSnapshots);
        }
        
        public double getChildSnapshotSplitWeight(ElementSnapshot childSnapshot) {
            Double d = (Double)childSnapshot2splitWeight.get(childSnapshot);
            return d == null ? -1 : d.doubleValue();
        }
        
        public double getResizeWeight() {
            return resizeWeight;
        }
        
        /** Indicates whether component represented by this node is visible or not. */
        public boolean isVisibleInSplit() {
            int count = 0;
            for(Iterator it = getChildSnapshots().iterator(); it.hasNext(); ) {
                ElementSnapshot child = (ElementSnapshot)it.next();
                if(child.hasVisibleDescendant()) {
                    count++;
                    // At leas two are needed so the split is showing.
                    if(count >= 2 
                            //#124847
                            || (Constants.SWITCH_HIDE_EMPTY_DOCUMENT_AREA 
                                && getParent() == null && count==1)) {
                        return true;
                    }
                }
            }
            
            return false;
        }

        /** Indicates whether there is at least one visible descendant. */
        public boolean hasVisibleDescendant() {
            for(Iterator it = getChildSnapshots().iterator(); it.hasNext(); ) {
                ElementSnapshot child = (ElementSnapshot)it.next();
                if(child.hasVisibleDescendant()) {
                    return true;
                }
            }
            
            return false;
        }
        
        public String toString() {
            return super.toString() + "[orientation=" // NOI18N
                + (orientation == Constants.HORIZONTAL ? "horizontal" : "vertical") // NOI18N
                + "]"; // NOI18N
        }
       
    }

    /** */
    public static class ModeSnapshot extends ElementSnapshot { 
        private final ModeImpl mode;
        
        private final String name;
        private final int state;
        private final int kind;
        private final Rectangle bounds;
        private final int frameState;
        private final TopComponent selectedTopComponent;
        private final TopComponent[] openedTopComponents;
        private final double resizeWeight;
        
        public ModeSnapshot(ModelElement originator, SplitSnapshot parent, ModeImpl mode, double resizeWeight) {
            super(originator, parent);
            
            this.mode = mode;

            this.name = mode.getName();
            this.state = mode.getState();
            this.kind = mode.getKind();
            this.bounds = mode.getBounds();
            this.frameState = mode.getFrameState();
            this.selectedTopComponent = mode.getSelectedTopComponent();
            this.openedTopComponents = mode.getOpenedTopComponents().toArray(new TopComponent[0]);
            this.resizeWeight = resizeWeight;
        }
        
        
        public boolean originatorEquals(ElementSnapshot o) {
            if(!super.originatorEquals(o)) {
                return false;
            }
            
            // XXX Even if originators are same, they differ if their states are different.
            // Difference -> split vs. separate representations.
            ModeSnapshot me = (ModeSnapshot)o;
            return getState() == me.getState();
        }
        
        public ModeImpl getMode() {
            return mode;
        }
        
        public String getName() {
            return name;
        }

        public int getState() {
            return state;
        }

        public int getKind() {
            return kind;
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public int getFrameState() {
            return frameState;
        }

        public TopComponent getSelectedTopComponent() {
            return selectedTopComponent;
        }

        public TopComponent[] getOpenedTopComponents() {
            return openedTopComponents;
        }
        
        public double getResizeWeight() {
            return resizeWeight;
        }

        public boolean isVisibleInSplit() {
            if(getOpenedTopComponents().length == 0) {
                return false;
            }

            if(getState() == Constants.MODE_STATE_SEPARATED) {
                return false;
            }
            
            if(mode.getKind() == Constants.MODE_KIND_EDITOR ) {
                WindowManagerImpl wm = WindowManagerImpl.getInstance();
                if( null != wm.getEditorMaximizedMode() && wm.getEditorMaximizedMode() != mode )
                    return false;
            }

            return true;
        }
        
        public boolean isVisibleSeparate() {
            if(getOpenedTopComponents().length == 0) {
                return false;
            }
            
            if(getState() == Constants.MODE_STATE_JOINED) {
                return false;
            }
            
            return true;
        }
        
        public boolean hasVisibleDescendant() {
            return isVisibleInSplit();
        }
        
        public String toString() {
            return super.toString() + "[name=" + mode.getName() + ", permanent=" + mode.isPermanent() // NOI18N
                + ", constraints=" + Arrays.asList(mode.getConstraints()) + "]"; // NOI18N
        }

    }
    
    public static class SlidingModeSnapshot extends ModeSnapshot { 

        private final String side;
        private final Map<TopComponent,Integer> slideInSizes;
        
        public SlidingModeSnapshot(ModeImpl mode, String side, Map<TopComponent,Integer> slideInSizes) {
            super(null, null, mode, 0D);
            
            this.side = side;
            this.slideInSizes = slideInSizes;
        }
        
        public String getSide () {
            return side;
        }
        
        public Map<TopComponent,Integer> getSlideInSizes() {
            return slideInSizes;
        }
    } // end of SlidingModeSnapshot
    
    
    /** */
    public static class EditorSnapshot extends ElementSnapshot {
        private final ModeStructureSnapshot.ElementSnapshot editorAreaSnapshot;
        private final double resizeWeight;
        
        public EditorSnapshot(ModelElement originator, SplitSnapshot parent,
        ElementSnapshot editorAreaSnapshot, double resizeWeight) {
            super(originator, parent);
            
            this.editorAreaSnapshot = editorAreaSnapshot;
            this.resizeWeight = resizeWeight;
        }
        
        public double getResizeWeight() {
            return resizeWeight;
        }
        
        public ElementSnapshot getEditorAreaSnapshot() {
            return editorAreaSnapshot;
        }

        /** Indicates whether component represented by this node is visible or not. */
        public boolean isVisibleInSplit() {
            if(Constants.SWITCH_HIDE_EMPTY_DOCUMENT_AREA) {
                return null != editorAreaSnapshot && editorAreaSnapshot.isVisibleInSplit();
            } else {
                return true;
            }
        }

        /** Indicates whether there is at least one visible descendant. */
        public boolean hasVisibleDescendant() {
            return isVisibleInSplit();
        }
        
        public String toString() {
            return super.toString() + "\n" + editorAreaSnapshot; // NOI18N
        }
    }

    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nModesSnapshot hashCode=" + Integer.toHexString(hashCode())); // NOI18N
        sb.append("\nSplit modes:\n"); // NOI18N
        sb.append(dumpSnapshot(splitRootSnapshot, 0)); 
        sb.append("\nSeparate Modes:"); // NOI18N
        sb.append(dumpSet(separateModeSnapshots));
        return sb.toString();
    }
    
    private static String dumpSnapshot(ElementSnapshot snapshot, int indent) {
        StringBuffer sb = new StringBuffer();
        String indentString = createIndentString(indent);
        
        if(snapshot instanceof SplitSnapshot) {
            SplitSnapshot splitSnapshot = (SplitSnapshot)snapshot;
            sb.append(indentString + "split="+splitSnapshot); // NOI18N
            indent++;
            for(Iterator it = splitSnapshot.getChildSnapshots().iterator(); it.hasNext(); ) {
                ElementSnapshot child = (ElementSnapshot)it.next();
                sb.append("\n" + dumpSnapshot(child, indent)); // NOI18N
            }
        } else if(snapshot instanceof ModeSnapshot) {
            sb.append(indentString + "mode=" + snapshot); // NOI18N
        } else if(snapshot instanceof EditorSnapshot) {
            sb.append(indentString + "editor=" + snapshot); // NOI18N
            sb.append(dumpSnapshot(((EditorSnapshot)snapshot).getEditorAreaSnapshot(), ++indent));
        }
        
        return sb.toString();
    }
    
    private static String createIndentString(int indent) {
        StringBuffer sb = new StringBuffer(indent);
        for(int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        
        return sb.toString();
    }
    
    private static String dumpSet(Set separateModes) {
        StringBuffer sb = new StringBuffer();
        
        for(java.util.Iterator it = separateModes.iterator(); it.hasNext(); ) {
            sb.append("\nmode=" + it.next());
        }
        
        return sb.toString();
    }
    
}

