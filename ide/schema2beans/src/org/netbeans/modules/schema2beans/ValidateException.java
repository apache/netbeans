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

package org.netbeans.modules.schema2beans;

public class ValidateException extends Exception {
    protected Object failedBean;
    protected String failedPropertyName;
    protected FailureType failureType;

    public ValidateException(String msg, String failedPropertyName, Object failedBean) {
        super(msg);
        this.failedBean = failedBean;
        this.failedPropertyName = failedPropertyName;
    }

    public ValidateException(String msg, FailureType ft,
                             String failedPropertyName, Object failedBean) {
        super(msg);
        this.failureType = ft;
        this.failedBean = failedBean;
        this.failedPropertyName = failedPropertyName;
    }

    public String getFailedPropertyName() {return failedPropertyName;}
    public Object getFailedBean() {return failedBean;}
    public FailureType getFailureType() {return failureType;}

    public static class FailureType {
        private final String name;

        private FailureType(String name) {this.name = name;}

        public String toString() { return name;}

        public static final FailureType NULL_VALUE = new FailureType("NULL_VALUE");
        public static final FailureType DATA_RESTRICTION = new FailureType("DATA_RESTRICTION");
        public static final FailureType ENUM_RESTRICTION = new FailureType("ENUM_RESTRICTION");
        public static final FailureType ALL_RESTRICTIONS = new FailureType("ALL_RESTRICTIONS");
        public static final FailureType MUTUALLY_EXCLUSIVE = new FailureType("MUTUALLY_EXCLUSIVE");
    }
    
}

