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

package org.netbeans.modules.ide.ergonomics.fod;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.Module;
import org.netbeans.api.project.Project;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.ModuleSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jirka Rechtacek
 */
public final class FeatureManager
implements PropertyChangeListener, LookupListener {
    private static FeatureManager INSTANCE;
    private static final Logger UILOG = Logger.getLogger("org.netbeans.ui.ergonomics"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("FoD Processor"); // NOI18N

    private final Lookup.Result<ModuleInfo> result;
    private final ChangeSupport support;
    private Set<String> enabledCnbs = Collections.emptySet();

    private FeatureManager() {
        support = new ChangeSupport(this);
        result = Lookup.getDefault().lookupResult(ModuleInfo.class);
        result.addLookupListener(this);
        resultChanged(null);
    }

    public static synchronized FeatureManager getInstance () {
        if (INSTANCE == null) {
            INSTANCE = new FeatureManager();
        }
        return INSTANCE;
    }
    
    public RequestProcessor.Task create(Runnable r) {
        return RP.create(r);
    }
    public RequestProcessor.Task create(Runnable r, boolean finished) {
        return RP.create(r, finished);
    }


    static void logUI(String msg, Object... params) {
        LogRecord rec = new LogRecord(Level.FINE, msg);
        rec.setResourceBundleName("org.netbeans.modules.ide.ergonomics.fod.Bundle"); // NOI18N
        rec.setResourceBundle(NbBundle.getBundle(ConfigurationPanel.class));
        rec.setParameters(params);
        rec.setLoggerName(UILOG.getName());
        UILOG.log(rec);
    }

    static boolean showInAU(ModuleInfo mi) {
        return Main.getModuleSystem().isShowInAutoUpdateClient(mi);
    }


    public static Map<String,String> nbprojectTypes() {
        return FeatureInfo.nbprojectTypes();
    }

    public static Map<String,String> projectFiles() {
        return FeatureInfo.projectFiles();
    }

    public static Collection<? extends FeatureInfo> features() {
        return featureTypesLookup().lookupAll(FeatureInfo.class);
    }

    /** @return feature info that contains given cnb in its cnbs or null if
     * not found
     */
    static FeatureInfo findInfo(String cnb) {
        for (FeatureInfo fi : features()) {
            if (fi.getCodeNames().contains(cnb)) {
                return fi;
            }
        }
        return null;
    }

    /** Returns the amount of (partially) enabled clusters, or -1 if not
     * computed.
     * @return
     */
    public static int dumpModules() {
        return dumpModules(Level.FINE, Level.FINEST);
    }
    /** Returns the amount of (partially) enabled clusters, or -1 if not
     * computed.
     * @param withLevel with what severity dump the modules?
     * @param detailsLevel level to print detailed infos
     * @return
     */
    public static int dumpModules(Level withLevel, Level detailsLevel) {
        if (!FoDLayersProvider.LOG.isLoggable(withLevel)) {
            return -1;
        }
        int cnt = 0;
        Collection<? extends ModuleInfo> allModules = Lookup.getDefault().lookupAll(ModuleInfo.class);
        for (FeatureInfo info : features()) {
            Set<String> enabled = new TreeSet<String>();
            Set<String> disabled = new TreeSet<String>();
            for (ModuleInfo m : allModules) {
                if (info.getCodeNames().contains(m.getCodeNameBase())) {
                    if (m.isEnabled()) {
                        enabled.add(m.getCodeNameBase());
                    } else {
                        disabled.add(m.getCodeNameBase());
                    }
                }
            }
            if (enabled.isEmpty() && disabled.isEmpty()) {
                FoDLayersProvider.LOG.log(withLevel, info.clusterName + " not present"); // NOTICES
                continue;
            }
            if (enabled.isEmpty()) {
                FoDLayersProvider.LOG.log(withLevel, info.clusterName + " disabled"); // NOTICES
                continue;
            }
            if (disabled.isEmpty()) {
                FoDLayersProvider.LOG.log(withLevel, info.clusterName + " enabled"); // NOTICES
                cnt++;
                continue;
            }
            FoDLayersProvider.LOG.log(withLevel,
                info.clusterName + " enabled " + enabled.size() + " disabled " + disabled.size()); // NOTICES
            cnt++;
            for (String cnb : disabled) {
                FoDLayersProvider.LOG.log(detailsLevel, "- " + cnb); // NOI18N
            }
            for (String cnb : enabled) {
                FoDLayersProvider.LOG.log(detailsLevel, "+ " + cnb); // NOI18N
            }
        }
        return cnt;
    }

    /** Used from tests */
    public static synchronized void assignFeatureTypesLookup(Lookup lkp) {
        boolean eaOn = false;
        assert eaOn = true;
        if (!eaOn) {
            throw new IllegalStateException();
        }
        featureTypesLookup = lkp;
        noCnbCheck = true;
    }

    private static Lookup featureTypesLookup;
    private static boolean noCnbCheck;
    private static synchronized Lookup featureTypesLookup() {
        if (featureTypesLookup != null) {
            return featureTypesLookup;
        }

        String clusters = System.getProperty("netbeans.dirs");
        if (clusters == null) {
            featureTypesLookup = Lookup.EMPTY;
        } else {
            InstanceContent ic = new InstanceContent();
            AbstractLookup l = new AbstractLookup(ic);
            String[] paths = clusters.split(File.pathSeparator);
            for (String c : paths) {
                int last = c.lastIndexOf(File.separatorChar);
                String clusterName = c.substring(last + 1).replaceFirst("[0-9\\.]*$", "");
                String basename = "/org/netbeans/modules/ide/ergonomics/" + clusterName;
                String layerName = basename + "/layer.xml";
                String bundleName = basename + "/Bundle.properties";
                URL layer = FeatureManager.class.getResource(layerName);
                URL bundle = FeatureManager.class.getResource(bundleName);
                if (bundle != null) {
                    FeatureInfo info;
                    try {
                        info = FeatureInfo.create(clusterName, layer, bundle);
                        ic.add(info);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            featureTypesLookup = l;
        }
        return featureTypesLookup;
    }

    public void addChangeListener(ChangeListener l) {
        support.addChangeListener(l);
    }
    public void removeChangeListener(ChangeListener l) {
        support.removeChangeListener(l);
    }

    public void resultChanged(LookupEvent ev) {
        for (ModuleInfo m : result.allInstances()) {
            m.removePropertyChangeListener(this);
            m.addPropertyChangeListener(this);
        }
        Set<String> tmp = new HashSet<String>();
        for (ModuleInfo mi : result.allInstances()) {
            if (mi.isEnabled()) {
                tmp.add(mi.getCodeNameBase());
            }
        }
        enabledCnbs = tmp;
        if (ev != null) {
            fireChange();
        }
    }
    public void propertyChange(PropertyChangeEvent evt) {
        if (ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {
            ModuleInfo mi = (ModuleInfo)evt.getSource();
            if (!noCnbCheck && enabledCnbs.contains(mi.getCodeNameBase()) && mi.isEnabled()) {
                return;
            }
            fireChange();
            if (mi.isEnabled()) {
                enabledCnbs.add(mi.getCodeNameBase());
            } else {
                enabledCnbs.remove(mi.getCodeNameBase());
            }
        }
    }

    private void fireChange() {
        FoDLayersProvider.LOG.fine("Firing FeatureManager change"); // NOI18N
        for (FeatureInfo f : features()) {
            f.clearCache();
        }
        support.fireChange();
        FoDLayersProvider.LOG.fine("FeatureManager change delivered"); // NOI18N
    }

    /** Useful for testing */
    public final void waitFinished() {
        RP.post(new Runnable() {
            public void run() {
            }
        }).waitFinished();
    }


    //
    // Features Off Demand
    //
    private static final Logger LOG = Logger.getLogger(FeatureManager.class.getName());
    static void associateFiles(List<FileObject> enabled) {
        long when = 0;
        for (FileObject f : enabled) {
            long t = f.lastModified().getTime();
            if (t > when) {
                when = t;
            }
        }
        for (FileObject f : enabled) {
            LOG.log(Level.FINE, "Enabling ErgoControl for {0}", f);
            try {
                f.setAttribute("ergonomicsEnabled", when); // NOI18N
                f.setAttribute("ergonomicsUnused", 0); // NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static void incrementUnused(Project[] projects) throws IOException {
        final FileObject fo = FileUtil.getConfigFile("Modules"); // NOI18N
        if (fo == null) {
            return;
        }
        final FileObject[] arr = fo.getChildren();

        Set<String> enabled = new HashSet<String>();
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            Module m = (Module) mi;
            if (m.isAutoload() || m.isEager() || m.isFixed()) {
                continue;
            }
            if (m.isEnabled()) {
                enabled.add(m.getCodeNameBase());
            }
        }


        Map<String,Long> cnb2Date = new HashMap<String, Long>();
        Map<Long,List<FileObject>> date2Files = new HashMap<Long,List<FileObject>>();
        Set<String> explicitlyUsedCnbs = new HashSet<String>();
        for (int i = 0; i < arr.length; i++) {
            final FileObject module = arr[i];
            final String cnb = module.getName().replace('-', '.');
            final Object when = module.getAttribute("ergonomicsEnabled"); // NOI18N
            LOG.log(Level.FINEST, "Controlling {0}: {1}", new Object[] { module, when });
            if (!(when instanceof Long) || ((Long)when) < module.lastModified().getTime()) {
                if (enabled.contains(cnb)) {
                    explicitlyUsedCnbs.add(cnb);
                }
                continue;
            }
            final Long date = (Long) when;
            cnb2Date.put(cnb, date);
            List<FileObject> files = date2Files.get(date);
            if (files == null) {
                files = new ArrayList<FileObject>();
                date2Files.put(date, files);
            }
            files.add(module);
        }

        Set<String> transitivelyUsedCnbs = transitiveDeps(explicitlyUsedCnbs);

        List<FeatureInfo> unused = new ArrayList<FeatureInfo>();
        NEXT_FEATURE: for (FeatureInfo fi : FeatureManager.features()) {
            for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
                if (!isModuleFrom(mi, fi.clusterName)) {
                    continue;
                }
                if (transitivelyUsedCnbs.contains(mi.getCodeNameBase())) {
                    LOG.log(Level.FINE, "Transitive dependency on {0}", mi.getCodeNameBase());
                    markUsed(unused, fi, cnb2Date, date2Files);
                    continue NEXT_FEATURE;
                }
            }
            unused.add(fi);
        }
        for (int i = 0; i < projects.length; i++) {
            FeatureProjectFactory.Data d = new FeatureProjectFactory.Data(
                projects[i].getProjectDirectory(), true
            );

            for (FeatureInfo fi : FeatureManager.features()) {
                if (!fi.isEnabled()) {
                    continue;
                }
                if (fi.isProject(d) == 0) {
                    boolean markedProject = false;
                    try {
                        String markerClass = fi.getExtraProjectMarkerClass();
                        if (markerClass != null) {
                            Class testClass = projects[i].getClass().getClassLoader().loadClass(markerClass);
                            markedProject |= projects[i].getLookup().lookup(testClass) != null;
                        }
                    } catch (ClassNotFoundException ex) {}
                    if (!markedProject) {
                        continue;
                    }
                }
                markUsed(unused, fi, cnb2Date, date2Files);
            }
        }

        Set<FileObject> processed = new HashSet<FileObject>();
        for (FeatureInfo fi : unused) {
            LOG.log(Level.FINE, "Unused feature {0}", fi.clusterName);
            Long groupId = null;
            for (String cnb : fi.getCodeNames()) {
                if (groupId == null) {
                    groupId = cnb2Date.get(cnb);
                    if (groupId == null) {
                        break;
                    }
                }
                if (!groupId.equals(cnb2Date.get(cnb))) {
                    date2Files.remove(groupId);
                    groupId = null;
                    break;
                }
            }
            if (groupId != null) {
                for (List<FileObject> list : date2Files.values()) {
                    for (FileObject increment : list) {
                        if (!processed.add(increment)) {
                            continue;
                        }
                        int now = 0;
                        Object obj = increment.getAttribute("ergonomicsUnused"); // NOI18N
                        if (obj instanceof Number) {
                            now = ((Number)obj).intValue();
                        }
                        now++;
                        increment.setAttribute("ergonomicsUnused", now); // NOI18N
                        LOG.log(Level.FINE, "Incremented to {0} for {1}", new Object[] { now, increment });
                    }
                }
            }
        }
    }
    public static void disableUnused(int howMuch) throws Exception {
        FileObject fo = FileUtil.getConfigFile("Modules"); // NOI18N
        final FileObject[] arr = fo.getChildren();
        boolean first = true;
        for (int i = 0; i < arr.length; i++) {
            FileObject module = arr[i];
            final Object when = module.getAttribute("ergonomicsEnabled"); // NOI18N
            LOG.log(Level.FINEST, "Checking {0}: {1}", new Object[] { module, when });
            if (!(when instanceof Long) || ((Long) when) < module.lastModified().getTime()) {
                continue;
            }
            final Object unused = module.getAttribute("ergonomicsUnused"); // NOI18N
            LOG.log(Level.FINEST, "Unused {0}", unused);
            if (!(unused instanceof Number) || ((Number)unused).intValue() < howMuch) {
                continue;
            }
            String cnb = module.getName().replace('-', '.');
            if (first) {
                LOG.info("Long time unused modules found, disabling:"); // NOI18N
                first = false;
            }
            LOG.info(cnb);
            module.revert();
        }
    }

    private static Set<String> transitiveDeps(Set<String> cnbs) {
        HashSet<String> all = new HashSet<String>();
        all.addAll(cnbs);
        for (;;) {
            int prev = all.size();
            for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
                if (all.contains(mi.getCodeNameBase())) {
                    for (Dependency d : mi.getDependencies()) {
                        if (d.getType() != Dependency.TYPE_MODULE) {
                            continue;
                        }
                        String moduleName = d.getName();
                        int slash = moduleName.indexOf('/');
                        if (slash != -1) {
                            moduleName = moduleName.substring(0, slash);
                        }
                        all.add(moduleName);
                    }
                }
            }
            if (prev == all.size()) {
                Set<String> test = null;
                assert (test = new HashSet<String>(all)) != null;
                if (test != null) {
                    for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
                        test.remove(mi.getCodeNameBase());
                    }
                    assert test.isEmpty() : "Only CNBs are in the set: " + test;
                }
                return all;
            }
        }
    }
    
    static boolean isModuleFrom(ModuleInfo mi, String prefix) {
        File f;
        if (mi instanceof Module) {
            f = ((Module)mi).getJarFile();
        } else {
            return false;
        }
        if (f != null && f.getParentFile().getName().equals("modules")) { // NOI18N
            if (f.getParentFile().getParentFile().getName().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
    private static void markUsed(
        List<FeatureInfo> unused,
        FeatureInfo fi,
        Map<String, Long> cnb2Date,
        Map<Long, List<FileObject>> date2Files
    ) throws IOException {
        unused.remove(fi);
        for (String cnb : fi.getCodeNames()) {
            final Long thisIsUsedGroup = cnb2Date.get(cnb);
            final List<FileObject> files = date2Files.get(thisIsUsedGroup);
            if (files != null) {
                for (FileObject usedFile : files) {
                    Object prev = usedFile.getAttribute("ergonomicsUnused"); // NOI18N
                    if (!(prev instanceof Number) || ((Number) prev).intValue() == 0) {
                        LOG.log(Level.FINE, "Already marked as used: {0}", usedFile);
                        continue;
                    }
                    LOG.log(Level.FINE, "Marking {0} as used", usedFile);
                    usedFile.setAttribute("ergonomicsUnused", 0); // NOI18N
                }
                date2Files.remove(thisIsUsedGroup);
            }
        }
    }
}
