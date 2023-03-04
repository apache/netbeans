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
 * DataAggregation.java
 *
 * Created on October 8, 2002, 1:02 PM
 */

package org.netbeans.performance.spi;
import java.util.Iterator;
import java.util.List;
/** An interface for things (such as log files) which wrap collections
 * of objects parsed from a log.  DataAggregations are the second primary class in
 * the interface to parsed data.<P> Note, the interface for this class is
 * definitely pre-alpha - there are several more query methods than are being
 * used, and the query interface should be revised to use regular expressions.
 * <P>DataAggregations are a sort of primitive database.  They are used both
 * for sorting data by run/run type/etc.  The subclass AbstractLogFile provides
 * a wrapper for log files which can parse a log and populate themselves with
 * LogElements representing the contents of a log.
 * @author Tim Boudreau
 */
public interface DataAggregation extends LogElement {

    /** Returns an iterator capable of iterating the <i>entire</i> subtree of this
     * DataAggregation (for child elements that are instances of DataAggregation,
     * this iterator will defer to the child DataAggregation's iterator until it
     * is complete).  The returned iterator does not only iterate this
     * DataAggregation's children, but all of it's children's children recursively
     * as well.
     * @return The iterator
     */    
    public Iterator iterator ();
    /** Find a child LogElement with the passed unqualified name which belongs directly to this
     * DataAggregation.
     */    
    public LogElement findChild (String name);
    /** Find a child element with the specified exact path*/    
    public LogElement findElement (String path);
    /** Find all the children of this element that are accepted by the passed
     * ElementFilter instance.
     */    
    public List findElements (ElementFilter ef);
    /** Get the name of this object.  DataAggregations have names, which are used in
     * constructing paths.
     */    
    public String getName();
    /** Get an array of all of the child DataAggregations of this aggregation. */    
    public DataAggregation[] getChildAggregations();
    /** Write (currently serialize) the DataAggregation to disk for safe keeping. */    
    public void writeToFile (String filename) throws java.io.IOException;
    /** Get a list of all LogElement objects that belong directly to this
     * DataAggregation.
     */    
    public List getAllElements();
    /** A query interface for a DataAggregation's subtree.  This method is enhanced
     * by also allowing an ElementFilter to further winnow the results.
     */    
    public LogElement [] query (String search, ElementFilter ef);
    /** A query interface for a DataAggregation's subtree.  The query is a
     * multi-wildcard string, somewhat similar to filesystem wildcards on Unix.
     * An example query string could be<BR><code>/022802/Solaris^/gcinfo</code><BR>to
     * get all of the contained elements with the name <code>gcinfo</code> and whose
     * path begins with the prefix<code>/022802/Solaris</code>.<P>(<B><I>The above
     * example is wrong!  Replace the ^ with a * character. The * followed by a /
     * character breaks this javadoc comment).
     */    
    public LogElement [] query (String search);
}
