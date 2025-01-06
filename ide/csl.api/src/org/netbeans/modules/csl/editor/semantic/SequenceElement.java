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

package org.netbeans.modules.csl.editor.semantic;

import java.util.Comparator;
import javax.swing.text.Position;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.api.ColoringAttributes.Coloring;

/**
 * Each SequeneceElement represents a OffsetRange/Coloring/Language tuple that
 * is managed for semantic highlighting purposes. They are comparable since they
 * are maintained in a TreeSet (sorted by the OffsetRanges).
 *
 * @author Tor Norbye
 */
record SequenceElement(Language language, Position start, Position end, Coloring coloring) {

    public static final Comparator<? super SequenceElement> POSITION_ORDER =
            (e1, e2) -> e1.start.getOffset() != e2.start.getOffset() ? e1.start.getOffset() - e2.start.getOffset()
                : e1.end.getOffset() - e2.end.getOffset();
}
