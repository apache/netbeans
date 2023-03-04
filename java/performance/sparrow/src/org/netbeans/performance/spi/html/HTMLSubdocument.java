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
 * HTMLSubdocument.java
 *
 * Created on October 17, 2002, 9:50 PM
 */

package org.netbeans.performance.spi.html;

/** A convenient HTML subdocument wrapper.  Handy if you are not creating
 * the sections of an HTML document in the order they should appear in
 * the document.  Supports titles and generating a name link for the
 * subdocument.
  * @author  Tim Boudreau
 */
public class HTMLSubdocument extends AbstractHTMLContainer {
    String name=null;
    /** Creates a new instance of HTMLSubdocument.  An A NAME tag will be
     * generated with the passed name.  A title header will appear if the
     * title is non-null and non-0-length. */
    public HTMLSubdocument(String title, String name, int preferredWidth) {
        super (title, preferredWidth);

    }

    public HTMLSubdocument(String title, String name) {
        super (title);
        this.name=name;
    }

    public HTMLSubdocument(String title) {
        super (title);
    }

    public HTMLSubdocument() {
    }

    public String getName() {
        return name;
    }
    
    public void toHTML (StringBuffer sb) {
	HTMLIterator i = iterator();
        if (name != null) {
            sb.append ("<A NAME=\"");
            sb.append (name);
            sb.append ("\">&nbsp;</A>");
        }
        if (title !=null) {
            sb.append ("<H3>");
            sb.append (title);
            sb.append ("</H3>\n");
        }
	while (i.hasNext()) {
            i.nextHTML().toHTML(sb);
            sb.append ("\n");
        }
    }
    
    public void para () {
        add ("<P>");
    }
    
    public void hr() {
        add ("<HR>");
    }
    
    public void br() {
        add ("<BR>");
    }
    
}
