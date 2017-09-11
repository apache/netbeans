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
package org.netbeans.api.java.platform;

import org.openide.modules.SpecificationVersion;

/**
 * Represents profile installed in the Java SDK
 */
public class Profile {

    private String name;
    private SpecificationVersion version;

    /**
     * Creates new Profile
     * @param name of the profile, e.g. MIDP
     * @param version of the profile, e.g. 1.0
     */
    public Profile (String name, SpecificationVersion version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Returns the name of the profile
     * @return String
     */
    public final String getName () {
        return this.name;
    }

    /**
     * Returns the version of the profile
     * @return String
     */
    public final SpecificationVersion getVersion () {
        return this.version;
    }


    public int hashCode () {
        int hc = 0;
        if (name != null)
            hc = name.hashCode() << 16;
        if (version != null)
            hc += version.hashCode();
        return hc;
    }

    public boolean equals (Object other) {
        if (other instanceof Profile) {
            Profile op = (Profile) other;
            return this.name == null ? op.name == null : this.name.equals(op.name) &&
                   this.version == null ? op.version == null : this.version.equals (op.version);
        }
        else
            return false;
    }

    public String toString () {
        String str;
        str = this.name == null ? "" : this.name;
        str += " " + this.version == null ? "" : this.version.toString(); // NOI18N
        return str;
    }
}
