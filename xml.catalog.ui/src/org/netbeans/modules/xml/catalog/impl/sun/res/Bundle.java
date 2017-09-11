/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.catalog.impl.sun.res;

import org.openide.util.NbBundle;


public final class Bundle {
    private Bundle() {
        throw new IllegalStateException("No Instance Allowed");     //NOI18N
    }

    public static String PROP_Catalog() {
        return NbBundle.getMessage(Bundle.class, "PROP_Catalog");
    }

    public static String PROP_Catalog_desc() {
        return NbBundle.getMessage(Bundle.class, "PROP_Catalog_desc");
    }

    public static String PROP_catalog_name() {
        return NbBundle.getMessage(Bundle.class, "PROP_catalog_name");
    }

    public static String PROP_catalog_name_desc() {
         return NbBundle.getMessage(Bundle.class, "PROP_catalog_name_desc");
    }

    public static String PROP_catalog_info() {
        return NbBundle.getMessage(Bundle.class, "PROP_catalog_info");
    }

    public static String PROP_catalog_info_desc() {
        return NbBundle.getMessage(Bundle.class, "PROP_catalog_info_desc");
    }

    public static String ACSD_CatalogCustomizer() {
        return NbBundle.getMessage(Bundle.class, "ACSD_CatalogCustomizer");
    }

    public static String CatalogCustomizer_locationLabel_mneString() {
        return NbBundle.getMessage(Bundle.class, "CatalogCustomizer.locationLabel.mne");
    }

    public static String ACSD_locationTextField() {
        return NbBundle.getMessage(Bundle.class, "ACSD_locationTextField");
    }

    public static String MNE_preference() {
        return NbBundle.getMessage(Bundle.class, "MNE_preference");
    }

    public static String ACSD_preference() {
        return NbBundle.getMessage(Bundle.class, "ACSD_preference");
    }

    public static String MNE_file() {
        return NbBundle.getMessage(Bundle.class, "MNE_file");
    }

    public static String ACSD_file() {
        return NbBundle.getMessage(Bundle.class, "ACSD_file");
    }

    public static String CatalogCustomizer_locationLabel_text() {
        return NbBundle.getMessage(Bundle.class, "CatalogCustomizer.locationLabel.text");
    }

    public static String HINT_pp() {
        return NbBundle.getMessage(Bundle.class, "HINT_pp");
    }

    public static String DESC_catalog_fmts() {
        return NbBundle.getMessage(Bundle.class, "DESC_catalog_fmts");
    }
}
