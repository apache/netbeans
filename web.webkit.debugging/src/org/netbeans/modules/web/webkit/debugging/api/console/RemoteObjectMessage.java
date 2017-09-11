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
package org.netbeans.modules.web.webkit.debugging.api.console;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.PropertyDescriptor;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;

/**
 *
 * @author Martin
 */
class RemoteObjectMessage extends ConsoleMessage {
    
    private final RemoteObject ro;
    private final WebKitDebugging webKit;
    
    RemoteObjectMessage(WebKitDebugging webKit, RemoteObject ro) {
        super(ro.getOwningProperty());
        this.webKit = webKit;
        this.ro = ro;
    }

    @Override
    public String getType() {
        return ro.getType().getName();
    }

    @Override
    public String getText() {
        if (ro.getType() == RemoteObject.Type.OBJECT) {
            String className = ro.getClassName();
            if (className != null &&
                className.startsWith("HTML") && className.endsWith("Element")) {
                
                List<PropertyDescriptor> properties = webKit.getRuntime().getRemoteObjectProperties(ro, true);
                for (PropertyDescriptor pd : properties) {
                    if ("outerHTML".equals(pd.getName())) {
                        return pd.getValue().getValueAsString();
                    }
                }
            }
            return ro.getDescription();
        }
        return ro.getValueAsString();
    }

    @Override
    public List<ConsoleMessage> getSubMessages() {
        if (ro.getType() == RemoteObject.Type.OBJECT) {
            List<PropertyDescriptor> properties = ro.getProperties();
            List<ConsoleMessage> propMessages = new ArrayList<ConsoleMessage>(properties.size());
            for (PropertyDescriptor pd : properties) {
                // #229457 - prevent NPE
                if (pd.getValue() != null) {
                    propMessages.add(new PropertyMessage(webKit, pd));
                }
            }
            return propMessages;
        } else {
            return super.getSubMessages();
        }
    }
    
}
