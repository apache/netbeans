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

package org.netbeans.modules.cnd.discovery.performance;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.FileStore;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class FileSystemChecker {

    private static final int SOCKET_CONNECTION_TIMEOUT = 5 * 1000;
    private static final int TIME_TRASH_HOLD = 10;
    private static final int LS_SPEED_TRASH_HOLD = 300;
    private static final int READ_SPEED_TRASH_HOLD = 100;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            usages();
            return;
        }
        String root = null;
        int depth = 0;
        boolean checkLinks = false;
        for (int i = 0; i < args.length; i++) {
            if ("-path".equals(args[i]) && i + 1 < args.length) { // NOI18N
                root = args[i + 1];
            } else if ("-depth".equals(args[i]) && i + 1 < args.length) { // NOI18N
                depth = Integer.parseInt(args[i + 1]);
            } else if ("-link".equals(args[i])) { // NOI18N
                checkLinks = true;
            }
        }
        if (root == null || depth == 0) {
            usages();
            return;
        }
        Path resolve = Paths.get(root);
        if (Files.exists(resolve)) {
            go(resolve, depth, checkLinks);
        } else {
            System.err.println("Path " + resolve + " does not exist"); // NOI18N
        }
    }

    private static void go(Path root, int depth, boolean checkLinks) throws IOException {
        Set<FileVisitOption> options = new HashSet<>();
        options.add(FileVisitOption.FOLLOW_LINKS);
        System.out.println("Root " + root); // NOI18N
        System.out.println("Depth " + depth); // NOI18N
        MyFileVisitor myVisitor = new MyFileVisitor(root, checkLinks);
        Files.walkFileTree(root, options, depth, myVisitor);
        for (Statistic store : myVisitor.stores.values()) {
            System.out.println(store.store.type() + "\t" + store.store.name()); // NOI18N
            if (store.startPath != null) {
                System.out.println("\tShortest path\t" + store.startPath); // NOI18N
            }
            if (store.duration > 0) {
                System.out.println("\tVisit " + format(store.visitCount) // NOI18N
                        + " items for " + format(store.duration / 1000 / 1000) // NOI18N
                        + " ms. " + format(store.visitCount * 1000 * 1000 * 1000 / store.duration) + " items/s"); // NOI18N
            }
            if (store.readTime > 0) {
                System.out.println("\tRead " + format(store.readCount / 1024) // NOI18N
                        + " Kb for " + format(store.readTime / 1000 / 1000) // NOI18N
                        + " ms. " + format(store.readCount * 1000 * 1000 * 1000 / store.readTime / 1024) + " Kb/s"); // NOI18N
            }
            if ("nfs".equals(store.store.type())) { // NOI18N
                String host = store.store.name();
                int sep = host.indexOf(':');
                if (sep > 0) {
                    String mountPath = host.substring(sep + 1);
                    host = host.substring(0, sep);
                    if (host.indexOf(',') > 0) {
                        host = host.substring(0, host.indexOf(','));
                    }
                    long socketPing = getSocketPing(host);
                    System.out.println("\tPing " + format(socketPing / 1000) + " mcs."); // NOI18N

                }
            }
            {
                if (store.duration > 0) {
                    long time = store.duration / 1000 / 1000 / 1000;
                    long speed = store.visitCount * 1000 * 1000 * 1000 / store.duration;
                    if (time > TIME_TRASH_HOLD && speed < LS_SPEED_TRASH_HOLD) {
                        System.out.println("\tSlow file system detected. Expected visiting speed more than "+LS_SPEED_TRASH_HOLD+" items/s."); // NOI18N
                    }
                }
                if (store.readTime > 0) {
                    long time = store.readTime / 1000 / 1000 / 1000;
                    long speed = store.readCount * 1000 * 1000 * 1000 / store.readTime / 1024;
                    if (time > TIME_TRASH_HOLD && speed < READ_SPEED_TRASH_HOLD) {
                        System.out.println("\tSlow file system detected. Expected reading speed more than "+READ_SPEED_TRASH_HOLD+" Kb/s."); // NOI18N
                    }
                }
            }
        }
    }

    private static long getSocketPing(String host) {
        try {
            List<Long> times = new ArrayList<>();
            int COUNT = 25;
            InetAddress addr = InetAddress.getByName(host);
            for (int i = 0; i < COUNT; i++) {
                try {
                    long s = System.nanoTime();
                    addr.isReachable(SOCKET_CONNECTION_TIMEOUT);
                    final long delta = System.nanoTime() - s;
                    times.add(delta);
                } catch (ConnectException ex) {
                } catch (SocketTimeoutException ex) {
                } catch (IOException ex) {
                }
            }
            if (times.size() > 0) {
                Collections.sort(times);
                return times.get(0);
            }
        } catch (UnknownHostException ex) {
        } catch (IOException ex) {
        }
        return 0;
    }

    private static String format(long val) {
        String res = Long.toString(val);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < res.length(); i++) {
            char c = res.charAt(res.length() - i - 1);
            if (i % 3 == 0 && i > 0) {
                buf.insert(0, ','); //NOI18N
            }
            buf.insert(0, c);
        }
        return buf.toString();
    }

    private static void usages() {
        System.err.println("Check File System Performance"); // NOI18N
        System.err.println("Usage:"); // NOI18N
        System.err.println("java -cp org-netbeans-modules-cnd-discovery.jar org.netbeans.modules.cnd.discovery.performance.FileSystemChecker -path <path> -depth <depth>"); // NOI18N
        System.err.println("\t-path <path>\tAbsolute path to source root"); // NOI18N
        System.err.println("\t-depth <depth>\tRestrict traverse subfolders"); // NOI18N
        System.err.println("\t-link\tPrint links that go out of the root"); // NOI18N
    }

    private static final class MyFileVisitor implements FileVisitor<Path> {

        private final Path root;
        private final boolean checkLinks;
        private long currStart;
        private final HashMap<FileStore, Statistic> stores = new HashMap<>();

        private MyFileVisitor(Path root, boolean checkLinks) {
            this.root = root;
            this.checkLinks = checkLinks;
            this.currStart = System.nanoTime();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            FileStore fileStore = Files.getFileStore(dir);
            Statistic stat = stores.get(fileStore);
            if (stat == null) {
                stat = new Statistic(fileStore);
                stores.put(fileStore, stat);
            }
            if (stat.startPath == null) {
                stat.startPath = dir;
            } else {
                if (stat.startPath.toString().length() > dir.toString().length()) {
                    stat.startPath = dir;
                }
            }
            final Path fileName = dir.getFileName();
            if (fileName != null) {
                final String name = fileName.toString();
                if (name != null) {
                    if (name.equals("SCCS") || name.equals("CVS") || name.equals(".hg") || name.equals(".svn") || name.equals(".ade_path")) { // NOI18N
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try {
                if (Files.isSymbolicLink(file)) {
                    Path to = Files.readSymbolicLink(file);
                    if (!to.isAbsolute()) {
                        to = file.getParent().resolve(to).normalize();
                    }
                    if (checkLinks) {
                        if (!to.startsWith(root)) {
                            System.out.println("Linked dir " + file + " -> " + to); // NOI18N
                        }
                    }
                    if (Files.isRegularFile(to)) {
                        countRegularFile(to);
                    }
                } else if (Files.isRegularFile(file)) {
                    countRegularFile(file);
                } else if (Files.isDirectory(file)) {
                    FileStore fileStore = Files.getFileStore(file);
                    Statistic stat = stores.get(fileStore);
                    if (stat == null) {
                        stat = new Statistic(fileStore);
                        stores.put(fileStore, stat);
                    }
                    if (stat.startPath == null) {
                        stat.startPath = file;
                    } else {
                        if (stat.startPath.toString().length() > file.toString().length()) {
                            stat.startPath = file;
                        }
                    }
                    stat.visitCount++;
                    long beg = System.nanoTime();
                    stat.duration += beg - currStart;
                    currStart = beg;
                }
            } catch (IOException ex) {
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        private void readFile(Path file, Statistic stat) {
            long beg = System.nanoTime();
            if (Files.isReadable(file)) {
                try (InputStream read = Files.newInputStream(file)) {
                    int count = 0;
                    while (true) {
                        int i = read.read();
                        if (i < 0) {
                            break;
                        }
                        stat.readCount++;
                        if (count > 10 * 1024) {
                            break;
                        }
                        count++;
                    }
                } catch (IOException ex) {
                } finally {
                    stat.readTime += System.nanoTime() - beg;
                }
            }
        }

        private void countRegularFile(Path file) throws IOException {
            FileStore fileStore = Files.getFileStore(file);
            Statistic stat = stores.get(fileStore);
            if (stat == null) {
                stat = new Statistic(fileStore);
                stores.put(fileStore, stat);
            }
            if (stat.startPath == null) {
                stat.startPath = file;
            } else {
                if (stat.startPath.toString().length() > file.toString().length()) {
                    stat.startPath = file;
                }
            }
            stat.visitCount++;
            stat.duration += System.nanoTime() - currStart;
            readFile(file, stat);
            currStart = System.nanoTime();
        }
    }

    private static final class Statistic {

        private final FileStore store;
        private Path startPath = null;
        private long readCount = 0;
        private long readTime = 0;
        private long visitCount = 0;
        private long duration;

        private Statistic(FileStore store) {
            this.store = store;
        }
    }
}
