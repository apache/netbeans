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
package org.netbeans.modules.java.source.save;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.PositionConverter;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.PositionRefProvider;
import org.netbeans.modules.java.source.save.CasualDiff.Diff;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class DiffUtilities {
    private static final Logger LOG = Logger.getLogger(DiffUtilities.class.getName());
    
    public static List<ModificationResult.Difference> diff2ModificationResultDifference(FileObject fo, PositionConverter converter, Map<Integer, String> userInfo, String originalCode, String newCode, Source src) throws IOException, BadLocationException {
        return diff2ModificationResultDifference(fo, converter, userInfo, originalCode, diff(originalCode, newCode, 0), src);
    }

    public static List<Diff> diff(String origContent, String newContent, int offset) {
        return diff(origContent, newContent, offset, null, offset);
    }
    
    public static List<Diff> diff(String origContent, String newContent, int offset, int[] sections, int lineStart) {
        List<Diff> diffs = new ArrayList<Diff>();
        new DiffFacility(diffs).withSections(sections, lineStart).makeListMatch(origContent, newContent, offset);
        return diffs;
    }
    
    public static List<ModificationResult.Difference> diff2ModificationResultDifference(FileObject fo, PositionConverter converter, Map<Integer, String> userInfo, String content, List<Diff> diffs, Source src) throws IOException, BadLocationException {
        diffs.sort((Diff o1, Diff o2) -> o1.getPos() - o2.getPos());

        Rewriter out = new Rewriter(fo, converter, userInfo, src);
        char[] buf = content.toCharArray();

        // Copy any leading comments.
        for (Diff d : diffs) {
            switch (d.type) {
                case INSERT:
                    out.copyTo(d.getPos());
                    out.writeTo(d.getText());
                    break;
                case DELETE:
                    out.copyTo(d.getPos());
                    out.skipThrough(buf, d.getEnd());
                    break;
                default:
                    throw new AssertionError("unknown CasualDiff type: " + d.type);
            }
        }

        return out.diffs;
    }


    // Innerclasses ------------------------------------------------------------
    private static class Rewriter {

        private int offset = 0;
        private PositionRefProvider prp;
        private PositionConverter converter;
        public List<ModificationResult.Difference> diffs = new LinkedList<ModificationResult.Difference>();
        private Map<Integer, String> userInfo;
        private final Source src;
        
        public Rewriter(FileObject fo, PositionConverter converter, Map<Integer, String> userInfo, Source src) throws IOException {
            this.src = src;
            this.converter = converter;
            this.userInfo = userInfo;
            if (fo != null) {
                prp = PositionRefProvider.get(fo);
            }
            if (prp == null)
                throw new IOException("Could not find CloneableEditorSupport for " + FileUtil.getFileDisplayName (fo)); //NOI18N
        }

        public void writeTo(String s) throws IOException, BadLocationException {
            ModificationResult.Difference diff = diffs.size() > 0 ? diffs.get(diffs.size() - 1) : null;
            if (diff != null && diff.getKind() == ModificationResult.Difference.Kind.REMOVE && diff.getEndPosition().getOffset() == offset) {
                diffs.remove(diffs.size() - 1);
                diffs.add(JavaSourceAccessor.getINSTANCE().createDifference(ModificationResult.Difference.Kind.CHANGE, diff.getStartPosition(), diff.getEndPosition(), diff.getOldText(), s, diff.getDescription(), src));
            } else {
                int off = converter != null ? converter.getOriginalPosition(offset) : offset;
                if (off >= 0) {
                    diffs.add(JavaSourceAccessor.getINSTANCE().createDifference(ModificationResult.Difference.Kind.INSERT, prp.createPosition(off, Bias.Forward), prp.createPosition(off, Bias.Backward), null, s, userInfo.get(offset), src));
                }
            }
        }

        public void skipThrough(char[] in, int pos) throws IOException, BadLocationException {
            String origText = new String(in, offset, pos - offset);
            org.netbeans.api.java.source.ModificationResult.Difference diff = diffs.size() > 0 ? diffs.get(diffs.size() - 1) : null;
            if (diff != null && diff.getKind() == org.netbeans.api.java.source.ModificationResult.Difference.Kind.INSERT && diff.getStartPosition().getOffset() == offset) {
                diffs.remove(diffs.size() - 1);
                diffs.add(JavaSourceAccessor.getINSTANCE().createDifference(ModificationResult.Difference.Kind.CHANGE, diff.getStartPosition(), diff.getEndPosition(), origText, diff.getNewText(), diff.getDescription(), src));
            } else {
                int off = converter != null ? converter.getOriginalPosition(offset) : offset;
                if (off >= 0) {
                    diffs.add(JavaSourceAccessor.getINSTANCE().createDifference(ModificationResult.Difference.Kind.REMOVE, prp.createPosition(off, Bias.Forward), prp.createPosition(off + origText.length(), Bias.Backward), origText, null, userInfo.get(offset), src));
                }
            }
            offset = pos;
        }

        public void copyTo(int pos) throws IOException {
            offset = pos;
        }
    }
}
