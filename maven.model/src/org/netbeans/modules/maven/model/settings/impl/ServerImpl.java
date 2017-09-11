/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.settings.impl;

import org.netbeans.modules.maven.model.settings.Configuration;
import org.netbeans.modules.maven.model.settings.Server;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ServerImpl extends SettingsComponentImpl implements Server {

    private static final Class<? extends SettingsComponent>[] ORDER = new Class[] {
        Configuration.class
    };

    public ServerImpl(SettingsModel model, Element element) {
        super(model, element);
    }
    
    public ServerImpl(SettingsModel model) {
        this(model, createElementNS(model, model.getSettingsQNames().MIRROR));
    }

    // attributes

    // child elements


    @Override
    public void accept(SettingsComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getId() {
        return getChildElementText(getModel().getSettingsQNames().ID.getQName());
    }

    @Override
    public void setId(String id) {
        setChildElementText(getModel().getSettingsQNames().ID.getName(), id,
                getModel().getSettingsQNames().ID.getQName());
    }

    @Override
    public String getUsername() {
        return getChildElementText(getModel().getSettingsQNames().USERNAME.getQName());
    }

    @Override
    public void setUsername(String username) {
        setChildElementText(getModel().getSettingsQNames().USERNAME.getName(), username,
                getModel().getSettingsQNames().USERNAME.getQName());
    }

    @Override
    public String getPassphrase() {
        return getChildElementText(getModel().getSettingsQNames().PASSPHRASE.getQName());
    }

    @Override
    public void setPassphrase(String passphrase) {
        setChildElementText(getModel().getSettingsQNames().PASSPHRASE.getName(), passphrase,
                getModel().getSettingsQNames().PASSPHRASE.getQName());
    }

    @Override
    public String getPrivateKey() {
        return getChildElementText(getModel().getSettingsQNames().PRIVATEKEY.getQName());
    }

    @Override
    public void setPrivateKey(String key) {
        setChildElementText(getModel().getSettingsQNames().PRIVATEKEY.getName(), key,
                getModel().getSettingsQNames().PRIVATEKEY.getQName());
    }


    @Override
    public Configuration getConfiguration() {
        return getChild(Configuration.class);
    }

    @Override
    public void setConfiguration(Configuration config) {
        setChild(Configuration.class, getModel().getSettingsQNames().CONFIGURATION.getName(), config,
                getClassesBefore(ORDER, Configuration.class));
    }


    public static class List extends ListImpl<Server> {
        public List(SettingsModel model, Element element) {
            super(model, element, model.getSettingsQNames().SERVER, Server.class);
        }

        public List(SettingsModel model) {
            this(model, createElementNS(model, model.getSettingsQNames().SERVERS));
        }
    }


}
