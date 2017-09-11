/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.*;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.spi.autoupdate.CustomInstaller;
import org.netbeans.spi.autoupdate.CustomUninstaller;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInfo;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 * @author Jiri Rechtacek, Radek Matous
 */
public abstract class OperationSupportImpl {
    private static final OperationSupportImpl FOR_INSTALL = new ForInstall();
    private static final OperationSupportImpl FOR_ENABLE = new ForEnable();
    private static final OperationSupportImpl FOR_DISABLE = new ForDisable();
    private static final OperationSupportImpl FOR_DIRECT_DISABLE = new ForDirectDisable();
    private static final OperationSupportImpl FOR_UNINSTALL = new ForUninstall();
    private static final OperationSupportImpl FOR_DIRECT_UNINSTALL = new ForDirectUninstall();
    private static final OperationSupportImpl FOR_CUSTOM_INSTALL = new ForCustomInstall ();
    private static final OperationSupportImpl FOR_CUSTOM_UNINSTALL = new ForCustomUninstall ();
    
    private static final Logger LOGGER = Logger.getLogger ("org.netbeans.modules.autoupdate.services.OperationSupportImpl");
    
    public static OperationSupportImpl forInstall() {
        return FOR_INSTALL;
    }
    public static OperationSupportImpl forUninstall() {
        return FOR_UNINSTALL;
    }
    public static OperationSupportImpl forDirectUninstall() {
        return FOR_DIRECT_UNINSTALL;
    }
    public static OperationSupportImpl forEnable() {
        return FOR_ENABLE;
    }
    public static OperationSupportImpl forDisable() {
        return FOR_DISABLE;
    }
    public static OperationSupportImpl forDirectDisable() {
        return FOR_DIRECT_DISABLE;
    }
    public static OperationSupportImpl forCustomInstall () {
        return FOR_CUSTOM_INSTALL;
    }
    public static OperationSupportImpl forCustomUninstall () {
        return FOR_CUSTOM_UNINSTALL;
    }
    
    public abstract Boolean doOperation(ProgressHandle progress/*or null*/, OperationContainer<?> container) throws OperationException;
    public abstract void doCancel () throws OperationException;
    public abstract void doRestart (Restarter restarter, ProgressHandle progress) throws OperationException;
    public abstract void doRestartLater (Restarter restarter);
            
    /** Creates a new instance of OperationContainer */
    private OperationSupportImpl() {
    }
    
    private static class ForEnable extends OperationSupportImpl {
        @Override 
        public synchronized Boolean doOperation(ProgressHandle progress,
                OperationContainer<?> container) throws OperationException {
           
            boolean needsRestart = false;
            
            try {
                if (progress != null) {
                    progress.start();
                }
                
                ModuleManager mm = null;
                List<? extends OperationInfo> elements = container.listAll();
                Set<ModuleInfo> moduleInfos = new HashSet<ModuleInfo> ();
                for (OperationInfo operationInfo : elements) {
                    UpdateElementImpl impl = Trampoline.API.impl (operationInfo.getUpdateElement ());
                    moduleInfos.addAll(impl.getModuleInfos(true));
                }
                final Set<Module> modules = new HashSet<Module>();
                for (ModuleInfo info : moduleInfos) {
                    Module m = Utilities.toModule (info);
                    if (Utilities.canEnable (m)) {
                        modules.add(m);
                        LOGGER.log (Level.FINE, "Module will be enabled " + m.getCodeNameBase ());
                    }
                    if (mm == null) {
                        mm = m.getManager();
                    }
                }                
                
                assert mm != null;
                
                needsRestart = mm.hasToEnableCompatModules(modules);
                
                final ModuleManager fmm = mm;
                try {
                    fmm.mutex ().writeAccess (new ExceptionAction<Boolean> () {
                        @Override
                        public Boolean run () throws Exception {
                            return enable(fmm, modules);
                        }
                    });
                } catch (MutexException ex) {
                    Exception x = ex.getException ();
                    assert x instanceof OperationException : x + " is instanceof OperationException";
                    if (x instanceof OperationException) {
                        throw (OperationException) x;
                    }
                }
            } finally {
                if (progress != null) {
                    progress.finish();
                }
            }
            
            return needsRestart;
        }
        
