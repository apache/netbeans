/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.analysis.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class Line2Offset {

    private int[] lines;

    public Line2Offset(FileObject fileObject) {
        try {
            init(fileObject);
        } catch (IOException ex) {
            lines = new int[]{};
        }
    }

    private void init(FileObject fileObject) throws IOException {
        List<Integer> lso = new LinkedList<Integer>();
        lso.add(0);
        int currentPosition = 0;
        for(String s : fileObject.asLines(getEncoding(fileObject))) {
             currentPosition += s.length();
             currentPosition++;
            lso.add(currentPosition);
        }
        lines = new int[lso.size()+1];
        int idx = 0;
        for (Integer offset : lso) {
            lines[idx++] = offset;
        }
        lines[idx]=currentPosition;
    }
    
    private String getEncoding(FileObject fo) {
        Charset cs = null;
        if (fo != null && fo.isValid()) {
            cs = FileEncodingQuery.getEncoding(fo);
        }
        if (cs == null) {
            cs = FileEncodingQuery.getDefaultEncoding();
        }
        return cs.name();
    }

    public int[] getLineOffset(int line) {
        line--;
        if (line < lines.length) {
            int start = lines[line];
            if (line+1 < lines.length) {
                int end = lines[line+1];
                return  new int[]{start,end};
            }
            return  new int[]{start,start+1};
        }
        return new int[]{0,1};
    }

    public int getLineByOffset(int offset) {
        int low = 0;
        int high = lines.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = lines[mid];
            if (midVal < offset) {
                if (low == high) {
                    return low + 1;
                }
                low = mid + 1;
            } else if (midVal > offset) {
                if (low == high) {
                    return low;
                }
                high = mid - 1;
            } else {
                return mid + 1;
            }
        }
        return low;
    }

}
