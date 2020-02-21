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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.awt.BorderLayout;
import java.io.IOException;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.windows.WindowManager;

/**
 * Miscellaneous utility classes useful for the Ipe module
 */
public class IpeUtils {

    public static String getMime(DataObject dob) {
	// migrated from DebugProjectAction
        FileObject primaryFile = dob == null ? null : dob.getPrimaryFile();
        String mime = primaryFile == null ? "" : primaryFile.getMIMEType();
        return mime;
    }

    /** Return an absolute, normalized version of the filename (which
	might be relative)
	@param path The path to be converted
	@return A normalized version of the filename, or null if there
	         is some problem (IOException etc. in File's getCanonicalPath
		 method)
    */
    public static String normalizePath(String path, FileSystem fs) {
	if (path == null)
	    return null;
	try {
            return FileSystemProvider.getCanonicalPath(fs, path);
	} catch (IOException e) {
	    return null;
	}
    }

    /**
     * Convert strings of the form "abc.xyz" to "abc".
     */

    public static String stripSuffix(String path) {
	int dotx = path.lastIndexOf('.');
	if (dotx == -1)
	    return path;	// already without a suffix
	else if (dotx == 0)
	    return "";		// NOI18N
	else
	    return path.substring(0, dotx);
    }


    /** Add quotes around the string if necessary.
	This is the case when the string contains space or meta characters.
	For now, we only worry about space, tab, *, [, ], ., ( and )
    */
    public static String quoteIfNecessary(String s) {
	int n = s.length();
	if (n == 0) {
	    // Don't quote empty strings ("")
	    return s;
	}
	// A quoted string in the first place?
	if ((s.charAt(0) == '"') ||
	    (s.charAt(n-1) == '"')) {
	    return s;
	}
	
	for (int i = 0; i < n; i++) {
	    char c = s.charAt(i);
	    if ((c == ' ') || (c == '\t') || (c == '*') ||
		(c == '[') || (c == ']') || (c == '.') ||
		(c == '(') || (c == ')')) {
		// Contains some kind of meta character == so quote the
		// darn thing
		return '"' + s + '"'; // NOI18N
	    }
	}
	return s;
    }
    
    public static String unquoteIfNecessary(String s) {
        if (s == null) {
            return null;
        }
        
        if (s.charAt(0) == '"') {
            s = s.substring(1);
        }
        
        if (s.charAt(s.length()-1) == '"') {
            s = s.substring(0, s.length()-1);
        }
        
        return s;
    }

