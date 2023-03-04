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
package org.netbeans.modules.php.latte.completion;

import java.util.MissingResourceException;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public interface LatteDocumentationFactory {

    LatteDocumentation create(String elementName);

    public abstract static class BaseDocumentationFactory implements LatteDocumentationFactory {

        protected abstract String getDocumentationKey();

        @Override
        @NbBundle.Messages("MSG_NoDocumentation=Documentation not found.")
        public LatteDocumentation create(String itemName) {
            String content;
            try {
                content = NbBundle.getMessage(LatteDocumentation.Factory.class, getDocumentationKey() + itemName);
            } catch (MissingResourceException ex) {
                content = Bundle.MSG_NoDocumentation();
            }
            return new LatteDocumentation.DummyDocumentation(itemName, content);
        }

    }

    public static final class MacroDocumentationFactory extends BaseDocumentationFactory {

        private static final LatteDocumentationFactory INSTANCE = new MacroDocumentationFactory();

        public static LatteDocumentationFactory getInstance() {
            return INSTANCE;
        }

        private MacroDocumentationFactory() {
        }

        @Override
        protected String getDocumentationKey() {
            return "MACRO_"; //NOI18N
        }

    }

    public static final class HelperDocumentationFactory extends BaseDocumentationFactory {

        private static final LatteDocumentationFactory INSTANCE = new HelperDocumentationFactory();

        public static LatteDocumentationFactory getInstance() {
            return INSTANCE;
        }

        private HelperDocumentationFactory() {
        }

        @Override
        protected String getDocumentationKey() {
            return "HELPER_"; //NOI18N
        }

    }

    public static final class KeywordDocumentationFactory extends BaseDocumentationFactory {

        private static final LatteDocumentationFactory INSTANCE = new KeywordDocumentationFactory();

        public static LatteDocumentationFactory getInstance() {
            return INSTANCE;
        }

        private KeywordDocumentationFactory() {
        }

        @Override
        protected String getDocumentationKey() {
            return "KEYWORD_"; //NOI18N
        }

    }

    public static final class IteratorItemDocumentationFactory extends BaseDocumentationFactory {

        private static final LatteDocumentationFactory INSTANCE = new IteratorItemDocumentationFactory();

        public static LatteDocumentationFactory getInstance() {
            return INSTANCE;
        }

        private IteratorItemDocumentationFactory() {
        }

        @Override
        protected String getDocumentationKey() {
            return "ITERATOR_ITEM_"; //NOI18N
        }

    }

    public static final class VariableDocumentationFactory extends BaseDocumentationFactory {

        private static final LatteDocumentationFactory INSTANCE = new VariableDocumentationFactory();

        public static LatteDocumentationFactory getInstance() {
            return INSTANCE;
        }

        private VariableDocumentationFactory() {
        }

        @Override
        protected String getDocumentationKey() {
            return "VARIABLE_"; //NOI18N
        }

    }

    public static final class ControlDocumentationFactory extends BaseDocumentationFactory {

        private static final LatteDocumentationFactory INSTANCE = new ControlDocumentationFactory();

        public static LatteDocumentationFactory getInstance() {
            return INSTANCE;
        }

        private ControlDocumentationFactory() {
        }

        @Override
        protected String getDocumentationKey() {
            return "CONTROL_"; //NOI18N
        }

    }

}
