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
/*
 * Average.java
 *
 * Created on October 16, 2002, 9:35 PM
 */

package org.netbeans.performance.spi;

/**An interface defining properties for standard statistical
 * analysis.
 *
 * @author  Tim Boudreau
 */
public interface Average {

    /** Returns the highest value in the set of values
     * used to create this element.
     * the set of values used to create this element.
     */
    public Float getMax();

    /** Returns the maximum percentage of the mean this
     * value varies by, calculated as<P>
     * <code>((Math.abs (mean - (Math.max (min, max)))/mean) * 100
     * </code>
     */
    public Float getMaxVariance();

    /** Returns the mean, or arithmetic average of
     * the set of values used to create this element.
     */
    public Float getMean();

    /** Returns the median value of the entry set used to create
     * this element
     */
    public Float getMedian();

    /** Returns the lowest value in the set of values
     * used to create this element.
     * the set of values used to create this element.
     */
    public Float getMin();
    
    /** Returns the values used to create this element.      */
    public float[] getSamples();
    
    /** Returns the standard deviation of the statistics used to
     * create this element.
     */
    public Double getStandardDeviation();
    
    /** Returns the standard deviation as a percentage of
     * the mean.
     */
    public Float getVariance();
    
}
