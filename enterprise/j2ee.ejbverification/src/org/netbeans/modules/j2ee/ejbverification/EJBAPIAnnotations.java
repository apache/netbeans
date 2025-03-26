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

package org.netbeans.modules.j2ee.ejbverification;

/**
 * This class defines constants that represent various annotation type names
 * defined in EJB specification.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface EJBAPIAnnotations {
    String ASYNCHRONOUS = "javax.ejb.Asynchronous"; //NOI18N
    String ASYNCHRONOUS_JAKARTA = "jakarta.ejb.Asynchronous"; //NOI18N

    String REMOTE = "javax.ejb.Remote"; //NOI18N
    String REMOTE_JAKARTA = "jakarta.ejb.Remote"; //NOI18N
    String LOCAL = "javax.ejb.Local"; //NOI18N
    String LOCAL_JAKARTA = "jakarta.ejb.Local"; //NOI18N

    String STATELESS = "javax.ejb.Stateless"; // NOI18N
    String STATELESS_JAKARTA = "jakarta.ejb.Stateless"; // NOI18N

    String STATEFUL = "javax.ejb.Stateful"; // NOI18N
    String STATEFUL_JAKARTA = "jakarta.ejb.Stateful"; // NOI18N
    String INIT = "javax.ejb.Init"; // NOI18N
    String INIT_JAKARTA = "jakarta.ejb.Init"; // NOI18N
    String REMOVE = "javax.ejb.Remove"; // NOI18N
    String REMOVE_JAKARTA = "jakarta.ejb.Remove"; // NOI18N

    String MESSAGE_DRIVEN = "javax.ejb.MessageDriven"; // NOI18N
    String MESSAGE_DRIVEN_JAKARTA = "jakarta.ejb.MessageDriven"; // NOI18N
    String ACTIVATION_CONFIG_PROPERTY = "javax.ejb.ActivationConfigProperty"; // NOI18N
    String ACTIVATION_CONFIG_PROPERTY_JAKARTA = "javaxjakartaejb.ActivationConfigProperty"; // NOI18N

    String REMOTE_HOME = "javax.ejb.RemoteHome"; //NOI18N
    String REMOTE_HOME_JAKARTA = "jakarta.ejb.RemoteHome"; //NOI18N
    String LOCAL_HOME = "javax.ejb.LocalHome"; //NOI18N
    String LOCAL_HOME_JAKARTA = "jakarta.ejb.LocalHome"; //NOI18N

    String TRANSACTION_MANAGEMENT = "javax.ejb.TransactionManagement"; //NOI18N
    String TRANSACTION_MANAGEMENT_JAKARTA = "jakarta.ejb.TransactionManagement"; //NOI18N

    //value attribute in annotations with single attribute
    String VALUE = "value"; //NOI18N

    String WEB_SERVICE = "javax.jws.WebService"; //NOI18N
    String WEB_SERVICE_JAKARTA = "jakarta.jws.WebService"; //NOI18N
    // TODO: Add other ones here including enum types
    String LOCAL_BEAN = "javax.ejb.LocalBean";
    String LOCAL_BEAN_JAKARTA = "jakarta.ejb.LocalBean";

    String POST_CONSTRUCT = "javax.annotation.PostConstruct";
    String POST_CONSTRUCT_JAKARTA = "jakarta.annotation.PostConstruct";
    String AROUND_INVOKE = "javax.interceptor.AroundInvoke";
    String AROUND_INVOKE_JAKARTA = "jakarta.interceptor.AroundInvoke";

    String SCHEDULE = "javax.ejb.Schedule"; //NOI18N
    String SCHEDULE_JAKARTA = "jakarta.ejb.Schedule"; //NOI18N
    // @Schedule parameter for persistent timer
    String PERSISTENT = "persistent"; //NOI18N

    String SESSION_SYNCHRONIZATION = "javax.ejb.SessionSynchronization"; //NOI18N
    String SESSION_SYNCHRONIZATION_JAKARTA = "jakarta.ejb.SessionSynchronization"; //NOI18N
}
