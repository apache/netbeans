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
package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds information about available ActivationConfigProperties of the EJB specifications.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ActivationConfigProperties {

    private static final List<ActivationConfigProperty> ACP_30 = new ArrayList<ActivationConfigProperty>();
    private static final List<ActivationConfigProperty> ACP_32 = new ArrayList<ActivationConfigProperty>();

    public static final String ACKNOWLEDGE_MODE = "acknowledgeMode";                //NOI18N
    public static final String DESTINATION_TYPE = "destinationType";                //NOI18N
    public static final String MESSAGE_SELECTOR = "messageSelector";                //NOI18N
    public static final String SUBSCRIPTION_DURABILITY = "subscriptionDurability";  //NOI18N
    public static final String CLIENT_ID = "clientId";                              //NOI18N
    public static final String CONNECTION_FACTORY_LOOKUP = "connectionFactoryLookup";//NOI18N
    public static final String DESTINATION_LOOKUP = "destinationLookup";            //NOI18N
    public static final String SUBSCRIPTION_NAME = "subscriptionName";                 //NOI18N

    static {
        // since EJB3.0
        ACP_30.add(new ActivationConfigProperty(ACKNOWLEDGE_MODE, AcknowledgeMode.class, EjbVersion.EJB_3_0));
        ACP_30.add(new ActivationConfigProperty(DESTINATION_TYPE, DestinationType.class, EjbVersion.EJB_3_0));
        ACP_30.add(new ActivationConfigProperty(MESSAGE_SELECTOR, String.class, EjbVersion.EJB_3_0));
        ACP_30.add(new ActivationConfigProperty(SUBSCRIPTION_DURABILITY, SubscriptionDurability.class, EjbVersion.EJB_3_0));

        // since EJB3.2
        ACP_32.add(new ActivationConfigProperty(ACKNOWLEDGE_MODE, AcknowledgeMode.class, EjbVersion.EJB_3_2));
        ACP_32.add(new ActivationConfigProperty(CLIENT_ID, String.class, EjbVersion.EJB_3_2));
        ACP_32.add(new ActivationConfigProperty(CONNECTION_FACTORY_LOOKUP, String.class, EjbVersion.EJB_3_2));
        ACP_32.add(new ActivationConfigProperty(DESTINATION_TYPE, DestinationType.class, EjbVersion.EJB_3_2));
        ACP_32.add(new ActivationConfigProperty(DESTINATION_LOOKUP, String.class, EjbVersion.EJB_3_2));
        ACP_32.add(new ActivationConfigProperty(MESSAGE_SELECTOR, String.class, EjbVersion.EJB_3_2));
        ACP_32.add(new ActivationConfigProperty(SUBSCRIPTION_DURABILITY, SubscriptionDurability.class, EjbVersion.EJB_3_2));
        ACP_32.add(new ActivationConfigProperty(SUBSCRIPTION_NAME, String.class, EjbVersion.EJB_3_2));
    }

    protected static List<ActivationConfigProperty> getActivationConfigProperties(EjbVersion forVersion) {
        if (forVersion == EjbVersion.EJB_3_0) {
            return ACP_30;
        } else {
            return ACP_32;
        }
    }

    public static class ActivationConfigProperty {

        private final String name;
        private final Class propertyClass;
        private final EjbVersion version;

        public ActivationConfigProperty(String name, Class propertyClass, EjbVersion version) {
            this.name = name;
            this.propertyClass = propertyClass;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public Class getPropertyClass() {
            return propertyClass;
        }

        public EjbVersion getVersion() {
            return version;
        }

    }

    public static enum AcknowledgeMode {
        // these values actually violates spec, but only these values works with GF
        // should be Auto_acknowledge and Dups_ok_acknowledge
        // see http://docs.oracle.com/javaee/6/api/javax/ejb/ActivationConfigProperty.html
        AUTO_ACKNOWLEDGE("Auto-acknowledge"),          //NOI18N
        DUPS_OK_ACKNOWLEDGE("Dups-ok-acknowledge");    //NOI18N

        private final String value;

        private AcknowledgeMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static enum DestinationType {
        QUEUE("javax.jms.Queue"),   //NOI18N
        TOPIC("javax.jms.Topic");   //NOI18N

        private final String value;

        private DestinationType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static enum SubscriptionDurability {
        NON_DURABLE("NonDurable"), //NOI18N
        DURABLE("Durable");         //NOI18N

        private final String value;

        private SubscriptionDurability(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    protected static enum EjbVersion {
        EJB_3_0,
        EJB_3_2
    }

}
