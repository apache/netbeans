/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
