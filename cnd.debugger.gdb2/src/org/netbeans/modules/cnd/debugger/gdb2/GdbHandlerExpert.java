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

package org.netbeans.modules.cnd.debugger.gdb2;

import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

import org.netbeans.modules.cnd.debugger.common2.values.Action;
import org.netbeans.modules.cnd.debugger.common2.values.FunctionSubEvent;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.Handler;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerCommand;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.HandlerExpert;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpointType;

import org.netbeans.modules.cnd.debugger.common2.debugger.Address;
import org.netbeans.modules.cnd.debugger.common2.debugger.Constants;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.ExceptionBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.ExceptionBreakpointType;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.FunctionBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.InstructionBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.SysCallBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.SysCallBreakpointType;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.VariableBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.utils.props.Property;

import org.netbeans.modules.cnd.debugger.gdb2.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

public class GdbHandlerExpert implements HandlerExpert {
    private static final boolean sendShortPaths = Boolean.getBoolean("gdb.breakpoints.shortpaths"); // NOI18N

    // "infinity" for gdb is largest signed 32bit number:
    static final Integer infinity = new Integer(0x7fffffff);

    private final GdbDebuggerImpl debugger;

    private static final Logger LOG = Logger.getLogger(GdbHandlerExpert.class.toString());

    public GdbHandlerExpert(GdbDebuggerImpl debugger) {
        this.debugger = debugger;
    }

    Handler newHandler(NativeBreakpoint template,
		       MIResult result,
		       NativeBreakpoint breakpoint) {
        //assertBkptResult(result);
        MIValue bkptValue = result.value();
	MITList props = bkptValue.asTuple();

	if (breakpoint == null) {
	    breakpoint = createBreakpoint(props, template);
	} else {
	    assert ! breakpoint.hasHandler();
	}
	update(template, breakpoint, props);
	Handler handler = new Handler(debugger, breakpoint);
	setGenericProperties(handler, props);

        if (! (result.matches(GdbDebuggerImpl.MI_BKPT) || result.matches(GdbDebuggerImpl.MI_WPT))) {
            handler.setError(Catalog.get("MSG_InvalidLocation")); //NOI18N
        }

	return handler;
    }

    Handler replaceHandler(NativeBreakpoint template,
                           Handler originalHandler, MIResult result, NativeBreakpoint... targetTemplate) {
        //assertBkptResult(result);
        if ( !(result.matches(GdbDebuggerImpl.MI_BKPT) || result.matches(GdbDebuggerImpl.MI_WPT)) ) {
            if (targetTemplate.length == 1) {

                String newLine = targetTemplate[0].getPos().propertyByName("lineNumber").toString(); //NOI18N
                originalHandler.breakpoint().getPos().propertyByName("lineNumber").setFromString(newLine); //NOI18N
                originalHandler.setError(Catalog.get("MSG_InvalidLocation")); //NOI18N
            }
            return originalHandler;
        }
	MIValue bkptValue = result.value();
	MITList props = bkptValue.asTuple();

	NativeBreakpoint breakpoint = originalHandler.breakpoint();
	update(template, breakpoint, props);
	Handler handler = new Handler(debugger, breakpoint);
	setGenericProperties(handler, props);
	return handler;
    }

    private static void assertBkptResult(MIResult result) {
        assert result.variable().equals(GdbDebuggerImpl.MI_BKPT) ||
                result.variable().equals(GdbDebuggerImpl.MI_WPT) : "Result " + result + " is not a breakpoint"; //NOI18N
    }

    @Override
    public ReplacementPolicy replacementPolicy() {
	return ReplacementPolicy.EXPLICIT;
    }

    // interface HandlerExpert
    @Override
    public Handler childHandler(NativeBreakpoint bpt) {
	NativeBreakpoint breakpoint;
	if (bpt.isToplevel()) {
	    breakpoint = bpt.makeSubBreakpointCopy();
	} else {
	    breakpoint = bpt;
	}
	Handler handler = new Handler(debugger, breakpoint);
	return handler;
    }

