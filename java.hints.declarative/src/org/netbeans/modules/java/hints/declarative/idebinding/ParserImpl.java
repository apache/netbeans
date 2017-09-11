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
package org.netbeans.modules.java.hints.declarative.idebinding;

import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintTokenId;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author lahvac
 */
public class ParserImpl extends Parser {

    private Snapshot snapshot;
    private DeclarativeHintsParser.Result result;
    
    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        this.snapshot = snapshot;
        this.result   = null;
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        if (this.result == null) {
            this.result = new DeclarativeHintsParser().parse(snapshot.getSource().getFileObject(),
                                                             snapshot.getText(),
                                                             snapshot.getTokenHierarchy().tokenSequence(DeclarativeHintTokenId.language()));
        }

        return new ResultImpl(snapshot, result);
    }

    @Override
    public void cancel() {}

    @Override
    public void addChangeListener(ChangeListener changeListener) {}

    @Override
    public void removeChangeListener(ChangeListener changeListener) {}

    private static final class ResultImpl extends Result {

        private final DeclarativeHintsParser.Result result;

        public ResultImpl(Snapshot _snapshot, DeclarativeHintsParser.Result result) {
            super(_snapshot);
            this.result = result;
        }

        @Override
        protected void invalidate() {
        }

    }

    @MimeRegistration(mimeType=DeclarativeHintTokenId.MIME_TYPE, service=ParserFactory.class)
    public static final class FactoryImpl extends ParserFactory {
        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new ParserImpl();
        }
    }

    public static DeclarativeHintsParser.Result getResult(Result r) {
        if (r instanceof ResultImpl) {
            return ((ResultImpl) r).result;
        }

        return null;
    }
}
