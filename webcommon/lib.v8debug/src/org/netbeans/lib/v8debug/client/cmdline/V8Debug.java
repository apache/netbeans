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
package org.netbeans.lib.v8debug.client.cmdline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Breakpoint;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.V8StepAction;
import org.netbeans.lib.v8debug.connection.ClientConnection;
import org.netbeans.lib.v8debug.commands.Backtrace;
import org.netbeans.lib.v8debug.commands.ChangeBreakpoint;
import org.netbeans.lib.v8debug.commands.ClearBreakpoint;
import org.netbeans.lib.v8debug.commands.Continue;
import org.netbeans.lib.v8debug.commands.Evaluate;
import org.netbeans.lib.v8debug.commands.Frame;
import org.netbeans.lib.v8debug.commands.GC;
import org.netbeans.lib.v8debug.commands.ListBreakpoints;
import org.netbeans.lib.v8debug.commands.Lookup;
import org.netbeans.lib.v8debug.commands.References;
import org.netbeans.lib.v8debug.commands.Scope;
import org.netbeans.lib.v8debug.commands.Scopes;
import org.netbeans.lib.v8debug.commands.Scripts;
import org.netbeans.lib.v8debug.commands.SetBreakpoint;
import org.netbeans.lib.v8debug.commands.Source;
import org.netbeans.lib.v8debug.commands.Threads;
import org.netbeans.lib.v8debug.commands.V8Flags;
import org.netbeans.lib.v8debug.commands.Version;
import org.netbeans.lib.v8debug.connection.IOListener;
import org.netbeans.lib.v8debug.events.AfterCompileEventBody;
import org.netbeans.lib.v8debug.events.BreakEventBody;
import org.netbeans.lib.v8debug.events.CompileErrorEventBody;
import org.netbeans.lib.v8debug.events.ExceptionEventBody;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Boolean;
import org.netbeans.lib.v8debug.vars.V8Function;
import org.netbeans.lib.v8debug.vars.V8Number;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8ScriptValue;
import org.netbeans.lib.v8debug.vars.V8String;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin Entlicher
 */
public class V8Debug {
    
    private static final ResourceBundle resource = ResourceBundle.getBundle(V8Debug.class.getPackage().getName()+".Bundle");
    
    private static boolean debug = false;
    private static boolean closing = false;
    
    private final ClientConnection cc;
    private long requestSequence = 1l;
    private final Map<Long, V8Script> scriptsById = new HashMap<>();
    private final Map<Long, V8Command> internalCommands = Collections.synchronizedMap(new HashMap<Long, V8Command>());
    
    private long numFrames = -1l;
    private V8Frame[] framesToPrint;
    private String toEvaluate;
    private Testeable testeable;
    
