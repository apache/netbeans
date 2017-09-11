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

import java.util.Locale;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.modules.SpecificationVersion;

/** Specification of the Java SDK
 */
public final class Specification {

    private final String name;
    private final SpecificationVersion version;
    private final Profile[] profiles;
    private final String displayName;


    /**
     * Creates new SDK Specification
     * @param name of the specification e.g J2SE
     * @param version of the specification e.g. 1.4
     */
    public Specification (@NullAllowed String name, @NullAllowed SpecificationVersion version) {
        this (name, version, null, null);
    }
        

    /**
     * Creates new SDK Specification
     * @param name of the specification e.g J2SE
     * @param version of the specification e.g. 1.4
     * @param profiles of the Java SDK
     */
    public Specification (
        @NullAllowed final String name,
        @NullAllowed final SpecificationVersion version,
        @NullAllowed final Profile[] profiles) {
        this(name, version, null, profiles);
    }
    
    /**
     * Creates new SDK Specification
     * @param name of the specification e.g J2SE
     * @param version of the specification e.g. 1.4
     * @param displayName the display name of the Java SDK e.g. "Java SE".
     * @param profiles of the Java SDK
     * @since 1.26
     */
    public Specification (
        @NullAllowed final String name,
        @NullAllowed final SpecificationVersion version,
        @NullAllowed final String displayName,
        @NullAllowed final Profile[] profiles) {
        this.name = name;
        this.version = version;
        this.displayName = displayName;
        this.profiles = profiles;
    }

    /**
     * Returns the name of the Java specification, e.g. {@code j2se}
     * @return String
     */
    public final String getName () {
        return this.name;
    }

    /**
     * Returns the version of the Java specification e.g 1.4
     * @return instance of SpecificationVersion
     */
    public final SpecificationVersion getVersion () {
        return this.version;
    }

    /**
     * Returns profiles supported by the Java platform.
     * @return list of profiles, or null if not applicable
     */
    public final Profile[] getProfiles () {
        return this.profiles;
    }
    
    /**
     * Returns the display name of the Java SDK.
     * While the {@link Specification#getName()} is used as a system name
     * the {@link Specification#getDisplayName()} is used while presenting the
     * Java SDK to the user.
     * @return the user friendly name, e.g. "Java SE" for "j2se" SDK.
     * @since 1.26
     */
    @NonNull
    public String getDisplayName() {
        if (displayName != null) {
            return displayName;
        }
        final String defaultName = getName();
        return defaultName == null ?
            "": //NOI18N
            defaultName.toUpperCase(Locale.ENGLISH);
    }

    @Override
    public int hashCode () {
        int hc = 0;
        if (this.name != null)
            hc = this.name.hashCode() << 16;
        if (this.version != null)
            hc += this.version.hashCode();
        return hc;
    }

    @Override
    public boolean equals (Object other) {
        if (other instanceof Specification) {
            Specification os = (Specification) other;
            boolean re = this.name == null ? os.name == null : this.name.equals(os.name) &&
                         this.version == null ? os.version == null : this.version.equals (os.version);
            if (!re || this.profiles == null)
                return re;
            if (os.profiles == null || this.profiles.length != os.profiles.length)
                return false;
            for (int i=0; i<os.profiles.length; i++)
                re &= this.profiles[i].equals(os.profiles[i]);
            return re;
        }
        else
            return false;
    }

    @Override
    public String toString () {
        String str = this.name == null ? "" : this.name + " "; // NOI18N
        str += this.version == null ? "" : this.version + " "; // NOI18N
        if (this.profiles != null) {
            str+="["; // NOI18N
            for (int i = 0; i < profiles.length; i++) {
                str+= profiles[i]+ " "; // NOI18N
            }
            str+="]"; // NOI18N
        }
        return str;
    }

}
