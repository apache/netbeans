/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.document;

import javax.swing.event.DocumentEvent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Utilities related to document post-modification.
 *
 * @author Miloslav Metelka
 */
public class DocumentPostModificationUtils {

    /**
     * Mark the given document event as post-modification edit.
     * This is used by document implementations to distinguish between regular edits
     * and the edits created as a result of post-modification e.g. an instant rename functionality.
     * 
     * @param evt event into which the post-modification marker should be added.
     *  The passed event has to have the event property storage already initialized
     *  by {@link DocumentUtilities#addEventPropertyStorage(javax.swing.event.DocumentEvent)}.
     */
    public static void markPostModification(@NonNull DocumentEvent evt) {
        DocumentUtilities.putEventPropertyIfSupported(evt, DocumentPostModificationUtils.class, Boolean.TRUE);
    }

    /**
     * Test whether the given document event is a document post-modification edit event.
     * For example a caret may treat the post-modification edits differently than regular edits.
     *
     * @param evt event to test.
     * @return true if the event is a post-modification edit or false otherwise.
     */
    public static boolean isPostModification(@NonNull DocumentEvent evt) {
        return Boolean.TRUE.equals(DocumentUtilities.getEventProperty(evt, DocumentPostModificationUtils.class));
    }

}
