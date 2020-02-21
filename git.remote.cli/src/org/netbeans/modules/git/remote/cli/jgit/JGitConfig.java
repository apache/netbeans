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
package org.netbeans.modules.git.remote.cli.jgit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class JGitConfig {
    public static final String CONFIG_CORE_SECTION = "core";
    public static final String CONFIG_REMOTE_SECTION = "remote";    
    public static final String CONFIG_KEY_FILEMODE = "filemode";
    public static final String CONFIG_KEY_AUTOCRLF = "autocrlf";
    public static final String CONFIG_BRANCH_SECTION = "branch";
    public static final String CONFIG_KEY_AUTOSETUPMERGE = "autosetupmerge";
    public static final String CONFIG_KEY_REMOTE = "remote";
    public static final String CONFIG_KEY_MERGE = "merge";
    public static final String CONFIG_KEY_BARE = "bare";
    public static final String CONFIG_KEY_FF = "ff";
    public static final String CONFIG_KEY_URL = "url"; //NOI18N
    public static final String CONFIG_KEY_PUSHURL = "pushurl"; //NOI18N
    public static final String CONFIG_KEY_FETCH = "fetch"; //NOI18N
    public static final String CONFIG_KEY_PUSH = "push"; //NOI18N
    private static final String CONFIG_LOCATION = ".git/config";
    
    private final TreeMap<SectionKey, TreeMap<String,String>> map = new TreeMap<>();
    private final VCSFileProxy location;
    
    public JGitConfig(VCSFileProxy location) {
        this.location = location;
    }
    
    public void load() {
        //[core]
        //	repositoryformatversion = 0
        //	filemode = true
        //	bare = false
        //	logallrefupdates = true
        //[remote "origin"]
        //	fetch = +refs/heads/*:refs/remotes/origin/*
        //	url = https://github.com/git/git
        //[branch "master"]
        //	remote = origin
        //	merge = refs/heads/master
        VCSFileProxy config = VCSFileProxy.createFileProxy(location, CONFIG_LOCATION);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(config.getInputStream(false), "UTF-8")); //NOI18N
            SectionKey section = null;
            String line;
            map.clear();
            while ((line = reader.readLine()) != null) {
                int i = line.indexOf('#');
                if (i >= 0) {
                    if (i == 0) {
                        continue;
                    }
                    line = line.substring(0, i);
                }
                line = line.replace('\t', ' ');
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                i = line.indexOf('[');
                int j = line.indexOf(']');
                if (i >= 0 && j > i) {
                    // start section
                    String s = line.substring(i+1,j);
                    i = s.indexOf('"');
                    if (i > 0) {
                        String key = s.substring(0,i).trim();
                        String sub = s.substring(i+1, s.length()-1);
                        section = new SectionKey(key, sub);
                    } else {
                        section = new SectionKey(s, null);
                    }
                    map.put(section, new TreeMap<String, String>());
                    continue;
                }
                i = line.indexOf('=');
                if (section != null && i > 0) {
                    String key = line.substring(0, i).trim();
                    String value = line.substring(i+1).trim();
                    map.get(section).put(key, value);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public void save() {
        VCSFileProxy config = VCSFileProxy.createFileProxy(location, CONFIG_LOCATION);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(VCSFileProxySupport.getOutputStream(config), "UTF-8")); //NOI18N
            for(Map.Entry<SectionKey, TreeMap<String,String>> entry : map.entrySet()) {
                SectionKey key = entry.getKey();
                bw.write('[');
                bw.write(key.section);
                if (key.subSection != null) {
                    bw.write(" \"");
                    bw.write(key.subSection);
                    bw.write('"');
                }
                bw.write(']');
                bw.newLine();
                for(Map.Entry<String,String> e : entry.getValue().entrySet()) {
                    bw.write('\t');
                    bw.write(e.getKey());
                    bw.write(" = ");
                    bw.write(e.getValue());
                    bw.newLine();
                }
            }
            
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public void setString(String section, String subsection, String key, String value) {
        SectionKey storageKey = new SectionKey(section, subsection);
        TreeMap<String, String> storage = map.get(storageKey);
        if (storage == null) {
            storage = new TreeMap<>();
            map.put(storageKey, storage);
        }
        storage.put(key, value);
    }

    public String getString(String section, String subsection, String key) {
        SectionKey storageKey = new SectionKey(section, subsection);
        TreeMap<String, String> storage = map.get(storageKey);
        if (storage == null) {
            return null;
        }
        return storage.get(key);
    }

    public void setBoolean(String section, String subsection, String key, boolean b) {
        SectionKey storageKey = new SectionKey(section, subsection);
        TreeMap<String, String> storage = map.get(storageKey);
        if (storage == null) {
            storage = new TreeMap<>();
            map.put(storageKey, storage);
        }
        storage.put(key, Boolean.toString(b));
    }

    public boolean getBoolean(String section, String subsection, String key, boolean b) {
        SectionKey storageKey = new SectionKey(section, subsection);
        TreeMap<String, String> storage = map.get(storageKey);
        if (storage == null) {
            return false;
        }
        String val = storage.get(key);
        return Boolean.toString(true).equals(val);
    }

    public void unset(String section, String subsection, String key) {
        SectionKey storageKey = new SectionKey(section, subsection);
        TreeMap<String, String> storage = map.get(storageKey);
        if (storage != null) {
            storage.remove(key);
            if (storage.isEmpty()) {
                map.remove(storageKey);
            }
        }
    }

    public void unsetSection(String section, String subsection) {
        SectionKey storageKey = new SectionKey(section, subsection);
        map.remove(storageKey);
    }


    public Collection<String> getSubsections(String section) {
        List<String> res = new ArrayList<>();
        for(Map.Entry<SectionKey, TreeMap<String,String>> entry : map.entrySet()) {
            SectionKey key = entry.getKey();
            if (section.equals(key.section)) {
                if (key.subSection != null) {
                    res.add(key.subSection);
                }
            }
        }
        return res;
    }


    public Collection<String> getSections() {
        List<String> res = new ArrayList<>();
        for(Map.Entry<SectionKey, TreeMap<String,String>> entry : map.entrySet()) {
            SectionKey key = entry.getKey();
            if (!res.contains(key.section)) {
                res.add(key.section);
            }
        }
        return res;
    }

    private static final class SectionKey implements Comparable<SectionKey>{
        private final String section;
        private final String subSection;
        
        private SectionKey(String section, String subSection) {
            this.section = section;
            this.subSection = subSection;
        }

        @Override
        public int compareTo(SectionKey o) {
            int res = section.compareTo(o.section);
            if (res == 0) {
                if (subSection == null && o.subSection == null) {
                    return 0;
                } else if (subSection == null) {
                    return -1;
                } else if (o.subSection == null) {
                    return 1;
                } else {
                    return subSection.compareTo(o.subSection);
                }
            }
            return res;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SectionKey other = (SectionKey) obj;
            if (!Objects.equals(this.section, other.section)) {
                return false;
            }
            if (!Objects.equals(this.subSection, other.subSection)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.section);
            hash = 83 * hash + Objects.hashCode(this.subSection);
            return hash;
        }

        @Override
        public String toString() {
            if (subSection != null) {
                return "["+section+" \""+subSection+"\"]";
            }
            return "["+section+"]";
        }
    }
}
