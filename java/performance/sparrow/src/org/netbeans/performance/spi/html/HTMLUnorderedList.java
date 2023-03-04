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
 * HTMLUnorderedList.java
 *
 * Created on October 17, 2002, 7:53 PM
 */

package org.netbeans.performance.spi.html;

/** Wrapper for unordered lists.
 * @author Tim Boudreau
 */
public class HTMLUnorderedList extends AbstractHTMLContainer {
    public HTMLUnorderedList() {

    }

    public HTMLUnorderedList(String title) {
        super(title);
    }

    public HTMLUnorderedList(String title, int preferredWidth) {
        super(title, preferredWidth);
    }

    public void toHTML (StringBuffer sb){
        sb.append ("<BR>");
        if (title.length() > 0) {
            sb.append("<B>");
            sb.append(title);
            sb.append("</B><BR>\n");
        }
        sb.append("<UL>");
        HTMLIterator i = iterator();
        HTML next;
        while (i.hasNext()) {
            next = i.nextHTML();
            if (next instanceof HTMLListItem) {
                sb.append(next.toHTML());
            } else {
                sb.append("\n   <LI>");
                sb.append(next.toHTML());
                sb.append("   </LI>");
            }
        }
        sb.append("</UL><P>\n");
    }
}
