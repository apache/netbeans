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
package org.netbeans.modules.janitor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.*;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.modules.Places;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.OnShowing;

/**
 *
 * @author Laszlo Kishalmi
 */
@Messages({
    "# {0} - is the user directory name",
    "# {1} - the days of abandonement",
    "# {2} - the disk space can be reclaimed (in megabytes)",
    "TIT_ABANDONED_USERDIR=NetBeans {0} was last used {1} days ago.",
    "# {0} - the name of the abandoned cache dir.",
    "# {1} - the disk space can be reclaimed (in megabytes)",
    "TIT_ABANDONED_CACHEDIR=NetBeans cache directory {0} seems to be abandoned.",
    "# {0} - is the user directory name",
    "# {1} - the days of abandonement",
    "# {2} - the disk space can be reclaimed (in megabytes)",
    "DESC_ABANDONED_USERDIR=Remove unused data and cache directories of NetBeans {0}. "
    + "Free up {2} MB of disk space.",
    "# {0} - is the cache directory name",
    "# {1} - the disk space can be reclaimed (in megabytes)",
    "DESC_ABANDONED_CACHEDIR=NetBeans could not find a user dir for cache dir {0}, so it is probably abandoned. "
    + "Remove abandoned cache dir, "
    + "free up {1} MB of disk space.",
    "TIT_CONFIRM_CLEANUP=Confirm Cleanup",
    "# {0} - the dirname to be cleaned up",
    "TXT_CONFIRM_CLEANUP=Remove user and cache data for NetBeans {0}?",
    "# {0} - the dirname to be cleaned up",
    "TXT_CONFIRM_CACHE_CLEANUP=Remove abandoned cache dir?",
    "# {0} - the dirname to be cleaned up",
    "LBL_CLEANUP=Removing unused/abandoned user and/or cache dirs."
})
public class Janitor {

    private static final Logger LOG = Logger.getLogger(Janitor.class.getName());
    
    private static final int UNUSED_DAYS = 30;

    public static final String PROP_JANITOR_ENABLED = "janitorEnabled"; //NOI18N
    public static final String PROP_UNUSED_DAYS = "UnusedDays"; //NOI18N
    public static final String PROP_AUTO_REMOVE_ABANDONED_CACHE = "autoRemoveAbandonedCache";

    private static final String LOGFILE_NAME = "var/log/messages.log"; //NOI18N
    private static final String ALL_CHECKSUM_NAME = "lastModified/all-checksum.txt"; //NOI18N
    private static final String LAST_VERSION_NAME = ".lastUsedVersion"; //NOI18N

    private static final String NB_VERSION;

    @StaticResource
    private static final String CLEAN_ICON = "org/netbeans/modules/janitor/resources/clean.gif"; //NOI18N

    static final RequestProcessor JANITOR_RP = new RequestProcessor("janitor", 1); //NOI18N
    static final Map<ActionListener, Notification> CLEANUP_TASKS = new WeakHashMap<>();

    static {
        String version = System.getProperty("netbeans.buildnumber"); //NOI18N
        if (version != null) {
            // remove git hash from the build number
            int dash = version.lastIndexOf('-');
            if (dash + 41 == version.length()) { // 40 chars for git SHA sum, 1 for the dash
                version = version.substring(0, dash);
            }
        }
        NB_VERSION = version;
    }

    static void scanForJunk() {
        // Remove previously opened notifications
        CLEANUP_TASKS.values().forEach((nf) -> nf.clear());
        CLEANUP_TASKS.clear();

        Icon clean = ImageUtilities.loadImageIcon(CLEAN_ICON, false);
        List<CleanupPair> candidates = getCandidates();

        Instant now = Instant.now();
        int maxUnused = getUnusedDays();
        
        for (CleanupPair candidate : candidates) {
            int age = candidate.age(now);
            int toFree = candidate.size();
            String name = candidate.getName();
            if (candidate.userdir != null) {
                if (age > maxUnused) {
                    ActionListener cleanupListener = cleanupAction(candidate, Bundle.TXT_CONFIRM_CLEANUP(name));
                    Notification nf = NotificationDisplayer.getDefault().notify(
                            Bundle.TIT_ABANDONED_USERDIR(name, age, toFree),
                            clean,
                            Bundle.DESC_ABANDONED_USERDIR(name, age, toFree),
                            cleanupListener);

                    CLEANUP_TASKS.put(cleanupListener, nf);
                }
            } else {
                if (isAutoRemoveAbandonedCache()) {
                    LOG.log(Level.INFO, "Janitor autoremove abandoned cache: " + candidate.cachedir.dir);
                    JANITOR_RP.post(() -> cleanup(candidate));
                } else {
                    ActionListener cleanupListener = cleanupAction(candidate, Bundle.TXT_CONFIRM_CACHE_CLEANUP(name));
                    Notification nf = NotificationDisplayer.getDefault().notify(
                            Bundle.TIT_ABANDONED_CACHEDIR(name, toFree),
                            clean,
                            Bundle.DESC_ABANDONED_CACHEDIR(name, toFree),
                            cleanupListener);
                    CLEANUP_TASKS.put(cleanupListener, nf);
                }
            }
        }
    }

