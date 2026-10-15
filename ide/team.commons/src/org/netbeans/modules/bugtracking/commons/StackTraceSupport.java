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

package org.netbeans.modules.bugtracking.commons;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Finds stacktraces in texts.
 *
 *  XXX Does not handle poorly formated stacktraces e.g.
 *  http://www.netbeans.org/issues/show_bug.cgi?id=100005&x=17&y=10
 *
 *  XXX: Needs to filter out indentical stacktrace hashes
 *
* @author Petr Hrebejk, Jan Stola, Tomas Stupka
 */
class StackTraceSupport {

    private static final Pattern ST_PATTERN =
           Pattern.compile("([\\p{Alnum}\\.\\$_<>]*?)\\((?:Native Method|Unknown Source|Compiled Code|([\\p{Alnum}\\.\\$_]*?):(\\p{Digit}+?))\\)", Pattern.DOTALL);

    private StackTraceSupport() { }

    @SuppressWarnings("empty-statement")
    private static void findAndOpen(String text) {
        List<StackTracePosition> st = StackTraceSupport.find(text);
        for (StackTracePosition stp : st) {
            StackTraceElement ste = stp.getStackTraceElements()[0];
            String path = getPath(ste);
            open(path, ste.getLineNumber() - 1);
            break;
        }
    }

    private static void findAndShowHistory(String text) {
        List<StackTracePosition> st = StackTraceSupport.find(text);
        for (StackTracePosition stp : st) {
            StackTraceElement ste = stp.getStackTraceElements()[0];
            String path = getPath(ste);
            openSearchHistory(path, ste.getLineNumber());
            break;
        }
    }

    private static String getPath(StackTraceElement ste ) {
        String path = ste.getClassName();
        int index = path.indexOf('$');
        if (index != -1) {
            path = path.substring(0, index);
        }
        path = path.replace(".", "/") + ".java"; // XXX .java ???
        return path;
    }

    /**
     * package private for test purposes
     */
    static List<StackTracePosition> find(String text) {

       LinkedList<StackTracePosition> result = new LinkedList<StackTracePosition>();
       if ( text == null) {
           return result;
       }

//       List<Integer> lineBreaks = new ArrayList<Integer>();
//       int pos = -1;
//       while( (pos = text.indexOf("\n", pos + 1)) > -1) {
//           lineBreaks.add(pos);
//       }

       String nt = removeAll( text, '\n');
       //String nt = text.replace('\n', ' ');

       Matcher m  = ST_PATTERN.matcher(nt);

       List<StackTraceElement> st = new ArrayList<StackTraceElement>();
       subs = new ArrayList<String>();
       int last = -1;       
       int start = -1;
       while( m.find() ) {
           if(start == -1) start = m.start();
           if ( !isStacktraceContinuation( nt, last, m.start() ) ) {
               StackTraceElement[] stArray = st.toArray(new StackTraceElement[0]);
               // Ignore zero line and one line stacktraces
               if ( stArray.length > 1 ) {
                   start = adjustFirstLinePosition(text, start);
                   result.add( new StackTracePosition(stArray, start, last) );
//                   if (result.size() > 50) {
//                       result.removeFirst(); // XXX WTF
//                   }
               }
               st = new ArrayList<StackTraceElement>();
               start = m.start();
               subs = new ArrayList<String>();
           }
           StackTraceElement ste = createStackTraceElement(m.group(1), m.group(2), m.group(3));
           if ( ste != null ) {
               st.add(ste);
           }

           last = m.end();
       }
       if ( !st.isEmpty() ) {
           start = adjustFirstLinePosition(text, start);
           result.add( new StackTracePosition(st.toArray(new StackTraceElement[0]), start, last) );
       }

//       int i = 0;
//       for (StackTracePosition stp : result) {
//           for (; i < lineBreaks.size(); i++) {
//               int lb = lineBreaks.get(i);
//               if(lb > stp.end) break;
//           }
//           stp.start += i;
//           stp.end += i;
//       }

       return result;
   }

   private static List<String> subs;

