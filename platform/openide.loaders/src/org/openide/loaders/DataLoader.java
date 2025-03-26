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

package org.openide.loaders;


import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.Action;
import org.openide.filesystems.*;
import org.openide.nodes.NodeOp;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.SafeException;

/** A data loader recognizes {@link FileObject}s and creates appropriate
* {@link DataObject}s to represent them.
* The created data object must be a subclass
* of the <EM>representation class</EM> provided in the constructor.
* <P>
* Subclasses of <code>DataLoader</code> should be made <EM>JavaBeans</EM> with
* additional parameters, so a user may configure the loaders in the loader pool.
*
* @author Jaroslav Tulach
*/
public abstract class DataLoader extends SharedClassObject implements DataObject.Factory {
    /** error manager for logging the happenings in loaders */
    static final Logger ERR = Logger.getLogger("org.openide.loaders.DataLoader"); // NOI18N

    // XXX why is this necessary? otherwise reading loader pool now throws heavy
    // InvalidClassException's reading (abstract!) DataLoader...? --jglick
    private static final long serialVersionUID = 1986614061378346169L;

    /** property name of display name */
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N
    /** property name of list of actions */
    public static final String PROP_ACTIONS = "actions"; // NOI18N
    /** property name of list of default actions */
    private static final String PROP_DEF_ACTIONS = "defaultActions"; // NOI18N
   /** key to hold reference to out action manager */
    private static final Object ACTION_MANAGER = new Object ();
    /** representation class, not public property */
    private static final Object PROP_REPRESENTATION_CLASS = new Object ();
    /** representation class name, not public property */
    private static final Object PROP_REPRESENTATION_CLASS_NAME = new Object ();

    private static final int LOADER_VERSION = 1;
    
    /** Create a new data loader.
    * Pass its representation class as a parameter to the constructor. 
    * It is recommended that representation class is superclass of all
    * DataObjects produced by the loaded, but it is not required any more.
    *
    * @param representationClass the superclass (not necessarily) of all objects 
    *    returned from {@link #findDataObject}. The class may be anything but
    *    should be chosen to be as close as possible to the actual class of objects returned from the loader,
    *    to best identify the loader's data objects to listeners.
    * @deprecated Use {@link #DataLoader(String)} instead.
    */
    @Deprecated
    protected DataLoader(Class<? extends DataObject> representationClass) {
        putProperty (PROP_REPRESENTATION_CLASS, representationClass);
        putProperty (PROP_REPRESENTATION_CLASS_NAME, representationClass.getName());
        if (representationClass.getClassLoader() == getClass().getClassLoader()) {
            ERR.warning("Use of super(" + representationClass.getName() + ".class) in " + getClass().getName() + "() should be replaced with super(\"" + representationClass.getName() + "\") to reduce unnecessary class loading");
        }
    }

    /** Create a new data loader.
     * Pass its representation class name
    * as a parameter to the constructor. The constructor is then allowed
    * to return only subclasses of the representation class as the result of
    * {@link #findDataObject}.
    *
    * @param representationClassName the name of the superclass for all objects
     *   returned from
    *    {@link #findDataObject}. The class may be anything but
    *    should be chosen to be as close as possible to the actual class of objects returned from the loader,
    *    to best identify the loader's data objects to listeners.
    *
    * @since 1.10
    */
    protected DataLoader( String representationClassName ) {
        putProperty (PROP_REPRESENTATION_CLASS_NAME, representationClassName);
        // ensure the provided name is correct and can be loaded
        assert getRepresentationClass() != null;
    }
    
    /**
     * Get the representation class for this data loader, as passed to the constructor.
     * @return the representation class
     */
    public final Class<? extends DataObject> getRepresentationClass() {
        Class<?> _cls = (Class<?>) getProperty(PROP_REPRESENTATION_CLASS);
        if (_cls != null) {
            return _cls.asSubclass(DataObject.class);
        }

        Class<? extends DataObject> cls;
        String clsName = (String)getProperty (PROP_REPRESENTATION_CLASS_NAME);
        try {
            cls = Class.forName(clsName, false, getClass().getClassLoader()).asSubclass(DataObject.class);
        } catch (ClassNotFoundException cnfe) {
            throw (IllegalStateException) new IllegalStateException("Failed to load " + clsName + " from " + getClass().getClassLoader()).initCause(cnfe);
        }
	
        putProperty (PROP_REPRESENTATION_CLASS, cls);
        return cls;
    }
    
    /**
     * Get the name of the representation class for this data loader.
     * Might avoid actually loading the class.
     * @return the class name
     * @see #getRepresentationClass
     * @since 3.25
     */
    public final String getRepresentationClassName() {
        return (String)getProperty (PROP_REPRESENTATION_CLASS_NAME);
    }

