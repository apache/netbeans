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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.spi.queries;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.netbeans.modules.queries.UnknownEncoding;
import org.openide.filesystems.FileObject;

/**
 * Information about encoding of a file.
 * <p>
 * A default implementations are registered by the
 * <code>org.netbeans.modules.projectapi</code> module which firstly looks up the
 * implementation of this interface in the <code>DataObject</code> lookup. When
 * available it delegates to it. When the implementation isn't available in the
 * <code>DataObject</code> lookup or it returns null it tries to find a
 * project corresponding to the file and checks whether that project has an
 * implementation of this interface in its lookup. If so, it delegates to
 * that implementation. Therefore it is not generally necessary
 * for a project type provider nor data loader to register its own global implementation of
 * this query.
 * </p>
 * <div class="nonnormative">
 * <p>
 * Typical implementation returns a {@link Charset} for recognized file. The
 * implementation which needs to analyze the content of the file (XML, HTML) 
 * should implement a subclass of the {@link Charset} and provide own {@link CharsetEncoder}
 * end {@link CharsetDecoder} which buffer the input up to 4 KB and either delegate
 * to the correct {@link Charset} when the encoding is found in the buffer or signal 
 * that they are not able to process the file and other {@link Charset} should be used
 * by calling the {@link FileEncodingQueryImplementation#throwUnknownEncoding} method.
 * </p>
 * </div>
 * @since org.netbeans.modules.queries/1 1.9
 * @author Tomas Zezula
 */
public abstract class FileEncodingQueryImplementation {
    
    /**
     * Returns encoding of a given file or folder. For folders the default encoding for the whole subtree
     * shall be returned (if known).
     * @param file to find an encoding for
     * @return encoding which should be used for given file
     * or null when nothing is known about the file encoding.
     */
    public abstract Charset getEncoding (FileObject file);
    
    /**
     * By calling this method the {@link CharsetEncoder} and 
     * {@link CharsetDecoder} signal that they are not able to handle
     * the document and the other {@link Charset} should be used. This
     * method may be called during processing the first 4 KB of data and
     * before any output has been written.
     */
    protected static void throwUnknownEncoding() {
        throw new UnknownEncoding ();
    }
    
}
