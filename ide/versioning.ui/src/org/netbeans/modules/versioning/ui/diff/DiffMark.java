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

package org.netbeans.modules.versioning.ui.diff;

import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.netbeans.api.diff.Difference;

import java.awt.*;

/**
 * Error stripe mark for differences.
 *
 * @author Maros Sandor
 */
final class DiffMark implements Mark {

    private final int[] span;
    private final Color color;
    private final String desc;

    public DiffMark(Difference difference, Color color) {
        if (difference.getType() == Difference.DELETE) {
            int start = difference.getSecondStart() - 1;
            if (start < 0) start = 0;
            span = new int[] { start, start };
        } else {
            span = new int[] { difference.getSecondStart() - 1, difference.getSecondEnd() - 1 };
        }
        this.color = color;
        desc = DiffSidebar.getShortDescription(difference);
    }

    public int getType() {
        return TYPE_ERROR_LIKE;
    }

    public Status getStatus() {
        return Status.STATUS_OK;
    }

    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    public Color getEnhancedColor() {
        return color;
    }

    public int[] getAssignedLines() {
        return span;
    }

    public String getShortDescription() {
        return desc;
    }
}
