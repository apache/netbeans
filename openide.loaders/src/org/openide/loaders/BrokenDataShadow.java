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

package org.openide.loaders;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.ref.Reference;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/** For representing data shadows with broken link to original file.
* Since 1.13 it extends MultiDataObject.
* @author Ales Kemr
*/
final class BrokenDataShadow extends MultiDataObject {
    /** Name of original fileobject */
    private URL url;

    /**
     * Check validity of data shadows only in default FS.
     *
     * @see #canCheckValidity(FileObject)
     */
    private static final boolean CHECK_ONLY_DEFAULT = Boolean.getBoolean(
            "org.openide.loaders.BrokenDataShadow.CHECK_ONLY_DEFAULT"); //NOI18N

    /** Constructs new broken data shadow for given primary file.
    *
    * @param fo the primary file
    * @param loader the loader that created the object
    */
    public BrokenDataShadow (
        FileObject fo, MultiFileLoader loader
    ) throws DataObjectExistsException {        
        super (fo, loader);                                
        
        try {
            url = DataShadow.readURL(fo);
        } catch (IOException ex) {
            try {
                url = new URL("file",null,"/UNKNOWN"); //NOI18N
            } catch (MalformedURLException ex2) {
                Logger.getLogger(BrokenDataShadow.class.getName()).log(Level.WARNING, null, ex2);
            }
        }
        enqueueBrokenDataShadow(this);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
        
    /** Map of <String(nameoffileobject), DataShadow> */
    private static Map<String, Set<Reference<BrokenDataShadow>>> allDataShadows;
    
    private static final long serialVersionUID = -3046981691235483810L;
    
    /** Getter for the Set that contains all DataShadows. */
    static synchronized Map<String, Set<Reference<BrokenDataShadow>>> getDataShadowsSet() {
       if (allDataShadows == null) {
           allDataShadows = new HashMap<String, Set<Reference<BrokenDataShadow>>>();
       }
        return allDataShadows;
    }
    
    private static synchronized void enqueueBrokenDataShadow(BrokenDataShadow ds) {
        Map<String, Set<Reference<BrokenDataShadow>>> m = getDataShadowsSet ();
        
        String prim = ds.getUrl().toExternalForm();
        Reference<BrokenDataShadow> ref = new DataShadow.DSWeakReference<BrokenDataShadow>(ds);
        Set<Reference<BrokenDataShadow>> s = m.get (prim);
        if (s == null) {
            s = java.util.Collections.<Reference<BrokenDataShadow>>singleton (ref);
            getDataShadowsSet ().put (prim, s);
        } else {
            if (! (s instanceof HashSet)) {
                s = new HashSet<Reference<BrokenDataShadow>> (s);
                getDataShadowsSet ().put (prim, s);
            }
            s.add (ref);
        }
    }

    /** @return all active DataShadows or null */
    private static synchronized List<BrokenDataShadow> getAllDataShadows() {
        if (allDataShadows == null || allDataShadows.isEmpty()) {
            return null;
        }
        
        List<BrokenDataShadow> ret = new ArrayList<BrokenDataShadow>(allDataShadows.size());
        Iterator<Set<Reference<BrokenDataShadow>>> it = allDataShadows.values ().iterator();
        while (it.hasNext()) {
            Set<Reference<BrokenDataShadow>> ref = it.next();
            Iterator<Reference<BrokenDataShadow>> refs = ref.iterator ();
            while (refs.hasNext ()) {
                Reference<BrokenDataShadow> r = refs.next ();
                BrokenDataShadow shadow = r.get();
                if (shadow != null) {
                    ret.add(shadow);
                }
            }
        }
        
        return ret;
    }
    
    /** Checks whether a change of the given dataObject
     * does not revalidate a BrokenDataShadow
     */
    static void checkValidity(EventObject ev) {
        synchronized (BrokenDataShadow.class) {
            if (allDataShadows == null || allDataShadows.isEmpty()) {
                return;
            }
        }
        DataObject src = null;
        if (ev instanceof OperationEvent) {
            src = ((OperationEvent)ev).getObject();
        }
        
        FileObject file;
        if (src != null) {
            file = src.getPrimaryFile ();
        } else {
            if (ev instanceof FileEvent) {
                file = ((FileEvent)ev).getFile();
            } else {
                return;
            }
        }

        if (!canCheckValidity(file)) {
            return;
        }

        String key;
        try {
            key = file.getURL().toExternalForm();
        } catch (FileStateInvalidException ex) {
            // OK, exit
            return;
        }
        
        Set shadows = null;
        synchronized (BrokenDataShadow.class) {
            if (allDataShadows == null || allDataShadows.isEmpty ()) return;
            
            if (src != null) {
                shadows = (Set)allDataShadows.get(key);
                if (shadows == null) {
                    // we know the source of the event and there are no
                    // shadows with such original
                    return;
                }
            }
        }
        
        
        List all = getAllDataShadows();
        if (all == null) {
            return;
        }
        
        int size = all.size();
        for (int i = 0; i < size; i++) {
            Object obj = all.get(i);
            ((BrokenDataShadow) obj).refresh();
        }
    }
    
    /**
     * Check if validity of broken data shadows can be checked for this file.
     *
     * See bug 43315 and bug 247812. The OS sometimes asked to insert floppy
     * disk or CD-ROM.
     *
     * @param file The file that may be referenced by broken data shadow.
     * @return True if validity of passed file can be checked.
     */
    private static boolean canCheckValidity(FileObject file) {

        if (CHECK_ONLY_DEFAULT) {
            // #43315 hotfix: disable validity checking for non-SFS filesystem
            try {
                return file.getFileSystem().isDefault();
            } catch (FileStateInvalidException e) {
                // something wrong, exit
                DataObject.LOG.log(Level.WARNING, e.toString(), e);
                return false;
            }
        } else {
            return true;
        }
    }

    /** Constructs new broken data shadow for given primary file.
    * @param fo the primary file
    */
    private BrokenDataShadow (FileObject fo) throws DataObjectExistsException {
        this(fo, DataLoaderPool.getShadowLoader());
    }
    
    /* Getter for delete action.
    * @return true if the object can be deleted
    */
    public boolean isDeleteAllowed() {
        return getPrimaryFile().canWrite();
    }

    /* Check if link to original file is still broken */    
    public void refresh() {
        try {
            if (URLMapper.findFileObject(getUrl()) != null) {
                /* Link to original file was repaired */
                this.setValid(false);
            }
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }
    
    /* Getter for copy action.
    * @return true if the object can be copied
    */
    public boolean isCopyAllowed() {
        return true;
    }

    /* Getter for move action.
    * @return true if the object can be moved
    */
    public boolean isMoveAllowed() {
        return getPrimaryFile().canWrite();
    }

    /* Getter for rename action.
    * @return true if the object can be renamed
    */
    public boolean isRenameAllowed () {
        return getPrimaryFile().canWrite();
    }

    /* Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /* Creates node delegate.
    */    
    protected Node createNodeDelegate () {
        return new BrokenShadowNode (this);
    }

    URL getUrl() {
        return url;
    }

    /** Node for a broken shadow object. */
    private static final class BrokenShadowNode extends DataNode {
        
        /** message to create name of node */
        private static MessageFormat format;
        
        /** the sheet computed for this node or null */
        private Sheet sheet;

        private static final String ICON_NAME = "org/openide/loaders/brokenShadow.gif"; // NOI18N

        /** Create a node.
         * @param broken data shadow
         */        
        public BrokenShadowNode (BrokenDataShadow par) {            
            super (par,Children.LEAF);
            setIconBaseWithExtension(ICON_NAME);
        }
        
        /** Get the display name for the node.
         * A filesystem may {@link org.openide.filesystems.FileSystem#getStatus specially alter} this.
         * @return the desired name
        */
        public String getDisplayName () {
            if (format == null) {
                format = new MessageFormat (DataObject.getString ("FMT_brokenShadowName"));
            }
            return format.format (createArguments ());
        }
        
        public Action[] getActions(boolean context) {
            return new Action[] {
                        SystemAction.get (CutAction.class),
                        SystemAction.get (CopyAction.class),
                        SystemAction.get (PasteAction.class),
                        null,
                        SystemAction.get (DeleteAction.class),
                        null,
                        SystemAction.get (ToolsAction.class),
                        SystemAction.get (PropertiesAction.class)
                    };
        }
    
        /** Returns modified properties of the original node.
        * @return property sets 
        */
        public PropertySet[] getPropertySets () {
            if (sheet == null) {
                sheet = cloneSheet ();                
            }
            return sheet.toArray ();
        }
        
        /** Clones the property sheet of original node.
        */
        private Sheet cloneSheet () {
            PropertySet[] sets = super.getPropertySets ();

            Sheet s = new Sheet ();
            for (int i = 0; i < sets.length; i++) {
                Sheet.Set ss = new Sheet.Set ();
                ss.put (sets[i].getProperties ());
                ss.setName (sets[i].getName ());
                ss.setDisplayName (sets[i].getDisplayName ());
                ss.setShortDescription (sets[i].getShortDescription ());

                // modifies the set if it contains name of object property
                modifySheetSet (ss);
                
                s.put (ss);
            }

            return s;
        }
        
        /** Modifies the sheet set to contain name of property and name of
        * original object.
        */
        private void modifySheetSet (Sheet.Set ss) {
            Property p = ss.remove (DataObject.PROP_NAME);
            if (p != null) {
                p = new PropertySupport.Name (this);
                ss.put (p);

                p = new Name ();
                ss.put (p);
            }
        }
        
        /** Creates arguments for given shadow node */
        private Object[] createArguments () {
            return new Object[] {
                       getDataObject().getName ()
                   };
        }    
    
        /** Class for original name property of broken link
        */
        private final class Name extends PropertySupport.ReadWrite<String> {
            
            public Name () {
                super (
                    "BrokenLink", // NOI18N
                    String.class,
                    DataObject.getString ("PROP_brokenShadowOriginalName"),
                    DataObject.getString ("HINT_brokenShadowOriginalName")
                );
            }

            /* Getter */
            public String getValue () {
                BrokenDataShadow bds = (BrokenDataShadow)getDataObject();
                return bds.getUrl().toExternalForm();
            }
            
            /* Does nothing, property is readonly */
            public void setValue (String newLink) {
                BrokenDataShadow bds = (BrokenDataShadow)getDataObject();
                try {
                    URL u = new URL(newLink);
                    DataShadow.writeOriginal(bds.getPrimaryFile(), u);
                    bds.url = u;
                } catch (IOException ex) {
                    throw (IllegalArgumentException) new IllegalArgumentException(ex.toString()).initCause(ex);
                }
                bds.refresh ();
            }
        }                
        
    }
}
