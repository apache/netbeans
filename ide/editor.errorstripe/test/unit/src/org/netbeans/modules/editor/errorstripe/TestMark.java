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

package org.netbeans.modules.editor.errorstripe;

import java.awt.Color;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;

/**
 *
 * @author Jan Lahoda
 */
public class TestMark implements Mark {

    private Status status;
    private String description;
    private Color  color;
    private int[]  lines;
    private int    priority;

    public TestMark(Status status, String description, Color color, int[] lines) {
        this(status, description, color, lines, PRIORITY_DEFAULT);
    }
    
    public TestMark(Status status, String description, Color color, int[] lines, int priority) {
        this.status = status;
        this.description = description;
        this.color = color;
        this.lines = lines;
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public String getShortDescription() {
        return description;
    }

    public Color getEnhancedColor() {
        return color;
    }

    public int[] getAssignedLines() {
        return lines;
    }

    public int getType() {
        return TYPE_ERROR_LIKE;
    }
    
    public int getPriority() {
        return priority;
    }
    
}
