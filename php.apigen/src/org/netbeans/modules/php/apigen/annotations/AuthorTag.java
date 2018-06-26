/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.apigen.annotations;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class AuthorTag extends AnnotationCompletionTag {
    private static final Logger LOGGER = Logger.getLogger(AuthorTag.class.getName());
    private static final String USER_PROPERTZIES_PATH = "Templates/Properties/User.properties"; //NOI18N
    private static final String USER_PROPERTY_NAME = "user"; //NOI18N
    private static final String DEFAULT_USER = "John Doe <john.doe@example.com>"; //NOI18N

    public static AuthorTag create() {
        String userProperty = DEFAULT_USER;
        // TODO: maybe there is a better way how to get user properties, but noone knows it :)
        FileObject userPropertiesFile = FileUtil.getConfigFile(USER_PROPERTZIES_PATH);
        if (userPropertiesFile != null) {
            Properties properties = new Properties();
            try {
                properties.load(userPropertiesFile.getInputStream());
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            userProperty = properties.getProperty(USER_PROPERTY_NAME, DEFAULT_USER);
        }
        return new AuthorTag(userProperty);
    }

    public AuthorTag(String userProperty) {
        super("author", "@author ${\"" + userProperty + "\"}", //NOI18N
                NbBundle.getMessage(AuthorTag.class, "AuthorTag.documentation"));
    }

}
