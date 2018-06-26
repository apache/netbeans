/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
