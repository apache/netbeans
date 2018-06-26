/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
