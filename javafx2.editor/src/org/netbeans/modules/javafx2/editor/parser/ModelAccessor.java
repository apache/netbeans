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
package org.netbeans.modules.javafx2.editor.parser;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinition;
import org.netbeans.modules.javafx2.editor.completion.model.EventHandler;
import org.netbeans.modules.javafx2.editor.completion.model.FxInclude;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.FxObjectBase;
import org.netbeans.modules.javafx2.editor.completion.model.FxScriptFragment;
import org.netbeans.modules.javafx2.editor.completion.model.HasContent;
import org.netbeans.modules.javafx2.editor.completion.model.HasResource;
import org.netbeans.modules.javafx2.editor.completion.model.ImportDecl;
import org.netbeans.modules.javafx2.editor.completion.model.LanguageDecl;
import org.netbeans.modules.javafx2.editor.completion.model.MapProperty;
import org.netbeans.modules.javafx2.editor.completion.model.PropertySetter;
import org.netbeans.modules.javafx2.editor.completion.model.PropertyValue;
import org.netbeans.modules.javafx2.editor.completion.model.StaticProperty;

/**
 * Provides extra access to model properties, so partial instances
 * can be built and attributed.
 *
 * @author sdedic
 */
public abstract class ModelAccessor {
    static ModelAccessor INSTANCE;
    
    static {
        try {
            Class.forName(FxModel.class.getName());
        } catch (ClassNotFoundException ex) {
             throw new IllegalStateException(ex);
        }
    }
    
    public static void setInstance(ModelAccessor i) {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = i;
    }
    
    /**
     * Creates new model. Note that the parameters are live and
     * <b>may be changed</b> by the caller.
     * 
     * @param imports
     * @param defs
     * @return 
     */
    public abstract FxModel    newModel(URL baseURL, List<ImportDecl> imports, List<FxNewInstance> defs);
    public abstract ImportDecl createImport(String imported, boolean wildcard);
    public abstract LanguageDecl createLanguage(String lang);
    public abstract FxInclude createInclude(String included, String id);
    public abstract FxNewInstance createInstance(String sourceName, CharSequence value, boolean constant, String factory, String id);
    public abstract FxNewInstance createCustomRoot(String sourceName, String id);
    public abstract FxObjectBase createCopyReference(boolean copy, String targetName);
    public abstract PropertySetter createProperty(String name, boolean implicit);
    public abstract StaticProperty createStaticProperty(String name, String sourceName);
    public abstract MapProperty createMapProperty(String name, Map<String, CharSequence> values);
    
    public abstract EventHandler createEventHandler(String eventName);
    
    public abstract FxNode createElement(String localName);
    public abstract FxNode createErrorElement(String localName);
    public abstract FxScriptFragment createScript(String sourceRef);
    public abstract EventHandler asMethodRef(EventHandler h);
    
    public abstract void initModel(FxModel model, String controller, FxInstance rootInstance, LanguageDecl language);
    /**
     * Resolves class name for instance nodes, accessor name for property and event nodes
     * @param n
     * @param handle 
     */
    @SuppressWarnings("rawtypes")
    public abstract void resolve(FxNode n, ElementHandle nameHandle, TypeMirrorHandle typeHandle, 
            ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info);
    
    public abstract void addContent(HasContent content, CharSequence additionalContent);
    
    /**
     * Adds child to the parent, based on the parent/child type. May throw
     * {@link IllegalArgumentException} if the child is not appropriate for the parent.
     * 
     * @param parent
     * @param child 
     */
    public abstract void addChild(FxNode parent, FxNode child) throws IllegalArgumentException;
    
    public abstract void resolveResource(HasResource decl, URL resolved);
    
    public abstract NodeInfo i(FxNode n);
    public abstract <T extends FxNode> T makeBroken(T n);
    
    public abstract void addDefinitions(FxModel model, Collection<FxNewInstance> definitions);
    
    public abstract void setNamedInstances(FxModel model, Map<String, FxInstance> instances);
    
    public abstract void resolveReference(FxObjectBase copyOrReference, FxInstance original);
    
    public abstract void attach(FxNode node, FxModel model);

    public abstract void rename(FxInstance instance, PropertyValue pv, String newName);
}
