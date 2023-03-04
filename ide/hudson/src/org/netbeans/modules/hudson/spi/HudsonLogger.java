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

package org.netbeans.modules.hudson.spi;

import org.netbeans.modules.hudson.api.HudsonJob;
import org.openide.windows.OutputWriter;

/**
 * Permits extension of hyperlinking behavior for console output.
 */
public interface HudsonLogger {
    
    /**
     * Starts a new session, e.g. one build's output.
     * @param job a job producing output
     * @return a session
     */
    HudsonLogSession createSession(HudsonJob job);

    /**
     * Session for a single build.
     */
    interface HudsonLogSession {

        /**
         * Permits logger to "claim" this line of output.
         * @param line a line of text (no final newline)
         * @param stream a stream to print to (can add a hyperlink)
         * @return true if this logger handled the output, false otherwise
         */
        boolean handle(String line, OutputWriter stream);
    }
}