        @Override
        public void doCancel () throws OperationException {
            assert false : "Not supported yet";
        }
        
        private static boolean enable(ModuleManager mm, Set<Module> toRun) throws OperationException {
            boolean retval = false;
            try {
                mm.enable(toRun);
                retval = true;
            } catch(IllegalArgumentException ilae) {
                throw new OperationException(OperationException.ERROR_TYPE.ENABLE, ilae);
            } catch(InvalidException ie) {
                throw new OperationException(OperationException.ERROR_TYPE.ENABLE, ie);
            }
            return retval;
        }

        @Override
        public void doRestart (Restarter restarter, ProgressHandle progress) throws OperationException {
            LifecycleManager.getDefault().markForRestart();
            LifecycleManager.getDefault().exit();
        }

        @Override
        public void doRestartLater (Restarter restarter) {
            LifecycleManager.getDefault().markForRestart();
        }        
    }
    
    private static class ForDisable extends OperationSupportImpl {
        private Collection<File> controlFileForDisable = null;
        private Collection<UpdateElement> affectedModules = null;
        @Override
        public synchronized Boolean doOperation(ProgressHandle progress,
                OperationContainer<?> container) throws OperationException {
            try {
                if (progress != null) {
                    progress.start();
                }
                ModuleManager mm = null;
                List<? extends OperationInfo> elements = container.listAll();
                affectedModules = new HashSet<UpdateElement> ();
                Set<ModuleInfo> moduleInfos = new HashSet<ModuleInfo> ();
                for (OperationInfo operationInfo : elements) {
                    UpdateElementImpl impl = Trampoline.API.impl (operationInfo.getUpdateElement ());
                    affectedModules.add (operationInfo.getUpdateElement ());
                    moduleInfos.addAll (impl.getModuleInfos ());
                }
                Set<ModuleInfo> modules = new HashSet<ModuleInfo> ();
                for (ModuleInfo info : moduleInfos) {
                    Module m = Utilities.toModule (info);
                    if (Utilities.canDisable (m)) {
                        modules.add(m);
                        LOGGER.log (Level.FINE, "Mark module " + m.getCodeNameBase () + " for disable.");
                    }
                    if (mm == null) {
                        mm = m.getManager();
                    }
                }
                assert mm != null;
                ModuleDeleterImpl deleter = new ModuleDeleterImpl ();
                controlFileForDisable = deleter.markForDisable (modules, progress);
            } finally {
                if (progress != null) {
                    progress.finish();
                }
            }
            
            return true;
        }
        
        @Override
        public void doCancel () throws OperationException {
            if (controlFileForDisable != null) {
                controlFileForDisable = null;
            }
            if (affectedModules != null) {
                affectedModules = null;
            }
        }

        @Override
        public void doRestart (Restarter restarter, ProgressHandle progress) throws OperationException {
            // write files marked to delete (files4remove) into temp file
            // Updater will handle it
            Utilities.writeFileMarkedForDisable (controlFileForDisable);
            
            // restart IDE
            Utilities.deleteAllDoLater ();
            LifecycleManager.getDefault ().exit ();
            // if exit&restart fails => use restart later as fallback
            doRestartLater (restarter);
        }

        @Override
        public void doRestartLater (Restarter restarter) {
            // write files marked to delete (files4remove) into temp file
            // Updater will handle it
            Utilities.writeFileMarkedForDisable (controlFileForDisable);
            
            // shedule module for restart
            for (UpdateElement el : affectedModules) {
                UpdateUnitFactory.getDefault().scheduleForRestart (el);
            }
            
            // write deactivate_later.txt
            Utilities.writeDeactivateLater (controlFileForDisable);
        }
        
    }
    
