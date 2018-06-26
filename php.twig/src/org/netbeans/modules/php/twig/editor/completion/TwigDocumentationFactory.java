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
