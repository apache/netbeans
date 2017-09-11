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

package org.netbeans.modules.beans.beaninfo;

/** Resource string constants for pattern node icons.
*
* @author Petr Hrebejk
*/
interface IconBases {

    // Properties for Bean Info Features. There should be added S for selected features
    // and N for non selected features at the end of the string.

    public static final String BIF_DESCRIPTOR =
        "org/netbeans/modules/beans/resources/bifDescriptor"; // NOI18N !!! MUST BE CHANGED, BAD ICON

    public static final String BIF_PROPERTY_RW =
        "org/netbeans/modules/beans/resources/bifPropertyRW_"; // NOI18N

    public static final String BIF_PROPERTY_RO =
        "org/netbeans/modules/beans/resources/bifPropertyRO_"; // NOI18N

    public static final String BIF_PROPERTY_WO =
        "org/netbeans/modules/beans/resources/bifPropertyWO_"; // NOI18N

    public static final String BIF_IDXPROPERTY_RW =
        "org/netbeans/modules/beans/resources/bifIndexedPropertyRW_"; // NOI18N

    public static final String BIF_IDXPROPERTY_RO =
        "org/netbeans/modules/beans/resources/bifIndexedPropertyRO_"; // NOI18N

    public static final String BIF_IDXPROPERTY_WO =
        "org/netbeans/modules/beans/resources/bifIndexedPropertyWO_"; // NOI18N

    public static final String BIF_EVENTSET_MULTICAST =
        "org/netbeans/modules/beans/resources/bifEventSetMC_"; // NOI18N

    public static final String BIF_EVENTSET_UNICAST =
        "org/netbeans/modules/beans/resources/bifEventSetUC_"; // NOI18N

    public static final String BIF_METHOD =
        "org/netbeans/modules/beans/resources/bifMethod_"; // NOI18N

}
