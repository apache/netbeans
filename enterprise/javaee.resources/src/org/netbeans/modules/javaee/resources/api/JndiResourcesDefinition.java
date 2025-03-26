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
package org.netbeans.modules.javaee.resources.api;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JndiResourcesDefinition {

    private JndiResourcesDefinition() {
    }

    public static final String ANN_DATA_SOURCE = "javax.annotation.sql.DataSourceDefinition"; //NOI18N
    public static final String ANN_JMS_CONNECTION_FACTORY = "javax.jms.JMSConnectionFactoryDefinition"; //NOI18N
    public static final String ANN_JMS_DESTINATION = "javax.jms.JMSDestinationDefinition"; //NOI18N
    public static final String ANN_JMS_DESTINATIONS = "javax.jms.JMSDestinationDefinitions"; //NOI18N
    //public static final String ANN_MAIL_SESSION = ".MailSessionDefinition"; //NOI18N
    public static final String ANN_CONNECTION_RESOURCE = "javax.resource.ConnectorResourceDefinition"; //NOI18N
    public static final String ANN_ADMINISTRED_OBJECT = "javax.resource.AdministeredObjectDefinition"; //NOI18N
    public static final String ANN_JMS_DESTINATION_JAKARTA = "jakarta.jms.JMSDestinationDefinition"; //NOI18N
    public static final String ANN_JMS_DESTINATIONS_JAKARTA = "jakarta.jms.JMSDestinationDefinitions"; //NOI18N

}
