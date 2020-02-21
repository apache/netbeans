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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.cnd.debugger.dbx;

import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.FormatOption;

/**
 *
 * 
 */
public enum DbxEvalFormat implements FormatOption {
    DEFAULT(Catalog.get("Default_format"), ""), //NOI18N
    HEXADECIMAL4(Catalog.get("l_Hexadecimal"), "-fx"), //NOI18N
    HEXADECIMAL8(Catalog.get("L_Hexadecimal"), "-flx"), //NOI18N
    DECIMAL4(Catalog.get("l_Decimal"), "-fd"), //NOI18N
    DECIMAL8(Catalog.get("L_Decimal"), "-fld"), //NOI18N
    UNSIGNED_DECIMAL4(Catalog.get("l_U_Decimal"), "-fu"), //NOI18N
    UNSIGNED_DECIMAL8(Catalog.get("L_U_Decimal"), "-flu"), //NOI18N
    FLOAT4(Catalog.get("l_Float"), "(float)"), //NOI18N
    FLOAT8(Catalog.get("L_Float"), "(double)"); //NOI18N

    private final String dispName;
    private final String option;
    
    DbxEvalFormat(String dispName, String option) {
        this.dispName = dispName;
        this.option = option;
    }

    @Override
    public String toString() {
        return dispName;
    }

    public String getOption() {
        return option;
    }
}
