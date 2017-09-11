/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.xml.axi;

import java.util.Objects;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor2;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;

/**
 * Represents a xs:include or xs:import declaration, a reference to another
 * XML Schema.
 * @author sdedic
 * @since 1.33
 */
public class SchemaReference extends AXIComponent {
    public static final String PROP_TARGET_NAMESPACE = "targetNamespace";   // NOI18N
    public static final String PROP_SCHEMA_LOCATION = "schemaLocation";   // NOI18N
    
    private final boolean include;
    
    /**
     * Target namespace. Valid only for imports, must be null for imports
     */
    private String  targetNamespace;
    
    /**
     * Optional schema location
     */
    private String  schemaLocation;
    
    public SchemaReference(AXIModel model, SchemaModelReference schemaComponent) {
        super(model, schemaComponent);
        this.include = schemaComponent instanceof Include;
    }

    public SchemaReference(AXIModel model, boolean include) {
        super(model);
        this.include = include;
    }

    public SchemaReference(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
        assert sharedComponent instanceof SchemaReference;
        include = ((SchemaReference)sharedComponent).isInclude();
    }
    
    public boolean isInclude() {
        return include;
    }
    
    public boolean isImport() {
        return !include;
    }

    @Override
    public void accept(AXIVisitor visitor) {
        if (visitor instanceof AXIVisitor2) {
            ((AXIVisitor2)visitor).visit(this);
        }
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setTargetNamespace(String targetNamespace) {
        if (isInclude() && targetNamespace != null) {
            throw new IllegalArgumentException("xs:include does not support targetNamespace");
        }
        String old = getTargetNamespace();
        if (Objects.equals(old, targetNamespace)) {
            return;
        }
        this.targetNamespace = targetNamespace;
        firePropertyChange(PROP_TARGET_NAMESPACE, old, targetNamespace);
    }

    public void setSchemaLocation(String schemaLocation) {
        String old = getSchemaLocation();
        if (Objects.equals(old, schemaLocation)) {
            return;
        }
        this.schemaLocation = schemaLocation;
        firePropertyChange(PROP_SCHEMA_LOCATION, old, targetNamespace);
    }
}
