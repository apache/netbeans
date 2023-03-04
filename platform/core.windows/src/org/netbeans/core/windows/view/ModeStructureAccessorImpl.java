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


package org.netbeans.core.windows.view;


import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.ModeStructureSnapshot.ElementSnapshot;
import org.netbeans.core.windows.model.ModelElement;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Used to pass information of modes model state to view in nice fashion.
 * Need to figure out to which package this class belongs, temporary here.
 *
 * @author  Peter Zavadsky
 */
final class ModeStructureAccessorImpl implements ModeStructureAccessor {
    
    private final ElementAccessor splitRootAccessor;
    
    private final Set<ModeAccessor> separateModeAccessors;
    
    private final Set<SlidingAccessor> slidingModeAccessors;
    
    /** Creates a new instance of ModesModelAccessorImpl. */
    public ModeStructureAccessorImpl(ElementAccessor splitRootAccessor, 
            Set<ModeAccessor> separateModeAccessors, 
            Set<SlidingAccessor> slidingModeAccessors) {
        this.splitRootAccessor = splitRootAccessor;
        this.separateModeAccessors = separateModeAccessors;
        this.slidingModeAccessors = slidingModeAccessors;
    }

    @Override
    public ElementAccessor getSplitRootAccessor() {
        return splitRootAccessor;
    }
    
    @Override
    public ModeAccessor[] getSeparateModeAccessors() {
        return separateModeAccessors.toArray(new ModeAccessor[0]);
    }
    
    @Override
    public SlidingAccessor[] getSlidingModeAccessors() {
        return slidingModeAccessors.toArray(new SlidingAccessor[0]);
    }

    /** @param name name of mode */
    public ModeAccessor findModeAccessor(String name) {
        ModeAccessor ma = findModeAccessorOfName(splitRootAccessor, name);
        if(ma != null) {
            return ma;
        }
        
        for(Iterator it = separateModeAccessors.iterator(); it.hasNext(); ) {
            ma = (ModeAccessor)it.next();
            if(name.equals(ma.getName())) {
                return ma;
            }
        }
        
        for(Iterator it = slidingModeAccessors.iterator(); it.hasNext(); ) {
            ma = (ModeAccessor)it.next();
            if(name.equals(ma.getName())) {
                return ma;
            }
        }
        
        return null;
    }
    
    private static ModeAccessor findModeAccessorOfName(ElementAccessor accessor, String name) {
        if(accessor instanceof ModeAccessor) {
            ModeAccessor ma = (ModeAccessor)accessor;
            if(name.equals(ma.getName())) {
                return ma;
            }
        } else if(accessor instanceof SplitAccessor) {
            SplitAccessor split = (SplitAccessor)accessor; 
            ElementAccessor[] children = split.getChildren();
            for( int i=0; i<children.length; i++ ) {
                ModeAccessor ma = findModeAccessorOfName(children[i], name);
                if(ma != null) {
                    return ma;
                }
            }
        } else if(accessor instanceof EditorAccessor) {
            EditorAccessor editorAccessor = (EditorAccessor)accessor;
            ModeAccessor ma = findModeAccessorOfName(editorAccessor.getEditorAreaAccessor(), name);
            if(ma != null) {
                return ma;
            }
        }
        
        return null;
    }
    

    /** Superclass for accessor of model element.
     * There are three types, split, mode, and editor (represents editor area) type. */
    abstract static class ElementAccessorImpl implements ElementAccessor {
        // PENDING revise
        /** Corresponding object in model (SplitNode or ModeNode for separate mode). */
        private final ModelElement originator;
        /** Corresponding snapshot. */
        private final ModeStructureSnapshot.ElementSnapshot snapshot;
        
        
        public ElementAccessorImpl(ModelElement originator, ModeStructureSnapshot.ElementSnapshot snapshot) {
            this.originator = originator;
            this.snapshot = snapshot;
        }

        /** Gets originator object. Used only in model. */
        @Override
        public final ModelElement getOriginator() {
            return originator;
        }
        
        @Override
        public final ModeStructureSnapshot.ElementSnapshot getSnapshot() {
            return snapshot;
        }
        
        @Override
        public boolean originatorEquals(ElementAccessor o) {
            if(o instanceof ElementAccessorImpl) {
                return getClass().equals(o.getClass()) // To prevent mismatch between split and mode accessor.
                                                       // Split has now originator corresponding to first child.
                    && ((ElementAccessorImpl)o).originator == originator;
            }
            return false;
        }
        
        @Override
        public String toString() {
            return super.toString() + "[originatorHash=" + (originator != null ? Integer.toHexString(originator.hashCode()) : "null") + "]"; // NOI18N
        }
    }
    
    /** */ 
    static final class SplitAccessorImpl extends ElementAccessorImpl implements SplitAccessor {
        private final int orientation;
        private final double[] splitPositions; // relative
        private final ElementAccessor[] children;
        private final double resizeWeight;
        
        public SplitAccessorImpl(ModelElement originator, ElementSnapshot snapshot,
        int orientation, double[] splitPositions,
        ElementAccessor[] children, double resizeWeight) {
            super(originator, snapshot); // It correspond to the first child model element.
            
            this.orientation = orientation;
            this.splitPositions = splitPositions;
            this.children = children;
            this.resizeWeight = resizeWeight;
        }

        @Override
        public int getOrientation() {
            return orientation;
        }
        
        @Override
        public double[] getSplitWeights() {
            return splitPositions;
        }
        
        @Override
        public ElementAccessor[] getChildren() {
            return children;
        }
        