    private void appendCommandStart(StringBuilder cmd, NativeBreakpoint breakpoint) {
        cmd.append("-break-insert"); //NOI18N
        cmd.append(debugger.getGdbVersionPeculiarity().breakPendingFlag());

	if (breakpoint.getTemp()) {
	    cmd.append(" -t");					// NOI18N
        }

	if (breakpoint.getCondition() != null) {
	    cmd.append(" -c ").append(quote(breakpoint.getCondition()));	// NOI18N
        }

	if (breakpoint.hasCountLimit()) {
	    if (breakpoint.getCountLimit() == -1) {
		cmd.append(" -i ").append(infinity);			// NOI18N
	    } else {
		cmd.append(" -i ").append(breakpoint.getCountLimit() - 1);	// NOI18N
	    }
	}

	// -p doesn't seem to be documented
	if (breakpoint.getThread() != null) {
	    cmd.append(" -p ").append(breakpoint.getThread());		// NOI18N
        }

	if (!breakpoint.isEnabled()) {
            cmd.append(debugger.getGdbVersionPeculiarity().breakDisabledFlag());
        }
    }

    // interface HandlerExpert
    @Override
    public HandlerCommand commandFormNew(NativeBreakpoint breakpoint) {

	//
	// First, weed out options gdb doesn't support
        // but do not fail, see IZ 191537
        //
	if (breakpoint.getAction() != Action.STOP) {
            LOG.warning(Catalog.get("MSG_OnlyStopGdb")); // NOI18N
//	    return HandlerCommand.makeError(Catalog.get("MSG_OnlyStopGdb")); // NOI18N
        }

        if (breakpoint.getBreakpointType() instanceof SysCallBreakpointType && !debugger.getGdbVersionPeculiarity().isSyscallBreakpointsSupported()) {
            return  HandlerCommand.makeError(null);
        }

	if (!IpeUtils.isEmpty(breakpoint.getWhileIn())) {
            LOG.warning(Catalog.get("MSG_NoWhileGdb")); // NOI18N
//	    return HandlerCommand.makeError(Catalog.get("MSG_NoWhileGdb")); // NOI18N
        }

	if (!IpeUtils.isEmpty(breakpoint.getLwp())) {
            LOG.warning(Catalog.get("MSG_NoLwpGdb")); // NOI18N
//	    return HandlerCommand.makeError(Catalog.get("MSG_NoLwpGdb")); // NOI18N
        }

        StringBuilder cmd = new StringBuilder();

	Class<?> bClass = breakpoint.getClass(); // dynamic type
	if (bClass == LineBreakpoint.class) {
	    LineBreakpoint lb = (LineBreakpoint) breakpoint;

	    String file = lb.getFileName();
	    int line = lb.getLineNumber();

	    file = debugger.localToRemote("LineBreakpoint", file); // NOI18N

            // unify separators
            file = file.replace("\\","/"); //NOI18N

	    // switch to short paths if spaces detected
            if (sendShortPaths || file.indexOf(' ') != -1) {
                String baseDir = debugger.getNDI().getConfiguration().getBaseDir().replace("\\", "/"); //NOI18N
                if (file.startsWith(baseDir + '/')) {
                    file = file.substring(baseDir.length() + 1);
                } else {
                    file = CndPathUtilities.getBaseName(file);
                }
            }

	    String fileLine;
	    if (file != null && file.length() > 0) {
		fileLine = file + ":" + line;	// NOI18N
	    } else {
		fileLine = "" + line;		// NOI18N
	    }
            appendCommandStart(cmd, breakpoint);
	    cmd.append(" \"").append(fileLine).append('"'); // NOI18N

	} else if (bClass == InstructionBreakpoint.class) {
	    InstructionBreakpoint ib = (InstructionBreakpoint) breakpoint;
            appendCommandStart(cmd, breakpoint);
	    cmd.append(" *").append(ib.getAddress()); // NOI18N

	} else if (bClass == FunctionBreakpoint.class) {
	    FunctionBreakpoint fb = (FunctionBreakpoint) breakpoint;
	    FunctionSubEvent se = fb.getSubEvent();

	    String function = null;
	    if (se.equals(FunctionSubEvent.IN)) {
		function = fb.getFunction();
	    } else if (se.equals(FunctionSubEvent.INFUNCTION)) {
		// Not supported
		return HandlerCommand.makeError(null);
	    } else if (se.equals(FunctionSubEvent.RETURNS)) {
		// Not supported
		return HandlerCommand.makeError(null);
	    }

            appendCommandStart(cmd, breakpoint);
	    // MI -break-insert doesn't like like spaces in function names.
	    // Surrounding teh whole function signature with quotes seems
	    // to help.
	    cmd.append(" \"").append(function).append('"'); // NOI18N
        } else if (bClass == VariableBreakpoint.class) {
            VariableBreakpoint vb = (VariableBreakpoint) breakpoint;
            cmd.append("-break-watch "); //NOI18N
            cmd.append(vb.getVariable());
        } else if (bClass == ExceptionBreakpoint.class) {
            ExceptionBreakpoint eb = (ExceptionBreakpoint) breakpoint;
            cmd.append("catch throw"); //NOI18N
        } else if (bClass == SysCallBreakpoint.class) {
            SysCallBreakpoint sb = (SysCallBreakpoint) breakpoint;
            cmd.append("catch syscall "); //NOI18N
            String sysCall = sb.getSysCall();
            if (sysCall != null) {
                cmd.append(sysCall);
            }
	} else {
	    return HandlerCommand.makeError(null);
	}

	return new GdbHandlerCommand(GdbHandlerCommand.Type.REPLACE, cmd.toString());
    }

