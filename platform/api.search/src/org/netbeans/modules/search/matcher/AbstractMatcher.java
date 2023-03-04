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
