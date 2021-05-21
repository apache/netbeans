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

package org.netbeans.modules.beans;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.Action;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;

/** Superclass of nodes representing bean patterns.
*
* @author Petr Hrebejk
*/


public class PatternNode extends AbstractNode {
    
    /** Array of the actions of the java methods, constructors and fields. */
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
                /*
                SystemAction.get(OpenAction.class),
                null,
                SystemAction.get(CutAction.class),
                SystemAction.get(CopyAction.class),
                null,
                SystemAction.get(DeleteAction.class),
                SystemAction.get(RenameAction.class),
                null,
                //SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class),
                */
            };


    /** Associated pattern. */
    protected Pattern pattern;

    /** Is this node read-only or are modifications permitted? */
    protected boolean writeable;

    /** Create a new pattern node.
    *
    * @param pattern pattern to represent
    * @param children child nodes
    * @param writeable <code>true</code> if this node should allow modifications.
    *        These include writable properties, clipboard operations, deletions, etc.
    */
    public PatternNode(Pattern pattern, Children children, boolean writeable) {
        super(children);
        this.pattern = pattern;
        this.writeable = writeable;
        
//        setActions(DEFAULT_ACTIONS);

        //this.pattern.addPropertyChangeListener(new WeakListeners.PropertyChange (this));
//        this.pattern.addPropertyChangeListener( WeakListeners.propertyChange (this, this.pattern));
        displayFormat = null;
    }
    
    public PatternNode( ClassPattern cp, boolean writeable ) {
        super(new PatternChildren(cp.getPatterns()));
        this.writeable = writeable;
        this.pattern = cp;
    }

    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public Image getIcon(int type) {
        return pattern.getIcon();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return pattern.getIcon();
    }

    @Override
    public String getDisplayName() {
        return pattern.getName();
    }

    @Override
    public String getHtmlDisplayName() {
        return pattern.getHtmlDisplayName();
    }
    
    /* Gets the short description of this node.
    * @return A localized short description associated with this node.
    */
    public String getShortDescription() {
        return super.getShortDescription(); // If not ovewloaded in ancestors
    }

//    public javax.swing.Action getPreferredAction() {
//        return SystemAction.get(OpenAction.class);
//    }
    
    /** Test whether this node can be renamed.
    * The default implementation assumes it can if this node is {@link #writeable}.
    *
    * @return <code>true</code> if this node can be renamed
    */
    @Override
    public boolean canRename() {
        return writeable;
    }

    /** Test whether this node can be deleted.
    * The default implementation assumes it can if this node is {@link #writeable}.
    *
    * @return <code>true</code> if this node can be renamed
    */
    @Override
    public boolean canDestroy () {
        return writeable;
    }

    /* Copy this node to the clipboard.
    *
    * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
    * @throws IOException if it could not copy
    */
    @Override
    public Transferable clipboardCopy () throws IOException {
        //PENDING
        return super.clipboardCopy();
    }

    /* Cut this node to the clipboard.
    *
    * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
    * @throws IOException if it could not cut
    */
    @Override
    public Transferable clipboardCut () throws IOException {
        if (!writeable)
            throw new IOException();

        //PENDING
        return super.clipboardCopy();
    }

    /** Test whether this node can be copied.
    * The default implementation returns <code>true</code>.
    * @return <code>true</code> if it can
    */
    @Override
    public boolean canCopy () {
        return false;
    }

    /** Test whether this node can be cut.
    * The default implementation assumes it can if this node is {@link #writeable}.
    * @return <code>true</code> if it can
    */
    @Override
    public boolean canCut () {
        return writeable;
    }

    @Override
    public Action[] getActions(boolean context) {
        return DEFAULT_ACTIONS;
    }

//    /** Set all actions for this node.
//    * @param actions new list of actions
//    */
//    public void setActions(SystemAction[] actions) {
//        systemActions = actions;
//    }

    /** Sets the name of the node */
    @Override
    public final void setName( String name ) {
//        try {
//            BeanUtils.beginTrans(true);
//            boolean rollback = true;
//            try  finally {
//                pattern.patternAnalyser.setIgnore(false);
//                BeanUtils.endTrans(rollback);
//            }
//            
//            superSetName( name );
//            
//        } catch (JmiException e) {
//            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
//        }

    }

    /** Called when node name is changed */
    public final void superSetName(String name) {
        super.setName( name );
    }

    /** Set's the name of pattern. Must be defined in descendants
    */
    protected  void setPatternName(String name) {
        throw new UnsupportedOperationException();
    };

