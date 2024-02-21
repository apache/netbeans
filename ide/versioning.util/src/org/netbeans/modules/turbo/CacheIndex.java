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

package org.netbeans.modules.turbo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tomas Stupka
 */
public abstract class CacheIndex {

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.turbo.CacheIndex");
    private Map<File, Set<File>> index = new ConcurrentHashMap<File, Set<File>>();

    public CacheIndex() { }

    /**
     * Returns true if the given file is managed by the particular vcs
     * @param file
     * @return true if the given file is managed by the particular vcs otherwise false
     */
    protected abstract boolean isManaged(File file);

    public File[] get(File key) {
        LOG.log(Level.FINE, "get({0})", new Object[]{key});

        if(key == null) {
            return new File[0];
        }

        synchronized(this) {
            Set<File> ret = index.get(key);
            if(ret == null) {
                LOG.log(Level.FINE, " get({0}) returns no files", new Object[]{key});
                return new File[0];
            }

            LOG.log(Level.FINE, " get({0}) returns {1}", new Object[]{key, ret.size()});
            if(LOG.isLoggable(Level.FINER)) {
                LOG.finer("   " + ret);
            }

            return ret.toArray(new File[0]);
        }
    }

    public File[] getAllValues() {
        LOG.fine("getAllValues()");
        
        List<Set<File>> values;
        synchronized(this) {
            Collection<Set<File>> c = index.values();
            values = new ArrayList<Set<File>>(c.size());
            values.addAll(c);
        }

        Set<File> ret = new HashSet<>();
        for (Set<File> valuesSet : values) {
            synchronized(this) {
                ret.addAll(valuesSet);
            }
        }

        LOG.log(Level.FINE, " getAllValues() returns {0}", new Object[]{ret.size()});
        if(LOG.isLoggable(Level.FINER)) {
            LOG.finer("   " + ret);
        }

        return ret.toArray(new File[0]);
    }

    public void add(File file) {
        LOG.log(Level.FINE, "add({0})", new Object[]{file});

        assert file != null;
        if(file == null) {
            return;
        }

        File parent = file.getParentFile();
        if (parent == null) {
            // this should not happen
            LOG.log(Level.INFO, "add: trying to add a FS root {0})", new Object[]{file}); //NOI18N
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "add: trying to add a FS root", new Exception()); //log also the stacktrace
            }
            return;
        }
        synchronized(this) {
            Set<File> set = index.get(parent);
            if(set == null) {
                LOG.log(Level.FINER, "  add({0}) - creating new file entry", new Object[]{file});
                set = Collections.synchronizedSet(new HashSet<File>());
                set.add(file);
                index.put(parent, set);
            } else {
                set.add(file);
            }

            ensureParents(parent);           
        }
    }

    public void add(File file, Set<File> files) {
        LOG.log(Level.FINE, "add({0}, Set<File>)", new Object[]{file});
        if(LOG.isLoggable(Level.FINER)) {
            LOG.finer("   " + files);
        }
        if(files == null) {
            files = new HashSet<File>(0);
        }
        Set<File> newSet = new HashSet<File>(files.size());

        synchronized(this) {
            Set<File> oldSet = index.get(file);
            if(oldSet != null) {
                for (File f : oldSet) {
                    if(!files.contains(f) && // removed from the set but
                       index.get(f) != null) // remove only if there are no files underneath
                    {
                        newSet.add(f);
                    }
                }
            }
            newSet.addAll(files);

            LOG.log(Level.FINE, "  add({0}, Set<File>) - add entries", new Object[]{file});
            index.put(file, Collections.synchronizedSet(newSet));
            if(newSet.size() > 0) {
                ensureParents(file);
            } else  {
                cleanUpParents(file);
            }

        }
    }

    private void ensureParents(File file) {
        File pFile = file;
        LOG.log(Level.FINE, "  ensureParents({0})", new Object[]{pFile});

        while (true) {
            File parent = file.getParentFile();
            LOG.log(Level.FINE, "  ensureParents({0}) - parent {1}", new Object[]{pFile, parent});
            if (parent == null) {
                LOG.log(Level.FINE, "  ensureParents({0}) - done", new Object[]{pFile, parent});
                break;
            }

            synchronized(this) {
                Set<File> set = index.get(parent);
                if (set == null) {
                    LOG.log(Level.FINE, "  ensureParents({0}) - parent {1} - no entry" , new Object[]{pFile, parent});
                    if (!isManaged(parent)) {
                        LOG.log(Level.FINE, "  ensureParents({0}) - parent {1} - not managed - done!", new Object[]{pFile, parent});
                        break;
                    }
                    set = new HashSet<File>();
                    LOG.log(Level.FINE, "  ensureParents({0}) - parent {1} - creating parent node", new Object[]{pFile, parent});
                    index.put(parent, set);
                }
                LOG.log(Level.FINE, "  ensureParents({0}) - parent {1} - adding file {2}", new Object[]{pFile, parent, file});
                set.add(file);
            }
            file = parent;
        }
    }

    private void cleanUpParents(File file) {
        File pFile = file;
        LOG.log(Level.FINE, "  cleanUpParents({0})", new Object[]{pFile});

        Set<File> set = index.get(file);
        if(set != null && set.size() > 0) {
            LOG.log(Level.FINE, "  cleanUpParents({0}) - children underneath. stop.", new Object[]{pFile});
            return;
        }

        LOG.log(Level.FINE, "  cleanUpParents({0}) - removing node", new Object[]{pFile});
        index.remove(file);
        while(true) {
            File parent = file.getParentFile();
            LOG.log(Level.FINE, "  cleanUpParents({0}) - parent {1}", new Object[]{pFile, parent});

            if (parent == null) {
                LOG.log(Level.FINE, "  cleanUpParents({0}) - done", new Object[]{pFile, parent});
                break;
            }

            synchronized(this) {
                set = index.get(parent);
                if(set == null) {
                    LOG.log(Level.FINE, "  cleanUpParents({0}) - parent {1} empty - stop", new Object[]{pFile, parent});
                    break;
                }

                if(set.size() == 1) {
                    File lastLonelyFile = set.iterator().next();
                    if(file.equals(lastLonelyFile) && index.get(lastLonelyFile) == null) { // remove the last node only when it indeed equals the removing file
                        // file under parent point to nowhere -> remove whole parent node
                        LOG.log(Level.FINE, "  cleanUpParents({0}) - parent {1} size 1 - remove", new Object[]{pFile, parent});
                        index.remove(parent);
                    } else {
                        break;
                    }
                } else {
                    LOG.log(Level.FINE, "  cleanUpParents({0}) - parent {1} - remove file {2}", new Object[]{pFile, parent, file});
                    // more then one file under parent node -> remove only file from parents children if it's pointing to nowhere
                    if(index.get(file) == null) {
                        set.remove(file);
                    }
                    break;
                }
            }
            file = parent;
        }

    }
}