   // XXX Pretty ugly heuristics
   private static boolean isStacktraceContinuation(String text, int last, int start) {
       if ( last == -1 ) {
           return true;
       }

       else {
           String sub = text.substring(last,start);
           subs.add(sub);
           //System.out.println("  SUB: " + sub );
//            if ( !sub.contains("at")) {
//                return false;
//            }
           for( int i = 0; i < sub.length(); i++) {
               char ch = sub.charAt(i);
               switch( ch ) {
                   case ' ':
                   case 'a':
                   case '\t':
                   case 't':
                   case '\n':
                   case '\r':
                   case 'c':
                   case 'h':
                   case '[':
                   case ']':
                       continue;
                   default:
                     //  System.out.println("  ???? " + Integer.valueOf(ch));
                       return false;
               }
           }
           return true;
       }
   }

   private static int adjustFirstLinePosition(String text, int start) {
       // Adjust the start index so the first line of the stacktrace also
       // includes 'at' or '[catch]'.
       if (start > 0) {
           int startOfLine = start - 1;
           while (startOfLine > 0) {
               if (text.charAt(startOfLine) == '\n') {
                   startOfLine++;
                   break;
               } else {
                   startOfLine--;
               }
           }
           if (isStacktraceContinuation(text, startOfLine, start)) {
               return startOfLine;
           }
       }
       return start;
   }

   private static StackTraceElement createStackTraceElement(String method, String file, String line) {
       int lastDot = method.lastIndexOf('.');
       if ( lastDot == -1 ) {
           return null;
       }
       Integer lineNr;
       try {
           lineNr = line == null ? -1 : Integer.parseInt(line);
       } catch (NumberFormatException e) { 
           return null;
       }
       return new StackTraceElement( method.substring(0, lastDot),
                                     method.substring(lastDot + 1),
                                     file,
                                     lineNr );

   }

   private static String removeAll( String source, char toRemove) {

       StringBuilder sb = new StringBuilder();

       for (int i = 0; i < source.length(); i++) {
           char c = source.charAt(i);
           if ( c == '\n' ) {
               if ( i > 1 && source.charAt( i - 2) == 'a' && source.charAt( i - 2) == 't' ) { // XXX WTF
                   sb.append("");
               }
               // Skip the new line
               sb.append(" ");
           }
           else {
               sb.append(c);
           }
       }

       return sb.toString();
   }

    static class StackTracePosition {
        private final StackTraceElement[] stackTraceElements;
        private final int start;
        private final int end;
        StackTracePosition(StackTraceElement[] stackTraceElements, int start, int end) {
            this.stackTraceElements = stackTraceElements;
            this.start = start;
            this.end = end;
        }
        int getStartOffset() {
            return start;
        }
        int getEndOffset() {
            return end;
        }
        StackTraceElement[] getStackTraceElements() {
            return stackTraceElements;
        }
    }

    static void open(String path, final int line) {
        IDEServices ideServices = Support.getInstance().getIDEServices();
        if(ideServices != null) {
            ideServices.openDocument(path, line);
        }
    }

    static boolean isAvailable() {
        IDEServices ideServices = Support.getInstance().getIDEServices();
        return ideServices != null && ideServices.providesOpenDocument();
    }
    
    @NbBundle.Messages({"CTL_ShowHistoryTitle=Show History",
                        "# {0} - path to be opened",  "MSG_NoHistory=History View not available for file with path\n {0}."})
    private static void openSearchHistory(final String path, final int line) {
        if(path == null) {
            return;
        }
        final IDEServices ideServices = Support.getInstance().getIDEServices();
        if(ideServices == null || !ideServices.providesOpenHistory()) {
            return;
        }
        Support.getInstance().getParallelRP().post(new Runnable() {
            @Override
            public void run() {
                if(!ideServices.openHistory(path, line)) {
                    Util.notifyError(Bundle.CTL_ShowHistoryTitle(), Bundle.MSG_NoHistory(path));
                }
            }
        });
    }

