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
 * LogFile.java
 *
 * Created on October 8, 2002, 12:40 PM
 */

package org.netbeans.performance.spi;
import org.netbeans.performance.spi.html.HTML;
/** Defines an interface representing an entry in a log file of
 * some kind, and a way to get context information about
 * the log file in question and context for the element.  This is
 * the base interface that all items parsed from logs should
 * expose.
 * @author Tim Boudreau
 */
public interface LogElement {
    /** Returns the path to the object, including the name of the object itself.
     * The structure is similar to a Filesystem path.  The portion of the path
     * preceding the name of the object is obtained from its <i>parent</i>.  <P>The
     * path is used as a means of segregating similar runs.  For example a path
     * for a log element representing a lineswitch used for one run of the software
     * might be:<br><code>/02282002/Windows XP/128Mb/performanceTests/javaconfig/NewSize
     * </code><br>
     * @return The path of the object, including its name - the fully qualified name of the
     * object.
     */    
    public String getPath ();
    /** The parent of the object.  A DataAggregation is an extension to LogElement that
     * makes it a container for other LogElements, including other DataAggregation.
     * This is the means by which multiple sets of identical data are segregated.
     * @return The parent object or null if no parent.
     */    
    public DataAggregation getParent();
    /** Convenience search method for locating an ancestor (a DataAggregation that is
     * named in the path of the element) of the element.
     * @param name The unqualified name of the object in question.
     * @return The element requested or null.
     */    
    public DataAggregation findAncestor (String name);
     /** Create an HTML representation of the object.  HTML representations of objects
      * should be self contained (no dangling tags) and be easily added to an
      * HTMLDocument object.
      * @return An object implementing the HTML interface which produces an HTML representation
      * of the object.
      * @see org.netbeans.spi.html.HTMLDocument
      * @see org.netbeans.spi.html.HTML
      */     
    public HTML toHTML ();
}
 
