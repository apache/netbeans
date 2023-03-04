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

package org.netbeans.modules.lexer.gen;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.netbeans.modules.lexer.gen.util.LexerGenUtilities;

/**
 * Mutable alternative of the {@link org.netbeans.api.lexer.TokenId}
 * used when the generated language source is being composed.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class MutableTokenId {

    public static final String SAMPLE_TEXT_CHECK_NONE = "none";
    public static final String SAMPLE_TEXT_CHECK_LENGTH = "length";
    public static final String SAMPLE_TEXT_CHECK_TEXT = "text";
    
    private final LanguageData languageData;
    
    private final String name;
    
    private int intId;
    
    private String tokenTypeName;
    
    private List categoryNames;
    
    private List sampleTexts;
    
    private String sampleTextCheck;
    
    private boolean caseInsensitive;
    
    private String comment;
    

    /**
     * Construct new MutableTokenId. All the properties
     * are set by corresponding setters.
     */
    public MutableTokenId(LanguageData languageData, String name) {
        this.languageData = languageData;
        this.name = name;
        this.intId = -1; // assign invalid int id initially

        categoryNames = new ArrayList();
        sampleTexts = new ArrayList();
    }
    
    /**
     * @return the language data to which this mutable tokenId belongs.
     */
    public final LanguageData getLanguageData() {
        return languageData;
    }
    
    /**
     * @return name of this tokenId.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return numeric identification of this tokenId.
     *  The initially assigned intId is set to -1.
     */
    public int getIntId() {
        return intId;
    }

    /**
     * Override the currently set intId (if any).
     * <BR>The value being set
     * will be skipped in the LanguageData automatically.
     * @param intId integer identification of this tokenId.
     */
    public void setIntId(int intId) {
        this.intId = intId;
        // Skip this numeric intId
        languageData.skipIntId(intId);
    }
    
    /**
     * @return name of the static field for this tokenId
     *  in the generated language class.
     */
    public String getFieldName() {
        return LexerGenUtilities.idToUpperCase(getName());
    }

    /**
     * @return name of the static field for the integer constant
     *  for this tokenId in the generated language class.
     */
    public String getIntIdFieldName() {
        return getFieldName() + "_INT";
    }
    
    /**
     * Assign unique int identification by using
     * {@link LanguageData#uniqueIntId()}.
     */
    public void assignUniqueIntId() {
        setIntId(languageData.uniqueIntId());
    }

    /**
     * @return name of the field in the token-types class
     * (e.g. xxxConstants for javacc or xxxTokenTypes for antlr)
     * that corresponds to this token id or null
     * if this token-id does not have any associated field
     * in the token-types class.
     */
    public String getTokenTypeName() {
        return tokenTypeName;
    }
    
    public void updateByTokenType(String tokenTypeName) {
        this.tokenTypeName = tokenTypeName;
        languageData.getTokenTypes().updateId(this);
    }
    
    /**
     * @return list of category names to which this token belongs.
     * The list can be modified by adding/removing category names.
     */
    public List getCategoryNames() {
        return categoryNames;
    }
    
    /**
     * @return whether this tokenId is case insensitive
     * which means that when adding a new sampleText
     * by {@link addSampleText(String)} then
     * the corresponding upper-case and lower-case
     * representations are added too.
     * <BR>For "html" sample text the "HTML"
     * will be added too.
     * <BR>For "Begin" sample text the "begin" and "BEGIN"
     * will be added too.
     *
     * <P>The value of this flag is not transferred
     * into resulting {@link org.netbeans.api.lexer.TokenId}
     * in any way.
     */
    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    /**
     * Set whether this tokenId is case insensitive or not.
     * @param caseInsensitive whether this tokenId is case insensitive.
     *  If the value is true then all the existing sample texts
     *  will be re-added so the number of the sample
     *  texts can grow as explained in {@link #isCaseInsensitive()}.
     * @see #isCaseInsensitive()
     */
    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
        
        if (caseInsensitive) { // re-add the existing samples
            Iterator currentSamplesIterator = new ArrayList(sampleTexts).iterator();
            sampleTexts.clear();
            while (currentSamplesIterator.hasNext()) {
                addSampleText((String)currentSamplesIterator.next());
            }
        }
    }

    /** Add a sample text to the tokenId.
     * @param sampleText sample text to add to the current list of sample texts.
     */
    public void addSampleText(String sampleText) {
        addUniqueSampleText(sampleText);
        
        if (caseInsensitive) {
            addUniqueSampleText(sampleText.toLowerCase());
            addUniqueSampleText(sampleText.toUpperCase());
        }
    }

    private void addUniqueSampleText(String sampleText) {
        if (sampleTexts.indexOf(sampleText) == -1) {
            sampleTexts.add(sampleText);
        }
    }
    
    /** Get the sample texts list.
     */
    public List getSampleTexts() {
        return sampleTexts;
    }
    
    /**
     * Clear all existing sample texts.
     */
    public void resetSamples() {
        sampleTexts.clear();
    }
    
    public String getSampleTextCheck() {
        return sampleTextCheck;
    }
    
    public void setSampleTextCheck(String sampleTextCheck) {
        this.sampleTextCheck = sampleTextCheck;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getName());
        sb.append(", intId=");
        sb.append(getIntId());
        sb.append(", tokenType=");
        sb.append(getTokenTypeName());
        List samples = getSampleTexts();
        int samplesCount = samples.size();
        for (int i = 0; i < samplesCount; i++) {
            if (i == 0) {
                sb.append(", samples={");
            }
            sb.append('"');
            sb.append(samples.get(i));
            sb.append('"');
            if (i == samplesCount - 1) {
                sb.append('}');
            }
        }
        sb.append(", sampleTextCheck=");
        sb.append(getSampleTextCheck());
        
        return sb.toString();
    }

}
