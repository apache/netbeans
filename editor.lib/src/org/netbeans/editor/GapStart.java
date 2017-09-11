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

package org.netbeans.editor;

/**
 * A given object can publish this interface if it allows
 * an efficient access to its gap-based data storage
 * and wants to give its clients a hint about how to access
 * the data efficiently.
 * <P>For example {@link javax.swing.text.Document} instance
 * having gap-based document content can allow to get an instance
 * of GapStart as a property:<PRE>
 *      GapStart gs = (GapStart)doc.getProperty(GapStart.class);
 *      int gapStart = gs.getGapStart();
 * <PRE>
 * Once the start of the gap is known the client can optimize
 * access to the document's data. For example if the client
 * does not care about the chunks in which it gets the document's data
 * it can access the characters so that no character copying is done:<PRE>
 *      Segment text = new Segment();
 *      doc.getText(0, gapStart, text); // document's data below gap
 *      ...
 *      doc.getText(gapStart, doc.getLength(), text); // document's data over gap
 *      ...
 * <PRE>
 *
 * @author Miloslav Metelka
 * @version 1.00
 * @deprecated deprecated without replacement. Possibly use document's view as CharSequence
 *  by {@link org.netbeans.lib.editor.util.swing.DocumentUtilities#getText(javax.swing.text.Document)}.
 */

public interface GapStart {

    /**
     * Get the begining of the gap in the object's gap-based data.
     * @return &gt;=0 and &lt;= total size of the data of the object.
     */
    public int getGapStart();

}
