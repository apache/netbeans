/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.jsf.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.el.CompilationContext;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELParserResult;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.netbeans.modules.web.el.spi.ELPlugin;
import org.netbeans.modules.web.el.spi.Function;
import org.netbeans.modules.web.el.spi.ImplicitObject;
import org.netbeans.modules.web.el.spi.ImplicitObjectType;
import org.netbeans.modules.web.el.spi.ResourceBundle;
import static org.netbeans.modules.web.el.spi.ImplicitObjectType.*;
import org.netbeans.modules.web.el.spi.ResolverContext;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.spi.JsfSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = ELPlugin.class)
public class JspJsfELPlugin extends ELPlugin {

    private static final Logger LOGGER = Logger.getLogger(JspJsfELPlugin.class.getName());
    
    private static final String PLUGIN_NAME = "JSP JSF EL Plugin"; //NOI18N
    private static final String VOID_RETURN_TYPE = "void";
    private Collection<String> MIMETYPES = Arrays.asList(new String[]{"text/x-jsp", "text/x-tag"});
    private Collection<ImplicitObject> implicitObjects;
    private Collection<ImplicitObject> implicitObjectsJakarta;

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public Collection<String> getMimeTypes() {
        return MIMETYPES;
    }

    @Override
    public Collection<ImplicitObject> getImplicitObjects(FileObject file) {
        if (!MIMETYPES.contains(file.getMIMEType())) {
            return Collections.<ImplicitObject>emptyList();
        }
        final Project project = FileOwnerQuery.getOwner(file);
        if (project == null || JsfVersionUtils.forProject(project).isAtLeast(JsfVersion.JSF_3_0)) {
            return getImplicitObjectsJakarta();
        } else {
            return getImplicitObjects();
        }
    }

    @Override
    public List<ResourceBundle> getResourceBundles(FileObject file, ResolverContext context) {
        return Collections.emptyList();
    }

    @Override
    public boolean isValidProperty(ExecutableElement executableElement, Source source, CodeCompletionContext completionContext, CompilationContext compilationContext) {
        if (executableElement == null || source == null || completionContext == null || 
                compilationContext == null || executableElement.getReturnType() == null) {
            return false;
        }

        Attribute attribute = getAttributeOnCaret(source, completionContext);
        if (attribute == null) {
            return false;
        }
        
        TypeMirror elementReturnType = executableElement.getReturnType();
        Types types = compilationContext.info().getTypes();
        
        String methodSignature = attribute.getMethodSignature();

        // When no signature was found in Tag library.
        if (methodSignature == null || methodSignature.isEmpty()) {

            String attributeReturnType = attribute.getType();
            if (attributeReturnType == null) {
                return false;
            }
            
            TypeElement attributeReturnTypeElement = ELTypeUtilities.getElementForType(compilationContext, attributeReturnType);
            if (attributeReturnTypeElement == null) {
                return false;
            }
            
            // method has not parameters and return types are assignable.
            if ( executableElement.getParameters().isEmpty() && 
                    ( (TypeKind.VOID == elementReturnType.getKind() && VOID_RETURN_TYPE.equals(attributeReturnType)) ||
                    (types.isAssignable(elementReturnType, attributeReturnTypeElement.asType())) ) ) {
                return true;
            }
            return false;
        }
        
        methodSignature = methodSignature.trim();
        
        // Get Classes of parameters from attribute method signature.
        String attributeName = attribute.getName();
        int atributeNameIndex = methodSignature.indexOf(attributeName);
        if (atributeNameIndex == -1) {
            LOGGER.log(Level.FINE, "Attribute not found in the method signature: element={0},attribute={1}",
                    new Object[]{executableElement.getSimpleName(), attributeName});
            return false;
        }
        String attributeReturnType = methodSignature.substring(0, atributeNameIndex).trim();
        String attributeParametersStr = methodSignature.substring(atributeNameIndex + attributeName.length()).replaceAll("[()]", "").trim();

        List<String> attributeParameters = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(attributeParametersStr, ",");
        while (tokenizer.hasMoreTokens()) {
            String parameterClass = tokenizer.nextToken();
            if (parameterClass != null) {
                attributeParameters.add(parameterClass);
            }
        }
                    
        boolean signatureEquals = true;
        
        // Now compare each type from executableElement and attribute.
        List<? extends VariableElement> elementParameters = executableElement.getParameters();
        if (elementParameters == null || elementParameters.size() != attributeParameters.size()) {
            return false;
        }
        
        for (int i = 0; i < elementParameters.size(); i++) {
            VariableElement variableElementParameter = elementParameters.get(i);
            
            String attributeParameterClass = attributeParameters.get(i);
            TypeElement attributeParameterClassTypeElement = ELTypeUtilities.getElementForType(compilationContext, attributeParameterClass);

            if (variableElementParameter == null || 
                    variableElementParameter.asType() == null || 
                    attributeParameterClassTypeElement == null || 
                    attributeParameterClassTypeElement.asType() == null) {
                // Error getting informations about parameters. Can not continue.
                return false;
            }
            
            if (!types.isSameType(variableElementParameter.asType(), attributeParameterClassTypeElement.asType())) {
                signatureEquals = false;
                break;
            }
            signatureEquals = true;
        }

        // Signatures are not equal
        if (!signatureEquals) {
            return false;
        }

        // Return types of the element and attribute are void
        if (TypeKind.VOID == elementReturnType.getKind() && VOID_RETURN_TYPE.equals(attributeReturnType)) {
            return true;
        }

        // Attribute return the same type as the element
        TypeElement elementForReturnType = ELTypeUtilities.getElementForType(compilationContext, attributeReturnType);
        if (elementForReturnType != null && (types.isSameType(elementReturnType, elementForReturnType.asType()))) {
            return true;
        }

        return false;
    }

