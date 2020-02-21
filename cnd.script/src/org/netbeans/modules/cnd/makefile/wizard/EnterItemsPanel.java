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

package  org.netbeans.modules.cnd.makefile.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.regex.PatternSyntaxException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.cnd.makefile.utils.IpeFileSystemView;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makefile.utils.UnixRE;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Create the Sources panel in the Makefile wizard.
 */

public abstract class EnterItemsPanel extends MakefileWizardPanel {

    /** Serial version number */
    static final long serialVersionUID = 5260017369797781413L;

    // the fields in the sources panel...
    private JLabel	entryLabel;
    private JTextField	entryText;
    private JButton	entryChooser;

    private JButton	addBtn;
    private JButton	changeBtn;
    private JButton	removeBtn;
    private JButton	upArrow;
    private JButton	downArrow;
    private JList	list;

    /** Store the file (possibly customized) file chooser */
    protected JFileChooser  fc;

    /** Expand a directory to a list of files if set */
    private boolean expandDirs;

    /** Use Directory Chooser rather than File Chooser */
    private boolean dirChooser;

    /** Use Directory abd file Chooser */
    private boolean dirAndFileChooser;

    /** Use MspFileFilter as file filter */
    private boolean mspFilter;

    /** Change the default button per change criteria */
    private boolean dynamicNext;

    /** Change the Last button if this is set */
    private boolean dynamicLast;

    /** Don't allow the Next or Last buttons to be enabled unless we have list items */
    private boolean itemsRequired;

    /** Add to beginning of list */
    private boolean addBeginning;

    /** Used to store source filters */
    private HashSet<UnixRE> filters;

    /** Last chooser directory the user visited */
    private File lastChooserDir;

    /**
     *  Show a dialog asking if a directory should be replaced with either its
     *  leaf files or its leaf files and all of its descendant directories leaf
     *  files
     */
    protected final static int EXPAND_DIRS = 0x1;

    /** Use a directory chooser rather than a regular file chooser */
    protected final static int DIR_CHOOSER = 0x2;

    /** Use the MspFileFilter class as the chooser's file filter */
    protected final static int MSP_FILTER = 0x4;

    /** Dynamically set the default button */
    protected final static int DYNAMIC_DEFAULT_BUTTONS = 0x8;

    /** Dynamically set the Last button */
    protected final static int DYNAMIC_LAST_BUTTON = 0x10;

    /** We require list items to go to the next (or last) panel */
    protected final static int ITEMS_REQUIRED = 0x20;

    /** Add to beginning of list */
    protected final static int ADD_BEGINNING = 0x40;

    /** Use a directory and file chooser */
    protected final static int DIR_AND_FILE_CHOOSER = 0x80;

    /** The nextButton is set if we are dynamically setting the default */
    private JButton nextButton;

    /** Remember if Last is enabled */
    private boolean lastEnabled;


    /**
     * Constructor for the Makefile sources panel. Remember, most of the panel
     * is inherited from WizardDescriptor.
     */

    public EnterItemsPanel(MakefileWizard wd) {
	super(wd);

	nextButton = null;
	lastChooserDir = null;
	lastEnabled = false;
    }


