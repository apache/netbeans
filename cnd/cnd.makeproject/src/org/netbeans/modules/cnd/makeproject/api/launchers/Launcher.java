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
package org.netbeans.modules.cnd.makeproject.api.launchers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public final class Launcher {
    private final int index;
    //displayed name, can be null
    private String name;
    //command is required field, cannot be null
    //if you want to change command -> delete launcher and add new one
    private final String command;
    private String buildCommand;
    private String runDir;
    private final Map<String, String> env = new HashMap<>();
    private String symbolFiles;
    private boolean hide = false;
    private boolean runInOwnTab = true;
    //can not be set after the creation
    private final Launcher common;

    public Launcher(String command, Launcher common) {
        this(LaunchersRegistry.COMMON_LAUNCHER_INDEX, command, common);
    }
    
    public Launcher(int index, String command, Launcher common) {
        this. index = index;
        this.command = command;
        this.common = common;
    }

    /*package*/ int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    /*package*/ void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public String getBuildCommand() {
        return buildCommand;
    }

    public void setBuildCommand(String buildCommand) {
        this.buildCommand = buildCommand;
    }

    public String getRunDir() {
        if (runDir != null) {
            return runDir;
        } else if (common != null){
            return common.getRunDir();
        } else {
            return null;
        }
    }

    /*package*/ void setRunDir(String runDir) {
        this.runDir = runDir;
    }

    public Map<String, String> getEnv() {
        Map<String, String> ret;
        if (common != null) {
            ret = common.getEnv();
        } else {
            ret = new HashMap<>();
        }
        ret.putAll(env);
        return ret;
    }

    /*package*/ void putEnv(String key, String value) {
        env.put(key, value);
    }

    public String getSymbolFiles() {
        if (symbolFiles != null) {
            return symbolFiles;
        } else if (common != null) {
            return common.getSymbolFiles();
        } else {
            return null;
        }
    }

    /*package*/ void setSymbolFiles(String symbolFiles) {
        this.symbolFiles = symbolFiles;
    }

    public boolean isHide() {
        return hide;
    }

    /*package*/ void setHide(boolean isHide) {
        this.hide = isHide;
    }

    public boolean runInOwnTab() {
        return runInOwnTab;
    }

    /*package*/ void setRunInOwnTab(boolean runInOwnTab) {
        this.runInOwnTab = runInOwnTab;
    }

    public String getDisplayedName() {
        return (name == null ? command : name);
    }

    @Override
    public boolean equals(Object obj) {     // Maybe we should use another field
        if (obj instanceof Launcher) {
            return ((Launcher) obj).getDisplayedName().equals(getDisplayedName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        assert false : "hashCode is not designed";
        return 17;
    }

    boolean isLauncherEquals(Launcher other) {
        if (other == null) {
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
        if (!Objects.equals(this.hide, other.hide)) {
            return false;
        }
        if (!Objects.equals(this.runInOwnTab, other.runInOwnTab)) {
            return false;
        }
        if (!Objects.equals(this.env, other.env)) {
            return false;
        }
        if (!Objects.equals(this.symbolFiles, other.symbolFiles)) {
            return false;
        }
        if (common == null && other.common == null ||
            common != null && other.common != null && common.isLauncherEquals(other.common)) {
            return true;
        }
        return false;
    }
}
