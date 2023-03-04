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
package org.netbeans.modules.spring.beans.completion.completors;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.StringTokenizer;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.CompletorUtils;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.MatchType;
import org.netbeans.modules.spring.java.Property;
import org.netbeans.modules.spring.java.PropertyFinder;
import org.netbeans.modules.spring.java.PropertyType;
import org.w3c.dom.Node;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class PropertyCompletor extends Completor {

    public PropertyCompletor(int invocationOffset) {
        super(invocationOffset);
    }

    @Override
    protected int initAnchorOffset(CompletionContext context) {
        int idx = context.getCurrentTokenOffset() + 1;
        String typedPrefix = context.getTypedPrefix();
        int offset = typedPrefix.lastIndexOf('.'); // NOI18N
        return idx + offset + 1;
    }

    @Override
    protected void compute(final CompletionContext context) throws IOException {
        final String propertyPrefix = context.getTypedPrefix();
        final JavaSource js = JavaUtils.getJavaSource(context.getFileObject());
        if (js == null) {
            return;
        }

        // traverse the properties
        final int dotIndex = propertyPrefix.lastIndexOf("."); // NOI18N

        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws Exception {
                Node beanTag = SpringXMLConfigEditorUtils.getBean(context.getTag());
                if (beanTag == null) {
                    return;
                }
                String className = new BeanClassFinder(
                        SpringXMLConfigEditorUtils.getTagAttributes(beanTag),
                        context.getFileObject()).findImplementationClass(true);
                if (className == null) {
                    return;
                }
                TypeElement te = JavaUtils.findClassElementByBinaryName(className, cc);
                if (te == null) {
                    return;
                }
                TypeMirror startType = te.asType();
                ElementUtilities eu = cc.getElementUtilities();

                // property chain
                if (dotIndex != -1) {
                    String getterChain = propertyPrefix.substring(0, dotIndex);
                    StringTokenizer tokenizer = new StringTokenizer(getterChain, "."); // NOI18N

                    while (tokenizer.hasMoreTokens() && startType != null) {
                        String propertyName = tokenizer.nextToken();
                        Property[] props = new PropertyFinder(startType, propertyName, eu, MatchType.PREFIX).findProperties();

                        // no matching element found
                        if (props.length == 0 || props[0].getGetter() == null) {
                            startType = null;
                            break;
                        }

                        TypeMirror retType = props[0].getGetter().getReturnType();
                        if (retType.getKind() == TypeKind.DECLARED) {
                            startType = retType;
                        } else {
                            startType = null;
                        }
                    }
                }

                if (startType == null) {
                    return;
                }

                String setterPrefix = "";
                if (dotIndex != propertyPrefix.length() - 1) {
                    setterPrefix = propertyPrefix.substring(dotIndex + 1);
                }

                Property[] props = new PropertyFinder(startType, setterPrefix, eu, MatchType.PREFIX).findProperties();

                final EnumSet<PropertyType> typeWithSetters = EnumSet.of(PropertyType.READ_WRITE, PropertyType.WRITE_ONLY);
                for (Property prop : props) {
                    final boolean hasSetter = typeWithSetters.contains(prop.getType());
                    if (hasSetter) {
                        addCacheItem(SpringXMLConfigCompletionItem.createPropertyItem(getAnchorOffset(), prop));
                    }
                }
            }
        }, false);
    }

    @Override
    public boolean canFilter(CompletionContext context) {
        return CompletorUtils.canFilter(context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset(), CompletorUtils.RESOURCE_PATH_ELEMENT_ACCEPTOR);
    }

    @Override
    protected List<SpringXMLConfigCompletionItem> doFilter(CompletionContext context) {
        return CompletorUtils.filter(getCacheItems(), context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset());
    }
}
