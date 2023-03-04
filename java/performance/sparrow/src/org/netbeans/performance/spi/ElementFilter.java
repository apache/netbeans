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
 * ElementFilter.java
 *
 * Created on October 8, 2002, 4:47 PM
 */

package org.netbeans.performance.spi;

/** Defines a filter object that can be used to retrieve only
 *  elements from a DataAggregation that it approves.  Useful for
 *  finding only elements of a certain kind.
 *
 * @author  Tim Boudreau
 */
public interface ElementFilter {
    public boolean accept (LogElement le);
}
