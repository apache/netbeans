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

package org.netbeans.modules.form.layoutdesign;

/**
 * @author Tomas Pavek
 */

public interface LayoutConstants {

    // size constants

    /**
     * Indicates a size or position not defined in the layout model; it must
     * be obtained elsewhere (e.g. from a real component).
     */
    int NOT_EXPLICITLY_DEFINED = -1;

    /**
     * Specifies that a min or max size value should be the same as
     * the preferred size value.
     */
    int USE_PREFERRED_SIZE = -2;

    /**
     * Possible types of preferred default gaps (paddings).
     */
    enum PaddingType { RELATED, UNRELATED, INDENT, SEPARATE }
    // keep this order for old tests using int (so the index matches the old number)

    final PaddingType[] PADDINGS = { PaddingType.RELATED, PaddingType.UNRELATED,
                                     PaddingType.INDENT, PaddingType.SEPARATE };

    // structure type constants

    /**
     * Indicates a single layout interval without internal structure.
     */
    int SINGLE = 101;

    /**
     * Indicates a layout interval containing a sequence of sub-intervals
     * (placed one after anoother).
     */
    int SEQUENTIAL = 102;

    /**
     * Indicates a layout interval containing sub-intervals arranged parallely.
     */
    int PARALLEL = 103;

    // alignment constants (independent on orientation/axis)
    // also serves the role of index to array of positions

    int DEFAULT = -1;
    int LEADING = 0;
    int TRAILING = 1;
    int CENTER = 2;
    int BASELINE = 3;

    // orientation constants (dimensions)

    /**
     * Constant/index of the horizontal orientation (X axis).
     */
    int HORIZONTAL = 0;

    /**
     * Constant/index of the vertical orientation (Y axis).
     */
    int VERTICAL = 1;

    /**
     * The number of dimensions. Obviously 2 ;)
     */
    int DIM_COUNT = 2;

    // other constants

//    int MAX_OUT = Short.MAX_VALUE;
//    int MIN_OUT = Short.MIN_VALUE;
    String PROP_HORIZONTAL_MIN_SIZE = "horizontalMinSize"; // NOI18N
    String PROP_HORIZONTAL_PREF_SIZE = "horizontalPrefSize"; // NOI18N
    String PROP_HORIZONTAL_MAX_SIZE = "horizontalMaxSize"; // NOI18N
    String PROP_VERTICAL_MIN_SIZE = "verticalMinSize"; // NOI18N
    String PROP_VERTICAL_PREF_SIZE = "verticalPrefSize"; // NOI18N
    String PROP_VERTICAL_MAX_SIZE = "verticalMaxSize"; // NOI18N

    // are components in same linksizegroup?
    int INVALID = -1;
    int FALSE = 0;
    int TRUE = 1;
}