//    /** Create a node property representing the pattern's name.
//    * @param canW if <code>false</code>, property will be read-only
//    * @return the property.
//    */
//    protected Node.Property createNameProperty(boolean canW) {
//        return new PatternPropertySupport(PatternProperties.PROP_NAME, String.class, canW) {
//                   /** Gets the value */
//                   public Object getValue () {
//                       return ((Pattern)pattern).getName();
//                   }
//
//                   /** Sets the value */
//                   public void setValue(Object val) throws IllegalArgumentException,
//                       IllegalAccessException, InvocationTargetException {
//                       super.setValue(val);
//                       String str = (String) val;
//                       try {
//                           BeanUtils.beginTrans(true);
//                           boolean rollback = true;
//                           try  finally {
//                               pattern.patternAnalyser.setIgnore(false);
//                               BeanUtils.endTrans(rollback);
//                           }
//                       } catch (JmiException e) {
//                           throw new InvocationTargetException(e);
//                       } catch (ClassCastException e) {
//                           throw new IllegalArgumentException();
//                       }
//                       superSetName(str);
//                   }
//               };
//    }

    /** Called when the node has to be destroyed */
    public void destroy() throws IOException {
//        try {
//            BeanUtils.beginTrans(true);
//            boolean rollback = true;
//            try  finally {
//                BeanUtils.endTrans(rollback);
//            }
//        } catch (JmiException e) {
//            IOException ioe = new IOException();
//            ioe.initCause(e);
//            throw ioe;
//        }
//        super.destroy();
    }

//    protected static String getFormattedMethodName(Method method) {
//        String name = null;
//        Format fmt = SourceNodes.createElementFormat("{n} ({p})"); // NOI18N
//        try {
//            if (method != null) {
//                name = fmt.format (method);
//            }
//        } catch (IllegalArgumentException e) {
//            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
//        }
//
//        return name != null? name: PatternNode.getString("LAB_NoMethod"); // NOI18N
//    }

    public void updateRecursively( Pattern p ) {
        Children ch = getChildren();
        if ( ch instanceof PatternChildren ) {           
           HashSet<Pattern> oldSubs = new HashSet<Pattern>( p.getPatterns() );

           
           // Create a hashtable which maps Description to node.
           // We will then identify the nodes by the description. The trick is 
           // that the new and old description are equal and have the same hashcode
           Node[] nodes = ch.getNodes( true );           
           HashMap<Pattern,PatternNode> oldPattern2node = new HashMap<Pattern, PatternNode>();           
           for (Node node : nodes) {
               oldPattern2node.put(((PatternNode)node).pattern, (PatternNode)node);
           }
           
           // Now refresh keys
           ((PatternChildren)ch).resetKeys(p.getPatterns() /*, pattern.ui.getFilters() */);

           
           // Reread nodes
           nodes = ch.getNodes( true );
           
           for( Pattern newSub : p.getPatterns() ) {
           //for( Node newNode : nodes) {
                //Pattern newSub = ((PatternNode)newNode).pattern;
                PatternNode node = oldPattern2node.get(newSub);
                if ( node != null ) { // filtered out
                    if ( !oldSubs.contains(newSub) && node.getChildren() != Children.LEAF) {                                           
                        pattern.getPatternAnalyser().getUI().expandNode(node); // Make sure new nodes get expanded
                    }     
                    node.updateRecursively( newSub ); // update the node recursively
                }
           }
        }
                        
        Pattern oldPattern = pattern; // Remember old description        
        pattern = p; // set new descrioption to the new node

        // XXXX 

        
        if ( oldPattern.getHtmlDisplayName() != null && !oldPattern.getHtmlDisplayName().equals(pattern.getHtmlDisplayName())) {
            fireDisplayNameChange(oldPattern.name, pattern.name);
        }
          if( oldPattern.getIcon() != null &&  oldPattern.getIcon() != pattern.getIcon()) {
            fireIconChange();
            fireOpenedIconChange();
        }
    }

}
