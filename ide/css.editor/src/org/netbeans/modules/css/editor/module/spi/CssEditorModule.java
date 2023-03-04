/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.editor.module.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 * The basic class clients wanting to extend the CSS editor functionality needs
 * to implement and register.
 * 
 * An implementation of this class needs to be registered in the default lookup 
 * (use @ServiceProvider(service = CssModule.class) annotation preferably).
 * 
 * This class allows to provide:
 * - pseudo classes and pseudo elements into the completion,
 * - list of css properties. For each property a simple grammar can be 
 *   provided which is then used for the property values checking and for the 
 *   code completion,
 * - advanced code completion
 * - semantic highlighting
 * - mark occurrences 
 * - folds
 * - navigator items
 * - hyperlinking
 * 
 * @todo Pending issues:
 * - the pseudo classes and elements needs to be context aware. Currently if one 
 *   provides a pseudo class/element, the completion offers it in all context where
 *   a pseudo class or element is allowed. So it is not *easily* possible to offer 
 *   them only for selected elements or scopes.
 * - the same as the item above is valid also for the properties
 * - clients needs to have a way to provide property acceptors
 * 
 * @author mfukala@netbeans.org
 */
public abstract class CssEditorModule {
    
 
    /**
     * Returns a list of css pseudo classes for the given context
     * @param context instance of {@link EditorFeatureContext}
     * @return list of css pseudo classes
     */
    public Collection<String> getPseudoClasses(EditorFeatureContext context) {
        return null;
    }
    
    /**
     * Returns a list of css pseudo elements for the given context
     * @param context instance of {@link EditorFeatureContext}
     * @return list of css pseudo elements
     */
    public Collection<String> getPseudoElements(EditorFeatureContext context) {
        return null;
    }
    
    public PropertySupportResolver.Factory getPropertySupportResolverFactory() {
        return null;
    }

    /**
     * Gets a collection of property names which are applicable in the given context.
     * 
     * <b>Rules:</b> (provider == implementor of the {@link CssEditorModule})
     * 
     * 1. provider can define new visible and invisible properties
     * 
     * 2. the property definitions can depend on property definitions provided by
     *    other provider. Care must be taken here as generally various properties
     *    can be available for different contexts. It is highly recommended to depend
     *    only on the "core css" properties as these context free properties are
     *    always available.
     * 
     * 3. provider can NOT override existing properties provided by the other providers
     *    (in fact such duplicity may happen, warning will be logged, and the first 
     *    property definition from the provider with highest priority will be used)
     * 
     *    Note: possibly if one really needs it we can add such support at least
     *          for  visible properties which no one depends on. If this was 
     *          supported for all properties the caching would be much more complicated.
     * 
     * 4. if a provider returns a property name for the given context file,
     *    it is mandatory that subsequent call to getPropertyDefinition() 
     *    must return non-null value.
     * 
     * 5. <b>the provider itself is responsible for caching of the returned property definitions!</b>
     * 
     * 6. the behavior if the context is null is undefined as this typically won't happen
     * 
     * @param file context file, may be null!
     */
    public Collection<String> getPropertyNames(FileObject file) {
        return Collections.emptyList();
    }
    
     /**
     * Gets an instance of {@link PropertyDefinition} for the give property name.
     * 
     * The module must return a non-null instance only if it also returns the property name
     * in {@link #getPropertyNames(org.openide.filesystems.FileObject)!
     * 
     * @param propertyName name of the property
     */
    public PropertyDefinition getPropertyDefinition(String propertyName) {
        return null;
    }
    
    /**
     * Returns a list of {@link HelpResolver} for the given file context.
     * 
     * @param file context file
     * @return collection of {@link HelpResolver}
     */
    public Collection<HelpResolver> getHelpResolvers(FileObject file) {
        return null;
    }
    
