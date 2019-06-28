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
package org.netbeans.modules.payara.tooling;

/**
 * Names of objects existing in the testing domain.
 *
 * @author Peter Benedikovic
 */
public class TestDomainV4Constants {

    public static final String NODE_NAME = "localhost-test-domain";

    public static final String STANDALONE_INSTANCE = "test-instance";
    public static final String CLUSTER = "test-cluster";
    public static final String CLUSTER_INSTANCE = "test-cluster-instance";

    public static final String APPLICATION = "html5";

    public static final String JMS_CONNECTION_POOL = "jms/testConnectionPool";
    public static final String JMS_CONNECTION_FACTORY = "jms/testConnectionFactory";
    public static final String JDBC_CONNECTION_POOL = "TestPool";
    public static final String JDBC_CONNECTION = "jdbc/testConnection";
}
