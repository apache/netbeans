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
package org.netbeans.modules.j2ee.jboss4.config;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Hejl
 */
public class JB7MessageDestinationHandler extends DefaultHandler {

    private final List<JBossMessageDestination> messageDestinations = new ArrayList<JBossMessageDestination>();

    private boolean isDestinations;

    private boolean isDestination;

    private List<String> jndiNames = new ArrayList<String>();

    public List<JBossMessageDestination> getMessageDestinations() {
        return messageDestinations;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("jms-destinations".equals(qName)) {
            isDestinations = true;
        } else if (isDestinations && ("jms-queue".equals(qName) || "jms-topic".equals(qName))) {
            isDestination = true;
        } else if (isDestination && "entry".equals(qName)) {
            jndiNames.add(attributes.getValue("name"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isDestination) {
            if ("jms-queue".equals(qName)) {
                isDestination = false;
                for (String name : jndiNames) {
                    messageDestinations.add(new JBossMessageDestination(name, Type.QUEUE));
                }
                jndiNames.clear();
            } else if ("jms-topic".equals(qName)) {
                isDestination = false;
                for (String name : jndiNames) {
                    messageDestinations.add(new JBossMessageDestination(name, Type.TOPIC));
                }
                jndiNames.clear();
            }    
        } else if (isDestinations) {
            if ("jms-destinations".equals(qName)) {
                jndiNames.clear();
                isDestination = false;
                isDestinations = false;
            }
        }
    }

}
