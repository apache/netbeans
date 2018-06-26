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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl, ads
 */
public enum JSFConfigQNames {

    FACES_CONFIG("faces-config"),                   //NOI18N
    //general
    DESCRIPTION("description"),                     //NOI18N
    DISPLAY_NAME("display-name"),                   //NOI18N
    ICON("icon"),                                   //NOI18N
    SMALL_ICON("small-icon"),                       //NOI18N
    LARGE_ICON("large-icon"),                       //NOI18N
    //managed-bean
    MANAGED_BEAN("managed-bean"),                   //NOI18N
    MANAGED_BEAN_NAME("managed-bean-name"),         //NOI18N
    MANAGED_BEAN_CLASS("managed-bean-class"),       //NOI18N
    MANAGED_BEAN_SCOPE("managed-bean-scope"),       //NOI18N
    MANAGED_BEAN_EXTENSION("managed-bean-extension"),//NOI18N
    MANAGED_PROPERTY("managed-property"),           //NOI18N
    MAP_ENTRIES("map-entries"),                     //NOI18N
    KEY_CLASS("key-class"),                         //NOI18N
    VALUE_CLASS("value-class"),                     //NOI18N
    LIST_ENTRIES("list-entries"),                   //NOI18N
    //navigation-rule
    NAVIGATION_RULE("navigation-rule"),             //NOI18N
    FROM_VIEW_ID("from-view-id"),                   //NOI18N
    //navigation-case
    NAVIGATION_CASE("navigation-case"),             //NOI18N
    IF("if"),                                       //NOI18N
    NAVIGATION_RULE_EXTENSION("navigation-rule-extension"),//NOI18N
    FROM_OUTCOME("from-outcome"),                   //NOI18N
    FROM_ACTION("from-action"),                     //NOI18N
    TO_VIEW_ID("to-view-id"),                       //NOI18N
    REDIRECT("redirect"),                           //NOI18N
    INCLUDE_VIEW_PARAMS("include-view-params"),     //NOI18N
    VIEW_PARAM("view-param"),                       //NOI18N
    VALUE("value"),                                 //NOI18N
    //converter
    CONVERTER("converter"),                         //NOI18N
    CONVERTER_EXTENSION("converter-extension"),     //NOI18N
    CONVERTER_ID("converter-id"),                   //NOI18N
    CONVERTER_FOR_CLASS("converter-for-class"),     //NOI18N
    CONVERTER_CLASS("converter-class"),             //NOI18N
    //application
    APPLICATION("application"),                     //NOI18N
    VIEW_HANDLER("view-handler"),                   //NOI18N
    LOCALE_CONFIG("locale-config"),                 //NOI18N
    DEFAULT_LOCALE("default-locale"),               //NOI18N
    SUPPORTED_LOCALE("supported-locale"),           //NOI18N
    ACTION_LISTENER("action-listener"),             //NOI18N
    DEFAULT_RENDER_KIT_ID("default-render-kit-id"), //NOI18N
    MESSAGE_BUNDLE("message-bundle"),               //NOI18N
    NAVIGATION_HANDLER("navigation-handler"),       //NOI18N
    PARTIAL_TRAVERSAL("partial-traversal"),         //NOI18N
    STATE_MANAGER("state-manager"),                 //NOI18N
    EL_RESOLVER("el-resolver"),                     //NOI18N
    SYSTEM_EVENT_LISTENER("system-event-listener"), //NOI18N
    SYSTEM_EVENT_LISTENER_CLASS("system-event-listener-class"),//NOI18N
    SYSTEM_EVENT_CLASS("system-event-class"),       //NOI18N
    SOURCE_CLASS("source-class"),                   //NOI18N
    PROPERTY_RESOLVER("property-resolver"),         //NOI18N
    VARIABLE_RESOLVER("variable-resolver"),         //NOI18N
    RESOURCE_HANDLER("resource-handler"),           //NOI18N
    APPLICATION_EXTENSION("application-extension"), //NOI18N
    DEFAULT_VALIDATORS("default-validators"),       //NOI18N
    // ordering
    ORDERING("ordering"),                           //NOI18N
    AFTER("after"),                                 //NOI18N
    BEFORE("before"),                               //NOI18N
    ABSOLUTE_ORDERING("absolute-ordering"),         //NOI18N
    OTHERS("others"),                               //NOI18N
    // factory
    FACTORY("factory"),                             //NOI18N
    APPLICATION_FACTORY("application-factory"),     //NOI18N
    EXCEPTION_HANDLER_FACTORY("exception-handler-factory"),//NOI18N
    EXTERNAL_CONTEXT_FACTORY("external-context-factory"), //NOI18N
    FACES_CONTEXT_FACTORY("faces-context-factory"), //NOI18N
    FACELET_CACHE_FACTORY("facelet-cache-factory"), //NOI18N
    PARTIAL_VIEW_CONTEXT_FACTORY("partial-view-context-factory"),//NOI18N
    LIFECYCLE_FACTORY("lifecycle-factory"),         //NOI18N
    VIEW_DECLARATION_LANGUAGE_FACTORY("view-declaration-language-factory"),//NOI18N
    TAG_HANDLER_DELEGATE_FACTORY("tag-handler-delegate-factory"),//NOI18N
    RENDER_KIT_FACTORY("render-kit-factory"),       //NOI18N
    VISIT_CONTEXT_FACTORY("visit-context-factory"), //NOI18N
    FACTORY_EXTENSION("factory-extension"),         //NOI18N
    // component
    COMPONENT("component"),                         //NOI18N
    COMPONENT_TYPE("component-type"),               //NOI18N
    COMPONENT_CLASS("component-class"),             //NOI18N
    FACET("facet"),                                 //NOI18N
    FACET_NAME("facet-name"),                       //NOI18N
    ATTRIBUTE("attribute"),                         //NOI18N
    ATTRIBUTE_NAME("attribute-name"),                //NOI18N
    ATTRIBUTE_CLASS("attribute-class"),              //NOI18N
    PROPERTY("property"),                           //NOI18N
    PROPERTY_NAME("property-name"),                 //NOI18N
    PROPERTY_CLASS("property-class"),               //NOI18N
    COMPONENT_EXTENSION("component-extension"),     //NOI18N
    // name
    NAME("name"),                                   //NOI18N
    // referenced-bean
    REFERENCED_BEAN("referenced-bean"),             //NOI18N
    REFERENCED_BEAN_NAME("referenced-bean-name"),   //NOI18N
    REFERENCED_BEAN_CLASS("referenced-bean-class"), //NOI18N
    // render-kit
    RENDER_KIT("render-kit"),                       //NOI18N
    RENDER_KIT_ID("render-kit-id"),                 //NOI18N
    RENDER_KIT_CLASS("render-kit-class"),           //NOI18N
    RENDERER("renderer"),                           //NOI18N
    COMPONENT_FAMILY("component-family"),           //NOI18N
    RENDERER_TYPE("renderer-type"),                 //NOI18N
    RENDERER_CLASS("renderer-class"),               //NOI18N
    CLIENT_BEHAVIOR_RENDERER("client-behavior-renderer"),//NOI18N
    CLIENT_BEHAVIOR_RENDERER_TYPE("client-behavior-renderer-type"),//NOI18N
    CLIENT_BEHAVIOR_RENDERER_CLASS("client-behavior-renderer-class"),//NOI18N
    RENDER_KIT_EXTENSION("render-kit-extension"),   //NOI18N
    // lifecycle
    LIFECYCLE("lifecycle"),                         //NOI18N
    PHASE_LISTENER("phase-listener"),               //NOI18N
    LIFECYCLE_EXTENSION("lifecycle-extension"),     //NOI18N
    // validator
    VALIDATOR("validator"),                         //NOI18N
    VALIDATOR_ID("validator-id"),                   //NOI18N
    VALIDATOR_CLASS("validator-class"),             //NOI18N
    VALIDATOR_EXTENSION("validator-extension"),     //NOI18N
    // behavior
    BEHAVIOR("behavior"),                           //NOI18N
    BEHAVIOR_ID("behavior-id"),                     //NOI18N
    BEHAVIOR_CLASS("behavior-class"),               //NOI18N
    BEHAVIOR_EXTENSION("behavior-extension"),       //NOI18N
    // faces-config-extension
    FACES_CONFIG_EXTENSION("faces-config-extension"),//NOI18N
    // resource_bundle
    RESOURCE_BUNDLE("resource-bundle"),             //NOI18N
    BASE_NAME("base-name"),                         //NOI18N
    VAR("var"),                                     //NOI18N
    // resource library contracts
    RESOURCE_LIBRARY_CONTRACTS("resource-library-contracts"),//NOI18N
    URL_PATTERN("url-pattern"),                     //NOI18N
    CONTRACTS("contracts"),                         //NOI18N
    CONTRACT_MAPPING("contract-mapping"),          //NOI18N
    // JSF2.2 factories
    FLASH_FACTORY("flash-factory"),                 //NOI18N
    FLOW_HANDLER_FACTORY("flow-handler-factory"),
    // faces flow
    START_NODE("start-node"),                       //NOI18N
    VIEW("view"),                                   //NOI18N
    VDL_DOCUMENT("vdl-document"),                   //NOI18N
    DEFAULT_OUTCOME("default-outcome"),             //NOI18N
    SWITCH("switch"),                               //NOI18N
    METHOD_CALL("method-call"),                     //NOI18N
    FLOW_RETURN("flow-return"),                     //NOI18N
    INITIALIZER("initializer"),                     //NOI18N
    FINALIZER("finalizer"),                         //NOI18N
    FLOW_CALL("flow-call"),                         //NOI18N
    INBOUND_PARAMETER("inbound-parameter"),         //NOI18N
    OUTBOUND_PARAMETER("outbound-parameter"),       //NOI18N
    FLOW_REFERENCE("flow-reference"),               //NOI18N
    FLOW_ID("flow-id"),                             //NOI18N
    FLOW_DOCUMENT_ID("flow-document-id"),           //NOI18N
    METHOD("method"),                               //NOI18N
    FLOW_DEFINITION("flow-definition"),             //NOI18N
    PARAMETER("parameter"),                         //NOI18N
    CLASS("class"),                                 //NOI18N
    // protected views
    PROTECTED_VIEWS("protected-views");             //NOI18N

