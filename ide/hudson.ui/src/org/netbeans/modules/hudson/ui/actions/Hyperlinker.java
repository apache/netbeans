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

package org.netbeans.modules.hudson.ui.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.spi.HudsonLogger;
import org.netbeans.modules.hudson.spi.HudsonLogger.HudsonLogSession;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.OutputWriter;

/**
 * Manages warning/error/stack trace hyperlinking in the Output Window.
 */
public final class Hyperlinker {

    private static final Logger LOG = Logger.getLogger(Hyperlinker.class.getName());

    private final HudsonLogSession[] sessions;

    public Hyperlinker(HudsonJob job) {
        List<HudsonLogSession> _sessions = new ArrayList<HudsonLogSession>();
        for (HudsonLogger logger : Lookup.getDefault().lookupAll(HudsonLogger.class)) {
            _sessions.add(logger.createSession(job));
        }
        sessions = _sessions.toArray(new HudsonLogSession[0]);
    }

    public void handleLine(String line, OutputWriter stream) {
        for (HudsonLogSession session : sessions) {
            if (session.handle(line, stream)) {
                break;
            }
        }
        // DefaultLogger is last and always handles it
    }

    @ServiceProvider(service = HudsonLogger.class, position = Integer.MAX_VALUE)
    public static final class DefaultLogger implements HudsonLogger {

        @Override
        public HudsonLogSession createSession(HudsonJob job) {
            return new HudsonLogSession() {

                @Override
                public boolean handle(String line, OutputWriter stream) {
                    stream.println(line);
                    return true;
                }
            };
        }
    }
}
