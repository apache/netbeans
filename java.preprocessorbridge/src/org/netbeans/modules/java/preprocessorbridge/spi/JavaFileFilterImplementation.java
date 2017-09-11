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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.preprocessorbridge.spi;

import java.io.Reader;
import java.io.Writer;
import javax.swing.event.ChangeListener;

/**
 * This interface in a friend contract among the j2me project and java/source
 * module. The implementation preprocesses the java file content when it's red by the
 * java infrastructure if needed. From the performance reasons there can be just one
 * implementation of this interface for all sources in the project.
 * 
 * @author Tomas Zezula
 */
public interface JavaFileFilterImplementation {
        
    /**
     * Filters an {@link Reader} by the preprocessor.
     * Called when the file is read from the disk before its content is passed to javac.
     * PRE: The input parameter is never null
     * POST: The returned Reader is not null
     * Threading: The implementor is responsible for concurrent safety, tail call hand off protocol is suggested.
     * @param r {@link Reader} to be preprocessed
     * @return an preprocessed {@link Reader}
     */
    public Reader filterReader (Reader r);
    
    /**
     * Filters an input {@link CharSequence} by the preprocessor. From the performance reason
     * it's highly recommended to implement the method using decorator pattern.
     * Called before the content of the editor is passed to the javac.
     * PRE: The input parameter is never null
     * POST: The returned CharSequence is not null
     * Threading: The implementor is responsible for concurrent safety, tail call hand off protocol is suggested.
     * @param charSequence {@link CharSequence} to be preprocessed
     * @return an preprocessed {@link CharSequence}
     */
    public CharSequence filterCharSequence (CharSequence charSequence);
        
    /**
     * Filters an {@link Writer} by the preprocessor.
     * Called before the file is written to the disk.
     * PRE: The input parameter is never null
     * POST: The returned Writer is not null
     * Threading: The implementor is responsible for concurrent safety, tail call hand off protocol is suggested.
     * @param w {@link Writer} to be preprocessed
     * @return an preprocessed {@link Writer}
     */
    public Writer filterWriter (Writer w);
    
    /**
     * Adds an {@link ChangeListener} to the {@link JavaFileFilterImplementation}
     * The implementor should fire a change when the rules for preprocessing has changed
     * and files should be rescanned.
     * @param listener to be added
     */
    public void addChangeListener (ChangeListener listener);
    

    /**
     * Removes an {@link ChangeListener} to the {@link JavaFileFilterImplementation}
     * The implementor should fire a change when the rules for preprocessing has changed
     * and files should be rescanned.
     * @param listener to be removed
     */
    public void removeChangeListener (ChangeListener listener);
}
