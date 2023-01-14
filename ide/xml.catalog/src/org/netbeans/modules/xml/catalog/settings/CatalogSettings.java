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
package org.netbeans.modules.xml.catalog.settings;

import java.io.*;
import java.util.*;
import java.beans.*;
import java.util.logging.Level;
import org.netbeans.modules.xml.catalog.lib.IteratorIterator;

import org.openide.util.io.NbMarshalledObject;

import org.netbeans.modules.xml.catalog.spi.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;


/** 
 * The pool holding mounted catalogs both at per project basics
 * and at at global basics. Project scope catalogs are always considered a higher
 * priority ones.
 * <p>
 * Global scope catalogs are intended to be used by semantics modules for
 * which one can assume that if an user enabled such module then the user
 * really wants to have enabled module catalog. It can be done declarativelly
 * at module layer as <code>InstanceCookie</code> providers of {@link CatalogReader}:
 * <pre>
 * <filesystem>
 * <folder name="Plugins"><folder name="XML"><folder name="UserCatalogs">
 *   <file name="org-mycompany-mymodule-MyCatalog.instance">
 *      <attr name="instanceCreate" 
 *            methodValue="org.mycompany.mymodule.MyCatalog.createSingleton"/>
 *      <attr name="instanceOf" 
 *            stringValue="org.netbeans.modules.xml.catalog.spi.CatalogReader"/>
 *   </file>
 * </folder></folder></folder>
 * </filesystem>
 * </pre>
 * <p>
 * Project scope settings are currently only accesible by this class <coda>addCatalog</code>
 * and <code>removeCatalog</code> methods. It's persistent for <code>Serializable</code>
 * implementations.
 *
 * @deprecated Modules are highly suggested to use declarative registrations
 * of global catalogs. Project scope catalogs should be managed by user via UI only.
 *
 * @thread implementation is thread safe
 *
 * @author  Petr Kuzel
 */
@Deprecated
@ServiceProvider(service = CatalogSettings.class)
public final class CatalogSettings implements Externalizable {

    /** Serial Version UID */
    private static final long serialVersionUID = 7895789034L;

    public static final int VERSION_1 = 1;  //my externalization protocol version

    /** Identifies property holding mounted catalogs */
    public static final String PROP_MOUNTED_CATALOGS = "catalogs"; // NOI18N

    // folder at SFS holding global registrations
    private static final String REGISTRATIONS = "Plugins/XML/UserCatalogs";

    // cached instance
    private static Lookup userCatalogLookup;

    // ordered set of mounted catalogs
    private List mountedCatalogs = new ArrayList(5);

    private PropertyChangeSupport listeners = null; 

    private final CatalogListener catalogListener = new CL();
    
    // the only active instance in current project
    private static CatalogSettings instance = null;
    
    /** 
     * Just for externalization purposes.
     * It MUST NOT be called directly by a user code.
     */
    public CatalogSettings() {
        init();
    }
    

    /**
     * Initialized the instance from externalization.
     */
    private void init() {
        listeners = new PropertyChangeSupport(this);
    }
    
    
    /**
     * Return active settings <b>instance</b> in the only one active project.
     * @deprecated does not allow multiple opened projects
     */
    public static synchronized CatalogSettings getDefault() {
        if (instance == null) {
            instance = Lookup.getDefault().lookup(CatalogSettings.class);
        }
        return instance;
    }
    
    /** 
     * Register mounted catalog at project scope level.
     * @param provider to be registered. Must not be null.
     */
    public final void addCatalog(CatalogReader provider) {
        synchronized (this) {
            if (provider == null)
                throw new IllegalArgumentException("null provider not permited"); // NOI18N
            if (mountedCatalogs.contains(provider) == false) {
                mountedCatalogs.add(provider);
            }   
        }
        firePropertyChange(PROP_MOUNTED_CATALOGS, null, null);
        
        // add listener to the catalog
        try {
            provider.addCatalogListener(catalogListener);
        } catch (UnsupportedOperationException ex) {
            // ignore it, we just can not listen at it and save it on change
            // it is fully OK until the catalog instance supports data source
            // change
        }
    }
    
    /** 
     * Deregister given catalog at project scope level. 
     */
    public final void removeCatalog(CatalogReader provider) {
        synchronized (this) {
            mountedCatalogs.remove(provider);
        }
        firePropertyChange(PROP_MOUNTED_CATALOGS, null, null);
        
        // remove listener to the catalog
        try {
            provider.removeCatalogListener(catalogListener);
        } catch (UnsupportedOperationException ex) {
            // ignore it
        }
        
    }

    /**
     * Tests whether removeCatalog will actualy eliminate it.
     * @return false for catalogs declared at layer
     *         true for catalogs added by user using Add action.
     */
    public final boolean isRemovable(CatalogReader provider) {
        return mountedCatalogs.contains(provider);
    }

    /**
     * Return iterator of providers of given class.
     * 
     * @param providerClasses returned providers will be assignable to it
     *                        e.g. <code>CatalogReader</code> class or <code>null</code>
     *                        as a wildcard.
     * @return providers of given class or all if passed <code>null/code> argument.
     *         It never returns null.
     */
    public final synchronized Iterator getCatalogs(Class[] providerClasses) {

        // compose global registrations and local(project) registrations
        IteratorIterator it = new IteratorIterator();                       
        it.add(mountedCatalogs.iterator());
        
        Lookup.Template template = new Lookup.Template(CatalogReader.class);
        Lookup.Result result = getUserCatalogsLookup().lookup(template);
        it.add(result.allInstances().iterator());
        
        if (providerClasses == null)
            return it;
        
        ArrayList list = new ArrayList();
        
        while (it.hasNext()) {
            Object next = it.next();
            // provider test
            boolean add = true;
            for (int i=0; i<providerClasses.length; i++) {
                if (!providerClasses[i].isAssignableFrom(next.getClass())) {
                    add = false;
                    break;
                }
            }
            // add passed
            if (add) list.add(next);
        }
        return list.iterator();
    }    

