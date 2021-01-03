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
package org.netbeans.modules.python.debugger;

import java.awt.EventQueue;
import org.openide.text.Line;
import org.openide.text.Annotatable;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JEditorPane;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import javax.swing.text.StyledDocument;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.modules.python.debugger.backend.PluginEvent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.text.NbDocument;
import org.openide.windows.TopComponent;

/**
 * Misc debugger utility classes
 */
public class Utils {

  private final static String _PY_ = "py";
  private final static String _OSNAME_ = "os.name";
  private final static String _WINDOWS_ = "Windows";
  private final static String _SLASH_ = "/";
  private static Object _currentLine = null;

  static void markCurrent(final Object line) {
    // safely return on null line (issue 150371)
    if (line == null) {
      return;
    }
    unmarkCurrent();

    Annotatable[] annotatables = (Annotatable[]) line;
    int i = 0, k = annotatables.length;

    // first line with icon in gutter
    DebuggerAnnotation[] annotations = new DebuggerAnnotation[k];
    if (annotatables[i] instanceof Line.Part) {
      annotations[i] = new DebuggerAnnotation(
              DebuggerAnnotation.CURRENT_LINE_PART_ANNOTATION_TYPE,
              annotatables[i]);
    } else {
      annotations[i] = new DebuggerAnnotation(
              DebuggerAnnotation.CURRENT_LINE_ANNOTATION_TYPE,
              annotatables[i]);
    }

    // other lines
    for (i = 1; i < k; i++) {
      if (annotatables[i] instanceof Line.Part) {
        annotations[i] = new DebuggerAnnotation(
                DebuggerAnnotation.CURRENT_LINE_PART_ANNOTATION_TYPE2,
                annotatables[i]);
      } else {
        annotations[i] = new DebuggerAnnotation(
                DebuggerAnnotation.CURRENT_LINE_ANNOTATION_TYPE2,
                annotatables[i]);
      }
    }
    _currentLine = annotations;

    showLine(line);
  }

  static void unmarkCurrent() {
    if (_currentLine != null) {

//            ((DebuggerAnnotation) currentLine).detach ();
      int i, k = ((DebuggerAnnotation[]) _currentLine).length;
      for (i = 0; i < k; i++) {
        ((DebuggerAnnotation[]) _currentLine)[i].detach();
      }

      _currentLine = null;
    }
  }

  public static boolean contains(Object currentLine, Line line) {
    if (currentLine == null) {
      return false;
    }
    final Annotatable[] a = (Annotatable[]) currentLine;
    int i, k = a.length;
    for (i = 0; i < k; i++) {
      if (a[i].equals(line)) {
        return true;
      }
      if (a[i] instanceof Line.Part &&
              ((Line.Part) a[i]).getLine().equals(line)) {
        return true;
      }
    }
    return false;
  }

