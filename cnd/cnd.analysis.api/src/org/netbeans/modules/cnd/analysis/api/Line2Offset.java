/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
