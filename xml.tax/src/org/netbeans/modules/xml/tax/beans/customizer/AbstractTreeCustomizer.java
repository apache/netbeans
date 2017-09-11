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
    abstract protected void initComponentValues ();
    
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
    abstract protected void updateReadOnlyStatus (boolean editable);
    
    
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
