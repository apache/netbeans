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

package org.netbeans.modules.editor.errorstripe.privatespi;

import java.awt.Color;

/**Provides description of a mark that should be displayed in the Error Stripe.
 *
 * @author Jan Lahoda
 */
public interface Mark {

    /**Mark that is error-like. The mark will be shown as
     * a thin horizontal line.
     */
    public static final int TYPE_ERROR_LIKE = 1;

    public static final int TYPE_CARET = 2;

    /**Default priority.
     */
    public static final int PRIORITY_DEFAULT = 1000;

    /**Return of what type is this mark. Currently only one type
     * exists: {@link #TYPE_ERROR_LIKE}. Other types may be
     * introduced later.
     *
     * @return {@link #TYPE_ERROR_LIKE}
     */
    public int getType();
    
    /**Returns status that represents this mark.
     *
     *@return status representing this mark
     */
    public Status getStatus();
    
    /**Returns priority of this mark. The priority prioritizes the marks in the same
     * status. The smaller number, the greater priority.
     *
     * @return priority of this mark
     * @see #PRIORITY_DEFAULT
     */
    public int getPriority();
    
    /**Returns enhanced (non-standard) color of this mark. If null, default color
     * for given status will be used.
     *
     * @return Color or null if default should be used.
     */
    public Color  getEnhancedColor();
    
    /**Returns line span which represents this mark.
     *
     * @return an array of size two, the first item represents starting line of the span,
     *         the second item ending line of the span. Both lines are inclusive.
     */
    public int[]  getAssignedLines();
    
    /**Return some human readable short description to be shown for
     * example in tooltips.
     *
     * @return a short description.
     */
    public String getShortDescription();
    
}
