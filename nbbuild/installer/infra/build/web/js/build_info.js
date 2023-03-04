/**
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


var build_info = new Object;

build_info.BUILD_DISPLAY_VERSION       = "{build.display.version}";
build_info.BUILD_DISPLAY_VERSION_SHORT = "{build.display.version.short}";

build_info.ZIP_FILES_PREFIX            = "{nb.zip.files.prefix}";
build_info.BUNDLE_FILES_PREFIX         = "{nb.bundle.files.prefix}";
build_info.BOUNCER_PRODUCT_PREFIX      = "{nb.bundle.files.prefix}";

build_info.COMMUNITY_BUILD             = "{community.mlbuild}";
build_info.SHOW_ADDITIONAL_LANGUAGES   = 0;//"{enable.languages.redirect}";

build_info.ADDITIONAL_BUILD_LOCATION   = "{alternative.languages.page.url}";

build_info.BUILD_LOCATION = "";

build_info.LOAD_OMNITURE_CODE          = 0;
build_info.LOAD_GOOGLE_ANALYTICS_CODE  = 0;
build_info.USE_BOUNCER                 = 0;
build_info.ADD_VERSION_INFO_TO_URL     = 0;

build_info.BOUNCER_URL = "http://services.netbeans.org/bouncer/index.php";

build_info.USE_HTML_ZIP_LISTING = 0;

build_info.add_download_tabs = function() {
	add_download_tab("8.1", "http://netbeans.org/downloads/");
	add_download_tab(DEVELOPMENT_TITLE /*,DEVELOPMENT_BUILDS_LINK*/);
	add_download_tab(ARCHIVE_TITLE,ARCHIVE_BUILDS_LINK);
}
add_build_info(build_info);
