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

package org.netbeans.lib.uihandler;

import java.util.logging.LogRecord;
import java.util.logging.XMLFormatter;

/** Represents a gesture that initiated the given LogRecord.
 *
 * @author Jaroslav Tulach
 */
public enum InputGesture {
    KEYBOARD, MENU, TOOLBAR;


    private static final XMLFormatter F = new XMLFormatter();

    /** Finds the right InputGesture for given LogRecord.
     * @param rec the record
     * @return the gesture that initiated the record or <code>null</code> if unknown
     */
    public static InputGesture valueOf(LogRecord rec) {
        if ("UI_ACTION_BUTTON_PRESS".equals(rec.getMessage())) {
            String fullMsg = F.format(rec);
            if (fullMsg.indexOf("Actions$Menu") >= 0) {
                return MENU;
            }
            if (fullMsg.indexOf("Actions$Toolbar") >= 0) {
                return TOOLBAR;
            }
        }
        if ("UI_ACTION_KEY_PRESS".equals(rec.getMessage())) {
            return KEYBOARD;
        }
        return null;
    }
}
