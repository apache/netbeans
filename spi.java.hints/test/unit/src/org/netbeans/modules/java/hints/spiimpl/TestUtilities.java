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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.spiimpl;

import junit.framework.Assert;

/**
 *
 * @author Jan Lahoda
 */
public class TestUtilities {

    private TestUtilities() {
    }

    public static String detectOffsets(String source, int[] positionOrSpan) {
        return detectOffsets(source, positionOrSpan, "\\|");
    }

    public static String detectOffsets(String source, int[] positionOrSpan, String delimiter) {
        //for now, the position/span delimiter is '|', without possibility of escaping:
        String[] split = source.split(delimiter);
        
        Assert.assertTrue("incorrect number of position markers (|)", positionOrSpan.length == split.length - 1);
        
        StringBuilder sb = new StringBuilder();
        int index = 0;
        int offset = 0;
        
        for (String s : split) {
            sb.append(s);
            if (index < positionOrSpan.length)
                positionOrSpan[index++] = (offset += s.length());
        }
        
        return sb.toString();
    }

}