    // interface HandlerExpert
    @Override
    public HandlerCommand commandFormCustomize(final NativeBreakpoint clonedBreakpoint,
		                       final NativeBreakpoint repairedBreakpoint) {
        Set<Property> diff = NativeBreakpoint.diff(repairedBreakpoint, clonedBreakpoint);
        GdbHandlerCommand cmd = null;
        for (final Property property : diff) {
            GdbHandlerCommand old = cmd;
            if (Constants.PROP_BREAKPOINT_COUNTLIMIT.equals(property.key())) {
                String value = "0"; //NOI18N
                if (property.getAsObject() != null) {
                    value = property.getAsObject().toString();
                }
                cmd = new GdbHandlerCommand(GdbHandlerCommand.Type.CHANGE,
                        "-break-after " + repairedBreakpoint.getId() + //NOI18N
                        ' ' + value) {
                            @Override
                            void onDone() {
                                repairedBreakpoint.setCountLimit(clonedBreakpoint.getCountLimit(), clonedBreakpoint.hasCountLimit());
                                repairedBreakpoint.update();
                            }
                        };
                cmd.setNext(old);
            } else if (Constants.PROP_BREAKPOINT_CONDITION.equals(property.key())) {
                String value = "";
                if (property.getAsObject() != null) {
                    value = property.getAsObject().toString();
                }
                cmd = new GdbHandlerCommand(GdbHandlerCommand.Type.CHANGE,
                        "-break-condition " + repairedBreakpoint.getId() + //NOI18N
                        ' ' + value) {
                            @Override
                            void onDone() {
                                repairedBreakpoint.setCondition(clonedBreakpoint.getCondition());
                                repairedBreakpoint.update();
                            }
                        };
                cmd.setNext(old);
            } else {
                return commandFormNew(clonedBreakpoint);
            }
        }
        if (cmd != null) {
            return cmd;
        } else {
            return commandFormNew(clonedBreakpoint);
        }
    }

    private static NativeBreakpoint createBreakpoint(MITList results,
			   NativeBreakpoint template) {
	NativeBreakpointType type = null;

        if (template != null) {
            type = template.getBreakpointType();
        } else {
            if ("catchpoint".equals(results.getConstValue("type"))) { //NOI18N
                type = new SysCallBreakpointType();
            } else {
                type = new ExceptionBreakpointType();
            }
        }

	NativeBreakpoint newBreakpoint = null;
	if (type != null)
	    newBreakpoint = type.newInstance(NativeBreakpoint.SUBBREAKPOINT);

	return newBreakpoint;
    }

