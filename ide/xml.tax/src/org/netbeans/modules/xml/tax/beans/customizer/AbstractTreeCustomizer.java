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
package org.netbeans.modules.xml.tax.beans.customizer;

import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.netbeans.tax.TreeObject;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public abstract class AbstractTreeCustomizer extends JPanel implements Customizer, PropertyChangeListener {

    /** Serial Version UID */
    private static final long serialVersionUID =7141277140374364170L;
    
    /** */
    private static final String TEXT_DEFAULT = Util.THIS.getString ("TEXT_DEFAULT"); // NOI18N
    
    /** */
    public static final String MIME_XML = "text/xml"; // NOI18N
    
    /** */
    public static final String MIME_DTD = "text/x-dtd"; // NOI18N
    
    /** */
    public static final String MIME_TXT = "text/plain"; // NOI18N
    
    
    /** Used to disable propertu changes etc. during initilizing. */
    protected boolean initializing;
    
    /** */
    private TreeObject treeObject;
    
    /** Does this registered itself listeners as TreeNode? */
    private boolean treeListening = false;
    
    
    //
    // init
    //
    
    /** We call virtual method from constructor. Use initializing to check stage. */
    public AbstractTreeCustomizer () {
        super ();
        
        treeObject   = null;
        initializing = false;
    }
    
    
    //
    // from Customizer
    //
    
    /** Set the object to be customized.
     * @param bean The object to be customized.
     */
    public final void setObject (Object bean) throws IllegalArgumentException {
        try {
            initializing = true;
            
            if (! (bean instanceof TreeObject))
                throw new IllegalArgumentException (Util.THIS.getString ("PROP__invalid_instance", bean)); // NOI18N
            
            treeObject = (TreeObject)bean;
            
            ownInitComponents ();
            
            initValues ();
        } finally {
            initializing = false;
        }
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeObject getTreeObject () {
        return treeObject;
    }
    
    /**
     */
    private final void initValues () {
        initComponentValues ();
        updateReadOnlyStatus ();
        initListeners ();
    }
    
    /**
     */
    protected abstract void initComponentValues ();
    
    /**
     */
    protected void ownInitComponents () {
    }
    
    /**
     */
    private void updateReadOnlyStatus () {
        updateReadOnlyStatus (!!! getTreeObject ().isReadOnly ());
    }
    
    /**
     */
    protected abstract void updateReadOnlyStatus (boolean editable);
    
    
    //
    // events
    //
    
    /**
     */
    private void initListeners () {
        if (!treeListening) {
            treeObject.addPropertyChangeListener (org.openide.util.WeakListeners.propertyChange (this, treeObject));
            treeListening = true;
        }
    }
    
    /**
     * It will be called from AWT thread and it will never be caller during init stage.
     */
    protected void safePropertyChange (PropertyChangeEvent pche) {
        if (pche.getPropertyName ().equals (TreeObject.PROP_READ_ONLY)) {
            updateReadOnlyStatus ();
        }
    }
    
    /**
     * Filter out notifications during selfinitialization stage and
     * pass others in AWT thread.
     */
    public final void propertyChange (final PropertyChangeEvent e) {
        if (initializing)
            return;
        
        if (SwingUtilities.isEventDispatchThread ()) {
            safePropertyChange (e);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    AbstractTreeCustomizer.this.safePropertyChange (e);
                }
            });
        }
    }
    
    
    //
    // Utils
    //
    
    protected static String text2null (String text) {
        if ( text.equals (TEXT_DEFAULT) )
            return null;
        if ( text.length () == 0 )
            return null;
        if ( text.trim ().length () == 0 )
            return null;
        return text;
    }
    
    protected static String null2text (String maybeNull) {
        if ( maybeNull == null )
            return TEXT_DEFAULT;
        return maybeNull;
    }
    
    protected static boolean applyKeyPressed (KeyEvent evt) {
        return (evt.isControlDown () && (evt.getKeyCode () == KeyEvent.VK_ENTER));
    }
    
}