    /**
     * Expand '~' and env variables in path.
     * Also strips off leading and trailing white space.
     *
     *	@param filename input string to be expanded
     *	@return the expanded string
     *
     * <P>Handles:
     * <ul>
     *   <li> If '~' is the first non-white space char, then:
     *     <ul>
     *	     <li> ~	    =>	home dir
     *	      <li> ~user    =>	user's home dir
     *	      <li> \~	    =>	~/
     *     </ul>
     *
     *   <li> If the environment variable a = "foo" and b = "bar" then:
     *     <ul>
     *	     <li> $a	    =>	foo
     *	     <li> $a$b	    =>	foobar
     *	     <li> $a.c	    =>	foo.c
     *	     <li> xxx$a	    =>	xxxfoo
     *	     <li> ${a}!	    =>	foo!
     *	     <li> \$a	    =>	$a
     *     </ul>
     * </ul>
     */
    /* OLD
    public static String expandPath(String filename) {
	int si = 0; // Index into 'source' (filename)
	int max = filename.length(); // Length of filename
	int beginIndex;
	int endIndex;
	StringBuffer dp = new StringBuffer(256); // Result buffer
	
	// Skip leading whitespace
	while (si < max && Character.isSpaceChar(filename.charAt(si))) {
	    si++;
	}

	// Expand ~ and ~user
	if (si < max && filename.charAt(si) == '~') {
	    if (si++ < max && (si == max || filename.charAt(si) == '/')) {
		// ~/filename
		dp.append(System.getProperty("user.home"));    // NOI18N
	    } else { // ~user/filename
		PasswordEntry pent = new PasswordEntry();
		beginIndex = si;
		while (si < max && filename.charAt(si) != '/') {
		    si++;
		}

		if (pent.fillFor(filename.substring(beginIndex, si))) {
		    dp.append(pent.getHomeDirectory());
		} else {
		    // lookup failed - use raw string
		    dp.append(filename.substring(beginIndex, si));
		}
	    }
	}

	//
        // Expand inline environment variables
        //
	while (si < max) {
	    char c = filename.charAt(si++);
	    if (c == '\\' && si < max) {
		if (filename.charAt(si) == '$') {
		    // Don't try and expand it as an environment
		    // variable. It is being escaped
		    dp.append('\\');
		    dp.append('$');
		    si++;			// skip over the '$'
		} else {
		    // Don't loose the escaped character
		    dp.append(c);
		}
	    } else if (c == '$' && si < max && filename.charAt(si) == '(') {
		// A Make variable
		endIndex = filename.indexOf(')', si);
		dp.append('$');
		if (endIndex > -1) {
		    dp.append(filename.substring(si, endIndex));
		    si = endIndex;
		} else {
		    // this is probably an error but we just pass it through
		    dp.append(filename.substring(si));
		    si = max;
		}
	    } else if (c == '$' && si < max) {
		// An environment variable!
		boolean braces = (filename.charAt(si) == '{');
		
		if (braces) { // skip over left brace
		    si++;
		}
		
		// Find end of environment variable
		beginIndex = si;
		while (si < max) {
		    char c2 = filename.charAt(si);
		    if (braces && c2 == '}') {
			break;
		    }
		    if (!(Character.isLetterOrDigit(c2) || (c2 == '_'))) {
			break;
		    }
		    si++;
		}
		
		endIndex = si;
		if ((si < max) && braces) {
		    si++; // skip over right brace
		}
		
		if (endIndex > beginIndex) {
		    String value = System.getenv(
			      filename.substring(beginIndex, endIndex));
		    
		    if (value != null) {
			dp.append(value);
		    } else {
			// Bad/unknown env variable: Put it back in
			// the string (it might be a filename)
			dp.append('$');
			if (braces) {
			    dp.append('{');
			}
			dp.append(filename.substring(beginIndex, endIndex));
			if (braces) {
			    dp.append('}');
			}
		    }
		} else {
		    // Empty string
		    dp.append('$');
		    if (braces) {
			dp.append("{}");				//NOI18N
		    }
		}
	    } else {
		// Just add the character
		dp.append(c);
	    }
	}
	
	return dp.toString();
    }
     */

    /** Compare two boolean values and return 0 if they are equal,
	-1 if the first is "less" than the second, and 1 if the first
	is "greater" than the second.  false is considered less than
	true. This imposes a sorting order on boolean values which
	is used for instance in many debugger tables. */

    public static int boolCompare(boolean x, boolean y) {
	if (x == y) {
	    return 0;
	}
	if (x) {
	    return 1;
	} else {
	    return -1;
	}
    }

    /**
     * Same String within "size"
     */
    public static boolean sameString(String a, String b, int size) {
	if (a == null) {
	    return (b == null);
	} else if (b == null) {
	    return false;
	} else {
	    String c = new String(a);
	    String d = new String(b);
	    if (a.length() > size)
		c = c.substring(0,size);
	    if (b.length() > size)
		d = d.substring(0,size);
	    return c.equals(d);
	}
    }

    /**
     * Same as String.equals, but allows arguments to be null
     */
    public static boolean sameString(String a, String b) {
	if (a == null) {
	    return (b == null);
	} else if (b == null) {
	    return false;
	} else {
	    return a.equals(b);
	}
    }

    /**
     * Return true if 'str' is null or is empty after trimming.
     */
    public static boolean isEmpty(String str) {
	return str == null || str.trim().length() == 0;
    }

