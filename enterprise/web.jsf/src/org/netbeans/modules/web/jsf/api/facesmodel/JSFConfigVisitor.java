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

package org.netbeans.modules.web.jsf.api.facesmodel;



/**
 *
 * @author Petr Pisl
 */
public interface JSFConfigVisitor {

    void visit(FacesConfig component);
    void visit(ManagedBean component);
    void visit(NavigationRule component);
    void visit(NavigationCase component);
    void visit(Converter component);
    void visit(Description component);
    void visit(DisplayName compoent);
    void visit(Icon component);
    void visit(ViewHandler component);
    void visit(Application component);
    void visit(LocaleConfig component);
    void visit(DefaultLocale component);
    void visit(SupportedLocale component);
    void visit(ResourceBundle component);
    void visit( ActionListener listener );
    void visit( DefaultRenderKitId id );
    void visit( MessageBundle bundle );
    void visit( NavigationHandler handler );
    void visit( PartialTraversal traversal );
    void visit( StateManager traversal );
    void visit( ElResolver traversal );
    void visit( PropertyResolver traversal );
    void visit( VariableResolver traversal );
    void visit( ResourceHandler traversal );
    void visit( FacesSystemEventListener listener );
    void visit( DefaultValidators validators );
    void visit( Ordering ordering );
    void visit( After after);
    void visit( Before before );
    void visit( Name name);
    void visit( Others others );
    void visit( AbsoluteOrdering absoluteOrdering );
    void visit( Factory factory );
    void visit( FacesValidatorId id );
    void visit( ApplicationFactory factory );
    void visit( ExceptionHandlerFactory factory );
    void visit( ExternalContextFactory factory );
    void visit( FacesContextFactory factory);
    void visit( FaceletCacheFactory factory);
    void visit( PartialViewContextFactory factory );
    void visit( LifecycleFactory factory );
    void visit( ViewDeclarationLanguageFactory factory );
    void visit( TagHandlerDelegateFactory factory );
    void visit( RenderKitFactory factory );
    void visit( VisitContextFactory factory );
    void visit( FacesComponent component );
    void visit( Facet component );
    void visit( ConfigAttribute attr );
    void visit( Property property );
    void visit( FacesManagedProperty property );
    void visit( ListEntries entries );
    void visit( MapEntries entries );
    void visit( If iff );
    void visit( Redirect redirect);
    void visit( ViewParam param);
    void visit( ReferencedBean bean );
    void visit( RenderKit kit );
    void visit( FacesRenderer renderer );
    void visit( FacesClientBehaviorRenderer renderer );
    void visit( Lifecycle lifecycle );
    void visit( PhaseListener listener );
    void visit( FacesValidator validator );
    void visit ( FacesBehavior behavior );
    void visit(ProtectedViews protectedView);
    void visit(UrlPattern urlPattern);
    void visit(ResourceLibraryContracts resourceLibraryContracts);
    void visit(ContractMapping contractMapping);
    void visit(FlashFactory flashFactory);
    void visit(FlowHandlerFactory flowHandlerFactory);
    void visit(FlowDefinition facesFlowDefinition);
    void visit(FlowStartNode startNode);
    void visit(FlowInitializer initializer);
    void visit(FlowFinalizer finalizer);
    void visit(FlowView view);
    void visit(FlowSwitch swtch);
    void visit(FlowDefaultOutcome defaultOutcome);
    void visit(FlowReturn retrn);
    void visit(FromOutcome fromOutcome);
    void visit(FlowCall flowCall);
    void visit(FlowCallFacesFlowReference flowReference);
    void visit(FlowId flowId);
    void visit(FlowDocumentId flowDocumentId);
    void visit(Value value);
    void visit(Method method);
    void visit(FlowMethodCall methodCall);
    void visit(Clazz clazz);
    void visit(FlowCallParameter parameter);
    void visit(FlowCallInOutParameter parameter);

    /**
     * Default shallow visitor.
     */
    public static class Default implements JSFConfigVisitor {
        public void visit(FacesConfig component) {
            visitChild();
        }
        public void visit(ManagedBean component) {
            visitChild();
        }
        public void visit(NavigationRule component) {
            visitChild();
        }
        public void visit(NavigationCase component) {
            visitChild();
        }
        public void visit(Converter component) {
            visitChild();
        }
        public void visit(Description component) {
            visitChild();
        }
        public void visit(DisplayName component) {
            visitChild();
        }
        public void visit(Icon component) {
            visitChild();
        }
        public void visit(ViewHandler component) {
            visitChild();
        }
        public void visit(Application component) {
            visitChild();
        }

        public void visit(LocaleConfig component) {
            visitChild();
        }

        public void visit(DefaultLocale component) {
            visitChild();
        }

        public void visit(SupportedLocale component) {
            visitChild();
        }

        public void visit(ResourceBundle component) {
            visitChild();
        }

        public void visit( ActionListener listener ) {
            visitChild();
        }

        public void visit( DefaultRenderKitId id ) {
            visitChild();
        }

        public void visit( MessageBundle id ) {
            visitChild();
        }

        public void visit( NavigationHandler handler ){
            visitChild();
        }

        public void visit( PartialTraversal traversal ) {
            visitChild();
        }

        public void visit( StateManager manager ) {
            visitChild();
        }

        public void visit( ElResolver resolver ) {
            visitChild();
        }

        public void visit( PropertyResolver resolver ) {
            visitChild();
        }

