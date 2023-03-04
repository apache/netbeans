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
