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

package org.netbeans.editor.view.spi;

/**
 * Views that implement this interface
 * signal that they work in a mode when
 * they just estimate their size instead
 * of exact measurements that are likely
 * more expensive for computation.
 * <br>
 * By default the view should be in non-estimated mode
 * i.e. the exact measurements should be used
 * unless the estimated span flag is set.
 *
 * <p>
 * If a particular view does not implement this interface
 * then it is assumed that the view uses exact measurements.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface EstimatedSpanView {
    
    /**
     * Check whether this view is currently
     * in a mode when it just estimates its span.
     *
     * @return true if the view estimates its preferred horizontal
     *   and vertical preferred, maximum and minimum spans.
     *   Returns false if the view measurements are exact.
     */
    public boolean isEstimatedSpan();
    
    /**
     * Set whether this view uses estimated span computation
     * instead of exact measurements.
     *
     * @param estimatedSpan whether view will estimate
     *  the spans or use exact measurements.
     */
    public void setEstimatedSpan(boolean estimatedSpan);
    
}