        public void visit( VariableResolver resolver ) {
            visitChild();
        }

        public void visit( ResourceHandler handler ) {
            visitChild();
        }

        public void visit( FacesSystemEventListener listener ) {
            visitChild();
        }

        public void visit( DefaultValidators validators ) {
            visitChild();
        }

        public void visit( Ordering ordering ) {
            visitChild();
        }

        public void visit( After after ) {
            visitChild();
        }

        public void visit( Before before ) {
            visitChild();
        }

        public void visit( Name name ) {
            visitChild();
        }

        public void visit( Others others ) {
            visitChild();
        }

        public void visit( AbsoluteOrdering ordering ) {
            visitChild();
        }

        public void visit( Factory factory ) {
            visitChild();
        }

        public void visit( FacesValidatorId id ) {
            visitChild();
        }

        public void visit(ApplicationFactory factory ){
            visitChild();
        }

        public void visit( ExceptionHandlerFactory factory ){
            visitChild();
        }

        public void visit(ExternalContextFactory factory ){
            visitChild();
        }

        public void visit( FacesContextFactory factory){
            visitChild();
        }

        public void visit( FaceletCacheFactory factory){
            visitChild();
        }

        public void visit( PartialViewContextFactory factory ){
            visitChild();
        }

        public void visit( LifecycleFactory factory ){
            visitChild();
        }

        public  void visit( ViewDeclarationLanguageFactory factory ){
            visitChild();
        }

        public void visit( TagHandlerDelegateFactory factory ){
            visitChild();
        }

        public void visit( RenderKitFactory factory ){
            visitChild();
        }

        public void visit( VisitContextFactory factory ){
            visitChild();
        }

        public void visit( FacesComponent component ){
            visitChild();
        }

        public void visit( Facet facet ){
            visitChild();
        }

        public void visit( ConfigAttribute attribute ){
            visitChild();
        }

        public void visit( Property property ){
            visitChild();
        }

        public void visit( FacesManagedProperty property ){
            visitChild();
        }

        public void visit( ListEntries entries ){
            visitChild();
        }

        public void visit( MapEntries entries ){
            visitChild();
        }

        public void visit( If iff ){
            visitChild();
        }

        public void visit( Redirect redirect ){
            visitChild();
        }

        public void visit( ViewParam param ){
            visitChild();
        }

        public void visit( ReferencedBean bean ){
            visitChild();
        }

        public void visit( RenderKit kit ){
            visitChild();
        }

        public void visit( FacesRenderer render ){
            visitChild();
        }

        public void visit( FacesClientBehaviorRenderer render ){
            visitChild();
        }

        public void visit( Lifecycle lifecycle ){
            visitChild();
        }

        public void visit( PhaseListener listener ){
            visitChild();
        }

        public void visit( FacesValidator validator ){
            visitChild();
        }

        public void visit( FacesBehavior behavior ){
            visitChild();
        }

        protected void visitChild() {
        }

        @Override
        public void visit(FlowDefinition facesFlowDefinition) {
            visitChild();
        }

        @Override
        public void visit(ProtectedViews protectedView) {
            visitChild();
        }

        @Override
        public void visit(ResourceLibraryContracts resourceLibraryContracts) {
            visitChild();
        }

        @Override
        public void visit(FlashFactory flashFactory) {
            visitChild();
        }

        @Override
        public void visit(FlowHandlerFactory flowHandlerFactory) {
            visitChild();
        }

        @Override
        public void visit(UrlPattern urlPattern) {
            visitChild();
        }

        @Override
        public void visit(ContractMapping contractMapping) {
            visitChild();
        }

        @Override
        public void visit(FlowStartNode startNode) {
            visitChild();
        }

        @Override
        public void visit(FlowInitializer initializer) {
            visitChild();
        }

        @Override
        public void visit(FlowFinalizer finalizer) {
            visitChild();
        }

        @Override
        public void visit(FlowView view) {
            visitChild();
        }

        @Override
        public void visit(FlowSwitch svitch) {
            visitChild();
        }

        @Override
        public void visit(FlowDefaultOutcome defaultOutcome) {
            visitChild();
        }

        @Override
        public void visit(FlowReturn retrn) {
            visitChild();
        }

        @Override
        public void visit(FromOutcome fromOutcome) {
            visitChild();
        }

        @Override
        public void visit(FlowCall flowCall) {
            visitChild();
        }

        @Override
        public void visit(FlowCallFacesFlowReference flowReference) {
            visitChild();
        }

        @Override
        public void visit(FlowId flowId) {
            visitChild();
        }

        @Override
        public void visit(FlowDocumentId flowDocumentId) {
            visitChild();
        }

        @Override
        public void visit(Value value) {
            visitChild();
        }

        @Override
        public void visit(Method method) {
            visitChild();
        }

        @Override
        public void visit(FlowMethodCall methodCall) {
            visitChild();
        }

        @Override
        public void visit(Clazz clazz) {
            visitChild();
        }

        @Override
        public void visit(FlowCallParameter parameter) {
            visitChild();
        }

        @Override
        public void visit(FlowCallInOutParameter parameter) {
            visitChild();
        }
    }

    /**
     * Deep visitor.
     */
    public static class Deep extends Default {
        protected void visitChild(JSFConfigComponent component) {
            for (JSFConfigComponent child : component.getChildren()) {
                child.accept(this);
            }
        }
    }

}