  public static void showLine(final Object line) {
//        SwingUtilities.invokeLater (new Runnable () {
//            public void run () {
//                ((Line) line).show (Line.SHOW_GOTO);
//            }
//        });

    final Annotatable[] a = (Annotatable[]) line;
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        if (a[0] instanceof Line) {
          ((Line) a[0]).show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
        } else if (a[0] instanceof Line.Part) {
          ((Line.Part) a[0]).getLine().show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
        } else {
          throw new InternalError();
        }
      }
    });
  }

  public static int getLineNumber(Object line) {
//        return ((Line) line).getLineNumber ();

    final Annotatable[] a = (Annotatable[]) line;
    if (a[0] instanceof Line) {
      return ((Line) a[0]).getLineNumber();
    } else if (a[0] instanceof Line.Part) {
      return ((Line.Part) a[0]).getLine().getLineNumber();
    } else {
      throw new InternalError();
    }
  }

  /**
  format current line as expected by NETBEANS editor from the python's event
  current line
  TODO  : complete and make a tiny test to make the current line Icon show up
  at the right place
   */
  public static Object getLine(final PluginEvent event) {
    File file = new File(event.get_source());
    final int lineNumber = event.get_line();
    if (file == null) {
      return null;
    }
    if (lineNumber < 0) {
      return null;
    }

    FileObject fileObject = FileUtil.toFileObject(file);
    EditorCookie editor;
    LineCookie lineCookie;

    try {
      DataObject d = DataObject.find(fileObject);
      editor = (EditorCookie) d.getCookie(EditorCookie.class);
      lineCookie = (LineCookie) d.getCookie(LineCookie.class);
      assert editor != null;
      assert lineCookie != null;
      StyledDocument doc = editor.openDocument();

      final int[] line = new int[1];
      Line l = lineCookie.getLineSet().getCurrent(lineNumber - 1);

      Annotatable[] annotatables = new Annotatable[1];
      annotatables[0] = l;

      return annotatables;
    } catch (IOException | IndexOutOfBoundsException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Object getLineAnnotatable(final String filePath, final int lineNumber) {
    Annotatable[] annotables = null;

    if (filePath == null || lineNumber < 0) {
      return null;
    }

    File file = new File(filePath);
    FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
    if (fileObject == null) {
      System.out.println("Cannot resolve \"" + filePath + '"');
      return null;
    }

    LineCookie lineCookie = getLineCookie(fileObject);
    assert lineCookie != null;
    Line line = lineCookie.getLineSet().getCurrent(lineNumber);
    annotables = new Annotatable[]{line};
    return annotables;
  }

  public static LineCookie getLineCookie(final FileObject fo) {
    LineCookie result = null;
    try {
      DataObject dataObject = DataObject.find(fo);
      if (dataObject != null) {
        result = (LineCookie) dataObject.getCookie(LineCookie.class);
      }
    } catch (DataObjectNotFoundException e) {
      System.out.println("Cannot find DataObject for: " + fo + " :" + e.getMessage());
    }
    return result;
  }

  public static Object gotoLine(String fName, int lineNumber) {
    File file = FileUtil.normalizeFile(new File(fName));
    FileObject fileObject = FileUtil.toFileObject(file);
    EditorCookie editor;
    LineCookie lineCookie;

    try {
      DataObject d = DataObject.find(fileObject);
      editor = (EditorCookie) d.getCookie(EditorCookie.class);
      lineCookie = (LineCookie) d.getCookie(LineCookie.class);
      assert editor != null;
      assert lineCookie != null;
      StyledDocument doc = editor.openDocument();

      final int[] line = new int[1];
      Line l = lineCookie.getLineSet().getCurrent(lineNumber);
      Annotatable[] annotatables = new Annotatable[1];
      annotatables[0] = l;

      showLine(annotatables);

      return doc;
    } catch (IOException | IndexOutOfBoundsException e) {
      e.printStackTrace();
    }
    return null;
  }

  /** Return number of lines in the document */
  public static int getLineCount(BaseDocument doc) {
    int lineCnt;
    try {
      if (doc != null) {
        lineCnt = Utilities.getLineOffset(doc, doc.getLength()) + 1;
      } else {
        lineCnt = 1;
      }
    } catch (BadLocationException e) {
      lineCnt = 1;
    }
    return lineCnt;
  }

  public static String getPath(FileObject fo) {
    File osFile = FileUtil.toFile(fo);
    return osFile.getAbsolutePath();
  }

  /**
  return back document's file name from document instance
   */
  public static String getDocumentSource(Document doc) {
    Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
    FileObject f = null;
    if (sdp instanceof FileObject) {
      f = (FileObject) sdp;
    }
    if (sdp instanceof DataObject) {
      f = ((DataObject) sdp).getPrimaryFile();
    }
    if (f == null) {
      return null;
    }
    return getPath(f);
  }

  public static EditorCookie getEditorFromFileName(String fName) {
    FileObject sfo = FileUtil.getConfigFile(fName);
    // obtain dat object
    DataObject doDo;
    try {
      doDo = DataObject.find(sfo);
    } catch (DataObjectNotFoundException e) {
      return null;
    }

    return (EditorCookie) doDo.getCookie(EditorCookie.class);
  }

  public static Document getDocumentFromFileName(String fName) {
    EditorCookie editor = getEditorFromFileName(fName);
    if (editor == null) {
      return null;
    }
    return editor.getDocument();
  }

  /**
  open provided source name inside editor
   */
  public static Document displaySource(String source) {
    File file = new File(source);
    FileObject fileObject = FileUtil.toFileObject(file);

    try {
      DataObject d = DataObject.find(fileObject);

      EditorCookie editor;
      editor = (EditorCookie) d.getCookie(EditorCookie.class);
      assert editor != null;
      Document doc = editor.openDocument();
      return doc;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static boolean isPythonSource(FileObject fo) {
    // check for Python stuff
    if (fo.getExt().equals(_PY_)) {
      return true;
    }
    return false;
  }

  
  public static Line getLine(String url, int lineNumber) {
    FileObject file;
    try {
      file = URLMapper.findFileObject(new URL(url));
    } catch (MalformedURLException e) {
      return null;
    }
    if (file == null) {
      return null;
    }
    DataObject dataObject = null;
    try {
      dataObject = DataObject.find(file);
    } catch (DataObjectNotFoundException ex) {
      return null;
    }
    if (dataObject == null) {
      return null;
    }
    LineCookie lineCookie = (LineCookie) dataObject.getCookie(LineCookie.class);
    if (lineCookie == null) {
      return null;
    }
    Line.Set ls = lineCookie.getLineSet();
    if (ls == null) {
      return null;
    }
    try {
      return ls.getCurrent(lineNumber);
    } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
    }
    return null;
  }
  

  public static Line getCurrentLine() {
    Node[] nodes = TopComponent.getRegistry().getCurrentNodes();
    if (nodes == null) {
      return null;
    }
    if (nodes.length != 1) {
      return null;
    }
    Node n = nodes[0];
    FileObject fo = (FileObject) n.getLookup().lookup(FileObject.class);
    if (fo == null) {
      DataObject dobj = (DataObject) n.getLookup().lookup(DataObject.class);
      if (dobj != null) {
        fo = dobj.getPrimaryFile();
      }
    }
    if (fo == null) {
      return null;
    }
    if (!isPythonSource(fo)) {
      return null;
    }
    LineCookie lineCookie = (LineCookie) n.getCookie(LineCookie.class);
    if (lineCookie == null) {
      return null;
    }
    EditorCookie editorCookie = (EditorCookie) n.getCookie(EditorCookie.class);
    if (editorCookie == null) {
      return null;
    }
    JEditorPane jEditorPane = getEditorPane(editorCookie);
    if (jEditorPane == null) {
      return null;
    }
    StyledDocument document = editorCookie.getDocument();
    if (document == null) {
      return null;
    }
    Caret caret = jEditorPane.getCaret();
    if (caret == null) {
      return null;
    }
    int lineNumber = NbDocument.findLineNumber(document, caret.getDot());
    try {
      Line.Set lineSet = lineCookie.getLineSet();
      assert lineSet != null : lineCookie;
      return lineSet.getCurrent(lineNumber);
    } catch (IndexOutOfBoundsException ex) {
      return null;
    }
  }

  private static JEditorPane getEditorPane_(EditorCookie editorCookie) {
    JEditorPane[] op = editorCookie.getOpenedPanes();
    if ((op == null) || (op.length < 1)) {
      return null;
    }
    return op[0];
  }

  private static JEditorPane getEditorPane(final EditorCookie editorCookie) {
    if (SwingUtilities.isEventDispatchThread()) {
      return getEditorPane_(editorCookie);
    } else {
      final JEditorPane[] ce = new JEditorPane[1];
      try {
        EventQueue.invokeAndWait(new Runnable() {

          @Override
                  public void run() {
                    ce[0] = getEditorPane_(editorCookie);
                  }
                });
      } catch (InvocationTargetException | InterruptedException ex) {
        ex.printStackTrace();
      }
      return ce[0];
    }
  }
}  
