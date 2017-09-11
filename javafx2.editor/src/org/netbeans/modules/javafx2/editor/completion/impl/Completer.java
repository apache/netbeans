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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Completer provides CompletionItems for a certain feature or a language
 * construct. Its {@link Factory} is called when the user invokes the CC,
 * and may create a Completer instance if it decides the context is interesting.
 * The Completer may produce zero to many CompletionItems.
 * <p/>
 * The Completer may hold state, a new instance should be created for each Completion
 * invocation by the {@code Factory.createCompleter} method.
 * @author sdedic
 */
public interface Completer {
    /**
     * Processes the CompletionContext and provides the completion items.
     * The method is called <b>OUTSIDE</b> the Document's read lock.
     * 
     * @return null, or any number (incl. zero) CompletionItems
     */
    @CheckForNull
    public List<? extends CompletionItem> complete();
    
    public boolean hasMoreItems();
     
    /**
     * Factory interface should be registered into MIME lookup using {@link MimeRegistration}
     * annotation. The {@link #createCompleter} will be called with an initialized
     * CompletionContext to decide whether the Factory can provide an appropriate Completer.
     * This allows to decompose completion code into pieces and extend it over time.
     */
    @MimeLocation(subfolderName="completion")
    public interface Factory {
        
        /**
         * Called by the infrastructure when completion items are to be produced.
         * The method should check the {@link CompletionContext}, whether its state
         * is applicable to this Completer and if so, the {@link Completer} instance 
         * should be returned.
         * <p/>
         * New Completer instance should be allocated and initialized
         * with CompletionContext by this method.
         * 
         * @param ctx contextual information to create completion items
         * @return 
         */
        @CheckForNull
        public Completer    createCompleter(@NonNull CompletionContext ctx);
    }
}