    static ActionListener cleanupAction(CleanupPair cp, String label) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JanitorPanel panel = new JanitorPanel(label);
                DialogDescriptor descriptor = new DialogDescriptor(
                        panel,
                        Bundle.TIT_CONFIRM_CLEANUP(),
                        true,
                        DialogDescriptor.YES_NO_OPTION,
                        DialogDescriptor.YES_OPTION,
                        null
                );
                if (DialogDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(descriptor)) {
                    JANITOR_RP.post(() -> cleanup(cp));
                }
                Janitor.setEnabled(panel.isEnabledOnStartup());
                Notification nf = CLEANUP_TASKS.get(this);
                if (nf != null) {
                    nf.clear();
                }
            }
        };
    }

    static void cleanup(CleanupPair cp) {
        try (ProgressHandle handle = ProgressHandle.createHandle(Bundle.LBL_CLEANUP(cp.getName()))) {
            handle.start();
            cp.delete();
        }
    }

    public static final Preferences getPreferences() {
        return NbPreferences.forModule(Janitor.class);
    }

    @OnShowing
    public static final class PlatformOpenHook implements Runnable {

        @Override
        public void run() {
            JANITOR_RP.post(Janitor::markUserCacheDirs, 5_000);
            if (isEnabled()) {
                // Starting delayed, not to interfere with other startup IO operations
                JANITOR_RP.post(Janitor::scanForJunk, 60_000);
            }
        }

    }

    static void markUserCacheDirs() {
        writeVersion(Places.getCacheDirectory());
        writeVersion(Places.getUserDirectory());
    }

    static void runNow() {
        JANITOR_RP.post(Janitor::scanForJunk);
    }


    static void writeVersion(File baseDir) {
        File lastUsedVersion = new File(baseDir, LAST_VERSION_NAME);
        if (NB_VERSION != null) {
            try (FileWriter fw = new FileWriter(lastUsedVersion)) {
                fw.write(NB_VERSION);
            } catch (IOException ex) {
                // do nothing we've tried...
                LOG.log(Level.FINE, "Could not write version info." , ex); //NOI18N
            }
        }
    }

    static List<CleanupPair> getCandidates() {
        Set<String> names = new HashSet<>();
        File userDir = Places.getUserDirectory();
        if (userDir != null) {
            File userParent = userDir.getParentFile();
            for (File f : userParent.listFiles()) {
                if (f.isDirectory() && !f.equals(userDir)) {
                    names.add(f.getName());
                }
            }
        }

        File cacheDir = Places.getCacheDirectory();
        if (cacheDir != null) {
            File cacheParent = cacheDir.getParentFile();
            for (File f : cacheParent.listFiles()) {
                if (f.isDirectory() && !f.equals(cacheDir)) {
                    names.add(f.getName());
                }
            }
        }
        
        List<CleanupPair> ret = new LinkedList<>();
        for (String name : names) {
            CleanupDir user = CleanupDir.get(CleanupDir.Kind.USERDIR, name);
            CleanupDir cache = CleanupDir.get(CleanupDir.Kind.CACHEDIR, name);
            if (user != null || cache != null) {
                
                ret.add(new CleanupPair(user, cache));
            }
        }
        return ret;
    }

    static void setEnabled(boolean b) {
        getPreferences().putBoolean(PROP_JANITOR_ENABLED, b);
    }

    static boolean isEnabled() {
        return getPreferences().getBoolean(PROP_JANITOR_ENABLED, true);
    }

    static void setUnusedDays(int days) {
        getPreferences().putInt(PROP_UNUSED_DAYS, days);
    }

    static int getUnusedDays() {
        return getPreferences().getInt(PROP_UNUSED_DAYS, UNUSED_DAYS);
    }

    static boolean isAutoRemoveAbandonedCache() {
        return getPreferences().getBoolean(PROP_AUTO_REMOVE_ABANDONED_CACHE, true);
    }

    static void setAutoRemoveAbandonedCache(boolean b) {
        getPreferences().putBoolean(PROP_AUTO_REMOVE_ABANDONED_CACHE, b);
    }

    private static class CleanupDir {

        enum Kind {USERDIR, CACHEDIR};
        
        private final Path dir;
        private final Kind kind; 

        private CleanupDir(Path dir, Kind kind) {
            this.dir = dir;
            this.kind = kind;
        }

        static CleanupDir get(CleanupDir.Kind kind, String version) {
            Path dir = kind == CleanupDir.Kind.USERDIR ? Places.getUserDirectory().toPath() : Places.getCacheDirectory().toPath();
            Path f = dir.getParent().resolve(version);
            Path test = f.resolve((kind == CleanupDir.Kind.USERDIR) ? LOGFILE_NAME : ALL_CHECKSUM_NAME);
            return !f.equals(dir) && Files.isDirectory(f) && Files.isRegularFile(test) ? new CleanupDir(f, kind) : null;
        }

        public long size() {

            if (dir == null) {
                return 0;
            }
            final AtomicLong size = new AtomicLong(0);

            try {
                Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        size.addAndGet(attrs.size());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                LOG.log(Level.FINE, "Something went wrong calculating the size of " + dir, e); //NOI18N
            }

            return size.get();
        }

        public String getName() {
            String name = dir.getFileName().toString();
            Path f = dir.resolve(LAST_VERSION_NAME);
            if (Files.isRegularFile(f)) {
                try {
                    if (Files.size(f) < 100) {
                        try (BufferedReader br = Files.newBufferedReader(f)) {
                            name = br.readLine();
                        }
                    } else{
                        LOG.log(Level.WARNING, "Skipped version file " + f + " as it is suspiciously large."); //NOI18N
                    }
                } catch (IOException ex) {
                    // Could not read the file, stick to the dirname
                }
            } else {
                LOG.log(Level.INFO, f.toString() + " is missing fallback to dirname: " + name); //NOI18N
                switch (name) { // Map a few elder Snap release revision to IDE version number, these could be removed in NetBeans 21/22
                    case "80":
                        return "18";
                    case "76":
                        return "17";
                    case "74":
                        return "16";
                    case "69":
                        return "15";
                }
            }
            return name;
        }
        
        public int age(Instant now) {
            int ret = -1;
            Path f;
            switch (kind) {
                case CACHEDIR:
                    f = dir.resolve(ALL_CHECKSUM_NAME);
                    break;
                default:
                    f = dir.resolve(LOGFILE_NAME);
            }
            if (Files.isRegularFile(f)) {
                    try {
                        Instant lastModified = Files.getLastModifiedTime(f).toInstant();
                        ret = (int) Duration.between(lastModified, now).toDays();
                    } catch (IOException ex) {
                        //Just ignore what we can't process
                    }                
            }
            return ret;
        }
        
        public void delete() {
            try {
                Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                // Well we've tried
                LOG.log(Level.INFO, "Janitor couldn't remove " + dir.toString(), ex); //NOI18N
            }
        }
    }

    private static class CleanupPair {
        final CleanupDir userdir;
        final CleanupDir cachedir;

        public CleanupPair(CleanupDir userdir, CleanupDir cachedir) {
            this.userdir = userdir;
            this.cachedir = cachedir;
            if ((userdir == null) && (cachedir == null)) {
                throw new IllegalArgumentException("Both user and cache dirs cannot be null!"); //NOI18N
            }
        }
        
        public String getName() {
            return userdir != null ? userdir.getName() : cachedir.getName();
        }

        public int age(Instant now) {
            return userdir != null ? userdir.age(now) : cachedir.age(now);
        }
        
        public int size() {
            int sum = 0;
            sum += userdir != null ? userdir.size() : 0;
            sum += cachedir != null ? cachedir.size() : 0;
            return sum / (1_000_000) + 1;
        }
        
        public void delete() {
            if (userdir != null) userdir.delete();
            if (cachedir != null) cachedir.delete();
        }
        
    }    

}