    /**
     *  Create the panel. Since this class is used as a superclass for several
     *  panels information is passed in to allow some customization at creation
     *  time.
     *
     *  @param label	The label for the text field at the top of the panel
     *  @param flags	Set some optional flags to override default behavior
     */
    protected void create(String label, char mnemonic, int flags) {
	int gridy = 0;

	expandDirs = (flags & EXPAND_DIRS) == EXPAND_DIRS;
	dirChooser = (flags & DIR_CHOOSER) == DIR_CHOOSER;
        dirAndFileChooser = (flags & DIR_AND_FILE_CHOOSER) == DIR_AND_FILE_CHOOSER;
	mspFilter = (flags & MSP_FILTER) == MSP_FILTER;
	dynamicNext = (flags & DYNAMIC_DEFAULT_BUTTONS) == DYNAMIC_DEFAULT_BUTTONS;
	dynamicLast = (flags & DYNAMIC_LAST_BUTTON) == DYNAMIC_LAST_BUTTON;
	itemsRequired = (flags & ITEMS_REQUIRED) == ITEMS_REQUIRED;
	addBeginning = (flags & ADD_BEGINNING) == ADD_BEGINNING;



	JPanel mainPanel = new JPanel();
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();

	if (dynamicNext) {
	    nextButton = MakefileWizard.getMakefileWizard().getNextButton();
	}

        setLayout(new java.awt.GridBagLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        mainPanel.add(createTextPanel(label, mnemonic), gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        mainPanel.add(createButtonRow1(), gridBagConstraints);

	// Create the scrolling list of sources and related buttons...
	JLabel listLabel = new JLabel(getListLabel());
	listLabel.setDisplayedMnemonic(getListMnemonic());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        mainPanel.add(listLabel, gridBagConstraints);

	list = new JList(new DefaultListModel());
	listLabel.setLabelFor(list);
	JScrollPane sp = new JScrollPane(list);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(sp, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        mainPanel.add(createButtonRow2(), gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(mainPanel, gridBagConstraints);




	/*
        setLayout(new GridBagLayout());
	GridBagConstraints grid = new GridBagConstraints();
	grid.anchor = GridBagConstraints.NORTHWEST;
	grid.gridx = 0;

	if (dynamicNext) {
	    nextButton = MakefileWizard.getMakefileWizard().getNextButton();
	}

	JPanel textPanel = createTextPanel(label, mnemonic);
	grid.gridy = gridy++;
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.weightx = 1.0;
	add(textPanel, grid);

	JPanel row1 = createButtonRow1();
	grid.gridy = gridy++;
	grid.fill = GridBagConstraints.NONE;
	grid.insets.top = 5;
	grid.weightx = 0.0;
	add(row1, grid);

	// Create the scrolling list of sources and related buttons...
	JLabel listLabel = new JLabel(getListLabel());
	listLabel.setDisplayedMnemonic(getListMnemonic());
	grid.gridy = gridy++;
	grid.insets.top = 11;
	add(listLabel, grid);

	list = new JList(new DefaultListModel());
	listLabel.setLabelFor(list);
	JScrollPane sp = new JScrollPane(list);
	grid.gridy = gridy++;
	grid.fill = GridBagConstraints.BOTH;
	grid.insets.top = 0;
	grid.weightx = 1.0;
	grid.weighty = 1.0;
	grid.gridheight = GridBagConstraints.RELATIVE;
	add(sp, grid);

	JPanel row2 = createButtonRow2();
	grid.gridy = gridy++;
	grid.fill = GridBagConstraints.NONE;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.gridheight = GridBagConstraints.REMAINDER;
	grid.insets.top = 5;
	grid.weighty = 0.0;
	add(row2, grid);
	*/


	/*
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	addComponent(createSysLibPanel(), gridBagConstraints);
	*/

	setupListeners();
    }

    void addComponent(JComponent component, GridBagConstraints gridx) {
	add(component, gridx);
    }

//    private JPanel createSysLibPanel() {
//	JPanel sysLibPanel = new JPanel();
//        sysLibPanel.setLayout(new java.awt.GridBagLayout());
//
//	JLabel sysLibLabel = new JLabel();
//        sysLibLabel.setText("Syslibs:");
//        sysLibPanel.add(sysLibLabel, new java.awt.GridBagConstraints());
//
//	JTextField sysLibTextField = new JTextField();
//        sysLibLabel.setLabelFor(sysLibTextField);
//        sysLibTextField.setEditable(false);
//        sysLibTextField.setText("jTextField1");
//        sysLibTextField.setFocusable(false);
//        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
//        sysLibPanel.add(sysLibTextField, gridBagConstraints);
//
//	return sysLibPanel;
//    }

    /** Override for custom label */
    protected String getListLabel() {
	return getString("LBL_ListLabel");				// NOI18N
    }

    /** Override for custom mnemonic */
    protected char getListMnemonic() {
	return getString("MNEM_ListLabel").charAt(0);			// NOI18N
    }


    /** Create the text panel and file chooser in its own panel */
    private JPanel createTextPanel(String wholeLabel, char mnemonic) {
	int gridy = 0;
	String label;

	GridBagConstraints grid = new GridBagConstraints();
	JPanel panel = new JPanel(new GridBagLayout());

	// In some cases the label is multiline. Since a JLabel is single line
	// we need to break up the label and create multiple JLabels.
	StringTokenizer tok = new StringTokenizer(wholeLabel, "\n");	// NOI18N
	label = tok.nextToken();

	// Create the textfield, its label and buttons.
        entryLabel = new JLabel(label);
	entryLabel.setDisplayedMnemonic(mnemonic);
	grid.anchor = GridBagConstraints.WEST;
	grid.gridx = 0;
	grid.gridy = gridy++;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	panel.add(entryLabel, grid);

	while (tok.hasMoreTokens()) {
	    label = tok.nextToken();
	    grid.gridy = gridy++;
	    panel.add(new JLabel(label), grid);
	}

        entryText = new JTextField();
	entryLabel.setLabelFor(entryText);
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.gridy = gridy++;
	grid.gridwidth = GridBagConstraints.RELATIVE;
	grid.weightx = 1.0;
	panel.add(entryText, grid);

        entryChooser = new JButton(getString("BTN_Chooser"));		// NOI18N
	entryChooser.setMnemonic(getString("MNEM_Chooser").charAt(0));	// NOI18N
	grid.fill = GridBagConstraints.NONE;
	grid.gridx = GridBagConstraints.RELATIVE;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.insets = new Insets(0, 5, 0, 0);
	grid.weightx = 0.0;
	panel.add(entryChooser, grid);
	createChooser(entryChooser);

	return panel;
    }


    /** Create the first row of buttons */
    private JPanel createButtonRow1() {
	JPanel row = new JPanel(new GridLayout(1, 2, 6, 0));

	addBtn = new JButton(getString("BTN_Add"));			// NOI18N
	addBtn.setMnemonic(getString("MNEM_Add").charAt(0));		// NOI18N
	addBtn.setEnabled(false);
	row.add(addBtn);

	changeBtn = new JButton(getString("BTN_Change"));		// NOI18N
	changeBtn.setMnemonic(getString("MNEM_Change").charAt(0));	// NOI18N
	changeBtn.setEnabled(false);
	row.add(changeBtn);

	return row;
    }


    /** Create the 2nd row of buttons */
    private JPanel createButtonRow2() {
	JPanel row = new JPanel(new GridLayout(1, 3, 6, 0));

	removeBtn = new JButton(getString("BTN_Remove"));		// NOI18N
	removeBtn.setMnemonic(getString("MNEM_Remove").charAt(0));	// NOI18N
	removeBtn.setEnabled(false);
	row.add(removeBtn);

	upArrow = new JButton(getString("BTN_Up"));			// NOI18N
	upArrow.setMnemonic(getString("MNEM_Up").charAt(0));		// NOI18N
	upArrow.setEnabled(false);
	row.add(upArrow);

	downArrow = new JButton(getString("BTN_Down"));			// NOI18N
	downArrow.setMnemonic(getString("MNEM_Down").charAt(0));	// NOI18N
	downArrow.setEnabled(false);
	row.add(downArrow);

	return row;
    }


    /** Return a pointer to the JList */
    public JList getList() {
	return list;
    }


    /** Return an array of all items collected in the list */
    public String[] getListItems() {
	Object[] o = ((DefaultListModel) list.getModel()).toArray();
	String[] s = new String[o.length];

	for (int i = 0; i < o.length; i++) {
	    s[i] = o[i].toString().trim();
	}

	return s;
    }


    /**
     *  Expand the text string to an array of (ListItem) objects.
     *
     *  Note: The currentDirectory may not exist or may not be a directory.
     *  We need to handle this gracefully. If it doesn't exist its not an error
     *  but we can't do any RE expansion or relative path lookup.
     *
     *  @param text	The complete string entered into the text field
     *  @return		An array of ListItem[] objects, one for each file name
     */
    private Object[] expandFileList(String text) {
	StringTokenizer	st = new StringTokenizer(text);
	LinkedList<ListItem>	aList = new LinkedList<ListItem>();
	TokType		type = new TokType();
	boolean		neFileAdded = false;
	int		tcount = 0;

	while (st.hasMoreTokens()) {
	    String tok = validateInput(st.nextToken());

	    if (tok != null) {	// tok can be null, see validateInput javadoc
		if (tok.startsWith("$(")) { // NOI18N
		    // Treat all chars up to matching ')' as one token
		    int plevel = 1;
		    int pos = 2;
		    while (plevel > 0) {
			for (int i = pos; i < tok.length(); i++) {
			    if (tok.charAt(i) == '(') {
                                plevel++;
                            }
			    if (tok.charAt(i) == ')') {
                                plevel--;
                            }
			    if (plevel <= 0) {
                                break;
                            }
			}
			if (plevel > 0) {
			    if (st.hasMoreTokens()) {
				pos = tok.length() + 1;
				tok = tok + " " + st.nextToken(); // NOI18N
			    }
			    else {
				break;
			    }
			}
		    }
		}

		LinkedList<ListItem> tmp = expandToken(tok, type);
		tcount++;

		if (tmp.size() == 0) {
		    if (!type.isRE()) {	// add nonexistant file
			aList.add(new ListItem(tok, false));
			neFileAdded = true;
		    }
		} else {
		    ListIterator<ListItem> iter = tmp.listIterator();
		    while (iter.hasNext()) {
			ListItem item = iter.next();
			File file = new File(item.getName());

			if (expandDirs && file.isDirectory()) {
			    LinkedList<ListItem> l = processDirectory(tok, file);
			    if (l != null) {
				aList.addAll(l);
			    }
			    else {
				tcount--; // it was cancelled
			    }
			} else {
			    aList.add(new ListItem(
				    file.getAbsolutePath(), file.exists()));
			}
		    }
		}
	    }
	}

	//
	// The criterial for displaying an alert is an arbitrary decision from
	// WorkShop's ProjectWizard.
	//
	if (checkErrorConditions(tcount, aList, neFileAdded)) {
	    ErrorInfo einfo = getErrorInfo();
	    NotifyDescriptor nd = new NotifyDescriptor(einfo.getMsg(),
			einfo.getTitle(), NotifyDescriptor.DEFAULT_OPTION,
			NotifyDescriptor.WARNING_MESSAGE,
			new Object[] { NotifyDescriptor.OK_OPTION },
			NotifyDescriptor.OK_OPTION);
	    DialogDisplayer.getDefault().notify(nd);
	}

	return aList.toArray(new ListItem[aList.size()]);
    }


    /**
     *  By default its an error if only one token was found and it didn't match any file.
     *
     *  @param tcount	The number of tokens
     *  @param list	The list of token matches
     *  @param nefiles	True if non existant files were specified or matched
     */
    protected boolean checkErrorConditions(int tcount, LinkedList<ListItem> list, boolean nefiles) {
	return tcount == 1 && (list.size() == 0 || nefiles);
    }

    /** Derived classes supply alert title and message */
    protected abstract ErrorInfo getErrorInfo();


    /**
     *  Expand a UnixRE into a linked list of files.
     *
     *  @param token	The regular expression to be expanded
     *  @param type	Tell if the token is a regular expression
     *  @return		A linked list of type ItemList with an entry for each
     *			item to display.
     */
    private LinkedList<ListItem> expandToken(String token, TokType type) {
	LinkedList<ListItem> files = new LinkedList<ListItem>();
	REParser rep = new REParser(token);
	String comp = rep.getFirstComponent();
	String re = rep.getRegularExpression();
	String rem = rep.getRemainder();
    
	if (re == null) {
	    File f = new File(comp);
	    type.setRE(false);
	    if (f.exists()) {
		files.add(new ListItem(f.getAbsolutePath(), f.exists()));
	    }
	} else {
	    type.setRE(true);
	    LinkedList<ListItem> alist = processDir(comp, re);
	    TokType dontcare = new TokType();
	    if (alist != null) {
		if (rem == null) {
		    files.addAll(alist);
		} else {
		    ListIterator<ListItem> iter = alist.listIterator();

		    while (iter.hasNext()) {
			ListItem item = iter.next();
			StringBuilder buf = new StringBuilder(256);

			buf.append(item.getName());
			buf.append(File.separator);
			buf.append(rem);
			files.addAll(expandToken(buf.toString(), dontcare));
		    }
		}
	    }
	}

	return files;
    }


    /**
     *  Given a directory and UnixRE pattern return a LinkedList of all matching
     *  files and directories.
     *
     *  @param dir  The directory to match the UnixRE against
     *  @return	    The LinkedList containing all matches
     */
    private LinkedList<ListItem> processDir(String dir, String pattern) {
	LinkedList<ListItem> aList = new LinkedList<ListItem>();
	File[] files = getFileArray(new File(dir));
	UnixRE re;

	if (files == null) {
	    return null;
	} else {
	    try {
		re = new UnixRE(pattern);
	    } catch (PatternSyntaxException e) {
		if (Boolean.getBoolean("netbeans.debug.exceptions")) {	// NOI18N
		    e.printStackTrace();
		}
		return null;
	    }

	    for (int i = 0; i < files.length; i++) {
		String name = files[i].toString();
		if (re.match(name)) {
		    File file = new File(name);
		    aList.add(new ListItem(file.getAbsolutePath(),
					file.exists()));
		}
	    }
	}

	return aList;
    }



    /**
     *  Break the text field down into 3 fields. The first part is the name of
     *  the longest directory component with no UnixRE chars, the 2nd part is
     *  the UnixRE, and the 3rd part is the remainder. If there are no UnixRE
     *  chars in the whole string then the firstComonent is the full file name.
     *  In all cases, the breaks are at '/' characters.
     */
    private class REParser {

	/** Every directory and file component before the first RE pattern */
	private String firstComponent;

	/** The (optional) regular expression */
	private String regularExpression;

	/** Anything left after the regular expression */
	private String remainder;

	/**
	 *  Parse the given text for quick lookup later.
	 *
	 *  @param text	The input from the JTextField
	 */
	public REParser(String text) {
	    int pos1 = 0, pos2;

	    if (text.charAt(0) != File.separatorChar) {
		text = getMakefileData().getBaseDirectory(MakefileData.EXPAND) +
			'/' + text;
	    }
	    for (int i = 0; i < text.length(); i++) {
		char c = text.charAt(i);
		if (c == '/') {
		    pos1 = i;		// store for later use
		}
		if (c == '*' || c == '?' || c == '[') {
		    // pos points to the last '/'
		    firstComponent = new String(text.substring(0, pos1));

		    // set pos to the '/' delimiting the RE
		    pos2 = text.indexOf('/', i);
		    if (pos2 == -1) {
			regularExpression =
				new String(text.substring(pos1 + 1));
			remainder = null;
		    } else {
			regularExpression =
				new String(text.substring(pos1 + 1, pos2));
			remainder =
				new String(text.substring(pos2 + 1));
		    }
		    return;
		}
	    }
	    
	    // didn't find any regular expression characters
	    firstComponent = new String(text);
	    regularExpression = null;
	    remainder = null;
	}

	/** Getter for firstComponent */
	public String getFirstComponent() {
	    return firstComponent;
	}

	/** Getter for regularExpression */
	public String getRegularExpression() {
	    return regularExpression;
	}

	/** Getter for remainder */
	public String getRemainder() {
	    return remainder;
	}
    }


    /**
     *  This should be File.listFiles() except that method ignores "." and
     *  "..". This method adds those missing directories.
     */
    private File[] getFileArray(File dir) {
	File[] files = dir.listFiles();

	if (files == null) {
	    return null;
	}

	File[] augmented = new File[files.length + 2];
	augmented[0] = new File(".");					// NOI18N
	augmented[1] = new File("..");					// NOI18N
	for (int i = 0; i < files.length; i++) {
	    augmented[i + 2] = files[i];
	}

	return augmented;
    }


    /**
     *  Ask the user if just this directory should be checked or if a recursive
     *  directory traversal should be done. Call a method to add the files based
     *  on what the user specifies.
     *
     *  @param name The name of the directory being added/processed
     *  @param file The File representing the named directory
     *  @return A linked list containing a ListItem for each selected file
     *
     *  Note: If no files are chosen an empty LinkedList is returned.
     */
    private LinkedList<ListItem> processDirectory(String name, File file) {

	// Ask the user what to do
	String msg = MessageFormat.format(
			    getString("MSG_CREATE_SOURCE_DIRECTORY"),	// NOI18N
			    new Object[] { name });
	JButton subdirs = new JButton(getString("BTN_Subdirs"));	// NOI18N
	subdirs.setMnemonic(getString("MNEM_Subdirs").charAt(0));	// NOI18N
	JButton dirs = new JButton(getString("BTN_Dirs"));		// NOI18N
	dirs.setMnemonic(getString("MNEM_Dirs").charAt(0));		// NOI18N
	JButton cancel = new JButton(getString("BTN_Cancel"));		// NOI18N
	String TITLE = getString("LBL_QUESTION_WINDOW");		// NOI18N
	
	NotifyDescriptor nd = new NotifyDescriptor(msg,
		    TITLE, NotifyDescriptor.DEFAULT_OPTION,
		    NotifyDescriptor.QUESTION_MESSAGE,
		    new JButton[] {subdirs, dirs, cancel},
		    subdirs);

	// Post the QuestionWindow
	Object ret = DialogDisplayer.getDefault().notify(nd);

	// Process the user's answer
	if (ret instanceof JButton) {
	    if (ret.equals(subdirs)) {
		return addDirectoryFiles(name, file, true);
	    } else if (ret.equals(dirs)) {
		return addDirectoryFiles(name, file, false);
	    }
	}
	
	return null;		// return null list for cancel
    }


    /**
     *  Add the files in the given directory. Recursively do subdirectories if
     *  the recurse flag is set.
     *
     *  @param parent The relative pathname of the parent directory
     *  @param dir The File representing the parent directory
     *  @param recurse Tells if we should recursively call ourselves for
     *		       subdirectories
     *  @return A linked list containing a ListItem for each file added
     */
    private LinkedList<ListItem> addDirectoryFiles(
			    String parent, File dir, boolean recurse) {
	LinkedList<ListItem> aList = new LinkedList<ListItem>();

	// Add each file in the directory
	File[] files = dir.listFiles(new SrcsFileFilter());
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                aList.add(new ListItem(
                            parent + File.separator + files[i].getName(), true));
            }
        }

	if (recurse) {
	    // Recursively call this method for each subdirectory
	    File[] dirs = dir.listFiles(new DirFilter());
            if (files != null) {
                for (int i = 0; i < dirs.length; i++) {
                    aList.addAll(addDirectoryFiles(parent + File.separator +
                                dirs[i].getName(), dirs[i], true));
                }
            }
	}

	return aList;
    }


    /**
     *  Add all items to the item list which have been specified in objects.
     */
    protected void addMultipleFiles(Object[] objects) {

	if (objects != null) {		    // no files in a directory
	    DefaultListModel model = (DefaultListModel) list.getModel();
	    String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);

	    for (int i = 0; i < objects.length; i++) {
		String path = CndPathUtilities.getRelativePath(
			cwd, ((ListItem) objects[i]).getName());
		if (!model.contains(path)) {	// expensive! but necessary
		    if (addBeginning) {
                        model.add(0, path);
                    } else {
                        model.addElement(path);
                    }
		}
	    }
	}
    }


    /**
     *  Add all items to the item list which have been specified in files.
     */
    protected void addMultipleFiles(File[] files) {

	if (files != null) {
	    DefaultListModel model = (DefaultListModel) list.getModel();
	    String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);

	    for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    LinkedList<ListItem> aList = processDirectory(files[i].getPath(), files[i]);
                    if (aList != null) {
                        for(ListItem item : aList){
                            String path = CndPathUtilities.getRelativePath(cwd, item.getName());
                            if (!model.contains(path)) {	    // expensive! but necessary
                                if (addBeginning) {
                                    model.add(0, path);
                                } else {
                                    model.addElement(path);
                                }
                            }
                        }
                    }
                } else {
                    String path = CndPathUtilities.getRelativePath(cwd, files[i].getPath());
                    if (!model.contains(path)) {	    // expensive! but necessary
                        if (addBeginning)
                            model.add(0, path);
                        else
                            model.addElement(path);
                    }
                }
	    }
	}
    }


    /**
     *  The default implementation returns the input unchanged. Classes derived
     *  from EnterItemsPanel may override the default and add/remove text or
     *  even return a null to indicate the whole token was unwanted.
     */
    protected String validateInput(String token) {
	return token;
    }


    /**
     *  Create a FileChoose for the text field.
     */
    protected void createChooser(JButton chooser) {

	chooser.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
	    
		if (fc == null) {
		    fc = new JFileChooser();
		    fc.setApproveButtonText(getString("BTN_Approve"));	// NOI18N
		    fc.setMultiSelectionEnabled(true);
		    fc.setFileSystemView(new IpeFileSystemView(fc.getFileSystemView()));
		    if (dirChooser) {
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		        fc.setDialogTitle(getString("TITLE_DirChooser"));	// NOI18N
                    } else if (dirAndFileChooser) {
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		        fc.setDialogTitle(getString("DLG_FILE_CHOOSER_TITLE"));	// NOI18N
		    }  else {
		        fc.setDialogTitle(getString("DLG_FILE_CHOOSER_TITLE"));	// NOI18N
		    }
		    if (mspFilter) {
			fc.setFileFilter(new MspFileFilter());
		    }
		}

		// See if the user has already typed a directory. If so use it.
		File f = null;
		String cur = CndPathUtilities.expandPath(getText());
		if (cur.length() > 0) {
		    f = new File(cur);
		}

		if (f != null && f.isDirectory()) {
		    fc.setCurrentDirectory(f);
		} else if (lastChooserDir != null) {
		    fc.setCurrentDirectory(lastChooserDir);
		} else {
		    fc.setCurrentDirectory(new
			    File(getMakefileData().getBaseDirectory(MakefileData.EXPAND)));
		}
		int returnVal = fc.showDialog(EnterItemsPanel.this, null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    addMultipleFiles(fc.getSelectedFiles());
		    lastChooserDir = fc.getCurrentDirectory();
		    entryText.setText(null);
		    addBtn.setEnabled(false);
		}
	    }
	});
    }

    public JTextField getEntryText() {
	return entryText;
    }

    private String getText() {
	return CndPathUtilities.expandPath(entryText.getText());
    }


    /**
     *  The validation method. This panel is valid iff a writable directory is
     *  specified.
     */
    @Override
    public boolean isPanelValid() { 

	if (itemsRequired && list.getModel().getSize() == 0) {
	    return false;
	} else {
	    return true; 
	}
    }


    @Override
    public void addNotify() {

	super.addNotify();

	// disable up/down/remove buttons (4880337)
	removeBtn.setEnabled(false);
	upArrow.setEnabled(false);
	downArrow.setEnabled(false);
    changeBtn.setEnabled(list != null && list.getSelectedIndex() >= 0);

	if (itemsRequired && list.getModel().getSize() == 0) {
	    nextButton.setEnabled(false);
	}

	if (dynamicNext && entryText.getDocument().getLength() > 0) {
	    addBtn.setEnabled(true);
	    CndUIUtilities.setDefaultButton(getRootPane(), addBtn);
	}
	entryText.selectAll();
	CndUIUtilities.requestFocus(entryText);
    }

    @Override
    public void removeNotify() {
	super.removeNotify();

	if (dynamicNext || itemsRequired) {
	    nextButton.setEnabled(true);
	    getRootPane().setDefaultButton(nextButton);
	}

	if (fc != null && fc.isShowing()) {
	    Object o = fc.getTopLevelAncestor();
	    if (o != null && o instanceof JDialog) {
		((JDialog) o).dispose();
	    }
	}
    }


    /**
     *  Setup listeners for entryText and list. This needs to be done after all
     *  components are created so its not done in the create* methods.
     */
    private void setupListeners() {
	final DefaultListModel model = (DefaultListModel) list.getModel();

	entryText.getDocument().addDocumentListener(new DocumentListener() {
	    public void changedUpdate(DocumentEvent e) {
		if (e.getDocument().getLength() >= 1) {
		    addBtn.setEnabled(true);
		    if (dynamicNext) {
			getRootPane().setDefaultButton(addBtn);
		    }
		}
	    }

	    public void insertUpdate(DocumentEvent e) {
		if (e.getDocument().getLength() >= 1) {
		    addBtn.setEnabled(true);
		    if (dynamicNext && list.getModel().getSize() == 0) {
			getRootPane().setDefaultButton(addBtn);
		    }

		    int min = list.getMinSelectionIndex();
		    int max = list.getMaxSelectionIndex();
		    if (min >= 0 && max >= 0 && min == max) {
			// Single line selections:
			changeBtn.setEnabled(true);
		    }
		}
	    }

	    public void removeUpdate(DocumentEvent e) {
		if (e.getDocument().getLength() == 0) {
		    addBtn.setEnabled(false);
		    changeBtn.setEnabled(false);
		    if (dynamicNext) {
			getRootPane().setDefaultButton(nextButton);
		    }
		}
	    }
	});

	addBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		addMultipleFiles(expandFileList(getText()));
		list.clearSelection();
		entryText.setText(null);
		addBtn.setEnabled(false);
		changeBtn.setEnabled(false);
		if ((dynamicNext && !itemsRequired) ||
			    (itemsRequired && model.getSize() > 0)) {
		    nextButton.setEnabled(true);
		    getRootPane().setDefaultButton(nextButton);
		}
	    }
	});

	changeBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
                String path = getText();
                if (!model.contains(path)) {	    // expensive! but necessary
                    model.set(list.getMinSelectionIndex(), path);
                }
	    }
	});

	removeBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		//int min = list.getMinSelectionIndex();
		//int max = list.getMaxSelectionIndex();
		//model.removeRange(min, max);

		int min = list.getMinSelectionIndex();
		int[] removeList = list.getSelectedIndices();
		for (int i = removeList.length; i > 0; i--) {
		    model.remove(removeList[i-1]);
		}

		int selectedIndex = (min >= list.getModel().getSize()) ? min-1 : min;
		if (selectedIndex >= 0) {
		    list.ensureIndexIsVisible(selectedIndex);
		    list.setSelectedIndex(selectedIndex);
		    removeBtn.setEnabled(true);
		    changeBtn.setEnabled(true);
		    upArrow.setEnabled(selectedIndex > 0);
		    downArrow.setEnabled(selectedIndex < (list.getModel().getSize()-1));
		}
		else {
		    removeBtn.setEnabled(false);
		    changeBtn.setEnabled(false);
		    upArrow.setEnabled(false);
		    downArrow.setEnabled(false);
		}
	    }
	});

	list.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		// Ignore any calls while value is being adjusted (ie, dragged)
		if (!e.getValueIsAdjusting()) {
		    int min = list.getMinSelectionIndex();
		    int max = list.getMaxSelectionIndex();

		    if (min >= 0 && max >= 0) {
			if (min == max) {
			    // Single line selections:

			    // Set selected value in the text field and enable
			    // the change button.
			    entryText.setText(model.get(min).toString());
			    addBtn.setEnabled(true);
			    changeBtn.setEnabled(true);

			    // Enable the up and down arrows unless selection
			    // is 0th or last entry.
			    upArrow.setEnabled(min != 0);
			    downArrow.setEnabled(min != (model.getSize() - 1));
			} else {
			    entryText.setText(null);
			    changeBtn.setEnabled(false);
			    upArrow.setEnabled(false);
			    downArrow.setEnabled(false);
			}
		    }

		    // We can delete any selection
		    removeBtn.setEnabled(true);
		}
	    }
	});

	model.addListDataListener(new ListDataListener() {
	    public void contentsChanged(ListDataEvent e) {
		if (model.getSize() > 0) {
		    if (dynamicLast && !lastEnabled &&
					getMakefileData().isComplete(true)) {
			lastEnabled = true;
			//MakefileWizard.getMakefileWizard().setFinishEnabled(true);
		    }
		} else {
		    if (dynamicLast && lastEnabled) {
			lastEnabled = false;
			//MakefileWizard.getMakefileWizard().setFinishEnabled(false);
		    }
		}
	    }

	    public void intervalRemoved(ListDataEvent e) {
		if (model.getSize() == 0) {
		    if (dynamicNext && itemsRequired && list.getModel().getSize() == 0) {
			getRootPane().setDefaultButton(addBtn);
			nextButton.setEnabled(false);
		    }
		    if (dynamicLast && lastEnabled) {
			lastEnabled = false;
			//MakefileWizard.getMakefileWizard().setFinishEnabled(false);
		    }
		}
	    }

	    public void intervalAdded(ListDataEvent e) {
		if (dynamicNext && itemsRequired) {
		    nextButton.setEnabled(true);
		    getRootPane().setDefaultButton(nextButton);
		}
		if (dynamicLast && !lastEnabled &&
				    getMakefileData().isComplete(true)) {
		    lastEnabled = true;
		    //MakefileWizard.getMakefileWizard().setFinishEnabled(true);
		}
	    }
	});

	// There is an assumption that the button is disabled if it cannot be
	// decreased.
	upArrow.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int idx = list.getSelectedIndex();

		Object o = model.remove(idx);
		model.add(idx - 1, o);
		list.setSelectedIndex(idx - 1);
		if (idx == 1) {
		    upArrow.setEnabled(false);
		}
	    }
	});

	// There is an assumption that the button is disabled if it cannot be
	// increased.
	downArrow.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int idx = list.getSelectedIndex();

		Object o = model.remove(idx);
		model.add(idx + 1, o);
		list.setSelectedIndex(idx + 1);
		if (idx == (model.getSize() - 1)) {
		    downArrow.setEnabled(false);
		}
	    }
	});
    }


    /**
     *  Filter to display directories and source files in the FileChooser.
     */
    private class MspFileFilter extends javax.swing.filechooser.FileFilter {

	/** Store the filter string since its used in several places */
	private String filterString;

	/** Constructor for MspFileFilter */
	public MspFileFilter() {
	    filterString = getString("DFLT_SourceFilter");		// NOI18N
	}

	/**
	 *  Match the given file against the default source file regular
	 *  expression.
	 *
	 *  @param file The source file to check
	 *  @return true if its a source file or directory
	 */
	public boolean accept(File file) {
	
	    if (file.isDirectory()) {
		return true;
	    }

	    if (filters == null) {
		StringTokenizer st = new StringTokenizer(filterString);
		filters = new HashSet<UnixRE>(15);

		synchronized (filters) {
		    try {
			while (st.hasMoreTokens()) {
			    filters.add(new UnixRE(st.nextToken()));
			}
		    } catch (PatternSyntaxException ex) {
			ex.printStackTrace();
			return false;
		    }
		}
	    }

	    synchronized (filters) {
		Iterator iter = filters.iterator();
		while (iter.hasNext()) {
		    UnixRE re = (UnixRE) iter.next();

		    if (re.match(file.getName())) {
			return true;
		    }
		}
	    }

	    return false;
	}

	public String getDescription() {
	    return filterString;
	}
    }


    /**
     *  Filter to eliminate directories and non-source files. Uses the default
     *  sources filter to match only source files.
     */
    private class SrcsFileFilter implements java.io.FileFilter {

	/** Store the filter string since its used in several places */
	private String filterString;

	/** Constructor for SrcsFileFilter */
	public SrcsFileFilter() {
	    filterString = getString("DFLT_SourceFilter");		// NOI18N
	}

	/**
	 *  Match the given file against the default source file regular
	 *  expression. Verify its not a directory first.
	 *
	 *  @param file The source file to check
	 *  @return true if its a source file otherwise false
	 */
	public boolean accept(File file) {
	
	    if (file.isDirectory()) {
		return false;
	    }

	    if (filters == null) {
		StringTokenizer st = new StringTokenizer(filterString);
		filters = new HashSet<UnixRE>(15);

		synchronized (filters) {
		    try {
			while (st.hasMoreTokens()) {
			    filters.add(new UnixRE(st.nextToken()));
			}
		    } catch (PatternSyntaxException ex) {
			ex.printStackTrace();
			return false;
		    }
		}
	    }

	    synchronized (filters) {
		Iterator iter = filters.iterator();
		while (iter.hasNext()) {
		    UnixRE re = (UnixRE) iter.next();

		    if (re.match(file.getName())) {
			return true;
		    }
		}
	    }

	    return false;
	}
    }


    /**
     *  Filter to eliminate all files except directories.
     */
    private static class DirFilter implements java.io.FileFilter {
	public boolean accept(File file) {
	    return file.isDirectory();
	}
    }


    /**
     *  Store an item for each token in the original text and each file or
     *  directory found during the search.
     */
    protected static class ListItem {

	/** a name from the text field */
	private String name;

	/** does the file already exist? */
	private boolean exists;


	/** Create item for the list */
	public ListItem(String name, boolean exists) {

	    this.name = name;
	    this.exists = exists;
	}

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public boolean isExists() {
	    return exists;
	}
    }


    /** Is the token a regular expression token? */
    private static final class TokType {

	private boolean re;

	void setRE(boolean re) {
	    this.re = re;
	}

	boolean isRE() {
	    return re;
	}
    }

    protected static final class ErrorInfo {

	private String title;
	private String msg;

	protected ErrorInfo() {
	    this(null, null);
	}

	protected ErrorInfo(String title, String msg) {
	    this.title = title;
	    this.msg = msg;
	}

	String getTitle() {
	    return title;
	}

	void setTitle(String title) {
	    this.title = title;
	}

	String getMsg() {
	    return msg;
	}

	void setMsg(String msg) {
	    this.msg = msg;
	}
    }
}
