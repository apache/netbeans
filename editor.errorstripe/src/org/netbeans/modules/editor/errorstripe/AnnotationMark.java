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

package org.netbeans.modules.editor.errorstripe;

import java.awt.Color;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationMark implements Mark {

    private AnnotationDesc annotation;

    /** Creates a new instance of AnnotationMark */
    public AnnotationMark(AnnotationDesc annotation) {
        this.annotation = annotation;
    }

    public Status getStatus() {
        AnnotationType type = annotation.getAnnotationTypeInstance();
//        System.err.println("type = " + type );
        AnnotationType.Severity severity = type.getSeverity();
        
//        System.err.println("severity = " + severity );
        
        return AnnotationViewDataImpl.get(severity);
    }

    public Color getEnhancedColor() {
        AnnotationType type = annotation.getAnnotationTypeInstance();
        
        return type.getCustomSidebarColor();
    }

    public int[] getAssignedLines() {
        return new int[] {annotation.getLine(), annotation.getLine()};
    }

    public String getShortDescription() {
        return annotation.getShortDescription();
    }

    public int getType() {
        return TYPE_ERROR_LIKE;
    }
    
    public int getPriority() {
        return annotation.getAnnotationTypeInstance().getPriority();
    }

    public AnnotationDesc getAnnotationDesc() {
        return annotation;
    }
    
}
