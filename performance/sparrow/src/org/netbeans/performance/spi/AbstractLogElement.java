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
 * AbstractLogElement.java
 *
 * Created on October 8, 2002, 2:30 PM
 */

package org.netbeans.performance.spi;
import org.netbeans.performance.spi.html.*;
import java.io.Serializable;
/** Abstraction of an element in a log file, or other loggable
 * information.  Imposes no particular constraints on what is
 * logged, or whether it is a name-value pair, an event with a
 * timestamp, something you just want to count the number of
 * or anything else.  It does contain some infrastructure for
 * parsing lines of text on demand, on the assumption that most
 * subclasses will need to do that.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractLogElement implements LogElement, Serializable {
    protected String line;
    protected boolean parsed=false;
    protected DataAggregation parent=null;
    private String path=null;
    /**Name string is used to construct the path, but is not necessarily
     * exposed unless the subclass implements Named.
     */
    protected String name="Unnamed";

    /** Creates a new instance of AbstractLogElement using the
     * passed String as the line from a log file which this object
     * will represent.  The line will be parsed on demand when
     * accessors in subclasses call checkParsed() (which will,
     * in turn, call parse() if the line has not already been
     * parsed).
     */
    protected AbstractLogElement(String line) {
        this.line = line;
    }
    
    /**No-argument constructor.  Note that subclasses that use
     * this constructor will need to override methods such as
     * <code>toString()</code> which assume that the <code>line</code>
     * field will have a value.
     */
    protected AbstractLogElement () {
    }
    
    /**Parse the String passed to the constructor to generate any
     * instance fields client code may ask for.  Note that while it
     * is unlikely that this code will be run in a multithreaded
     * environment, accessors which call checkparsed before returning
     * a value should be synchronized for safety's sake.
     */
    protected void checkParsed () {
        if (!parsed) {
            try {
                parse();
                parsed=true;
            } catch (ParseException pe) {
                pe.printStackTrace();
            }
        }
    }
    

    
    /** Parse the String passed to the constructor, which presumably consists
     * of a line from a log file.  This method should populate any instance
     * fields which subclasses will provide accessors for. 
     * Subclasses should not call this method directly, but instead call
     * checkParse() in accessors which rely of fully parsed data being
     * available.<P>Note that this method <i>can</i> throw an UnsupportedOperationException
     * (used only in the case of NameValueLogElement, which is a special case).
     */
    protected abstract void parse() throws ParseException;
    
    /**Returns a String representation of the object, presumably the original
     * log line that created the instance.  <B>Note:</B>  Subclasses that 
     * directly or indirectly expose the no-argument constructor (for example,
     * <code>NameValueLogElement</code>, which allows a name-value pair to
     * be passed to the constructor, rather than parsing a line of text to
     * derive the name and value) need override this method and return a
     * reasonable String representation of the log element.
     */
    public String toString() {
        return line;
    }
    
    /**Log elements have a concept of a &quot;path&quot; which allows them
     * to be differentiated based on what set of test runs they belong to,
     * or other information.  This is primarily useful for filtering and
     * building reports.  The path field is set in the addNotify() method.
     */
    public String getPath() {
        if (parent == null) {
            return "/" + name;
        } else {
            return getParent().getPath() + "/" + name;
        } 
    }
    
    protected void addNotify (DataAggregation parent) {
        this.parent = parent;
    }
    
    public DataAggregation getParent () {
        return parent;
    }
    /*
    public int hashCode() {
        return getPath().hashCode() * name.hashCode() ^ 37;
    }
     */
    
    
    public DataAggregation findAncestor(String name) {
        DataAggregation curr = getParent();
        while (curr != null) {
            if (curr.getName().equals(name)) {
                return curr;
            } else {
                curr = curr.getParent();
            }
        }
        return null;
    }
    
    public HTML toHTML() {
        return new HTMLTextItem ("Instance of " + getClass());
    }
    
}
