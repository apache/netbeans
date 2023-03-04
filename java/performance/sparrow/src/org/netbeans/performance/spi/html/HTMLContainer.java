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
 * HTMLContainer.java
 *
 * Created on October 17, 2002, 7:43 PM
 */

package org.netbeans.performance.spi.html;

/** Interface defining HTML containers.  An HTML container is an HTML instance that
 * can contain other HTML instances (such as a table, a list or a document).
 * @author  Tim Boudreau
 */
public interface HTMLContainer {
    /** Add an HTML element to the container element.
     * @param html The string to add.
     */
    public void add(HTML html);
    /** Add a String to the container (it will be wrapped in an HTMLTextElement instance
     * and passed to <code>add (HTML html)</code>.
     * @param st The string to add.
     */
    public void add(String st);
    /** Returns an iterator that can iterate all of the elements of the container in
     * order.
     */
    public HTMLIterator iterator();
}
