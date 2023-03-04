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
package org.netbeans.modules.csl.hints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.core.AbstractTaskFactory;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * This class is based on JavaHintsFactory in Retouche's org.netbeans.modules.java.hints
 *
 * @author Jan Lahoda
 */
public class GsfHintsFactory extends AbstractTaskFactory {
    public static final String LAYER_NAME = "csl-hints";
    
    /**
     * Creates a new instance of GsfHintsFactory
     */
    public GsfHintsFactory() {
        super(true); // XXX: Phase.RESOLVED, Priority.BELOW_NORMAL
    }

    @Override
    public Collection<? extends SchedulerTask> createTasks(Language l, Snapshot snapshot) {
        // avoid issue #230209, hint provider is useless without FileObject.
        FileObject fo = snapshot.getSource().getFileObject();
        if (fo != null) {
            return Collections.singleton(new GsfHintsProvider(fo));
        } else {
            return null;
        }
    }
    
    /**
     * Forces refresh of errors the same way as if the parse task was called by
     * the "cycle". Processes just 1 level of ParserResult, does not walk down to embeddings
     */
    public static List<ErrorDescription> getErrors(Snapshot s, ParserResult res, Snapshot tls) throws ParseException {
        FileObject fo = s.getSource().getFileObject();
        if (fo == null) {
            // see issue #212967
            return new ArrayList<ErrorDescription>();
        }
        GsfHintsProvider hp = new GsfHintsProvider(fo);
        List<ErrorDescription> descs = new ArrayList<ErrorDescription>();
        hp.processErrors(s, res, null, descs, tls);
        return descs;
    }

}
