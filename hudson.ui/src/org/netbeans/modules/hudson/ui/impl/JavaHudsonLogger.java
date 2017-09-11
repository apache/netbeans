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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui.impl;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.spi.HudsonLogger;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Handles Java stack traces for Hudson.
 * Currently just hyperlinks any classes found in open projects.
 */
@ServiceProvider(service=HudsonLogger.class, position=100)
public class JavaHudsonLogger implements HudsonLogger {

    private static final Logger LOG = Logger.getLogger(JavaHudsonLogger.class.getName());

    public HudsonLogSession createSession(HudsonJob job) {
        return new Session();
    }

    // derived from org.netbeans.modules.java.project.JavaAntLogger.STACK_TRACE
    // (would be nicer to have a utility API to parse stack traces, but unclear where this would go)
    private static final Pattern STACK_TRACE = Pattern.compile(
    "(?:\t|\\[catch\\] )at ((?:[a-zA-Z_$][a-zA-Z0-9_$]*\\.)*)[a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z_$<][a-zA-Z0-9_$>]*\\(([a-zA-Z_$][a-zA-Z0-9_$]*\\.java):([0-9]+)\\)"); // NOI18N

    private static class Session implements HudsonLogSession {

        public boolean handle(String line, OutputWriter stream) {
            Matcher m = STACK_TRACE.matcher(line);
            if (!m.matches()) {
                return false;
            }
            String pkg = m.group(1);
            String filename = m.group(2);
            String resource = pkg.replace('.', '/') + filename;
            int lineNumber = Integer.parseInt(m.group(3));
            try {
                stream.println(line, new Hyperlink(resource, lineNumber));
                return true;
            } catch (IOException x) {
                LOG.log(Level.INFO, null, x);
            }
            stream.println(line);
            return true;
        }

    }

    private static class Hyperlink implements OutputListener {

        private static final RequestProcessor RP =
                new RequestProcessor(Hyperlink.class);
        private final String resource;
        private final int lineNumber;

        Hyperlink(String resource, int lineNumber) {
            this.resource = resource;
            this.lineNumber = lineNumber;
        }

        public void outputLineAction(OutputEvent ev) {
            acted(true);
        }

        public void outputLineSelected(OutputEvent ev) {
            acted(false);
        }

        public void outputLineCleared(OutputEvent ev) {}

        private void acted(final boolean force) {
            RP.post(new Runnable() {
                public @Override void run() {
                    FileObject source = GlobalPathRegistry.getDefault().findResource(resource);
                    if (source != null) {
                        // XXX possible to also display exception message in status line
                        // (would need to have grepped output for EXCEPTION_MESSAGE)
                        HudsonLoggerHelper.openAt(source, lineNumber - 1, -1, force);
                    } else if (force) {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            });
        }

    }

}
