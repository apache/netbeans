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
package org.netbeans.modules.editor.lib2.highlighting;

import org.netbeans.spi.editor.highlighting.SplitOffsetHighlightsSequence;

/**
 * Split-offsets highlights sequence that can cover text by highlights without "gaps".
 *
 * @author Miloslav Metelka
 */
public interface CoveringHighlightsSequence extends SplitOffsetHighlightsSequence {
    
    /**
     * For true this highlights sequence will return highlights so that a subsequent highlight
     * starts where the previous one ended and {@link #getAttributes()}
     * method will return null in case the particular area has no highlight.
     * <br>
     * When false this HS works like a regular one and {@link #getAttributes() }
     * is expected to always return non-null.
     * 
     * @return true if this is a covering highlights sequence or false for regular HS.
     */
    boolean isCovering();
    
}
