/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.editor;

import java.util.Iterator;
import java.util.List;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;

/**
 * Various internal tests
 *
 * @author Miloslav Metelka
 * @version 0.10
 * @deprecated Should never have been made public. No replacement.
 */
@Deprecated
public class EditorDebug {

    private EditorDebug() {
        // instance creation has no sense
    }

    public static void dumpPlanes(BaseDocument doc) {
        /*
        Class markClasses[] = new Class[] {
                                  MarkFactory.LineMark.class,
                                  MarkFactory.CaretMark.class,
                                  MarkFactory.DrawMark.class,
                                  MarkFactory.SyntaxMark.class,
                                  Mark.class
                              };
        char markChars[] = new char[] {
                               'L', 'C', 'D', 'S', 'B'
                           };
        System.out.println("--------------------------- DUMP OF MARK PLANES --------------------------------"); // NOI18N
        System.out.println("Mark legend:\nD - DrawMark\n" // NOI18N
                           + "S - SyntaxMark\nB - BaseMark\n" // NOI18N
                           + "L - LineMark\nC - CaretMark"); // NOI18N
        System.out.println(doc.op.markPlanesToString(markClasses, markChars));
        System.out.println("--------------------------------------------------------------------------------\n"); // NOI18N
         */
    }

    public static void dumpSyntaxMarks(final BaseDocument doc) {
        System.out.println("--------------------------- DUMP OF SYNTAX MARKS --------------------------------"); // NOI18N
        final int docLen = doc.getLength();
        // Suspended because of docmarks rewrite due to #11692
/*        doc.op.renderMarks(
            new DocMarks.Renderer() {
                public void render() {
                    int markCnt = getMarkCnt();
                    int index = 0;
                    int pos = 0;
                    int lastPos = pos;
                    int lastMarkPos = 0;
                    int maxMarkDistance = 0;
                    int minMarkDistance = docLen;
                    Mark markArray[] = getMarkArray();
                    SyntaxSeg.Slot slot = SyntaxSeg.getFreeSlot();
                    Syntax syntax = doc.getFreeSyntax();

                    try {
                        syntax.load(null, slot.array, 0, 0, false, 0);
                        while (index < markCnt) {
                            Mark mark = markArray[index++];
                            pos += getRelPos(mark);
                            if (mark instanceof MarkFactory.SyntaxMark) {
                                MarkFactory.SyntaxMark syntaxMark = (MarkFactory.SyntaxMark)mark;
                                int delta = pos - lastMarkPos;
                                if (delta > maxMarkDistance) {
                                    maxMarkDistance = delta;
                                }
                                if (delta < minMarkDistance) {
                                    minMarkDistance = delta;
                                }
                                lastMarkPos = pos;

                                int preScan = syntax.getPreScan();
                                int loadPos = lastPos - preScan;
                                int scanLen = pos - loadPos;
                                try {
                                    slot.load(doc, loadPos, scanLen);
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                                syntax.relocate(slot.array, slot.offset + preScan,
                                                scanLen - preScan, (pos == docLen), pos);

                                while (syntax.nextToken() != null) { }

                                lastPos = pos;
                                try {
                                    System.out.println(((syntaxMark == doc.op.eolMark)
                                                        ? "!!EOLMark!!" : "syntaxMark:") // NOI18N
                                                       + " getOffset()=" + Utilities.debugPosition(doc, syntaxMark.getOffset()) // NOI18N
                                                       + ", getLine()=" + syntaxMark.getLine() // NOI18N
                                                       + ", " + syntaxMark // NOI18N
                                                       + ",\n    StateInfo=" + syntaxMark.getStateInfo() // NOI18N
                                                       + ",\n    Syntax:" + syntax); // NOI18N
                                } catch (InvalidMarkException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } finally {
                        doc.releaseSyntax(syntax);
                        SyntaxSeg.releaseSlot(slot);
                    }

                    System.out.println("Maximum mark distance is " + maxMarkDistance // NOI18N
                                       + "\nMinimum mark distance is " + minMarkDistance); // NOI18N
                }
            }
        );
 */
        System.out.println("--------------------------------------------------------------------------------\n"); // NOI18N
    }

