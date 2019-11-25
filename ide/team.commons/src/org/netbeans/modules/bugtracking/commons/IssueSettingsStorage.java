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
package org.netbeans.modules.bugtracking.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.Places;

/**
 *
 * @author Tomas Stupka
 */
public class IssueSettingsStorage {
    
    private static  final Logger LOG = Logger.getLogger(IssueSettingsStorage.class.getName());
    private static final String PROP_COLLAPSED_COMMENT_PREFIX = "collapsed.comment";   // NOI18N
    
    private static IssueSettingsStorage instance;
    private final File storage;

    private IssueSettingsStorage() { 
        storage = getStorageRootFile();
        if(!storage.exists()) {
            storage.mkdirs();
        }
    }
    
    public synchronized static IssueSettingsStorage getInstance() {
        if(instance == null) {
            instance = new IssueSettingsStorage();
        }
        return instance;
    }
    
    private File getStorageRootFile() {
        return new File(new File(Places.getUserDirectory(), "config"), "issue-tracking");         // NOI18N
    }

    public Collection<Long> loadCollapsedCommenst(String repoUrl, String id) {
        File file = getIssuePropertiesFile(repoUrl, id);
        FileLocks.FileLock l = FileLocks.getLock(file);
        try {
            Properties p = load(file, repoUrl, id);
            Set<Long> s = new HashSet<Long>();
            for(Object k : p.keySet()) {
                String key = k.toString();
                if(key.startsWith(PROP_COLLAPSED_COMMENT_PREFIX) && "true".equals(p.get(key))) {
                    s.add(Long.parseLong(key.substring(PROP_COLLAPSED_COMMENT_PREFIX.length())));
                }
            }
            return s;
        } catch (IOException ex) {
            Support.LOG.log(Level.WARNING, repoUrl + " " + id, ex);
        } finally {
            l.release();
        }
                
        return Collections.emptySet();
    }
    
    private Properties load(File file, String repoUrl, String id) throws IOException {
        Properties p = new Properties();
        if(!file.exists()) {
            file.createNewFile();
        }
        FileInputStream fis = new FileInputStream(file);
        try {
            p.load(fis);
        } finally {
            fis.close();
        }
        return p;
        }
    
    public void storeCollapsedComments(Collection<Long> collapsedComments, String repoUrl, String id) {
        File file = getIssuePropertiesFile(repoUrl, id);
        FileLocks.FileLock l = FileLocks.getLock(file);
        try {
            Properties p = load(file, repoUrl, id);
            clear(p, PROP_COLLAPSED_COMMENT_PREFIX);
            for (Long i : collapsedComments) {
                p.put(PROP_COLLAPSED_COMMENT_PREFIX + i, "true");
            }
            FileOutputStream fos = new FileOutputStream(file);
            try {
                p.store(fos, "");
            } finally {
                fos.close();
            }
        } catch (IOException ex) {
            Support.LOG.log(Level.WARNING, repoUrl + " " + id, ex);
        } finally {
            l.release();
        }
    }

    private void clear(Properties p, String keyPrefix) {
        Iterator<Object> it = p.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next().toString();
            if(key.startsWith(keyPrefix)) {
                it.remove();
            }
        }
    }
    
    private File getIssuePropertiesFile(String repoUrl, String id) {
        return new File(getNameSpaceFolder(storage, repoUrl), id);
    }
    
    private static Map<String, String> loggedUrls;
    static File getNameSpaceFolder(File storage, String url) {
        File folderLegacy = new File(storage, TextUtils.encodeURL(url));
        File folder = new File(storage, TextUtils.getMD5(url));
        if(folderLegacy.exists()) {
            folderLegacy.renameTo(folder);
        } 
        if(!folder.exists()) {
            folder.mkdirs();
        }
        if(LOG.isLoggable(Level.FINE)) {
            if(loggedUrls == null) {
                loggedUrls = new HashMap<String, String>(1);
            }
            String folderPath = loggedUrls.get(url);
            if(folderPath == null) {
                folderPath = folder.getAbsolutePath();
                loggedUrls.put(url, folderPath);
                LOG.log(Level.FINE, "storage folder for URL {0} is {1}", new Object[]{url, folderPath}); // NOI18N
            }
        }
        return folder;
    }
    
    private static class FileLocks {
        private static FileLocks instance;
        private synchronized static FileLocks getInstance() {
            if(instance == null) {
                instance = new FileLocks();
            }
            return instance;
        }
        private final Map<String, FileLock> locks = new HashMap<String, FileLock>();
        static FileLock getLock(File file) {
            synchronized(getInstance().locks) {
                FileLock fl = getInstance().locks.get(file.getAbsolutePath());
                if(fl == null) {
                    fl = getInstance().new FileLock(file);
                }
                getInstance().locks.put(file.getAbsolutePath(), fl);
                return fl;
            }
        }
        class FileLock {
            private final File file;
            public FileLock(File file) {
                this.file = file;
            }
            void release() {
                synchronized(getInstance().locks) {
                    getInstance().locks.remove(file.getAbsolutePath());
                }
            }
        }
    }
}
