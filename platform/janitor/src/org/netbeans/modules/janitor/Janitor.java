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
package org.netbeans.modules.janitor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
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

    private static final int UNUSED_DAYS = 30;

    public static final String PROP_JANITOR_ENABLED = "janitorEnabled"; //NOI18N
    public static final String PROP_UNUSED_DAYS = "UnusedDays"; //NOI18N
    public static final String PROP_AUTO_REMOVE_ABANDONED_CACHE = "autoRemoveAbandonedCache";

    private static final String LOGFILE_NAME = "var/log/messages.log"; //NOI18N
    private static final String ALL_CHECKSUM_NAME = "lastModified/all-checksum.txt"; //NOI18N
    @StaticResource
    private static final String CLEAN_ICON = "org/netbeans/modules/janitor/resources/clean.gif"; //NOI18N

    static final RequestProcessor JANITOR_RP = new RequestProcessor("janitor", 1); //NOI18N
    static final Map<ActionListener, Notification> CLEANUP_TASKS = new WeakHashMap<>();

    static void scanForJunk() {
        // Remove previously opened notifications
        CLEANUP_TASKS.values().forEach((nf) -> nf.clear());
        CLEANUP_TASKS.clear();

        Icon clean = ImageUtilities.loadImageIcon(CLEAN_ICON, false);
        List<Pair<String, Integer>> otherVersions = getOtherVersions();

        for (Pair<String, Integer> ver : otherVersions) {
            String name = ver.first();
            Integer age = ver.second();
            long toFree = size(getUserDir(name)) + size(getCacheDir(name));
            toFree = toFree / (1_000_000) + 1;
            if (getUserDir(name) != null) {
                ActionListener cleanupListener = cleanupAction(name, Bundle.TXT_CONFIRM_CLEANUP(name));
                Notification nf = NotificationDisplayer.getDefault().notify(
                        Bundle.TIT_ABANDONED_USERDIR(name, age, toFree),
                        clean,
                        Bundle.DESC_ABANDONED_USERDIR(name, age, toFree),
                        cleanupListener);

                CLEANUP_TASKS.put(cleanupListener, nf);
            } else {
                if (isAutoRemoveAbanconedCache()) {
                    JANITOR_RP.post(() -> cleanup(name));
                } else {
                    ActionListener cleanupListener = cleanupAction(name, Bundle.TXT_CONFIRM_CACHE_CLEANUP(name));
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
    
    static ActionListener cleanupAction(String name, String label) {
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
                    JANITOR_RP.post(() -> cleanup(name));
                }
                Janitor.setEnabled(panel.isEnabledOnStartup());
                Notification nf = CLEANUP_TASKS.get(this);
                if (nf != null) {
                    nf.clear();
                }
            }
        };
    }

    static void cleanup(String name) {
        try (ProgressHandle handle = ProgressHandle.createHandle(Bundle.LBL_CLEANUP(name))){
            handle.start();
            deleteDir(getUserDir(name));
            deleteDir(getCacheDir(name));
        }        
    }
    
    public static final Preferences getPreferences() {
        return NbPreferences.forModule(Janitor.class);
    }

    @OnShowing
    public static final class PlatformOpenHook implements Runnable {

        @Override
        public void run() {
            if (isEnabled()) {
                // Starting delayed, not to interfere with other startup IO operations
                JANITOR_RP.post(Janitor::scanForJunk, 60_000);
            }
        }

    }

    static void runNow() {
        JANITOR_RP.post(Janitor::scanForJunk);
    }

    static File getUserDir(String version) {
        File ret = null;
        File userDir = Places.getUserDirectory();
        if (userDir != null) {
            ret = new File(userDir.getParentFile(), version);
            ret = ret.isDirectory() ? ret : null;
        }

        return ret;
    }

    static File getCacheDir(String version) {
        File ret = null;
        File cacheDir = Places.getCacheDirectory();
        if (cacheDir != null) {
            ret = new File(cacheDir.getParentFile(), version);
            ret = ret.isDirectory() ? ret : null;
        }
        return ret;
    }

    static void deleteDir(File dir) {
        if ((dir == null) || !dir.exists()) return;
        Path path = dir.toPath();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
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
        } catch(IOException ex) {
            // Well we've tried
        }
    }

    public static long size(File f) {

        if (f == null) {
            return 0;
        }
        final Path path = f.toPath();
        final AtomicLong size = new AtomicLong(0);

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
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
        }

        return size.get();
    }

    static List<Pair<String, Integer>> getOtherVersions() {
        File userDir = Places.getUserDirectory();
        List<Pair<String, Integer>> ret = new LinkedList<>();
        Set<String> availableUserDirs = new HashSet<>();
        Instant now = Instant.now();
        if (userDir != null) {
            File userParent = userDir.getParentFile();
            for (File f : userParent.listFiles()) {
                availableUserDirs.add(f.getName());
                Path logFile = new File(f, LOGFILE_NAME).toPath();
                if (!f.equals(userDir) && Files.isRegularFile(logFile)) {
                    try {
                        Instant lastModified = Files.getLastModifiedTime(logFile).toInstant();
                        Integer age = (int) Duration.between(lastModified, now).toDays();
                        if (lastModified.plus(getUnusedDays(), ChronoUnit.DAYS).isBefore(now)) {
                                ret.add(Pair.of(f.getName(), age));
                        }
                    } catch (IOException ex) {
                        //Just ignore what we can't process
                    }
                }
            }
        }

        //Search for abandoned cache dirs (cache dirs with no user dir)
        File cacheDir = Places.getCacheDirectory();
        if (cacheDir != null) {
            File cacheParent = cacheDir.getParentFile();
            for (File f : cacheParent.listFiles()) {
                if (f.isDirectory() && !availableUserDirs.contains(f.getName())) {
                    if (new File(f, ALL_CHECKSUM_NAME).exists() && !cacheDir.equals(f)) {
                        try {
                            Instant lastModified = Files.getLastModifiedTime(f.toPath()).toInstant();
                            Integer age = (int) Duration.between(lastModified, now).toDays();
                            ret.add(Pair.of(f.getName(), age));
                        } catch (IOException ex) {
                            //Just ignore what we can't process
                        }
                    }
                }
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

    static boolean isAutoRemoveAbanconedCache() {
        return getPreferences().getBoolean(PROP_AUTO_REMOVE_ABANDONED_CACHE, true);
    }

    static void setAutoRemoveAbanconedCache(boolean b) {
        getPreferences().putBoolean(PROP_AUTO_REMOVE_ABANDONED_CACHE, b);
    }

}
