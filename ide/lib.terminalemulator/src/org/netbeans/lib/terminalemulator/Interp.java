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

/*
 * "Interp.java"
 * Interp.java 1.7 01/07/23
 */

package org.netbeans.lib.terminalemulator;

import java.awt.event.KeyEvent;

public interface Interp {
    public String name();
    public void processChar(char c);

    /**
     * Handle a function key.
     * 'e' must be consumed if the Interp recognizes it and sends a sequence.
     * @param e
     */
    public void keyPressed(KeyEvent e);

    /**
     * Convert a terminal-specific character to the canonical curses ACS code.
     * @param inChar
     * @return '\0' if no conversion took place.
     */
    public char mapACS(char inChar);

    public void softReset();
} 