    private void update(NativeBreakpoint template,
			NativeBreakpoint breakpoint,
			MITList props) {
	breakpoint.removeAnnotations();
	setGenericProperties(breakpoint, props);
	setSpecificProperties(template, breakpoint, props);
    }

    private void setGenericProperties(Handler handler, MITList props) {
	// enabled
	String enabledString = props.getConstValue("enabled", "y"); // NOI18N
        handler.setEnabled("y".equals(enabledString)); //NOI18N

	// 'number'
	int number = Integer.parseInt(props.getConstValue("number", "0")); // NOI18N
	handler.setId(number);
    }

    private void setGenericProperties(NativeBreakpoint breakpoint, MITList props) {
	// temporary
	String dispString = props.getConstValue("disp"); // NOI18N
        breakpoint.setTemp("del".equals(dispString)); //NOI18N

	// count
	MIValue ignoreValue = props.valueOf("ignore");		// NOI18N
	if (ignoreValue != null) {
	    String ignoreString = ignoreValue.asConst().value();
	    long ignore = Long.parseLong(ignoreString);
	    // our bpt view converts -1 to a literal "infinity".
	    if (ignore == infinity)
		ignore = -1;
	    else
		ignore++;
	    breakpoint.setCountLimit(ignore, true);
	} else {
	    breakpoint.setCountLimit(0, false);
	}

	// thread
        breakpoint.setThread(props.getConstValue("thread", null)); //NOI18N

	// condition
        breakpoint.setCondition(props.getConstValue("cond", null)); //NOI18N

	// action
	Action action = Action.STOP;
	breakpoint.setAction(action);
    }

    private String getFileName(MITList props,
			       NativeBreakpoint originalBreakpoint) {
	String filename = null;
	// 'fullname' (try it first but it's not always available)
	MIValue fullnameValue = props.valueOf("fullname"); // NOI18N
        String fullnameString = null;
        if (fullnameValue == null) {
            // try pending
            fullnameValue = props.valueOf("pending"); // NOI18N
            if (fullnameValue != null) {
                fullnameString = fullnameValue.asConst().value();
                // remove line number
                int pos = fullnameString.lastIndexOf(':'); //NOI18N
                if (pos != -1) {
                    fullnameString = fullnameString.substring(0, pos);
                }
            }
        } else {
            fullnameString = fullnameValue.asConst().value();
        }
	if (fullnameString != null) {
	    fullnameString = debugger.remoteToLocal("getFileName", fullnameString); // NOI18N

            // convert to world
            fullnameString = debugger.fmap().engineToWorld(fullnameString);
	    filename = fullnameString;
	} else {
	    // 'file'
	    MIValue fileValue = props.valueOf("file"); // NOI18N
	    if (fileValue == null) {
		return originalBreakpoint.getPos().propertyByName("fileName").toString(); // NOI18N
            }
//	    String fileString = fileValue.asConst().value();

	    // 'file' property is just a basename and rather useless ...
	    // Extract original full filename from command:
	    if (originalBreakpoint instanceof LineBreakpoint) {
		LineBreakpoint olb = (LineBreakpoint) originalBreakpoint;
		filename = olb.getFileName();
            }
	}
	return filename;
    }

    private static int getLine(MITList props, NativeBreakpoint originalBreakpoint) {
	MIValue lineValue = props.valueOf("line"); // NOI18N
        String lineString;
	if (lineValue == null) {
            // try pending
            MIValue fullnameValue = props.valueOf("pending"); // NOI18N
            if (fullnameValue == null) {
                Property lineProperty = originalBreakpoint.getPos().propertyByName("lineNumber"); // NOI18N
                if (lineProperty == null) {
                    return 0;
                } else {
                    return Integer.parseInt(lineProperty.toString());
                }
            }
            String lineStr = fullnameValue.asConst().value();
            // remove line number
            int pos = lineStr.lastIndexOf(':'); //NOI18N
            if (pos != -1) {
                lineString = lineStr.substring(pos+1);
            } else {
                return 0;
            }
        } else {
            lineString = lineValue.asConst().value();
        }

        try {
            return Integer.parseInt(lineString);
        } catch (NumberFormatException numberFormatException) {
            //do nothing
        }
	return 0;
    }