    /** Get actions.
     * These actions are used to compose
    * a popup menu for the data object. Also these actions should
    * be customizable by the user, so he can modify the popup menu on a
    * data object.
    *
    * @return array of system actions or <CODE>null</CODE> if this loader does not have any
    *   actions
    */
    public final SystemAction[] getActions () {
        Action[] arr = getSwingActions ();
        
        List<SystemAction> list = new ArrayList<SystemAction>();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof SystemAction || arr[i] == null) {
                list.add((SystemAction) arr[i]);
            }
        }
        
        return list.toArray(new SystemAction[0]);
    }
    
    /** Swing actions getter, used from DataNode */
    final Action[] getSwingActions () {
        DataLdrActions mgr = findManager ();
        if (mgr != null) {
            Action[] actions;
            try {
                actions = (Action[]) mgr.instanceCreate();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                actions = null;
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                actions = null;
            }
            if (actions == null) {
                return new Action[0];
            }
        
            return actions;
        } else {
            // old behaviour, that stores actions in properties
            SystemAction[] actions = (SystemAction[])getProperty (PROP_ACTIONS);
            if ( actions == null ) {
                actions = (SystemAction[])getProperty (PROP_DEF_ACTIONS);
                if ( actions == null ) {
                    actions = defaultActions();
                    putProperty (PROP_DEF_ACTIONS, actions, false);
                }
            }
            return actions;
        }
    }
        
    
    /** Identifies the name of context in layer files where the 
     * loader wishes to store its own actions and also read them.
     * In principle any {@link javax.swing.Action} instance can be registered
     * in the context and it will be visible in the default DataNode
     * for data object created by this loader. Only SystemAction can however
     * be manipulated from DataLoader getActions/setActions methods.
     * <p>
     * The default implementation returns null to indicate that no
     * layer reading should be used (use {@link #defaultActions} instead).
     * <p>
     * {@link javax.swing.JSeparator} instances may be used to separate items.
     * <p>
     * Suggested context name: <code>Loaders/<em>PRIMARY-FILE/MIME-TYPE</em>/Actions</code>
     *
     * @return the string name of the context on layer files to read/write actions to
     * @since 5.0
     */
    protected String actionsContext () {
        return null;
    }
    
    /**
     * Get default actions.
     * @deprecated Instead of overriding this method it is preferable to override {@link #actionsContext}.
     * @return array of default system actions
     */
    @Deprecated
    protected SystemAction[] defaultActions () {
        SystemAction[] actions = NodeOp.getDefaultActions();
        return actions;
    }
    
    /** Actions manager.
     */
    private final DataLdrActions findManager () {
        Object manager = getProperty (ACTION_MANAGER);
        if (manager instanceof Class) {
            return null;
        }
        DataLdrActions mgr = (DataLdrActions)manager;
        boolean newlyCreated = false;
        if (mgr == null) {
            String context = actionsContext ();
            if (context == null) {
                // mark we have no context
                putProperty (ACTION_MANAGER, getClass ());
                return null;
            }
            
            FileObject fo = FileUtil.getConfigFile(context);
            if (fo == null) {
                fo = FileUtil.getConfigRoot();
                try {
                    fo = FileUtil.createFolder (fo, context);

                } catch (IOException ex) {
                    ERR.log(Level.WARNING, null, ex);
                }
                newlyCreated = true;
            }
            
            mgr = new DataLdrActions (DataFolder.findFolder (fo), this);
            if (newlyCreated) {
                SystemAction[] arr = defaultActions ();
                if (arr != null) {
                    mgr.setActions (arr);
                }
            }
            putProperty (ACTION_MANAGER, mgr);
        }
        return mgr;
    }
    
    /** Allows the friend code (package and tests) to wait while actions
     * are synchronized with the state of disk.
     */
    final void waitForActions () {
        DataLdrActions mgr = findManager ();
        if (mgr != null) {
            mgr.waitFinished ();
        }
    }
    
    /** Set actions.
    * <p>Note that this method is public, not protected, so it is possible for anyone
    * to modify the loader's popup actions externally (after finding the loader
    * using {@link DataLoaderPool#firstProducerOf}).
    * While this is possible, anyone doing so must take care to place new actions
    * into sensible positions, including consideration of separators.
    * This may also adversely affect the intended feel of the data objects.
    * A preferable solution is generally to use {@link org.openide.actions.ToolsAction service actions}.
    * @param actions actions for this loader or <CODE>null</CODE> if it should not have any
    * @see #getActions
    */
    public final void setActions (SystemAction[] actions) {
        DataLdrActions mgr = findManager ();
        if (mgr != null) {
            mgr.setActions (actions);
        } else {
            putProperty (PROP_ACTIONS, actions, true);
        }
    }
    
    /** Assigns this loader new array of swing actions.
     * @param arr List<Action>
     */
    final void setSwingActions (List/*<Action>*/ arr) {
        firePropertyChange (PROP_ACTIONS, null, null);
    }

    /** Get the current display name of this loader.
    * @return display name
    */
    public final String getDisplayName () {
        String dn = (String) getProperty (PROP_DISPLAY_NAME);
        if (dn != null) {
            return dn;
        } else {
            dn = defaultDisplayName();            
            if (dn != null) {
                return dn;
            } else {
                return getRepresentationClassName();
            }
        }
    }

    /** Set the display name for this loader. Only subclasses should set the name.
    * @param displayName new name
    */
    protected final void setDisplayName (final String displayName) {
        putProperty (PROP_DISPLAY_NAME, displayName, true);
    }

    /** Get the default display name of this loader.
    * @return default display name
    */
    protected String defaultDisplayName () {
        return NbBundle.getBundle(DataLoader.class).getString ("LBL_loader_display_name");
    }

    /** Find a data object appropriate to the given file object--the meat of this class.
     * 
     * @param fo file object
     * @param recognized set of already processed files
     * @return created data object
     * @throws java.io.IOException
     * @since 7.0
     */
    public final DataObject findDataObject (
        FileObject fo, final Set<? super FileObject> recognized
    ) throws IOException {
        class Rec implements RecognizedFiles {
            public void markRecognized(FileObject fo) {
                recognized.add(fo);
            }
        }
        RecognizedFiles rec =
            recognized == DataLoaderPool.emptyDataLoaderRecognized ? DataLoaderPool.emptyDataLoaderRecognized : new Rec();

        return findDataObject(fo, rec);
    }
    
    /** Find a data object appropriate to the given file object--the meat of this class.
     * <p>
    * For example: for files with the same basename but extensions <EM>.java</EM> and <EM>.class</EM>, the handler
    * should return the same <code>DataObject</code>.
    * <P>
    * The loader can add all files it has recognized into the <CODE>recognized</CODE>
    * buffer. Then all these files will be excluded from further processing.
    *
    * @param fo file object to recognize
    * @param recognized recognized file buffer
    * @exception DataObjectExistsException if the data object for the
    *    primary file already exists
    * @exception IOException if the object is recognized but cannot be created
    * @exception InvalidClassException if the class is not instance of
    *    {@link #getRepresentationClass}
    *
    * @return suitable data object or <CODE>null</CODE> if the handler cannot
    *   recognize this object (or its group)
    * @see #handleFindDataObject
    */
    public final DataObject findDataObject (
        FileObject fo, RecognizedFiles recognized
    ) throws IOException {
	try {
	    return DataObjectPool.handleFindDataObject( this, fo, recognized );
	} catch (IOException ioe) {
	    throw ioe;
	} catch (ThreadDeath td) {
	    throw td;
	} catch (RuntimeException e) {
        if (e.getClass().getName().startsWith("org.openide.util.lookup")) { // NOI18N
            // to propagate
            // org.openide.util.lookup.AbstractLookup$ISE: You are trying to modify lookup from lookup query!
            throw e;
        }
	    // Some strange error, perhaps an unexpected exception in
	    // MultiFileLoader.findPrimaryFile. Such an error ought
	    // not cause whole folder recognizer to die! Assume that
	    // file/loader is kaput and continue.
	    IOException ioe = new IOException (e.toString());
            Logger.getLogger(DataLoader.class.getName()).log(Level.WARNING, null, e);
	    ioe.initCause(e);
	    throw ioe;
	}
	
	/*
	if (obj != null && !getRepresentationClass ().isInstance (obj)) {
	    // does not fullfil representation class
	    throw new java.io.InvalidClassException (obj.getClass ().toString ());
	}
	
	return obj;
	*/
    }

    /** Find a data object appropriate to the given file object (as implemented in subclasses).
     * @see #findDataObject
    * @param fo file object to recognize
    * @param recognized recognized file buffer
    * @exception DataObjectExistsException as in <code>#findDataObject</code>
    * @exception IOException as in <code>#findDataObject</code>
    *
    * @return the data object or <code>null</code>
    */
    protected abstract DataObject handleFindDataObject (
        FileObject fo, RecognizedFiles recognized
    ) throws IOException;

    /** Utility method to mark a file as belonging to this loader.
    * When the file is to be recognized this loader will be used first.
    * <P>
    * This method is used by {@link DataObject#markFiles}.
    *
    * @param fo file to mark
    * @exception IOException if setting the file's attribute failed
    */
    public final void markFile (FileObject fo) throws IOException {
        DataLoaderPool.setPreferredLoader(fo, this);
    }
    
    
    

    /** Writes nothing to the stream.
    * @param oo ignored
    */
    @Override
    public void writeExternal (ObjectOutput oo) throws IOException {
        oo.writeObject( new Integer(LOADER_VERSION) );
        
        SystemAction[] arr = (SystemAction[])getProperty (PROP_ACTIONS);
        if (arr == null) {
            oo.writeObject (null);
        } else {
            // convert actions to class names
            List<String> names = new LinkedList<String>();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    names.add (null);
                } else {
                    names.add (arr[i].getClass ().getName ());
                }
            }
            oo.writeObject (names.toArray ());
        }
        
        String dn = (String) getProperty (PROP_DISPLAY_NAME);
        if ( dn == null )
            dn = ""; // NOI18N
        oo.writeUTF ( dn );        
    }

    /** Reads actions and display name from the stream.
    * @param oi input source to read from
    * @exception SafeException if some of the actions is not found in the 
    *    stream, but all the content has been read ok. Subclasses can
    *    catch this exception and continue reading from the stream
    */
    @Override
    public void readExternal (ObjectInput oi)
    throws IOException, ClassNotFoundException {
        Exception main = null;
        int version = 0;        
        
        Object first = oi.readObject ();
        if ( first instanceof Integer ) {            
            version = ((Integer)first).intValue();
            first = oi.readObject ();
        }
        // new version that reads the names of the actions - NB3.1
        Object[] arr = (Object[])first;
        boolean isdefault = true;

        SystemAction[] defactions = getActions ();

        if ( version > 0 || ( version == 0 && arr.length != defactions.length ))
            isdefault = false;
        if (arr != null) {
            List<SystemAction> ll = new ArrayList<SystemAction>(arr.length);
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    ll.add (null);
                    if ( version == 0 && isdefault && defactions[i] != null)
                        isdefault = false;
                    continue;
                }

                try {
                    ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                    if (loader == null) {
                        loader = getClass ().getClassLoader ();
                    }
                    Class<? extends SystemAction> c = Class.forName (
                        Utilities.translate((String)arr[i]),
                        false, // why resolve?? --jglick
                        loader
                    ).asSubclass(SystemAction.class);
                    SystemAction ac = SystemAction.get(c);

                    ll.add (ac);
                    if ( version == 0 && isdefault && !defactions[i].equals(ac))
                        isdefault = false;
                } catch (ClassNotFoundException ex) {
                    if (main == null) {
                        main = ex;
                    } else {
                        Throwable t = main;
                        while (t.getCause() != null) {
                            t = t.getCause();
                        }
                        t.initCause(ex);
                    }
                }
            }
            if (main == null && !isdefault) {
                // Whole action list was successfully read.
                setActions(ll.toArray(new SystemAction[0]));
            } // Else do not try to override the default action list if it is incomplete anyway.
        }
        
        String displayName = oi.readUTF ();
        if ( displayName.equals("") || ( version == 0 && displayName.equals(defaultDisplayName()))) // NOI18N
            displayName = null;
        setDisplayName( displayName );
        
        if (main != null) {
            // exception occured during reading 
            SafeException se = new SafeException (main);
            // Provide a localized message explaining that there is no big problem.
            String message = NbBundle.getMessage (DataLoader.class, "EXC_missing_actions_in_loader", getDisplayName ());
            Exceptions.attachLocalizedMessage(se, message);
            throw se;
        }
    }

    @Override
    protected boolean clearSharedData () {
        return false;
    }

    /** Get a registered loader from the pool.
     * @param loaderClass exact class of the loader (<em>not</em> its data object representation class)
     * @return the loader instance, or <code>null</code> if there is no such loader registered
     * @see DataLoaderPool#allLoaders()
     */
    public static <T extends DataLoader> T getLoader(Class<T> loaderClass) {
        return findObject(loaderClass, true);
    }

    // XXX huh? --jglick
    // The parameter can be <CODE>null</CODE> to
    // simplify testing whether the file object fo is valid or not
    /** Buffer holding a list of primary and secondary files marked as already recognized, to prevent further scanning.
    */
    public interface RecognizedFiles {
        /** Mark this file as being recognized. It will be excluded
        * from further processing.
        *
        * @param fo file object to exclude
        */
        public void markRecognized (FileObject fo);
    }

}
