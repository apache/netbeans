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
package org.netbeans.modules.gsf.testrunner.api;

/**
 * Enums for representing status of a test case or suite.
 * 
 * @author Erno Mononen
 */
public enum Status {

    PASSED(1,"00CC00"), PENDING(1<<1,"800080"), FAILED(1<<2,"FF0000"), ERROR(1<<3,"FF0000"), ABORTED(1<<4,"D69D29"), SKIPPED(1<<5,"585858"), PASSEDWITHERRORS(1<<6,"00CC00"), IGNORED(1<<7,"000000"); //NOI18N

    private final int bitMask;
    private final String displayColor;

    private Status(int bitMask, String displayColor) {
        this.bitMask = bitMask;
        this.displayColor = displayColor;
    }

    /**
     * @return the bit mask for this status.
     */
    public int getBitMask(){
        return bitMask;
    }

    /**
     * @return the html display color for this status.
     */
    public String getHtmlDisplayColor() {
        return displayColor;
    }

    /**
     * @return true if the given status represents a failure or an error.
     */
    public static boolean isFailureOrError(Status status) {
        return FAILED.equals(status) || ERROR.equals(status);
    }

    /**
     * @return true if the given status represents a skipped test.
     */
    public static boolean isSkipped(Status status) {
        return SKIPPED.equals(status);
    }

    /**
     * @return true if the given status represents an aborted test.
     */
    public static boolean isAborted(Status status) {
        return ABORTED.equals(status);
    }

    /**
     *
     * @return true if the given mask is applied in this status.
     */
    public boolean isMaskApplied(int mask){
        return (mask & getBitMask()) != 0;
    }

}
