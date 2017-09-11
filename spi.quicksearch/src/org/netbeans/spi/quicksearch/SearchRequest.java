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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.quicksearch;

import java.util.List;
import javax.swing.KeyStroke;
import org.netbeans.modules.quicksearch.Accessor;

/**
 * Description of quick search request.
 * 
 * Implementors of {@link SearchProvider} are expected to get information from
 * SearchRequest instance and perform search appropriately in 
 * {@link SearchProvider#evaluate} method.
 *
 * @author Dafe Simonek
 */
public final class SearchRequest {
    
    static {
        // init of accessor implementation, part of Accessor pattern
        Accessor.DEFAULT = new AccessorImpl();
    }    
    
    /** Text to search for */
    private String text;
    
    /** Shortcut to search for */
    private List <? extends KeyStroke> stroke;

    SearchRequest (String text, List<? extends KeyStroke> stroke) {
        this.text = text;
        this.stroke = stroke;
    }

    /**
     * Access to text used for searching. Can be null if shortcut was entered by
     * user instead of plain text.
     * 
     * @return Text entered by user into Quick Search UI or null.
     */
    public String getText () {
        return text;
    }
       
    /**
     * Access to shortcut used for searching. Can be null if plain text was
     * entered by user instead of shortcut.
     * 
     * @return Shortcut entered by user into Quick Search UI or null.
     */
    public List<? extends KeyStroke> getShortcut () {
        return stroke;
    }

}