    public static void test(JTextComponent component) {
        /*
        BaseTextUI ui = ((BaseTextUI)component.getUI());
        BaseView view = (BaseView)ui.getRootView(component);
        view = (BaseView)view.getView(0);
        final BaseDocument doc = (BaseDocument)component.getDocument();
        EditorUI editorUI = ui.getEditorUI();
        final int docLen = doc.getLength();

        System.out.println("\n------------------------- Registry --------------------------------"); // NOI18N
        System.out.println(Registry.registryToString());

        System.out.println("\n------------------------- DEBUGGING INFORMATION --------------------------------"); // NOI18N
        String buf = "Document: " + doc // NOI18N
                     + "\nview.mainHeight=" + ((LeafView)view).mainHeight // NOI18N
                     + "\nDoubleBuffering=" + component.isDoubleBuffered(); // NOI18N
        buf += "\ncomponent.getLocation()=" + component.getLocation() // NOI18N
               + "\ncomponent.getSize()=" + component.getSize() // NOI18N
               + "\nvirtualSize=" + editorUI.virtualSize; // NOI18N
        buf += "\nEditorUI LAYERS:\n" + editorUI.getDrawLayerList(); // NOI18N
        System.out.println(buf);

        System.out.println(doc.op.infoToString());

        buf = "\n------------------------ CR occurence test ------------------------\n"; // NOI18N
        try {
            char chars[] = doc.getChars(0, docLen);
            int i;
            for (i = 0; i < docLen; i++) {
                if (chars[i] == '\r') {
                    buf += "CR found at pos=" + i + ", line=" + doc.op.getLine(i) + "\n"; // NOI18N
                    break;
                }
            }
            if (i == docLen) {
                buf += "No CR found. CR occurence test suceeded."; // NOI18N
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        System.out.println(buf);
         */
    }

    public static void checkSettings(Class kitClass) throws Exception {
//        int readBufferSize = SettingsUtil.getInteger(kitClass, SettingsNames.READ_BUFFER_SIZE,
//                             SettingsDefaults.defaultReadBufferSize);
//
//        int writeBufferSize = SettingsUtil.getInteger(kitClass, SettingsNames.WRITE_BUFFER_SIZE,
//                              SettingsDefaults.defaultWriteBufferSize);
//
//        int readMarkDistance = SettingsUtil.getInteger(kitClass, SettingsNames.READ_MARK_DISTANCE,
//                               SettingsDefaults.defaultReadMarkDistance);
//
//        int markDistance = SettingsUtil.getInteger(kitClass, SettingsNames.MARK_DISTANCE,
//                           SettingsDefaults.defaultMarkDistance);
//
//        int maxMarkDistance = SettingsUtil.getInteger(kitClass, SettingsNames.MAX_MARK_DISTANCE,
//                              SettingsDefaults.defaultMaxMarkDistance);
//
//        int minMarkDistance = SettingsUtil.getInteger(kitClass, SettingsNames.MIN_MARK_DISTANCE,
//                              SettingsDefaults.defaultMinMarkDistance);
//
//        int syntaxUpdateBatchSize = SettingsUtil.getInteger(kitClass, SettingsNames.SYNTAX_UPDATE_BATCH_SIZE,
//                                    SettingsDefaults.defaultSyntaxUpdateBatchSize);
//
//
//        // Now perform checks
//        if (maxMarkDistance < markDistance) {
//            throw new Exception("maxMarkDistance=" + maxMarkDistance // NOI18N
//                            + " < markDistance=" + markDistance); // NOI18N
//        }
//
//        if (markDistance < minMarkDistance) {
//            throw new Exception("markDistance=" + markDistance // NOI18N
//                            + " < minMarkDistance=" + minMarkDistance); // NOI18N
//        }
//
//        if (readMarkDistance < minMarkDistance) {
//            throw new Exception("readMarkDistance=" + readMarkDistance // NOI18N
//                            + " < minMarkDistance=" + minMarkDistance); // NOI18N
//        }
//
//        if (syntaxUpdateBatchSize < maxMarkDistance) {
//            throw new Exception("syntaxUpdateBatchSize=" + syntaxUpdateBatchSize // NOI18N
//                            + " < maxMarkDistance=" + maxMarkDistance); // NOI18N
//        }
    }

    /** Replace '\n', '\r' and '\t' in the string so they are identifiable. */
    public static String debugString(String s) {
        return (s != null) ? debugChars(s.toCharArray(), 0, s.length())
            : "NULL STRING"; // NOI18N
    }

    public static String debugChars(Segment seg) {
        return debugChars(seg.array, seg.offset, seg.count);
    }

    public static String debugChars(char chars[]) {
        return debugChars(chars, 0, chars.length);
    }

