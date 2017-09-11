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
package org.netbeans.modules.maven.model.settings.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.impl.SettingsComponentImpl;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class SettingsExtensibilityElementBase extends SettingsComponentImpl
        implements SettingsExtensibilityElement {
    
    public SettingsExtensibilityElementBase(SettingsModel model, Element e) {
        super(model, e);
    }

    public SettingsExtensibilityElementBase(SettingsModel model, QName name) {
        this(model, createElementNS(model, name));
    }
     
    @Override
    public void accept(SettingsComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getElementText() {
        return getText();
    }

    @Override
    public void setElementText(String text) {
        setText(getQName().getLocalPart(), text);
    }

    public static class StringAttribute implements Attribute {
        private String name;
        public StringAttribute(String name) { this.name = name; }
        @Override
        public Class getType() { return String.class; }
        @Override
        public String getName() { return name; }
        @Override
        public Class getMemberType() { return null; }
    }
    
    @Override
    public String getAttribute(String attribute) {
        return getAttribute(new StringAttribute(attribute));
    }
    
    @Override
    public void setAttribute(String attribute, String value) {
        setAttribute(attribute, new StringAttribute(attribute), value);
    }
    
    @Override
    public String getContentFragment() {
        return super.getXmlFragment();
    }
    
    @Override
    public void setContentFragment(String text) throws IOException {
        super.setXmlFragment(CONTENT_FRAGMENT_PROPERTY, text);
    }

    @Override
    public void addAnyElement(SettingsExtensibilityElement anyElement, int index) {
        List<SettingsComponent> all = getChildren();
        if (index > all.size() || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        insertAtIndex(EXTENSIBILITY_ELEMENT_PROPERTY, anyElement, index);
    }

    @Override
    public void removeAnyElement(SettingsExtensibilityElement any) {
        super.removeExtensibilityElement(any);
    }

    @Override
    public List<SettingsExtensibilityElement> getAnyElements() {
        List<SettingsExtensibilityElement> result = new ArrayList<SettingsExtensibilityElement>();
        List<SettingsExtensibilityElement> allEEs = super.getExtensibilityElements();
        for (SettingsExtensibilityElement ee : allEEs) {
            if (! ee.getModel().getQNames().contains(ee.getQName())) {
                result.add(ee);
            }
        }
        return Collections.unmodifiableList(result);
    }   
}