    public static Attribute getAttributeOnCaret(Source source, CodeCompletionContext completionContext) {
        if (source == null || completionContext == null) {
            return null;
        }
        
        final ParserResult parserResult = completionContext.getParserResult();
        if (!(parserResult instanceof ELParserResult)) {
            return null;
        }
        
        ELParserResult elParserResult = (ELParserResult) parserResult;
        final Parser.Result[] results = new Parser.Result[1];
                            
        try {
            ParserManager.parse(Collections.singleton(parserResult.getSnapshot().getSource()), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    results[0] = resultIterator.getParserResult(parserResult.getSnapshot().getOriginalOffset(0) - 2);
                }

            });
            
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "Error parsing Source with mimeType=" + source.getMimeType(), ex);
            return null;
        }
        
        Parser.Result result = results[0];
        if (!(result instanceof HtmlParserResult)) {
            // unexpected instance of Result
            return null;
        }
        
        HtmlParserResult htmlParserResult = (HtmlParserResult) result;
        Snapshot htmlParserSnapshot = htmlParserResult.getSnapshot();

        int caretOffset = completionContext.getCaretOffset();
        ELElement elementAtCaret = elParserResult.getElementAt(caretOffset);
        
        if (elementAtCaret == null) {
            return null;
        }
        
        int elementAtCaretOriginalOffset = elementAtCaret.getSnapshot().getOriginalOffset(elementAtCaret.getEmbeddedOffset().getStart());
        int elementAtCaretEmbeddedOffset = htmlParserSnapshot.getEmbeddedOffset(elementAtCaretOriginalOffset);
        CharSequence htmlParserResultText = htmlParserSnapshot.getText();

        Element element = htmlParserResult.findByPhysicalRange(elementAtCaretEmbeddedOffset, true);
        if (!(element instanceof OpenTag)) {
            return null;
        }
        
        OpenTag openTag = (OpenTag) element;

        String namespacePrefix = openTag.namespacePrefix() == null ? null : openTag.namespacePrefix().toString();
        Map<String, String> namespaces = htmlParserResult.getNamespaces();
        String namespace = null;

        if (namespaces != null && namespacePrefix != null && !namespacePrefix.isEmpty()) {
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (namespacePrefix.equalsIgnoreCase(entry.getValue())) {
                    namespace = entry.getKey();
                }
            }
        }

        if (namespace == null) {
            // No namespace was found.
            LOGGER.log(Level.FINE, "JspJsfELPlugin: No namespace found for prefix: {0}", namespacePrefix);
            return null;
        }

        
        String textToCaret = htmlParserResultText.subSequence(0, elementAtCaretEmbeddedOffset).toString();
        int spaceLastIndex = textToCaret.lastIndexOf(" ");
        String attributeName = textToCaret.substring(spaceLastIndex).replaceAll("[^A-Za-z0-9 ]", "").trim();
        
        JsfSupport jsfSupport = JsfSupportProvider.get(source);
        if (jsfSupport == null) {
            return null;
        }

        Library library = jsfSupport.getLibrary(namespace);
        if (library == null) {
            return null;
        }

        String tagName = openTag.unqualifiedName().toString();
        LibraryComponent component = library.getComponent(tagName);
        if (component == null) {
            // Library component not found
            LOGGER.log(Level.FINE, "JspJsfELPlugin: Library component not found for tag name: {0}", tagName);
            return null;
        }

        Tag tag = component.getTag();
        if (tag == null) {
            // Tag not found
            LOGGER.log(Level.FINE, "JspJsfELPlugin: Tag not found for component: {0}", component.getName());
            return null;
        }
        Attribute attribute = tag.getAttribute(attributeName);
        
        return attribute;
    }
    
    static class FacesContextObject extends JsfImplicitObject {

        public FacesContextObject(String name) {
            super("facesContext", name, OBJECT_TYPE); //NOI18N
        }
    }

    static class ViewObject extends JsfImplicitObject {

        public ViewObject(String name) {
            super("view", name, OBJECT_TYPE); //NOI18N
        }
    }

    private synchronized Collection<ImplicitObject> getImplicitObjects() {
        if (implicitObjects == null) {
            initImplicitObjects();
        }
        return implicitObjects;
    }

    private synchronized void initImplicitObjects() {
        implicitObjects = new ArrayList<ImplicitObject>(2);
//        implicitObjects.addAll(getScopeObjects());
        implicitObjects.add(new ViewObject("javax.faces.component.UIViewRoot"));
        implicitObjects.add(new FacesContextObject("javax.faces.context.FacesContext"));
    }

    private synchronized Collection<ImplicitObject> getImplicitObjectsJakarta() {
        if (implicitObjectsJakarta == null) {
            initImplicitObjectsJakarta();
        }
        return implicitObjectsJakarta;
    }

    private synchronized void initImplicitObjectsJakarta() {
        implicitObjectsJakarta = new ArrayList<ImplicitObject>(2);
//        implicitObjects.addAll(getScopeObjects());
        implicitObjectsJakarta.add(new ViewObject("jakarta.faces.component.UIViewRoot"));
        implicitObjectsJakarta.add(new FacesContextObject("jakarta.faces.context.FacesContext"));
    }

    @Override
    public List<Function> getFunctions(FileObject file) {
        return Collections.emptyList();
    }

//    /**
//     * @return the implicit scope objects, i.e. {@code requestScope, sessionScope} etc.
//     */
//    private static Collection<ELImplicitObject> getScopeObjects() {
//        Collection<ELImplicitObject> result = new ArrayList<ELImplicitObject>(4);
//        result.add(new ELImplicitObject("pageScope")); // NOI18N
//        result.add(new ELImplicitObject("sessionScope")); // NOI18N
//        result.add(new ELImplicitObject("applicationScope")); // NOI18N
//        result.add(new ELImplicitObject("requestScope"));
//        for (ELImplicitObject each : result) {
//            each.setType(ImplicitObjectType.SCOPE_TYPE);
//        }
//        return result;
//
//    }

    private static class JsfImplicitObject implements ImplicitObject {

        private String name, clazz;
        private ImplicitObjectType type;

        public JsfImplicitObject(String name, String clazz, ImplicitObjectType type) {
            this.name = name;
            this.clazz = clazz;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ImplicitObjectType getType() {
            return type;
        }

        @Override
        public String getClazz() {
            return clazz;
        }
    }
}
