/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.spi.editor.hints;

import java.util.Collection;
import java.util.ArrayList;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.editor.hints.HintsControllerImpl;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * One of the main entry points for the Editor Hints SPI. Call one of the
 * setErrors method to change the hints on the document.
 * @author Jan Lahoda
 */
public final class HintsController {

    /** No instances of this class are needed. */
    private HintsController() {
    }

    /**
     * Assign given list of errors to a file. This removes any errors that were assigned to this
     * file before under the same "layer". The file to which the errors should be assigned
     * is gathered from the given document.
     * 
     * @param doc document to which the errors should be assigned
     * @param layer unique layer ID
     * @param errors to use
     */
    public static void setErrors(final @NonNull Document doc, final @NonNull String layer, @NonNull Collection<? extends ErrorDescription> errors) {
        Parameters.notNull("doc", doc);
        Parameters.notNull("layer", layer);
        Parameters.notNull("errors", errors);

        final Collection<? extends ErrorDescription> errorsCopy = new ArrayList<ErrorDescription>(errors);

        WORKER.post(new Runnable() {
            public void run() {
                HintsControllerImpl.setErrors(doc, layer, errorsCopy);
            }
        });
    }
    
    /**
     * Assign given list of errors to a given file. This removes any errors that were assigned to this
     * file before under the same "layer".
     *
     * @param file to which the errors should be assigned
     * @param layer unique layer ID
     * @param errors to use
     */
    public static void setErrors(final @NonNull FileObject file, @NonNull final String layer, @NonNull Collection<? extends ErrorDescription> errors) {
        Parameters.notNull("file", file);
        Parameters.notNull("layer", layer);
        Parameters.notNull("errors", errors);

        final Collection<? extends ErrorDescription> errorsCopy = new ArrayList<ErrorDescription>(errors);

        WORKER.post(new Runnable() {
            public void run() {
                HintsControllerImpl.setErrors(file, layer, errorsCopy);
            }
        });
    }
    
    private static RequestProcessor WORKER = new RequestProcessor("HintsController worker");
    
}
