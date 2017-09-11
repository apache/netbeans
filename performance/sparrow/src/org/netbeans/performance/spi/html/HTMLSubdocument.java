/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
