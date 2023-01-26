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

package org.netbeans.modules.editor.lib2.view;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Various statistics about view creation and maintaining.
 * 
 * @author Miloslav Metelka
 */

public final class ViewStats {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewStats.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewStats.class.getName());
    
    private static final int TEXT_LAYOUT_CREATED_OR_REUSED_THRESHOLD = 200;
    
    private static final int STALE_VIEW_CREATION_TIMEOUT = 10;
    
    private static int textLayoutCreatedCount;
    
    private static int textLayoutCreatedCharCount;
    
    private static int textLayoutReusedCount;
    
    private static int textLayoutReusedCharCount;
    
    private static int staleViewCreationCount;

    private ViewStats() { // No instances
    }
    
    public static void incrementTextLayoutCreated(int charCount) {
        textLayoutCreatedCount++;
        textLayoutCreatedCharCount += charCount;
        if (LOG.isLoggable(Level.FINE)) {
            if (LOG.isLoggable(Level.FINEST) ||
                    (textLayoutCreatedCount % TEXT_LAYOUT_CREATED_OR_REUSED_THRESHOLD) == 0)
            {
                LOG.fine(stats());
            }
        }
    }

    public static void incrementTextLayoutReused(int charCount) {
        textLayoutReusedCount++;
        textLayoutReusedCharCount += charCount;
        if (LOG.isLoggable(Level.FINE)) {
            if (LOG.isLoggable(Level.FINEST) ||
                    (textLayoutReusedCount % TEXT_LAYOUT_CREATED_OR_REUSED_THRESHOLD) == 0)
            {
                LOG.fine(stats());
            }
        }
    }

    public static void incrementStaleViewCreations() {
        staleViewCreationCount++;
        if (LOG.isLoggable(Level.FINE)) {
            if (LOG.isLoggable(Level.FINEST) ||
                (staleViewCreationCount % STALE_VIEW_CREATION_TIMEOUT) == 0)
            {
                LOG.fine(stats());
            }
        }
    }
    
    public static String stats() {
        return "TextLayouts:" + // NOI18N
                "\n  Created:\tcount: " + textLayoutCreatedCount + // NOI18N
                "\tchar-count: " + textLayoutCreatedCharCount + // NOI18N
                "\n  Reused:\tcount: " + textLayoutReusedCount + // NOI18N
                "\tchar-count: " + textLayoutReusedCharCount + // NOI18N
                "\nStaleCreations: " + staleViewCreationCount + // NOI18N
                "\n"; // NOI18N
    }

}
