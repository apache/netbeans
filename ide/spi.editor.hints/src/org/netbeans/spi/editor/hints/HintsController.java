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
