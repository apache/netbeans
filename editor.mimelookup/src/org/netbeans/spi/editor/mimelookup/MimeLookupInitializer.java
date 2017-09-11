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

package org.netbeans.spi.editor.mimelookup;

import org.openide.util.Lookup;

/**
 *  Provides an initialization of MimeLookup on either global or mime-type
 *  specific level.
 *  <br>
 *  The implementations of this class should be registed to default lookup using {@link org.openide.util.lookup.ServiceProvider}.
 *  <br>
 *  Such registered instance serves as a global level initializer
 *  which can further be asked for children by {@link #child(String)}
 *  which will lead to forming of a tree initializers hierarchy.
 *  <br>
 *  The contents provided by {@link #lookup()} of the global-level initializer
 *  (the one registered in the layer) will automatically appear
 *  in all the results returned by <code>MimeLookup</code> for any particular mime type.
 *  <br>
 *  Once someone asks for a <code>MimeLookup</code> for a specific mime-type
 *  by using {@link org.netbeans.api.editor.mimelookup.MimeLookup#getMimeLookup(String)}
 *  the global level initializer will be asked for {@link #child(String)}
 *  and the {@link #lookup()} on the returned children
 *  will define the result data (together with the global-level initializer's lookup).
 *  <br>
 *  This process can be arbitrarily nested for embedded mime-types.
 *  
 * <p> 
 *  An example implementation of MimeLookupInitializer
 *  that works over xml layer file system can be found at mime lookup module
 *  implementation <a href="http://editor.netbeans.org/source/browse/editor/mimelookup/src/org/netbeans/modules/editor/mimelookup/Attic/LayerMimeLookupImplementation.java">LayerMimeLookupInitializer</a>
 *
 *  @author Miloslav Metelka, Martin Roskanin
 *  @deprecated Use {@link MimeDataProvider} instead.
 */
@Deprecated
public interface MimeLookupInitializer {

    /**
     * Lookup providing mime-type sensitive or global-level data
     * depending on which level this initializer is defined.
     * 
     * @return Lookup or null, if there are no lookup-able objects for mime or global level.
     */
    Lookup lookup();
    
    /**
     * Retrieves a Lookup.Result of MimeLookupInitializers for the given sub-mimeType.
     *
     * @param mimeType mime-type string representation e.g. "text/x-java"
     * @return non-null lookup result of MimeLookupInitializer(s).
     *  <br/>
     *  Typically there should be just one child initializer although if there
     *  will be more than one all of them will be taken into consideration.
     *  <br/>
     *  If there will be no specific initializers for the particular mime-type
     *  then an empty result should be returned.
     */
    Lookup.Result<MimeLookupInitializer> child(String mimeType);

}
