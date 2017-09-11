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
package org.netbeans.modules.search.matcher;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnmappableCharacterException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.search.MatchingObject.Def;
import org.netbeans.modules.search.ResultView;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Base for all matchers used by the basic search provider.
 *
 * @author jhavlin
 */
public abstract class AbstractMatcher  {

    private static final Logger LOG = Logger.getLogger(
            AbstractMatcher.class.getName());
    private long totalTime = 0;
    private int matchingFiles = 0;
    private int matchingItems = 0;
    private boolean strict = true;

    public AbstractMatcher() {
    }
    
    public final Def check(FileObject file, SearchListener listener) {
        long start = System.currentTimeMillis();
        Def def = checkMeasuredInternal(file, listener);
        long end = System.currentTimeMillis();
        if (def != null) {
            matchingFiles++;
            if (def.getTextDetails() != null
                    && !def.getTextDetails().isEmpty()) {
                matchingItems++;
            }
        }
        totalTime += end - start;
        return def;
    }

    protected abstract Def checkMeasuredInternal(FileObject file,
            SearchListener listener);

    public long getTotalTime() {
        return totalTime;
    }

    public int getMatchingFiles() {
        return matchingFiles;
    }

    public int getMatchingItems() {
        return matchingItems;
    }

    public abstract void terminate();

    public boolean isStrict() {
        return strict;
    }

    /**
     * @param strict True if an error should be raised for decoding errors
     * (unmappable character etc.), false if such error should be ignored.
     * Strict mode should be used when replacing.
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public CharsetDecoder prepareDecoder(Charset charset) {
        CharsetDecoder decoder = charset.newDecoder();
        if (strict) {
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        } else {
            decoder.onMalformedInput(CodingErrorAction.IGNORE);
            decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        }
        return decoder;
    }

    /**
     * Handle an error thrown while file decoding. Inform search listener and
     * append detailed info into the IDE Log.
     */
    protected final void handleDecodingError(SearchListener listener,
            FileObject file, CharsetDecoder decoder,
            CharacterCodingException e) {

        String charsetName;
        try {
            if (decoder.isAutoDetecting() && decoder.isCharsetDetected()) {
                Charset c = decoder.detectedCharset();
                if (c != null) {
                    charsetName = c.displayName();
                } else {
                    charsetName = decoder.charset().displayName();
                }
            } else {
                charsetName = decoder.charset().displayName();
            }
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Failed to obtain actual charset", ex); //NOI18N
            charsetName = decoder == null ? "null" : decoder.toString();//NOI18N
        }

        String msg = NbBundle.getMessage(ResultView.class,
                "TEXT_INFO_ERROR_ENCODING", charsetName);               //NOI18N
        listener.fileContentMatchingError(file.getPath(),
                new Exception(msg, e));
        LOG.log(Level.INFO, "{0}; UnmappableCharacterException: {1}", //NOI18N
                new Object[]{file.getPath(), e.getMessage()});
    }
}