    public static void register(final JTextPane textPane) {
        if(!isAvailable()) {
            return;
        }
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                final StyledDocument doc = textPane.getStyledDocument();
                String text = "";
                try {
                    text = doc.getText(0, doc.getLength());
                } catch (BadLocationException ex) {
                    Support.LOG.log(Level.SEVERE, null, ex);
                }
                final String comment = text;
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        final List<StackTracePosition> stacktraces = find(comment);
                        if (!stacktraces.isEmpty()) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    underlineStacktraces(doc, textPane, stacktraces, comment);

                                    textPane.removeMouseListener(getHyperlinkListener());
                                    textPane.addMouseListener(getHyperlinkListener());

                                    textPane.removeMouseMotionListener(getHyperlinkListener());
                                    textPane.addMouseMotionListener(getHyperlinkListener());
                                }
                            });
                        }
                    }
                };
                Support.getInstance().getParallelRP().post(r);
            }
        });
        
    }

    private static void underlineStacktraces(StyledDocument doc, JTextPane textPane, List<StackTracePosition> stacktraces, String comment) {
        Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style hlStyle = doc.addStyle("regularBlue-stacktrace", defStyle); // NOI18N
        hlStyle.addAttribute(HyperlinkSupport.STACKTRACE_ATTRIBUTE, new StackTraceAction());
        StyleConstants.setForeground(hlStyle, UIUtils.getLinkColor());
        StyleConstants.setUnderline(hlStyle, true);

        int last = 0;
        textPane.setText(""); // NOI18N
        for (StackTraceSupport.StackTracePosition stp : stacktraces) {
            int start = stp.getStartOffset();
            int end = stp.getEndOffset();

            if (last < start) {
                insertString(doc, comment, last, start, defStyle);
            }
            last = start;

            // for each line skip leading whitespaces (look bad underlined)
            boolean inStackTrace = (comment.charAt(start) > ' ');
            for (int i = start; i < end; i++) {
                char ch = comment.charAt(i);
                if ((inStackTrace && ch == '\n') || (!inStackTrace && ch > ' ')) {
                    insertString(doc, comment, last, i, inStackTrace ? hlStyle : defStyle);
                    inStackTrace = !inStackTrace;
                    last = i;
                }
            }

            if (last < end) {
                insertString(doc, comment, last, end, inStackTrace ? hlStyle : defStyle);
            }
            last = end;
        }
        try {
            doc.insertString(doc.getLength(), comment.substring(last), defStyle);
        } catch (BadLocationException ex) {
            Support.LOG.log(Level.SEVERE, null, ex);
        }
    }
      
    private static void insertString(final StyledDocument doc, final String comment, final int last, final int start, final Style defStyle) {
        try {
            doc.insertString(doc.getLength(), comment.substring(last, start), defStyle);
        } catch (BadLocationException ex) {
            Support.LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    private static MouseInputAdapter hyperlinkListener;
    private static MouseInputAdapter getHyperlinkListener() {
        if (hyperlinkListener == null) {
            hyperlinkListener = new MouseInputAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            Element elem = element(e);
                            AttributeSet as = elem.getAttributes();
                            StackTraceAction stacktraceAction = (StackTraceAction) as.getAttribute(HyperlinkSupport.STACKTRACE_ATTRIBUTE);
                            if (stacktraceAction != null) {
                                try {
                                    StackTraceAction.openStackTrace(elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset()), false);
                                } catch(Exception ex) {
                                    Support.LOG.log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    } catch(Exception ex) {
                        Support.LOG.log(Level.SEVERE, null, ex);
                    }
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    showMenu(e);
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    showMenu(e);
                }
                
                @Override
                public void mouseMoved(MouseEvent e) { }
                
                private Element element(MouseEvent e) {
                    JTextPane pane = (JTextPane)e.getSource();
                    StyledDocument doc = pane.getStyledDocument();
                    return doc.getCharacterElement(pane.viewToModel(e.getPoint()));
                }
                private void showMenu(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        try {
                            Element elem = element(e);
                            if (elem.getAttributes().getAttribute(HyperlinkSupport.STACKTRACE_ATTRIBUTE) != null) {
                                String stackFrame = elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());
                                JPopupMenu menu = new JPopupMenu();
                                menu.add(new StackTraceAction(stackFrame, false));
                                menu.add(new StackTraceAction(stackFrame, true));
                                menu.show((JTextPane)e.getSource(), e.getX(), e.getY());
                            }
                        } catch(Exception ex) {
                            Support.LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            };
        }
        return hyperlinkListener;
    }

    static class StackTraceAction extends AbstractAction {
        private String stackFrame;
        private boolean showHistory;

        StackTraceAction() {
        }

        StackTraceAction(String stackFrame, boolean showHistory) {
            this.stackFrame = stackFrame;
            this.showHistory = showHistory;
            String name = NbBundle.getMessage(StackTraceAction.class, showHistory ? "StackTraceSupport.StackTraceAction.showHistory" : "StackTraceSupport.StackTraceAction.open"); // NOI18N
            putValue(Action.NAME, name);
        }

        static void openStackTrace(String text, boolean showHistory) {
            if (showHistory) {
                findAndShowHistory(text);
            } else {
                findAndOpen(text);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            openStackTrace(stackFrame, showHistory);
        }
    }

}
