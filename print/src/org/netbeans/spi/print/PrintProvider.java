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
 * License. When distributing the software, include this License Header
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
package org.netbeans.spi.print;

import java.util.Date;

/**
 * Print provider is the collection of the pages
 * to be printed, where collection is the 2D matrix.
 *
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.24
 */
public interface PrintProvider {

    /**
     * Returns print pages being shown and printed.
     * The pages will be shown in the Print Preview dialog
     * as 2D matrix, e.g. page <code>pages[1][2]</code> will
     * be shown in second row and third column in the dialog.
     *
     * @param width specifies the width of pages in pixels.
     * @param height specifies the height of pages in pixels.
     * @param zoom specifies the scale of pages.
     * The zoom can take positive double value:
     * <code>0.2</code> means <code>20%</code>,
     * <code>1.0</code> - <code>100%</code>,
     * <code>3.1415</code> - <code>314.5%</code> etc.
     *
     * @return pages being printed for the given width, height and zoom
     */
    PrintPage[][] getPages(int width, int height, double zoom);

    /**
     * Indicates the name of the document being printed which
     * will be shown in the header/footer. By default, the
     * name is shown in the left part of the header.
     * @return name of the document which can be used in header/footer
     */
    String getName();

    /**
     * Indicates the time at which the user last made a modification to
     * the document, diagram, etc. being printed which might affect its
     * printed appearance. The document might not have been saved since then.
     * @return time at which the printable document was last changed
     */
    Date lastModified();
}
