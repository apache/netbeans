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
package org.netbeans.modules.jshell.parsing;

import org.netbeans.modules.jshell.model.ConsoleModel;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.jshell.model.ConsoleContents;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
@EmbeddingProvider.Registration(
        mimeType = "text/x-repl", targetMimeType = "text/x-java")
public class ConsoleEmbeddingProvider extends EmbeddingProvider {
    
    @Override
    public void cancel() {
        
    }
    
    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        Document d = snapshot.getSource().getDocument(false);
        if (d == null) {
            return Collections.emptyList();
        }
        ShellSession session = ShellSession.get(d);
        if (session == null) {
            return Collections.emptyList();
        }
        List[] res = new List[1];
        try {
            ParserManager.parse(Collections.singleton(snapshot.getSource()), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ConsoleContents cts = ConsoleContents.get(resultIterator);
                    if (cts == null) {
                        res[0] = Collections.emptyList();
                        return;
                    }
                    ConsoleSection inputSection = cts.getInputSection();
                    EmbeddingProcessor p = new EmbeddingProcessor(session, cts, snapshot, inputSection);
                    res[0] = p.process();
                }
                
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return (List<Embedding>)res[0];
    }
    
    @Override
    public int getPriority() {
        return 0;
    }
}
