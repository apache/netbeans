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

package org.netbeans.modules.cnd.debugger.gdb2;

import java.awt.Dialog;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.Frame;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerImpl;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.BreakpointModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.DisProgressPanel;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.Disassembly;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public class GdbDisassembly extends Disassembly {
    private String functionName = "";
    private String intFileName = "";
    private String resolvedFileName = "";
    private String address = "";
    private boolean withSource = true;

    private final Map<Integer,String> regNames = new HashMap<Integer,String>();
    private final Map<Integer,String> regValues = new HashMap<Integer,String>();
    private final Set<Integer> regModified = new  HashSet<Integer>();

    private static final String ADDRESS_HEADER="address"; // NOI18N
    private static final String FUNCTION_HEADER="func-name"; // NOI18N
    private static final String OFFSET_HEADER="offset"; // NOI18N
    private static final String INSTR_HEADER="inst"; // NOI18N
    private static final String LINE_HEADER="line"; // NOI18N
    private static final String FILE_HEADER="file"; // NOI18N
    private static final String NUMBER_HEADER="number"; // NOI18N
    private static final String VALUE_HEADER="value"; // NOI18N

    public static final String REGISTER_NAMES_HEADER="^done,register-names="; // NOI18N
    public static final String REGISTER_VALUES_HEADER="^done,register-values="; // NOI18N
    public static final String REGISTER_MODIFIED_HEADER="^done,changed-registers="; // NOI18N
    public static final String RESPONSE_HEADER="^done,asm_insns="; // NOI18N
    private static final String COMBINED_HEADER="src_and_asm_line={"; // NOI18N

    private static final String COMMENT_PREFIX="!"; // NOI18N

    private boolean cancelled = false;

    public GdbDisassembly(NativeDebuggerImpl debugger, BreakpointModel breakpointModel) {
        super(debugger, breakpointModel);
    }

    protected void cancel() {
        cancelled = true;
    }

    public void update(String msg) {
        assert msg.contains(RESPONSE_HEADER) : "Invalid asm response message"; // NOI18N
        cancelled = false;

        Dialog dialog = null;

        Frame frame = getDebugger().getCurrentFrame();
        if (frame == null) {
            return;
        }

        String currentAddr = getDebugger().getCurrentFrame().getCurrentPC();

        attachUpdateListener();

        DisText text = new DisText();

        int pos = RESPONSE_HEADER.length();
        boolean nameSet = false;

        long start = System.currentTimeMillis();
        boolean dialogOpened = false;
        DisProgressPanel panel = null;

        for (;!cancelled;) {
            int combinedPos = msg.indexOf(COMBINED_HEADER, pos);
            int addressPos = msg.indexOf(ADDRESS_HEADER, pos);

            try {
                if (panel != null) {
                    panel.setProgress(pos*100/msg.length());
                }
                if (!cancelled && !dialogOpened && System.currentTimeMillis() - start > 2000) {
                    dialogOpened = true;
                    panel = new DisProgressPanel();
                    final DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(GdbDisassembly.class, "DIS_PROGRESS_TITLE")); // NOI18N
                    dd.setOptions(new Object[]{DialogDescriptor.CANCEL_OPTION});
                    dialog = DialogDisplayer.getDefault().createDialog(dd);
                    final Dialog dlg = dialog;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dlg.setVisible(true);
                            if (dd.getValue() == DialogDescriptor.CANCEL_OPTION) {
                                cancel();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }

            if (addressPos == -1) {
                break;
            }

            if (combinedPos != -1 && combinedPos < addressPos) {
                int lineIdx = Integer.parseInt(readValue(LINE_HEADER, msg, combinedPos));
                if (lineIdx > 0) {
                    //String path = debugger.getRunDirectory();
                    String fileStr = readValue(FILE_HEADER, msg, combinedPos);
                    if (resolvedFileName != null && CndPathUtilities.getBaseName(resolvedFileName).equals(fileStr)) {
                        FileObject src_fo = EditorBridge.findFileObject(resolvedFileName, getDebugger());
                        if (src_fo != null && src_fo.isValid()) {
                            try {
                                String lineText = DataObject.find(src_fo).getCookie(LineCookie.class).getLineSet().getCurrent(lineIdx-1).getText();
                                if (lineText != null && lineText.length() > 0) {
                                    text.addLine(new CommentLine(COMMENT_PREFIX + lineText));
                                }
                            } catch (Exception ex) {
                                // do nothing
                            }
                        } else {
                            text.addLine(new CommentLine(COMMENT_PREFIX + NbBundle.getMessage(GdbDisassembly.class, "MSG_Source_Not_Found", fileStr, lineIdx))); // NOI18N
                        }
                    }
                }
                pos = combinedPos+1;
            } else {
                // read instruction in this line
                GdbDisLine line = new GdbDisLine(msg, addressPos);
                if (!nameSet && currentAddr.equals(line.getAddress())) {
                    functionName = line.getFunction();
                    getDataObject().getNodeDelegate().setDisplayName(getHeader());
                    text.addLine(new CommentLine(functionName + "()\n")); // NOI18N
                    nameSet = true;
                }
                if (!nameSet || functionName.equals(line.getFunction())) {
                    text.addLine(line);
                }
                pos = addressPos+1;
            }
        }
        if (!cancelled) {
            text.save();
        }

        if (dialog != null) {
            final Dialog dlg = dialog;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    dlg.setVisible(false);
                    dlg.dispose();
                }
            });
        }

        if (cancelled) {
            close();
            return;
        }
        // If we got empty dis try to reload without source line info
        if (text.isEmpty() && withSource) {
            reloadDis(false, true);
            return;
        }
        setText(text);
    }

    public void updateRegNames(String msg) {
        assert msg.startsWith(REGISTER_NAMES_HEADER) : "Invalid asm response message"; // NOI18N
        regNames.clear();
        int idx = 0;
        int pos = msg.indexOf('"', REGISTER_NAMES_HEADER.length()); // NOI18N
        while (pos != -1) {
            int end = msg.indexOf('"', pos+1); // NOI18N
            if (end == -1) {
                break;
            }
            String value = msg.substring(pos+1, end);
            regNames.put(idx++, value);
            pos = msg.indexOf('"', end+1); // NOI18N
        }
    }

    public void updateRegModified(String msg) {
        assert msg.startsWith(REGISTER_MODIFIED_HEADER) : "Invalid asm response message"; // NOI18N
        regModified.clear();
        int pos = msg.indexOf('"', REGISTER_MODIFIED_HEADER.length()); // NOI18N
        while (pos != -1) {
            int end = msg.indexOf('"', pos+1); // NOI18N
            if (end == -1) {
                break;
            }
            String index = msg.substring(pos+1, end);
            try {
                regModified.add(Integer.valueOf(index));
            } catch (NumberFormatException nfe) {
                //do nothing
            }
            pos = msg.indexOf('"', end+1); // NOI18N
        }
        //RegisterValuesProvider.getInstance().fireRegisterValuesChanged();
    }

    public void updateRegValues(String msg) {
        assert msg.startsWith(REGISTER_VALUES_HEADER) : "Invalid asm response message"; // NOI18N
        regValues.clear();
        int pos = msg.indexOf(NUMBER_HEADER);
        while (pos != -1) {
            String idx = readValue(NUMBER_HEADER, msg, pos);
            String value = readValue(VALUE_HEADER, msg, pos);
            try {
                regValues.put(Integer.valueOf(idx), value);
            } catch (NumberFormatException nfe) {
                // do nothing
            }
            pos = msg.indexOf(NUMBER_HEADER, pos+1);
        }
        // Todo: we know that updated registers will fire the update, but better make it updated at one piece
        //RegisterValuesProvider.getInstance().fireRegisterValuesChanged();
    }