    /**
     * Apply 'equals' to two arrays of Strings
     */
    public static boolean sameStringArray(String[] a, String[] b) {
	if (a == b)
	    return true;
	if (a == null || b == null)
	    return false;
	if (a.length != b.length)
	    return false;
	for (int x = 0; x < a.length; x++) {
	    if (!IpeUtils.sameString(a[x], b[x]))
		return false;
	}
	return true;
    }

    public static String [] cloneStringArray(String [] array) {
	if (array == null)
	    return null;
	String[] clone = new String[array.length];
	System.arraycopy(array, 0, clone, 0, array.length);
	return clone;
    }
    
    // Utility to request focus for a component by using the 
    // swing utilities to invoke it at a later
    //
    // I commented this out and things seeemed to be OK, but
    // it's hard to tell what was broken and hard to judge if
    // this workaround is no longer neccessary in newer jdk's so we leave it.
    // cnds IpeUtils and makefile wizard still use this too.

    public static void requestFocus(final Component c) {
	SwingUtilities.invokeLater(new Runnable() {
                @Override
		public void run() {
		    if (c != null) {
			if (c.getParent() != null) {
			    try {
				c.requestFocus();
			    } catch (NullPointerException npe) {
				// Throw away the npe. This is probably due to
				// the parent of this component not existing 
				// before we're through processing the 
				// requestFocus() call. This can happen when
				// quickly clicking through a wizard.
			    }
			}
		    }
		}
	    });
    }

    /**
     * Reimplementation of the very useful JOptionPane.getWindowForComponent()
     * which is package private.
     */
    static public Window getWindowForComponent(Component parent) {
	if (parent == null) {
	    return (Window) WindowManager.getDefault().getMainWindow();
	} else if (parent instanceof Frame || parent instanceof Dialog) {
	    return (Window) parent;
	} else {
	    return getWindowForComponent(parent.getParent());	// recurse
	}
    }

    public static JDialog createDialog(Component parent,
				String title,
				JComponent content) {
	final JDialog dialog;
        Window window = IpeUtils.getWindowForComponent(parent);
        if (window instanceof Frame) {
            dialog = new JDialog((Frame)window, title, true);
        } else {
            dialog = new JDialog((Dialog)window, title, true);
        }

        Container contentPane = dialog.getContentPane();
        contentPane.add(content, java.awt.BorderLayout.CENTER);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
	return dialog;
    }

    /* LATER
    // Not fully baked yet
    public static class OurFileUtil {
	public static boolean isAbsolute(String path) {
	    return path.startsWith(File.separator);
	}

	public static String first(String path) {
	    if (path.startsWith(File.separator))
		path = path.substring(1);
	    int sepx = path.indexOf(File.separatorChar);
	    if (sepx == -1)
		return path;
	    else
		return path.substring(0, sepx);
	}

	public static String rest(String path) {
	    if (path.startsWith(File.separator))
		path = path.substring(1);
	    int sepx = path.indexOf(File.separatorChar);
	    if (sepx == -1)
		return "";
	    else
		return path.substring(sepx+1);
	}

	public static boolean beginsWith(String path, String pc) {
	    if (pc.startsWith(File.separator))
		pc = pc.substring(1);
	    return first(path).equals(pc);
	}

	static {
	    try {
		assert first("/a/b/c").equals("a");
		assert first("a/b/c").equals("a");
		assert first("a").equals("a");

		assert rest("/a/b/c").equals("b/c");
		assert rest("a/b/c").equals("b/c");
		assert rest("a").equals("");

		assert beginsWith("/a/b", "a") == true;
		assert beginsWith("a/b", "a") == true;
		assert beginsWith("/a/b", "/a") == true;
		assert beginsWith("a/b", "/a") == true;
		assert beginsWith("/a", "a") == true;
		assert beginsWith("a", "/a") == true;

	    } catch (RuntimeException x) {
	    }
	}
    }
     */
    
    /*
     * Manage error dialogs
     * SHOULD this be per debugger engine or global?
     */
    private static Document errorDoc = null;
    private static Dialog errorDialog = null;