    /**
     * Provide Lookup containing registered module catalogs.
     */
    private static Lookup getUserCatalogsLookup() {
        if (userCatalogLookup == null) {
            userCatalogLookup = Lookups.forPath(REGISTRATIONS);
        }
        return userCatalogLookup;
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~ listeners ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    public void addPropertyChangeListener(PropertyChangeListener l){
        listeners.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.removePropertyChangeListener(l);
    }
    
    private void firePropertyChange(String name, Object oldValue, Object newValue) {
        listeners.firePropertyChange(name, oldValue, newValue);
    }
    
    // ~~~~~~~~~~~~~~~~~~ Persistent state ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /**
     * Read persistent catalog settings logging diagnostics information if needed.
     */
    public synchronized void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        //super.readExternal(in);        

        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("CatalogSettings.readExternal()"); // NOI18N

        int version = in.readInt();  //IN version
        
        // version switch
        
        if (version != VERSION_1) throw new StreamCorruptedException("Unsupported catalog externalization protocol version (" + version + ").");  // NOI18N
        
        int persistentCount = in.readInt();  //IN count
        
        for (int i = 0; i<persistentCount; i++) {

            String catalogClass = (String) in.readObject();  //IN class name
            NbMarshalledObject marshaled = (NbMarshalledObject) in.readObject(); //IN marshalled object
            try {              
                Object unmarshaled = marshaled.get();
                if (mountedCatalogs.contains(unmarshaled) == false) {
                    mountedCatalogs.add(unmarshaled);
                }
            } catch (ClassNotFoundException ex) {
                //ignore probably missing provider class
                Exceptions.printStackTrace(
                    Exceptions.attachSeverity(
                        Exceptions.attachMessage(ex, NbBundle.getMessage(CatalogSettings.class, "EXC_deserialization_failed", catalogClass)),
                        Level.INFO
                    )
                );
            } catch (IOException ex) {
                //ignore incompatible classes
                Exceptions.printStackTrace(
                    Exceptions.attachSeverity(
                        Exceptions.attachMessage(ex, NbBundle.getMessage(CatalogSettings.class, "EXC_deserialization_failed", catalogClass)),
                        Level.INFO
                    )
                );
            } catch (RuntimeException ex) {
                //ignore catalog that can not deserialize itself without NPE etc.
                Exceptions.printStackTrace(
                    Exceptions.attachSeverity(
                        Exceptions.attachMessage(ex, NbBundle.getMessage(CatalogSettings.class, "EXC_deserialization_failed", catalogClass)),
                        Level.INFO
                    )
                );
            }
        }
    }
    
    /**
     * Write persistent catalog settings as NbMarshalledObjects with some diagnostics information.
     */
    public synchronized void writeExternal(ObjectOutput out) throws IOException  {
        //super.writeExternal(out);

        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("CatalogSettings.writeExternal()"); // NOI18N

        out.writeInt(VERSION_1);  //OUT version
        
        int persistentCount = 0;

        Iterator it = mountedCatalogs.iterator();
        
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof Serializable) {
                persistentCount++;
            }            
        }
        
        it = mountedCatalogs.iterator();
                
        out.writeInt(persistentCount);  //OUT count
        
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof Serializable) {
                try {
                    NbMarshalledObject marshaled = new NbMarshalledObject(next);
                    out.writeObject(next.getClass().getName());  //OUT class name
                    out.writeObject(marshaled);  //OUT marshalled object
                } catch (IOException ex) {
                    // catalog can not be serialized
                    Exceptions.printStackTrace(
                        Exceptions.attachSeverity(
                            Exceptions.attachMessage(ex, NbBundle.getMessage(CatalogSettings.class, "EXC_serialization_failed", next.getClass())),
                            Level.INFO
                        )
                    );
                } catch (RuntimeException ex) {
                    //skip this odd catalog
                    Exceptions.printStackTrace(
                        Exceptions.attachSeverity(
                            Exceptions.attachMessage(ex, NbBundle.getMessage(CatalogSettings.class, "EXC_serialization_failed", next.getClass())),
                            Level.INFO
                        )
                    );
                }
            }
        }        
    }    
        
    /**
     * For debugging purposes only.
     */
    @Override public String toString() {
        Lookup.Template template = new Lookup.Template<CatalogReader>(CatalogReader.class);
        Lookup.Result result = getUserCatalogsLookup().lookup(template);        
        return "CatalogSettings[ global-scope: " + result.allInstances() + 
            ", project-scope: " + mountedCatalogs + " ]";
    }        
    
    
    /**
     * Private catalog listener exposing all changes at catalogs
     * as a change at this bean, so it get saved later.
     */
    private class CL implements CatalogListener {
    
        /** Given public ID has changed - disappeared.  */
        public void notifyRemoved(String publicID) {
        }

        /** Given public ID has changed - created.  */
        public void notifyNew(String publicID) {
        }

        /** Given public ID has changed.  */
        public void notifyUpdate(String publicID) {
        }

        /*
         * It is typical data source change.
         */
        public void notifyInvalidate() {
            firePropertyChange("settings changed!", null, CatalogSettings.this);
        }
    }
    
}