    private static class ForDirectDisable extends OperationSupportImpl {
        @Override
        public synchronized Boolean doOperation(ProgressHandle progress,
                OperationContainer<?> container) throws OperationException {
            try {
                if (progress != null) {
                    progress.start();
                }
                ModuleManager mm = null;
                List<? extends OperationInfo> elements = container.listAll();
                Set<ModuleInfo> moduleInfos = new HashSet<ModuleInfo> ();
                for (OperationInfo operationInfo : elements) {
                    UpdateElementImpl impl = Trampoline.API.impl (operationInfo.getUpdateElement ());
                    moduleInfos.addAll (impl.getModuleInfos ());
                }
                final Set<Module> modules = new HashSet<Module>();
                for (ModuleInfo info : moduleInfos) {
                    Module m = Utilities.toModule (info);
                    if (Utilities.canDisable (m)) {
                        modules.add(m);
                        LOGGER.log (Level.FINE, "Module will be disabled " + m.getCodeNameBase ());
                    }
                    if (mm == null) {
                        mm = m.getManager();
                    }
                }
                assert mm != null;
                final ModuleManager fmm = mm;
                try {
                    fmm.mutex ().writeAccess (new ExceptionAction<Boolean> () {
                        @Override
                        public Boolean run () throws Exception {
                            return disable(fmm, modules);
                        }
                    });
                } catch (MutexException ex) {
                    Exception x = ex.getException ();
                    assert x instanceof OperationException : x + " is instanceof OperationException";
                    if (x instanceof OperationException) {
                        throw (OperationException) x;
                    }
                }
            } finally {
                if (progress != null) {
                    progress.finish();
                }
            }
            
            return false;
        }
        
        private static boolean disable (ModuleManager mm, Set<Module> toRun) throws OperationException {
            boolean retval = false;
            try {
                mm.disable (toRun);
                retval = true;
            } catch(IllegalArgumentException ilae) {
                throw new OperationException(OperationException.ERROR_TYPE.ENABLE, ilae);
            }
            return retval;
        }

        @Override
        public void doCancel () throws OperationException {
            assert false : "Not supported yet";
        }

        @Override
        public void doRestart (Restarter restarter, ProgressHandle progress) throws OperationException {
            throw new UnsupportedOperationException ("Not supported yet.");
        }

        @Override
        public void doRestartLater (Restarter restarter) {
            throw new UnsupportedOperationException ("Not supported yet.");
        }
        
    }
    
    private static class ForUninstall extends OperationSupportImpl {
        private Collection<File> files4remove = null;
        private Collection<UpdateElement> affectedModules = null;
        @Override
        public synchronized Boolean doOperation(ProgressHandle progress,
                OperationContainer<?> container) throws OperationException {
            try {
                if (progress != null) {
                    progress.start();
                }
                ModuleDeleterImpl deleter = new ModuleDeleterImpl();
                
                List<? extends OperationInfo> infos = container.listAll ();
                Set<ModuleInfo> moduleInfos = new HashSet<ModuleInfo> ();
                affectedModules = new HashSet<UpdateElement> ();
                for (OperationInfo operationInfo : infos) {
                    UpdateElement updateElement = operationInfo.getUpdateElement ();
                    UpdateElementImpl updateElementImpl = Trampoline.API.impl (updateElement);
                    switch (updateElementImpl.getType ()) {
                    case KIT_MODULE :    
                    case MODULE :
                        moduleInfos.add (((ModuleUpdateElementImpl) updateElementImpl).getModuleInfo ());
                        affectedModules.add (updateElementImpl.getUpdateElement ());
                        break;
                    case STANDALONE_MODULE :
                    case FEATURE :
                        for (ModuleUpdateElementImpl moduleImpl : ((FeatureUpdateElementImpl) updateElementImpl).getContainedModuleElements ()) {
                            moduleInfos.add (moduleImpl.getModuleInfo ());
                            if (moduleImpl.getUpdateUnit ().getInstalled () != null) {
                                affectedModules.add (moduleImpl.getUpdateElement ());
                            }
                        }
                        break;
                    case CUSTOM_HANDLED_COMPONENT :

                        break;
                    default:
                        assert false : "Not supported for impl " + updateElementImpl;
                    }
                }
                try {
                    files4remove = deleter.markForDelete (moduleInfos, progress);
                } catch(IOException iex) {
                    throw new OperationException(OperationException.ERROR_TYPE.UNINSTALL, iex);
                }

            } finally {
                if (progress != null) {
                    progress.finish();
                }
            }

            return true;
        }
        @Override
        public void doCancel () throws OperationException {
            if (files4remove != null) {
                files4remove = null;
            }
            if (affectedModules != null) {
                affectedModules = null;
            }
        }

