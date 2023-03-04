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

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.*;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Radek Matous, Jiri Rechtacek
 */
public final class OperationContainerImpl<Support> {
    private boolean upToDate = false;
    private File unpack200;
    private OperationContainerImpl () {}
    public static final Logger LOGGER = Logger.getLogger (OperationContainerImpl.class.getName ());    
    private final List<OperationInfo<Support>> operations = new CopyOnWriteArrayList<OperationInfo<Support>>();
    private Throwable lastModified;
    private final Collection<OperationInfo<Support>> affectedEagers = new HashSet<OperationInfo<Support>> ();
    public static OperationContainerImpl<InstallSupport> createForInstall () {
        return new OperationContainerImpl<InstallSupport> (OperationType.INSTALL);
    }
    public static OperationContainerImpl<InstallSupport> createForInternalUpdate () {
        return new OperationContainerImpl<InstallSupport> (OperationType.INTERNAL_UPDATE);
    }
    public static OperationContainerImpl<InstallSupport> createForUpdate () {
        return new OperationContainerImpl<InstallSupport> (OperationType.UPDATE);
    }
    public static OperationContainerImpl<OperationSupport> createForDirectInstall() {
        OperationContainerImpl<OperationSupport> impl = new OperationContainerImpl<OperationSupport>(OperationType.INSTALL);
        impl.delegate = OperationContainer.createForUpdate();
        return impl;
    }

    public static OperationContainerImpl<OperationSupport> createForDirectUpdate() {
        OperationContainerImpl<OperationSupport> impl = new OperationContainerImpl<OperationSupport>(OperationType.UPDATE);
        impl.delegate = OperationContainer.createForUpdate();
        return impl;
    }
    public static OperationContainerImpl<OperationSupport> createForUninstall () {
        return new OperationContainerImpl<OperationSupport> (OperationType.UNINSTALL);
    }
    public static OperationContainerImpl<OperationSupport> createForDirectUninstall () {
        return new OperationContainerImpl<OperationSupport> (OperationType.DIRECT_UNINSTALL);
    }
    public static OperationContainerImpl<OperationSupport> createForEnable () {
        return new OperationContainerImpl<OperationSupport> (OperationType.ENABLE);
    }
    public static OperationContainerImpl<OperationSupport> createForDisable () {
        return new OperationContainerImpl<OperationSupport> (OperationType.DISABLE);
    }
    public static OperationContainerImpl<OperationSupport> createForDirectDisable () {
        return new OperationContainerImpl<OperationSupport> (OperationType.DIRECT_DISABLE);
    }
    public static OperationContainerImpl<OperationSupport> createForInstallNativeComponent () {
        return new OperationContainerImpl<OperationSupport> (OperationType.CUSTOM_INSTALL);
    }
    public static OperationContainerImpl<OperationSupport> createForUninstallNativeComponent () {
        return new OperationContainerImpl<OperationSupport> (OperationType.CUSTOM_UNINSTALL);
    }
    @SuppressWarnings({"unchecked"})
    public OperationInfo<Support> add (UpdateUnit updateUnit, UpdateElement updateElement) throws IllegalArgumentException {
        OperationInfo<Support> retval = null;
        boolean isValid = isValid (updateUnit, updateElement);
        if (UpdateUnitFactory.getDefault().isScheduledForRestart (updateElement)) {
            LOGGER.log (Level.INFO, updateElement + " is scheduled for restart IDE.");
            throw new IllegalArgumentException (updateElement + " is scheduled for restart IDE.");
        }
        if (!isValid) {
            throw new IllegalArgumentException("Invalid " + updateUnit + " for operation " + type);
        }
        if (isValid) {
            switch (type) {
            case UNINSTALL :
            case DIRECT_UNINSTALL :
            case CUSTOM_UNINSTALL :
            case ENABLE :
            case DISABLE :
            case DIRECT_DISABLE :
                if (updateUnit.getInstalled () != updateElement) {
                    throw new IllegalArgumentException (updateUnit.getInstalled () +
                            " and " + updateElement + " must be same for operation " + type);
                }
                break;
            case INSTALL :
            case UPDATE :
            case CUSTOM_INSTALL:
                if (updateUnit.getInstalled () == updateElement) {
                    throw new IllegalArgumentException (updateUnit.getInstalled () +
                            " and " + updateElement + " cannot be same for operation " + type);
                }
                break;
            case INTERNAL_UPDATE:
                /*
                if (updateUnit.getInstalled () != updateElement) {
                    throw new IllegalArgumentException (updateUnit.getInstalled () +
                            " and " + updateElement + " must be same for operation " + type);
                }*/
                break;
            default:
                assert false : "Unknown type of operation " + type;
            }
        }
        synchronized(this) {
            if (!contains (updateUnit, updateElement)) {
                retval = Trampoline.API.createOperationInfo (new OperationInfoImpl<Support> (updateUnit, updateElement));
                assert retval != null : "Null support for " + updateUnit + " and " + updateElement;
                changeState (operations.add (retval));
                boolean asserts = false;
                assert asserts = true;
                if (asserts) {
                    lastModified = new Exception("Added operation: " + retval);
                }
            }
        }
        return retval;
    }
    public boolean remove (UpdateElement updateElement) {
        OperationInfo toRemove = find (updateElement);
        if (toRemove != null) {
            remove (toRemove);
        }
        return toRemove != null;
    }
    