    private QName qname_1_1;
    private QName qname_1_2;
    private QName qname_2_0;
    private QName qname_2_1;
    private QName qname_2_2;


    public static final String JSF_1_2_NS = "http://java.sun.com/xml/ns/javaee";  //NOI18N
    public static final String JSF_2_0_NS = "http://java.sun.com/xml/ns/javaee";  //NOI18N
    public static final String JSF_2_1_NS = "http://java.sun.com/xml/ns/javaee";  //NOI18N
    public static final String JSF_2_2_NS = "http://xmlns.jcp.org/xml/ns/javaee"; //NOI18N
    public static final String JSF_1_1_NS = javax.xml.XMLConstants.NULL_NS_URI;
    public static final String JSFCONFIG_PREFIX = javax.xml.XMLConstants.DEFAULT_NS_PREFIX;


    JSFConfigQNames(String localName) {
        qname_1_1 = new QName(JSF_1_1_NS, localName, JSFCONFIG_PREFIX);
        qname_1_2 = new QName(JSF_1_2_NS, localName, JSFCONFIG_PREFIX);
        qname_2_0 = new QName(JSF_2_0_NS, localName, JSFCONFIG_PREFIX);
        qname_2_1 = new QName(JSF_2_1_NS, localName, JSFCONFIG_PREFIX);
        qname_2_2 = new QName(JSF_2_2_NS, localName, JSFCONFIG_PREFIX);
    }