        @Override
        public void doRestart (Restarter restarter, ProgressHandle progress) throws OperationException {
            // write files marked to delete (files4remove) into temp file
            // Updater will handle it
            Utilities.writeFileMarkedForDelete (files4remove);
            
            // restart IDE
            Utilities.deleteAllDoLater ();
            LifecycleManager.getDefault ().exit ();
            // if exit&restart fails => use restart later as fallback
            doRestartLater (restarter);
        }

        @Override
        public void doRestartLater (Restarter restarter) {
            // write files marked to delete (files4remove) into temp file
            // Updater will handle it
            Utilities.writeFileMarkedForDelete (files4remove);
            
            // shedule module for restart
            for (UpdateElement el : affectedModules) {
                UpdateUnitFactory.getDefault().scheduleForRestart (el);
            }
            
            // write deactivate_later.txt
            Utilities.writeDeactivateLater (files4remove);
        }
        
    }
    
    private static class ForDirectUninstall extends OperationSupportImpl {
        @Override
        public synchronized Boolean doOperation(ProgressHandle progress,
                OperationContainer<?> container) throws OperationException {
            try {
                if (progress != null) {
                    progress.start();
                }
                ModuleDeleterImpl deleter = new ModuleDeleterImpl();
                
                List<? extends OperationInfo> infos = container.listAll ();
                Set<ModuleInfo> moduleInfos = new HashSet<ModuleInfo> ();
                Set<UpdateUnit> affectedModules = new HashSet<UpdateUnit> ();
                Set<UpdateUnit> affectedFeatures = new HashSet<UpdateUnit> ();
                for (OperationInfo operationInfo : infos) {
                    UpdateElement updateElement = operationInfo.getUpdateElement ();
                    UpdateElementImpl updateElementImpl = Trampoline.API.impl (updateElement);
                    switch (updateElementImpl.getType ()) {
                    case KIT_MODULE :    
                    case MODULE :
                        moduleInfos.add (((ModuleUpdateElementImpl) updateElementImpl).getModuleInfo ());
                        affectedModules.add (updateElementImpl.getUpdateUnit ());
                        break;
                    case STANDALONE_MODULE :
                    case FEATURE :
                        for (ModuleUpdateElementImpl moduleImpl : ((FeatureUpdateElementImpl) updateElementImpl).getContainedModuleElements ()) {
                            moduleInfos.add (moduleImpl.getModuleInfo ());
                            if (moduleImpl.getUpdateUnit ().getInstalled () != null) {
                                affectedModules.add (moduleImpl.getUpdateUnit ());
                            }
                        }
                        affectedFeatures.add (updateElement.getUpdateUnit ());
                        break;
                    default:
                        assert false : "Not supported for impl " + updateElementImpl;
                    }
                }
                try {
                    deleter.delete (moduleInfos.toArray (new ModuleInfo[0]), progress);
                } catch(IOException iex) {
                    throw new OperationException(OperationException.ERROR_TYPE.UNINSTALL, iex);
                }
                
                for (UpdateUnit unit : affectedModules) {
                    assert unit.getInstalled () != null : "Module " + unit + " is installed while doing uninstall.";
                    LOGGER.log (Level.FINE, "Module was uninstalled " + unit.getCodeName ());
                    UpdateUnitImpl impl = Trampoline.API.impl (unit);
                    impl.setAsUninstalled();
                }
                for (UpdateUnit unit : affectedFeatures) {
                    assert unit.getInstalled () != null : "Feature " + unit + " is installed while doing uninstall.";
                    LOGGER.log (Level.FINE, "Feature was uninstalled " + unit.getCodeName ());
                    UpdateUnitImpl impl = Trampoline.API.impl (unit);
                    impl.setAsUninstalled();
                }
            } finally {
                if (progress != null) {
                    progress.finish();
                }
            }
            
            return false;
        }
        @Override
        public void doCancel () throws OperationException {
            assert false : "Not supported yet";
        }

