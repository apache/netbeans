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
package org.netbeans.modules.php.nette2.utils;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class Constants {
    public static final String NETTE_ACTION_METHOD_PREFIX = "action"; //NOI18N
    public static final String NETTE_RENDER_METHOD_PREFIX = "render"; //NOI18N
    public static final String NETTE_PRESENTER_EXTENSION = ".php"; //NOI18N
    public static final String VALID_ACTION_NAME_REGEX = "^[a-zA-Z0-9][a-zA-Z0-9_]*$"; //NOI18N
    public static final String NETTE_PRESENTER_SUFFIX = "Presenter"; //NOI18N
    public static final String LATTE_MIME_TYPE = "text/x-latte"; //NOI18N
    public static final String LATTE_TEMPLATE_EXTENSION = ".latte"; //NOI18N
    public static final String ICON_PATH = "org/netbeans/modules/php/nette2/ui/resources/nette_badge_8.png"; // NOI18N
    public static final String COMMON_CONFIG_PATH = "app/config"; //NOI18N
    public static final String EXTRA_INDEX_PATH = "index.php"; //NOI18N
    public static final String COMMON_INDEX_PATH = "www/index.php"; //NOI18N
    public static final String COMMON_BOOTSTRAP_PATH = "app/bootstrap.php"; //NOI18N
    public static final String NETTE_LIBS_DIR = "/libs/Nette"; //NOI18N
    public static final String NETTE_TEMP_DIR = "/temp"; //NOI18N

    private Constants() {
    }

}
