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

package org.netbeans.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.ServiceType;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.io.NbMarshalledObject;

/** Works with all service types.
*
* @author Jaroslav Tulach
* @deprecated Obsoleted by lookup and new settings system.
*/
@SuppressWarnings({"deprecation","unchecked"})
@Deprecated
@org.openide.util.lookup.ServiceProvider(service=org.openide.ServiceType.Registry.class)
public final class Services extends ServiceType.Registry implements LookupListener {
    /** serial */
    static final long serialVersionUID =-7558069607307508327L;
    
    /** Result containing all current services. */
    private Lookup.Result<ServiceType> allTypes;
    
    /** Mapping between service name and given ServiceType instance. */
    private Map<String, ServiceType> name2Service;
    
    /** Default instance */
    public static Services getDefault () {
        return (Services)Lookup.getDefault ().lookup (org.openide.ServiceType.Registry.class);
    }
    
    public Services() {
        name2Service = new HashMap<String, ServiceType>();
    }
    
    public ServiceType find(Class clazz) {
        return (ServiceType)Lookup.getDefault().lookup(clazz);
    }
    
    /** Override to specially look up no-op services. */
    public ServiceType find (String name) {
        Map<String, ServiceType> lookupMap = name2Service;
        ServiceType ret;
        synchronized (lookupMap) {
            ret = lookupMap.get(name);
        }
        
        if (ret == null) {
            ret = super.find(name);
            synchronized (lookupMap) {
                lookupMap.put(name, ret);
            }
        }
        
        return ret;
    }
    
    /** Result containing all current services. */
    private Lookup.Result<ServiceType> getTypesResult() {
        boolean init = false;
        synchronized (this) {
            if (allTypes == null) {
                allTypes = Lookup.getDefault().lookupResult(ServiceType.class);
                allTypes.addLookupListener(this);
                init = true;
            }
        }
        if (init) resultChanged(null);
        return allTypes;
    }
    
    /** A change in lookup occured.
     * @param ev event describing the change
     */
    public void resultChanged(LookupEvent ev) {
        synchronized (name2Service) {
            name2Service.clear();
        }
    }
    
    /** Getter for list of all services types.
    * @return list of ServiceType
    */
    public java.util.List<ServiceType> getServiceTypes () {
        return new ArrayList<ServiceType>(getTypesResult().allInstances());
    }
    
