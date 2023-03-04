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
 * AbstractHTMLContainer.java
 *
 * Created on October 17, 2002, 7:47 PM
 */

package org.netbeans.performance.spi.html;
import java.util.*;
/** Convenience base class for HTML container elements.
* @author  Tim Boudreau
*/
abstract class AbstractHTMLContainer extends AbstractHTML implements HTMLContainer {
    /** The HTML items in the container. */
    protected List items = new LinkedList();
    /** The title of the container or null. */
    protected String title = "";
    protected AbstractHTMLContainer(String title, int preferredWidth) {
        super(preferredWidth);
        this.title=title;
    }
    protected AbstractHTMLContainer(String title) {
        this.title = title;
    }
    protected AbstractHTMLContainer(int preferredWidth) {
        this.title = title;
    }
    protected AbstractHTMLContainer() {
    }
    public void add(HTML html) {
        if (html == this) throw new IllegalArgumentException ("Cannot add an element to itself!");
        items.add(html);
    }
    public void add(String st) {
        items.add(new HTMLTextItem(st)); 
    }
    public HTMLIterator iterator() {
        return new HTMLIterator(items);
    }
}

