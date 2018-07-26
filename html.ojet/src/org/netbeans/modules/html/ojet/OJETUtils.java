/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.ojet;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.knockout.api.KODataBindTokenId;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class OJETUtils {

    public static final String OJ_COMPONENT = "ojComponent";  //NOI18N
    public static final String OJ_MODULE = "ojModule"; //NOI18N

    @StaticResource
    public static final String OJET_ICON_PATH = "org/netbeans/modules/html/ojet/ui/resources/ojet-icon.png"; // NOI18N
    public static final ImageIcon OJET_ICON = ImageUtilities.loadImageIcon(OJET_ICON_PATH, false); // NOI18N

    private static final AtomicBoolean isLogged = new AtomicBoolean(false);
    public static final String USG_LOGGER_NAME = "org.netbeans.ui"; // NOI18N
    private static final Logger USG_LOGGER = Logger.getLogger(USG_LOGGER_NAME);
    private static final String USG_MESSAGE = "GENERIC_OJET_CC";
    
    public static void logUsage(List<? extends Object> params) {
        if (!isLogged.get()) {
            LogRecord logRecord = new LogRecord(Level.WARNING, USG_MESSAGE);
            logRecord.setLoggerName(USG_LOGGER.getName());
            logRecord.setResourceBundle(NbBundle.getBundle(OJETUtils.class));
            logRecord.setResourceBundleName(OJETUtils.class.getPackage().getName() + ".Bundle"); // NOI18N
            if (params != null) {
                logRecord.setParameters(params.toArray(new Object[params.size()]));
            }
            USG_LOGGER.log(logRecord);
            isLogged.set(true);
        }
    }
    
    public static String getPrefix(OJETContext ojContext, Document document, int offset) {
        TokenHierarchy th = TokenHierarchy.get(document);
        String empty = "";
        switch (ojContext) {
            case DATA_BINDING:
                TokenSequence<KODataBindTokenId> ts = LexerUtils.getTokenSequence(th, offset, KODataBindTokenId.language(), false);
                if (ts != null) {
                    int diff = ts.move(offset);
                    if (diff == 0 && ts.movePrevious() || ts.moveNext()) {
                        //we are on a token of ko-data-bind token sequence
                        Token<KODataBindTokenId> etoken = ts.token();
                        if (etoken.id() == KODataBindTokenId.KEY) {
                            //ke|
                            CharSequence prefix = diff == 0 ? etoken.text() : etoken.text().subSequence(0, diff);
                            return prefix.toString();
                        }
                    }
                    break;
                } 
        }
        return empty;
    }

    public static int getPrefixOffset(OJETContext ojContext, Document document, int offset) {
        TokenHierarchy th = TokenHierarchy.get(document);
        int result = offset;
        switch (ojContext) {
            case DATA_BINDING:
                TokenSequence<KODataBindTokenId> ts = LexerUtils.getTokenSequence(th, offset, KODataBindTokenId.language(), false);
                if (ts != null) {
                    int diff = ts.move(offset);
                    if (diff == 0 && ts.movePrevious() || ts.moveNext()) {
                        //we are on a token of ko-data-bind token sequence
                        Token<KODataBindTokenId> etoken = ts.token();
                        if (etoken.id() == KODataBindTokenId.KEY) {
                            //ke
                            return ts.offset();
                        }
                    }
                    break;
                } 
        }
        return result;
    }
    
    
}
