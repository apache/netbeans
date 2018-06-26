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
package org.netbeans.modules.php.dbgp.annotations;

import org.openide.text.Annotatable;
import org.openide.text.Annotation;

/**
 * Debugger Annotation class.
 *
 * @author ads
 */
public abstract class DebuggerAnnotation extends Annotation {
    /**
     * Annotation type constants.
     */
    public static final String CURRENT_LINE_ANNOTATION_TYPE2 = "CurrentPC2"; //NOI18N
    public static final String CURRENT_LINE_PART_ANNOTATION_TYPE = "CurrentPCLinePart"; //NOI18N
    public static final String CURRENT_LINE_PART_ANNOTATION_TYPE2 = "CurrentPC2LinePart"; //NOI18N
    private String myMessage;

    public DebuggerAnnotation(Annotatable annotatable) {
        attach(annotatable);
    }

    public DebuggerAnnotation(Annotatable annotatable, String message) {
        this(annotatable);
        myMessage = message;
    }

    /**
     * <pre>
     * The type returned should correspond to "name" of annotation.
     * Name of annotation is defined in layer.xml , section :
     * "<folder name="AnnotationTypes">".
     * Each annotation has its xml file with annotation properties.
     * Annotaitons could be user defined ( as PHPError, PHPWarning and PHPNotice )
     * and defined in other ( f.e. debugger code module ).
     * There are a lot of annotaions available in
     * org.netbeans.modules.debugger.resources package.
     * All types except  PHPError, PHPWarning and PHPNotice are got from there.
     *
     * </pre>
     *
     * @see org.openide.text.Annotation#getAnnotationType()
     */
    @Override
    public abstract String getAnnotationType();

    @Override
    public String getShortDescription() {
        return myMessage;
    }

}
