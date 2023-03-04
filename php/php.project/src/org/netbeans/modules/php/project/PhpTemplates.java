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
package org.netbeans.modules.php.project;

import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 * @author ads
 */
final class PhpTemplates implements RecommendedTemplates, PrivilegedTemplates {

    private static final String[] TYPES = new String[] {
        "PHP", // NOI18N
        "XML", // NOI18N
        "simple-files", // NOI18N
        "html5", // NOI18N
    };

    private static final String[] PRIVILEGED_NAMES = new String[] {
        /*
         * See discussion about the set of templates here:
         * http://php.netbeans.org/issues/show_bug.cgi?id=122121
         */
        "Templates/Scripting/EmptyPHP.php", // NOI18N
        "Templates/Scripting/EmptyPHPWebPage.php", // NOI18N
        "Templates/Scripting/PHPClass.php", // NOI18N
        "Templates/Scripting/PHPInterface.php", // NOI18N
        "Templates/Other/html.html", // NOI18N
        "Templates/Other/xhtml.xhtml", // NOI18N
        "Templates/Other/javascript.js", // NOI18N
        "Templates/Other/CascadeStyleSheet.css", // NOI18N
        "Templates/ClientSide/style.scss", // NOI18N
        "Templates/ClientSide/style.less", // NOI18N
        "Templates/Other/Folder", // NOI18N
    };

    @Override
    public String[] getRecommendedTypes() {
        return TYPES;
    }

    @Override
    public String[] getPrivilegedTemplates() {
        return PRIVILEGED_NAMES;
    }
}
