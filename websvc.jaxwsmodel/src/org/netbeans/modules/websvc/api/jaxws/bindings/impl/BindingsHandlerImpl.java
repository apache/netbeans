/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.websvc.api.jaxws.bindings.impl;

import java.util.Collections;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerName;
import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsHandlerImpl extends BindingsComponentImpl implements BindingsHandler {

    /**
     * Creates a new instance of BindingsHandlerImpl
     */
    public BindingsHandlerImpl(BindingsModelImpl model, Element element) {
        super(model, element);
    }

    public BindingsHandlerImpl(BindingsModelImpl model) {
        this(model, createPrefixedElement(BindingsQName.HANDLER.getQName(), model));
    }

    public void setHandlerClass(BindingsHandlerClass handlerClass) {
        java.util.List<Class<? extends BindingsComponent>> classes = Collections.emptyList();
        setChild(BindingsHandlerClass.class, HANDLER_CLASS_PROPERTY, handlerClass,
                classes);
    }

    public void removeHandlerClass(BindingsHandlerClass handlerClass) {
        removeChild(HANDLER_CLASS_PROPERTY, handlerClass);
    }

    public BindingsHandlerClass getHandlerClass() {
        return getChild(BindingsHandlerClass.class);
    }

    protected String getNamespaceURI() {
        return BindingsQName.JAVAEE_NS_URI;
    }

    public void setHandlerName(BindingsHandlerName handlerName) {
        java.util.List<Class<? extends BindingsComponent>> names = Collections.emptyList();
        setChild(BindingsHandlerName.class, HANDLER_NAME_PROPERTY, handlerName,
                names);
    }

    public BindingsHandlerName getHandlerName() {
        return getChild(BindingsHandlerName.class);
    }
}
