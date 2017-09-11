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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.properties;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;

import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * Standard node representing a <code>PresentableFileEntry</code>.
 *
 * @see  PresentableFileEntry
 * @author Petr Jiricka
 */
public class FileEntryNode extends AbstractNode {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7882925922830244768L;

    /** FileEntry of this node. */
    private PresentableFileEntry entry;


    /**
     * Creates a data node for a given file entry.
     * The provided children object will be used to hold all child nodes.
     *
     * @param entry entry to work with
     * @param ch children container for the node
     */
    public FileEntryNode (PresentableFileEntry entry, Children ch) {
        super(ch);
        this.entry = entry;

        PropL propListener = new PropL ();
        entry.addPropertyChangeListener(
                WeakListeners.propertyChange(propListener, entry));
        entry.getDataObject().addPropertyChangeListener (propListener);
        
        super.setName (entry.getName ());
    }

    private String getBundleString(String s){
        return NbBundle.getMessage(FileEntryNode.class, s);
    }

    /**
     * Get a cookie. Delegated to {@link PresentableFileEntry#getCookie}. is
     * @return the cookie or <code>null</code>
     */
    public <T extends Node.Cookie> T getCookie(Class<T> cl) {
        T c = entry.getCookie(cl);
        if (c != null) {
            return c;
        } else {
            return super.getCookie (cl);
        }
    }

    /** Gets the represented entry.
     * @return the entry
     */
    public PresentableFileEntry getFileEntry() {
        return entry;
    }

    /** Indicate whether the node may be destroyed.
     * @return tests {@link DataObject#isDeleteAllowed}
     */
    public boolean canDestroy () {
        return entry.isDeleteAllowed ();
    }

    /** Destroyes the node. */
    public void destroy () throws IOException {
        entry.delete ();
        super.destroy ();
    }

    /** 
     * @return true if this node allows copying.
     */
    public final boolean canCopy () {
        return entry.isCopyAllowed ();
    }

    /**
     * @return true if this node allows cutting.
     */
    public final boolean canCut () {
        return entry.isMoveAllowed ();
    }

    /** Rename the data object.
     * @param name new name for the object
     * @exception IllegalArgumentException if the rename failed
     */
    public void setName (String name) {
        try {
            entry.renameEntry (name);
            super.setName (name);
        } catch (IOException ex) {
            throw new IllegalArgumentException (ex.getMessage ());
        }
    }

    /** Gets default action.
     * @deprecated
     * @return no action if the underlying entry is a template. Otherwise the abstract node's default action is returned, possibly <code>null</code>.
     */
    public SystemAction getDefaultAction () {
        if (entry.isTemplate ()) {
            return null;
        } else {
            Action a = getPreferredAction();
            if(a instanceof SystemAction){
                return (SystemAction) a;
            } else {
                return null;
            }            
        }
    }
 
    /** Gets default action.
     * @return no action if the underlying entry is a template. Otherwise the abstract node's default action is returned, possibly <code>null</code>.
     */ 
    public Action getPreferredAction() {
        if (entry.isTemplate ()) {
            return null;
        } else {
            return super.getPreferredAction();
        }
    }
    
    /** Initializes sheet of properties. Allows subclasses to
     * overwrite it.
     * @return the default sheet to use
     */
    protected Sheet createSheet () {
        Sheet s = Sheet.createDefault ();
        Sheet.Set ss = s.get (Sheet.PROPERTIES);

        Node.Property p;

        p = new PropertySupport.ReadWrite<String>(
                PROP_NAME,
                String.class,
                getBundleString("PROP_name"),
                getBundleString("HINT_name")
            ) {
                public String getValue() {
                    return entry.getName();
                }

                public void setValue(String val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    if (!canWrite()) {
                        throw new IllegalAccessException();
                    }
                    FileEntryNode.this.setName(val);
                }

                public boolean canWrite () {
                    return entry.isRenameAllowed();
                }
            };
        p.setName (DataObject.PROP_NAME);
        ss.put (p);

        try {
            p = new PropertySupport.Reflection<Boolean>(
                    entry, Boolean.TYPE, "isTemplate", "setTemplate" // NOI18N
                );
            p.setName (DataObject.PROP_TEMPLATE);
            p.setDisplayName (getBundleString("PROP_template"));
            p.setShortDescription (getBundleString("HINT_template"));
            ss.put (p);
        } catch(Exception ex) {
            throw new IllegalStateException();
        }
        return s;
    }


    /**
     * Support for firing property change.
     *
     * @param ev event describing the change
     */
    void fireChange (PropertyChangeEvent ev) {
        String propertyName = ev.getPropertyName();
        if (propertyName.equals(Node.PROP_COOKIE)) {
            fireCookieChange();
            return;
        }
        // dataobject may have a property that this node does not have
        if (hasProperty(propertyName)) {
            firePropertyChange(propertyName, ev.getOldValue(), ev.getNewValue());
        }
        if (propertyName.equals(DataObject.PROP_NAME)) {
            super.setName (entry.getName ());
        }
    }
    
    private boolean hasProperty(String name) {
        Node.PropertySet[] npsets = getPropertySets();
        for (int i = 0; i < npsets.length; i++) {
            Node.PropertySet npset = npsets[i];
            Node.Property[] nps = npset.getProperties();
            for (int j = 0; j < nps.length; j++) {
                Node.Property np = nps[j];
                if (name.equals(np.getName())) return true;
            }
        }
        return false;
    }    

    /** Property listener on data object that delegates all changes of
     * properties to this node.
     */
    private class PropL extends Object implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent ev) {
            fireChange (ev);
        }
    }
    
}
