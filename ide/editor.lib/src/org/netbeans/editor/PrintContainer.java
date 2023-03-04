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

package org.netbeans.editor;

import java.awt.Color;
import java.awt.Font;

/**
* Container for printed text. The parts of text attributed by font,
* fore and back color are added to it for the whole printed area.
*
* @author Miloslav Metelka
* @version 1.00
*/

public interface PrintContainer {

    /** Add the attributed characters to the container.
     * @param chars characters being added.
     * @param font font of the added characters
     * @param foreColor foreground color of the added characters
     * @param backColor background color of the added characters
     */
    public void add(char[] chars, Font font, Color foreColor, Color backColor);

    /** End of line was found. */
    public void eol();

    /**
     * @return true if the container needs to init empty line with
     * at least one character. Printing then adds one space
     * to each empty line.
     * False means that the container is able to accept
     * lines with no characters.
     */
    public boolean initEmptyLines();

}
