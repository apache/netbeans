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
 * NameValuePairLogElement.java
 *
 * Created on October 8, 2002, 3:01 PM
 */

package org.netbeans.performance.spi;
import org.netbeans.performance.spi.html.*;
/** Extension of NameValueLogElement with basic parsing of
 * lines containing a separator character (the default is
 * <code>=</code> unless a different argument is passed
 * to the appropriate constructor.  Also contains a method
 * for converting the parsed String representation of the
 * resulting object into some other object type if needed.
 * The default implementation just keeps the String.
 *
 * @author  Tim Boudreau
 */
public class NameValuePairLogElement extends ValueLogElement implements Named {
    private transient String separator = "=";
    /** A do nothing constructor for subclassers. */
    protected NameValuePairLogElement() {
        //do nothing constructor for subclassers.
    }

    /** Creates a new instance of NameValuePairLogElement */
    public NameValuePairLogElement(String s) {
        super (s.trim());
    }
    /**Constructor for name value pairs separated by some character
     * other than &quot;=&quot;.
     */
    public NameValuePairLogElement (String s, String separator) {
        this (s);
        this.separator = separator;
    }
    
    /**Parses the name value pair string passed to the constructor in
     * order to populate the name and value properties.  This method
     * will be called automatically by checkParsed() - subclasses should
     * not call this method directly, but simply make sure that all of
     * the field accessors on their classes call checkParsed before
     * returning a value.  Subclasses should populate any instance
     * fields representing properties of a log entry here.
     */
    protected void parse() throws ParseException {
        if (name.indexOf (separator) == -1) {
            throw new ParseException (line, "Separator " + separator + " not found in text " + line + " for " + getPath());
        }
        int idx = line.indexOf (separator);
        name = line.substring (0, idx).trim();
        value = processValue(line.substring (idx+1, line.length()-1).trim());
    }

    /**Method to allow further parsing of the value string - for example, 
     * integer or float log entries will want to override this method and
     * return an appropriate Integer or Float object by parsing the String.
     * This method is called by the default implementation of parse() in 
     * NameValuePairLogElement;  the base implementation does not perform
     * any conversion, but simply returns the String it is passed.
     */
    protected Object processValue (String stringval) {
        return stringval;
    }
    

    public synchronized String getName () {
        checkParsed();
        return name;
    }    
    
    public String toString() {
        checkParsed();
        return name + "=" + value;
    }
    
    public HTML toHTML() {
        checkParsed();
        HTMLTable result = new HTMLTable ();
        result.add (name);
        result.add (new HTMLTextItem (value));
        return result;
    }
    
    /*
     //XXX todo
    public int hashCode () {
        checkParsed();
        return name.hashCode() * value.hashCode() ^ 37;
    }
    
    public boolean equals (Object o) {
        return (o instanceof NameValuePairLogElement) && 
          (((NameValuePairLogElement) o).name.equals (name));
    }
     */
    
}
