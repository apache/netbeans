/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
