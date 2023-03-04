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

package org.netbeans.modules.java.api.common.impl;

import java.io.File;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.openide.util.lookup.ServiceProvider;

/**
 * Logger which should suppress or prettify typical Ant output from build-impl.xml.
 */
@ServiceProvider(service=AntLogger.class, position=50)
public class ProjectAntLogger extends AntLogger {

    private static final String JAVAC = "javac";    //NOI18N
    private static final String LOADRESOURCE = "loadresource";  //NOI18N

    @Override
    public boolean interestedInSession(AntSession session) {
        // Even if the initiating project is not an Ant-based project, suppress these messages.
        // However disable our tricks when running at VERBOSE or higher.
        return session.getVerbosity() <= AntEvent.LOG_INFO;
    }

    @Override
    public boolean interestedInScript(File script, AntSession session) {
        if (script.getName().equals("build-impl.xml")) { // NOI18N
            File parent = script.getParentFile();
            if (parent != null && parent.getName().equals("nbproject")) { // NOI18N
                File parent2 = parent.getParentFile();
                if (parent2 != null && parent2.isDirectory()) {
                    return true;
                }
            }
        }
        // Was not a nbproject/build-impl.xml; ignore it.
        return false;
    }

    @Override
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }

    @Override
    public String[] interestedInTasks(AntSession session) {
        return new String[] {JAVAC, LOADRESOURCE};
    }

    @Override
    public int[] interestedInLogLevels(AntSession session) {
        return new int[] {
            AntEvent.LOG_WARN
        };
    }

    @Override
    public void taskFinished(AntEvent event) {
        if (JAVAC.equals(event.getTaskName())) {
            Throwable t = event.getException();
            AntSession session = event.getSession();
            if (t != null && !session.isExceptionConsumed(t)) {
                // Some error was thrown from build-impl.xml#compile. Ignore it; generally
                // it will have been a compilation error which we do not wish to show.
                session.consumeException(t);
            }
        }
    }

    @Override
    public void messageLogged(AntEvent event) {
        final AntSession session = event.getSession();
        final String task = event.getTaskName();
        final String line = event.getMessage();
        assert line != null;
        if (LOADRESOURCE.equals(task) &&
                line.equals("module-info.java doesn't exist") &&    // NOI18N
                event.getLogLevel() == AntEvent.LOG_WARN) {
            //Reading of module-info which does not exist.
            //The loadresource even with quiet true logs a warning, make it verbose.
            event.consume();
            session.deliverMessageLogged(event, line, AntEvent.LOG_VERBOSE);
        }
    }


}