    public QName getQName(JSFVersion version) {
        QName value = qname_1_1;
        if (version.equals(JSFVersion.JSF_1_2)) {
            value = qname_1_2;
        } else if (version.equals(JSFVersion.JSF_2_0)) {
            value = qname_2_0;
        } else if (version.equals(JSFVersion.JSF_2_1)) {
            value = qname_2_1;
        } else if (version.equals(JSFVersion.JSF_2_2)) {
            value = qname_2_2;
        }
        return value;
    }

    public QName getQName(String namespaceURI) {
        return new QName(namespaceURI, getLocalName(), JSFCONFIG_PREFIX);
    }

    public String getLocalName() {
        return qname_1_2.getLocalPart();
    }

    public String getQualifiedName(JSFVersion version) {
        String value = qname_1_1.getPrefix() + ":" + qname_1_1.getLocalPart();
        if (version.equals(JSFVersion.JSF_1_2)) {
            value = qname_1_2.getPrefix() + ":" + qname_1_2.getLocalPart();
        }
        return value;
    }

    public static boolean areSameQName(JSFConfigQNames jsfqname, Element element) {
        QName qname = AbstractDocumentComponent.getQName(element);
        if (JSFConfigQNames.JSF_1_2_NS.equals(element.getNamespaceURI())) {
            return jsfqname.getQName(JSFVersion.JSF_1_2).equals(qname);
        } else if (JSFConfigQNames.JSF_2_0_NS.equals(element.getNamespaceURI())) {
            return jsfqname.getQName(JSFVersion.JSF_2_0).equals(qname);
        } else if (JSFConfigQNames.JSF_2_1_NS.equals(element.getNamespaceURI())) {
            return jsfqname.getQName(JSFVersion.JSF_2_1).equals(qname);
        } else if (JSFConfigQNames.JSF_2_2_NS.equals(element.getNamespaceURI())) {
            return jsfqname.getQName(JSFVersion.JSF_2_2).equals(qname);
        }
        return jsfqname.getLocalName().equals(qname.getLocalPart());
    }

