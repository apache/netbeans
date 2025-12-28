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
package org.netbeans.modules.cloud.oracle.assets;

/**
 * Validates if the implementing class is suitable for adding to {@link CloudAssets}.
 * The validation result is represented by {@link Result}, which includes a {@link ValidationStatus}
 * indicating the outcome of the validation.
 *
 * <p>The possible validation states are:
 * <ul>
 *   <li>{@link ValidationStatus#OK} - Indicates that the item is suitable for adding to {@link CloudAssets}
 *       without any warnings or restrictions.</li>
 *   <li>{@link ValidationStatus#WARNING} - Indicates that the item may be added to {@link CloudAssets},
 *       but with a warning message to notify the user of potential issues.</li>
 *   <li>{@link ValidationStatus#ERROR} - Indicates that the item cannot be added to {@link CloudAssets}.
 *       A warning message will be shown, and the addition process will be blocked.</li>
 * </ul>
 * 
 * @author Jan Horvath
 */
public interface Validator {

    enum ValidationStatus {
        OK, WARNING, ERROR
    };

    public class Result {

        public final ValidationStatus status;
        public final String message;

        public Result(ValidationStatus status, String message) {
            this.status = status;
            this.message = message;
        }

    }

    public Result validate();

}
