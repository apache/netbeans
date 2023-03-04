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

package org.netbeans.modules.php.editor.completion;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpVariable;
import org.netbeans.modules.php.editor.completion.PHPCompletionItem.CompletionRequest;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.elements.VariableElementImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Convert {@link PhpBaseElement PHP element} to {@link CompletionProposal}.
 * @author Tomas Mysik
 */
public final class PhpElementCompletionItem {

    private PhpElementCompletionItem() {
    }

    static CompletionProposal fromPhpElement(PhpBaseElement element, CompletionRequest request) {
        assert element != null;
        if (element instanceof PhpVariable) {
            return new PhpVariableCompletionItem((PhpVariable) element, request);
        }
        throw new IllegalArgumentException("Unsupported PHP element type (only variables are currently supported): " + element);
    }

    private static final class PhpVariableCompletionItem extends PHPCompletionItem.VariableItem {
        private final PhpVariable variable;

        public PhpVariableCompletionItem(PhpVariable variable, CompletionRequest request) {
            super(VariableElementImpl.create(variable.getName(), variable.getOffset(), getFileNameUrl(variable), null, Collections.<TypeResolver>emptySet(), false), request);
            this.variable = variable;
        }

        @Override
        protected String getTypeName() {
            String fullyQualifiedName = variable.getFullyQualifiedName();
            if (fullyQualifiedName != null) {
                return fullyQualifiedName;
            }
            return super.getTypeName();
        }

        @Override
        public boolean isSmart() {
            return true;
        }

        private static String getFileNameUrl(PhpVariable variable) {
            FileObject file = variable.getFile();
            if (file != null && file.isValid()) {
                try {
                    return Utilities.toURI(FileUtil.toFile(file)).toURL().toExternalForm();
                } catch (MalformedURLException ex) {
                    Logger.getLogger(PhpElementCompletionItem.class.getName()).log(Level.WARNING, null, ex);
                }
            }
            return null;
        }
    }
}
