/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.dbschema;

/** Placeholder to represent a database identifier - not really implemented
* yet.
*/
public final class DBIdentifier {
    private String name;
    transient private String fullName = null;

    /** Default constructor
     */
    public DBIdentifier() {
    }

    /** Creates a new identifier with a given name.
     * @param name the name
     */
    private DBIdentifier(String name) {
        this.name = name;
    }

    /** Creates an identifier with the supplied fully qualified name.
     * @param name the name of the identifier to create
     * @return the identifier
     */
    public static DBIdentifier create(String name) {
        String shortName = name.intern();
        String longName = null;
        int semicolonIndex = name.indexOf(';');
        DBIdentifier returnId = null;

        if (semicolonIndex == -1) {
            String testName = findShortName(name);

            if (!testName.equals(name)) {	
                shortName = testName.intern();
                longName = name;
            } else {
                int index = name.lastIndexOf('/');
                if (index != -1) {	
                    shortName = name.substring(index + 1).intern();
                    longName = name;
                }
            }
        } else {
            String firstHalf = name.substring(0, semicolonIndex);
            String secondHalf = name.substring(semicolonIndex + 1);
            String testFirstName = findShortName(firstHalf);
            String testSecondName = findShortName(secondHalf);

            if (!testFirstName.equals(firstHalf) && !testSecondName.equals(secondHalf)) {	
                shortName = testFirstName + ';' + testSecondName;
                longName = name;
            }
        }
        
        returnId = new DBIdentifier(shortName);

        if (longName != null)
            returnId.setFullName(longName);

        return returnId;
    }
    
    /** Returns a short name.
     * @param name the fully qualified name.
     * @return a short name.
     */
    private static String findShortName(String name) {
        int index = name.lastIndexOf('.');

        if (index != -1)
            return name.substring(index + 1);

        return name;
    }

    /** Gets the simple name within a package.
     * @return the simple name
     */
    public String getName() {
        return name;
    }
    
    /** Sets the simple name.
     * @param name the simple name
     */
    public void setName (String name) {
        this.name = name;
    }    

    /** Gets the fully qualified name with the schema/table prefix (if any).
     * @return the fully qualified name
     */
    public String getFullName () {
        return fullName;
    }
    
    /** Sets the fully qualified name.
     * @param fullName the fully qualified name
     */
    public void setFullName (String fullName) {
        this.fullName = fullName;
    }
  
    /** Returns a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString() {
        return name;
    }

    /** Compare the specified Identifier with this Identifier for equality.
     * @param id Identifier to be compared with this
     * @return true if the specified object equals to specified Identifier otherwise false.
     */
    public boolean compareTo(DBIdentifier id, boolean source) {
        if (id.fullName != null && fullName != null)
            if (id.fullName.equals(fullName))
                return true;
            else
                return false;

        if (id.name.equals(name))
            return true;
    
        return false;
    }
}
