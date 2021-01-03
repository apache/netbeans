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
 */package org.netbeans.modules.python.debugger.utils;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;

/**

this class defines basic Logging functions using a swing Text Pane
with Text coloring calpabilities

 */
public class SwingMessageArea
        extends JPanel {

  public final static String PYTHON_SHELL_INVITE = ">>>";
  private final long _OBSERVER_SLEEP_TIME_ = 250;
  private final static Color _DEFAULT_BACKGROUND_COLOR_ = Color.black;
  private final static Color _DEFAULT_NORMAL_COLOR_ = Color.lightGray;
  private final static Color _DEFAULT_ERROR_COLOR_ = Color.red;
  private final static Color _DEFAULT_WARNING_COLOR_ = Color.magenta;
  private final static Color _DEFAULT_HEADER_COLOR_ = Color.cyan;
  private final static Border _LOWERED_BORDER_ = new SoftBevelBorder(BevelBorder.LOWERED);
  // private final static int    _MAX_LINES_ = 100 ; // Max size of area in lines

  // defining styles used inside document
  private final static String _ERROR_STYLE_ = "error";
  private final static String _HEADER_FOOTER_STYLE_ = "header_footer";
  private final static String _WARNING_STYLE_ = "warning";
  private Font _font = new Font("TimesRoman", 10, 0);
  // applicable document colors
  private Color _backgroundColor = _DEFAULT_BACKGROUND_COLOR_;
  private Color _normalColor = _DEFAULT_NORMAL_COLOR_;
  private Color _errorColor = _DEFAULT_ERROR_COLOR_;
  private Color _warningColor = _DEFAULT_WARNING_COLOR_;
  private Color _headerColor = _DEFAULT_HEADER_COLOR_;

  // availaible document styles
  private Style _messageStyle;
  private Style _errorStyle;
  private Style _headerStyle;
  private Style _warningStyle;
  private JTextPane _textPane; // Text area
  private _SCROLLER_ _scroller; // makes the text scrollable
  private int _curSize; // current Area Size in line
  private boolean _refresh = false;
  private _SHELL_OBSERVER_ _observer = new _SHELL_OBSERVER_();
  private CommandLineListener _listener = null;

  public synchronized void addCommandLineListener(CommandLineListener listener) {
    _listener = listener;
  }

  public synchronized void removeCommandLineListener(CommandLineListener listener) {
    if (_listener == listener) {
      _listener = null;
    }
  }

  public void hasEnabled(boolean enabled) {
    if (!enabled) {
      _observer.enterSleep();
    }
  }

  class _MESSAGE_POPUP_
          extends MouseAdapter {

    private JPopupMenu _popup = null;

    public _MESSAGE_POPUP_() {
    }

    private void createPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        _popup = new JPopupMenu();
        JMenuItem mi = _popup.add(new JMenuItem("clear message area"));
        mi.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent ev) {
            clearScreen();
          }
        });
        // a copy shortcut with CRTL C shortcut key
        JMenuItem mi1 = _popup.add(new JMenuItem("copy"));
        mi1.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        mi1.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent ev) {
            _textPane.copy();
          }
        });
        // cut 
        //JMenuItem mi2 = _popup.add(new JMenuItem("cut")) ;
        //mi2.addActionListener(new ActionListener()
        //    {  public void actionPerformed( ActionEvent e )
        //    { 
        //      _textPane.cut();  
        //    }
        //    } 
        //  )  ; 
        _popup.show(SwingMessageArea.this, e.getX(), e.getY() % _scroller.getSize().height);
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      createPopup(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
      createPopup(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      createPopup(e);
    }
  }

  public void set_refresh(boolean refresh) {
    _refresh = refresh;
  }

  class _SCROLLER_ extends JScrollPane {

    public _SCROLLER_(Component view) {
      super(view);
    }

    /**
    give the calpability to be located in SplitPane giving
    Full motion calpability to the SplitPane Divider
     */
    @Override
    public Dimension getMinimumSize() {
      return new Dimension(0, 0);
    }

    public float getAlignment() {
      return LEFT_ALIGNMENT;
    }
  }

  private void populateStyle(Style style, Font f, Color c) {
    style.addAttribute(StyleConstants.Foreground, c);
    style.addAttribute(StyleConstants.FontSize, (f.getSize()));
    style.addAttribute(StyleConstants.FontFamily, f.getFamily());
  }

  /**
  defines all the styles used by _textPane
  @param  no parameters
  @return none
   */
  public void setStyles() {
    // populate background color
    _textPane.setBackground(_backgroundColor);
    populateStyle(_messageStyle, _font, _normalColor);
    populateStyle(_errorStyle, _font, _errorColor);
    populateStyle(_warningStyle, _font, _warningColor);
    populateStyle(_headerStyle, _font, _headerColor);
  }

  private void initStyles() {
    // Use message style as default
    _messageStyle = _textPane.getStyle(StyleContext.DEFAULT_STYLE);
    _errorStyle = _textPane.addStyle(_ERROR_STYLE_, null);
    _warningStyle = _textPane.addStyle(_WARNING_STYLE_, null);
    _headerStyle = _textPane.addStyle(_HEADER_FOOTER_STYLE_, null);
  }

  /**
  remove the current first line of text

  @param  Document doc
  @exception BadLocationException
   */
  public void removeFirstLine() {
    Document doc = _textPane.getDocument();
    Element root = doc.getDefaultRootElement();

    int children = root.getElementCount();

    if (children > 1) {
      Element firstLine = root.getElement(0);
      try {
        int length = firstLine.getEndOffset() -
                firstLine.getStartOffset();
        doc.remove(firstLine.getStartOffset(), length);
        _curSize--;
      // TextDump.textDump(root);
      } catch (BadLocationException e) {
        e.printStackTrace();
      }
    }
  }

  /**
  remove all lines
   */
  public void clearScreen() {
    Document doc = _textPane.getDocument();
    Element root = doc.getDefaultRootElement();
    int children = root.getElementCount();
    while (children > 1) {
      Element firstLine = root.getElement(0);
      try {
        int length = firstLine.getEndOffset() -
                firstLine.getStartOffset();
        doc.remove(firstLine.getStartOffset(), length);
        _curSize--;
      // TextDump.textDump(root);
      } catch (BadLocationException e) {
        e.printStackTrace();
      }
      children = root.getElementCount();
    }
  }

  /** position window on last inserted line */
  private void refreshPosition() {
    Rectangle r = new Rectangle(0, _textPane.getHeight() - 2, 1, 1);
    _textPane.scrollRectToVisible(r);
    _textPane.validate();
  }

  class _INSERTER_
          extends Thread {

    private StringBuffer _msg = null;
    private Style _style;

    public _INSERTER_(String msg, Style style, boolean eol) {
      _style = style;
      if (msg != null) {
        _msg = new StringBuffer(msg);
        if (eol) {
          _msg.append('\n');
        }
      }
    }

    public void insert() {
      if (_msg != null) {
        synchronized (_textPane) {
          Document doc = _textPane.getDocument();

          try {
            doc.insertString(doc.getLength(),
                    _msg.toString(),
                    _style);
            _curSize++; // One more line Added
            if (_refresh) {
              refreshPosition();
            }
          } catch (BadLocationException e) {
            e.printStackTrace();
          }
        }
      }
    }

    @Override
    public void run() {
      insert();
    }
  }

  /**
  insert a newLine into document using given String and Style

  @param  String message insert it into area
  @param  Style  style used for message

  @return none
   */
  private void insertLine(String msg,
          Style style,
          boolean eol) {
    _INSERTER_ inserter = new _INSERTER_(msg, style, eol);
    try {
      SwingUtilities.invokeAndWait(inserter);
      _observer.setTimeStamp();
    } catch (InterruptedException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private void insertShellInvite() {
    _INSERTER_ inserter = new _INSERTER_(PYTHON_SHELL_INVITE, _messageStyle, false);
    SwingUtilities.invokeLater(inserter);

  }

  class _INPUT_KEY_
          extends KeyAdapter {

    private void parseCommand() {
      Document doc = _textPane.getDocument();
      int curPos = _textPane.getCaretPosition();
      try {
        String command = doc.getText(0, curPos);
        curPos--;
        int commandStartPos = -1;
        while (curPos >= 2) {
          if ((command.charAt(curPos) == '>') &&
                  (command.charAt(curPos - 1) == '>') &&
                  (command.charAt(curPos - 2) == '>')) {
            commandStartPos = curPos + 1;
            break;
          } else if (command.charAt(curPos) == '\n') // give upt if endof line is before >>>
          {
            break;
          }
          curPos--;
        }
        if (commandStartPos == -1) {
          shellInvite();
          return;
        }
        if (commandStartPos < command.length()) {
          command = command.substring(commandStartPos);
          if (command.charAt(0) == '\n') {
            // empty command ignore
            shellInvite();
            return;
          }
          if (_listener != null) {
            _listener.commandEntered(new CommandLineEvent(command));
          }
          // awake observer after command submission
          _observer.awake();
        } else {
          shellInvite();
        }

      } catch (BadLocationException e) {
      }
    }

    @Override
    public void keyPressed(KeyEvent ke) {
      // suspend shell observer while typing commands
      _observer.enterSleep();
      char entered = ke.getKeyChar();
      if (entered == '\n') // ENTER key
      {
        parseCommand();
      }
    }
  }

  private void init() {
    setLayout(new BorderLayout()); // request a sample BorderLayout
    _textPane = new JTextPane();
    _textPane.setEditable(true);

    _textPane.addKeyListener(new _INPUT_KEY_());
    setBorder(_LOWERED_BORDER_); // surround component with given border
    _scroller = new _SCROLLER_(_textPane);
    _textPane.addMouseListener(new _MESSAGE_POPUP_());
    initStyles(); // build textPane used document styles 
    setStyles(); // populate initial colors
    // start the shell observer
    _observer.start();

    add(BorderLayout.CENTER, _scroller);
  }

  /**
  MessageArea constructor
   */
  public SwingMessageArea() {
    init();
  }

  public void populateGUIInfos(Font font,
          Color background,
          Color header,
          Color error,
          Color warning,
          Color normal) {
    boolean guiChanged = false;
    if ((font != null) && (_font != font)) {
      _font = font;
      guiChanged = true;
    }
    if ((background != null) && (_backgroundColor != background)) {
      _backgroundColor = background;
      guiChanged = true;
    }
    if ((header != null) && (_headerColor != header)) {
      _headerColor = header;
      guiChanged = true;
    }
    if ((error != null) && (_errorColor != error)) {
      _errorColor = error;
      guiChanged = true;
    }
    if ((warning != null) && (_warningColor != warning)) {
      _warningColor = warning;
      guiChanged = true;
    }
    if ((normal != null) && (_normalColor != normal)) {
      _normalColor = normal;
      guiChanged = true;
    }
    if (guiChanged) {
      setStyles();
    }
  }

  /**
  MessageArea constructor with customized colors
   */
  public SwingMessageArea(Font font,
          Color background,
          Color header,
          Color error,
          Color warning,
          Color normal) {
    _font = font;
    _backgroundColor = background;
    _normalColor = normal;
    _warningColor = warning;
    _headerColor = header;
    _errorColor = error;

    init();
  }

  /**
  insert a new message on screen
  @param  String msg the one to be displayed
  @return none
   */
  public void message(String msg) {
    insertLine(msg, _messageStyle, true);
  }

  public void messageAppend(String msg) {
    insertLine(msg, _messageStyle, false);
  }

  public void headerAppend(String msg) {
    insertLine(msg, _headerStyle, false);
  }

  /**
  insert a new error on screen
  @param  String msg the one to be displayed
  @return none
   */
  public void error(String msg) {
    insertLine(msg, _errorStyle, true);
  }

  /**
  insert a new warning on screen
  @param  String msg the one to be displayed
  @return none
   */
  public void warning(String msg) {
    insertLine(msg, _warningStyle, true);
  }

  /**
  insert a new header_footer on screen
  @param  String msg the one to be displayed
  @return none
   */
  public void headerFooter(String msg) {
    insertLine(msg, _headerStyle, true);
  }

  class _SHELL_OBSERVER_
          extends Thread {

    private long _sleepTime = _OBSERVER_SLEEP_TIME_;
    private long _lastDisplay = System.currentTimeMillis();
    private boolean _sleeping = true;
    private boolean _running = true;

    public synchronized void setTimeStamp() {
      _lastDisplay = System.currentTimeMillis();
      if (_sleeping) {
        awake();
      }
    }

    public void enterSleep() {
      _sleeping = true;
    }

    public void awake() {
      _sleeping = false;
      super.interrupt();
    }

    @Override
    public void run() {
      while (_running) {
        try {
          if (_sleeping) {
            Thread.sleep(Long.MAX_VALUE);
          } else {
            Thread.sleep(_sleepTime);
            if (!_sleeping) // check that it has not been entering sleeping
            {
              long cur = System.currentTimeMillis();
              if ((cur - _lastDisplay) > _sleepTime) {
                // check Shell at bottom of doc
                shellInvite();
              }
            }
          }
        } catch (InterruptedException e) {
        }
      }
    }
  }

  private synchronized void shellInvite() {
    Document doc = _textPane.getDocument();
    int length = doc.getLength();
    int inviteLength = PYTHON_SHELL_INVITE.length();
    try {
      String endOfText = doc.getText(length - inviteLength, inviteLength);
      // append >>> python shell invite
      if (endOfText.equals(PYTHON_SHELL_INVITE)) {
        return; // already in place
      }
      insertShellInvite();
      // and position the caret after
      _textPane.setCaretPosition(doc.getLength());
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
  the main method is allways devoted to unit test purposes
   */
  public static void main(String argv[]) {
    // Exit the debug window frame
    class WL extends WindowAdapter {

      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    }

    class _MESSAGE_INSERTER_ extends Thread {

      private int _howMuch;
      private SwingMessageArea _message;
      private int _count;

      public _MESSAGE_INSERTER_(int howMuch,
              SwingMessageArea message) {
        _howMuch = howMuch;
        _message = message;
      }

      @Override
      public void run() {
        Runtime rt = Runtime.getRuntime();
        // insert some stuffs
        for (int ii = 1; ii < _howMuch; ii++) {
          long started = System.currentTimeMillis();
          _message.headerFooter("displaying a test message group : " + _count + " started");
          _message.message(ii + " : OPPING : register functions");
          _message.message(ii + " : ECHO : register functions");
          _message.message(ii + " : ARCF : register functions");
          _message.message(ii + " : SPL : register functions");
          _message.message(ii + " : OPPING : init");
          _message.message(ii + " : ECHO : init");
          _message.message(ii + " : ARCF : init");
          _message.message(ii + " : SPLO : init");
          _message.warning(ii + " WARNING : will be displayed this way");
          _message.error(ii + " ERROR : severe errors will be displayed this way");
          _message.message("Trying message !!!!!!!!!!!!!");
          _message.headerFooter("displaying message group : " + _count++ + " ended");
          _message.headerFooter("elapsed : " + (System.currentTimeMillis() - started) +
                  " free memory : " + rt.freeMemory());
          try {
            Thread.sleep(5); // give AWT thread a timeSlice
          } catch (InterruptedException ex) {
          }
        }

      }
    }

    class _ADD_MESSAGE_ implements ActionListener {

      private SwingMessageArea _message; // Acting on that guy

      public _ADD_MESSAGE_(SwingMessageArea message) {
        _message = message;
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        _message.error("test new error message insert");
      }
    }

    class _REMOVEALL_PRESSED_ implements ActionListener {

      private SwingMessageArea _message; // Acting on that guy

      public _REMOVEALL_PRESSED_(SwingMessageArea message) {
        _message = message;
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        _message.clearScreen();
      }
    }

    class _REMOVE_PRESSED_ implements ActionListener {

      private SwingMessageArea _message; // Acting on that guy

      public _REMOVE_PRESSED_(SwingMessageArea message) {
        _message = message;
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        long started = System.currentTimeMillis();
        _message.removeFirstLine();
        System.out.println("elapsed : " + (System.currentTimeMillis() - started));
      }
    }

    JFrame f = new JFrame(" *** Net Deamon ***");
    f.getContentPane().setLayout(new BorderLayout());
    Font fnt = new Font("TimesRoman", 10, 0);

    SwingMessageArea message = new SwingMessageArea(fnt,
            Color.lightGray,
            Color.blue,
            Color.red,
            Color.magenta,
            Color.black);

    JButton removeAll = new JButton("removeAll");
    removeAll.addActionListener(new _REMOVEALL_PRESSED_(message));

    JButton addMsg = new JButton("Add Message");
    addMsg.addActionListener(new _ADD_MESSAGE_(message));

    JButton remove = new JButton("remove");
    remove.addActionListener(new _REMOVE_PRESSED_(message));

    f.setForeground(Color.black);
    f.setBackground(Color.lightGray);
    f.addWindowListener(new WL());

    f.getContentPane().add("Center", message);
    f.getContentPane().add("South", removeAll);
    f.getContentPane().add("East", addMsg);
    new _MESSAGE_INSERTER_(50, message).start();
    f.getContentPane().add("North", remove);

    f.setSize(400, 200);
    f.setVisible(true);
  }
}
