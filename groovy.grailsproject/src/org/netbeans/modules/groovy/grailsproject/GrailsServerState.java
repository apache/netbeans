/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject;

import java.net.URL;
import org.netbeans.api.project.Project;

/**
 *
 * @author schmidtm, Petr Hejl
 */
public class GrailsServerState {

    private final Project project;

    /** <i>GuardedBy("this")</i> */
    private Process process;

    /** <i>GuardedBy("this")</i> */
    private boolean debug;

    /** <i>GuardedBy("this")</i> */
    private URL url;

    public GrailsServerState(Project prj) {
        this.project = prj;
    }

    public synchronized boolean isRunning() {
        if (process == null) {
            return false;
        }
        try {
            int exitVal = process.exitValue();
            return false;
        } catch (IllegalThreadStateException ex) {
            return true;
        }
    }

    public synchronized Process getProcess() {
        return process;
    }

    public synchronized void setProcess(Process process) {
        this.process = process;
    }

    public synchronized URL getRunningUrl() {
        if (isRunning()) {
            return url;
        }
        return null;
    }

    public synchronized void setRunningUrl(URL url) {
        this.url = url;
    }

    public synchronized boolean isDebug() {
        return debug;
    }

    public synchronized void setDebug(boolean debug) {
        this.debug = debug;
    }

}
