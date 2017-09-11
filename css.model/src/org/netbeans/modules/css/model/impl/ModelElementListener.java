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
package org.netbeans.modules.css.model.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.modules.css.model.api.AtRule;
import org.netbeans.modules.css.model.api.AtRuleId;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.BodyItem;
import org.netbeans.modules.css.model.api.CharSet;
import org.netbeans.modules.css.model.api.CharSetValue;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.Expression;
import org.netbeans.modules.css.model.api.FontFace;
import org.netbeans.modules.css.model.api.GenericAtRule;
import org.netbeans.modules.css.model.api.ImportItem;
import org.netbeans.modules.css.model.api.Imports;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.MediaBody;
import org.netbeans.modules.css.model.api.MediaExpression;
import org.netbeans.modules.css.model.api.MediaFeature;
import org.netbeans.modules.css.model.api.MediaFeatureValue;
import org.netbeans.modules.css.model.api.MediaQuery;
import org.netbeans.modules.css.model.api.MediaQueryList;
import org.netbeans.modules.css.model.api.MediaQueryOperator;
import org.netbeans.modules.css.model.api.MediaType;
import org.netbeans.modules.css.model.api.MozDocument;
import org.netbeans.modules.css.model.api.MozDocumentFunction;
import org.netbeans.modules.css.model.api.Namespace;
import org.netbeans.modules.css.model.api.NamespacePrefixName;
import org.netbeans.modules.css.model.api.Namespaces;
import org.netbeans.modules.css.model.api.Page;
import org.netbeans.modules.css.model.api.PlainElement;
import org.netbeans.modules.css.model.api.Prio;
import org.netbeans.modules.css.model.api.Property;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.PropertyValue;
import org.netbeans.modules.css.model.api.ResourceIdentifier;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.Selector;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.model.api.VendorAtRule;
import org.netbeans.modules.css.model.api.WebkitKeyframeSelectors;
import org.netbeans.modules.css.model.api.WebkitKeyframes;
import org.netbeans.modules.css.model.api.WebkitKeyframesBlock;
import org.openide.util.Exceptions;

/**
 * @author marekfukala
 */
public interface ModelElementListener {

    public void elementAdded(AtRuleId atRuleId);
    
    public void elementAdded(AtRule atRule);

    public void elementAdded(GenericAtRule genericAtRule);

    public void elementAdded(MozDocument mozDocument);

    public void elementAdded(MozDocumentFunction mozDocumentFunction);

    public void elementAdded(VendorAtRule vendorAtRule);

    public void elementAdded(WebkitKeyframes webkitKeyFrames);

    public void elementAdded(WebkitKeyframeSelectors webkitKeyframeSelectors);

    public void elementAdded(WebkitKeyframesBlock webkitKeyframesBlock);

    public void elementAdded(StyleSheet stylesheet);

    public void elementAdded(CharSet charSet);

    public void elementAdded(CharSetValue charSetValue);

    public void elementAdded(FontFace fontFace);

    public void elementAdded(Imports imports);

    public void elementAdded(ImportItem importItem);

    public void elementAdded(ResourceIdentifier resourceIdentifier);

    public void elementAdded(Media media);
    
    public void elementAdded(MediaBody mediaBody);
    
    public void elementAdded(MediaBodyItem mediaBodyItem);

    public void elementAdded(MediaQueryList mediaQueryList);

    public void elementAdded(MediaQuery mediaQuery);

    public void elementAdded(MediaQueryOperator mediaQueryOperator);

    public void elementAdded(MediaExpression mediaExpression);

    public void elementAdded(MediaFeature mediaFeature);
    
    public void elementAdded(MediaFeatureValue mediaFeatureValue);

    public void elementAdded(MediaType mediaType);

    public void elementAdded(Namespaces namespaces);

    public void elementAdded(Namespace namespace);

    public void elementAdded(NamespacePrefixName namespacePrefixName);

    public void elementAdded(Body body);

    public void elementAdded(BodyItem bodyItem);

    public void elementAdded(Rule rule);

    public void elementAdded(SelectorsGroup selectorsGroup);

    public void elementAdded(Selector selector);