    private static long getAddr(MITList props) {
	MIValue addrValue = props.valueOf("addr"); // NOI18N
	if (addrValue == null) {
	    return 0;
        }
	String addrString = addrValue.asConst().value();
        try {
            return Address.parseAddr(addrString);
        } catch (Exception e) {
            // can happen if addr=<PENDING> for example
            return 0;
        }
    }

    private void setSpecificProperties(NativeBreakpoint template,
				       NativeBreakpoint breakpoint,
				       MITList props) {


	if (template instanceof LineBreakpoint) {
	    LineBreakpoint lb = (LineBreakpoint) breakpoint;

	    String filename = getFileName(props, template);
	    int line = getLine(props, template);

	    lb.setFileAndLine(filename, line);

	} else if (template instanceof FunctionBreakpoint) {
	    FunctionBreakpoint fb = (FunctionBreakpoint) breakpoint;

            // All of this does not work for gdb, see IZ 195311
//	    MIValue funcValue = props.valueOf("func"); // NOI18N
//	    String funcString;
//	    if (funcValue != null) {
//		funcString = funcValue.asConst().value();
//            } else {
//		// We'll get an 'at' instead of a 'func' if there's
//		// no src debugging information at the given function.
//		MIValue atValue = props.valueOf("at"); // NOI18N
//		if (atValue != null) {
//		    funcString = atValue.asConst().value();
//
//		    // usually of the form
//		    // "<strdup+4>"
//		    // (but sometimes of the form "strdup@plt")
//
//		    // clean out <
//		    if (funcString.startsWith("<")) // NOI18N
//			funcString = funcString.substring(1);
//
//		    // clean out >
//		    int gtx = funcString.indexOf('>');
//		    if (gtx != -1)
//			funcString = funcString.substring(0, gtx);
//
//		    // clean out +4
//		    int plx = funcString.indexOf('+');
//		    if (plx != -1)
//			funcString = funcString.substring(0, plx);
//
//		} else {
//		    funcString = ((FunctionBreakpoint)template).getFunction();
//		}
//	    }

            MIValue funcValue = props.valueOf("func"); // NOI18N
            String funcString = ( funcValue == null ? null : funcValue.toString() );
            if (funcString == null) {
                funcString = ((FunctionBreakpoint) template).getFunction();
            }

            fb.setFunction(IpeUtils.unquoteIfNecessary(funcString));

	} else if (template instanceof InstructionBreakpoint) {
	    InstructionBreakpoint ib = (InstructionBreakpoint) breakpoint;

	    long addr = getAddr(props);

	    ib.setAddress(Address.toHexString0x(addr, true));
	} else if (template instanceof VariableBreakpoint) {
	    VariableBreakpoint vb = (VariableBreakpoint) breakpoint;

            String exp = props.getConstValue("exp"); //NOI18N
	    vb.setVariable(exp);
	}
    }

    void addAnnotations(Handler handler,
			       NativeBreakpoint breakpoint,
			       NativeBreakpoint template,
			       MIResult result) {
        //assertBkptResult(result);
	MIValue bkptValue = result.value();
	MITList props = bkptValue.asTuple();

	int line = getLine(props, template);
        String fileName = (line != 0) ? getFileName(props, template) : null;
	long addr = getAddr(props);
	// TMP if (line != 0 && fileName != null)
	{
	    if (fileName == null) { //|| !fileName.startsWith("/"))
		line = 0;
            }
	    handler.breakpoint().addAnnotation(fileName, line, addr);
	}
    }

    /**
     * Put quotes around a string and escape internal quotes.
     * <p>
     * Converts a string of the form
     *		strcmp(x, "hello")
     * to
     *		"strcmp(x, \"hello\")"
     */
    private static String quote(String in) {
	StringBuilder out = new StringBuilder();
	out.append('"');
	for (int sx = 0; sx < in.length(); sx++) {
	    char c = in.charAt(sx);
	    if (c == '"')
		out.append('\\');
	    out.append(c);
	}
	out.append('"');
	return out.toString();
    }

}
