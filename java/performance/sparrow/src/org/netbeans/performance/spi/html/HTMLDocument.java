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
 * HTMLDocument.java
 *
 * Created on October 17, 2002, 7:57 PM
 */

package org.netbeans.performance.spi.html;
import java.util.*;
/** Wrapper for an HTML document.
* @author Tim Boudreau
*/
public class HTMLDocument extends AbstractHTMLContainer {

    public HTMLDocument(String title) {
        super(title);
    }

    public HTMLDocument() {
    }

    public void toHTML(StringBuffer sb) {
        if (title.length() > 0) {
            genHtmlHeader(sb, title);
            sb.append("<H1>");
            sb.append(title);
            sb.append("</H1>\n<BR>");
        } else {
            genHtmlHeader(sb);
        }
        HTMLIterator i = iterator();
        while (i.hasNext()) {
            i.nextHTML().toHTML (sb);
        }
        sb.append("\n");
        genHtmlFooter(sb);
    }
}

