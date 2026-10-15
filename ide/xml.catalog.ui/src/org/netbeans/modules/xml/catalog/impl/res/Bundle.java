/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.catalog.impl.res;

import org.openide.util.NbBundle;


public class Bundle {
    private Bundle() {
        throw new IllegalStateException("No Instance Allowed"); //NOI18N
    }

    public static String NAME_x_catalog() {
        return NbBundle.getMessage(Bundle.class, "NAME_x_catalog");
    }

    public static String TEXT_x_catalog_desc() {
        return NbBundle.getMessage(Bundle.class, "TEXT_x_catalog_desc");
    }

    public static String PROP_xcatalog_location() {
        return NbBundle.getMessage(Bundle.class, "PROP_xcatalog_location");
    }

    public static String PROP_xcatalog_location_desc() {
        return NbBundle.getMessage(Bundle.class, "PROP_xcatalog_location_desc");
    }

    public static String PROP_xcatalog_name() {
        return NbBundle.getMessage(Bundle.class, "PROP_xcatalog_name");
    }

    public static String PROP_xcatalog_name_desc() {
        return NbBundle.getMessage(Bundle.class, "PROP_xcatalog_name_desc");
    }

    public static String PROP_xcatalog_info() {
        return NbBundle.getMessage(Bundle.class, "PROP_xcatalog_info");
    }

    public static String PROP_xcatalog_info_desc() {
        return NbBundle.getMessage(Bundle.class, "PROP_xcatalog_info_desc");
    }

    public static String NAME_system_catalog() {
        return NbBundle.getMessage(Bundle.class, "NAME_system_catalog");
    }

    public static String TEXT_system_catalog_desc() {
        return NbBundle.getMessage(Bundle.class, "TEXT_system_catalog_desc");
    }

    public static String ACSD_SystemCatalogCustomizer() {
        return NbBundle.getMessage(Bundle.class, "ACSD_SystemCatalogCustomizer");
    }

    public static String SystemCatalogCustomizer_readOnly_text() {
        return NbBundle.getMessage(Bundle.class, "SystemCatalogCustomizer.readOnly.text");
    }

    public static String ACSD_XCatalogCustomizer() {
        return NbBundle.getMessage(Bundle.class, "ACSD_XCatalogCustomizer");
    }

    public static String XCatalogCustomizer_locationLabel_mne() {
        return NbBundle.getMessage(Bundle.class, "XCatalogCustomizer.locationLabel.mne");
    }

    public static String ACSD_locationTextField() {
        return NbBundle.getMessage(Bundle.class, "ACSD_locationTextField");
    }

    public static String XCatalogCustomizer_locationLabel_text() {
        return NbBundle.getMessage(Bundle.class, "XCatalogCustomizer.locationLabel.text");
    }

    public static String DESC_xcatalog_fmts() {
        return NbBundle.getMessage(Bundle.class, "DESC_xcatalog_fmts");
    }

    public static String TEXT_catalog_not_valid() {
        return NbBundle.getMessage(Bundle.class, "TEXT_catalog_not_valid");
    }
}
