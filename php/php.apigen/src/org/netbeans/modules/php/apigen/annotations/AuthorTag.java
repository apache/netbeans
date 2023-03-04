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
