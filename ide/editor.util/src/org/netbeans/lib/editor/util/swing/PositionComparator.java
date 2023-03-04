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

package org.netbeans.lib.editor.util.swing;

import java.util.Comparator;
import javax.swing.text.Position;

/**
 * Comparator for {@link Position} objects.
 *
 * @author Miloslav Metelka
 * @since 1.6
 */

public final class PositionComparator implements Comparator<Position> {
    
    public static final PositionComparator INSTANCE = new PositionComparator();

    public int compare(Position p1, Position p2) {
        return p1.getOffset() - p2.getOffset();
    }

}
