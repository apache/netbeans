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