    public void elementAdded(Declarations declarations);

    public void elementAdded(Declaration declaration);
    
    public void elementAdded(PropertyDeclaration declaration);

    public void elementAdded(Property property);

    public void elementAdded(PropertyValue propertyValue);

    public void elementAdded(Expression expression);

    public void elementAdded(Prio prio);

    public void elementAdded(PlainElement plainElement);

    public void elementAdded(Page page);
    
    public void elementRemoved(AtRuleId atRuleId);
    
    public void elementRemoved(AtRule atRule);

    public void elementRemoved(GenericAtRule genericAtRule);

    public void elementRemoved(MozDocument mozDocument);

    public void elementRemoved(MozDocumentFunction mozDocumentFunction);

    public void elementRemoved(VendorAtRule vendorAtRule);

    public void elementRemoved(WebkitKeyframes webkitKeyFrames);

    public void elementRemoved(WebkitKeyframeSelectors webkitKeyframeSelectors);

    public void elementRemoved(WebkitKeyframesBlock webkitKeyframesBlock);

    public void elementRemoved(StyleSheet stylesheet);

    public void elementRemoved(CharSet charSet);

    public void elementRemoved(CharSetValue charSetValue);

    public void elementRemoved(FontFace fontFace);

    public void elementRemoved(Imports imports);

    public void elementRemoved(ImportItem importItem);

    public void elementRemoved(ResourceIdentifier resourceIdentifier);

    public void elementRemoved(Media media);
    
    public void elementRemoved(MediaBody mediaBody);
    
    public void elementRemoved(MediaBodyItem mediaBodyItem);

    public void elementRemoved(MediaQueryList mediaQueryList);

    public void elementRemoved(MediaQuery mediaQuery);

    public void elementRemoved(MediaQueryOperator mediaQueryOperator);

    public void elementRemoved(MediaExpression mediaExpression);

    public void elementRemoved(MediaFeature mediaFeature);
    
    public void elementRemoved(MediaFeatureValue mediaFeatureValue);

    public void elementRemoved(MediaType mediaType);

    public void elementRemoved(Namespaces namespaces);

    public void elementRemoved(Namespace namespace);

    public void elementRemoved(NamespacePrefixName namespacePrefixName);

    public void elementRemoved(PropertyValue propertyValue);

    public void elementRemoved(Body body);

    public void elementRemoved(BodyItem bodyItem);

    public void elementRemoved(Rule rule);

    public void elementRemoved(SelectorsGroup selectorsGroup);

    public void elementRemoved(Selector selector);

    public void elementRemoved(Declarations declarations);

    public void elementRemoved(Declaration declaration);
    
    public void elementRemoved(PropertyDeclaration declaration);

    public void elementRemoved(Property property);

    public void elementRemoved(Expression expression);

    public void elementRemoved(Prio prio);

    public void elementRemoved(PlainElement plainElement);

    public void elementRemoved(Page page);

    public static class Support {

