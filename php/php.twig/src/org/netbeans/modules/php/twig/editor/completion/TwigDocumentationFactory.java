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
package org.netbeans.modules.php.twig.editor.completion;

import java.util.MissingResourceException;
import org.netbeans.modules.php.twig.editor.completion.TwigDocumentation.TwigDocumentationImpl;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public interface TwigDocumentationFactory {

    TwigDocumentation create(String elementName);

    public abstract static class BaseDocumentationFactory implements TwigDocumentationFactory {

        protected abstract String getDocumentationKey();

        @Override
        @NbBundle.Messages("MSG_NoDocumentation=Documentation not found.")
        public TwigDocumentation create(String elementName) {
            TwigDocumentation result;
            try {
                result = new TwigDocumentationImpl(NbBundle.getMessage(TagDocumentationFactory.class, getDocumentationKey() + elementName));
            } catch (MissingResourceException ex) {
                result = new TwigDocumentationImpl(Bundle.MSG_NoDocumentation());
            }
            return result;
        }

    }

    public static final class TagDocumentationFactory extends BaseDocumentationFactory {

        private static final TwigDocumentationFactory INSTANCE = new TagDocumentationFactory();

        public static TwigDocumentationFactory getInstance() {
            return INSTANCE;
        }

        private TagDocumentationFactory() {
        }

        @Override
        protected String getDocumentationKey() {
            return "TAG_"; //NOI18N
        }

    }

    public static final class FilterDocumentationFactory extends BaseDocumentationFactory {

        private static final TwigDocumentationFactory INSTANCE = new FilterDocumentationFactory();

        public static TwigDocumentationFactory getInstance() {
            return INSTANCE;
        }

        private FilterDocumentationFactory() {
        }

        @Override
        protected String getDocumentationKey() {
            return "FILTER_"; //NOI18N
        }

    }

    public static final class FunctionDocumentationFactory extends BaseDocumentationFactory {

        private static final TwigDocumentationFactory INSTANCE = new FunctionDocumentationFactory();

        public static TwigDocumentationFactory getInstance() {
            return INSTANCE;
        }

        private FunctionDocumentationFactory() {
        }

        @Override
        protected String getDocumentationKey() {
            return "FUNCTION_"; //NOI18N
        }

    }

    public static final class TestDocumentationFactory extends BaseDocumentationFactory {

        private static final TwigDocumentationFactory INSTANCE = new TestDocumentationFactory();

        public static TwigDocumentationFactory getInstance() {
            return INSTANCE;
        }

        private TestDocumentationFactory() {
        }

        @Override
        protected String getDocumentationKey() {
            return "TEST_"; //NOI18N
        }

    }

    public static final class OperatorDocumentationFactory extends BaseDocumentationFactory {

        private static final TwigDocumentationFactory INSTANCE = new OperatorDocumentationFactory();

        public static TwigDocumentationFactory getInstance() {
            return INSTANCE;
        }

        private OperatorDocumentationFactory() {
        }

        @Override
        protected String getDocumentationKey() {
            return "OPERATOR_"; //NOI18N
        }

    }

}
