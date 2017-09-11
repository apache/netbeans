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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.axi;

import java.io.IOException;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.xml.axi.impl.SchemaGeneratorFactoryImpl;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 *
 * @author Ayub Khan
 */
public abstract class SchemaGeneratorFactory {
    
    public enum TransformHint{
        OK,
        SAME_DESIGN_PATTERN,
        INVALID_SCHEMA,
        NO_GLOBAL_ELEMENTS,
        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS,
        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ATTRIBUTES,
        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS_AND_ATTRIBUTES,
        GLOBAL_ELEMENTS_HAVE_NO_GRAND_CHILDREN,
        NO_ATTRIBUTES,
        WILL_REMOVE_TYPES,
        WILL_REMOVE_GLOBAL_ELEMENTS,
        WILL_REMOVE_GLOBAL_ELEMENTS_AND_TYPES,
        CANNOT_REMOVE_TYPES,
        CANNOT_REMOVE_GLOBAL_ELEMENTS,
        CANNOT_REMOVE_GLOBAL_ELEMENTS_AND_TYPES;
    }
    
    private static SchemaGeneratorFactory instance;
    
    /** Creates a new instance of SchemaGeneratorFactory */
    public static SchemaGeneratorFactory getDefault() {
        if(instance == null)
            instance = new SchemaGeneratorFactoryImpl();
        return instance;
    }
    
    /*
     * infers design pattern
     *
     */
    public abstract SchemaGenerator.Pattern inferDesignPattern(AXIModel am);
    
    /*
     * Updates schema using a a particular design pattern
     *
     */
    public abstract void updateSchema(SchemaModel sm,
            SchemaGenerator.Pattern pattern) throws BadLocationException, IOException;
    
    /*
     * returns list of all master axi global elements
     *
     * @param am - AXIModel
     * @return ges - list of all master axi global elements
     */    
    public abstract List<Element> findMasterGlobalElements(AXIModel am);
    
    /*
     * can transforms schema using a a particular design pattern
     *
     * @param sm - SchemaModel
     * @param currentPattern - if null then checks only if the schema is valid and well-formed for transform
     * @param targetPattern - if null then checks only if the schema is valid and well-formed for transform
     */
    public abstract TransformHint canTransformSchema(SchemaModel sm,
            SchemaGenerator.Pattern currentPattern, SchemaGenerator.Pattern targetPattern);
    
    /*
     * can transforms schema using a a particular design pattern
     *
     * @param sm - SchemaModel
     * @param currentPattern - if null then checks only if the schema is valid and well-formed for transform
     * @param targetPattern - if null then checks only if the schema is valid and well-formed for transform
     * @param ges - list of all master axi global elements
     */
    public abstract TransformHint canTransformSchema(SchemaModel sm,
            SchemaGenerator.Pattern currentPattern, SchemaGenerator.Pattern targetPattern,
            List<Element> ges);
    
    /*
     * transforms schema using a a particular design pattern
     *
     */
    public abstract void transformSchema(SchemaModel sm,
            SchemaGenerator.Pattern targetPattern) throws IOException;
}