        private static void fireElementEvent(Element element, ModelElementListener listener, boolean add) {
            Class clazz = element.getClass();
            Class[] interfaces = clazz.getInterfaces();
            for (Class in : interfaces) {
                if (Element.class.isAssignableFrom(in)) {
                    try {
                        //the interface extends Element - so this is the one we are interested in
                        String methodName = add ? "elementAdded" : "elementRemoved";
                        Method method = listener.getClass().getMethod(methodName, in);
                        method.invoke(listener, element);
                    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        public static void fireElementAdded(Element element, ModelElementListener listener) {
            fireElementEvent(element, listener, true);
        }

        public static void fireElementRemoved(Element element, ModelElementListener listener) {
            fireElementEvent(element, listener, false);
        }
    }

//        public static void fireElementAdded(Element element, ModelElementListener listener) {
//            if (element instanceof StyleSheet) {
//                listener.elementAdded((StyleSheet) element);
//            } else if (element instanceof CharSet) {
//                listener.elementAdded((CharSet) element);
//            } else if (element instanceof CharSetValue) {
//                listener.elementAdded((CharSetValue) element);
//            } else if (element instanceof FontFace) {
//                listener.elementAdded((FontFace) element);
//            } else if (element instanceof Imports) {
//                listener.elementAdded((Imports) element);
//            } else if (element instanceof ImportItem) {
//                listener.elementAdded((ImportItem) element);
//            } else if (element instanceof ResourceIdentifier) {
//                listener.elementAdded((ResourceIdentifier) element);
//            } else if (element instanceof MediaQueryList) {
//                listener.elementAdded((MediaQueryList) element);
//            } else if (element instanceof Media) {
//                listener.elementAdded((Media) element);
//            } else if (element instanceof MediaQuery) {
//                listener.elementAdded((MediaQuery) element);
//            } else if (element instanceof MediaQueryOperator) {
//                listener.elementAdded((MediaQueryOperator) element);
//            } else if (element instanceof MediaFeature) {
//                listener.elementAdded((MediaFeature) element);
//            } else if (element instanceof MediaExpression) {
//                listener.elementAdded((MediaExpression) element);
//            } else if (element instanceof MediaType) {
//                listener.elementAdded((MediaType) element);
//            } else if (element instanceof Namespaces) {
//                listener.elementAdded((Namespaces) element);
//            } else if (element instanceof Namespace) {
//                listener.elementAdded((Namespace) element);
//            } else if (element instanceof NamespacePrefixName) {
//                listener.elementAdded((NamespacePrefixName) element);
//            } else if (element instanceof PropertyValue) {
//                listener.elementAdded((PropertyValue) element);
//            } else if (element instanceof Body) {
//                listener.elementAdded((Body) element);
//            } else if (element instanceof BodyItem) {
//                listener.elementAdded((BodyItem) element);
//            } else if (element instanceof Rule) {
//                listener.elementAdded((Rule) element);
//            } else if (element instanceof SelectorsGroup) {
//                listener.elementAdded((SelectorsGroup) element);
//            } else if (element instanceof Selector) {
//                listener.elementAdded((Selector) element);
//            } else if (element instanceof Declarations) {
//                listener.elementAdded((Declarations) element);
//            } else if (element instanceof Declaration) {
//                listener.elementAdded((Declaration) element);
//            } else if (element instanceof Property) {
//                listener.elementAdded((Property) element);
//            } else if (element instanceof Expression) {
//                listener.elementAdded((Expression) element);
//            } else if (element instanceof Prio) {
//                listener.elementAdded((Prio) element);
//            } else if (element instanceof Page) {
//                listener.elementAdded((Page) element);
//            }
//        }
//        public static void fireElementRemoved(Element element, ModelElementListener listener) {
//            if (element instanceof StyleSheet) {
//                listener.elementRemoved((StyleSheet) element);
//            } else if (element instanceof CharSet) {
//                listener.elementRemoved((CharSet) element);
//            } else if (element instanceof CharSetValue) {
//                listener.elementRemoved((CharSetValue) element);
//            } else if (element instanceof Imports) {
//                listener.elementRemoved((Imports) element);
//            } else if (element instanceof FontFace) {
//                listener.elementRemoved((FontFace) element);
//            } else if (element instanceof ImportItem) {
//                listener.elementRemoved((ImportItem) element);
//            } else if (element instanceof ResourceIdentifier) {
//                listener.elementRemoved((ResourceIdentifier) element);
//            } else if (element instanceof Media) {
//                listener.elementRemoved((Media) element);
//            } else if (element instanceof MediaQuery) {
//                listener.elementRemoved((MediaQueryList) element);
//            } else if (element instanceof MediaQuery) {
//                listener.elementRemoved((MediaQuery) element);
//            } else if (element instanceof MediaQueryOperator) {
//                listener.elementRemoved((MediaQueryOperator) element);
//            } else if (element instanceof MediaFeature) {
//                listener.elementRemoved((MediaFeature) element);
//            } else if (element instanceof MediaExpression) {
//                listener.elementRemoved((MediaExpression) element);
//            } else if (element instanceof MediaType) {
//                listener.elementRemoved((MediaType) element);
//            } else if (element instanceof Namespaces) {
//                listener.elementRemoved((Namespaces) element);
//            } else if (element instanceof Namespace) {
//                listener.elementRemoved((Namespace) element);
//            } else if (element instanceof NamespacePrefixName) {
//                listener.elementRemoved((NamespacePrefixName) element);
//            } else if (element instanceof PropertyValue) {
//                listener.elementRemoved((PropertyValue) element);
//            } else if (element instanceof Body) {
//                listener.elementRemoved((Body) element);
//            } else if (element instanceof BodyItem) {
//                listener.elementRemoved((BodyItem) element);
//            } else if (element instanceof Rule) {
//                listener.elementRemoved((Rule) element);
//            } else if (element instanceof SelectorsGroup) {
//                listener.elementRemoved((SelectorsGroup) element);
//            } else if (element instanceof Selector) {
//                listener.elementRemoved((Selector) element);
//            } else if (element instanceof Declarations) {
//                listener.elementRemoved((Declarations) element);
//            } else if (element instanceof Declaration) {
//                listener.elementRemoved((Declaration) element);
//            } else if (element instanceof Property) {
//                listener.elementRemoved((Property) element);
//            } else if (element instanceof Expression) {
//                listener.elementRemoved((Expression) element);
//            } else if (element instanceof Prio) {
//                listener.elementRemoved((Prio) element);
//            } else if (element instanceof Page) {
//                listener.elementRemoved((Page) element);
//            }
//        }
//    }
    
    public static class Adapter implements ModelElementListener {

        @Override
        public void elementAdded(StyleSheet stylesheet) {
        }

        @Override
        public void elementAdded(CharSet charSet) {
        }
        
        @Override
        public void elementAdded(AtRule atRule) {
        }

        @Override
        public void elementAdded(CharSetValue charSetValue) {
        }

        @Override
        public void elementAdded(FontFace fontFace) {
        }

        @Override
        public void elementAdded(Imports imports) {
        }

        @Override
        public void elementAdded(ImportItem importItem) {
        }

        @Override
        public void elementAdded(ResourceIdentifier resourceIdentifier) {
        }

        @Override
        public void elementAdded(MediaQueryList mediaQueryList) {
        }

        @Override
        public void elementAdded(MediaQuery mediaQuery) {
        }

        @Override
        public void elementAdded(Namespaces namespaces) {
        }

        @Override
        public void elementAdded(Namespace namespace) {
        }

        @Override
        public void elementAdded(NamespacePrefixName namespacePrefixName) {
        }

        @Override
        public void elementAdded(Body body) {
        }

        @Override
        public void elementAdded(BodyItem bodyItem) {
        }

        @Override
        public void elementAdded(Rule rule) {
        }

        @Override
        public void elementAdded(SelectorsGroup selectorsGroup) {
        }

        @Override
        public void elementAdded(Selector selector) {
        }

        @Override
        public void elementAdded(Declarations declarations) {
        }
        
        @Override
        public void elementAdded(Declaration declaration) {
        }

        @Override
        public void elementAdded(PropertyDeclaration declaration) {
        }

        @Override
        public void elementAdded(Property property) {
        }

        @Override
        public void elementAdded(Expression expression) {
        }

        @Override
        public void elementAdded(Prio prio) {
        }

        @Override
        public void elementAdded(PlainElement plainElement) {
        }

        @Override
        public void elementRemoved(StyleSheet stylesheet) {
        }

        @Override
        public void elementRemoved(CharSet charSet) {
        }

        @Override
        public void elementRemoved(CharSetValue charSetValue) {
        }

        @Override
        public void elementRemoved(FontFace fontFace) {
        }

        @Override
        public void elementRemoved(Imports imports) {
        }

        @Override
        public void elementRemoved(ImportItem importItem) {
        }

        @Override
        public void elementRemoved(ResourceIdentifier resourceIdentifier) {
        }

        @Override
        public void elementRemoved(MediaQueryList mediaQueryList) {
        }

        @Override
        public void elementRemoved(MediaQuery mediaQuery) {
        }

        @Override
        public void elementRemoved(Namespaces namespaces) {
        }

        @Override
        public void elementRemoved(Namespace namespace) {
        }

        @Override
        public void elementRemoved(NamespacePrefixName namespacePrefixName) {
        }

        @Override
        public void elementRemoved(Body body) {
        }

        @Override
        public void elementRemoved(BodyItem bodyItem) {
        }

        @Override
        public void elementRemoved(Rule rule) {
        }

        @Override
        public void elementRemoved(SelectorsGroup selectorsGroup) {
        }

        @Override
        public void elementRemoved(Selector selector) {
        }

        @Override
        public void elementRemoved(Declarations declarations) {
        }

        @Override
        public void elementRemoved(Declaration declaration) {
        }
        
        @Override
        public void elementRemoved(PropertyDeclaration declaration) {
        }

        @Override
        public void elementRemoved(Property property) {
        }

        @Override
        public void elementRemoved(Expression expression) {
        }

        @Override
        public void elementRemoved(Prio prio) {
        }

        @Override
        public void elementRemoved(PlainElement plainElement) {
        }

        @Override
        public void elementAdded(MediaQueryOperator mediaQuery) {
        }

        @Override
        public void elementAdded(MediaExpression mediaQuery) {
        }

        @Override
        public void elementAdded(MediaFeature mediaQuery) {
        }

        @Override
        public void elementAdded(MediaType mediaQuery) {
        }

        @Override
        public void elementRemoved(MediaQueryOperator mediaQuery) {
        }

        @Override
        public void elementRemoved(MediaExpression mediaQuery) {
        }

        @Override
        public void elementRemoved(MediaFeature mediaQuery) {
        }

        @Override
        public void elementRemoved(MediaType mediaQuery) {
        }

        @Override
        public void elementRemoved(Media media) {
        }

        @Override
        public void elementAdded(Media media) {
        }

        @Override
        public void elementAdded(Page page) {
        }

        @Override
        public void elementRemoved(Page page) {
        }

        @Override
        public void elementAdded(PropertyValue propertyValue) {
        }

        @Override
        public void elementRemoved(PropertyValue propertyValue) {
        }

        @Override
        public void elementAdded(AtRuleId atRuleId) {
        }

        @Override
        public void elementAdded(GenericAtRule genericAtRule) {
        }

        @Override
        public void elementAdded(MozDocument mozDocument) {
        }

        @Override
        public void elementAdded(MozDocumentFunction mozDocumentFunction) {
        }

        @Override
        public void elementAdded(VendorAtRule vendorAtRule) {
        }

        @Override
        public void elementAdded(WebkitKeyframes webkitKeyFrames) {
        }

        @Override
        public void elementAdded(WebkitKeyframeSelectors webkitKeyframeSelectors) {
        }

        @Override
        public void elementAdded(WebkitKeyframesBlock webkitKeyframesBlock) {
        }

        @Override
        public void elementRemoved(AtRuleId atRuleId) {
        }

        @Override
        public void elementRemoved(AtRule atRule) {
        }

        @Override
        public void elementRemoved(GenericAtRule genericAtRule) {
        }

        @Override
        public void elementRemoved(MozDocument mozDocument) {
        }

        @Override
        public void elementRemoved(MozDocumentFunction mozDocumentFunction) {
        }

        @Override
        public void elementRemoved(VendorAtRule vendorAtRule) {
        }

        @Override
        public void elementRemoved(WebkitKeyframes webkitKeyFrames) {
        }

        @Override
        public void elementRemoved(WebkitKeyframeSelectors webkitKeyframeSelectors) {
        }

        @Override
        public void elementRemoved(WebkitKeyframesBlock webkitKeyframesBlock) {
        }

        @Override
        public void elementAdded(MediaFeatureValue mediaFeatureValue) {
        }

        @Override
        public void elementRemoved(MediaFeatureValue mediaFeatureValue) {
        }

        @Override
        public void elementAdded(MediaBody mediaBody) {
        }

        @Override
        public void elementRemoved(MediaBody mediaBody) {
        }

        @Override
        public void elementAdded(MediaBodyItem mediaBodyItem) {
        }

        @Override
        public void elementRemoved(MediaBodyItem mediaBodyItem) {
        }
        
    }
    
}
