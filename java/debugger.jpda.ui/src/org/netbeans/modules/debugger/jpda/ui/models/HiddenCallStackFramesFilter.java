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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.NodeActionsProvider;

/**
 *
 * @author Libor Kotouc
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/CallStackView",
                                 types={ TreeModelFilter.class,
                                         NodeModel.class,
                                         TableModelFilter.class,
                                         NodeActionsProvider.class }),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                                 types={ TreeModelFilter.class,
                                         NodeModel.class,
                                         TableModelFilter.class,
                                         NodeActionsProvider.class },
                                 position=11000)
})
public class HiddenCallStackFramesFilter implements TreeModelFilter,
                                                    NodeModel,
                                                    TableModelFilter,
                                                    NodeActionsProvider {
    
    private ContextProvider lookupProvider;
    private JPDADebugger debugger;
    private SourcePathProvider contextProvider;
   
    public HiddenCallStackFramesFilter (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = (JPDADebugger) lookupProvider.
            lookupFirst(null, JPDADebugger.class);
    }

    private static String convertSlash (String original) {
        return original.replace (File.separatorChar, '/');
    }

    private static String convertClassNameToRelativePath (
            String className
        ) {
        int i = className.indexOf ('$');
        if (i > 0) className = className.substring (0, i);
        String sourceName = className.replace('.', '/') + ".java";
        return sourceName;
    }

    private SourcePathProvider getSourcePathProvider() {
        if (contextProvider == null) {
            List l = lookupProvider.lookup (null, SourcePathProvider.class);
            contextProvider = (SourcePathProvider) l.get (0);
            int i, k = l.size ();
            for (i = 1; i < k; i++) {
                contextProvider = new CompoundSourcePathProvider (
                    (SourcePathProvider) l.get (i), 
                    contextProvider
                );
            }
        }
        return contextProvider;
    }

    /**
     * Translates a relative path ("java/lang/Thread.java") to url 
     * ("file:///C:/Sources/java/lang/Thread.java"). Uses GlobalPathRegistry
     * if global == true.
     *
     * @param relativePath a relative path (java/lang/Thread.java)
     * @param global true if global path should be used
     * @return url
     */
    private String getURL(String relativePath) {
        return getSourcePathProvider().getURL (relativePath, false);
    }

    private boolean isOnSourcePath(CallStackFrame csf) {
        
        String url = null;
        
        try {
            url = getURL (convertSlash (csf.getSourcePath (null)));
        } catch (AbsentInformationException e) {
            url = getURL (convertClassNameToRelativePath (csf.getClassName ()));
        }
        
        return url != null;
    }

    
    
    
    /** 
     * Returns filtered root of hierarchy.
     *
     * @param   original the original tree model
     * @return  filtered root of hierarchy
     */
    public Object getRoot (TreeModel original) {
        return original.getRoot ();
    }
    
    /**
     * Returns number of filtered children for given node.
     * 
     * @param   original the original tree model
     * @param   node the parent node
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (
        TreeModel original,
        Object node
    ) throws UnknownTypeException {
        /*if (node.equals (original.getRoot ())) {
            Object[] originalCh = original.getChildren (
                node, 
                0, 
                original.getChildrenCount (node)
            );
            int i, k = originalCh.length, j = 0;
            boolean in = false;
            for (i = 0; i < k; i++) {
                if (! (originalCh [i] instanceof CallStackFrame)) {
                    j++;
                    continue;
                }
                CallStackFrame f = (CallStackFrame) originalCh [i];
                if (!isOnSourcePath(f)) {
                    if (!in) {
                        j++;
                        in = true;
                    }
                } else {
                    in = false;
                    j++;
                }
            }
            return j;
        }*/
        if (node instanceof HiddenFrames) {
            return ((HiddenFrames) node).getStack ().size ();
        }
        return original.getChildrenCount (node);
    }
    
    /** 
     * Returns filtered children for given parent on given indexes.
     * Typically you should get original nodes 
     * (<code>original.getChildren (...)</code>), and modify them, or return
     * it without modifications. You should not throw UnknownTypeException
     * directly from this method!
     *
     * @param   original the original tree model
     * @param   parent a parent of returned nodes
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getChildren (...)</code> method call only!
     *
     * @return  children for given parent on given indexes
     */
    public Object[] getChildren (
        TreeModel original, 
        Object parent, 
        int from, 
        int to
    ) throws UnknownTypeException {
        if (parent.equals (original.getRoot ()) || parent instanceof JPDAThread) {
            Object[] originalCh = original.getChildren (
                parent, 
                0, 
                original.getChildrenCount (parent)
            );
            if (originalCh.length == 0 || !(originalCh[0] instanceof CallStackFrame)) {
                return originalCh;
            }
            int i, k = originalCh.length;
            ArrayList newCh = new ArrayList ();
            HiddenFrames hiddenFrames = null;
            for (i = 0; i < k; i++) {
                if (! (originalCh [i] instanceof CallStackFrame)) {
                    newCh.add (originalCh [i]);
                    continue;
                }
                CallStackFrame f = (CallStackFrame) originalCh [i];
                if (!isOnSourcePath(f)) {
                    if (hiddenFrames == null) {
                        hiddenFrames = new HiddenFrames ();
                        newCh.add (hiddenFrames);
                    }
                    hiddenFrames.addFrame (f);
                } else {
                    hiddenFrames = null;
                    newCh.add (f);
                }
            }
            //Fix issue #53878: 'to' returned from original in getChildrenCount()
            //is higher than number of children got from getChildren() in this method.
            //It is random JPDA debugger's bug.
            int lto = Math.min(newCh.size(), to);
            return newCh.subList (from, lto).toArray ();
        }
        if (parent instanceof HiddenFrames)
            return ((HiddenFrames) parent).getStack ().toArray ();
        return original.getChildren (parent, from, to);
    }
    
    /**
     * Returns true if node is leaf. You should not throw UnknownTypeException
     * directly from this method!
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.isLeaf (...)</code> method call only!
     * @return  true if node is leaf
     */
    public boolean isLeaf (TreeModel original, Object node) 
    throws UnknownTypeException {
        if (node instanceof HiddenFrames) return false;
        return original.isLeaf (node);
    }
    
    public void addModelListener (ModelListener l) {
    }
    public void removeModelListener (ModelListener l) {
    }
    
    public String getDisplayName (Object node) throws UnknownTypeException {
        if (node instanceof HiddenFrames)
            return NbBundle.getMessage(HiddenCallStackFramesFilter.class, "LBL_HIDDEN_FRAMES");
        throw new UnknownTypeException (node);
    }
    
    public String getIconBase (Object node) throws UnknownTypeException {
        if (node instanceof HiddenFrames)
            return NbBundle.getMessage(HiddenCallStackFramesFilter.class, "RES_FRAME_GROUP");
        throw new UnknownTypeException (node);
    }
    
    public String getShortDescription (Object node) throws UnknownTypeException {
        if (node instanceof HiddenFrames)
            return NbBundle.getMessage(HiddenCallStackFramesFilter.class, "TLT_HIDDEN_FRAMES");
        throw new UnknownTypeException (node);
    }

    //----------------------- TableModelFilter implementation ----------------------------
    
    public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
        
        original.setValueAt(node, columnID, value);
    }

    public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
        
        if (node instanceof HiddenFrames)
            return true;
        
        return original.isReadOnly(node, columnID);
    }

    public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
        
        if (node instanceof HiddenFrames)
            return "";
        
        return original.getValueAt(node, columnID);
    }
    
    
    //----------------------- NodeActionsProvide implementation ----------------------------

    public void performDefaultAction(Object node) throws UnknownTypeException {
    }

    public Action[] getActions(Object node) throws UnknownTypeException {
        if (!(node instanceof HiddenFrames))
            throw new UnknownTypeException (node);

        return new Action[0];
    }


    // innerclasses ............................................................
    
    public static class HiddenFrames {
        private List frames = new ArrayList ();
        
        void addFrame (CallStackFrame frame) {
            frames.add (frame);
        }
        
        List getStack () {
            return frames;
        }
        
        public boolean equals (Object o) {
            if (!(o instanceof HiddenFrames)) return false;
            if (frames.size () != ((HiddenFrames) o).frames.size ()) return false;
            if (frames.size () == 0) return o == this;
            return frames.get (0).equals (
                ((HiddenFrames) o).frames.get (0)
            );
        }
        
        public int hashCode () {
            if (frames.size () == 0) return super.hashCode ();
            return frames.get (0).hashCode ();
        }
    }

    private static class CompoundSourcePathProvider extends SourcePathProvider {

        private SourcePathProvider cp1, cp2;

        CompoundSourcePathProvider (
            SourcePathProvider cp1,
            SourcePathProvider cp2
        ) {
            this.cp1 = cp1;
            this.cp2 = cp2;
        }

        public String getURL (String relativePath, boolean global) {
            String p1 = cp1.getURL (relativePath, global);
            if (p1 != null) return p1;
            return cp2.getURL (relativePath, global);
        }

        public String getRelativePath (
            String url, 
            char directorySeparator, 
            boolean includeExtension
        ) {
            String p1 = cp1.getRelativePath (
                url, 
                directorySeparator, 
                includeExtension
            );
            if (p1 != null) return p1;
            return cp2.getRelativePath (
                url, 
                directorySeparator, 
                includeExtension
            );
        }
    
        public String[] getSourceRoots () {
            String[] fs1 = cp1.getSourceRoots ();
            String[] fs2 = cp2.getSourceRoots ();
            String[] fs = new String [fs1.length + fs2.length];
            System.arraycopy (fs1, 0, fs, 0, fs1.length);
            System.arraycopy (fs2, 0, fs, fs1.length, fs2.length);
            return fs;
        }
    
        public String[] getOriginalSourceRoots () {
            String[] fs1 = cp1.getOriginalSourceRoots ();
            String[] fs2 = cp2.getOriginalSourceRoots ();
            String[] fs = new String [fs1.length + fs2.length];
            System.arraycopy (fs1, 0, fs, 0, fs1.length);
            System.arraycopy (fs2, 0, fs, fs1.length, fs2.length);
            return fs;
        }

        public void setSourceRoots (String[] sourceRoots) {
            cp1.setSourceRoots (sourceRoots);
            cp2.setSourceRoots (sourceRoots);
        }

        public void addPropertyChangeListener (PropertyChangeListener l) {
            cp1.addPropertyChangeListener (l);
            cp2.addPropertyChangeListener (l);
        }

        public void removePropertyChangeListener (PropertyChangeListener l) {
            cp1.removePropertyChangeListener (l);
            cp2.removePropertyChangeListener (l);
        }
    }

}
