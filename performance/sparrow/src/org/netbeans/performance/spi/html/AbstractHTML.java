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
 * AbstractHTML.java
 *
 * Created on October 17, 2002, 7:50 PM
 */

package org.netbeans.performance.spi.html;
import java.io.*;
import java.util.Date;
/** Abstract base class for HTML subclasses, with convenience methods
 * for generating bits and pieces of HTML.
 * @author Tim Boudreau */
abstract class AbstractHTML implements HTML {
    private int preferredWidth=DONT_CARE;
    /** Create an instance, passing the preferred size. */
    protected AbstractHTML(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    /** Create an instance. */
    protected AbstractHTML() {
    }

    /** Get the preferred width (in table columns) of the element. */
    public int getPreferredWidth() {
        return preferredWidth;
    }

    public void writeToFile(String filename) throws IOException {
        File f = new File(filename);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(toHTML().getBytes());
        System.out.println("Wrote html to " + f.toString());
    }

    public String toString() {
        return toHTML();
    }

    public String toHTML () {
        StringBuffer sb = new StringBuffer();
        toHTML (sb);
        return sb.toString();
    }
    
    protected static final void genHtmlFooter(final StringBuffer sb) {
        sb.append("\n</BODY></HTML>");
    }
    
    protected static final void genHtmlHeader(final StringBuffer sb) {
        sb.append("<HTML><HEAD><TITLE>Test Report " + new Date().toString() + "</TITLE></HEAD><BODY BGCOLOR=\"#FFFFFF\">\n");
    }
    
    protected static final void genHtmlHeader(final StringBuffer sb, String title) {
        sb.append("<HTML><HEAD><TITLE>" + title + "</TITLE></HEAD><BODY BGCOLOR=\"#FFFFFF\">\n");
    }
    
    protected static final void genTableClose(final StringBuffer sb) {
        sb.append("\n</TABLE></TD></TR></TABLE>\n");
    }
    
    protected static final void genTableEntryClose(final StringBuffer sb) {
        sb.append("\n    </TD>");
    }
    
    protected static final void genTableEntryOpen(final StringBuffer sb) {
        sb.append("\n    <TD BGCOLOR=\"#FFFFFF\">");
    }
    
    protected static final void genTableHeaderClose(final StringBuffer sb) {
        sb.append("\n  </FONT></TH></TR>");
    }
    
    protected static final void genTableHeaderOpen(final StringBuffer sb) {
        genTableHeaderOpen(sb, 2);
    }
    
    protected static final void genTableHeaderOpen(final StringBuffer sb, int colspan) {
        sb.append("\n  <TR><TH COLSPAN=" + Integer.toString(colspan) + " BGCOLOR=#8888CC><FONT COLOR=#FFFFFF>");
    }
    
    protected static final void genTableHeaderOpen(final StringBuffer sb, int colspan, String colorString) {
        sb.append("\n  <TR><TH COLSPAN=" + Integer.toString(colspan) + " BGCOLOR=" +colorString + "><FONT COLOR=#FFFFFF>");
    }
    
    protected static final void genTableOpen(final StringBuffer sb, String title, int colspan) {
        genTableOpen(sb);
        if (title.length() > 0) {
            genTableHeaderOpen(sb, colspan);
            sb.append("<B>");
            sb.append(title);
            sb.append("</B>");
            genTableHeaderClose(sb);
        }
    }
    
    protected static final void genTableOpen(final StringBuffer sb, String title, int colspan, String colorString) {
        genTableOpen(sb);
        if (title.length() > 0) {
            genTableHeaderOpen(sb, colspan, colorString);
            sb.append("<B>");
            sb.append(title);
            sb.append("</B>");
            genTableHeaderClose(sb);
        }
    }
    
    protected static final void genTableOpen(StringBuffer sb) {
        sb.append("<TABLE BORDER=0 WIDTH=100% HEIGHT=100% BGCOLOR=\"#FFFFFF\"><TR><TD BGCOLOR=#CCCCCC><TABLE BORDER=0 WIDTH=100% HEIGHT=100% CELLSPACING=1 CELLPADDING=3>\n");
    }
    
    protected static final void genTableOpen(final StringBuffer sb, String title) {
        genTableOpen(sb, title, 2);
    }
    
    protected static final void genTableRowClose(final StringBuffer sb) {
        sb.append("\n  </TR>");
    }
    
    protected static final void genTableRowOpen(final StringBuffer sb) {
        sb.append("\n  <TR>");
    }
    
    protected static final void genTableHeader(final StringBuffer sb, String text) {
        genTableHeaderOpen(sb);
        sb.append(text);
        genTableHeaderClose(sb);
    }
    
    protected static final void addRowData(final StringBuffer sb, String title, Object data) {
        genTableRowOpen(sb);
        genTableEntryOpen(sb);
        sb.append("\n      " + title);
        genTableEntryClose(sb);
        genTableEntryOpen(sb);
        sb.append("<B>");
        sb.append("\n      " + ((data==null) ? "[null]" : data));
        sb.append("</B>");
        genTableEntryClose(sb);
        genTableRowClose(sb);
    }
    
    protected static final void genNullTableEntry(StringBuffer sb) {
        sb.append("\n    <TD BGCOLOR=#FFFFFF>&nbsp;</TD>");
    }
    
    /** A convenience method if it is necessary to produce formatted tooltips from 
     * arbitrary objects.
     */
    public static void wrapInHTMLTags (HTML html) {
        StringBuffer sb = new StringBuffer();
        sb.append ("<HTML>");
        html.toHTML (sb);
        sb.append ("</HTML>");
    }
    
    /**Test execution for HTML stuff.
     */
    public static void main(String[] args) throws Exception {
        HTMLDocument doc = new HTMLDocument("Test document");
        HTMLTextItem txt = new HTMLTextItem("This is a test document - we will see if it really works!");
        doc.add(txt);
        HTMLUnorderedList list = new HTMLUnorderedList("This is a list");
        list.add("item 1");
        list.add("item 2");
        list.add("item 3");
        list.add("item 4");
        list.add(new HTMLListItem("item 5", "This is a topical item"));
        list.add(new HTMLListItem("NetBeans.org", "The place everything happens", "http://www.netbeans.org", "anotherBrowser"));
        doc.add(list);
        HTMLTable table1 = new HTMLTable("A simple table");
        table1.add(txt);
        table1.add("I have some trees in my shoe");
        table1.add("Would you like some");
        table1.add("That is very good of you.");
        table1.add("That is very good of me too.");
        doc.add(table1);
        doc.add("<P>Okay, now lets try another table");
        HTMLTable table2 = new HTMLTable("A more complex table", 3,2);
        table2.add(txt);
        table2.add("I have some trees in my shoe");
        table2.add("Would you like some");
        table2.add(new HTMLTextItem ("This should take up an entire row.", HTML.SINGLE_ROW));
        table2.add("That is very good of you.");
        table2.add("That is very good of me too.");
        doc.add(table2);
        doc.add ("Boy this is fun!");
        HTMLTable table3 = new HTMLTable ("Tables within tables", 5);
        table3.add ("Hello there");
        table3.add (table2);
        table3.add ("I'm a little teacup");
        table3.add (table1);
        doc.add (table3);
        doc.add("That's all for now!");
        HTMLTextItem txt2 = new HTMLTextItem("This item wants to be wider than the table",22);
        table2.add (txt2);
        System.out.println(doc.toHTML());
        HTMLTable table4 = new HTMLTable("Really nested tables");
        table4.add (table1);
        table4.add (table2);
        table4.add ("I'm still a little teapot");
        table4.add (table3);
        table4.add ("I'm still a little teapot");
        HTMLTable table5 = new HTMLTable("Really nested tables");
        table5.add (table4);
        table5.add (table2);
        table5.add ("I'm still a little teapot");
        table5.add (table4);
        table5.add ("I'm still a little teapot");
        doc.add (table5);
        doc.writeToFile("/tmp/test.html");
    }    
}
