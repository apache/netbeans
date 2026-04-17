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

package org.netbeans.api.autoupdate;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl;
import org.openide.modules.ModuleInfo;

/**
 * An object that keeps requests for operations upon instances of <code>UpdateEelement</code>
 * (like install, uninstall, update, enable, disable), provides checks whether 
 * chosen operation is allowed (e.g. already installed plugin cannot be scheduled for install again), 
 * provides information which additional plugins are
 * required and so on.
 * <p>
 * Typical scenario how to use:
 * <ul>
 * <li>use one of factory methods for creating instance of <code>OperationContainer</code> 
 * for chosen operation: {@link #createForInstall}, {@link #createForUninstall}, 
 * {@link #createForUpdate}, {@link #createForEnable},{@link #createForDisable}</li>
 * <li>add instances of <code>UpdateElement</code> (see {@link OperationContainer#add})</li>
 * <li>check if additional required instances of <code>UpdateElement</code> are needed 
 * ({@link OperationInfo#getRequiredElements}), 
 * if so then these required instances should be also added</li>
 * <li>next can be tested for broken dependencies ({@link OperationInfo#getBrokenDependencies}) </li>
 * <li>call method {@link #getSupport} to get either {@link InstallSupport} or {@link OperationSupport} 
 * that can be used for performing operation</li>
 * 
 * </ul>
 * Code example:
 * <pre style="background-color: rgb(255, 255, 153);"> 
 * UpdateElement element = ...;
 * OperationContainer&lt;OperationSupport&gt; container = createForDirectInstall();
 * OperationInfo&lt;Support&gt; info = container.add(element);
 * Set&lt;UpdateElement&gt; required = info.getRequiredElements();
 * container.add(required);
 * OperationSupport support = container.getSupport();
 * support.doOperation(null);
 * </pre>
 *
 * @param <Support> the type of support for performing chosen operation like 
 * {@link OperationSupport} or {@link InstallSupport}
 * @author Radek Matous, Jiri Rechtacek
 */
public final class OperationContainer<Support> {
    /**
     * The factory method to construct instance of <code>OperationContainer</code> for install operation
     * @return newly constructed instance of <code>OperationContainer</code> for install operation
     */
    public static OperationContainer<InstallSupport> createForInstall() {
        OperationContainer<InstallSupport> retval =
                new OperationContainer<InstallSupport>(OperationContainerImpl.createForInstall(), new InstallSupport());
        retval.getSupportInner ().setContainer(retval);
        return retval;
    }