        @Override
        public double getResizeWeight() {
            return resizeWeight;
        }
        
        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append( super.toString() );
            buffer.append( "[orientation=" + orientation );// NOI18N
            buffer.append( ", splitPosition=" ); // NOI18N
            for( int i=0; i<splitPositions.length; i++ ) {
                buffer.append( splitPositions[i] );
                if( i < splitPositions.length-1 )
                    buffer.append( " : " ); // NOI18N
            }
            buffer.append( "]" ); // NOI18N
            return buffer.toString();
        }
    }

    /** */
    static class ModeAccessorImpl extends ElementAccessorImpl implements ModeAccessor { 
        
        public ModeAccessorImpl(ModelElement originator, ModeStructureSnapshot.ModeSnapshot snapshot) {
            super(originator, snapshot);
        }
        
        private ModeStructureSnapshot.ModeSnapshot getModeSnapShot() {
            return (ModeStructureSnapshot.ModeSnapshot)getSnapshot();
        }
        
        
        @Override
        public boolean originatorEquals(ElementAccessor o) {
            if(!super.originatorEquals(o)) {
                return false;
            }
            
            // XXX Even if originators are same, they differ if their states are different.
            // Difference -> split vs. separate representations.
            ModeAccessor me = (ModeAccessor)o;
            return getState() == me.getState();
        }
        
        @Override
        public ModeImpl getMode() {
            return getModeSnapShot().getMode();
        }
        
        @Override
        public String getName() {
            return getModeSnapShot().getName();
        }

        @Override
        public int getState() {
            return getModeSnapShot().getState();
        }

        @Override
        public int getKind() {
            return getModeSnapShot().getKind();
        }

        @Override
        public Rectangle getBounds() {
            return getModeSnapShot().getBounds();
        }

        @Override
        public int getFrameState() {
            return getModeSnapShot().getFrameState();
        }

        @Override
        public TopComponent getSelectedTopComponent() {
            return getModeSnapShot().getSelectedTopComponent();
        }

        @Override
        public TopComponent[] getOpenedTopComponents() {
            return getModeSnapShot().getOpenedTopComponents();
        }
        
        @Override
        public double getResizeWeight() {
            return getModeSnapShot().getResizeWeight();
        }
        
        @Override
        public String toString() {
            return super.toString() + "[name=" + getName() + " ]"; // NOI18N
        }

    }

    /** Data accessor for sliding view */
    static final class SlidingAccessorImpl extends ModeAccessorImpl implements SlidingAccessor { 

        private final String side;
        private final Map<TopComponent,Integer> slideInSizes;
        
        public SlidingAccessorImpl(ModelElement originator, 
                ModeStructureSnapshot.ModeSnapshot snapshot, 
                String side, Map<TopComponent,Integer> slideInSizes) {
            super(originator, snapshot);

            this.side = side;
            this.slideInSizes = slideInSizes;
        }
    
        @Override
        public String getSide() {
            return side;
        }
        
        @Override
        public Map<TopComponent,Integer> getSlideInSizes() {
            return slideInSizes;
        }
        
        @Override
        public boolean originatorEquals(ElementAccessor o) {
            if(!super.originatorEquals(o)) {
                return false;
            }
            
            // XXX Even if originators are same, they differ if their side are different.
            SlidingAccessor me = (SlidingAccessor)o;
            return getSide() == me.getSide();
        }
        
    } // end of SlidingAccessorImpl
        
    
    /** */
    static final class EditorAccessorImpl extends ElementAccessorImpl implements EditorAccessor {
        private final ElementAccessor editorAreaAccessor;
        private final double resizeWeight;
        
        public EditorAccessorImpl(ModelElement originator, ElementSnapshot snapshot,
        ElementAccessor editorAreaAccessor, double resizeWeight) {
            super(originator, snapshot);
            
            this.editorAreaAccessor = editorAreaAccessor;
            this.resizeWeight = resizeWeight;
        }
        
        @Override
        public double getResizeWeight() {
            return resizeWeight;
        }
        
        @Override
        public ElementAccessor getEditorAreaAccessor() {
            return editorAreaAccessor;
        }
        
        @Override
        public String toString() {
            return super.toString() + "\n" + editorAreaAccessor; // NOI18N
        }
    }

    
        @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nModesAccessorImpl hashCode=" + hashCode()); // NOI18N
        sb.append("\nSplit modes:\n"); // NOI18N
        sb.append(dumpAccessor(splitRootAccessor, 0)); 
        sb.append("\nSeparate Modes:"); // NOI18N
        sb.append(dumpSet(separateModeAccessors));
        return sb.toString();
    }
    
    private static String dumpAccessor(ElementAccessor accessor, int indent) {
        StringBuffer sb = new StringBuffer();
        String indentString = createIndentString(indent);
        
        if(accessor instanceof SplitAccessor) {
            SplitAccessor splitAccessor = (SplitAccessor)accessor;
            sb.append(indentString + "split="+splitAccessor); // NOI18N
            indent++;
            ElementAccessor[] children = splitAccessor.getChildren();
            for( int i=0; i<children.length; i++ ) {
                sb.append("\n" + dumpAccessor(children[i], indent)); // NOI18N
            }
        } else if(accessor instanceof ModeAccessor) {
            sb.append(indentString + "mode=" + accessor); // NOI18N
        } else if(accessor instanceof EditorAccessor) {
            sb.append(indentString + "editor=" + accessor); // NOI18N
            sb.append(dumpAccessor(((EditorAccessor)accessor).getEditorAreaAccessor(), ++indent));
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

