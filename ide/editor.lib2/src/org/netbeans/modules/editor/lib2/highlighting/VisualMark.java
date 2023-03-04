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

/**
 * Visual mark encapsulates y-coordinate offset together with an object
 * that provides an offset (assumed that it's tracked as a SWing position).
 * <br>
 * The y-coordinate is tracked as a raw value that must first be preprocessed
 * by {@link VisualMarkVector}.
 *
 * @author Miloslav Metelka
 */
public abstract class VisualMark {
    
    private double rawY;
    
    private final VisualMarkVector<?> markVector;
    
    protected VisualMark(VisualMarkVector<?> markVector) {
        this.markVector = markVector;
    }
    
    /**
     * Get offset of this visual mark.
     * <br>
     * It's assumed that the offset is tracked as a Swing position.
     *
     * @return &gt;=0 offset of this mark.
     */
    public abstract int getOffset();
    
    /**
     * Get y-coordinate offset of this mark.
     *
     * @return y of this mark.
     */
    public final double getY() {
        return markVector.raw2Y(rawY);
    }
    
    protected final VisualMarkVector<?> markVector() {
        return markVector;
    }
    
    double rawY() {
        return rawY;
    }
    
    void setRawY(double rawY) {
        this.rawY = rawY;
    }
    
}
