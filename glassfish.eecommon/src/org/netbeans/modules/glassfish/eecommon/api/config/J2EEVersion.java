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
package org.netbeans.modules.glassfish.eecommon.api.config;


/**
 *  Enumerated types for various J2EE versions.
 *
 *  Be careful with the compareTo method of this class.  It is there for comparing
 *  like versions (e.g. servlet 2.3 versus 2.4) only, but there is no type safety
 *  to prevent doing dumb things like comparing J2EE 1.4 with servlet 2.3.
 *
 *  Perhaps I can think of a better design in the next version.
 *
 * @author Peter Williams
 */
public final class J2EEVersion extends J2EEBaseVersion {	

    /** Represents J2EE version 1.3
     */
    public static final J2EEVersion J2EE_1_3 = new J2EEVersion(
            "1.3", 1300,    // NOI18N
            "1.3", 1300);   // NOI18N

    /** Represents J2EE version 1.4
     */
    public static final J2EEVersion J2EE_1_4 = new J2EEVersion(
            "1.4", 1400,    // NOI18N
            "1.4", 1400);   // NOI18N	

    /** Represents JavaEE version 5.0
     */
    public static final J2EEVersion JAVAEE_5_0 = new J2EEVersion(
            "5.0", 5000,    // NOI18N
            "5.0", 5000);   // NOI18N	

    /** Represents JavaEE version 6.0
     */
    public static final J2EEVersion JAVAEE_6_0 = new J2EEVersion(
            "6.0", 6000,    // NOI18N
            "6.0", 6000);   // NOI18N

    /** -----------------------------------------------------------------------
     *  Implementation
     */

    /** Creates a new instance of J2EEVersion 
     */
    private J2EEVersion(String version, int nv, String specVersion, int nsv) {
        super(version, nv, specVersion, nsv);
    }

    /** Comparator implementation that works only on J2EEVersion objects
     *
     *  @param obj J2EEVersion to compare with.
     *  @return -1, 0, or 1 if this version is less than, equal to, or greater
     *     than the version passed in as an argument.
     *  @throws ClassCastException if obj is not a J2EEVersion object.
     */
    public int compareTo(Object obj) {
        J2EEVersion target = (J2EEVersion) obj;
        return numericCompare(target);
    }

    public static J2EEVersion getJ2EEVersion(String version) {
        J2EEVersion result = null;

        if(J2EE_1_3.toString().equals(version)) {
            result = J2EE_1_3;
        } else if(J2EE_1_4.toString().equals(version)) {
            result = J2EE_1_4;
        } else if(JAVAEE_5_0.toString().equals(version)) {
            result = JAVAEE_5_0;
        } else if(JAVAEE_6_0.toString().equals(version)) {
            result = JAVAEE_6_0;
        }

        return result;
    }	
}
