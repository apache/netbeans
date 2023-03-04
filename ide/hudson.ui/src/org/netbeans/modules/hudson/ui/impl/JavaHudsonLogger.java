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
