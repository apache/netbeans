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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.filebasedfs.utils.Utils;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * FileLock with support for fine grained hard locking to ensure better performance
 * @author Radek Matous
 */
public class LockForFile extends FileLock {

    private static final ConcurrentHashMap<String, Namesakes> name2Namesakes =
            new ConcurrentHashMap<String, Namesakes>();
            static final String PREFIX = ".LCK";
            static final String SUFFIX = "~";
    private static final Logger LOGGER = Logger.getLogger(LockForFile.class.getName());
    private File file;
    private HardLockPath lock;
    private boolean valid = false;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                hardUnlockAll();
            }
        });
    }

    private LockForFile(File file) {
        super();
        this.file = file;
        this.lock = getLockFile(file);
    }

    public static LockForFile findValid(final File file) {
        Namesakes namesakes = name2Namesakes.get(file.getName());
        return (namesakes != null) ? namesakes.getInstance(file) : null;
    }

    public static LockForFile tryLock(final File file) throws IOException {
        LockForFile result = new LockForFile(file);
        return registerLock(result);
    }

    private static LockForFile registerLock(LockForFile result) throws IOException, FileAlreadyLockedException {
        File file = result.getFile();
        Namesakes namesakes = new Namesakes();
        Namesakes oldNamesakes = name2Namesakes.putIfAbsent(file.getName(), namesakes);
        if (oldNamesakes != null) {
            namesakes = oldNamesakes;
        }
        if (namesakes.putInstance(file, result) == null) {
            FileAlreadyLockedException alreadyLockedException = new FileAlreadyLockedException(file.getAbsolutePath());
            LockForFile previousLock = namesakes.getInstance(file);
            // #151576 - check for null although it should not happen
            if (previousLock != null) {
                alreadyLockedException.initCause(previousLock.lockedBy);
            }
            throw alreadyLockedException;
        }
        result.valid = true;
        return result;
    }

    public static void relock(final File theOld, File theNew) {
        if (theNew.isDirectory()) {
            Collection<Namesakes> namesakes = name2Namesakes.values();
            for (Namesakes sake : namesakes) {
                Collection<Reference<LockForFile>> all = sake.values();
                for (Reference<LockForFile> ref : all) {
                    LockForFile lock = ref.get();
                    if (lock != null) {
                        File f = lock.getFile();
                        String relPath = Utils.getRelativePath(theOld, f);
                        if (relPath != null) {
                            lock.relock(new File(theNew, relPath));
                        }
                    }
                }
            }
        } else {
            LockForFile lock = findValid(theOld);
            if (lock != null) {
                lock.relock(theNew);
            }
        }
    }


    private static synchronized void deregisterLock(LockForFile lockForFile) {
        if (lockForFile.isValid()) {
            if (lockForFile.isHardLocked()) {
                lockForFile.hardUnlock();
            }
            File file = lockForFile.getFile();
            Namesakes namesakes = name2Namesakes.get(file.getName());
            if (namesakes != null) {
                namesakes.remove(file);
                if (namesakes.isEmpty()) {
                    name2Namesakes.remove(file.getName());
                }
            }
        }
    }

    private void relock(File theNew) {
        try {
            LockForFile.deregisterLock(this);
            this.file = theNew;
            this.lock = LockForFile.getLockFile(theNew);
            registerLock(this);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /*not private for tests*/
    boolean hardLock() throws IOException {
        if (isHardLocked()) {
            throw new FileAlreadyLockedException(file.getAbsolutePath());
        }

        HardLockPath hardLock = getLock();

        HardLockRegistry.getInstance().doHardLock(hardLock, getFile().getAbsolutePath());

        return true;
    }

    /*not private for tests*/
    boolean hardUnlock() {
        return HardLockRegistry.getInstance().doHardUnlock(getLock());
    }

    private static synchronized boolean hardUnlockAll() {
        boolean result = true;
        Collection<Namesakes> sakes = name2Namesakes.values();
        for (LockForFile.Namesakes namesake : sakes) {
            Collection<Reference<LockForFile>> refs = namesake.values();
            for (Reference<LockForFile> reference : refs) {
                if (reference != null) {
                    LockForFile lockForFile = reference.get();
                    if (lockForFile.isHardLocked()) {
                        if (!lockForFile.hardUnlock()) {
                            result = false;
                        }
                    }
                }
            }
        }
        return result;
    }

    public HardLockPath getLock() {
        return lock;
    }

    public File getFile() {
        return file;
    }

    public File getHardLock() {
        String content = HardLockRegistry.getInstance().getHardLockContent(lock);
        if (content != null) {
            return new File(content);
        }
        return null;
    }

    public boolean isHardLocked() {
        File hLock = getHardLock();
        return (hLock != null) ? findValid(hLock) != null : false;
    }

    public void rename() {

    }

    public static HardLockPath getLockFile(File file) {
        file = FileUtil.normalizeFile(file);

        final File parentFile = file.getParentFile();
        final StringBuilder sb = new StringBuilder();

        sb.append(LockForFile.PREFIX);//NOI18N
        sb.append(file.getName());//NOI18N
        sb.append(LockForFile.SUFFIX);//NOI18N

        final String lckName = sb.toString();
        final File lck = new File(parentFile, lckName);
        return new HardLockPath(lck.getAbsoluteFile());
    }

    @Override
    public boolean isValid() {
        Namesakes namesakes = name2Namesakes.get(file.getName());
        Reference<LockForFile> ref = (namesakes != null) ? namesakes.get(file) : null;
        return (ref != null && super.isValid() && valid);
    }

    @Override
    public void releaseLock() {
        releaseLock(true);
    }
    final void releaseLock(boolean notify) {
        LockForFile.deregisterLock(this);
        super.releaseLock();
        if (notify) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            if (fo instanceof BaseFileObj) {
                ((BaseFileObj) fo).getProvidedExtensions().fileUnlocked(fo);
            }
        }
    }

    private static class Namesakes extends ConcurrentHashMap<File, Reference<LockForFile>> {

        private LockForFile getInstance(File file) {
            Reference<LockForFile> ref = get(file);
            return (ref != null) ? ref.get() : null;
        }

        private LockForFile putInstance(File file, LockForFile lock) throws IOException {
            if (!isEmpty() && findValid(lock.getFile()) == null) {
                hardLock();
                lock.hardLock();
            }
            Reference<LockForFile> old = putIfAbsent(file, new WeakReference<LockForFile>(lock));
            return (old != null) ? null : lock;
        }

        private void hardLock() throws IOException {
            Collection<Reference<LockForFile>> refs = values();
            for (Reference<LockForFile> reference : refs) {
                if (reference != null) {
                    LockForFile lockForFile = reference.get();
                    if (lockForFile != null) {
                        if (!HardLockRegistry.getInstance().hardLockExists(lockForFile.getLock())) {
                            lockForFile.hardLock();
                        }
                    }
                }
            }
        }
    }

    public static boolean hasActiveLockFileSigns(final String filename) {
        return filename.startsWith(PREFIX) && filename.endsWith(SUFFIX);
    }

    static final class HardLockPath {
        public final File file;

        public HardLockPath(File file) {
            this.file = file;
        }

    }

    /*not private for tests*/
    public static interface HardLockRegistry {
        public static HardLockRegistry getInstance() {
            return HardLockRegistryInstance.INSTANCE;
        }

        public void doHardLock(HardLockPath hardLock, String absolutePath) throws IOException;

        public boolean doHardUnlock(HardLockPath hardLock);

        public boolean hardLockExists(HardLockPath hardLock) throws IOException;

        public String getHardLockContent(HardLockPath hardLock);

    }

    //init the HardLockRegistry instance lazily:
    private static final class HardLockRegistryInstance {
        public static final HardLockRegistry INSTANCE = Lookup.getDefault().lookup(HardLockRegistry.class);
    }

    @ServiceProvider(service=HardLockRegistry.class, position=100)
    public static final class FileBaseHardLockRegistry implements HardLockRegistry {

        public void doHardLock(HardLockPath hardLock, String absolutePath) throws IOException {
            File hardLockFile = hardLock.file;
            hardLockFile.getParentFile().mkdirs();
            hardLockFile.createNewFile();
            OutputStream os = Files.newOutputStream(hardLockFile.toPath());
            try {
                os.write(absolutePath.getBytes());
            } finally {
                os.close();
            }
        }

        public boolean doHardUnlock(HardLockPath hardLock) {
            return hardLock.file.delete();
        }

        public boolean hardLockExists(HardLockPath hardLock) throws IOException {
            return FileChangedManager.getInstance().exists(hardLock.file);
        }

        public String getHardLockContent(HardLockPath hardLock) {
            if (FileChangedManager.getInstance().exists(hardLock.file)) {
                InputStream is = null;
                try {
                    is = new FileInputStream(hardLock.file);
                    byte[] path = new byte[is.available()];
                    if (path.length > 0 && is.read(path) == path.length) {
                        return new String(path);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }

            return null;
        }

    }

    @ServiceProvider(service=HardLockRegistry.class, position=1000)
    public static final class InMemoryHardLockRegistry implements HardLockRegistry {
        private final ConcurrentHashMap<String, String> hardLocks2Content =
                new ConcurrentHashMap<>();

        private static String getKey(HardLockPath hardLock) throws IOException {
            Path realParentPath = hardLock.file.toPath().getParent().toRealPath();
            return realParentPath.resolve(hardLock.file.getName()).toString();
        }

        public void doHardLock(HardLockPath hardLock, String absolutePath) throws IOException {
            hardLocks2Content.put(getKey(hardLock), absolutePath);
        }

        public boolean doHardUnlock(HardLockPath hardLock) {
            try {
                return hardLocks2Content.remove(getKey(hardLock)) != null;
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
                return false;
            }
        }

        public boolean hardLockExists(HardLockPath hardLock) throws IOException {
            return hardLocks2Content.containsKey(getKey(hardLock));
        }

        public String getHardLockContent(HardLockPath hardLock) {
            try {
                return hardLocks2Content.get(getKey(hardLock));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

    }
}
