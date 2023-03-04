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

package org.netbeans.modules.mercurial.ui.annotate;

import java.awt.Color;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;

/**
 *
 * @author Maros Sandor
 */
final class AnnotationMark implements Mark {

    private static final Color COLOR = new Color(0x58,0x90,0xBE);

    private final int line;
    private final String message;

    public AnnotationMark(int line, String message) {
        this.line = line;
        this.message = message;
    }

    public String getShortDescription() {
        return message;
    }
    
    public int[] getAssignedLines() {
        return new int[] {line, line};
    }
    
    public Color getEnhancedColor() {
        return COLOR;
    }
    
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    public Status getStatus() {
        return Status.STATUS_OK;
    }
    
    public int getType() {
        return TYPE_ERROR_LIKE;
    }
}