    private static final Set<QName> mappedQNames_1_1 = new HashSet<QName>();
    private static final Set<QName> mappedQNames_1_2 = new HashSet<QName>();
    private static final Set<QName> mappedQNames_2_0 = new HashSet<QName>();
    private static final Set<QName> mappedQNames_2_1 = new HashSet<QName>();
    private static final Set<QName> mappedQNames_2_2 = new HashSet<QName>();

    static {
        mappedQNames_1_1.add(FACES_CONFIG.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(MANAGED_BEAN.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(CONVERTER.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(NAVIGATION_RULE.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(NAVIGATION_CASE.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(DESCRIPTION.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(DISPLAY_NAME.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(ICON.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(APPLICATION.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(VIEW_HANDLER.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_1.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_1_1));
        mappedQNames_1_2.add(FACES_CONFIG.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(MANAGED_BEAN.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(CONVERTER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(NAVIGATION_RULE.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(NAVIGATION_CASE.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(DESCRIPTION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(DISPLAY_NAME.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(ICON.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(APPLICATION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(VIEW_HANDLER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(FACTORY.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(COMPONENT.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(REFERENCED_BEAN.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(RENDER_KIT.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(LIFECYCLE.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(VALIDATOR.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(FACES_CONFIG_EXTENSION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(ACTION_LISTENER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(DEFAULT_RENDER_KIT_ID.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(MESSAGE_BUNDLE.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(NAVIGATION_HANDLER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(STATE_MANAGER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(EL_RESOLVER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(PROPERTY_RESOLVER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(VARIABLE_RESOLVER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(LOCALE_CONFIG.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(APPLICATION_EXTENSION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(DEFAULT_LOCALE.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(SUPPORTED_LOCALE.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(APPLICATION_FACTORY.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(FACES_CONTEXT_FACTORY.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(LIFECYCLE_FACTORY.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(RENDER_KIT_FACTORY.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(FACTORY_EXTENSION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(FACET.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(ATTRIBUTE.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(PROPERTY.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(COMPONENT_EXTENSION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(CONVERTER_EXTENSION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(MANAGED_PROPERTY.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(MAP_ENTRIES.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(LIST_ENTRIES.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(MANAGED_BEAN_EXTENSION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(NAVIGATION_RULE_EXTENSION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(RENDER_KIT_EXTENSION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(PHASE_LISTENER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(LIFECYCLE_EXTENSION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(VALIDATOR_EXTENSION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(FACES_CONFIG_EXTENSION.getQName(JSFVersion.JSF_1_2));
        mappedQNames_1_2.add(RENDERER.getQName(JSFVersion.JSF_1_2));
        mappedQNames_2_0.add(FACES_CONFIG.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(MANAGED_BEAN.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(CONVERTER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(NAVIGATION_RULE.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(NAVIGATION_CASE.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(DESCRIPTION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(DISPLAY_NAME.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(ICON.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(APPLICATION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(VIEW_HANDLER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(FACTORY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(COMPONENT.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(REFERENCED_BEAN.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(RENDER_KIT.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(LIFECYCLE.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(VALIDATOR.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(FACES_CONFIG_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(ACTION_LISTENER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(DEFAULT_RENDER_KIT_ID.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(MESSAGE_BUNDLE.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(NAVIGATION_HANDLER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(STATE_MANAGER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(EL_RESOLVER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(PROPERTY_RESOLVER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(VARIABLE_RESOLVER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(LOCALE_CONFIG.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(APPLICATION_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(DEFAULT_LOCALE.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(SUPPORTED_LOCALE.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(APPLICATION_FACTORY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(FACES_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(LIFECYCLE_FACTORY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(RENDER_KIT_FACTORY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(FACTORY_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(FACET.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(ATTRIBUTE.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(PROPERTY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(COMPONENT_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(CONVERTER_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(MANAGED_PROPERTY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(MAP_ENTRIES.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(LIST_ENTRIES.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(MANAGED_BEAN_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(NAVIGATION_RULE_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(RENDER_KIT_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(PHASE_LISTENER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(LIFECYCLE_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(VALIDATOR_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(FACES_CONFIG_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(IF.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(REDIRECT.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(VIEW_PARAM.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(PARTIAL_TRAVERSAL.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(SYSTEM_EVENT_LISTENER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(RESOURCE_HANDLER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(DEFAULT_VALIDATORS.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(ORDERING.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(AFTER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(BEFORE.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(ABSOLUTE_ORDERING.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(OTHERS.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(EXCEPTION_HANDLER_FACTORY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(EXTERNAL_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(PARTIAL_VIEW_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(VIEW_DECLARATION_LANGUAGE_FACTORY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(TAG_HANDLER_DELEGATE_FACTORY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(VISIT_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(NAME.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(RENDERER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(CLIENT_BEHAVIOR_RENDERER.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(BEHAVIOR.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_0.add(BEHAVIOR_EXTENSION.getQName(JSFVersion.JSF_2_0));
        mappedQNames_2_1.add(FACES_CONFIG.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(MANAGED_BEAN.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(CONVERTER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(NAVIGATION_RULE.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(NAVIGATION_CASE.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(DESCRIPTION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(DISPLAY_NAME.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(ICON.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(APPLICATION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(VIEW_HANDLER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(COMPONENT.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(REFERENCED_BEAN.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(RENDER_KIT.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(LIFECYCLE.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(VALIDATOR.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(FACES_CONFIG_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(ACTION_LISTENER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(DEFAULT_RENDER_KIT_ID.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(MESSAGE_BUNDLE.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(NAVIGATION_HANDLER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(STATE_MANAGER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(EL_RESOLVER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(PROPERTY_RESOLVER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(VARIABLE_RESOLVER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(LOCALE_CONFIG.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(APPLICATION_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(DEFAULT_LOCALE.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(SUPPORTED_LOCALE.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(APPLICATION_FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(FACES_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(FACELET_CACHE_FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(LIFECYCLE_FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(RENDER_KIT_FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(FACTORY_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(FACET.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(ATTRIBUTE.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(PROPERTY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(COMPONENT_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(CONVERTER_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(MANAGED_PROPERTY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(MAP_ENTRIES.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(LIST_ENTRIES.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(MANAGED_BEAN_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(NAVIGATION_RULE_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(RENDER_KIT_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(PHASE_LISTENER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(LIFECYCLE_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(VALIDATOR_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(FACES_CONFIG_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(IF.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(REDIRECT.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(VIEW_PARAM.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(PARTIAL_TRAVERSAL.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(SYSTEM_EVENT_LISTENER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(RESOURCE_HANDLER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(DEFAULT_VALIDATORS.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(ORDERING.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(AFTER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(BEFORE.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(ABSOLUTE_ORDERING.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(OTHERS.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(EXCEPTION_HANDLER_FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(EXTERNAL_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(PARTIAL_VIEW_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(VIEW_DECLARATION_LANGUAGE_FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(TAG_HANDLER_DELEGATE_FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(VISIT_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(NAME.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(RENDERER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(CLIENT_BEHAVIOR_RENDERER.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(BEHAVIOR.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_1.add(BEHAVIOR_EXTENSION.getQName(JSFVersion.JSF_2_1));
        mappedQNames_2_2.add(FACES_CONFIG.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(MANAGED_BEAN.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(CONVERTER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(NAVIGATION_RULE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(NAVIGATION_CASE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(DESCRIPTION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(DISPLAY_NAME.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(ICON.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(APPLICATION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(VIEW_HANDLER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(COMPONENT.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(REFERENCED_BEAN.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(RENDER_KIT.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(LIFECYCLE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(VALIDATOR.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FACES_CONFIG_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(ACTION_LISTENER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(DEFAULT_RENDER_KIT_ID.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(MESSAGE_BUNDLE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(NAVIGATION_HANDLER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(STATE_MANAGER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(EL_RESOLVER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(PROPERTY_RESOLVER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(VARIABLE_RESOLVER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(LOCALE_CONFIG.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(APPLICATION_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(DEFAULT_LOCALE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(SUPPORTED_LOCALE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(APPLICATION_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FACES_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FACELET_CACHE_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(LIFECYCLE_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(RENDER_KIT_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FACTORY_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FACET.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(ATTRIBUTE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(PROPERTY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(COMPONENT_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(CONVERTER_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(MANAGED_PROPERTY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(MAP_ENTRIES.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(LIST_ENTRIES.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(MANAGED_BEAN_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(NAVIGATION_RULE_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(RENDER_KIT_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(PHASE_LISTENER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(LIFECYCLE_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(VALIDATOR_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FACES_CONFIG_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(IF.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(REDIRECT.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(VIEW_PARAM.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(PARTIAL_TRAVERSAL.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(SYSTEM_EVENT_LISTENER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(RESOURCE_HANDLER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(DEFAULT_VALIDATORS.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(ORDERING.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(AFTER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(BEFORE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(ABSOLUTE_ORDERING.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(OTHERS.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(EXCEPTION_HANDLER_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(EXTERNAL_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(PARTIAL_VIEW_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(VIEW_DECLARATION_LANGUAGE_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(TAG_HANDLER_DELEGATE_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(VISIT_CONTEXT_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(NAME.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(RENDERER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(CLIENT_BEHAVIOR_RENDERER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(BEHAVIOR.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(BEHAVIOR_EXTENSION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(RESOURCE_LIBRARY_CONTRACTS.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(RESOURCE_BUNDLE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(RESOURCE_HANDLER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(URL_PATTERN.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FLOW_HANDLER_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FLASH_FACTORY.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(START_NODE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(VIEW.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(VDL_DOCUMENT.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(DEFAULT_OUTCOME.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(SWITCH.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(METHOD_CALL.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FLOW_RETURN.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(INITIALIZER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FINALIZER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FLOW_CALL.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(INBOUND_PARAMETER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(OUTBOUND_PARAMETER.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FLOW_REFERENCE.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FLOW_ID.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(METHOD.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(FLOW_DEFINITION.getQName(JSFVersion.JSF_2_2));
        mappedQNames_2_2.add(PROTECTED_VIEWS.getQName(JSFVersion.JSF_2_2));
    }

    public static Set<QName> getMappedQNames(JSFVersion version) {
        Set<QName> mappedQNames = mappedQNames_1_1;
        if (version.equals(JSFVersion.JSF_1_2)) {
            mappedQNames = mappedQNames_1_2;
        } else if (version.equals(JSFVersion.JSF_2_0)) {
            mappedQNames = mappedQNames_2_0;
        } else if (version.equals(JSFVersion.JSF_2_1)) {
            mappedQNames = mappedQNames_2_1;
        } else if (version.equals(JSFVersion.JSF_2_2)) {
            mappedQNames = mappedQNames_2_2;
        }
        return Collections.unmodifiableSet(mappedQNames);
    }

}