    /** Setter for list of all services types. This allows to change
    * instaces of the objects but only of the types that are already registered
    * to the system by manifest sections.
    *
    * @param arr list of ServiceTypes 
    */
    public synchronized void setServiceTypes (java.util.List/*<ServiceType>*/ arr0) {
        if (arr0 == null) {
            throw new NullPointerException();
        }
        
        @SuppressWarnings("unchecked") java.util.List<ServiceType> arr = ensureSingleness((java.util.List<ServiceType>) arr0);
        
        HashMap<ServiceType,DataObject> services = new HashMap<ServiceType,DataObject>(20); // <service type, DataObject>
        searchServices(findSessionFolder("Services").getPrimaryFile(), services); // NOI18N
        
        // storing services
        HashMap<DataFolder,List<DataObject>> order = new HashMap<DataFolder,List<DataObject>>(10); // <parent folder, <file>>
        Iterator<ServiceType> it = arr.iterator();
        while (it.hasNext()) {
            ServiceType st = it.next();
            DataObject dobj = services.get(st);
            
            if (dobj != null) {
                // store existing
                try {
                    dobj = InstanceDataObject.create(dobj.getFolder(), dobj.getPrimaryFile().getName(), st, null);
                } catch (IOException ex) {
                    Logger.getLogger(Services.class.getName()).log(Level.WARNING, null, ex);
                }
                services.remove(st);
            } else {
                dobj = storeNewServiceType(st);
            }
            
            // compute order in folders
            if (dobj != null) {
                DataFolder parent = dobj.getFolder();
                List<DataObject> orderedFiles = order.get(parent);
                if (orderedFiles == null) {
                    orderedFiles = new ArrayList<DataObject>(6);
                    order.put(parent, orderedFiles);
                }
                orderedFiles.add(dobj);
            }
        }
        
        // storing order attribute
	for (Entry<DataFolder,List<DataObject>> entry: order.entrySet()) {
            DataObject parent = entry.getKey();
            List<DataObject> orderedFiles = entry.getValue();
            if (orderedFiles.size() < 2) continue;
            
            Iterator<DataObject> files = orderedFiles.iterator();
            StringBuilder orderAttr = new StringBuilder(64);
            while (files.hasNext()) {
                DataObject file = files.next();
                orderAttr.append(file.getPrimaryFile().getNameExt()).append('/');
            }
            orderAttr.deleteCharAt(orderAttr.length() - 1);
            try {
                parent.getPrimaryFile().
                    setAttribute("OpenIDE-Folder-Order", orderAttr.toString()); // NOI18N
            } catch (IOException ex) {
                Logger.getLogger(Services.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        
        // remove remaining services from default FS
	for (DataObject dobj: services.values()) {
            try {
                dobj.delete();
            } catch (IOException ex) {
                Logger.getLogger(Services.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        
    }
    
    private DataObject storeNewServiceType(ServiceType st) {
        Class stype = st.getClass ();
        // finds direct subclass of service type
        while (stype.getSuperclass () != ServiceType.class) {
            stype = stype.getSuperclass();
        }
        
        try{
            String folder = org.openide.util.Utilities.getShortClassName(stype);

            DataFolder dfServices = findSessionFolder("Services"); // NOI18N
            DataFolder dfTarget = DataFolder.create(dfServices, folder);
            
            return InstanceDataObject.create(dfTarget, null, st, null);
        } catch (Exception ex) {
            Logger.getLogger(Services.class.getName()).log(Level.WARNING, null, ex);
            return null;
        }
    }
    
    /** ensure that instance of the service type will be listed just once.
     */
    private List<ServiceType> ensureSingleness(List<ServiceType> l) {
        List<ServiceType> newList = new ArrayList<ServiceType>(l.size());
        
	for (ServiceType stype: l) {
            if (newList.contains(stype)) {
                continue;
            } else {
                newList.add(stype);
            }
        }
        
        return newList;
    }
    
    /** search all data objects containing service type instance. */
    private void searchServices(FileObject folder, Map<ServiceType,DataObject> services) {
        FileObject[] fobjs = folder.getChildren();
        for (int i = 0; i < fobjs.length; i++) {
            if (!fobjs[i].isValid())
                continue;
            if (fobjs[i].isFolder()) {
                searchServices(fobjs[i], services);
            } else {
                try {
                    DataObject dobj = DataObject.find(fobjs[i]);
                    InstanceCookie inst = (InstanceCookie) dobj.getCookie(InstanceCookie.class);

                    if (inst == null)
                        continue;
                    if (instanceOf(inst, ServiceType.class)) {
                        ServiceType ser = (ServiceType) inst.instanceCreate();

                        services.put(ser, dobj);
                    }
                }
                catch (DataObjectNotFoundException ex) {
                }
                catch (Exception ex) {
                    Logger.getLogger(Services.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        }
    }
    
    /** test if instance cookie is instance of clazz*/
    private static boolean instanceOf(InstanceCookie inst, Class clazz) {
        if (inst instanceof InstanceCookie.Of) {
            return ((InstanceCookie.Of) inst).instanceOf(clazz);
        } else {
            try {
                return clazz.isAssignableFrom(inst.instanceClass());
            } catch (Exception ex) {
                Logger.getLogger(Services.class.getName()).log(Level.WARNING, null, ex);
                return false;
            }
        }
    }
    
    /** all services */
    public Enumeration<ServiceType> services () {
        return Collections.enumeration (getServiceTypes ());
    }

    /** Get all available services that are subclass of given class
    * @param clazz the class that all services should be subclass of
    * @return an enumeration of {@link ServiceType}s that are subclasses of
    *    given class
    */
    public <T extends ServiceType> Enumeration<T> services(Class<T> clazz) {
        if (clazz == null) return org.openide.util.Enumerations.empty();
        Collection res = Lookup.getDefault().lookupAll(clazz);
        return Collections.enumeration(res);
    }
    
    /** Write the object down.
    */
    private void writeObject (ObjectOutputStream oos) throws IOException {
        Enumeration en = services ();
        while (en.hasMoreElements ()) {
            ServiceType s = (ServiceType)en.nextElement ();

            NbMarshalledObject obj;
            try {
                obj = new NbMarshalledObject (s);
            } catch (IOException ex) {
                Logger.getLogger(Services.class.getName()).log(Level.WARNING, null, ex);
                // skip the object if it cannot be serialized
                obj = null;
            }
            if (obj != null) {
                oos.writeObject (obj);
            }
        }

        oos.writeObject (null);
    }

    /** Read the object.
    */
    private void readObject (ObjectInputStream oos)
    throws IOException, ClassNotFoundException {
        final LinkedList<ServiceType> ll = new LinkedList<ServiceType> ();
        for (;;) {
            NbMarshalledObject obj = (NbMarshalledObject) oos.readObject();

            if (obj == null) {
                break;
            }
            try {
                ServiceType s = (ServiceType) obj.get();

                ll.add(s);
            }
            catch (IOException ex) {
                Logger.getLogger(Services.class.getName()).log(Level.WARNING, null, ex);
            }
            catch (ClassNotFoundException ex) {
                Logger.getLogger(Services.class.getName()).log(Level.WARNING, null, ex);
            }
        }

        getDefault ().setServiceTypes (ll);
    }

    /** Only one instance */
    private Object readResolve () {
        return getDefault ();
    }

    private static DataFolder findSessionFolder(String name) {
        try {
            FileObject fo = FileUtil.getConfigFile(name);
            if (fo == null) {
                // resource not found, try to create new folder
                fo = FileUtil.createFolder(FileUtil.getConfigRoot(), name);
            }
            return DataFolder.findFolder(fo);
        } catch (IOException ex) {
            throw (IllegalStateException) new IllegalStateException("Folder not found and cannot be created: " + name).initCause(ex); // NOI18N
        }
    }

}
