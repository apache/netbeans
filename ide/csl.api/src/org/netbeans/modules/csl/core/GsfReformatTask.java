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
package org.netbeans.modules.csl.core;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;


public class GsfReformatTask implements ReformatTask {

    private static final Logger LOG = Logger.getLogger(GsfReformatTask.class.getName());
    
    private final Context context;
    private final Source source;
    private Formatter formatter;
    private String mimeType;
    
    
    GsfReformatTask(Source source, Context context) {
        this.context = context;
        this.source = source;
    }

    private synchronized Formatter getFormatter() {
        if (formatter == null) {
            // XXX: Carefull here, generally context.mimePath() != mimeType. This
            // task's factory was created for a top level language (mimeType), but the task
            // itself can be used for an embedded language.
            // If the task is used for the document itself (not an embedded
            // section) Context.mimePath() == mimeType.
            // However, if it is used for an embedded section the Context.mimePath() gives
            // the mime path (languege path) of that section. Which is generally
            // something like 'application/x-httpd-eruby/text/x-ruby'. While the
            // task was registered for 'text/x-ruby'.
            // Therefore with the __current__ implementation of MimeLookupInitializerImpl
            // we can simply take the last component of Context.mimePath().
            MimePath mimePath = MimePath.parse(context.mimePath());
            mimeType = mimePath.size() > 1 ? mimePath.getMimeType(mimePath.size() - 1) : mimePath.getPath();
            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
            formatter = language.getFormatter();
        }
        return formatter;
    }

    public void reformat () throws BadLocationException {
        final Formatter f = getFormatter();
        
        if (f != null) {
            if (f.needsParserResult()) {
                try {
                    ParserManager.parse (
                        Collections.<Source> singleton (source),
                        new UserTask () {
                            public void run (ResultIterator resultIterator) throws ParseException {
                                MimePath mp = MimePath.get(resultIterator.getSnapshot().getMimeType());
                                String inh = mp.getInheritedType();
                                if (mp.getMimeType(0).equals(mimeType) || mimeType.equals(inh)) {
                                    Parser.Result parserResult = (ParserResult) resultIterator.getParserResult();
                                    if (!(parserResult instanceof ParserResult)) {
                                        return;
                                    }

                                    f.reformat(context, (ParserResult)parserResult);
                                }

                                for (Embedding e : resultIterator.getEmbeddings()) {
                                    run(resultIterator.getResultIterator(e));
                                }
                            }
                        }
                    );
                } catch (ParseException e) {
                    LOG.log(Level.WARNING, null, e);
                }
            } else {
                f.reformat (context, null);
            }
        }
    }

    public ExtraLock reformatLock() {
        Formatter f = getFormatter();

        if (f != null && f.needsParserResult()) {
            return new Lock();
        }

        return null;
    }
        
    private class Lock implements ExtraLock {

        public void lock() {
            Utilities.acquireParserLock();
        }

        public void unlock() {
            Utilities.releaseParserLock();
        }        
    }

}