    /**
     * The factory method to construct instance of <code>OperationContainer</code> for internal update operation
     * @return newly constructed instance of <code>OperationContainer</code> for internal update operation
     * @since 1.11
     */
    public static OperationContainer<InstallSupport> createForInternalUpdate() {
        OperationContainer<InstallSupport> retval =
                new OperationContainer<InstallSupport>(OperationContainerImpl.createForInternalUpdate(), new InstallSupport());
        retval.getSupportInner ().setContainer(retval);
        return retval;
    }

    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for install operation
     * @return newly constructed instance of <code>OperationContainer</code> for install operation
     */
    public static OperationContainer<OperationSupport> createForDirectInstall() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport> (OperationContainerImpl.createForDirectInstall(), new OperationSupport());
        retval.getSupportInner ().setContainer(retval);
        return retval;
    }    
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for update operation
     * @return newly constructed instance of <code>OperationContainer</code> for update operation
     */    
    public static OperationContainer<InstallSupport> createForUpdate() {
        OperationContainer<InstallSupport> retval =
                new OperationContainer<InstallSupport>(OperationContainerImpl.createForUpdate(), new InstallSupport());
        retval.getSupportInner ().setContainer(retval);
        return retval;
    }
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for update operation
     * @return newly constructed instance of <code>OperationContainer</code> for update operation
     */    
    public static OperationContainer<OperationSupport> createForDirectUpdate() {
        OperationContainerImpl<OperationSupport> implContainerForDirectUpdate = OperationContainerImpl.createForDirectUpdate();
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(implContainerForDirectUpdate, new OperationSupport());
        retval.getSupportInner ().setContainer(retval);
        return retval;
    }    
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for uninstall operation
     * @return newly constructed instance of <code>OperationContainer</code> for uninstall operation
     */        
    public static OperationContainer<OperationSupport> createForUninstall() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForUninstall(), new OperationSupport());
        retval.getSupportInner ().setContainer(retval);
        return retval;
    }
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for uninstall operation
     * @return newly constructed instance of <code>OperationContainer</code> for uninstall operation
     */            
    public static OperationContainer<OperationSupport> createForDirectUninstall() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForDirectUninstall(), new OperationSupport());
        retval.getSupportInner ().setContainer(retval);
        return retval;
    }
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for enable operation
     * @return newly constructed instance of <code>OperationContainer</code> for enable operation
     */            
    public static OperationContainer<OperationSupport> createForEnable() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForEnable(), new OperationSupport());
        retval.getSupportInner ().setContainer(retval);
        return retval;        
    }
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for disable operation
     * @return newly constructed instance of <code>OperationContainer</code> for disable operation
     */                
    public static OperationContainer<OperationSupport> createForDisable() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForDisable(), new OperationSupport());
        retval.getSupportInner ().setContainer(retval);
        return retval;
    }

    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for disable operation
     * @return newly constructed instance of <code>OperationContainer</code> for disable operation
     */                    
    public static OperationContainer<OperationSupport> createForDirectDisable() {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport>(OperationContainerImpl.createForDirectDisable(), new OperationSupport());
        retval.getSupportInner ().setContainer(retval);
        return retval;
    }
    
    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for installation of custom component
     * @return newly constructed instance of <code>OperationContainer</code> for installation of custom component
     */                    
    public static OperationContainer<OperationSupport> createForCustomInstallComponent () {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport> (OperationContainerImpl.createForInstallNativeComponent (), new OperationSupport());
        retval.getSupportInner ().setContainer (retval);
        return retval;
    }

    /**
     * The factory method to construct  instance of <code>OperationContainer</code> for uninstallation of custom component
     * @return newly constructed instance of <code>OperationContainer</code> for uninstallation of custom component
     */                        
    public static OperationContainer<OperationSupport> createForCustomUninstallComponent () {
        OperationContainer<OperationSupport> retval =
                new OperationContainer<OperationSupport> (OperationContainerImpl.createForUninstallNativeComponent (), new OperationSupport());
        retval.getSupportInner ().setContainer (retval);
        return retval;
    }
    
    /**
     * <p>See the difference between {@link #createForInstall} and {@link #createForDirectInstall} for example</p>
     * 
     * @return either {@link OperationSupport} or {@link InstallSupport} depending on type parameter of <code>OperationContainer&lt;Support&gt;</code> or
     * <code>null</code> if the <code>OperationContainer</code> is empty or contains any invalid elements
     * @see #listAll
     * @see #listInvalid
     *
     */                        
    public Support getSupport() {
        if (upToDate != null && upToDate) {
            return support;
        } else {
            if (listAll().size() > 0 && listInvalid().isEmpty()) {
                upToDate = true;
                return support;
            } else {
                return null;
            }
        }
    }
    
    Support getSupportInner () {
        return support;
    }
    
    /**
     * Check if <code>updateElement</code> can be added ({@link #add})
     * @param updateUnit
     * @param updateElement to be inserted.
     * @return <code>true</code> if chosen operation upon <code>updateElement</code> is allowed
     */
    public boolean canBeAdded(UpdateUnit updateUnit, UpdateElement updateElement) {
        return impl.isValid(updateUnit, updateElement);
    }
    
    /**
     * Adds all <code>elems</code>
     * @param elems to be inserted.
     */
    public void add(Collection<UpdateElement> elems) {
        if (elems == null) throw new IllegalArgumentException("Cannot add null value.");
        for (UpdateElement el : elems) {
            add(el);
        }
    }
    
    /**
     * Adds all <code>elems</code>
     * @param elems to be inserted.
     */
    public void add(Map<UpdateUnit, UpdateElement> elems) {
        if (elems == null) throw new IllegalArgumentException ("Cannot add null value.");
        for (Map.Entry<UpdateUnit, UpdateElement> entry : elems.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }
    
    
    /**
     * Adds <code>updateElement</code>
     * @param updateUnit
     * @param updateElement
     * @return instance of {@link OperationInfo}&lt;Support&gt; or
     * <code>null</code> if the <code>UpdateElement</code> is already present in the container
     */
    public OperationInfo<Support> add(UpdateUnit updateUnit,UpdateElement updateElement) {
        upToDate = false;
        return impl.add (updateUnit, updateElement);
    }
    
    /**
     * Adds <code>updateElement</code>
     * @param updateElement
     * @return instance of {@link OperationInfo}&lt;Support&gt; or
     * <code>null</code> if the <code>UpdateElement</code> is already present in the container
     */
    public OperationInfo<Support> add(UpdateElement updateElement) {
        upToDate = false;
        UpdateUnit updateUnit = updateElement.getUpdateUnit ();
        return add (updateUnit, updateElement);
    }
    
    
    /**
     * Removes all <code>elems</code>
     * @param elems
     */
    public void remove(Collection<UpdateElement> elems) {
        if (elems == null) throw new IllegalArgumentException ("Cannot add null value.");
        for (UpdateElement el : elems) {
            remove (el);
        }
    }        
    
    /**
     * Removes <code>updateElement</code>
     * @param updateElement
     * @return <code>true</code> if successfully added
     */
    public boolean remove(UpdateElement updateElement) {
        if (upToDate != null) {
            upToDate = false;
        }
        return impl.remove(updateElement);
    }
    
    
    /**
     * @param updateElement
     * @return <code>true</code> if this instance of <code>OperationContainer</code> 
     * contains the specified <code>updateElement</code>.     
     */
    public boolean contains(UpdateElement updateElement) {
        return impl.contains(updateElement);
    }

    /**
     * @return all instances of {@link OperationInfo}&lt;Support&gt; from this 
     * instance of <code>OperationContainer</code>
     */
    public List<OperationInfo<Support>> listAll() {
        return impl.listAllWithPossibleEager ();
    }
    
    /**
     * @return all invalid instances of {@link OperationInfo}&lt;Support&gt; from this 
     * instance of <code>OperationContainer</code>    
     */
    public List<OperationInfo<Support>> listInvalid() {
        return impl.listInvalid ();
    }

    
    /**
     * Removes <code>op</code>
     * @param op
     */
    public void remove(OperationInfo<Support> op) {
        if (upToDate != null) {
            upToDate = false;
        }
        impl.remove (op);
    }
    
    
    /**
     * Removes all content
     */
    public void removeAll() {
        if (upToDate != null) {
            upToDate = false;
        }
        impl.removeAll ();
    }
    
    /** Specifies location of unpack200 executable. {@code unpack200} has been
     * removed from JDK 14. As such it is not possible to unpack older NBM
     * files without providing alternative JDK implementation of this file.
     *
     * @param executable path to the executable
     * @since 1.65
     */
    public final void setUnpack200(File executable) {
        this.impl.setUnpack200(executable);
    }

    /**
     * Provides additional information
     * @param <Support> the type of support for performing chosen operation like 
     */
    public static final class OperationInfo<Support> {
        OperationContainerImpl<Support>.OperationInfoImpl<Support> impl;
        
        OperationInfo (OperationContainerImpl<Support>.OperationInfoImpl<Support> impl) {
            this.impl = impl;
        }
        
        public UpdateElement getUpdateElement() {return impl.getUpdateElement();}
        public UpdateUnit getUpdateUnit() {return impl.getUpdateUnit();}        
        /**
         * @return all required elements. Each of them represented by instance of <code>UpdateElement</code>
         */
        public Set<UpdateElement> getRequiredElements(){return new LinkedHashSet<UpdateElement> (impl.getRequiredElements());}
        
        /**
         * @return all broken dependencies. Each of them represented by the code name of the module 
         * @see ModuleInfo#getCodeNameBase()
         */
        public Set<String> getBrokenDependencies(){return impl.getBrokenDependencies();}
        
        /**
         * Reports parts missing from the installation. Will return codenames of required
         * unknown modules (e.g. from catalogs not fetched yet). Note differences to {@link #getBrokenDependencies()},
         * which report also broken requirements for packages or java version for specialized / optional modules.
         * @return set of missing parts (modules). If nothing is missing, returns empty set.
         * @since 1.57
         */
        public Set<String> getMissingParts() { return impl.getMissingParts(); }
        
        @Override
        public String toString () {
            return "OperationInfo: " + impl.getUpdateElement ().toString (); // NOI18N
        }
    }

    //end of API - next just impl details
    /** Creates a new instance of OperationContainer */
    private  OperationContainer(OperationContainerImpl<Support> impl, Support t) {
        this.impl = impl;
        this.support = t;
    }
    
    final OperationContainerImpl<Support> impl;
    private final Support support;
    private Boolean upToDate = null;
    
    @Override
    public String toString() {
        return super.toString() + "+" + impl;
    }
}