//    public List<org.netbeans.modules.cnd.debugger.common.disassembly.RegisterValue> getRegisterValues() {
//        List<org.netbeans.modules.cnd.debugger.common.disassembly.RegisterValue> res = new ArrayList<org.netbeans.modules.cnd.debugger.common.disassembly.RegisterValue>();
//        for (Integer idx : regValues.keySet()) {
//            String name = regNames.get(idx);
//            if (name == null) {
//                log.severe("Unknown register: " + idx); // NOI18N
//                name = String.valueOf(idx);
//            }
//            res.add(new org.netbeans.modules.cnd.debugger.common.disassembly.RegisterValue(name, regValues.get(idx), regModified.contains(idx)));
//        }
//        return res;
//    }

    @Override
    public void stateUpdated() {
        reloadDis(true, false);
    }

    @Override
    protected void reload() {
        reloadDis(true, false);
    }

    private void reloadDis(boolean withSource, boolean force) {
        this.withSource = withSource;
        if (!opened) {
            reloadFailed();
            return;
        }
        Frame frame = getDebugger().getCurrentFrame();
        if (frame == null) {
            reloadFailed();
            return;
        }
        String curAddress = frame.getCurrentPC();
        if (curAddress == null || curAddress.length() == 0) {
            reloadFailed();
            return;
        }

        if (!curAddress.equals(address)) {
            requestMode = withSource ? RequestMode.FILE_SRC : RequestMode.FILE_NO_SRC;
        } else if (requestMode == RequestMode.NONE) {
            reloadFailed();
            return;
        }

        if (force || getAddressLine(curAddress) == -1) {
            intFileName = ((GdbFrame)frame).getEngineFullName();
            resolvedFileName = frame.getFullPath();
            if ((intFileName == null || intFileName.length() == 0) &&
                    (requestMode == RequestMode.FILE_SRC || requestMode == RequestMode.FILE_NO_SRC)) {
                requestMode = withSource ? RequestMode.ADDRESS_SRC : RequestMode.ADDRESS_NO_SRC;
            }
            switch (requestMode) {
                case FILE_SRC:
                    getDebugger().disController().requestDis(withSource);
                    requestMode = RequestMode.FILE_NO_SRC;
                    break;
                case FILE_NO_SRC:
                    //debugger.getGdbProxy().data_disassemble(intFileName, frame.getLineNo(), withSource);
                    getDebugger().disController().requestDis(withSource);
                    requestMode = RequestMode.ADDRESS_SRC;
                    break;
                case ADDRESS_SRC:
                    getDebugger().disController().requestDis("$pc", 100, withSource); //NOI18N
                    //debugger.getGdbProxy().data_disassemble(1000, withSource);
                    requestMode = RequestMode.ADDRESS_NO_SRC;
                    break;
                case ADDRESS_NO_SRC:
                    getDebugger().disController().requestDis("$pc", 100, withSource); //NOI18N
                    //debugger.getGdbProxy().data_disassemble(1000, withSource);
                    requestMode = RequestMode.NONE;
                    break;
            }
        }

        address = curAddress;
    }

    /*public String getNextAddress(String address) {
        //TODO : can use binary search
        synchronized (lines) {
            for (Iterator<Line> iter = lines.iterator(); iter.hasNext();) {
                Line line = iter.next();
                if (line.address.equals(address)) {
                    // Fix for IZ:131372 (Step Over doesn't work in Disasm)
                    // return next address only for call instructions
                    if (line.instruction.startsWith("call") && iter.hasNext()) { // NOI18N
                        return iter.next().address;
                    }
                    return "";
                }
            }
            return "";
        }
    }*/

    /*
     * Reads expressions like param="value"
     * in this case readValue("param") will return "value"
     */
    private static String readValue(String name, String msg, int pos) {
        String paramHeader = name + "=\""; // NOI18N
        int start = msg.indexOf(paramHeader, pos);
        if (start != -1) {
            start += paramHeader.length();
            int end = msg.indexOf('"', start + 1); // NOI18N
            if (end != -1) {
                return msg.substring(start, end);
            }
        }
        return "";
    }

    private String getHeader() {
        String res = NbBundle.getMessage(GdbDisassembly.class, "LBL_Disassembly_Window"); // NOI18N
        if (functionName.length() > 0) {
            res += "(" + functionName + ")"; // NOI18N
        }
        return res;
    }

    @Override
    public void reset() {
        address = "";
        super.reset();
    }

    private static class GdbDisLine implements DisLine {
        private final String address;
        private final String function;
        private final int offset;
        private final String instruction;
        private int idx;

        public GdbDisLine(String msg, int pos) {
            this.address = readValue(ADDRESS_HEADER, msg, pos);
            this.function = readValue(FUNCTION_HEADER, msg, pos);
            int tmpOffset = 0;
            try {
                tmpOffset = Integer.parseInt(readValue(OFFSET_HEADER, msg, pos));
            } catch (Exception e) {
                //do nothing
            }
            this.offset = tmpOffset;
            String inst = readValue(INSTR_HEADER, msg, pos);
            if (inst.contains("\\t")) { //NOI18N
                inst = inst.replace("\\t", "\t"); //NOI18N
            }
            this.instruction = inst;
        }

        @Override
        public String getAddress() {
            return address;
        }

        public String getFunction() {
            return function;
        }

        @Override
        public int getIdx() {
            return idx;
        }

        @Override
        public void setIdx(int idx) {
            this.idx = idx;
        }

        @Override
        public String toString() {
            //return function + "+" + offset + ": (" + address + ") " + instruction; // NOI18N
            if (!function.isEmpty()) {
                return function + "+" + offset + ": " + instruction + '\n'; // NOI18N
            } else {
                return address + ": " + instruction + '\n'; // NOI18N
            }
        }
    }
}
