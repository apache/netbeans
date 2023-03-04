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

package org.netbeans.modules.php.project.ui.actions.support;

import java.util.logging.Logger;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.util.Lookup;

/**
 * Common action for all the possible Run Configurations of a PHP project.
 * <p>
 * Meant to be stateless, so thread safe.
 * @author Tomas Mysik
 */
public abstract class ConfigAction {
    public static enum Type {
        LOCAL,
        REMOTE,
        SCRIPT,
        INTERNAL,
        TEST,
        SELENIUM,
    }

    protected static final Logger LOGGER = Logger.getLogger(ConfigAction.class.getName());
    protected final PhpProject project;

    protected ConfigAction(PhpProject project) {
        assert project != null;
        this.project = project;
    }

    public static Type convert(PhpProjectProperties.RunAsType runAsType) {
        Type type = null;
        switch (runAsType) {
            case LOCAL:
                type = Type.LOCAL;
                break;
            case REMOTE:
                type = Type.REMOTE;
                break;
            case SCRIPT:
                type = Type.SCRIPT;
                break;
            case INTERNAL:
                type = Type.INTERNAL;
                break;
            default:
                throw new IllegalArgumentException("Unknown type: " + runAsType);
        }
        return type;
    }

    public static ConfigAction get(Type type, PhpProject project) {
        assert type != null;
        ConfigAction action = null;
        switch (type) {
            case LOCAL:
                action = new ConfigActionLocal(project);
                break;
            case REMOTE:
                action = new ConfigActionRemote(project);
                break;
            case SCRIPT:
                action = new ConfigActionScript(project);
                break;
            case INTERNAL:
                action = new ConfigActionInternal(project);
                break;
            case TEST:
                action = new ConfigActionTest(project);
                break;
            case SELENIUM:
                action = new ConfigActionSelenium(project);
                break;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
        assert action != null;
        return action;
    }

    public boolean isRunProjectEnabled() {
        return true;
    }

    public boolean isDebugProjectEnabled() {
        return DebugStarterFactory.getInstance() != null;
    }

    public abstract boolean isProjectValid();
    public abstract boolean isFileValid();

    public abstract boolean isRunFileEnabled(Lookup context);
    public abstract boolean isDebugFileEnabled(Lookup context);

    public boolean isRunMethodEnabled(Lookup context) {
        // disabled by default
        return false;
    }
    public boolean isDebugMethodEnabled(Lookup context) {
        // disabled by default
        return false;
    }

    public abstract void runProject();
    public abstract void debugProject();

    public abstract void runFile(Lookup context);
    public abstract void debugFile(Lookup context);

    public void runMethod(Lookup context) {
        throw new UnsupportedOperationException();
    }
    public void debugMethod(Lookup context) {
        throw new UnsupportedOperationException();
    }

    protected void showCustomizer() {
        PhpProjectUtils.openCustomizerRun(project);
    }

}
