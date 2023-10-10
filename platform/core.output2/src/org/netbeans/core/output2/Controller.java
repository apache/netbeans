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

package org.netbeans.core.output2;

import java.awt.Container;
import java.awt.Font;
import java.io.CharConversionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.Document;
import org.netbeans.core.output2.options.OutputOptions;
import org.openide.util.Exceptions;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOSelect;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputWriter;
import org.openide.xml.XMLUtil;

/**
 * Master controller for an output window, and supplier of the default instance.
 * The controller handles all actions of interest in an output window - the components
 * are merely containers for data which pass events of interest up the component hierarchy
 * to the controller via OutputWindow.getController(), for processing by the master
 * controller.  The controller is fully stateless, and stores information of interest in
 * the components as appropriate.
 */
public class Controller {

    static Controller controller;

    public static Controller getDefault() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
    }

    private Map<NbIO, WeakReference<OutputTab>> ioToTab =
            new WeakHashMap<NbIO, WeakReference<OutputTab>>();

    Controller() {}

    void eventDispatched(IOEvent ioe) {
        if (Controller.LOG) Controller.log ("Event received: " + ioe);
        NbIO io = ioe.getIO();
        int command = ioe.getCommand();
        boolean value = ioe.getValue();
        Object data = ioe.getData();
        OutputTab comp = findTabForIo(io);
        if (Controller.LOG) Controller.log ("Passing command to controller " + ioe);
        performCommand(comp, io, command, value, data);
        ioe.consume();
    }

    private OutputTab createOutputTab(NbIO io) {
        if (LOG) log ("Create component for nbio " + io);
        OutputTab result = new OutputTab (io);
        result.setName (io.getName() + " ");
        Action[] a = io.getToolbarActions();
        if (a != null) {
            result.setToolbarActions(a);
        }

        if (LOG) log ("Adding new tab " + result);
        ioToTab.put(io, new WeakReference<OutputTab>(result));
        IOContainer ioContainer = io.getIOContainer();
        ioContainer.add(result, result);
        ioContainer.setToolbarActions(result, a);
        if (io.getIcon() != null) {
            ioContainer.setIcon(result, io.getIcon());
        }
        if (io.getToolTipText() != null) {
            ioContainer.setToolTipText(result, io.getToolTipText());
        }
        io.setClosed(false);
        OutputWriter out = io.getOut();
        if (out instanceof NbWriter) {
            ((NbWriter) out).out().setDisposeOnClose(false);
        }

        //Make sure names are boldfaced for all open streams - if the tabbed
        //pane was just added in, it will just have used the name of the
        //component, which won't contain html
        for (OutputTab tab : getAllTabs()) {
            updateName(tab);
        }
        return result;
    }

    void removeTab(NbIO io) {
        WeakReference<OutputTab> tabReference = ioToTab.remove(io);
        if (tabReference != null) {
            OutputTab tab = tabReference.get();
            if (tab != null) {
                removeFromUpdater(tab);
            }
        }
    }

    void changeFontSizeBy(int change, boolean monospaced) {
        Font origFont = OutputOptions.getDefault().getFont(monospaced);
        int fontSize = origFont.getSize() + change;
        OutputOptions.getDefault().setFontSize(monospaced, fontSize);
        for (OutputTab tab : getAllTabs()) {
            OutputOptions opts = tab.getIO().getOptions();
            opts.setFontSize(monospaced, fontSize);
        }
        OutputOptions.storeDefault();
    }

    public void updateOptions(OutputOptions options) {
        options.setFontSize(true, options.getFont(false).getSize());
        for (OutputTab ot : getAllTabs()) {
            if (ot.getIO().getIOContainer().equals(IOContainer.getDefault())) {
                ot.getIO().getOptions().assign(options);
            }
        }
    }

    void removeFromUpdater(OutputTab tab) {
        if (nameUpdater != null) {
            nameUpdater.remove(tab);
        }
    }

    /**
     * Boldfaces the name of the output component if its NbIO's stream is open.
     * The update is delayed, and runs subsequently on the event queue - a process may
     * synchronously open and close tabs, all of which affects names, so we use this
     * technique and the CoalescedNameUpdater to coalesce all name changes - otherwise
     * the name change may be delayed.
     *
     * @param tab The component whose name may need adjusting
     */
    void updateName(OutputTab tab) {
        if (nameUpdater == null) {
            if (LOG) log ("Update name for " + tab.getIO() + " dispatching a name updater");
            nameUpdater = new CoalescedNameUpdater();
            SwingUtilities.invokeLater(nameUpdater);
        }
        nameUpdater.add(tab);
    }

    private static boolean htmlTabsBroken() {
        String version = System.getProperty("java.version");
        for (int i = 14; i < 18; i++) {
            if (version.startsWith("1.6.0_" + i)) {
                return true;
            }
        }
        if( version.startsWith("1.6.0") && "Aqua".equals( UIManager.getLookAndFeel().getID() ) )
            return true;
        return false;
    }

    // workaround for JDK bug (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6670274)
    // NB issue #113388
    private static final boolean DONT_USE_HTML = htmlTabsBroken();
    private CoalescedNameUpdater nameUpdater = null;
    /**
     * Calls to methods invoked on NbIO done on the EQ are invoked synchronously
     * (this avoids a delay in the output window appearing, so output starts
     * immediately).  However, we want to avoid multiple name changes being
     * propagated up to the window system because one tab was removed, another
     * was added, and so forth - the result is the title won't be updated until
     * the output run is nearly done, otherwise.  Also, the call to update the
     * TopComponent name is not terribly quick, so we don't want to do it any
     * more times than we need to.
     * <p>
     * This class coalesces name changes, which are run afterward on the event
     * queue.
     */
    class CoalescedNameUpdater implements Runnable {
        private Set<OutputTab> components = new HashSet<OutputTab>();
        CoalescedNameUpdater() {
        }

        public void add(OutputTab tab) {
            components.add(tab);
        }
        
        public void remove(OutputTab tab) {
            components.remove(tab);
        }

        boolean contains(OutputTab tab) {
            return components.contains(tab);
        }

        public void run() {
            List<OutputTab> toRemove = null;
            for (OutputTab t : components) {
                NbIO io = t.getIO();
                if (!ioToTab.containsKey(io)) {
                    if (toRemove == null) {
                        toRemove = new LinkedList<OutputTab>();
                    }
                    toRemove.add(t);
                    continue;
                }
                if (LOG) {
                    log ("Update name for " + io.getName() + " stream " +
                        "closed is " + io.isStreamClosed());
                }
                String escaped;
                try {
                    escaped = XMLUtil.toAttributeValue(io.getName());
                } catch (CharConversionException e) {
                    escaped = io.getName();
                }
                String name = io.isStreamClosed() ?  io.getName() + " " : //NOI18N
                    (DONT_USE_HTML ? io.getName() + " * " : "<html><b>" + escaped + " </b>&nbsp;</html>"); //NOI18N

                if (LOG) {
                    log("  set name to " + name);
                }
                //#88204 apostophes are escaped in xm but not html
                io.getIOContainer().setTitle(t, name.replace("&apos;", "'"));
            }
            if (toRemove != null) {
                components.removeAll(toRemove);
            }
            nameUpdater = null;
        }
    }

    /** Detect whether output tab is in a window that is in sliding mode.
     * Issue #202093.
     */
    private static boolean isInSlidingMode(OutputTab tab) {
        for (Container p = tab; p != null; p = p.getParent()) {
            if (p instanceof JComponent) {
                JComponent jp = (JComponent) p;
                Object sliding = jp.getClientProperty("isSliding"); //NOI18N
                if (sliding != null) {
                    if (sliding.equals(Boolean.TRUE)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Handles IOEvents posted into the AWT Event Queue by NbIO instances whose methods have
     * been called, as received by an OutputTab which has identified the event as being
     * intended for it.
     *
     * @param tab The output component associated with this IO, if any
     * @param io The IO which originated the event
     * @param command The ID, one of those defined in IOEvent, of the command
     * @param value The boolean value of the command, if pertinent
     * @param data The data associated with the command, if pertinent
     */
    void performCommand(OutputTab tab, NbIO io, int command, boolean value, Object data) {

        if (LOG) {
            log ("PERFORMING: " +  IOEvent.cmdToString(command) + " value=" + value + " on " + io + " tob " + tab);
        }

        IOContainer ioContainer = io.getIOContainer();

        switch (command) {
            case IOEvent.CMD_CREATE :
                createOutputTab(io);
                break;
            case IOEvent.CMD_INPUT_VISIBLE :
                if (value && tab == null) {
                    tab = createOutputTab(io);
                }
                if (tab != null) {
                    tab.setInputVisible(value);
                }
                break;
            case IOEvent.CMD_SELECT :
                if (tab == null) {
                    tab = createOutputTab(io);
                }
		if (io.isFocusTaken() || isInSlidingMode(tab)) {
		    ioContainer.requestActive();
		}

		// After fixing bug#185209 IOContainer.select() no longer
		// performs these operations for us so we have to do them.
		ioContainer.open();
		ioContainer.requestVisible();

		ioContainer.select(tab);
                break;
            case IOEvent.CMD_FINE_SELECT :
                if (tab == null) {
                    tab = createOutputTab(io);
                }

		// We get here via IOSelect.select().
		assert data == null || data instanceof Set;
		@SuppressWarnings("unchecked")		// NOI18N
		Set<IOSelect.AdditionalOperation> extraOps =
		    (Set<IOSelect.AdditionalOperation>) data;

		if (extraOps != null) {
		    // the order of these tests mimics the order of calls above.
		    if (io.isFocusTaken() && extraOps.contains(IOSelect.AdditionalOperation.REQUEST_ACTIVE))
			ioContainer.requestActive();
		    if (extraOps.contains(IOSelect.AdditionalOperation.OPEN))
			ioContainer.open();
		    if (extraOps.contains(IOSelect.AdditionalOperation.REQUEST_VISIBLE))
			ioContainer.requestVisible();
		}
		ioContainer.select(tab);
                break;
            case IOEvent.CMD_SET_TOOLBAR_ACTIONS :
                if (tab != null) {
                    Action[] a = (Action[]) data;
                    tab.setToolbarActions(a);
                    tab.getIO().getIOContainer().setToolbarActions(tab, a);
                }
                break;
            case IOEvent.CMD_CLOSE :
                if (tab != null) {
                    ioContainer.remove(tab);
                }
                io.dispose();
                break;
            case IOEvent.CMD_STREAM_CLOSED :
                if (value) {
                    if (tab == null) {
                        //The tab was already closed, throw away the storage.
                        if (io.out() != null) {
                            io.out().dispose();
                        }
                    } else {
                        updateName(tab);
                        if (tab.getIO().out() != null && tab.getIO().out().getLines().firstListenerLine() == -1) {
                            tab.getOutputPane().ensureCaretPosition();
                        }
                        if (tab == ioContainer.getSelected()) {
                            tab.updateActions();
                        }
                    }
                } else {
                    if (tab != null && tab.getParent() != null) {
                        updateName(tab);
                    }
                }
                break;
            case IOEvent.CMD_RESET :
                if (tab == null) {
                    if (LOG) log ("Got a reset on an io with no tab.  Creating a tab.");
                    createOutputTab(io);
                    ioContainer.requestVisible();
                    return;
                }
                if (LOG) log ("Setting io " + io + " on tab " + tab);
                tab.reset();
                updateName(tab);
                if (LOG) log ("Reset on " + tab + " tab displayable " + tab.isDisplayable() + " io " + io + " io.out " + io.out());
                break;

            case IOEvent.CMD_SET_ICON:
                if (tab != null) {
                    tab.getIO().getIOContainer().setIcon(tab, (Icon) data);
                }
                break;

            case IOEvent.CMD_SET_TOOLTIP:
                if (tab != null) {
                    tab.getIO().getIOContainer().setToolTipText(tab, (String) data);
                }
                break;

            case IOEvent.CMD_SCROLL:
                if (tab != null) {
                    tab.getOutputPane().scrollTo((Integer) data);
                }
                break;

            case IOEvent.CMD_DEF_COLORS:
                if (tab != null) {
                    Document doc = tab.getOutputPane().getDocument();
                    if (doc instanceof OutputDocument) {
                        Lines lines = ((OutputDocument) doc).getLines();
                        if (lines != null) {
                            IOColors.OutputType type = (IOColors.OutputType) data;
                            lines.setDefColor(type, io.getColor(type));
                            tab.getOutputPane().repaint();
                        }
                    }
                }
                break;
        }
    }

    /**
     * An OutputEvent implementation with a settable line index so it can be
     * reused.
     */
    static class ControllerOutputEvent extends OutputEvent {
        private int line;
        private OutWriter out;
        ControllerOutputEvent(NbIO io, int line) {
            super (io);
            out = io.out();
            this.line = line;
        }

        ControllerOutputEvent(NbIO io, OutWriter out, int line) {
            this(io, line);
            this.out = out;
        }

        void setLine (int line) {
            this.line = line;
        }

        public String getLine() {
            NbIO io = (NbIO) getSource();
            OutWriter out = io.out();
            try {
                if (out != null) {
                    String s = out.getLines().getLine(line);
                    //#46892 - newlines should not be appended to returned strings
                    if (s.endsWith("\n")) { //NOI18N
                        s = s.substring(0, s.length()-1);
                    }
                    //#70008 on windows \r can also be there..
                    if (s.endsWith("\r")) { //NOI18N
                        s = s.substring(0, s.length()-1);
                    }
                    return s;
                }
            } catch (IOException ioe) {
                IOException nue = new IOException ("Could not fetch line " + line + " on " + io.getName()); //NOI18N
                nue.initCause(ioe);
                Exceptions.printStackTrace(ioe);
            }
            return null;
        }
    }

    public static final boolean LOG = Boolean.getBoolean("nb.output.log") || Boolean.getBoolean("nb.output.log.verbose"); //NOI18N
    public static final boolean VERBOSE = Boolean.getBoolean("nb.output.log.verbose");
    static final boolean logStdOut = Boolean.getBoolean("nb.output.log.stdout"); //NOI18N
    public static void log (String s) {
        s = Long.toString(System.currentTimeMillis()) + ":" + s + "(" + Thread.currentThread() + ")  ";
        if (logStdOut) {
            System.out.println(s);
            return;
        }
        OutputStream os = getLogStream();
        byte b[] = new byte[s.length() + 1];
        char[] c = s.toCharArray();
        for (int i=0; i < c.length; i++) {
            b[i] = (byte) c[i];
        }
        b[b.length-1] = (byte) '\n';
        try {
            os.write(b);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(s);
        }
        try {
            os.flush();
        } catch (Exception e ) {}
    }
    
    public static void logStack() {
        if (logStdOut) {
            new Exception().printStackTrace();
            return;
        }
        Exception e = new Exception();
        e.fillInStackTrace();
        StackTraceElement[] ste = e.getStackTrace();

        for (int i=1; i < Math.min (22, ste.length); i++) {
            log ("   *   " + ste[i]);
        }
    }

    private static OutputStream logStream = null;
    private static OutputStream getLogStream() {
        if (logStream == null) {
            String spec = System.getProperty ("java.io.tmpdir") + File.separator + "outlog.txt";
            synchronized (Controller.class) {
                try {
                    File f = new File (spec);
                    if (f.exists()) {
                        f.delete();
                    }
                    f.createNewFile();
                    logStream = new FileOutputStream(f);
                } catch (Exception e) {
                    e.printStackTrace();
                    logStream = System.err;
                }
            }
        }
        return logStream;
    }

    private synchronized OutputTab findTabForIo(NbIO io) {
        WeakReference<OutputTab> tabReference = ioToTab.get(io);
        OutputTab result = tabReference == null ? null : tabReference.get();
        if (result == null && Controller.LOG) {
            Controller.log("Tab for IO " + io.getName() //NOI18N
                    + " was not found."); //NOI18N)
        }
        return result;
    }

    private synchronized List<OutputTab> getAllTabs() {
        List<OutputTab> tabs = new LinkedList<OutputTab>();
        for (WeakReference<OutputTab> tabReference : ioToTab.values()) {
            if (tabReference != null) {
                OutputTab outputTab = tabReference.get();
                if (outputTab != null) {
                    tabs.add(outputTab);
                }
            }
        }
        return tabs;
    }
}

