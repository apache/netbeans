/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.model;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;

/**
 * Represents an instance of a FXML bean. 
 *
 * @author sdedic
 */
public final class FxNewInstance extends FxInstance {
    private String          typeName;
    private String          initValue;
    private String          factoryMethod;
    /**
     * true, if the init value is a declared constant (fx:constant)
     */
    private boolean         constantValue;
    
    /**
     * True, if the instance represents a fx:root element - custom root.
     */
    private boolean         customRoot;
    
    public String getTypeName() {
        return typeName;
    }
    
    public FxNewInstance(String sourceName, boolean customRoot) {
        this.typeName = sourceName;
        this.customRoot = customRoot;
        if (customRoot) {
            setSourceName(FxXmlSymbols.FX_ROOT);
        } else {
            setSourceName(sourceName);
        }
    }

    public FxNewInstance(String sourceName) {
        setSourceName(sourceName);
        this.typeName = sourceName;
    }

    @Override
    public Kind getKind() {
        return Kind.Instance;
    }
    
    /**
     * True, if the instance represents a fx:root element
     * @return 
     */
    public boolean isCustomRoot() {
        return customRoot;
    }

    @Override
    public void accept(FxNodeVisitor v) {
        v.visitInstance(this);
    }
    
    FxNewInstance fromValue(CharSequence val) {
        this.initValue = val == null ? null : val.toString();
        return this;
    }
    
    FxNewInstance usingFactory(String factory) {
        this.factoryMethod = factory;
        return this;
    }
    
    void setConstant(boolean constant) {
        this.constantValue = constant;
    }
    
    public boolean isConstant() {
        return constantValue;
    }
    
    void resolveClass(String className, ElementHandle<TypeElement> handle) {
        if (!customRoot) {
            setSourceName(className);
        }
        setJavaType(handle);
    }

    public String getInitValue() {
        return initValue;
    }

    public String getFactoryMethod() {
        return factoryMethod;
    }
    
}

