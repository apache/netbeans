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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.glassfish.eecommon.api.config;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;


/**
 *  Base class to relate enumerated types of various J2EE/JavaEE versions.
 *
 * @author Peter Williams
 */
public abstract class J2EEBaseVersion implements Comparable {	

    /** -----------------------------------------------------------------------
     *  Implementation
     */
    // This is the module version id, string and numeric form.
    private final String j2eeModuleVersion; // e.g. "2.5" (servlet 2.5), "3.0" (ejb 3.0), etc.
    private final int numericModuleVersion;

    // This is the j2ee/javaee spec version, string and numeric form.
    private final int numericSpecVersion;

    
    /** Creates a new instance of J2EEBaseVersion 
     */
    protected J2EEBaseVersion(String moduleVersion, int nv, String specVersion, int nsv) {
        j2eeModuleVersion = moduleVersion;
        numericModuleVersion = nv;
        numericSpecVersion = nsv;
    }

    /** The string representation of this version.
     *
     * @return String representing the module specification version, e.g. servlet 2.x
     *   ejb-jar 2.x, etc.
     */
    @Override
    public String toString() {
        return j2eeModuleVersion;
    }

    /** Compare the j2ee/javaee spec version of this instance with another (as
     *  opposed to comparing the module type version.
     *
     * @param target Version object to compare with
     * @return -1, 0, 1 if this spec version is less than, equal to, or greater than
     *   the target version.
     */
    public int compareSpecification(J2EEBaseVersion target) {
        if(numericSpecVersion < target.numericSpecVersion) {
            return -1;
        } else if(numericSpecVersion > target.numericSpecVersion) {
            return 1;
        } else {
            return 0;
        }
    }

    /** For use by derived class to compare numeric versions.  Derived class
     *  should ensure target is the appropriate type before invoking this method
     *  to compare the version numbers themselves.
     *
     * @param target Version object to compare with
     * @return -1, 0, 1 if this module version is less than, equal to, or greater than
     *   the target version.
     */
    protected int numericCompare(J2EEBaseVersion target) {
        if(numericModuleVersion < target.numericModuleVersion) {
            return -1;
        } else if(numericModuleVersion > target.numericModuleVersion) {
            return 1;
        } else {
            return 0;
        }
    }

    public static J2EEBaseVersion getVersion(J2eeModule.Type moduleType, String moduleVersion) {
        J2EEBaseVersion version = null;
        if(J2eeModule.Type.WAR.equals(moduleType)) {
            version = ServletVersion.getServletVersion(moduleVersion);
        } else if(J2eeModule.Type.EJB.equals(moduleType)) {
            version = EjbJarVersion.getEjbJarVersion(moduleVersion);
        } else if(J2eeModule.Type.EAR.equals(moduleType)) {
            version = ApplicationVersion.getApplicationVersion(moduleVersion);
        } else if(J2eeModule.Type.CAR.equals(moduleType)) {
            version = AppClientVersion.getAppClientVersion(moduleVersion);
        }
        return version;
    }

    /*
    public static J2EEBaseVersion getJ2EEVersion(String version) {
        J2EEBaseVersion result = null;


        if(J2EE_1_3.toString().equals(version)) {
            result = J2EE_1_3;
        } else if(J2EE_1_4.toString().equals(version)) {
            result = J2EE_1_4;
        }

        return result;
    }
    */
    @Override
    public boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }
        if (this.getClass().isInstance(obj)) {
            return compareTo(obj) == 0;
        }
        return false;
    }


    @Override
    public int hashCode() {
        return numericSpecVersion;
    }
}
