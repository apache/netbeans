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
 * HTMLListItem.java
 *
 * Created on October 17, 2002, 7:55 PM
 */

package org.netbeans.performance.spi.html;
/** Wrapper for list items
 * @author Tim Boudreau
 */
public class HTMLListItem extends HTMLTextItem {
    String topic;
    String topicLink = null;
    String target = null;
    public HTMLListItem(String topic, String description) {
        super(description);
        this.topic = topic;
    }

    public HTMLListItem(String topic, String description, String topicLink) {
        super(description);
        this.topic = topic;
        this.topicLink = topicLink;
    }

    public HTMLListItem(String topic, String description, String topicLink, String browserTarget) {
        super(description);
        this.topic = topic;
        this.topicLink = topicLink;
        this.target = browserTarget;
    }

    public void toHTML(StringBuffer sb) {
        sb.append("\n  <LI><B>");
        if (topicLink != null) {
            sb.append ("<A HREF=\"");
            sb.append (topicLink);
            sb.append ("\"");
            if (target != null) {
                sb.append (" TARGET=\"");
                sb.append (target);
                sb.append ("\"");
            }
            sb.append (">");
        }
        sb.append(topic);
        if (topicLink != null) {
            sb.append ("</A>");
        }
        sb.append("</B> - ");
        super.toHTML(sb);
        sb.append("</LI>");
    }
}