    /**
     * The module may provide an information about some extra browsers / css rendering engines
     * 
     * @param file context file
     * @return collection of {@link Broser}s which should be active for the given context.
     */
    public Collection<Browser> getExtraBrowsers(FileObject file) {
        return null;
    }
    
    /**
     * If one wants to customize the css completion beyond the level which the PropertyDescriptor-s
     * allows, may use this method to achieve this.
     * 
     * @param context the code completion context
     * @return a list of completion proposals
     */
    public List<CompletionProposal> getCompletionProposals(CompletionContext context) {
        return Collections.emptyList();
    }
  
    /**
     * This method allows clients to provide semantic highlighting for some specific code parts. 
     * The returned NodeVisitor implementation typically visits the ParseTree and for 
     * interesting nodes or node structures puts OffsetRange->ColoringAttributes mapping to the result map.
     * 
     * May return null if there are no semantic highlights to show.
     * 
     * @param <T>
     * @param context
     * @param result
     * @return null or an implementation of NodeVisitor with the <T> type parameter
     */
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        return null;
    }
    
    /**
     * This method allows clients to provide mark occurrences functionality for some specific code parts. 
     * The returned NodeVisitor implementation typically visits the ParseTree and for 
     * nodes representing same/similar content puts their OffsetRange-s to the result set.
     * 
     * May return null if there are no mark occurrences areas
     * 
     * @param <T>
     * @param context
     * @param result
     * @return null or an implementation of NodeVisitor with the <T> type parameter
     */
    public <T extends Set<OffsetRange>> NodeVisitor<T> getMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result) {
        return null;
    }
    
     /**
     * This method allows clients to provide code folds for some specific code parts. 
     * The returned NodeVisitor implementation typically visits the ParseTree and for 
     * nodes representing interesting(foldable) content puts their String->OffsetRange 
     * pairs to the result map. The key is a string representing the fold type. 
     * For the values see csl.api module.
     * 
     * May return null if there are no folds
     * 
     * @param <T>
     * @param context
     * @param result
     * @return null or an implementation of NodeVisitor with the <T> type parameter
     */
    public <T extends Map<String, List<OffsetRange>>> NodeVisitor<T> getFoldsNodeVisitor(FeatureContext context, T result) {
        return null;
    }
    
     /**
     * This method allows clients to provide go to declaration / hyperlinking functionality.     
     * The returned NodeVisitor implementation typically visits the ParseTree and for 
     * nodes representing navigable content returns a pair of OffsetRange and FutureParamTask
     * which is responsible for the navigation action if the user decides to navigate.
     * 
     * The method is typically called when the user hovers the mouse cursor over 
     * the code with the Ctrl/Command key pressed. When the user clicks on a marked 
     * area then the FutureParamTask is invoked.
     * 
     * May return null if there is no declaration at the caret position
     * 
     * @param <T>
     * @param context
     * @param result
     * @return null or a pair of OffsetRange and FutureParamTask
     */
    public Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> getDeclaration(Document document, int caretOffset) {
        return null;
    }
    
    
     /**
     * This method allows clients to provide navigator content for some specific code parts. 
     * The returned NodeVisitor implementation typically visits the ParseTree and for 
     * nodes representing interesting content puts instancies of StructureItem-s to the result list.
     * 
     * May return null if there are no structure items
     * 
     * @param <T>
     * @param context
     * @param result
     * @return null or an implementation of NodeVisitor with the <T> type parameter
     */
    public <T extends List<StructureItem>> NodeVisitor<T> getStructureItemsNodeVisitor(FeatureContext context, T result) {
        return null;
    }
    
    public SemanticAnalyzer getSemanticAnalyzer() {
        return null;
    }
            
    public boolean isInstantRenameAllowed(EditorFeatureContext context) {
        return false;
    }
    
    public <T extends Set<OffsetRange>> NodeVisitor<T> getInstantRenamerVisitor(EditorFeatureContext context, T result) {
        return null;
    }
    
}
