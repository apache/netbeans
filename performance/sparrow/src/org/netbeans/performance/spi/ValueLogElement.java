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
 * ValueLogElement.java
 *
 * Created on October 8, 2002, 2:38 PM
 */

package org.netbeans.performance.spi;
import org.netbeans.performance.spi.html.*;
/**A log element that supplies a value derived from whatever
 * text it wraps.
 *
 * @author  Tim Boudreau
 */
public abstract class ValueLogElement extends AbstractLogElement implements Valued {
    protected Object value=null;

    /** Do nothing constructor for subclassers. */
    protected ValueLogElement () {
    }

    /** Creates a new instance of ValueLogElement, using the passed
     * String as the thing to be parsed to find the value
     * on demand.
     */
    public ValueLogElement(String s) {
        super (s);
    }

    /**Creates a new instances of ValueLogElement, using the
     * passed arguments as the value.  Instances created
     * by this constructor will <i>not</i> ever call the parse()
     * method - it is assumed the code that created them set
     * the values correctly.
     */
    protected ValueLogElement(Object value) {
        super();
        this.value = value;
        parsed=true;
    }
    
    public synchronized Object getValue () {
        checkParsed();
        return value;
    }

    /**Get a string representation of the object.  If the <code>line</code>
     * instance variable from AbstractLogElement is null, uses the <code>value</code>
     * field by calling toString() on it.
     */
    public String toString() {
        String result = super.toString();
        if (result == null) result = value.toString();
        return result;
    }


    public int hashCode () {
        checkParsed();
        if (value != null) {
            return value.hashCode() ^ 37;
        } else {
            return 0;
        }
    }
    
    public boolean equals (Object o) {
        checkParsed();
        return (o.getClass() == ValueLogElement.class) && ((ValueLogElement) o).value == value;
    }
    
    public HTML toHTML () {
        checkParsed();
        return new HTMLTextItem (value.toString());
    }
}