    public boolean contains (UpdateElement updateElement) {
        return find (updateElement) != null;
    }
    
    private OperationInfo<Support> find (UpdateElement updateElement) {
        OperationInfo<Support> toRemove = null;
        for (OperationInfo<Support> info : listAll ()) {
            if (info.getUpdateElement ().equals (updateElement)) {
                toRemove = info;
                break;
            }
        }
        return toRemove;
    }
    
    private boolean contains (UpdateUnit unit, UpdateElement element) {
        List<OperationInfo<Support>> infos = operations;
        for (OperationInfo info : infos) {
            if (info.getUpdateElement ().equals (element) ||
                    info.getUpdateUnit ().equals (unit)) {
                return true;
            }
        }
        return false;
    }
    
    private List<OperationInfo<Support>> listAll () {
        return Collections.unmodifiableList(operations);
    }
    
    public synchronized List<OperationInfo<Support>> listAllWithPossibleEager () {
        if (upToDate) {
            return listAll();
        }
            
        clearCache ();

        //if operations contains only first class modules - don`t search for eagers.
        boolean checkEagers = false;
        for (OperationInfo<?> i : operations) {
            if(!Utilities.isFirstClassModule(i.getUpdateElement())) {
               checkEagers = true;
               break;
            }
        }
        // handle eager modules

        if ((type == OperationType.INSTALL || type == OperationType.UPDATE || type==OperationType.INTERNAL_UPDATE) && checkEagers) {
            Collection<UpdateElement> all = new HashSet<UpdateElement> (operations.size ());
            for (OperationInfo<?> i : operations) {
                all.add(i.getUpdateElement());
            }
            for (OperationInfo<?> i : operations) {
                all.addAll(i.getRequiredElements());
            }
            // TODO: fragment modules are somewhat eager: they need to enable with their hosting module. They are not handled now,
            // so unless they are also eager, they won't be autoincluded.
            for (UpdateElement eagerEl : UpdateManagerImpl.getInstance ().getAvailableEagers ()) {
                if(eagerEl.getUpdateUnit().isPending() || eagerEl.getUpdateUnit().getAvailableUpdates().isEmpty()) {
                    continue;
                }
                UpdateElementImpl impl = Trampoline.API.impl (eagerEl);
                List <ModuleInfo> infos = new ArrayList <ModuleInfo>();
                if(impl instanceof ModuleUpdateElementImpl) {
                    ModuleUpdateElementImpl eagerImpl = (ModuleUpdateElementImpl) impl;
                    infos.add(eagerImpl.getModuleInfo ());
                } else if (impl instanceof FeatureUpdateElementImpl) {
                    FeatureUpdateElementImpl eagerImpl = (FeatureUpdateElementImpl) impl;
                    infos.addAll(eagerImpl.getModuleInfos ());
                } else {
                    assert false : eagerEl + " must instanceof ModuleUpdateElementImpl or FeatureUpdateElementImpl";
                }

                for(ModuleInfo mi: infos) {
                    Set<UpdateElement> reqs = new HashSet<UpdateElement> ();
                    for (Dependency dep : mi.getDependencies ()) {
                        Collection<UpdateElement> requestedElements = Utilities.handleDependency (eagerEl, dep, Collections.singleton (mi), new HashSet<Dependency> (), 
                                type == OperationType.UPDATE || type == OperationType.INTERNAL_UPDATE);
                        if (requestedElements != null) {
                            for (UpdateElement req : requestedElements) {
                                reqs.add (req);
                            }
                        }
                    }
                    if ((! reqs.isEmpty() && all.containsAll(reqs) && ! all.contains (eagerEl)) ||
                            (reqs.isEmpty() && impl.getUpdateUnit().getInstalled()!=null && type == OperationType.UPDATE && operations.size() > 0)) {
                        // adds affectedEager into list of elements for the operation
                        OperationInfo<Support> i = null;
                        try {
                            if(impl instanceof ModuleUpdateElementImpl) {
                                i = add (eagerEl.getUpdateUnit (), eagerEl);
                            } else if (impl instanceof FeatureUpdateElementImpl) {
                                FeatureUpdateElementImpl eagerImpl = (FeatureUpdateElementImpl) impl;
                                for (UpdateElementImpl contained : eagerImpl.getContainedModuleElements()) {
                                    if (contained.isEager()) {
                                        i = add (contained.getUpdateUnit (), contained.getUpdateElement());
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            //investigate the reason of 172220, 171975, 169588
                            boolean firstCondition = (! reqs.isEmpty() && all.containsAll (reqs) && ! all.contains (eagerEl));
                            boolean secondCondition = reqs.isEmpty() && impl.getUpdateUnit().getInstalled()!=null && type == OperationType.UPDATE && operations.size() > 0;
                            StringBuilder sb = new StringBuilder();
                            sb.append("\nIAE while adding eager element to the ").append(type).append(" container\n");
                            sb.append("\nEager: ").append(eagerEl);
                            sb.append("\nFirst condition : ").append(firstCondition);
                            sb.append("\nSecond condition : ").append(secondCondition);
                            sb.append("\nInstalled: ").append(impl.getUpdateUnit().getInstalled());
                            sb.append("\nPending: ").append(impl.getUpdateUnit().isPending());
                            sb.append("\nreqs: ").append(reqs).append(" (total : ").append(reqs.size()).append(")");
                            sb.append("\nall: ").append(all).append(" (total : ").append(all.size()).append(")");                            
                            sb.append("\noperation: ").append(operations).append(" (total: ").append(operations.size());
                            sb.append("\neager available updates: ").append(eagerEl.getUpdateUnit().getAvailableUpdates());
                            sb.append("\nUpdateElements in operations:");
                            for (OperationInfo<?> op : operations) {
                                sb.append("\n  ").append(op.getUpdateElement());
                            }
                            sb.append("\nUpdateElements in all:");
                            for (UpdateElement elem : all) {
                                sb.append("\n  ").append(elem);
                            }
                            sb.append("\n");
                            LOGGER.log(Level.INFO, sb.toString(), e);
                            throw e;
                        }
                        if (i != null) {
                            affectedEagers.add (i);
                        }
                    }
                }
            }
        }
        if (LOGGER.isLoggable (Level.FINE)) {
            LOGGER.log (Level.FINE, "== do listAllWithPossibleEager for " + type + " operation ==");
            for (OperationInfo info : operations) {
                LOGGER.log (Level.FINE, "--> " + info.getUpdateElement ());
            }
            if (affectedEagers != null) {
                LOGGER.log (Level.FINE, "   == includes affected eagers for " + type + " operation ==");
                for (OperationInfo eagerInfo : affectedEagers) {
                    LOGGER.log (Level.FINE, "   --> " + eagerInfo.getUpdateElement ());
                }
                LOGGER.log (Level.FINE, "   == done eagers. ==");
            }
            LOGGER.log (Level.FINE, "== done. ==");
        }
        upToDate = true;
        return listAll();
    }
    
    public List<OperationInfo<Support>> listInvalid () {
        List<OperationInfo<Support>> retval = new ArrayList<OperationInfo<Support>>();
        List<OperationInfo<Support>> infos = listAll ();
        for (OperationInfo<Support> oii: infos) {
            // find type of operation
            // differ primary element and required elements
            // primary use-case can be Install but could required update of other elements
            if (!isValid (oii.getUpdateUnit (), oii.getUpdateElement ())) {
                retval.add (oii);
            }
        }
        return retval;
    }
    
    public boolean isValid (UpdateUnit updateUnit, UpdateElement updateElement) {
        if (updateElement == null) {
            throw new IllegalArgumentException ("UpdateElement cannot be null for UpdateUnit " + updateUnit);
        } else if (updateUnit == null) {
            throw new IllegalArgumentException ("UpdateUnit cannot be null for UpdateElement " + updateElement);
        }
        boolean isValid;
        switch (type) {
        case INSTALL :
            isValid = OperationValidator.isValidOperation (type, updateUnit, updateElement);
            // at least first add must pass and respect type of operation
            if (! isValid && operations.size () > 0) {
                // try Update
                isValid = OperationValidator.isValidOperation (OperationType.UPDATE, updateUnit, updateElement);
            }
            break;
        case UPDATE :
            isValid = OperationValidator.isValidOperation (type, updateUnit, updateElement);
            // at least first add must pass and respect type of operation
            if (! isValid && operations.size () > 0) {
                // try Update
                isValid = OperationValidator.isValidOperation (OperationType.INSTALL, updateUnit, updateElement);
            }
            break;
        case INTERNAL_UPDATE:
            isValid = OperationValidator.isValidOperation (type, updateUnit, updateElement);
            // at least first add must pass and respect type of operation
            if (! isValid && operations.size () > 0) {
                // try Update
                isValid = OperationValidator.isValidOperation (OperationType.UPDATE, updateUnit, updateElement);
            }
            if (! isValid && operations.size () > 0) {
                // try Install
                isValid = OperationValidator.isValidOperation (OperationType.INSTALL, updateUnit, updateElement);
            }
            break;

        default:
            isValid = OperationValidator.isValidOperation (type, updateUnit, updateElement);
        }
        
        return isValid;
    }
    
    public synchronized void remove (OperationInfo op) {
        synchronized(this) {
            changeState (operations.remove (op));
            changeState (operations.removeAll (affectedEagers));
            affectedEagers.clear ();
            boolean asserts = false;
            assert asserts = true;
            if (asserts) {
                lastModified = new Exception("Removed " + op); // NOI18N
            }
        }
    }
    public synchronized void removeAll () {
        synchronized(this) {
            changeState (true);
            operations.clear ();
            affectedEagers.clear ();
            boolean asserts = false;
            assert asserts = true;
            if (asserts) {
                lastModified = new Exception("Removed all"); // NOI18N
            }
        }
    }

    @Override
    public String toString() {
        StringWriter sb = new StringWriter();
        PrintWriter pw = new PrintWriter(sb);
        pw.print(super.toString());
        if (lastModified != null) {
            pw.println();
            lastModified.printStackTrace(pw);
        }
        pw.flush();
        return sb.toString();
    }
    
    private void clearCache () {
        OperationValidator.clearMaps ();
    }
    
    private void changeState (boolean changed) {
        if (changed) {
            clearCache ();
        }
        upToDate = upToDate && ! changed;
    }
    
    public class OperationInfoImpl<Support> {
        private final UpdateElement updateElement;
        private final UpdateUnit uUnit;
        private Set<String> brokenDeps = null;
        private OperationInfoImpl (UpdateUnit uUnit, UpdateElement updateElement) {
            this.updateElement = updateElement;
            this.uUnit = uUnit;
        }
        public UpdateElement/*or null*/ getUpdateElement () {
            return updateElement;
        }
        public UpdateUnit/*or null*/ getUpdateUnit () {
            return uUnit;
        }
        private List<UpdateElement> requiredElements;
        public List<UpdateElement> getRequiredElements (){
            if (upToDate && requiredElements != null) {
                return requiredElements;
            }
            List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();
            for (OperationContainer.OperationInfo oii : listAll ()) {
                UpdateElementImpl impl = Trampoline.API.impl (oii.getUpdateElement ());
                List<ModuleInfo> infos = impl.getModuleInfos ();
                assert infos != null : "ModuleInfo for UpdateElement " + oii.getUpdateElement () + " found.";
                moduleInfos.addAll (infos);
            }
            brokenDeps = new HashSet<String> ();
            Set<UpdateElement> recommeded = new HashSet<UpdateElement>();
            requiredElements = OperationValidator.getRequiredElements (type, getUpdateElement (), moduleInfos, brokenDeps, recommeded);
            if (! brokenDeps.isEmpty() && ! recommeded.isEmpty()) {
                brokenDeps = new HashSet<String> ();
                requiredElements = OperationValidator.getRequiredElements (type, getUpdateElement (), moduleInfos, brokenDeps, recommeded);
            }
            return requiredElements;
        }
        
        public Set<String> getBrokenDependencies () {
            if (! upToDate) {
                brokenDeps = null;
            }
            if (brokenDeps != null) {
                return brokenDeps;
            }
            // shortcut, because OperationValidator.getRequiredElements is NOT consistent with
            // OperationValidator.getBrokenDependencies: while getRequiredElements ignores breakages of 
            // feature contents, getBrokenDependencies wille verify them in full, so feature's optional (platform-specific) module
            // will report the whole feature as broken on activation or install.
            if (requiredElements != null) {
                getRequiredElements();
                if (brokenDeps != null) {
                    return brokenDeps;
                }
            }
            List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();
            Set<String> brokenContents = new HashSet<>();
            for (OperationContainer.OperationInfo oii : listAll ()) {
                UpdateElementImpl impl = Trampoline.API.impl (oii.getUpdateElement ());
                Collection<ModuleInfo> infos = impl.getModuleInfos ();
                assert infos != null : "ModuleInfo for UpdateElement " + oii.getUpdateElement () + " found.";
                moduleInfos.addAll (infos);
                if (impl.getType() == UpdateManager.TYPE.FEATURE) {
                    // add unknown tokens / codenames as broken content.
                    brokenContents.addAll(((FeatureUpdateElementImpl)impl).getMissingElements());
                }
            }
            brokenContents.addAll(OperationValidator.getBrokenDependencies (type, getUpdateElement (), moduleInfos));
            return brokenContents;
        }
        
        /**
         * Reports missing parts of the feature.
         * @return missing/unknown directly contained codenames
         */
        public Set<String> getMissingParts() {
            Set<String> brokenContents = new HashSet<>();
            for (OperationContainer.OperationInfo oii : listAll ()) {
                UpdateElementImpl impl = Trampoline.API.impl (oii.getUpdateElement ());
                if (impl instanceof FeatureUpdateElementImpl) {
                    brokenContents.addAll(((FeatureUpdateElementImpl)impl).getMissingElements());
                }
            }
            return brokenContents;
        }
    }
    
    /** Creates a new instance of OperationContainer */
    private OperationContainerImpl (OperationType type) {
        this.type = type;
    }
        
    public OperationType getType () {
        return type;
    }
    
    public static enum OperationType {
        /** Install <code>UpdateElement</code> */
        INSTALL,
        /** Uninstall <code>UpdateElement</code> */
        UNINSTALL,
        /** Internally update installed <code>UpdateElement</code> without version increase */
        INTERNAL_UPDATE,
        /** Uninstall <code>UpdateElement</code> on-the-fly */
        DIRECT_UNINSTALL,
        /** Update installed <code>UpdateElement</code> to newer version. */
        UPDATE,
        /** Rollback installed <code>UpdateElement</code> to previous version. */
        REVERT,
        /** Enable <code>UpdateElement</code> */
        ENABLE,
        /** Disable <code>UpdateElement</code> */
        DIRECT_DISABLE,
        /** Disable <code>UpdateElement</code> on-the-fly */
        DISABLE,
        /** Install <code>UpdateElement</code> with custom installer. */
        CUSTOM_INSTALL,
        /** Uninstall <code>UpdateElement</code> with custom installer. */
        CUSTOM_UNINSTALL
    }
    private OperationType type;
    private OperationContainer delegate;

    /**
     * @return the unpack200 executable or {@code null}
     */
    public final File getUnpack200() {
        NO_PACK: if (unpack200 == null) {
            final String jreHome = System.getProperty("java.home"); // NOI18N
            if (jreHome == null) {
                break NO_PACK;
            }
            File javaHome = new File(jreHome);
            File pack200ux = new File(new File(javaHome, "bin"), "unpack200"); // NOI18N
            if (pack200ux.canExecute()) {
                return pack200ux;
            }
            File pack200exe = new File(new File(javaHome, "bin"), "unpack200.exe"); // NOI18N
            if (pack200exe.canExecute()) {
                return pack200exe;
            }
        }
        return unpack200;
    }

    /**
     * @param pack200 the pack200 to set
     */
    public final void setUnpack200(File pack200) {
        this.unpack200 = pack200;
    }
}
