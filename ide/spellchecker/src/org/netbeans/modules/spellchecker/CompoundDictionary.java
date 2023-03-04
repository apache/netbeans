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
package org.netbeans.modules.spellchecker;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.spellchecker.spi.dictionary.Dictionary;
import org.netbeans.modules.spellchecker.spi.dictionary.ValidityType;

/**
 *
 * @author Jan Lahoda
 */
public class CompoundDictionary implements Dictionary {

    private static final Logger LOGGER = Logger.getLogger(CompoundDictionary.class.getName());
    private Dictionary[] delegates;
    
    private CompoundDictionary(Dictionary... delegates) {
        this.delegates = delegates.clone();
    }
    
    public static Dictionary create(Dictionary... delegates) {
        return new CompoundDictionary(delegates);
    }
    
    public ValidityType validateWord(CharSequence word) {
        ValidityType result = ValidityType.INVALID;
        
        for (Dictionary d : delegates) {
            ValidityType thisResult = d.validateWord(word);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "validating word \"{0}\" using dictionary {1}, result: {2}", new Object[] {word, d.toString(), thisResult});
            }

            if (thisResult == ValidityType.VALID || thisResult == ValidityType.BLACKLISTED) {
                return thisResult;
            }
            
            if (thisResult == ValidityType.PREFIX_OF_VALID && result == ValidityType.INVALID) {
                result = ValidityType.PREFIX_OF_VALID;
            }
        }
        
        return result;
    }

    public List<String> findValidWordsForPrefix(CharSequence word) {
        List<String> result = new LinkedList<String>();
        
        for (Dictionary d : delegates) {
            result.addAll(d.findValidWordsForPrefix(word));
        }
        
        return result;
    }

    public List<String> findProposals(CharSequence word) {
        List<String> result = new LinkedList<String>();
        
        for (Dictionary d : delegates) {
            result.addAll(d.findProposals(word));
        }
        
        return result;
    }

}
