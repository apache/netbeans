/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