        @Override
        public void doRestart (Restarter restarter, ProgressHandle progress) throws OperationException {
            throw new UnsupportedOperationException ("Not supported yet.");
        }

        @Override
        public void doRestartLater (Restarter restarter) {
            throw new UnsupportedOperationException ("Not supported yet.");
        }
        
    }
    
    private static class ForInstall extends OperationSupportImpl {
        @Override
        public synchronized Boolean doOperation(ProgressHandle progress,
                OperationContainer container) throws OperationException {
            
            OperationContainer<InstallSupport> containerForUpdate = OperationContainer.createForUpdate();
            List<? extends OperationInfo> infos = container.listAll();
            for (OperationInfo info : infos) {
                containerForUpdate.add(info.getUpdateUnit(), info.getUpdateElement());
            }
            assert containerForUpdate.listInvalid().isEmpty();
            
            Validator v = containerForUpdate.getSupport().doDownload(ProgressHandleFactory.createHandle(OperationSupportImpl.class.getName()), null, false);
            Installer i = containerForUpdate.getSupport().doValidate(v, ProgressHandleFactory.createHandle(OperationSupportImpl.class.getName()));
            InstallSupportImpl installSupportImpl = Trampoline.API.impl(containerForUpdate.getSupport());
            Boolean needRestart = installSupportImpl.doInstall(i, ProgressHandleFactory.createHandle(OperationSupportImpl.class.getName()), true);
            return needRestart;
        }
        @Override
        public void doCancel () throws OperationException {
            assert false : "Not supported yet";
        }

        @Override
        public void doRestart (Restarter restarter, ProgressHandle progress) throws OperationException {
            throw new UnsupportedOperationException ("Not supported yet.");
        }

        @Override
        public void doRestartLater (Restarter restarter) {
            throw new UnsupportedOperationException ("Not supported yet.");
        }
        
    }
    
    private static class ForCustomInstall extends OperationSupportImpl {
        private Collection<UpdateElement> affectedModules = null;

        @Override
        public synchronized Boolean doOperation(ProgressHandle progress,
                OperationContainer<?> container) throws OperationException {
            boolean success = false;
            boolean started = false;
            try {                
                List<? extends OperationInfo> infos = container.listAll ();
                List<NativeComponentUpdateElementImpl> customElements = new ArrayList<NativeComponentUpdateElementImpl> ();
                for (OperationInfo operationInfo : infos) {
                    UpdateElementImpl impl = Trampoline.API.impl (operationInfo.getUpdateElement ());
                    assert impl instanceof NativeComponentUpdateElementImpl : "Impl of " + operationInfo.getUpdateElement () + " instanceof NativeComponentUpdateElementImpl.";
                    customElements.add ((NativeComponentUpdateElementImpl) impl);
                }
                assert customElements != null : "Some elements with custom installer found.";
                if(progress!=null) {
                    progress.start(customElements.size());
                }
                started = true;
                int index = 0;
                affectedModules = new HashSet<UpdateElement> ();
                for (NativeComponentUpdateElementImpl impl : customElements) {
                    if(progress!=null) {
                        progress.progress(NbBundle.getMessage(OperationSupportImpl.class, "OperationSupportImpl_Custom_Install", impl.getDisplayName()), ++index);
                    }
                    CustomInstaller installer = impl.getInstallInfo ().getCustomInstaller ();
                    assert installer != null : "CustomInstaller must found for " + impl.getUpdateElement ();
                    ProgressHandle handle = ProgressHandleFactory.createHandle("Installing " + impl.getDisplayName());
                    //handle.start();
                    success = installer.install (impl.getCodeName (),
                            impl.getSpecificationVersion () == null ? null : impl.getSpecificationVersion ().toString (),
                            handle);
                    try {
                        handle.finish();
                    } catch (IllegalStateException e) {
                        LOGGER.log(Level.FINE, "Can`t stop progress handle, likely was not started ", e);
                    }
                    if (success) {
                        UpdateUnitImpl unitImpl = Trampoline.API.impl (impl.getUpdateUnit ());
                        unitImpl.setInstalled (impl.getUpdateElement ());
                        affectedModules.add(impl.getUpdateElement());
                    } else {
                        throw new OperationException (OperationException.ERROR_TYPE.INSTALL, impl.getDisplayName ());
                    }
                }
            } finally {
                if (progress != null && started) {
                    progress.finish ();
                }
            }

            return success;

        }
        @Override
        public void doCancel () throws OperationException {
            assert false : "Not supported yet";
        }

