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
 * "BExtent.java"
 * BExtent.java 1.5 01/07/26
 */

package org.netbeans.lib.terminalemulator;

class BExtent {
    public BCoord begin;
    public BCoord end;

    public BExtent(BCoord begin, BCoord end) {
	this.begin = (BCoord) begin.clone();
	this.end = (BCoord) end.clone();
    } 

    public Extent toExtent(int bias) {
	return new Extent(new Coord(begin, bias), new Coord(end, bias));
    }

    /**
     * Override Object.toString
     */
    public String toString() {
	return "BExtent[" + begin + " " + end + "]";	// NOI18N
    } 
}
