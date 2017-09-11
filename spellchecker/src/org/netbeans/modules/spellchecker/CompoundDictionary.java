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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
