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

import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.java.Public;
import org.netbeans.modules.spring.java.Static;
import org.w3c.dom.Node;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class InitDestroyMethodCompletor extends JavaMethodCompletor {

    public InitDestroyMethodCompletor(int invocationOffset) {
        super(invocationOffset);
    }

    @Override
    protected Public getPublicFlag(CompletionContext context) {
        return Public.DONT_CARE;
    }

    @Override
    protected Static getStaticFlag(CompletionContext context) {
        return Static.NO;
    }

    @Override
    protected int getArgCount(CompletionContext context) {
        return 0;
    }

    @Override
    protected String getTypeName(CompletionContext context) {
        Node beanTag = SpringXMLConfigEditorUtils.getBean(context.getTag());
        return new BeanClassFinder(SpringXMLConfigEditorUtils.getTagAttributes(beanTag),
                context.getFileObject()).findImplementationClass(true);
    }
}
