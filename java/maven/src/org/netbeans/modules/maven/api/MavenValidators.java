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

package org.netbeans.modules.maven.api;

import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ValidatorUtils;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class MavenValidators {

    public static Validator<String> createArtifactIdValidators() {
        return ValidatorUtils.merge(
                    StringValidators.REQUIRE_NON_EMPTY_STRING,
//                  ValidatorUtils.merge(StringValidators.MAY_NOT_START_WITH_DIGIT,
                    ValidatorUtils.merge(StringValidators.NO_WHITESPACE,
                    StringValidators.regexp("[a-zA-Z0-9_\\-.]*[a-zA-Z0-9]{1}", NbBundle.getMessage(MavenValidators.class, "ERR_Coordinate_Invalid"), false)
               ));
    }

    public static Validator<String> createGroupIdValidators() {
        return ValidatorUtils.merge(
                    StringValidators.REQUIRE_NON_EMPTY_STRING,
//                  ValidatorUtils.merge(StringValidators.MAY_NOT_START_WITH_DIGIT,
                    ValidatorUtils.merge(StringValidators.NO_WHITESPACE,
                    StringValidators.regexp("[a-zA-Z0-9_\\-.]*[a-zA-Z0-9]{1}", NbBundle.getMessage(MavenValidators.class, "ERR_Coordinate_Invalid"), false)
               ));
    }

    public static Validator<String> createVersionValidators() {
        return ValidatorUtils.merge(
                    StringValidators.REQUIRE_NON_EMPTY_STRING,
//                  ValidatorUtils.merge(StringValidators.MAY_NOT_START_WITH_DIGIT,
                    ValidatorUtils.merge(StringValidators.NO_WHITESPACE,
                    StringValidators.regexp("[a-zA-Z0-9_\\-.]*[a-zA-Z0-9]{1}", NbBundle.getMessage(MavenValidators.class, "ERR_Coordinate_Invalid"),  false)
               ));
    }
    private MavenValidators() {
    }

}
