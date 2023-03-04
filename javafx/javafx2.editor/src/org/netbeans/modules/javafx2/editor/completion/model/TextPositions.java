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
package org.netbeans.modules.javafx2.editor.completion.model;

/**
 * Accessor to textual positions of a {@link FxNode}
 * @author sdedic
 */
public interface TextPositions {
    public static final int NOPOS = -1;

    /**
     * Start of the FxNode in the source text
     * @return 
     */
    public int  getStart();
    
    /**
     * Position just after the FxNode appearance.
     * @return 
     */
    public int  getEnd();
    
    /**
     * If the FxNode has some content, e.g. Element interior or attribute value,
     * returns the content start offset. Otherwise -1.
     * @return 
     */
    public int  getContentStart();
    
    /**
     * If the FxNode has some content, returns position just after the content.
     * Otherwise -1.
     * @return 
     */
    public int  getContentEnd();
    
    public enum Position {
        Start, End, ContentStart, ContentEnd
    };
    
    public boolean isDefined(Position pos);
    
    public boolean contains(int pos, boolean caret);
    
    public boolean contentContains(int pos, boolean caret);
}