        @Override
        public void doRestart (Restarter restarter, ProgressHandle progress) throws OperationException {
            markForRestart();
            LifecycleManager.getDefault ().exit ();
            // if exit&restart fails => use restart later as fallback
            doRestartLater (restarter);
        }

        @Override
        public void doRestartLater (Restarter restarter) {
            // shedule module for restart
            markForRestart();
            if(affectedModules!=null) {
            for (UpdateElement el : affectedModules) {
                UpdateUnitFactory.getDefault().scheduleForRestart (el);
            }
            }
        }
    }

    private static class ForCustomUninstall extends OperationSupportImpl {
        private Collection<UpdateElement> affectedModules = null;
        @Override
        public synchronized Boolean doOperation(ProgressHandle progress,
                OperationContainer<?> container) throws OperationException {
            boolean success = false;
            boolean started = false;
            try {

                List<? extends OperationInfo> infos = container.listAll ();
                List<NativeComponentUpdateElementImpl> customElements = new ArrayList<NativeComponentUpdateElementImpl> ();
                for (OperationInfo operationInfo : infos) {
                    UpdateElementImpl impl = Trampoline.API.impl (operationInfo.getUpdateElement ());
                    assert impl instanceof NativeComponentUpdateElementImpl : "Impl of " + operationInfo.getUpdateElement () + " instanceof NativeComponentUpdateElementImpl.";
                    customElements.add ((NativeComponentUpdateElementImpl) impl);
                }
                assert customElements != null : "Some elements with custom installer found.";
                progress.start(customElements.size());
                started = true;
                int index = 0;
                affectedModules = new HashSet<UpdateElement> ();
                for (NativeComponentUpdateElementImpl impl : customElements) {
                    progress.progress(NbBundle.getMessage(OperationSupportImpl.class, "OperationSupportImpl_Custom_Uninstall", impl.getDisplayName()), ++index);
                    CustomUninstaller uninstaller = impl.getNativeItem ().getUpdateItemDeploymentImpl ().getCustomUninstaller ();
                    assert uninstaller != null : "CustomInstaller must found for " + impl.getUpdateElement ();
                    ProgressHandle handle = ProgressHandleFactory.createHandle("Installing " + impl.getDisplayName());
                    success = uninstaller.uninstall (impl.getCodeName (),
                            impl.getSpecificationVersion () == null ? null : impl.getSpecificationVersion ().toString (),
                            handle);
                    handle.finish();
                    if (success) {
                        UpdateUnitImpl unitImpl = Trampoline.API.impl (impl.getUpdateUnit ());
                        unitImpl.setAsUninstalled ();
                        affectedModules.add(impl.getUpdateElement());
                    } else {
                        throw new OperationException (OperationException.ERROR_TYPE.UNINSTALL, impl.getDisplayName ());
                    }
                }
            } finally {
                if (progress != null && started) {
                    progress.finish ();
                }
            }

            return success;

        }
        @Override
        public void doCancel () throws OperationException {
            assert false : "Not supported yet";
        }

        @Override
        public void doRestart (Restarter restarter, ProgressHandle progress) throws OperationException {
            markForRestart();
            LifecycleManager.getDefault ().exit ();
            // if exit&restart fails => use restart later as fallback
            doRestartLater (restarter);
        }

        @Override
        public void doRestartLater (Restarter restarter) {
            // shedule module for restart
            markForRestart();
            if(affectedModules!=null) {
            for (UpdateElement el : affectedModules) {
                UpdateUnitFactory.getDefault().scheduleForRestart (el);
            }
            }
        }
    }

    private static void markForRestart() {
        try {
            LifecycleManager.getDefault().markForRestart();
        } catch (UnsupportedOperationException x) {
            LOGGER.log(Level.INFO, null, x);
        }
    }

}