    /** Replace '\n', '\r' and '\t' in the char array so they are identifiable. */
    public static String debugChars(char chars[], int offset, int len) {
        if (len < 0) {
            return "EditorDebug.debugChars() !ERROR! len=" + len + " < 0"; // NOI18N
        }
        if (offset < 0) {
            return "EditorDebug.debugChars() !ERROR! offset=" + offset + " < 0"; // NOI18N
        }
        if (offset + len > chars.length) {
            return "EditorDebug.debugChars() !ERROR! offset=" + offset + " + len=" + len // NOI18N
                   + " > chars.length=" + chars.length; // NOI18N
        }
        StringBuffer sb = new StringBuffer(len);
        int endOffset = offset + len;
        for (; offset < endOffset; offset++) {
            switch (chars[offset]) {
                case '\n':
                    sb.append("\\n"); // NOI18N
                    break;
                case '\t':
                    sb.append("\\t"); // NOI18N
                    break;
                case '\r':
                    sb.append("\\r"); // NOI18N
                    break;
                default:
                    sb.append(chars[offset]);
            }
        }
        return sb.toString();
    }

    public static String debugChar(char ch) {
        switch (ch) {
            case '\n':
                return "\\n"; // NOI18N
            case '\t':
                return "\\t"; // NOI18N
            case '\r':
                return "\\r"; // NOI18N
            default:
                return String.valueOf(ch);
        }
    }

    public static String debugPairs(int[] pairs) {
        String ret;
        if (pairs == null) {
            ret = "Null pairs"; // NOI18N
        } else if (pairs.length == 0) {
            ret = "No pairs"; // NOI18N
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < pairs.length; i += 2) {
                sb.append('[');
                sb.append(pairs[i]);
                sb.append(", "); // NOI18N
                sb.append(pairs[i + 1]);
                if (i < pairs.length - 1) {
                    sb.append("]\n"); // NOI18N
                }
            }
            ret = sb.toString();
        }

        return ret;
    }

    public static String debugArray(Object[] array) {
        String ret;
        if (array == null) {
            ret = "Null array"; // NOI18N
        } else if (array.length == 0) {
            ret = "Empty array"; // NOI18N
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; i++) {
                sb.append('[');
                sb.append(i);
                sb.append("]="); // NOI18N
                sb.append(array[i]);
                if (i != array.length - 1) {
                    sb.append('\n');
                }
            }
            ret = sb.toString();
        }
        return ret;
    }

    public static String  debugArray(int[] array) {
        String ret;
        if (array == null) {
            ret = "Null array"; // NOI18N
        } else if (array.length == 0) {
            ret = "Empty array"; // NOI18N
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; i++) {
                sb.append('[');
                sb.append(i);
                sb.append("]="); // NOI18N
                sb.append(array[i]);
                if (i != array.length - 1) {
                    sb.append('\n');
                }
            }
            ret = sb.toString();
        }
        return ret;
    }

    public static String debugBlocks(BaseDocument doc, int[] blocks) {
        String ret;
        if (blocks == null) {
            ret = "Null blocks"; // NOI18N
        } else if (blocks.length == 0) {
            ret = "Empty blocks"; // NOI18N
        } else if (blocks.length % 2 != 0) {
            ret = "Blocks.length=" + blocks.length + " is not even!"; // NOI18N
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < blocks.length; i += 2) {
                sb.append('[');
                sb.append(i);
                sb.append("]=("); // NOI18N
                sb.append(blocks[i]);
                sb.append(", "); // NOI18N
                sb.append(blocks[i + 1]);
                sb.append(") or ("); // NOI18N
                sb.append(Utilities.debugPosition(doc, blocks[i]));
                sb.append(", "); // NOI18N
                sb.append(Utilities.debugPosition(doc, blocks[i + 1]));
                sb.append(')');

                if (i != blocks.length - 1) {
                    sb.append('\n');
                }
            }
            ret = sb.toString();
        }
        return ret;
    }

    public static String debugList(List l) {
        String ret;
        if (l == null) {
            ret = "Null list"; // NOI18N
        } else if (l.size() == 0) {
            ret = "Empty list"; // NOI18N
        } else {
            int cnt = l.size();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < cnt; i++) {
                sb.append('[');
                sb.append(i);
                sb.append("]="); // NOI18N
                sb.append(l.get(i));
                if (i != cnt - 1) {
                    sb.append('\n');
                }
            }
            ret = sb.toString();
        }
        return ret;
    }

    public static String debugIterator(Iterator i) {
        String ret;
        if (i == null) {
            ret = "Null iterator"; // NOI18N
        } else if (!i.hasNext()) {
            ret = "Empty iterator"; // NOI18N
        } else {
            StringBuffer sb = new StringBuffer();
            int ind = 0;
            while (i.hasNext()) {
                sb.append('[');
                sb.append(ind++);
                sb.append("]="); // NOI18N
                sb.append(i.next().toString());
                if (i.hasNext()) {
                    sb.append('\n');
                }
            }
            ret = sb.toString();
        }
        return ret;
    }

}