    private V8Debug(String serverName, int serverPort) throws IOException {
        cc = new ClientConnection(serverName, serverPort);
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println(resource.getString("MSG_Usage"));
            return ;
        }
        int i = 0;
        if ("--debug".equals(args[0])) {
            debug = true;
            i++;
        }
        int serverPort = Integer.parseInt(args[i+1]);
        try {
            final V8Debug dbg = new V8Debug(args[i], serverPort);
            //dbg.debug = debug;
            dbg.startCommandLoop();
            dbg.responseLoop();
        } catch (IOException ex) {
            if (!closing) {
                printERR("ERR_IO", ex.getLocalizedMessage());
            }
        } catch (ParseException pex) {
            printERR("ERR_ProtocolParseError", pex.getLocalizedMessage());
        }
    }
    
    private static void printPrompt() {
        System.out.print("> ");
    }
    
    private static void printMSG(String key, Object... args) {
        System.out.println(MessageFormat.format(resource.getString(key), args));
    }
    
    private static void printERR(String key, Object... args) {
        System.err.println(MessageFormat.format(resource.getString(key), args));
        if (debug) {
            Thread.dumpStack();
        }
    }
    
    private void startCommandLoop() throws IOException {
        Thread cmdLoop = new Thread() {
            @Override
            public void run() {
                String line;
                //char[] cbuf = new char[4096];
                Reader r = new InputStreamReader(System.in);
                BufferedReader br = new BufferedReader(r);
                try {
                    while((line = br.readLine()) != null) {
                        line = line.trim();
                        boolean cont = doCommand(line);
                        if (!cont) {
                            break;
                        }
                    }
                    System.out.println("");
                    closing = true;
                    cc.close();
                } catch (IOException ex) {
                    printERR("ERR_IO", ex.getLocalizedMessage());
                }
            }
        };
        cmdLoop.setDaemon(true);
        cmdLoop.start();
        // Initial internal commands:
        internalCommands.put(requestSequence, V8Command.Scripts);
        V8Request scriptsRequest = Scripts.createRequest(requestSequence++);
        cc.send(scriptsRequest);
    }
    
    private void responseLoop() throws IOException, ParseException {
        try {
        cc.runEventLoop(new ClientConnection.Listener() {

            @Override
            public void header(Map<String, String> properties) {
                printMSG("MSG_HeaderProperties");
                for (Map.Entry pe : properties.entrySet()) {
                    System.out.println("  "+pe.getKey() + " = " + pe.getValue());
                }
                printPrompt();
            }
            
            @Override
            public void response(V8Response response) {
                if (debug) {
                    System.out.println("response: "+response+", success = "+response.isSuccess()+", running = "+response.isRunning()+", body = "+response.getBody());
                }
                boolean doPrompt = false;
                try {
                    doPrompt = handleResponse(response);
                } catch (IOException ex) {
                    printERR("ERR_IO", ex.getLocalizedMessage());
                }
                if (doPrompt) {
                    printPrompt();
                }
            }

            @Override
            public void event(V8Event event) {
                if (debug) {
                    System.out.println("event: "+event+", name = "+event.getKind());
                }
                boolean doPrompt = handleEvent(event);
                if (doPrompt) {
                    printPrompt();
                }
            }

        });
        } finally {
            cc.close();
            if (testeable != null) {
                testeable.notifyClosed();
            }
        }
    }
    
    private boolean doCommand(String command) throws IOException {
        String[] word = getWord(command);
        String cmd = word[0];
        String args = word[1];
        switch (cmd) {
            case "version":
                cc.send(Version.createRequest(requestSequence++));
                return true;
            case "step":
            case "s":
                int count = -1;
                try {
                    count = Integer.parseInt(args);
                } catch (NumberFormatException nfex) {}
                if (count >= 0) {
                    cc.send(Continue.createRequest(requestSequence++, V8StepAction.in, count));
                } else
                switch (args) {
                    case "up":
                    case "out":
                        cc.send(Continue.createRequest(requestSequence++, V8StepAction.out));
                        break;
                    case "over":
                        cc.send(Continue.createRequest(requestSequence++, V8StepAction.next));
                        break;
                    case "in":
                    case "into":
                    case "":
                        cc.send(Continue.createRequest(requestSequence++, V8StepAction.in));
                        break;
                    default:
                        printMSG("ERR_UnknownCommandStep", args);
                        printPrompt();
                }
                return true;
            case "next":
            case "n":
                count = -1;
                try {
                    count = Integer.parseInt(args);
                } catch (NumberFormatException nfex) {}
                if (count >= 0) {
                    cc.send(Continue.createRequest(requestSequence++, V8StepAction.next, count));
                } else {
                    cc.send(Continue.createRequest(requestSequence++, V8StepAction.next));
                }
                return true;
            case "out":
                count = -1;
                try {
                    count = Integer.parseInt(args);
                } catch (NumberFormatException nfex) {}
                if (count >= 0) {
                    cc.send(Continue.createRequest(requestSequence++, V8StepAction.out, count));
                } else {
                    cc.send(Continue.createRequest(requestSequence++, V8StepAction.out));
                }
                return true;
            case "cont":
            case "c":
                cc.send(Continue.createRequest(requestSequence++));
                return true;
            case "break":
            case "stop":
            //case "sb":
                word = getWord(args);
                String where = word[0];
                String what = word[1];
                if (!(where.equals("at") || where.equals("in"))) {
                    try {
                        long bpNumber = Long.parseLong(where);
                        // Change bp bpNumber:
                        changeBreakpoint(bpNumber, what);
                    } catch (NumberFormatException nfex) {
                        printMSG("ERR_UnknownCommandStop", where);
                        printPrompt();
                    }
                } else {
                    setBreakpoint(what);
                }
                return true;
            case "breakpoints":
            case "bps":
                cc.send(ListBreakpoints.createRequest(requestSequence++));
                return true;
            case "clear":
            case "cb":
                if (args.isEmpty()) {
                    printMSG("ERR_NoBrkpNumToDelete");
                    cc.send(ListBreakpoints.createRequest(requestSequence++));
                } else if ("all".equals(args)) {
                    // Delete all breakpoints
                } else {
                    try {
                        deleteBreakpoints(args);
                    } catch (NumberFormatException nfex) {
                        printMSG("ERR_WrongBrkpNumToDelete", args);
                        printPrompt();
                    }
                }
                return true;
            case "where":
            case "backtrace":
            case "bt":
                numFrames = -1l;
                cc.send(Backtrace.createRequest(requestSequence++, 0l, 1l, false, true));//Integer.MAX_VALUE, false));
                return true;
            case "frame":
            case "f":
                if (args.isEmpty()) {
                    cc.send(Frame.createRequest(requestSequence++, null));
                } else {
                    try {
                        long frameNum = Long.parseLong(args);
                        cc.send(Frame.createRequest(requestSequence++, frameNum));
                    } catch (NumberFormatException nfe) {
                        printMSG("ERR_WrongFrameNumber", args);
                        printPrompt();
                    }
                }
                return true;
            case "scripts":
                cc.send(Scripts.createRequest(requestSequence++));
                return true;
            case "source":
                Long frameNum = null;
                Long fromLine = null;
                Long toLine = null;
                if (!args.isEmpty()) {
                    Scanner scan = new Scanner(args);
                    if (scan.hasNextLong()) {
                        frameNum = scan.nextLong();
                    }
                    if (scan.hasNextLong()) {
                        fromLine = scan.nextLong();
                    }
                    if (scan.hasNextLong()) {
                        toLine = scan.nextLong();
                    }
                }
                cc.send(Source.createRequest(requestSequence++, frameNum, fromLine, toLine));
                return true;
            case "scope":
                if (args.isEmpty()) {
                    cc.send(Scope.createRequest(requestSequence++));
                } else {
                    Scanner scan = new Scanner(args);
                    if (!scan.hasNextLong()) {
                        printMSG("ERR_WrongScopeNumber", args);
                        printPrompt();
                    } else {
                        long scopeNumber = scan.nextLong();
                        Long frameNumber = null;
                        if (scan.hasNextLong()) {
                            frameNumber = scan.nextLong();
                        }
                        cc.send(Scope.createRequest(requestSequence++, scopeNumber, frameNumber));
                    }
                }
                return true;
            case "scopes":
                if (args.isEmpty()) {
                    cc.send(Scopes.createRequest(requestSequence++));
                } else {
                    try {
                        long frameNumber = Long.parseLong(args);
                        cc.send(Scopes.createRequest(requestSequence++, frameNumber));
                    } catch (NumberFormatException nfe) {
                        printMSG("ERR_WrongFrameNumber", args);
                        printPrompt();
                    }
                }
                return true;
            case "eval":
                if (args.isEmpty()) {
                    // TODO: eval mode
                }
            case "print":
                toEvaluate = args;
                cc.send(Evaluate.createRequest(requestSequence++, args));
                return true;
            case "locals":
                cc.send(Frame.createRequest(requestSequence++, null));
                return true;
            case "references":
            case "refs":
                if (args.isEmpty()) {
                    printMSG("ERR_MissingObjectHandle");
                    printPrompt();
                } else {
                    try {
                        long handle = Long.parseLong(args);
                        cc.send(References.createRequest(requestSequence++, References.Type.referencedBy, handle));
                    } catch (NumberFormatException nfe) {
                        printMSG("ERR_WrongObjectHandle", args);
                        printPrompt();
                    }
                }
                return true;
            case "instances":
            case "insts":
                if (args.isEmpty()) {
                    printMSG("ERR_MissingObjectHandle");
                    printPrompt();
                } else {
                    try {
                        long handle = Long.parseLong(args);
                        cc.send(References.createRequest(requestSequence++, References.Type.constructedBy, handle));
                    } catch (NumberFormatException nfe) {
                        printMSG("ERR_WrongObjectHandle", args);
                        printPrompt();
                    }
                }
                return true;
            case "lookup":
                if (!args.isEmpty()) {
                    args = args.replace(',', ' ');
                    Scanner scan = new Scanner(args);
                    if (!scan.hasNextLong()) {
                        printMSG("ERR_WrongObjectHandle", args);
                        printPrompt();
                    } else {
                        List<Long> handlesL = new ArrayList<>();
                        while (scan.hasNextLong()) {
                            long handle = scan.nextLong();
                            handlesL.add(handle);
                        }
                        long[] handles = new long[handlesL.size()];
                        for (int i = 0; i < handlesL.size(); i++) {
                            handles[i] = handlesL.get(i);
                        }
                        cc.send(Lookup.createRequest(requestSequence++, handles, false));
                    }
                } else {
                    printMSG("ERR_MissingObjectHandle");
                    printPrompt();
                }
                return true;
            case "gc":
                if (args.isEmpty()) {
                    cc.send(GC.createRequest(requestSequence++));
                } else {
                    cc.send(GC.createRequest(requestSequence++, args));
                }
                return true;
            case "flags":
                cc.send(V8Flags.createRequest(requestSequence++, args));
                return true;
            case "threads":
                cc.send(new V8Request(requestSequence++, V8Command.Threads, null));
                return true;
            case "exit":
            case "quit":
                cc.send(new V8Request(requestSequence++, V8Command.Disconnect, null));
                return true;
            case "help":
                printMSG("MSG_Help");
                printPrompt();
                return true;
            default:
                if (!cmd.isEmpty()) {
                    printMSG("ERR_UnknownCommand", cmd);
                }
                printPrompt();
                return true;
        }
    }
    
    private static String[] getWord(String str) {
        int index = 0;
        while (index < str.length()) {
            if (!Character.isWhitespace(str.charAt(index))) {
                index++;
            } else {
                break;
            }
        }
        String word = str.substring(0, index);
        String rest = (index >= str.length()) ? "" : str.substring(index).trim();
        return new String[] { word, rest };
    }
    
    private void setBreakpoint(String fileLine) throws IOException {
        int sep = fileLine.indexOf(':');
        if (sep < 0) {
            printERR("ERR_NoLineNumber");
            return ;
        }
        String scriptName = fileLine.substring(0, sep).trim();
        while(++sep < fileLine.length() && Character.isWhitespace(fileLine.charAt(sep))) ;
        if (sep == fileLine.length()) {
            printERR("ERR_NoLineNumber");
            return ;
        }
        String lineStr = fileLine.substring(sep);
        sep = lineStr.indexOf(':');
        int end;
        long line;
        Long column;
        if (sep < 0) {
            end = lineStr.indexOf(' ');
            if (end < 0) {
                end = lineStr.length();
            }
            try {
                line = Long.parseLong(lineStr.substring(0, end).trim());
            } catch (NumberFormatException nfex) {
                printERR("ERR_NotANumber", lineStr.substring(0, end).trim());
                return ;
            }
            column = null;
        } else {
            try {
                line = Long.parseLong(lineStr.substring(0, sep).trim());
            } catch (NumberFormatException nfex) {
                printERR("ERR_NotANumber", lineStr.substring(0, sep).trim());
                return ;
            }
            while(++sep < lineStr.length() && Character.isWhitespace(lineStr.charAt(sep))) ;
            end = lineStr.indexOf(' ', sep);
            if (end < 0) {
                end = lineStr.length();
            }
            try {
                column = Long.parseLong(lineStr.substring(sep, end).trim());
            } catch (NumberFormatException nfex) {
                printERR("ERR_NotANumber", lineStr.substring(sep, end).trim());
                return ;
            }
        }
        while(++end < lineStr.length() && Character.isWhitespace(lineStr.charAt(end))) ;
        if (end < lineStr.length()) {
            lineStr = lineStr.substring(end);
            int ci = lineStr.indexOf("if=");//condition=");
            String condition = null;
            if (ci >= 0) {
                //ci += 10;
                ci += 3;
                condition = lineStr.substring(ci).trim();
            }
            cc.send(SetBreakpoint.createRequest(requestSequence++, V8Breakpoint.Type.scriptName, scriptName, line-1, column, true, condition, null, null));
        } else {
            cc.send(SetBreakpoint.createRequest(requestSequence++, V8Breakpoint.Type.scriptName, scriptName, line-1, column));
        }
    }
    
    // break <num> enable/disable ignoreCount=<num hits> if=<condition>
    private void changeBreakpoint(long bpNumber, String how) throws IOException {
        Boolean enabled = null;
        Long ic = null;
        String condition = null;
        if (how.startsWith("enable")) {
            enabled = true;
            how = how.substring("enable".length()).trim();
        }
        if (how.startsWith("disable")) {
            enabled = false;
            how = how.substring("disable".length()).trim();
        }
        if (how.startsWith("ignoreCount=")) {
            how = how.substring("ignoreCount=".length());
            int i = 0;
            while (i < how.length()) {
                char c = how.charAt(i);
                if (!Character.isDigit(c)) {
                    break;
                }
            }
            try {
                ic = Long.parseLong(how.substring(0, i));
            } catch (NumberFormatException nfex) {
                printERR("ERR_NotANumber", how.substring(0, i));
            }
            how = how.substring(i).trim();
        }
        if (how.startsWith("if=")) {
            condition = how.substring(3).trim();
        }
        cc.send(ChangeBreakpoint.createRequest(requestSequence++, bpNumber, enabled, condition, ic));
    }
    
    private void deleteBreakpoints(String args) throws NumberFormatException, IOException {
        Scanner scanner = new Scanner(args);
        scanner.useDelimiter("[,\\p{javaWhitespace}]+");
        while (scanner.hasNext()) {
            String brkp = scanner.next();
            int dash = brkp.indexOf('-');
            if (dash > 0) {
                String brkp1 = brkp.substring(0, dash).trim();
                String brkp2 = brkp.substring(dash+1).trim();
                long b1 = Long.parseLong(brkp1);
                long b2 = Long.parseLong(brkp2);
                for (long b = b1; b <= b2; b++) {
                    deleteBreakpoint(b);
                }
            } else {
                long b = Long.parseLong(brkp);
                deleteBreakpoint(b);
            }
        }
    }
    
    private void deleteBreakpoint(long b) throws IOException {
        cc.send(ClearBreakpoint.createRequest(requestSequence++, b));
    }
    
    private boolean handleResponse(V8Response response) throws IOException {
        if (testeable != null) {
            testeable.notifyResponse(response);
        }
        V8Command internalCommand = internalCommands.remove(response.getRequestSequence());
        String errorMessage = response.getErrorMessage();
        if (errorMessage != null) {
            System.err.println(errorMessage);
            toEvaluate = null;
            return true;
        }
        V8Body body = response.getBody();
        switch (response.getCommand()) {
            case Scripts:
                Scripts.ResponseBody srb = (Scripts.ResponseBody) body;
                V8Script[] scripts = srb.getScripts();
                synchronized (scriptsById) {
                    for (V8Script script : scripts) {
                        scriptsById.put(script.getId(), script);
                    }
                }
                if (internalCommand != V8Command.Scripts) {
                    print(scripts);
                    return true;
                } else {
                    return false;
                }
            case Source:
                Source.ResponseBody srcrb = (Source.ResponseBody) body;
                printMSG("MSG_SourceLines", srcrb.getFromLine(), srcrb.getToLine(), srcrb.getTotalLines());
                printMSG("MSG_SourcePositions", srcrb.getFromPosition(), srcrb.getToPosition());
                System.out.println(srcrb.getSource());
                return true;
            case Continue:
                printMSG("MSG_Continue");
                return true;
            case Setbreakpoint:
                SetBreakpoint.ResponseBody sbb = (SetBreakpoint.ResponseBody) body;
                V8Breakpoint.ActualLocation[] locations = sbb.getActualLocations();
                if (locations.length == 0) {
                    printMSG("MSG_BreakAdded", sbb.getBreakpoint());
                } else {
                    String locationsStr = printStr(locations);
                    printMSG("MSG_BreakAddedLocations", sbb.getBreakpoint(), locationsStr);
                }
                return true;
            case Clearbreakpoint:
                ClearBreakpoint.ResponseBody cbb = (ClearBreakpoint.ResponseBody) body;
                printMSG("MSG_BreakDeleted", cbb.getBreakpoint());
                return true;
            case Listbreakpoints:
                ListBreakpoints.ResponseBody lbb = (ListBreakpoints.ResponseBody) body;
                printMSG("MSG_Breakpoints");
                for (V8Breakpoint b : lbb.getBreakpoints()) {
                    System.out.print("  ");
                    print(b);
                    System.out.println("");
                }
                printMSG("MSG_BreakOnAllExc", lbb.isBreakOnExceptions());
                printMSG("MSG_BreakOnUncaughtExc", lbb.isBreakOnUncaughtExceptions());
                return true;
            case Backtrace:
                Backtrace.ResponseBody bb = (Backtrace.ResponseBody) body;
                if (numFrames < 0l) {
                    numFrames = bb.getTotalFrames();
                    cc.send(Backtrace.createRequest(requestSequence++, 0l, numFrames, false, true));
                    return false;
                }
                numFrames = -1l;
                V8Frame[] frames = bb.getFrames();
                ReferencedValue[] referencedValues = response.getReferencedValues();
                Map<Long, V8Value> values;
                if (referencedValues.length > 0) {
                    values = new HashMap<>();
                    for (ReferencedValue rv : referencedValues) {
                        if (rv.hasValue()) {
                            values.put(rv.getReference(), rv.getValue());
                        }
                    }
                } else {
                    values = null;
                }
                for (V8Frame f : frames) {
                    V8Script script = null;
                    if (values != null) {
                        long scriptRef = f.getScriptRef();
                        V8Value scriptValue = values.get(scriptRef);
                        if (scriptValue instanceof V8ScriptValue) {
                            script = ((V8ScriptValue) scriptValue).getScript();
                        }
                    }
                    print(f, script);
                    System.out.println("");
                }
                return true;
                /*
                Set<Long> handlesToLookUp = new HashSet<>();
                for (V8Frame f : frames) {
                    handlesToLookUp.add(f.getScriptRef());
                }
                framesToPrint = frames;
                Long[] handlesL = handlesToLookUp.toArray(new Long[]{});
                long[] handles = new long[handlesL.length];
                for (int i = 0; i < handlesL.length; i++) {
                    handles[i] = handlesL[i];
                }
                internalCommands.put(requestSequence, V8Command.Lookup);
                cc.send(Lookup.createRequest(requestSequence++, handles, false));
                return false;
                */
            case Lookup:
                Lookup.ResponseBody lrb = (Lookup.ResponseBody) body;
                values = lrb.getValuesByHandle();
                for (Map.Entry<Long, V8Value> ve : values.entrySet()) {
                    System.out.print(ve.getKey()+": ");
                    print(ve.getValue());
                }
                return true;
                /*
                if (framesToPrint != null) {
                    for (V8Frame f : framesToPrint) {
                        long scriptRef = f.getScriptRef();
                        V8Value scriptValue = values.get(scriptRef);
                        V8Script script = null;
                        if (scriptValue instanceof V8ScriptValue) {
                            script = ((V8ScriptValue) scriptValue).getScript();
                        }
                        print(f, script);
                        System.out.println("");
                    }
                    framesToPrint = null;
                    return true;
                } else {
                    return false;
                }
                */
            case Evaluate:
                Evaluate.ResponseBody erb = (Evaluate.ResponseBody) body;
                print(erb.getValue());
                return true;
            case References:
                References.ResponseBody rrb = (References.ResponseBody) body;
                V8Value[] refs = rrb.getReferences();
                if (refs.length == 0) {
                    printMSG("MSG_NoReferences");
                } else {
                    printMSG("MSG_References", refs.length);
                    for (V8Value r : refs) {
                        print(r);
                    }
                }
                return true;
            case Frame:
                Frame.ResponseBody frb = (Frame.ResponseBody) body;
                V8Frame frame = frb.getFrame();
                {
                    String scriptName;
                    long sid = frame.getScriptRef();
                    V8Script script = getScript(sid);
                    if (script != null) {
                        scriptName = script.getName();
                    } else {
                        scriptName = "(id="+sid+")";
                    }
                    System.out.println(scriptName+":"+frame.getLine()+":"+frame.getColumn());
                    printMSG("MSG_SourceLine", frame.getSourceLineText());
                }
                printMSG("MSG_Arguments");
                printValues(frame.getArgumentRefs());
                printMSG("MSG_LocalVariables");
                printValues(frame.getLocalRefs());
                return true;
            case Gc:
                GC.ResponseBody gcrb = (GC.ResponseBody) body;
                printMSG("MSG_GC", gcrb.getBefore(), gcrb.getAfter());
                return true;
            case Threads:
                Threads.ResponseBody trb = (Threads.ResponseBody) body;
                printMSG("MSG_Threads", trb.getNumThreads());
                Map<Long, Boolean> tids = trb.getIds();
                for (Map.Entry<Long, Boolean> tid : tids.entrySet()) {
                    printMSG("MSG_Thread", tid.getKey(), tid.getValue());
                }
                return true;
            case Version:
                printMSG("MSG_Version", ((Version.ResponseBody) body).getVersion());
                return true;
            case Disconnect:
                closing = true;
                return false;
            default:
                return true;
        }
    }
    
    private boolean handleEvent(V8Event event) {
        if (testeable != null) {
            testeable.notifyEvent(event);
        }
        switch (event.getKind()) {
            case AfterCompile:
                AfterCompileEventBody aceb = (AfterCompileEventBody) event.getBody();
                V8Script script = aceb.getScript();
                synchronized (scriptsById) {
                    scriptsById.put(script.getId(), script);
                }
                return false;
            case CompileError:
                CompileErrorEventBody ceeb = (CompileErrorEventBody) event.getBody();
                script = ceeb.getScript();
                synchronized (scriptsById) {
                    scriptsById.put(script.getId(), script);
                }
                return false;
            case Break:
                System.out.println(""); // Newline to abandon prompt.
                BreakEventBody beb = (BreakEventBody) event.getBody();
                System.out.println("stopped at "+beb.getScript().getName()+", line = "+(beb.getSourceLine()+1)+" : "+beb.getSourceColumn()+"\ntext = "+beb.getSourceLineText());
                return true;
            case Exception:
                System.out.println(""); // Newline to abandon prompt.
                ExceptionEventBody eeb = (ExceptionEventBody) event.getBody();
                System.out.println("exception '"+eeb.getException()+"' stopped in "+eeb.getScript().getName()+", line = "+(eeb.getSourceLine()+1)+" : "+eeb.getSourceColumn()+"\ntext = "+eeb.getSourceLineText());
                return true;
            default:
                throw new IllegalStateException("Unknown event: "+event.getKind());
        }
    }
    
    private V8Script getScript(long id) {
        synchronized (scriptsById) {
            return scriptsById.get(id);
        }
    }
    
    private void print(V8Breakpoint b) {
        System.out.print(b.getNumber()+".: ");
        String scriptName = b.getScriptName();
        if (scriptName == null && b.getScriptId().hasValue()) {
            V8Script script = getScript(b.getScriptId().getValue());
            if (script != null) {
                scriptName = script.getName();
            } else {
                scriptName = "(script id="+b.getScriptId().getValue()+")";
            }
        }
        System.out.print(scriptName);
        if (b.getLine().hasValue()) {
            System.out.print(":"+(b.getLine().getValue()+1));
        }
        if (b.getColumn().hasValue()) {
            System.out.print(":"+b.getColumn().getValue());
        }
        System.out.print(" active="+b.isActive());
    }
    
    private void print(V8Frame f, V8Script script) {
        String scriptName;
        if (script == null) {
            scriptName = "(ref="+f.getScriptRef()+")";
        } else {
            scriptName = script.getName();
        }
        long line = f.getLine()+1;
        long column = f.getColumn();
        System.out.print(scriptName+":"+line+":"+column);
    }
    
    private void print(V8Script[] scripts) {
        if (scripts.length == 0) {
            printMSG("MSG_NoLoadedScripts");
            return ;
        }
        printMSG("MSG_LoadedScripts");
        V8Script[] sortedScripts = new V8Script[scripts.length];
        System.arraycopy(scripts, 0, sortedScripts, 0, scripts.length);
        Arrays.sort(sortedScripts, new Comparator<V8Script>() {
            @Override
            public int compare(V8Script s1, V8Script s2) {
                long d = s1.getId() - s2.getId();
                return (d == 0l) ? 0 : (d > 0l) ? 1 : -1;
            }
        });
        scripts = sortedScripts;
        long maxId = scripts[scripts.length - 1].getId();
        /*for (V8Script script : scripts) {
            long id = script.getId();
            if (id > maxId) {
                maxId = id;
            }
        }*/
        int numDigits = Long.toString(maxId).length();
        for (V8Script script : scripts) {
            StringBuilder out = new StringBuilder("#");
            String idStr = Long.toString(script.getId());
            for (int i = idStr.length(); i < numDigits; i++) {
                out.append('0');
            }
            out.append(idStr);
            out.append(": ");
            out.append(script.getName());
            System.out.println(out);
        }
    }

    private String printStr(V8Breakpoint.ActualLocation[] locations) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (V8Breakpoint.ActualLocation l : locations) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            String scriptName = l.getScriptName();
            if (scriptName != null) {
                sb.append(scriptName);
            } else {
                V8Script script = getScript(l.getScriptId().getValue());
                if (script != null) {
                    sb.append(script.getName());
                } else {
                    sb.append("(script id="+l.getScriptId()+")");
                }
            }
            sb.append(":");
            sb.append(l.getLine()+1);
            long c = l.getColumn();
            if (c >= 0) {
                sb.append(":");
                sb.append(c);
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    private void print(V8Value value) {
        System.out.print("  ");
        if (toEvaluate != null) {
            System.out.print(toEvaluate+" = ");
            toEvaluate = null;
        }
        System.out.println(printStr(value));
    }
    
    private String printStr(ReferencedValue refAndVal) {
        if (refAndVal.hasValue()) {
            V8Value value = refAndVal.getValue();
            return printStr(value);
        } else {
            return "reference: "+refAndVal.getReference();
        }
    }
    
    private String printStr(V8Value value) {
        switch (value.getType()) {
            case Boolean:
                return Boolean.toString(((V8Boolean) value).getValue());
            case Function:
                String name = ((V8Function) value).getName();
                if (name == null || name.isEmpty()) {
                    name = ((V8Function) value).getInferredName();
                }
                return name+"()";
            case Null:
                return String.valueOf((Object) null);
            case Number:
                V8Number n = (V8Number) value;
                switch (n.getKind()) {
                    case Double:
                        return Double.toString(n.getDoubleValue());
                    case Long:
                        return Long.toString(n.getLongValue());
                    default:
                        throw new IllegalStateException("Unknown kind: "+n.getKind());
                }
            case Object:
                V8Object o = (V8Object) value;
                StringBuilder sb = new StringBuilder("(");
                sb.append(o.getClassName());
                sb.append(')');
                if (o.getText() != null) {
                    sb.append(' ');
                    sb.append(o.getText());
                }
                if (o.getProperties() != null) {
                    Map<String, V8Object.Property> properties = o.getProperties();
                    String newLine = System.getProperty("line.separator");

                    properties.forEach((propName, v8Prop) -> {
                        sb.append(newLine);
                        sb.append("  ");
                        sb.append(propName);
                        sb.append(" = ");
                        sb.append('(');
                        sb.append(v8Prop.getType());
                        sb.append(") ref: ");
                        sb.append(v8Prop.getReference());
                    });
                }
                return sb.toString();
            case String:
                return "\""+((V8String) value).getValue()+"\"";
            case Undefined:
                return "undefined";
            default:
                if (value.getText() != null) {
                    return value.getText();
                }
                throw new IllegalStateException("Unknown value type: "+value.getType());
        }
    }

    private void printValues(Map<String, ReferencedValue> argumentRefs) {
        for (Map.Entry<String, ReferencedValue> entry : argumentRefs.entrySet()) {
            System.out.println(entry.getKey() + " = " + printStr(entry.getValue()));
        }
    }
    
    static final class TestAccess {
        
        static V8Debug createV8Debug(String hostName, int port, Testeable testeable) throws IOException {
            final V8Debug v8dbg = new V8Debug(hostName, port);
            v8dbg.testeable = testeable;
            v8dbg.cc.addIOListener(testeable);
            v8dbg.startCommandLoop();
            Thread responseLoop = new Thread("Response loop") {
                @Override
                public void run() {
                    try {
                        v8dbg.responseLoop();
                    } catch (IOException | ParseException ex) {
                        Logger.getLogger(V8Debug.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            responseLoop.setDaemon(true);
            responseLoop.start();
            return v8dbg;
        }
        
        static boolean doCommand(V8Debug v8dbg, String command) throws IOException {
            return v8dbg.doCommand(command);
        }
        
        static V8Script getScript(V8Debug v8dbg, long id) {
            return v8dbg.getScript(id);
        }
        
        static void send(V8Debug v8dbg, V8Request req) throws IOException {
            v8dbg.cc.send(req);
        }
        
        static boolean isClosed(V8Debug v8dbg) {
            return v8dbg.cc.isClosed();
        }
        
        static V8Script getScriptByName(V8Debug v8dbg, String name) {
            synchronized (v8dbg.scriptsById) {
                for (V8Script s : v8dbg.scriptsById.values()) {
                    if (name.equals(s.getName())) {
                        return s;
                    }
                }
            }
            return null;
        }
        
    }
    
    static interface Testeable extends IOListener {
        
        void notifyResponse(V8Response response);
        
        void notifyEvent(V8Event event);
        
        void notifyClosed();
        
    }

}
