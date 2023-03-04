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

package org.netbeans.modules.dbschema;

/** Placeholder to represent a database identifier - not really implemented
* yet.
*/
public final class DBIdentifier {
    private String name;
    private transient String fullName = null;

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
