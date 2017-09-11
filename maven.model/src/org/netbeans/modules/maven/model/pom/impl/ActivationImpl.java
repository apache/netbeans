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
package org.netbeans.modules.maven.model.pom.impl;

import java.util.*;	
import org.netbeans.modules.maven.model.pom.*;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ActivationImpl extends POMComponentImpl implements Activation {

    public ActivationImpl(POMModel model, Element element) {
        super(model, element);
    }
    
    public ActivationImpl(POMModel model) {
        this(model, createElementNS(model, model.getPOMQNames().ACTIVATION));
    }

    // attributes

    // child elements
    @Override
    public ActivationOS getActivationOS() {
        return getChild(ActivationOS.class);
    }

    @Override
    public void setActivationOS(ActivationOS activationOS) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(ActivationOS.class, getModel().getPOMQNames().ACTIVATIONOS.getName(), activationOS, empty);
    }

    @Override
    public ActivationProperty getActivationProperty() {
        return getChild(ActivationProperty.class);
    }

    @Override
    public void setActivationProperty(ActivationProperty activationProperty) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(ActivationProperty.class, getModel().getPOMQNames().ACTIVATIONPROPERTY.getName(), activationProperty, empty);
    }

    @Override
    public ActivationFile getActivationFile() {
        return getChild(ActivationFile.class);
    }

    @Override
    public void setActivationFile(ActivationFile activationFile) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(ActivationFile.class, getModel().getPOMQNames().ACTIVATIONFILE.getName(), activationFile, empty);
    }

    @Override
    public ActivationCustom getActivationCustom() {
        return getChild(ActivationCustom.class);
    }

    @Override
    public void setActivationCustom(ActivationCustom activationCustom) {
        List<Class<? extends POMComponent>> empty = Collections.emptyList();
        setChild(ActivationCustom.class, getModel().getPOMQNames().ACTIVATIONCUSTOM.getName(), activationCustom, empty);
    }

    @Override
    public void accept(POMComponentVisitor visitor) {
        visitor.visit(this);
    }

}
