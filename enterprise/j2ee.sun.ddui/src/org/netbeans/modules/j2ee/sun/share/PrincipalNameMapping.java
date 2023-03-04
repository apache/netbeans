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
package org.netbeans.modules.j2ee.sun.share;

/** Mapping of principal name and optional class-name field.
 *
 * The one interesting characteristic of this class is that equals and hashCode
 * only take into account the principal name field so two instances with the same
 * principal name but different classnames would be considered equal.  The reason
 * for this is because only the principal name is used as a key when searching
 * for this object in a collection.  It does not make sense to have two instances
 * that differ only by classname.
 *
 * @author Peter Williams
 */
public final class PrincipalNameMapping {
    
    private String principalName;
    private String className;

    public PrincipalNameMapping(String pn) {
        this(pn, null);
    }

    public PrincipalNameMapping(String pn, String cn) {
        assert(pn != null) : "Principal name cannnot be null";
        
        principalName = pn;
        className = cn;
    }

    public String toString() {
        if(className == null || className.length() == 0) {
            return principalName;
        }
        StringBuffer buffer = new StringBuffer(principalName.length() + className.length() + 10);
        buffer.append(principalName);
        buffer.append(" [cn=");
        buffer.append(className);
        buffer.append("]");
        return buffer.toString();
    }

    public String getPrincipalName() {
        return principalName;
    }

    public String getClassName() {
        return className;
    }

    public boolean equals(Object obj) {
        boolean result = false;

        if(obj instanceof PrincipalNameMapping) {
            result = principalName.equals(((PrincipalNameMapping) obj).getPrincipalName());
        }

        return result;
    }

    public int hashCode() {
        return principalName.hashCode();
    }
}
