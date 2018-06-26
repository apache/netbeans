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
