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
