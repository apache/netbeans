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
package org.netbeans.modules.cnd.makeproject.ui.launchers.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.launchers.LaunchersProjectMetadataFactory;
import org.netbeans.modules.cnd.makeproject.api.launchers.LaunchersRegistry;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 */
public class LaunchersConfig {

    private final Project project;
    private final TreeMap<Integer, LauncherConfig> map = new TreeMap<>();
    private final ArrayList<String> commentsPublic = new ArrayList<>();
    private final ArrayList<String> commentsPrivate = new ArrayList<>();
    //public static final int COMMON_LAUNCHER_INDEX = -1;
    private static final int COMMON_PUBLIC_INDEX = LaunchersRegistry.COMMON_LAUNCHER_INDEX;
    private static final int COMMON_PRIVATE_INDEX = -2;
    public LaunchersConfig(Project project) {
        this.project = project;
    }

    public List<LauncherConfig> getLaunchers() {
        ArrayList<LauncherConfig> res = new ArrayList<>();
        map.entrySet().forEach((e) -> {
            res.add(e.getValue().clone());
        });
        return res;
    }

    public void load() {
        FileObject projectDirectory = project.getProjectDirectory();
        final FileObject nbProjectFolder = projectDirectory.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbProjectFolder == null || !nbProjectFolder.isValid()) {  // LaunchersRegistry shouldn't be updated in case the project has been deleted.
            return;
        }
        final FileObject publicLaunchers = nbProjectFolder.getFileObject(LaunchersProjectMetadataFactory.NAME);
        final FileObject privateNbFolder = projectDirectory.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
        final FileObject privateLaunchers;
        if (privateNbFolder != null && privateNbFolder.isValid()) {
            privateLaunchers = privateNbFolder.getFileObject(LaunchersProjectMetadataFactory.NAME);
        } else {
            privateLaunchers = null;
        }
        map.put(COMMON_PUBLIC_INDEX, new LauncherConfig(COMMON_PUBLIC_INDEX, true));
        map.put(COMMON_PRIVATE_INDEX, new LauncherConfig(COMMON_PRIVATE_INDEX, false));
        if (publicLaunchers != null && publicLaunchers.isValid()) {
            load(publicLaunchers, true);
        }
        if (privateLaunchers != null && privateLaunchers.isValid()) {
            load(privateLaunchers, false);
        }
    }

    private void load(FileObject config, boolean pub) {
        BufferedReader in = null;
        try {
            int id = pub ? COMMON_PUBLIC_INDEX : COMMON_PRIVATE_INDEX;
            LauncherConfig l = map.get(id);
            if (l == null) {
                l = new LauncherConfig(id, pub);
                map.put(id, l);
            }
            boolean initComments = true;
            in = new BufferedReader(new InputStreamReader(config.getInputStream(), "UTF-8")); //NOI18N
            String line;
            while((line = in.readLine()) != null) {
                int i = line.indexOf('#');
                if (i >= 0) {
                    if (i == 0) {
                        if (initComments) {
                            if (pub) {
                                commentsPublic.add(line);
                            } else {
                                commentsPrivate.add(line);
                            }
                        }
                        continue;
                    }
                    line = line.substring(0, i);
                }
                line = line.replace('\t', ' ');
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                boolean eof = false;
                while (line.endsWith("\\") && !eof) { //NOI18N
                    String next = in.readLine();
                    if (next == null) {
                        eof = true;
                        line = line.substring(0, line.length() - 1);
                    } else {
                        next = next.replace('\t', ' ');
                        line = line.substring(0, line.length() - 1) + next;
                    }
                }
                i = line.indexOf('=');
                if (i > 0) {
                    String key = line.substring(0, i).trim();
                    String value = line.substring(i + 1).trim();
                    add(key, value, pub);

                }
                if (eof) {
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private void add(String key, String value, boolean pub) {
        if (key.startsWith(LaunchersRegistry.COMMON_TAG + ".")) { //NOI18N
            int id = pub ? COMMON_PUBLIC_INDEX : COMMON_PRIVATE_INDEX;
            LauncherConfig l = map.get(id);
            if (l == null) {
                l = new LauncherConfig(id, pub);
                map.put(id, l);
            }
            String subkey = key.substring(7);
            if (subkey.equals(LaunchersRegistry.DIRECTORY_TAG)) {
                l.runDir = value;
            } else if (subkey.equals(LaunchersRegistry.SYMFILES_TAG)) {
                l.symbolFiles = value;
            } else if (subkey.startsWith(LaunchersRegistry.ENV_TAG + ".")) { //NOI18N
                String var = subkey.substring(4);
                l.env.put(var, value);
            }
        } else if (key.startsWith(LaunchersRegistry.LAUNCHER_TAG)) {
            int i = key.indexOf('.');
            if (i > 0) {
                try {
                    int id = Integer.parseInt(key.substring(LaunchersRegistry.LAUNCHER_TAG.length(), i));
                    LauncherConfig l = map.get(id);
                    if (l == null) {
                        l = new LauncherConfig(id, pub);
                        map.put(id, l);
                    } else {
                        if(l.pub != pub) {
                            // Launcher from public section is overrided by private section.
                            // UI does not support node with two heads.
                            // Move launcher from public section to private
                            // Public seaction will hung. It will be deleted if other public launchesrs are touched.
                            l.pub = pub;
                        }
                    }
                    String subkey = key.substring(i + 1);
                    if (subkey.equals(LaunchersRegistry.NAME_TAG)) {
                        l.name = value;
                    } else if (subkey.equals(LaunchersRegistry.HIDE_TAG)) {
                        l.hide = "true".equals(value); //NOI18N
                    } else if (subkey.equals(LaunchersRegistry.RUN_IN_OWN_TAB_TAG)) {
                        l.runInOwnTab = !"false".equals(value); //NOI18N
                    } else if (subkey.equals(LaunchersRegistry.COMMAND_TAG)) {
                        l.command = value;
                    } else if (subkey.equals(LaunchersRegistry.BUILD_COMMAND_TAG)) {
                        l.buildCommand = value;
                    }  else if (subkey.equals(LaunchersRegistry.DIRECTORY_TAG)) {
                        l.runDir = value;
                    } else if (subkey.equals(LaunchersRegistry.SYMFILES_TAG)) {
                        l.symbolFiles = value;
                    } else if (subkey.startsWith(LaunchersRegistry.ENV_TAG + ".")) { //NOI18N
                        String var = subkey.substring(LaunchersRegistry.ENV_TAG.length()+1);
                        l.env.put(var, value);
                    }
                } catch (NumberFormatException ex) {
                    // skip
                }
            }
        }
    }

    private boolean isModified(ArrayList<LauncherConfig> launchers, boolean pub) {
        ArrayList<LauncherConfig> origin = new ArrayList<>();
        for(Map.Entry<Integer, LauncherConfig> e : map.entrySet()) {
            if (e.getValue().pub == pub) {
                origin.add(e.getValue());
            }
        }
        ArrayList<LauncherConfig> saved = new ArrayList<>();
        for(LauncherConfig l : launchers) {
            if (l.pub == pub) {
                saved.add(l);
            }
        }
        if (origin.size() != saved.size()) {
            return true;
        }
        for(int i = 0; i < origin.size(); i++) {
            LauncherConfig l1 = origin.get(i);
            LauncherConfig l2 = saved.get(i);
            if (!l1.equals(l2)) {
                return true;
            }
        }
        return false;
    }

    public void save(ArrayList<LauncherConfig> launchers) {
        int max = COMMON_PRIVATE_INDEX - 1;
        boolean monotonius = true;
        for (LauncherConfig l : launchers) {
            if (l.id > max) {
                max = l.id;
                continue;
            } else {
                monotonius = false;
                break;
            }
        }
        if (!monotonius) {
            // need to reorder IDs
            int i = 0;
            for (LauncherConfig l : launchers) {
                if (l.id >= 0) {
                    if (l.pub) {
                        i = (i + 1000) / 1000;
                        i = i * 1000;
                        l.id = i;
                    } else {
                        i = (i + 10) / 10;
                        i = i * 10;
                        l.id = i;
                    }
                }
            }
        }
        //
        boolean hasPrivateConfig = false;
        boolean hasPublicConfig = false;
        for (LauncherConfig l : launchers) {
            if (l.id >= 0) {
                if (l.pub) {
                    hasPublicConfig |= !l.name.isEmpty() || !l.command.isEmpty() || l.buildCommand.isEmpty() || !l.env.isEmpty() || !l.runDir.isEmpty() || !l.symbolFiles.isEmpty();
                } else {
                    hasPrivateConfig |= !l.name.isEmpty() || !l.command.isEmpty() || l.buildCommand.isEmpty() || !l.env.isEmpty() || !l.runDir.isEmpty() || !l.symbolFiles.isEmpty();
                }
            } else {
                if (l.pub) {
                    hasPublicConfig |= !l.env.isEmpty() || !l.runDir.isEmpty() || !l.symbolFiles.isEmpty();
                } else {
                    hasPrivateConfig |= !l.env.isEmpty() || !l.runDir.isEmpty() || !l.symbolFiles.isEmpty();
                }
            }
        }

        FileObject projectDirectory = project.getProjectDirectory();
        final FileObject nbProjectFolder = projectDirectory.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbProjectFolder == null || !nbProjectFolder.isValid()) {  // LaunchersRegistry shouldn't be updated in case the project has been deleted.
            return;
        }
        if (isModified(launchers, false)) {
            FileObject privateNbFolder = projectDirectory.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
            FileObject privateLaunchers = null;
            if (privateNbFolder != null && privateNbFolder.isValid()) {
                privateLaunchers = privateNbFolder.getFileObject(LaunchersProjectMetadataFactory.NAME);
                if (hasPrivateConfig) {
                    // save private configuration
                    if (privateLaunchers == null || !privateLaunchers.isValid()) {
                        try {
                            privateLaunchers = privateNbFolder.createData(LaunchersProjectMetadataFactory.NAME);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    save(launchers, privateLaunchers, false);
                } else {
                    if (privateLaunchers != null && privateLaunchers.isValid()) {
                        // save comments if private config exists
                        save(launchers, privateLaunchers, false);
                    }
                }
            }
        }

        if (isModified(launchers, true)) {
            FileObject publicLaunchers = nbProjectFolder.getFileObject(LaunchersProjectMetadataFactory.NAME);
            if (hasPublicConfig) {
                // save public configuration
                if (publicLaunchers == null || !publicLaunchers.isValid()) {
                    try {
                        publicLaunchers = nbProjectFolder.createData(LaunchersProjectMetadataFactory.NAME);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                save(launchers, publicLaunchers, true);
            } else {
                // save comments if public configuration exists
                if (publicLaunchers != null && publicLaunchers.isValid()) {
                    save(launchers, publicLaunchers, true);
                }
            }
        }
    }

    private void save(ArrayList<LauncherConfig> launchers, FileObject config, boolean pub) {
        if (config == null) {
            return;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(config.getOutputStream(), "UTF-8")) { //NOI18N
                @Override
                public void newLine() throws IOException {
                    write("\n"); //NOI18N
                }
                
            };
            ArrayList<String> c = pub ? commentsPublic : commentsPrivate;
            for (String s : c) {
                bw.write(s);
                bw.newLine();
            }
            boolean addNewLine = !c.isEmpty();
            for (LauncherConfig l : launchers) {
                if (l.pub == pub) {
                    if (l.id < 0) {
                        if (!l.runDir.isEmpty() || !l.symbolFiles.isEmpty() || !l.env.isEmpty()) {
                            if (addNewLine) {
                                bw.newLine();
                            }
                        }
                        if (!l.runDir.isEmpty()) {
                            bw.write(LaunchersRegistry.COMMON_TAG + "."); //NOI18N
                            bw.write(LaunchersRegistry.DIRECTORY_TAG);
                            bw.write("="+l.runDir); //NOI18N
                            bw.newLine();
                            addNewLine = true;
                        }
                        if (!l.symbolFiles.isEmpty()) {
                            bw.write(LaunchersRegistry.COMMON_TAG + "."); //NOI18N
                            bw.write(LaunchersRegistry.SYMFILES_TAG);
                            bw.write("="+l.symbolFiles); //NOI18N
                            bw.newLine();
                            addNewLine = true;
                        }
                        for(Map.Entry<String,String> e : l.env.entrySet()) {
                            bw.write(LaunchersRegistry.COMMON_TAG + "."); //NOI18N
                            bw.write(LaunchersRegistry.ENV_TAG+"."); //NOI18N
                            bw.write(e.getKey()+"="); //NOI18N
                            bw.write(e.getValue());
                            bw.newLine();
                            addNewLine = true;
                        }
                    } else {
                        if (addNewLine) {
                            bw.newLine();
                        }
                        if (!l.name.isEmpty()) {
                            bw.write(LaunchersRegistry.LAUNCHER_TAG + l.id + "."); //NOI18N
                            bw.write(LaunchersRegistry.NAME_TAG);
                            bw.write("="+l.name); //NOI18N
                            bw.newLine();
                            addNewLine = true;
                        }
                        if (l.hide) {
                            bw.write(LaunchersRegistry.LAUNCHER_TAG + l.id + "."); //NOI18N
                            bw.write(LaunchersRegistry.HIDE_TAG);
                            bw.write("=true"); //NOI18N
                            bw.newLine();
                            addNewLine = true;
                        }
                        if (!l.runInOwnTab) {
                            bw.write(LaunchersRegistry.LAUNCHER_TAG + l.id + "."); //NOI18N
                            bw.write(LaunchersRegistry.RUN_IN_OWN_TAB_TAG);
                            bw.write("=false"); //NOI18N
                            bw.newLine();
                            addNewLine = true;
                        }
                        if (!l.command.isEmpty()) {
                            StringBuilder buf = new StringBuilder();
                            buf.append(LaunchersRegistry.LAUNCHER_TAG + l.id + "."); //NOI18N
                            buf.append(LaunchersRegistry.COMMAND_TAG);
                            buf.append("="+l.command); //NOI18N
                            writeWrapLine(buf.toString(), bw);
                            addNewLine = true;
                        }
                        if (!l.buildCommand.isEmpty()) {
                            bw.write(LaunchersRegistry.LAUNCHER_TAG + l.id + "."); //NOI18N
                            bw.write(LaunchersRegistry.BUILD_COMMAND_TAG);
                            bw.write("="+l.buildCommand); //NOI18N
                            bw.newLine();
                            addNewLine = true;
                        }
                        if (!l.runDir.isEmpty()) {
                            bw.write(LaunchersRegistry.LAUNCHER_TAG + l.id + "."); //NOI18N
                            bw.write(LaunchersRegistry.DIRECTORY_TAG);
                            bw.write("="+l.runDir); //NOI18N
                            bw.newLine();
                            addNewLine = true;
                        }
                        if (!l.symbolFiles.isEmpty()) {
                            bw.write(LaunchersRegistry.LAUNCHER_TAG + l.id + "."); //NOI18N
                            bw.write(LaunchersRegistry.SYMFILES_TAG);
                            bw.write("="+l.symbolFiles); //NOI18N
                            bw.newLine();
                            addNewLine = true;
                        }
                        for(Map.Entry<String,String> e : l.env.entrySet()) {
                            bw.write(LaunchersRegistry.LAUNCHER_TAG + l.id + "."); //NOI18N
                            bw.write(LaunchersRegistry.ENV_TAG+"."); //NOI18N
                            bw.write(e.getKey()+"="); //NOI18N
                            bw.write(e.getValue());
                            bw.newLine();
                            addNewLine = true;
                        }
                    }
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

    private void writeWrapLine(String s, BufferedWriter bw) throws IOException {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            buf.append(c);
            if (c == ' ' && buf.length() > 80 && i+1 < s.length()) {
                buf.append("\\"); //NOI18N
                bw.write(buf.toString());
                bw.newLine();
                buf.setLength(0);
            }
        }
        bw.write(buf.toString());
        bw.newLine();
    }

    public static final class LauncherConfig {

        private int id;
        private boolean pub;
        private String name = "";
        private String command = "";
        private String buildCommand = "";
        private String runDir = "";
        private String symbolFiles = "";
        private Map<String, String> env = new TreeMap<>();
        private boolean hide = false;
        private boolean runInOwnTab = true;
        private boolean isModified = false;

        public LauncherConfig(int id, boolean pub) {
            this.id = id;
            this.pub = pub;
        }

        public boolean isModified() {
            return isModified;
        }

        public int getID() {
            return id;
        }

        /*package*/ void setID(int id) {
            isModified |= id != this.id;
            this.id = id;
        }

        public boolean getPublic() {
            return pub;
        }

        /*package*/ void setPublic(boolean pub) {
            isModified |= pub != this.pub;
            this.pub = pub;
        }

        public String getName() {
            return name;
        }

        /*package*/ void setName(String name) {
            isModified |= !name.equals(this.name);
            this.name = name;
        }

        public String getCommand() {
            return command;
        }

        /*package*/ void setCommand(String command) {
            isModified |= !command.equals(this.command);
            this.command = command;
        }

        public String getBuildCommand() {
            return buildCommand;
        }

        public void setBuildCommand(String buildCommand) {
            isModified |= !buildCommand.equals(this.buildCommand);
            this.buildCommand = buildCommand;
        }

        public String getRunDir() {
            return runDir;
        }

        /*package*/ void setRunDir(String runDir) {
            isModified |= !runDir.equals(this.runDir);
            this.runDir = runDir;
        }

        public Map<String, String> getEnv() {
            return env;
        }

        /*package*/ void putEnv(String key, String value) {
            env.put(key, value);
        }

        public String getSymbolFiles() {
            return symbolFiles;
        }

        /*package*/ void setSymbolFiles(String symbolFiles) {
            this.symbolFiles = symbolFiles;
        }

        public boolean isHide() {
            return hide;
        }

        public void setHide(boolean isHide) {
            isModified |= isHide != this.hide;
            this.hide = isHide;
        }

        public boolean runInOwnTab() {
            return runInOwnTab;
        }

        public void setrunInOwnTab(boolean runInOwnTab) {
            isModified |= runInOwnTab != this.runInOwnTab;
            this.runInOwnTab = runInOwnTab;
        }

        public String getDisplayedName() {
            return (name == null || name.isEmpty() ? command : name);
        }

        public LauncherConfig copy(int newID) {
            LauncherConfig res = new LauncherConfig(newID, pub);
            res.command = this.command;
            res.name = this.name;
            res.hide = this.hide;
            res.runInOwnTab = this.runInOwnTab;
            res.buildCommand = this.buildCommand;
            res.runDir = this.runDir;
            res.symbolFiles = this.symbolFiles;
            res.env = new TreeMap<>(env);
            return res;
        }
        
        @Override
        public LauncherConfig clone() {
            LauncherConfig res = new LauncherConfig(id, pub);
            res.command = this.command;
            res.name = this.name;
            res.hide = this.hide;
            res.runInOwnTab = this.runInOwnTab;
            res.buildCommand = this.buildCommand;
            res.runDir = this.runDir;
            res.symbolFiles = this.symbolFiles;
            res.env = new TreeMap<>(env);
            return res;
        }

        @Override
        public int hashCode() {
            return super.hashCode(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LauncherConfig other = (LauncherConfig) obj;
            if (this.id != other.id) {
                return false;
            }
            if (this.pub != other.pub) {
                return false;
            }
            if (this.hide != other.hide) {
                return false;
            }
            if (this.runInOwnTab != other.runInOwnTab) {
                return false;
            }
            if (this.isModified != other.isModified) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.command, other.command)) {
                return false;
            }
            if (!Objects.equals(this.buildCommand, other.buildCommand)) {
                return false;
            }
            if (!Objects.equals(this.runDir, other.runDir)) {
                return false;
            }
            if (!Objects.equals(this.symbolFiles, other.symbolFiles)) {
                return false;
            }
            if (!Objects.equals(this.env, other.env)) {
                return false;
            }
            return true;
        }

        
        @Override
        public String toString() {
            return getDisplayedName();
        }

    }
}
