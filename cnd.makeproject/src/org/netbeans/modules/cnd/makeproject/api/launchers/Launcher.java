/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
