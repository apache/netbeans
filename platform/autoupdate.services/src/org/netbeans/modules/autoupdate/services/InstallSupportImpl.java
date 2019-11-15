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

package org.netbeans.modules.autoupdate.services;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.CodeSigner;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.TrustAnchor;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import org.netbeans.Module;
import org.netbeans.api.autoupdate.*;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateInfoParser;
import org.netbeans.modules.autoupdate.updateprovider.MessageDigestValue;
import org.netbeans.modules.autoupdate.updateprovider.ModuleItem;
import org.netbeans.modules.autoupdate.updateprovider.NetworkAccess;
import org.netbeans.modules.autoupdate.updateprovider.NetworkAccess.Task;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.spi.autoupdate.KeyStoreProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.updater.ModuleDeactivator;
import org.netbeans.updater.ModuleUpdater;
import org.netbeans.updater.UpdateTracking;
import org.netbeans.updater.UpdaterInternal;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.xml.sax.SAXException;

import static org.netbeans.modules.autoupdate.services.Utilities.VERIFICATION_RESULT_COMPARATOR;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallSupportImpl {
    private final InstallSupport support;
    private boolean progressRunning = false;
    private static final Logger LOG = Logger.getLogger (InstallSupportImpl.class.getName ());
    
    private Map<UpdateElementImpl, File> element2Clusters = null;
    private final Set<File> downloadedFiles = new HashSet<File> ();
    private Boolean isGlobal;
    private boolean useUserdirAsFallback;
    private int wasDownloaded = 0;
    
    private Future<Boolean> runningTask;
    private final Object LOCK = new Object();

    private static enum STEP {
        NOTSTARTED,
        DOWNLOAD,
        VALIDATION,
        INSTALLATION,
        RESTART,
        FINISHED,
        CANCEL
    }       

    private STEP currentStep = STEP.NOTSTARTED;
    
    // validation results
    private final Collection<UpdateElementImpl> trusted = new ArrayList<>();
    private final Collection<UpdateElementImpl> signedVerified = new ArrayList<>();
    private final Collection<UpdateElementImpl> signedUnverified = new ArrayList<>();
    private final Collection<UpdateElementImpl> modified = new ArrayList<>();
    private final Map<UpdateElement, Collection<Certificate>> certs = new HashMap<>();
    private List<? extends OperationInfo> infos = null;
    
    private ExecutorService es = null;
    
    public InstallSupportImpl (InstallSupport installSupport) {
        support = installSupport;
    }
    
    @SuppressWarnings("ThrowableResultIgnored")
    public boolean doDownload (final ProgressHandle progress/*or null*/, final Boolean isGlobal, final boolean useUserdirAsFallback) throws OperationException {
        this.isGlobal = isGlobal;
        this.useUserdirAsFallback = useUserdirAsFallback;
        
        // start progress
        if (progress != null) {
            progress.start();
            progress.progress(NbBundle.getMessage(InstallSupportImpl.class, "InstallSupportImpl_Download_Estabilish"));
            progressRunning = false;
        }

        Callable<Boolean> downloadCallable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final OperationContainer<InstallSupport> container = support.getContainer ();
                assert container.listInvalid ().isEmpty () : support + ".listInvalid().isEmpty() but " + container.listInvalid () + " container: " + container;
                synchronized(LOCK) {
                    currentStep = STEP.DOWNLOAD;
                }

                infos = container.listAll ();
                // check write permissions before started download and then sum download size
                int size = 0;
                for (OperationInfo info : infos) {
                    UpdateElement ue = info.getUpdateElement();
                    InstallManager.findTargetDirectory(ue.getUpdateUnit().getInstalled(), Trampoline.API.impl(ue), isGlobal, useUserdirAsFallback);
                    size += ue.getDownloadSize();
                }
                
                int aggregateDownload = 0;
                
                try {
                    long id = System.currentTimeMillis();
                    for (OperationInfo info : infos) {
                        if (cancelled()) {
                            LOG.log (Level.INFO, "InstallSupport.doDownload was canceled"); // NOI18N
                            return false;
                        }
                        
                        int increment = doDownload(info, progress, aggregateDownload, size, id);
                        if (increment == -1) {
                            return false;
                        }
                        aggregateDownload += increment;
                    }
                    postDownload(id);
                }  finally {
                    // end progress
                    if (progress != null) {
                        progress.progress("");
                        progress.finish();
                    }
                }
                
                assert size == aggregateDownload : "Was downloaded " + aggregateDownload + ", planned was " + size;
                wasDownloaded = aggregateDownload;
                return true;
            }
        };
        
        boolean retval =  false;
        try {
            runningTask = getExecutionService ().submit (downloadCallable);
            retval = runningTask.get ();
        } catch (CancellationException ex) {
            LOG.log (Level.FINE, "InstallSupport.doDownload was cancelled", ex); // NOI18N
            return false;
        } catch(InterruptedException iex) {
            Exceptions.printStackTrace(iex);
        } catch(ExecutionException iex) {
            if (! (iex.getCause() instanceof OperationException)) {
                Exceptions.printStackTrace(iex);
            } else {
                throw (OperationException) iex.getCause ();
            }
        }
        return retval;
    }

    private void postDownload(long id) {
        for (File cluster : UpdateTracking.clusters(true)) {
            File runningDownloadDir = new File(cluster, Utilities.RUNNING_DOWNLOAD_DIR + '_' + id);
            if (runningDownloadDir.isDirectory()) {
                File downloadDir = new File(cluster, Utilities.DOWNLOAD_DIR);
                downloadDir.mkdirs();
                for (String nbmName : runningDownloadDir.list()) {
                        File nbmSource = new File(runningDownloadDir, nbmName);
                        File nbmTarget = new File(downloadDir, nbmName);
                        if (nbmTarget.exists()) {
                            nbmTarget.delete();
                        }
                    boolean res = nbmSource.renameTo(nbmTarget);
                    if (! res) {
                        LOG.log(Level.WARNING, "{0} didn''t move to {1}", new Object[]{nbmSource, nbmTarget});
                    }
                }
                runningDownloadDir.delete();
            }
        }
    }

    @SuppressWarnings("ThrowableResultIgnored")
    public boolean doValidate (final Validator validator, final ProgressHandle progress/*or null*/) throws OperationException {
        assert validator != null;
        Callable<Boolean> validationCallable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                synchronized(LOCK) {
                    assert currentStep != STEP.FINISHED;
                    if (currentStep == STEP.CANCEL) return false;
                    currentStep = STEP.VALIDATION;
                }
                final OperationContainer<InstallSupport> container = support.getContainer();
                assert container.listInvalid ().isEmpty () : support + ".listInvalid().isEmpty() but " + container.listInvalid () + "\ncontainer: " + container;

                // start progress
                if (progress != null) {
                    progress.start (wasDownloaded);
                }
                
                int aggregateVerified = 0;
                
                try {
                    for (OperationInfo info : infos) {
                        if (cancelled()) return false;
                        UpdateElementImpl toUpdateImpl = Trampoline.API.impl(info.getUpdateElement());
                        boolean hasCustom = toUpdateImpl.getInstallInfo().getCustomInstaller() != null;
                        if (hasCustom) {
                            // XXX: validation of custom installed
                            assert false : "InstallSupportImpl cannot support CustomInstaller!";
                        } else {
                            aggregateVerified += doValidate (info, progress, aggregateVerified);
                        }
                    }
                } finally {
                    // end progress
                    if (progress != null) {
                        progress.progress("");
                        progress.finish();
                    }
                }
                return true;
            }
        };
        boolean retval =  false;
        try {
            runningTask = getExecutionService ().submit (validationCallable);
            retval = runningTask.get ();
        } catch (CancellationException ex) {
            LOG.log (Level.FINE, "InstallSupport.doValidate was cancelled", ex); // NOI18N
            return false;
        } catch(InterruptedException iex) {
            if (iex.getCause() instanceof OperationException) {
                throw (OperationException) iex.getCause();
            }
            Exceptions.printStackTrace(iex);
        } catch(ExecutionException iex) {
            if (iex.getCause() instanceof SecurityException) {
                throw new OperationException(OperationException.ERROR_TYPE.MODIFIED, iex.getLocalizedMessage());
            } else {
                if (iex.getCause() instanceof OperationException) {
                    throw (OperationException) iex.getCause();
                }
                Exceptions.printStackTrace(iex);
            }
        }
        return retval;
    }
    
    private Set<ModuleUpdateElementImpl> affectedModuleImpls = null;
    private Set<FeatureUpdateElementImpl> affectedFeatureImpls = null; 
    
    @SuppressWarnings("ThrowableResultIgnored")
    public Boolean doInstall (final Installer installer, final ProgressHandle progress/*or null*/, final boolean forceInstall) throws OperationException {
        assert installer != null;
        Callable<Boolean> installCallable = new Callable<Boolean>() {
            @Override
            @SuppressWarnings("SleepWhileInLoop")
            public Boolean call() throws Exception {
                synchronized(LOCK) {
                    assert currentStep != STEP.FINISHED : currentStep + " != STEP.FINISHED";
                    if (currentStep == STEP.CANCEL) return false;
                    currentStep = STEP.INSTALLATION;
                }
                assert support.getContainer ().listInvalid ().isEmpty () : support + ".listInvalid().isEmpty() but " + support.getContainer ().listInvalid ();
                
                // do trust of so far untrusted certificates
                addTrustedCertificates ();

                affectedModuleImpls = new HashSet<ModuleUpdateElementImpl> ();
                affectedFeatureImpls = new HashSet<FeatureUpdateElementImpl> ();
                
                if (progress != null) progress.start();
                
                for (OperationInfo info : infos) {
                    UpdateElementImpl toUpdateImpl = Trampoline.API.impl (info.getUpdateElement ());
                    switch (toUpdateImpl.getType ()) {
                    case KIT_MODULE :
                    case MODULE :
                        affectedModuleImpls.add ((ModuleUpdateElementImpl) toUpdateImpl);
                        break;
                    case STANDALONE_MODULE :
                    case FEATURE :
                        affectedFeatureImpls.add ((FeatureUpdateElementImpl) toUpdateImpl);
                        affectedModuleImpls.addAll (((FeatureUpdateElementImpl) toUpdateImpl).getContainedModuleElements ());
                        break;
                    default:
                        // XXX: what other types
                        assert false : "Unsupported type " + toUpdateImpl;
                    }
                }
                
                boolean needsRestart = false;
                File targetCluster;
                List <UpdaterInfo> updaterFiles = new ArrayList <UpdaterInfo> ();
                
                for (ModuleUpdateElementImpl moduleImpl : affectedModuleImpls) {
                    synchronized(LOCK) {
                        if (currentStep == STEP.CANCEL) {
                            if (progress != null) progress.finish ();
                            return false;
                        }
                    }
                    
                    // skip installed element
                    if (Utilities.isElementInstalled (moduleImpl.getUpdateElement ())) {
                        continue;
                    }
                    
                    // find target dir
                    UpdateElement installed = moduleImpl.getUpdateUnit ().getInstalled ();
                    targetCluster = getTargetCluster (installed, moduleImpl, isGlobal, useUserdirAsFallback);
                    
                    URL source = moduleImpl.getInstallInfo ().getDistribution ();
                    LOG.log (Level.FINE, "Source URL for " + moduleImpl.getCodeName () + " is " + source);
                    
                    File dest = getDestination(targetCluster, moduleImpl.getCodeName(), source, 0);
                    assert dest != null : "Destination file exists for " + moduleImpl + " in " + targetCluster;
                    
                    JarFile jf = new JarFile(dest);
                    try {
                       for (JarEntry entry : Collections.list(jf.entries())) {
                            if (ModuleUpdater.AUTOUPDATE_UPDATER_JAR_PATH.equals(entry.toString()) ||
                                    entry.toString().matches(ModuleUpdater.AUTOUPDATE_UPDATER_JAR_LOCALE_PATTERN)) {
                                LOG.log(Level.INFO, entry.toString() + " is being installed from " + moduleImpl.getCodeName());
                                updaterFiles.add(new UpdaterInfo(entry, dest, targetCluster));
                                needsRestart = true;
                             }
                         }
                    } finally {
                        jf.close();
                     }


                    needsRestart |= needsRestart(installed != null, moduleImpl, dest);
                }
                
                try {
                    // store source of installed files
                    Utilities.writeAdditionalInformation (getElement2Clusters ());
                    for(int i=0;i<updaterFiles.size();i++) {
                        UpdaterInfo info = updaterFiles.get(i);
                        try {
                            Utilities.writeUpdateOfUpdaterJar (info.getUpdaterJarEntry(), info.getZipFileWithUpdater(), info.getUpdaterTargetCluster());
                        } catch (IOException ex) {
                            LOG.log(Level.INFO, "Cannot open or close jar file {0}", info.getZipFileWithUpdater());
                        }
                    }

                    if (! needsRestart || forceInstall) {
                        synchronized(LOCK) {
                            if (currentStep == STEP.CANCEL) {
                                if (progress != null) progress.finish ();
                                return false;
                            }
                        }

                        if (progress != null) progress.switchToDeterminate (affectedModuleImpls.size ());

                        final Set <File> files;
                        synchronized(downloadedFiles) {
                            files = new HashSet <File> (downloadedFiles);
                        }
                        if (! files.isEmpty ()) {
                            try {
                                FileUtil.runAtomicAction(new Runnable() {

                                    @Override
                                    public void run() {
                                        try {
                                            UpdaterInternal.update(
                                                files,
                                                new RefreshModulesListener (progress),
                                                NbBundle.getBranding()
                                            );
                                        } catch (InterruptedException ie) {
                                            LOG.log (Level.INFO, ie.getMessage (), ie);
                                        }
                                    }
                                });
                                for (ModuleUpdateElementImpl impl : affectedModuleImpls) {
                                    int rerunWaitCount = 0;
                                    Module module = Utilities.toModule (impl.getCodeName(), impl.getSpecificationVersion ());
                                    for (; rerunWaitCount < 100 && module == null; rerunWaitCount++) {
                                        LOG.log(Level.FINE, "Waiting for {0}@{1} #{2}", new Object[]{ impl.getCodeName(), impl.getSpecificationVersion(), rerunWaitCount});
                                        Thread.sleep(100);
                                        module = Utilities.toModule (impl.getCodeName(), impl.getSpecificationVersion ());
                                    }
                                    if (rerunWaitCount == 100) {
                                        LOG.log (Level.INFO, "Timeout waiting for loading module {0}@{1}", new Object[]{impl.getCodeName (), impl.getSpecificationVersion ()});
                                        afterInstall ();
                                        synchronized(downloadedFiles) {
                                            downloadedFiles.clear();
                                        }
                                        throw new OperationException (OperationException.ERROR_TYPE.INSTALL,
                                                NbBundle.getMessage(InstallSupportImpl.class, "InstallSupportImpl_TurnOnTimeout", // NOI18N
                                                impl.getUpdateElement ()));
                                    }
                                }
                            } catch(InterruptedException ie) {
                                LOG.log (Level.INFO, ie.getMessage (), ie);
                            }
                        }
                        afterInstall ();
                        synchronized(downloadedFiles) {
                            downloadedFiles.clear();
                        }
                    }
                } finally {
                    // end progress
                    if (progress != null) {
                        progress.progress("");
                        progress.finish();
                    }
                }
                
                return needsRestart && ! forceInstall ? Boolean.TRUE : Boolean.FALSE;
            }
        };
        
        boolean retval =  false;
        try {
            runningTask = getExecutionService ().submit (installCallable);
            retval = runningTask.get ();
        } catch (CancellationException ex) {
            LOG.log (Level.FINE, "InstallSupport.doInstall was cancelled", ex); // NOI18N
            return false;
        } catch(InterruptedException iex) {
            LOG.log (Level.INFO, iex.getLocalizedMessage (), iex);
        } catch(ExecutionException iex) {
            if (iex.getCause () instanceof OperationException) {
                throw (OperationException) iex.getCause ();
            } else {
                LOG.log (Level.INFO, iex.getLocalizedMessage (), iex);
            }
        } finally {
            if (! retval) {
                getElement2Clusters ().clear ();
            }
        }
        return retval;
    }
    
    private void afterInstall () {
        
        if (affectedModuleImpls != null) {
            for (ModuleUpdateElementImpl impl : affectedModuleImpls) {
                UpdateUnit u = impl.getUpdateUnit ();
                UpdateElement el = impl.getUpdateElement ();
                Trampoline.API.impl(u).updateInstalled(el);
            }
            affectedModuleImpls = null;
        }
        
        if (affectedFeatureImpls != null) {
            for (FeatureUpdateElementImpl impl : affectedFeatureImpls) {
                UpdateUnit u = impl.getUpdateUnit ();
                UpdateElement el = impl.getUpdateElement ();
                Trampoline.API.impl(u).updateInstalled(el);
            }
            affectedFeatureImpls = null;
        }
        
    }

    public void doRestart (Restarter restarter, ProgressHandle progress/*or null*/) throws OperationException {
        synchronized(LOCK) {
            assert currentStep != STEP.FINISHED;
            currentStep = STEP.RESTART;
        }        
        Utilities.deleteAllDoLater ();
        getElement2Clusters ().clear ();
        
        LifecycleManager.getDefault ().exit ();
        
        // if exit&restart fails => use restart later as fallback
        doRestartLater (restarter);
    }
    
    public void doRestartLater(Restarter restarter) {
        // schedule module for install later
        if (affectedModuleImpls != null) {
            for (ModuleUpdateElementImpl impl : affectedModuleImpls) {
                UpdateUnitFactory.getDefault ().scheduleForRestart (impl.getUpdateElement ());
            }
        }

        Utilities.writeInstallLater(new HashMap<UpdateElementImpl, File>(getElement2Clusters ()));
        getElement2Clusters ().clear ();
        synchronized (downloadedFiles) {
            downloadedFiles.clear();
        }
    }
    public String getCertificate(Installer validator, UpdateElement uElement) {
        Collection<Certificate> certificates = certs.get (uElement);
        if (certificates != null) {
            String res = "";
            for (Certificate c :certificates) {
                res += c;
            }
            return res;
        } else {
            return null;
        }
    }

    public boolean isTrusted(Installer validator, UpdateElement uElement) {
        UpdateElementImpl impl = Trampoline.API.impl (uElement);
        return checkUpdateElement(impl, trusted);
    }

    public boolean isSignedVerified(Installer validator, UpdateElement uElement) {
        UpdateElementImpl impl = Trampoline.API.impl (uElement);
        return checkUpdateElement(impl, signedVerified);
    }
    
    public boolean isSignedUnverified(Installer validator, UpdateElement uElement) {
        UpdateElementImpl impl = Trampoline.API.impl (uElement);
        return checkUpdateElement(impl, signedUnverified);
    }
    
    public boolean isContentModified(Installer validator, UpdateElement uElement) {
        UpdateElementImpl impl = Trampoline.API.impl (uElement);
        return checkUpdateElement(impl, modified);
    }
    
    private boolean checkUpdateElement(UpdateElementImpl impl, Collection<UpdateElementImpl> elements) {
        boolean res = false;
        switch (impl.getType ()) {
        case KIT_MODULE :
        case MODULE :
            res = elements.contains (impl);
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            FeatureUpdateElementImpl toUpdateFeatureImpl = (FeatureUpdateElementImpl) impl;
            Set<ModuleUpdateElementImpl> moduleImpls = toUpdateFeatureImpl.getContainedModuleElements ();
            res = ! moduleImpls.isEmpty ();
            for (ModuleUpdateElementImpl moduleImpl : moduleImpls) {
                // skip installed element
                if (Utilities.isElementInstalled (moduleImpl.getUpdateElement ())) {
                    continue;
                }
                
                res &= elements.contains (moduleImpl);
            }
            break;
        default:
            // XXX: what other types
            assert false : "Unsupported type " + impl;
        }
        return res;
    }
    
    private void addTrustedCertificates () {
        // find untrusted so far
        Collection<UpdateElementImpl> untrusted = new HashSet<UpdateElementImpl> (signedVerified);
        untrusted.removeAll (trusted);
        if (untrusted.isEmpty ()) {
            // all are trusted
            return;
        }
        
        // find corresponding certificates
        Collection<Certificate> untrustedCertificates = new HashSet<Certificate> ();
        for (UpdateElementImpl i : untrusted) {
            untrustedCertificates.addAll (certs.get (i.getUpdateElement ()));
        }
        
        if (! untrustedCertificates.isEmpty ()) {
            Utilities.addCertificates (untrustedCertificates);
        }
    }

    public void doCancel () throws OperationException {
        synchronized(LOCK) {
            currentStep = STEP.CANCEL;
        }
        if (runningTask != null && ! runningTask.isDone () && ! runningTask.isCancelled ()) {
            boolean cancelled = runningTask.cancel (true);
            assert cancelled : runningTask + " was cancelled.";
        }
        synchronized(downloadedFiles) {
            for (File f : downloadedFiles) {
                if (f != null && f.exists ()) {
                    f.delete ();
                    if (f.getParentFile().isDirectory() && f.getParentFile().list() != null && f.getParentFile().list().length == 0) {
                        f.getParentFile().delete();
                    }
                }
            }
            downloadedFiles.clear ();
        }
        Utilities.cleanUpdateOfUpdaterJar ();
        if (affectedFeatureImpls != null) affectedFeatureImpls = null;
        if (affectedModuleImpls != null) affectedModuleImpls = null;
        
        // also mapping elements to cluster clear because global vs. local may be changed
        getElement2Clusters ().clear ();
    }
    
    private int doDownload (OperationInfo info, ProgressHandle progress, final int aggregateDownload, final int totalSize, final long id) throws OperationException {
        UpdateElement toUpdateElement = info.getUpdateElement();
        UpdateElementImpl toUpdateImpl = Trampoline.API.impl (toUpdateElement);
        int res = 0;
        switch (toUpdateImpl.getType ()) {
        case KIT_MODULE :
        case MODULE :
            res += doDownload (toUpdateImpl, progress, aggregateDownload, totalSize, id);
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            FeatureUpdateElementImpl toUpdateFeatureImpl = (FeatureUpdateElementImpl) toUpdateImpl;
            Set<ModuleUpdateElementImpl> moduleImpls = toUpdateFeatureImpl.getContainedModuleElements ();
            int nestedAggregateDownload = aggregateDownload;
            for (ModuleUpdateElementImpl moduleImpl : moduleImpls) {
                // skip installed element
                if (Utilities.isElementInstalled (moduleImpl.getUpdateElement ())) {
                    continue;
                }
                
                int increment = doDownload (moduleImpl, progress, nestedAggregateDownload, totalSize, id);
                if (increment == -1) {
                    return -1;
                }
                nestedAggregateDownload += increment;
                res += increment;
            }
            break;
        default:
            // XXX: what other types
            assert false : "Unsupported type " + toUpdateImpl;
        }
        return res;
    }
    
    @SuppressWarnings("null")
    private int doDownload (UpdateElementImpl toUpdateImpl, ProgressHandle progress, final int aggregateDownload, final int totalSize, final long id) throws OperationException {
        if (cancelled()) {
            LOG.log (Level.INFO, "InstallSupport.doDownload was canceled, returns -1"); // NOI18N
            return -1;
        }
        
        UpdateElement installed = toUpdateImpl.getUpdateUnit ().getInstalled ();
        
        // find target dir
        File targetCluster = getTargetCluster (installed, toUpdateImpl, isGlobal, useUserdirAsFallback);
        assert targetCluster != null : "Target cluster for " + toUpdateImpl + " must exist.";
        if (targetCluster == null) {
            targetCluster = InstallManager.getUserDir ();
        }

        URL source = toUpdateImpl.getInstallInfo().getDistribution();
        LOG.log (Level.FINE, "Source URL for " + toUpdateImpl.getCodeName () + " is " + source);
        if(source==null) {
            final String errorString = NbBundle.getMessage(InstallSupportImpl.class, 
                    "InstallSupportImpl_NullSource", toUpdateImpl.getCodeName()); // NOI18N
            LOG.log (Level.INFO, errorString);
            throw new OperationException (OperationException.ERROR_TYPE.INSTALL, errorString);
        }

        File dest = getDestination (targetCluster, toUpdateImpl.getCodeName(), source, id);
        
        // skip already downloaded modules
        if (dest.exists ()) {
            LOG.log (Level.FINE, "Target NBM file " + dest + " of " + toUpdateImpl.getUpdateElement () + " already downloaded.");
            return toUpdateImpl.getDownloadSize ();
        }

        int c;
        
        // download
        try {
            String label = toUpdateImpl.getDisplayName ();
            File normalized = FileUtil.normalizeFile(getDestination(targetCluster, toUpdateImpl.getCodeName(), source, 0));
            File normalizedInProgress = FileUtil.normalizeFile(getDestination(targetCluster, toUpdateImpl.getCodeName(), source, id));
            synchronized(downloadedFiles) {
                downloadedFiles.add(normalized);
                downloadedFiles.add(normalizedInProgress);
            }
            c = copy (source, dest, progress, toUpdateImpl.getDownloadSize (), aggregateDownload, totalSize, label);
            boolean wasException = false;
            JarFile nbm = new JarFile(dest);
            try {
                Enumeration<JarEntry> en = nbm.entries();
                while (en.hasMoreElements()) {
                    JarEntry jarEntry = en.nextElement();
                    if (jarEntry.getName().endsWith(".external")) {
                        InputStream is = nbm.getInputStream(jarEntry);
                        try {
                            AtomicLong crc = new AtomicLong();
                            InputStream real = externalDownload(is, crc, jarEntry.getName());
                            if (crc.get() == -1L) {
                                throw new IOException(jarEntry.getName() + " does not contain CRC: line!");
                            }
                            byte[] arr = new byte[4096];
                            CRC32 check = new CRC32();
                            File external = new File(dest.getPath() + "." + Long.toHexString(crc.get()));
                            FileOutputStream fos = new FileOutputStream(external);
                            try {
                                for (;;) {
                                    int len = real.read(arr);
                                    if (len == -1) {
                                        break;
                                    }
                                    check.update(arr, 0, len);
                                    fos.write(arr, 0, len);
                                    if (progressRunning) {
                                        if ((c += len) <= toUpdateImpl.getDownloadSize()) {
                                            progress.progress(aggregateDownload + c);
                                        }
                                    }
                                }
                            } finally {
                                fos.close();
                            }
                            real.close();
                            if (check.getValue() != crc.get()) {
                                LOG.log(Level.INFO, "Deleting file with uncomplete external content(cause: wrong CRC) " + normalized);
                                dest.delete();
                                synchronized(downloadedFiles) {
                                    downloadedFiles.remove(normalized);
                                }
                                external.delete();
                                throw new IOException("Wrong CRC for " + jarEntry.getName());
                            }
                        } finally {
                            is.close();
                        }
                    }
                }
            } catch (FileNotFoundException x) {
                LOG.log(Level.INFO, x.getMessage(), x);
                wasException = true;
                throw new OperationException(OperationException.ERROR_TYPE.INSTALL, x.getLocalizedMessage());
            } catch (IOException x) {
                LOG.log(Level.INFO, x.getMessage(), x);
                wasException = true;
                throw new OperationException(OperationException.ERROR_TYPE.PROXY, x.getLocalizedMessage());
            } finally {
                nbm.close();
                if (wasException) {
                    dest.delete();
                }
            }
        } catch (UnknownHostException x) {
            LOG.log (Level.INFO, x.getMessage (), x);
            throw new OperationException (OperationException.ERROR_TYPE.PROXY, source.toString ());
        } catch (FileNotFoundException x) {
            LOG.log (Level.INFO, x.getMessage (), x);
            throw new OperationException (OperationException.ERROR_TYPE.INSTALL, x.getLocalizedMessage ());
        } catch (IOException x) {
            LOG.log (Level.INFO, x.getMessage (), x);
            throw new OperationException (OperationException.ERROR_TYPE.PROXY, source.toString ());
        }
        
        return toUpdateImpl.getDownloadSize();
    }

    private int doValidate (OperationInfo info, ProgressHandle progress, final int verified) throws OperationException {
        UpdateElement toUpdateElement = info.getUpdateElement();
        UpdateElementImpl toUpdateImpl = Trampoline.API.impl (toUpdateElement);
        int increment = 0;
        switch (toUpdateImpl.getType ()) {
        case KIT_MODULE :
        case MODULE :
            increment = doValidate (toUpdateImpl, progress, verified);
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            FeatureUpdateElementImpl toUpdateFeatureImpl = (FeatureUpdateElementImpl) toUpdateImpl;
            Set<ModuleUpdateElementImpl> moduleImpls = toUpdateFeatureImpl.getContainedModuleElements ();
            int nestedVerified = verified;
            for (ModuleUpdateElementImpl moduleImpl : moduleImpls) {
                // skip installed element
                if (Utilities.isElementInstalled (moduleImpl.getUpdateElement ())) {
                    continue;
                }
                int singleIncrement = doValidate (moduleImpl, progress, nestedVerified);
                nestedVerified += singleIncrement;
                increment += singleIncrement;
            }
            break;
        default:
            // XXX: what other types
            assert false : "Unsupported type " + toUpdateImpl;
        }
        return increment;
    }

    private int doValidate (UpdateElementImpl toUpdateImpl, ProgressHandle progress, final int verified) throws OperationException {
        UpdateElement installed = toUpdateImpl.getUpdateUnit ().getInstalled ();
        
        // find target dir
        File targetCluster = getTargetCluster (installed, toUpdateImpl, isGlobal, useUserdirAsFallback);

        URL source = toUpdateImpl.getInstallInfo().getDistribution();
        File dest = getDestination (targetCluster, toUpdateImpl.getCodeName(), source, 0);
        if (!dest.exists()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot find ").append(dest).append("\n");
            sb.append("Parent directory contains:").append(Arrays.toString(dest.getParentFile().list())).append("\n");
            for (File f : UpdateTracking.clusters(true)) {
                sb.append("Trying to find result in ").append(f).append(" = ");
                File alt = getDestination (targetCluster, toUpdateImpl.getCodeName(), source, 0);
                sb.append(alt).append(" exists ").append(alt.exists()).append("\n");
            }
            throw new OperationException(OperationException.ERROR_TYPE.INSTALL, sb.toString());
        }
        
        int wasVerified;

        // verify
        wasVerified = verifyNbm (toUpdateImpl.getUpdateElement (), dest, progress, verified);
        
        return wasVerified;
    }
    
    public static File getDestination(File targetCluster, String codeName, URL source) {
        return getDestination(targetCluster, codeName, source, 0);
    }
    
    private static File getDestination (File targetCluster, String codeName, URL source, long id) {
        LOG.log (Level.FINE, "Target cluster for " + codeName + " is " + targetCluster);
        File destDir = new File(targetCluster, 0 == id ? Utilities.DOWNLOAD_DIR : Utilities.RUNNING_DOWNLOAD_DIR + '_' + id);
        if (! destDir.exists ()) {
            destDir.mkdirs ();
        }
        String fileName = codeName.replace ('.', '-');
        String filePath = source.getFile().toLowerCase(Locale.US);
        String ext = filePath.endsWith(Utilities.NBM_EXTENTSION.toLowerCase(Locale.US)) ?
            Utilities.NBM_EXTENTSION : (
            filePath.endsWith(Utilities.JAR_EXTENSION.toLowerCase(Locale.US)) ?
                Utilities.JAR_EXTENSION : ""
            );

        File destFile = new File (destDir, fileName + ext);
        LOG.log(Level.FINE, "Destination file for " + codeName + " is " + destFile);
        return destFile;
    }
    
    private boolean cancelled() {
        synchronized (this) {
            return STEP.CANCEL == currentStep;
        }
    }

    private class OpenConnectionListener implements NetworkAccess.NetworkListener {
        private InputStream stream = null;
        int contentLength = -1;
        private URL source = null;
        private Exception ex = null;
        public OpenConnectionListener (URL source) {
            this.source = source;
        }
        public InputStream getInputStream() {
            return stream;
        }
        public int getContentLength() {
            return contentLength;
        }
        @Override
        public void streamOpened(InputStream stream, int contentLength) {
            LOG.log(Level.FINEST, "Opened connection for " + source);
            this.stream = stream;
            this.contentLength = contentLength;
        }

        @Override
        public void accessCanceled() {
            LOG.log(Level.INFO, "Opening connection for " + source + "was cancelled");
        }

        @Override
        public void accessTimeOut() {
            LOG.log(Level.INFO, "Opening connection for " + source + "was finised due to timeout");
        }

        @Override
        public void notifyException(Exception x) {
            ex = x;
        }
        public Exception getException() {
            return ex;
        }

    }
    @SuppressWarnings("ThrowableResultIgnored")
    private int copy (URL source, File dest, 
            ProgressHandle progress, final int estimatedSize, final int aggregateDownload, final int totalSize,
            String label) throws MalformedURLException, IOException {
        
        OpenConnectionListener listener = new OpenConnectionListener(source);
        final Task task = NetworkAccess.createNetworkAccessTask(source,
                AutoupdateSettings.getOpenConnectionTimeout(),
                listener,
                false);
        new Thread(new Runnable() {
            @SuppressWarnings("SleepWhileInLoop")
            @Override
            public void run() {
                while (true) {
                    if (task.isFinished()) {
                        break;
                    } else if(cancelled()) {
                        task.cancel();
                        break;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        //ignore
                    }                    
                }
            }
        }).start();
        
        task.waitFinished ();

        try {
            if(listener.getException()!=null) {
                throw listener.getException();
            }
        } catch (FileNotFoundException x) {
            LOG.log (Level.INFO, x.getMessage(), x);
            throw new IOException(NbBundle.getMessage(InstallSupportImpl.class,
                    "InstallSupportImpl_Download_Unavailable", source));            
        } catch (IOException x) {
            LOG.log (Level.INFO, x.getMessage(), x);
            throw new IOException(NbBundle.getMessage(InstallSupportImpl.class,
                    "InstallSupportImpl_Download_Unavailable", source));            
        } catch (Exception x) {
            LOG.log (Level.INFO, x.getMessage(), x);
            throw new IOException(NbBundle.getMessage(InstallSupportImpl.class,
                    "InstallSupportImpl_Download_Unavailable", source));
        }
        if (cancelled()) {
            LOG.log(Level.FINE, "Download of " + source + " was cancelled");
            throw new IOException("Download of " + source + " was cancelled");
        }

        InputStream is = listener.getInputStream();
        int contentLength = listener.getContentLength();

        BufferedInputStream bsrc = new BufferedInputStream (is);
        BufferedOutputStream bdest = null;
        
        LOG.log (Level.FINEST, "Copy " + source + " to " + dest + "[" + estimatedSize + "]");
        boolean canceled = false;
        int increment = 0;
        try {
            byte [] bytes = new byte [1024];
            int size;
            int c = 0;
            while (!(canceled = cancelled()) && (size = bsrc.read (bytes)) != -1) {
                if(bdest == null) {
                    bdest = new BufferedOutputStream (new FileOutputStream (dest));
                }
                bdest.write (bytes, 0, size);
                increment += size;
                c += size;
                if (! progressRunning && progress != null) {
                    progress.switchToDeterminate (totalSize);
                    progress.progress (label);
                    progressRunning = true;
                }
                if (c > 1024) {
                    if (progress != null) {
                        assert progressRunning;
                        progress.switchToDeterminate (totalSize);
                        int i = aggregateDownload + (increment < estimatedSize ? increment : estimatedSize);
                        progress.progress (label, i < totalSize ? i : totalSize);
                    }
                    c = 0;
                }
            }
            //assert estimatedSize == increment : "Increment (" + increment
            //        + ") of is equal to estimatedSize (" + estimatedSize + ").";
            if (estimatedSize != increment) {
                LOG.log (Level.FINEST, "Increment (" + increment + ") of is not equal to estimatedSize (" + estimatedSize + ").");
            }
        } catch (IOException ioe) {
            LOG.log (Level.INFO, "Writing content of URL " + source + " failed.", ioe);
        } finally {
            try {
                bsrc.close ();
                if (bdest != null) bdest.flush ();
                if (bdest != null) bdest.close ();
            } catch (IOException ioe) {
                LOG.log (Level.INFO, ioe.getMessage (), ioe);
            }
        }

        if (contentLength != -1 && increment != contentLength) {
            if(canceled) {
                LOG.log(Level.INFO, "Download of " + source + " was cancelled");
            } else {
                LOG.log(Level.INFO, "Content length was reported as " + contentLength + " byte(s) but read " + increment + " byte(s)");
            }
            if(bdest!=null && dest.exists()) {
                LOG.log(Level.INFO, "Deleting not fully downloaded file " + dest);
                dest.delete();
                File normalized = FileUtil.normalizeFile (dest);
                synchronized(downloadedFiles) {
                    downloadedFiles.remove(normalized);
                }
            }
            if(canceled) {
                throw new IOException("Download of " + source + " was cancelled");
            } else {
                throw new IOException("Server closed connection unexpectedly");
            }
        }

        LOG.log (Level.FINE, "Destination " + dest + " is successfully wrote. Size " + dest.length());
        
        return increment;
    }
    
    private int verifyNbm (UpdateElement el, File nbmFile, ProgressHandle progress, int verified) throws OperationException {
        UpdateElementImpl impl = Trampoline.API.impl(el);

        modified.remove(impl);
        trusted.remove(impl);
        signedVerified.remove(impl);
        signedUnverified.remove(impl);

        String res = null;
        try {
            // get trusted certificates
            Set<Certificate> trustedCerts = new HashSet<> ();
            Set<Certificate> validationCerts = new HashSet<>();
            Set<TrustAnchor> trustedCACerts = new HashSet<>();
            Set<TrustAnchor> validationCACerts = new HashSet<>();
            for (KeyStore ks : Utilities.getKeyStore (KeyStoreProvider.TrustLevel.TRUST)) {
                trustedCerts.addAll(Utilities.getCertificates(ks));
            }
            for (KeyStore ks : Utilities.getKeyStore (KeyStoreProvider.TrustLevel.VALIDATE)) {
                validationCerts.addAll(Utilities.getCertificates(ks));
            }
            for (KeyStore ks : Utilities.getKeyStore (KeyStoreProvider.TrustLevel.TRUST_CA)) {
                trustedCACerts.addAll(Utilities.getTrustAnchor(ks));
            }
            for (KeyStore ks : Utilities.getKeyStore (KeyStoreProvider.TrustLevel.VALIDATE_CA)) {
                validationCACerts.addAll(Utilities.getTrustAnchor(ks));
            }
            // load user certificates
            KeyStore ks = Utilities.loadKeyStore ();
            if (ks != null) {
                trustedCerts.addAll(Utilities.getCertificates(ks));
            }

            verified += el.getDownloadSize ();
            if (progress != null) {
                progress.progress (el.getDisplayName (), verified < wasDownloaded ? verified : wasDownloaded);
            }

            {
                MessageDigestChecker mdChecker = new MessageDigestChecker(impl.getMessageDigests());
                byte[] buffer = new byte[102400];
                int read;
                try(FileInputStream fis = new FileInputStream(nbmFile)) {
                    while((read = fis.read(buffer)) > 0) {
                        mdChecker.update(buffer, 0, read);
                    }
                }
                if(!mdChecker.validate()) {
                    for (String algorithm : mdChecker.getFailingHashes()) {
                        LOG.log(Level.INFO,
                            "Failed to validate message digest for ''{0}'' expected ''{1}'' got ''{2}''",
                            new Object[]{
                                nbmFile.getAbsolutePath(),
                                mdChecker.getExpectedHashAsString(algorithm),
                                mdChecker.getCalculatedHashAsString(algorithm)
                            });
                    }
                    res = Utilities.MODIFIED;
                } else if (mdChecker.isDigestAvailable() && impl.isCatalogTrusted()) {
                    res = Utilities.TRUSTED;
                }
            }

            if(res == null) {
                try {
                    Collection<CodeSigner> nbmCerts = Utilities.getNbmCertificates(nbmFile);
                    if (nbmCerts == null) {
                        res = Utilities.N_A;
                    } else if (nbmCerts.isEmpty()) {
                        res = Utilities.UNSIGNED;
                    } else {
                        // Iterate all certpaths that can be considered for the NBM
                        // choose the certpath, that has the highest trust level
                        // TRUSTED -> SIGNATURE_VERIFIED -> SIGNATURE_UNVERIFIED
                        // or comes first
                        for (CodeSigner cs : nbmCerts) {
                            String localRes = Utilities.verifyCertificates(cs, trustedCerts, trustedCACerts, validationCerts, validationCACerts);
                            // If there is no previous result or if the local
                            // verification yielded a better result than the
                            // previous result, replace it
                            if (res == null
                                || VERIFICATION_RESULT_COMPARATOR.compare(res, localRes) > 0) {
                                res = localRes;
                                certs.put(el, (List<Certificate>) cs.getSignerCertPath().getCertificates());
                            }
                        }
                    }
                } catch (SecurityException ex) {
                    LOG.log(Level.INFO, "The content of the jar/nbm has been modified or certificate paths were inconsistent - " + ex.getMessage(), ex);
                    res = Utilities.MODIFIED;
                }
            }

            if (res != null) {
                switch (res) {
                    case Utilities.MODIFIED:
                        modified.add(impl);
                        break;
                    case Utilities.TRUSTED:
                    case Utilities.N_A:
                        trusted.add(impl);
                        break;
                    case Utilities.SIGNATURE_VERIFIED:
                        signedVerified.add(impl);
                        break;
                    case Utilities.SIGNATURE_UNVERIFIED:
                        signedUnverified.add(impl);
                        break;
                }
            }
            updateFragmentStatus(impl, nbmFile);
            
        } catch (IOException ioe) {
            LOG.log (Level.INFO, ioe.getMessage (), ioe);
            res = "BAD_DOWNLOAD";
            throw new OperationException (OperationException.ERROR_TYPE.INSTALL,
                    NbBundle.getMessage(InstallSupportImpl.class, "InstallSupportImpl_Validate_CorruptedNBM", nbmFile)); // NOI18N
        } catch (KeyStoreException kse) {
            LOG.log (Level.INFO, kse.getMessage (), kse);
            res = "CORRUPTED";
            throw new OperationException (OperationException.ERROR_TYPE.INSTALL,
                    NbBundle.getMessage(InstallSupportImpl.class, "InstallSupportImpl_Validate_CorruptedNBM", nbmFile)); // NOI18N
        }
        
        LOG.log (Level.FINE, "NBM " + nbmFile + " was verified as " + res);
        return el.getDownloadSize ();
    }
    
    private void updateFragmentStatus(UpdateElementImpl el, File nbmFile) throws IOException {
        UpdateItemImpl impl = el.getInstallInfo().getUpdateItemImpl();
        if (!(impl instanceof ModuleItem)) {
            return;
        }
        ModuleItem mod = (ModuleItem)impl;
        if (mod.isFragment()) {
            String fhost = mod.getFragmentHost();
            Module m = Utilities.toModule(fhost);
            if (m != null && m.isEnabled()) {
                impl.setNeedsRestart(Boolean.TRUE);
            }
        }

        Map<String, UpdateItem> items;
        try {
            items = AutoupdateInfoParser.getUpdateItems(nbmFile);
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        for (UpdateItem realItem : items.values()) {
            UpdateItemImpl realImpl = Trampoline.SPI.impl(realItem);
            if (realImpl instanceof ModuleItem) {
                ModuleItem realMod = (ModuleItem)realImpl;
                if (!realMod.getCodeName().equals(el.getCodeName())) {
                    continue;
                }
                String fhost = realMod.getFragmentHost();
                if (fhost != null && !impl.isFragment()) {
                    mod.setFragmentHost(fhost);
                    Module m = Utilities.toModule(fhost);
                    if (m != null && m.isEnabled()) {
                        impl.setNeedsRestart(Boolean.TRUE);
                    }
                }
            }
        }
    }
    
    private boolean needsRestart (boolean isUpdate, UpdateElementImpl toUpdateImpl, File dest) {
        return InstallManager.needsRestart (isUpdate, toUpdateImpl, dest);
    }
    
    private static final class RefreshModulesListener implements PropertyChangeListener, Runnable  {
        private final ProgressHandle handle;
        private int i;
        private PropertyChangeEvent ev;
        
        public RefreshModulesListener (ProgressHandle handle) {
            this.handle = handle;
            this.i = 0;
        }
        
        @Override
        public void propertyChange(final PropertyChangeEvent ev) {
            if (UpdaterInternal.RUNNING.equals (ev.getPropertyName ())) {
                if (handle != null) {
                    handle.progress (ev.getNewValue() == null ? "" : ev.getNewValue().toString(), i++);
                }
            } else if (UpdaterInternal.FINISHED.equals (ev.getPropertyName ())){
                this.ev = ev;
                FileUtil.runAtomicAction(this);
            } else {
                assert false : "Unknown property " + ev.getPropertyName ();
            }
        }

        @Override
        public void run() {
            for (int loop = 0; loop < 10; loop++) {
                // XXX: the modules list should be refresh automatically when config/Modules/ changes
                Map<File,Long> modifiedFiles = NbCollections.checkedMapByFilter(
                    (Map)ev.getNewValue(), 
                    File.class, Long.class, true
                );
                long now = System.currentTimeMillis();
                for (Map.Entry<File,Long> e : modifiedFiles.entrySet()) {
                    touch(e.getKey(), Math.max(e.getValue(), now));
                }
                FileObject modulesRoot = FileUtil.getConfigFile(ModuleDeactivator.MODULES);
                if (modulesRoot != null) {
                    /* XXX: uncomment when #205120 fixed.
                    LOG.fine("Refreshing Modules directory"); // NOI18N
                    modulesRoot.refresh();
                    LOG.fine("Done refreshing Modules directory"); // NOI18N
                     */
                    LOG.fine("Refreshing whole MFS"); // NOI18N
                    modulesRoot.refresh();
                    try {
                        FileUtil.getConfigRoot().getFileSystem().refresh(true);
                    } catch (FileStateInvalidException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    LOG.fine("Done refreshing MFS"); // NOI18N
                }
                boolean ok = true;
                for (File file : modifiedFiles.keySet()) {
                    String rel = relativePath(file, new StringBuilder());
                    if (rel == null) {
                        continue;
                    }
                    FileObject fo = FileUtil.getConfigFile(rel);
                    if (fo == null) {
                        LOG.log(loop < 5 ? Level.FINE : Level.WARNING, "Cannot find " + rel);
                        ok = false;
                        continue;
                    }
                    LOG.fine("Refreshing " + fo);
                    fo.refresh();
                }
                if (ok) {
                    LOG.log(loop < 5 ? Level.FINE : Level.INFO, "All was OK on " + loop + " th iteration");
                    break;
                }
            }
            // if something was (un)installed, we need to recompute provides-requires info
            UpdateManagerImpl.getInstance().flushComputedInfo();
        }

    }
    
    private static String relativePath(File f, StringBuilder sb) {
        if (f == null) {
            return null;
        }
        if (f.getName().equals("config")) {
            return sb.toString();
        }
        if (sb.length() > 0) {
            sb.insert(0, '/');
        }
        sb.insert(0, f.getName());
        return relativePath(f.getParentFile(), sb);
    }
    
    private static void touch(File f, long minTime) {
        for (int cnt = 0; ;cnt++) {
            long time = f.lastModified();
            if (time > minTime) {
                break;
            }
            if (!f.exists()) {
                LOG.log(Level.FINE, "File {0} does not exist anymore", f);
                break;
            }
            LOG.log(Level.FINE, "Need to change time for {0} with delta {1}", new Object[]{f, minTime - f.lastModified()});
            try { synchronized (InstallSupportImpl.class) {
                InstallSupportImpl.class.wait(30);
            }} catch (InterruptedException ex) {}
            f.setLastModified(System.currentTimeMillis() - 1000);
        }
        LOG.log(Level.FINE, "Time stamp changed succcessfully {0}", f);
    }

    private File getTargetCluster(UpdateElement installed, UpdateElementImpl update, Boolean isGlobal, boolean useUserdirAsFallback) throws OperationException {
        File cluster = getElement2Clusters ().get (update);
        if (cluster == null) {
            cluster = InstallManager.findTargetDirectory (installed, update, isGlobal, useUserdirAsFallback);
            if (cluster != null) {
                getElement2Clusters ().put(update, cluster);
            }
        }
        return cluster;
    }
    
    private  Map<UpdateElementImpl, File> getElement2Clusters () {
        if (element2Clusters == null) {
            element2Clusters = new HashMap<UpdateElementImpl, File> ();
        }
        return element2Clusters;
    }
    
    private ExecutorService getExecutionService () {
        if (es == null || es.isShutdown ()) {
            es = Executors.newSingleThreadExecutor ();
        }
        return es;
    }
    
    // copied from nbbuild/antsrc/org/netbeans/nbbuild/AutoUpdate.java:
    private static InputStream externalDownload(InputStream is, AtomicLong crc, String pathTo) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        URLConnection conn;
        crc.set(-1L);
        String url = null;
        String externalUrl = null;
        IOException ioe = null;
        for (;;) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("CRC:")) {
                crc.set(Long.parseLong(line.substring(4).trim()));
            }
            if (line.startsWith("URL:")) {
                url = line.substring(4).trim();
                for (;;) {
                    int index = url.indexOf("${");
                    if (index == -1) {
                        break;
                    }
                    int end = url.indexOf("}", index);
                    String propName = url.substring(index + 2, end);
                    final String propVal = System.getProperty(propName);
                    if (propVal == null) {
                        throw new IOException("Can't find property " + propName);
                    }
                    url = url.substring(0, index) + propVal + url.substring(end + 1);
                }
                LOG.log(Level.INFO, "Trying external URL: {0}", url);
                try {
                    conn = new URL(url).openConnection();
                    conn.setConnectTimeout(AutoupdateSettings.getOpenConnectionTimeout());
                    conn.setReadTimeout(AutoupdateSettings.getOpenConnectionTimeout());
                    return conn.getInputStream();
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Cannot connect to {0}", url);
                    LOG.log(Level.INFO, "Details", ex);
                    if (ex instanceof UnknownHostException || ex instanceof ConnectException || ex instanceof SocketTimeoutException) {
                        ioe = ex;
                        externalUrl = url;
                    }
                }
            }
        }
        if (ioe == null) {
            throw new FileNotFoundException("Cannot resolve external reference to " + (url == null ? pathTo : url));
        } else {
            throw new IOException("resolving external reference to " + (externalUrl == null ? pathTo : externalUrl));
        }
    }
    
    private static class UpdaterInfo {
        private final JarEntry updaterJarEntry;
        private final File zipFileWithUpdater;
        private final File updaterTargetCluster;

        public UpdaterInfo(JarEntry updaterJarEntry, File updaterJarFile, File updaterTargetCluster) {
            this.updaterJarEntry = updaterJarEntry;
            this.zipFileWithUpdater = updaterJarFile;
            this.updaterTargetCluster = updaterTargetCluster;
        }

        public JarEntry getUpdaterJarEntry() {
            return updaterJarEntry;
        }

        public File getZipFileWithUpdater() {
            return zipFileWithUpdater;
        }

        public File getUpdaterTargetCluster() {
            return updaterTargetCluster;
        }

    }
}