    /**
     * Post a non-modal error message which ...
     * <ul>
     * <li>May contain newlines.
     * <li>Is copy-able out of the dialog.
     * </ul>
     * @param error text of the error message.
     */
    public static void postError(String error) {
	JTextArea textArea = null;
	if (errorDoc == null) {
	    // Used below
	    textArea = new JTextArea();
	    errorDoc = textArea.getDocument();
	}

	try {
	    errorDoc.insertString(errorDoc.getLength(), error, null);
	} catch (BadLocationException x) {
	}

	assert SwingUtilities.isEventDispatchThread();
        if (DebuggerOption.DO_NOT_POPUP_DEBUGGER_ERRORS_DIALOG.isEnabled(NativeDebuggerManager.get().globalOptions())) {
            //use top component
            //LogVi
            DebuggerErrorsTopComponent.findInstance().setErrorDoc(errorDoc);
            return;
        }
	if (errorDialog == null) { // Not already showing...

	    JPanel ev = new JPanel();
	    ev.setLayout(new BorderLayout());
            if (textArea == null) {
                textArea = new JTextArea();
                textArea.setDocument(errorDoc);
            }
	    textArea.setEditable(false);
	    textArea.setWrapStyleWord(true);
	    textArea.setLineWrap(true);
	    textArea.setBackground((java.awt.Color) javax.swing.UIManager.getDefaults().get("Label.background")); // NOI18N
	    textArea.setBorder(BorderFactory.createEmptyBorder());
	    JScrollPane scrollPane = new JScrollPane(textArea);
	    scrollPane.setVerticalScrollBarPolicy(
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    scrollPane.setHorizontalScrollBarPolicy(
		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    ev.setPreferredSize(new Dimension(500, 100));
	    ev.add(scrollPane, BorderLayout.CENTER);

	    Catalog.setAccessibleName(textArea,
		"ACSN_DebuggerErrorDialogTxt");// NOI18N
	    Catalog.setAccessibleDescription(textArea,
		"ACSD_DebuggerErrorDialogTxt"); // NOI18N

	    DialogDescriptor descriptor = new DialogDescriptor(
		ev,
		Catalog.get("DebuggerError")); // NOI18N
	    descriptor.setOptionsAlign(DialogDescriptor.BOTTOM_ALIGN);
	    descriptor.setModal(false);
	    descriptor.setMessageType(DialogDescriptor.ERROR_MESSAGE);
	    descriptor.setOptions(new Object[]{
		    DialogDescriptor.OK_OPTION
		});
	    ActionListener errorListener = new ActionListener() {

                @Override
		public void actionPerformed(ActionEvent event) {
		    if (event.getSource() == DialogDescriptor.OK_OPTION ||
			event.getSource() == NotifyDescriptor.CLOSED_OPTION) {

			errorDialog.dispose();
			errorDialog = null;
			errorDoc = null;
			return;
		    }
		}
	    };
	    descriptor.setButtonListener(errorListener);
	    errorDialog = DialogDisplayer.getDefault().createDialog(descriptor);
	    Catalog.setAccessibleDescription(errorDialog,
		"ACSD_DebuggerErrorDialog");	// NOI18N

	    // This is a workaround for the following problem:
	    // when the dialog (created by 'createDialog' API) is closed
	    // via Alt-F4 key or via frame's close button, i.e. when
	    // dialog receives windowClosing event, the 'actionPerformed'
	    // method is not called - on this event the dialog only sets
	    // the value NotifyDescriptor.CLOSED_OPTION to the dialog descriptor
	    // and disposed. Since our dialog is not modal, we cannot
	    // check this value right after show() call. So we have to
	    // add one more window listener to the dialog in order to set
	    // errorDialog to null.
	    errorDialog.addWindowListener(new java.awt.event.WindowAdapter() {

		@Override
		public void windowClosing(final java.awt.event.WindowEvent p1) {
		    errorDialog = null;
		    errorDoc = null;
		}
	    });

	    errorDialog.setVisible(true);
	}
    }
}
