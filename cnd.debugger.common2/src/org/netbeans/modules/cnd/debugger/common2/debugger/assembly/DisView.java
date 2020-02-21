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

package org.netbeans.modules.cnd.debugger.common2.debugger.assembly;

import java.util.HashMap;
import java.util.regex.*;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Insets;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.Caret;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.openide.util.actions.SystemAction;
import org.openide.util.ImageUtilities;
import org.openide.ErrorManager;

import org.openide.text.CloneableEditorSupport;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.JEditorPane;

import org.netbeans.modules.cnd.debugger.common2.utils.StopWatch;

import org.netbeans.modules.cnd.debugger.common2.debugger.actions.StepInstAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.StepOutInstAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.StepOverInstAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.RunToCursorInstAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.RegistersWindowAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.MemoryWindowAction;

import org.netbeans.modules.cnd.debugger.common2.debugger.Address;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

/**
 * A Disassembler View.
 * 
 * This is a compound View fed by a compound Model and feeding a Controller.
 *
 * The main sub-view is composed of an editorPane which renders the text
 * of the disasssembly. The data for this is pulled from DisFragModel model,
 * parsed and fed into the editorPane's StyledDocument.
 * 	At the moment we do our own parsing although there's experimental code 
 *	to have the parsing be done by the text/x-asm EditorKit provided by NB.
 *
 * The next sub-view is the background painting of the same editorPane
 * which is done by us and pulls data from StateModel and BreakpointModel to
 * render bpt and current-pc stripes.
 *
 * Then there is Gutter sub-view which also pulls data from StateModel and
 * BreakpointModel and renders bpt and current-pc annotations.
 *
 * The Controller can request a refresh of the current location, for example
 * when the view becomes visible, or of a specific location based on the
 * explicit addresses entered in the control panel at the bottom of the view.
 * The Controller is also fed by bpt-toggling clicks in the gutter.
 */

public class DisView {

    private String view_name =
	Catalog.get("TITLE_DisassemblerView"); //NOI18N

    private DisFragModel model;
    private StateModel stateModel;
    private BreakpointModel breakpointModel;
    private Controller controller;

    private boolean from_address = false;

    private CachedFontMetrics metrics = new CachedFontMetrics();

    private transient JComponent tree = null;
    private StyledDocument styledDoc;
    private Font default_font;
    private JEditorPane editorPane;
    private Gutter gutter;
    private JScrollPane textScrollPane;
    private JPanel columnHeader;	// For Toolbar to put buttons.
    private JPanel statusPanel;

    private JTextField patternText;
    private String debugPattern;

    private JLabel addressLabel;
    private JComboBox addressText;

    private JLabel fileLabel;
    private JTextField fileText;

    private JLabel functionLabel;
    private JTextField functionText;

    // line pattern and group #'s
    //
    // general form of input:
    //   0x00010d60:  func       :	st       %g1, [%l0 + 8]	! comment
    //   0x00010d60:  func+0x0020:	st       %g1, [%l0 + 8]	! comment
    // 112222222222233444444444444555555666666666666666666666666777777777

    private final String linePatternString =
	"(\\s*)(0x\\p{XDigit}+:)(\\s*)(.*:)(\\s)(.*?)(!.*)*\n"; // NOI18N
    private Pattern linePattern;

    // instruction pattern and group #'s
    //
    // general form of input:
    // st         %g1, [%l0 + 8]
    // 112222222223333333333333333

    private String insPatternString = "(.*?)(\\s+)(.*)"; // NOI18N
    private Pattern insPattern;

    private final Pattern commasPattern = Pattern.compile(","); // NOI18N


    private PopupListener popupListener;
    private GutterListener gutterListener; 
    private JPopupMenu popup;
    private JMenuItem menuItemGoToSource;
    private JMenuItem menuItemRegWindow;
    private JMenuItem menuItemMemWindow;
    private JMenuItem menuItemShowCurrentStatement;
    private JMenuItem menuItemAddBreakpoint;
    private JMenuItem menuItemDeleteBreakpoint;

    static enum LineStyle {
        Source_line,
        Code_line,
	address,
	label,
	ins,
	op,
	args,
	comment
    };

    /*
     * Perform action to get disassembler code from engine when user
     * do the request from Address textfield
     *
     */
    class AddressTextAction extends AbstractAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
	String ac = actionEvent.getActionCommand();
	    // 6573554
	    if (ac.equals("comboBoxChanged") ||		// NOI18N
		ac.equals("comboBoxEdited")) {		// NOI18N
		boolean found = false;
		JComboBox cb = (JComboBox)actionEvent.getSource();
		String start=(String)cb.getSelectedItem();

		for (int i = 0; i < addressText.getItemCount(); i++ ) {
		    if (start.compareTo((String)addressText.getItemAt(i)) == 0) {
			found = true;
			break;
		    }
		}
		if (!found) {
		    addressText.addItem(start);
		}
		
		// OLD start = start;
		from_address = true;
		controller.requestDis(start, 100, true);	// NOI18N
	    }
	}
    }

    /**
     * Show src code corresponding to this assembly code.
     * This is the reverse of DisassemblerWindowAction.
     */
    static class GoToSourceAction extends AbstractAction {
	public GoToSourceAction() {
	    super(Catalog.get("Dis_ACT_Go_To_Source"), 		// NOI18N
		new ImageIcon("open.gif"));			// NOI18N
	}
        @Override
	public void actionPerformed(ActionEvent ev) {
	    //controller.goToSource();
	}
    }

    class ShowCurrentStatementAction extends AbstractAction {
	public ShowCurrentStatementAction() {
	    super(Catalog.get("Dis_ACT_Show_Current_Statement"),// NOI18N
		new ImageIcon("open.gif"));			// NOI18N
	}
        @Override
	public void actionPerformed(ActionEvent ev) {
	    showCurrentStatement();
	}
    }

    private class BreakpointAction extends AbstractAction {
	private boolean add; // add/remove bpt 

	public BreakpointAction(String bptString, boolean add) {
	    super(bptString, new ImageIcon("cut.gif"));		// NOI18N
	    this.add = add;
	}
        @Override
	public void actionPerformed(ActionEvent ev) {
	    if (controller != null) {
		String address = addrFromLine(getCurrentLine());
		if (address != null) {
		    if (add)
			controller.setBreakpoint(address, true);
		    else
			controller.setBreakpoint(address, false);
	        }
	    }
	}
    }

    private class GutterListener extends MouseAdapter {

	@Override
	public void mouseClicked(MouseEvent e) {
	    toggleBreakpoint(e.getY());
	}

	public void toggleBreakpoint(int y) {
	    if (controller != null) {

		int pos = gutter.getPosition(0, y);
		String address = getLineAt(pos);
		address = addrFromLine(address);

		if (address != null)
		    controller.toggleBreakpoint(address);
	    }
	}
    }

    private class PopupListener
	extends MouseAdapter
	implements ActionListener, PopupMenuListener {

	JPopupMenu popup;

	PopupListener(JPopupMenu popupMenu) {
	    popup = popupMenu;
	}

	@Override
	public void mousePressed(MouseEvent e) {
	    maybeShowPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
	    if (e.isPopupTrigger()) {
		String selected_text = getCurrentLine();
		menuItemGoToSource.setEnabled(selected_text != null);

		String address = addrFromLine(selected_text);
		menuItemAddBreakpoint.setEnabled(address != null);
		menuItemDeleteBreakpoint.setEnabled(address != null);
		popup.show(e.getComponent(),
			   e.getX(), e.getY());
	    }
	}
        @Override
	public void actionPerformed(ActionEvent ev) {
	    JMenuItem source = (JMenuItem)(ev.getSource());
	}
        @Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}
        @Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	}
        @Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}
    }

    private static class CachedFontMetrics {
	private int ascent;
	private int descent;
	private int height;

	private void update(JComponent component) {
	    Font font = component.getFont();
	    FontMetrics fm = component.getFontMetrics(font);
	    ascent = fm.getAscent();
	    descent = fm.getDescent();
	    height = fm.getHeight();
	}

	public int ascent() {
	    return ascent;
	}

	public int descent() {
	    return descent;
	}

	public int height() {
	    return height;
	}
    }


    /**
     * A utility class for scanning lines in a paint function.
     */

    abstract class ViewScanner {

	/**
	 * Called for every "line" which is visible within the clip
	 * region 'g'.
	 * @param y the y of the bottom of the line in view coordinates.
	 * @param ycenter the y of the center of the line in view
	 * coordinates.
	 */
	protected abstract void scanLine(Graphics g, int y, int ycenter);


	/**
	 * Call 'scanLine()' for every line withing the clip region 'g'.
	 */

	public void scan(JEditorPane editorPane, Graphics g, CachedFontMetrics metrics) {
	    Rectangle clip = g.getClipBounds();

	    Insets margin = editorPane.getMargin();
	    int modulus = clip.y % metrics.height;
	    int offset = margin.top +
			 (modulus == 0? 0: metrics.height - modulus) +
			 0;

	    if (Log.Dis.gutter) {
		System.out.printf("margin.top %d  height %d  clip.y %d  %% %d  offset %d  firsty %d\n", // NOI18N
		    margin.top, metrics.height, clip.y, (clip.y % metrics.height), offset, clip.y + offset);
	    }


	    // yy is in clip coordinates

	    for (int yy = offset; yy < clip.height + metrics.height; yy += metrics.height) {
		// y is in view coordinates
		final int y = clip.y + yy;

		// figure y for center of row/line
		final int ycenter = y - metrics.height/2;

		if (Log.Dis.gutter) {
		    int x = clip.x;

		    g.setColor(editorPane.getForeground());
		    g.drawLine(x, y, x + clip.width, y);

		    g.setColor(Color.RED);
		    g.drawLine(x, ycenter, x + clip.width, ycenter);
		}

		scanLine(g, y, ycenter);
	    }
	}
    }

// Various examples:
// http://forum.java.sun.com/thread.jspa?threadID=585673&messageID=3020730
// nb-cvs-release551/editor/libsrc/org/netbeans/editor/GlyphGutter.java
// http://www.esus.com/docs/GetQuestionPage.jsp?uid=1326

    private class Gutter extends JComponent {
	private static final int WIDTH = 20;
	private JEditorPane editorPane;

	private final String bptResource =
	    Catalog.get("ICON_BPT_DisassemblerView"); // NOI18N
	private final Image bptImage = ImageUtilities.loadImage(bptResource);

	private final String disBptResource =
	    Catalog.get("ICON_Dis_BPT_DisassemblerView"); // NOI18N
	private final Image disBptImage = ImageUtilities.loadImage(disBptResource);

	private final String pcResource =
	    Catalog.get("ICON_CL_DisassemblerView"); // NOI18N
	private final Image pcImage = ImageUtilities.loadImage(pcResource);

	private final String visitResource =
	    Catalog.get("ICON_V_DisassemblerView"); // NOI18N
	private final Image visitImage = ImageUtilities.loadImage(visitResource);

	private final String bptHitResource =
	    Catalog.get("ICON_CLWBPT_DisassemblerView"); // NOI18N
	private final Image bptHitImage = ImageUtilities.loadImage(bptHitResource);

	public Gutter(JEditorPane editorPane) {
	    this.editorPane = editorPane;
	}

	public int getPosition(int x, int y) {
	    Point p = new Point(x, y);
	    return editorPane.viewToModel(p);
	}

	@Override
	public Dimension getPreferredSize() {
	    return new Dimension(WIDTH, editorPane.getHeight());
	}

	private final ViewScanner viewScanner = new ViewScanner() {
            @Override
	    protected void scanLine(Graphics g, int y, int ycenter) {
		Rectangle clip = g.getClipBounds();

		Point p = new Point(0, ycenter);
		int pos = editorPane.viewToModel(p);

		String line = getLineAt(pos);
		// line = line.trim();

		if (line.length() == 0)
		    return;

		final int bptCount = checkForBreakpoint(line);
		final int disBptCount = checkForDisabledBpt(line);
		final boolean pc = checkForCurrent(line);
		Image image = null;

		if (bptCount > 0 && pc) {
		    image = bptHitImage;
		} else if (bptCount > 0) {
		    image = bptImage;
		    if (disBptCount > 0)
			image = disBptImage;
		} else if (pc && stateModel.isVisited()) {
		    image = visitImage;
		} else if (pc) {
		    image = pcImage;
		}

		if (image != null) {
		    int imageHeight = image.getHeight(null);
		    int x = 0;
		    g.drawImage(image,
				x, y - metrics.height + (metrics.height-imageHeight)/2 + 1,
				getBackground(), null);
		}
	    }
	};

	@Override
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);

	    java.awt.Rectangle clip = g.getClipBounds();

	    g.setColor(getBackground());
	    g.fillRect(clip.x,
		       clip.y,
		       clip.width,
		       clip.height);

	    viewScanner.scan(editorPane, g, metrics);
	}
    }

    public DisView() {

	tree = new JPanel(new BorderLayout());
	tree.setName (view_name);

	editorPane = new JEditorPane() {

	    // workaround for turning off the default line-wrapping
	    // and enabling horizontal scrolling.

	    @Override
	    public void setSize(Dimension d){
		if (d.width < getParent().getSize().width)
		    d.width = getParent().getSize().width;
		super.setSize(d);
	    }

	    @Override
	    public boolean getScrollableTracksViewportWidth() {
		return false;
	    }


	    private final ViewScanner viewScanner = new ViewScanner() {

                @Override
		protected void scanLine(Graphics g, int y, int ycenter) {
		    Point p = new Point(0, ycenter);
		    int pos = editorPane.viewToModel(p);

		    String line = getLineAt(pos);
		    // line = line.trim();
		    if (line.length() == 0)
			return;

		    final int[] regIndex = checkForRegs(line);
		    final int bptCount = checkForBreakpoint(line);
		    final int disBptCount = checkForDisabledBpt(line);
		    final boolean pc = checkForCurrent(line);
		    Color stripeColor = null;

		
		    if (pc && stateModel.isVisited()) {
			// 0xE7E1EF light purple
			stripeColor = new Color(231, 225, 239);
		    } else if (bptCount > 0 && pc) {
			// 0xBDE6AA light green
			stripeColor = new Color(189, 230, 170);
		    } else if (bptCount > 0) {
			// 0xFC9F9D light pink;
			stripeColor = new Color(252, 159, 157);
			if (disBptCount > 0)
			    // 0xDCDCD8 light grey;
			    stripeColor = new Color(220, 220, 216);
		    } else if (pc) {
			// 0xBDE6AA light green
			stripeColor = new Color(189, 230, 170);
		    } 

		    int x = 0;
		    if (stripeColor != null) {
			g.setColor(stripeColor);
			g.fillRect(x, y - metrics.height, getWidth(), metrics.height);
		    } else {
			Graphics regg = g.create();
		    /* LATER
			 Font font = regg.getFont();
			 FontRenderContext fontcontext = ((Graphics2D)regg).getFontRenderContext();
			 RectangularShape rect = (RectangularShape)font.getStringBounds("abcdefgh", fontcontext);
			 double rectminx = rect.getMinX();
			 double rectminy = rect.getMinY();
			 double rectheight = rect.getHeight();
			 double rectwidth = rect.getWidth();

			if (regIndex != null && regIndex[0] > 0) {
			    // color first reg
			    regg.setColor(Color.CYAN);
			    regg.fillRect((x+1+regIndex[0])*7+24, y - metrics.height+2, 27, metrics.height-5);
			    if (regIndex[1] > 0) 
				// color second reg
				regg.fillRect((x+2+regIndex[1])*7+24, y - metrics.height+2, 27, metrics.height-5);
			}
		    */
		    }
		}
	    };

	    @Override
	    protected void paintComponent(Graphics passG) {

		final Graphics g = passG.create();

		final Rectangle clip = g.getClipBounds();

		// clear background
		// We need to do this opurselves because we've set
		// opacity to false
		g.setColor(getBackground());
		g.fillRect(clip.x,
			   clip.y,
			   clip.width,
			   clip.height);

		// paint background
		viewScanner.scan(editorPane, g, metrics);

		super.paintComponent(passG);
	    }
	};

	if (Log.Dis.cndAsm) {
	    EditorKit kit = CloneableEditorSupport.getEditorKit("text/x-asm"); // NOI18N
	    editorPane.setEditorKit(kit);
	} else {
	    EditorKit kit = new StyledEditorKit();
	    editorPane.setEditorKit(kit);
	}

	editorPane.getCaret().setVisible(true);
	/* DEBUG
	editorPane.getCaret().addChangeListener(new javax.swing.event.ChangeListener() {
	    public void stateChanged(javax.swing.event.ChangeEvent e) {
		System.out.printf("disasm caret: %s\n", e);
		editorPane.getCaret().setVisible(true);
	    }
	});
	*/
	editorPane.setCaretPosition(0);
	editorPane.setOpaque(false);

	// To DEBUG and fix 6573346
	// editorPane will only enable caret upon focus gain if 
	// it's enabled and editable, and we're not editable.

	editorPane.addFocusListener(new java.awt.event.FocusListener() {
            @Override
	    public void focusGained(java.awt.event.FocusEvent e) {
		// DEBUG System.out.printf("disasm: focusGained()\n");
		editorPane.getCaret().setVisible(true);
	    }
            @Override
	    public void focusLost(java.awt.event.FocusEvent e) {
		// DEBUG System.out.printf("disasm: focusLost()\n");
	    }
	});

	//ta.setWrapStyleWord(false);
	//editorPane.setMargin(new Insets(5,5,5,5));
	Font f = editorPane.getFont();
	default_font = new Font("Monospaced", f.getStyle(), f.getSize()+1); //NOI18N
	editorPane.setFont(default_font);
	editorPane.setEditable(false);

	if (!Log.Dis.cndAsm) {
	    styledDoc = new DefaultStyledDocument();
	    editorPane.setDocument(styledDoc);
	    addStylesToDocument(styledDoc);

	} else {
	    // We should be getting a StyledDocument because of the right 
	    // editor kit
	    Document doc = editorPane.getDocument();
	    if (doc instanceof StyledDocument)
		styledDoc = (StyledDocument) doc;
	}

	textScrollPane = new JScrollPane(editorPane);
	metrics.update(editorPane);
	gutter = new Gutter(editorPane);
	textScrollPane.setRowHeaderView(gutter);
	final int policy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
	// TMP final int policy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
	textScrollPane.setHorizontalScrollBarPolicy(policy);

	columnHeader = new JPanel(new BorderLayout());

	// create toolbar for stepi actions
	JToolBar toolbar = new JToolBar();
	toolbar.setFloatable(false);
	toolbar.setRollover(true);
	toolbar.setBorderPainted(true);

	if (Log.Dis.match) {
	    // text field to set value of 'debugPattern'
	    patternText = new JTextField();
	    patternText.setColumns(70);
	    patternText.setHorizontalAlignment(JTextField.LEFT);
	    patternText.setEditable(true);

	    patternText.addActionListener(new ActionListener() {
                @Override
		public void actionPerformed(ActionEvent evt) {
		    debugPattern = patternText.getText();
		    System.out.printf("Pattern: '%s'\n", debugPattern); // NOI18N
		    modelChanged();
		}
	    });
	    columnHeader.add(patternText, BorderLayout.CENTER);
	}
	columnHeader.add(toolbar, BorderLayout.EAST);

	JButton b;

	    b = new JButton(SystemAction.get(StepOverInstAction.class));
	    b.setText(null);
	    toolbar.add(b);

	    b = new JButton(SystemAction.get(StepInstAction.class));
	    b.setText(null);
	    toolbar.add(b);

	    b = new JButton(SystemAction.get(StepOutInstAction.class));
	    b.setText(null);
	    toolbar.add(b);

	    b = new JButton(SystemAction.get(RunToCursorInstAction.class));
	    b.setText(null);
	    toolbar.add(b);



	statusPanel = new JPanel(new java.awt.GridBagLayout());

	statusPanel.setToolTipText(Catalog.get("TIP_DisStatus")); // NOI18N
	    JPanel addressPanel = new JPanel(new java.awt.GridBagLayout());

		addressLabel = new JLabel(Catalog.get("LBL_Address")); // NOI18N
		addressLabel.setToolTipText(Catalog.get("TIP_DisAddress")); // NOI18N
		addressText = new JComboBox();
		addressText.setEditable(true);
		addressText.addActionListener(new AddressTextAction());
		addressLabel.setLabelFor(addressText);

		// 6754292
		java.awt.GridBagConstraints gridBagConstraints ;
		int gridx = 0;

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = gridx++;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
		addressPanel.add(addressLabel, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = gridx++;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
		gridBagConstraints.weightx = 1.0;
		addressPanel.add(addressText, gridBagConstraints);

	    JPanel filePanel = new JPanel(new java.awt.GridBagLayout());

		fileLabel = new JLabel();
		fileLabel.setText(Catalog.get("LBL_File")); // NOI18N
		fileLabel.setToolTipText(Catalog.get("TIP_DisFile")); // NOI18N

		fileText = new JTextField();
		fileText.setColumns(15);
		fileText.setHorizontalAlignment(JTextField.LEFT);
		fileText.setEditable(false);
		fileLabel.setLabelFor(fileText);

		gridx = 0;

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = gridx++;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
		filePanel.add(fileLabel);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = gridx++;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 2.0;
		gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
		filePanel.add(fileText);

	    JPanel functionPanel = new JPanel(new java.awt.GridBagLayout());

		functionLabel = new JLabel();
		functionLabel.setText(Catalog.get("LBL_Function")); // NOI18N
		functionLabel.setToolTipText(Catalog.get("TIP_DisFunction"));//NOI18N

		functionText = new JTextField();
		functionText.setColumns(15);
		functionText.setHorizontalAlignment(JTextField.LEFT);
		functionText.setEditable(false);
		functionLabel.setLabelFor(functionText);

		gridx = 0;

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = gridx++;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
		functionPanel.add(functionLabel);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = gridx++;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 2.0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
		functionPanel.add(functionText);

	    gridx = 0;

	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.gridx = gridx++;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    gridBagConstraints.weightx = 1.0;
	    gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
	    statusPanel.add(addressPanel, gridBagConstraints);

	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.gridx = gridx++;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    gridBagConstraints.weightx = 1.0;
	    gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
	    statusPanel.add(filePanel, gridBagConstraints);

	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.gridx = gridx++;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    gridBagConstraints.weightx = 1.0;
	    gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
	    statusPanel.add(functionPanel, gridBagConstraints);


	tree.add(columnHeader, BorderLayout.NORTH);
	tree.add(textScrollPane, BorderLayout.CENTER);
	tree.add(statusPanel, BorderLayout.SOUTH);

	createPopup();
    }


    private void createPopup() {
	popup = new JPopupMenu();
	popupListener = new PopupListener(popup);
	gutterListener = new GutterListener();

	menuItemGoToSource = new JMenuItem(new GoToSourceAction());
	popup.add(menuItemGoToSource);
	popup.addSeparator();

	// Add a group of output format items
	// popup.addSeparator();
	// ButtonGroup group = new ButtonGroup();
	//for (i=0; i < memory_formats.length; i++) {
	//    rbMenuItem = new JRadioButtonMenuItem(memory_formats[i]);
	//    if (i == memory_format) rbMenuItem.setSelected(true);
	//    //rbMenuItem.setMnemonic(KeyEvent.VK_R);
	//    rbMenuItem.addActionListener(popupListener);
	//    group.add(rbMenuItem);
	//    popup.add(rbMenuItem);
	//}

	menuItemShowCurrentStatement = new JMenuItem(new ShowCurrentStatementAction());
	popup.add(menuItemShowCurrentStatement);

	popup.addSeparator();
	menuItemAddBreakpoint = new JMenuItem(new BreakpointAction(Catalog.get("Dis_ACT_Set_Breakpoint"), true));	// NOI18N
	popup.add(menuItemAddBreakpoint);
	menuItemDeleteBreakpoint = new JMenuItem(new BreakpointAction(Catalog.get("Dis_ACT_Delete_Breakpoint"), false));// NOI18N
	popup.add(menuItemDeleteBreakpoint);

/* 6573577
	popup.addSeparator();
	popup.add(new ShowDynamicHelpPageAction());
*/

	menuItemRegWindow = new JMenuItem(SystemAction.get(RegistersWindowAction.class));
	popup.add(menuItemRegWindow);
	menuItemMemWindow = new JMenuItem(SystemAction.get(MemoryWindowAction.class));
	popup.add(menuItemMemWindow);
	popup.addSeparator();

	editorPane.addMouseListener(popupListener);
	gutter.addMouseListener(gutterListener);
    }


    public void setController(Controller controller) {
	this.controller = controller;
    }

    public Controller getController() {
	return controller;
    }

    public DisFragModel getModel() {
	return model;
    }


    private HashMap<Long, Integer> modelToViewMap =
	new HashMap<Long, Integer>();

    /**
     * Convert 'addr' a * text position.
     */

    public int modelToView(long addr) throws BadLocationException {
	Integer pos = modelToViewMap.get(addr);
	if (pos == null)
	    throw new BadLocationException("", 0);
	return pos;
    }


    private static boolean isSpace(char c) {
	return c == ' ' || c == '\t';
    }

    private void insertLineArray(String sline, LineStyle defaultStyle) {
	final char line[] = sline.toCharArray();
	final int length = line.length;

	// general form of input:
	//   ! comment
	//   0x00010d60:  func       :	st       %g1, [%l0 + 8]	! comment
	//   0x00010d60:  func+0x0020:	st       %g1, [%l0 + 8]	! comment

	int lx = 0;

	//
	// address
	//

	int addrx = lx;

	// look for colon
	while (lx < length && line[lx] != ':')
	    lx++;

	// no colon? we're done
	if (lx == length) {
	    insert(new String(line, addrx, length-addrx), defaultStyle);
	    return;
	}

	// lx points to ':'
	String address = new String(line, addrx, lx-addrx+1);
	insert(address, LineStyle.address);
	lx++;		// skip ':'

	//
	// label
	//

	int labelx = lx;

	// look for colon
	while (lx < length && line[lx] != ':')
	    lx++;

	// no colon? we're done
	if (lx == length) {
	    insert(new String(line, labelx, length-labelx), defaultStyle);
	    return;
	}

	// lx points to ':'
	String label = new String(line, labelx, lx-labelx+1);
	insert(label, LineStyle.label);
	lx++;		// skip ':'


	// 
	// op
	//

	int opx = lx;		// index of op including space before it

	// skip space before op
	while (lx < length && isSpace(line[lx]))
	    lx++;

	// skip op
	while (lx < length && ! isSpace(line[lx]))
	    lx++;

	// lx points to after 'op'
	String op = new String(line, opx, lx-opx);
	insert(op, LineStyle.op);

	while (true) {
	    int argx = lx;	// index of arg including stuff before it

	    // skip space until first argument
	    while (lx < length && isSpace(line[lx]))
		lx++;

	    // EOL? we're done
	    if (lx == length)
		break;

	    // comment? we're done
	    if (line[lx] == '!') {
		insert(new String(line, argx, length-argx), defaultStyle);
		break;
	    }

	    // collect argument
	    while (lx < length &&
		   !isSpace(line[lx]) &&
		   line[lx] != ',' &&
		   line[lx] != '!') {
		lx++;
	    }

	    // comment? we're done
	    if (lx < length && line[lx] == '!') {
		insert(new String(line, argx, length-argx), defaultStyle);
		break;
	    }

	    // print arg
	    String arg = new String(line, argx, lx-argx);
	    insert(arg, LineStyle.args);

	    if (lx < length && line[lx] == ',') {
		insert(",", defaultStyle); // NOI18N
		lx++;
	    }
	}
    }

    private void insertLineString(String line, LineStyle defaultStyle) {
	// general form of input:
	//   ! comment
	//   0x00010d60:  func       :	st       %g1, [%l0 + 8]	! comment
	//   0x00010d60:  func+0x0020:	st       %g1, [%l0 + 8]	! comment

	// address
	int colonx1 = line.indexOf(':');
	if (colonx1 == -1) {
	    insert(line, defaultStyle);
	    return;
	}

	String address = line.substring(0, colonx1+1);
	insert(address, LineStyle.address);

	// label
	int colonx2 = line.indexOf(':', colonx1+1);
	if (colonx2 == -1) {
	    insert(line.substring(colonx1+1), defaultStyle);
	    return;
	}

	String label = line.substring(colonx1+1, colonx2+1);
	insert(label, LineStyle.label);

	int lx = colonx2+1;
	int opx = lx;		// index of op including space before it

	// skip space before op
	while (lx < line.length() && Character.isWhitespace(line.charAt(lx)))
	    lx++;


	// skip op
	while (lx < line.length() && !Character.isWhitespace(line.charAt(lx)))
	    lx++;
	int eopx = lx;

	String op = line.substring(opx, eopx);
	insert(op, LineStyle.op);

	while (true) {
	    int argx = lx;	// index of arg including stuff before it

	    // skip space until first argument
	    while (lx < line.length() && Character.isWhitespace(line.charAt(lx)))
		lx++;

	    // EOL? we're done
	    if (lx == line.length())
		break;

	    // comment? we're done
	    if (line.charAt(lx) == '!') {
		insert(line.substring(lx), defaultStyle);
		break;
	    }

	    // collect argument
	    while (lx < line.length() &&
		   !Character.isWhitespace(line.charAt(lx)) &&
		   line.charAt(lx) != ',' &&
		   line.charAt(lx) != '!') {
		lx++;
	    }

	    // comment? we're done
	    if (lx < line.length() && line.charAt(lx) == '!') {
		insert(line.substring(lx), defaultStyle);
		break;
	    }

	    // print arg
	    int eargx = lx;
	    String arg = line.substring(argx, eargx);
	    insert(arg, LineStyle.args);

	    if (lx < line.length() && line.charAt(lx) == ',') {
		insert(",", defaultStyle); // NOI18N
		lx++;
	    }
	}
	insert("\n", defaultStyle); // NOI18N
    }

    /**
     * Insert styled disassembly instruction
     */
    private void insertIns(String ins, LineStyle defaultStyle) {
	Matcher im = insPattern.matcher(ins);
	if (im.matches()) {
	    insert(im.group(1), LineStyle.op);	// op
	    insert(im.group(2), defaultStyle);	// space

	    // arguments
	    String rargs = im.group(3);
	    String[] args = commasPattern.split(rargs);
	    for (int ax = 0; ax < args.length; ax++) {
		String arg = args[ax].trim();
		insert(arg, LineStyle.args);
		if (ax+1 < args.length)
		    insert(", ", defaultStyle); // NOI18N
	    }
	} else {
	    insert(ins, LineStyle.ins);
	}
    }

    private void insertLineMatcher(String line, LineStyle defaultStyle) {
	Matcher m = linePattern.matcher(line);
	if (m.matches()) {
	    if (Log.Dis.match) {
		for (int gx = 0; gx <= m.groupCount(); gx++) {
		    System.out.printf("\tG[%d] '%s'\n", // NOI18N
				      gx, m.group(gx));
		}
	    }
	    insert(m.group(1), defaultStyle);		// space
	    insert(m.group(2), LineStyle.address);	// address
	    insert(m.group(3), defaultStyle);		// space
	    insert(m.group(4), LineStyle.label);	// label
	    insert(m.group(5), defaultStyle);		// space
	    insertIns(m.group(6), defaultStyle);		// ins
	    if (m.group(7) != null) {
		insert("\t", defaultStyle);			// EOL // NOI18N
		insert(m.group(7), LineStyle.comment);	// possible comment
	    }
	    insert("\n", defaultStyle);			// EOL // NOI18N
	} else {
	    insert(line, defaultStyle);
	    if (Log.Dis.match)
		System.out.printf("\tno match\n"); // NOI18N
	}
    }

    /**
     * Insert a styled disassembly line.
     */
    private void insertLine(String line, LineStyle defaultStyle) {
	if (Log.Dis.match)
	    System.out.printf("line %s", line); // NOI18N

	if (false) {
	    insert(line, defaultStyle);
	} else if (true) {
	    insertLineArray(line, defaultStyle);
	} else if (false) {
	    insertLineString(line, defaultStyle);
	} else {
	    insertLineMatcher(line, defaultStyle);
	}
    }

    /**
     * The contents of the code model changed; refill our Document.
     */

    public void modelChanged() {
	try {
	// reset modelToViewMap
	modelToViewMap = new HashMap<Long, Integer>();

	if (model == null)
	    return;

	StopWatch sw = new StopWatch(String.
	    format("Dis document creation for %d lines",	// NOI18N
		model.size()));
	sw.start();

	int carpos = getCaretPosition();
	clear();
	sw.mark("clear"); // NOI18N

	linePattern = Pattern.compile(linePatternString);

	if (debugPattern != null)
	    insPatternString = debugPattern;
	insPattern = Pattern.compile(insPatternString);
	sw.mark("compattern"); // NOI18N

	if (Log.Dis.match) {
	    System.out.printf("============================================\n"); // NOI18N
	    System.out.printf("pattern '%s'\n", linePatternString); // NOI18N
	    System.out.printf("pattern '%s'\n", insPatternString); // NOI18N
	}

	StringBuilder sb = new StringBuilder();

	for (DisFragModel.Line srcLine : model) {
	    String line = srcLine.toString();

	    // build modelToViewMap as we go along
	    String addr = addrFromLine(line);
	    long address ;
	    if (addr != null) {
		try {
		    address = Address.parseAddr(addr);
		    modelToViewMap.put(address, getCaretPosition());
		} catch (NumberFormatException x) {
		    ErrorManager.getDefault().notify(x);
		}
	    }

	    LineStyle defaultStyle = figureStyle(line);

	    if (checkForCurrent(line) && !stateModel.isVisited()) {
		carpos = getCaretPosition();
	    }
		
	    if (Log.Dis.cndAsm) {
		// asm lexer doesn't handle '<'s well:
		//	   0x00010b58:  main+344:	call  0x20ed0 <~C>

		int badx = line.indexOf('<');
		if (badx != -1)
		    line = line.substring(0, badx-1);

		if (defaultStyle == LineStyle.Source_line) {
		    sb.append("! " + line);		// NOI18N
		} else {
		    sb.append(line);
		}
	    } else {
		if (defaultStyle == LineStyle.Source_line) {
		    insert(line, defaultStyle);
		} else {
		    insertLine(line, defaultStyle);
		}
	    }

	}
	if (Log.Dis.cndAsm) {
	    sw.mark("setText"); // NOI18N
	    editorPane.setText(sb.toString());
	}

	sw.stop();
	if (Log.Dis.time)
	    sw.dump();

	stateModel.setAddrHashMap(modelToViewMap);

	setCaretPosition(carpos);

	// For some reason (document.clear()?) after the above the
	// caret goes invisible, so make it visible again.
	editorPane.getCaret().setVisible(true);

        gutter.repaint();
        editorPane.repaint();
	} catch (Throwable x) {
	    x.printStackTrace();
	}
    }

    public void stateModelChanged() {
        if (stateModel == null) {
            fileText.setText(null);
	    fileText.setToolTipText(null);
            functionText.setText(null);
        } else {
	    if (stateModel.getFile() != null) {
		fileText.setText(CndPathUtilities.getBaseName(stateModel.getFile()));
		fileText.setToolTipText(stateModel.getFile());
	    } else {
		fileText.setText(null);
		fileText.setToolTipText(null);
	    }
	    if (stateModel.getFunction() != null) 
		functionText.setText(stateModel.getFunction());
	    else
		functionText.setText(null);
        }

	try {
	    if (!from_address)
	        setCaretPosition(modelToView(stateModel.getPC()));
	} catch (Exception x) {
	    // fail on mapping source to assembly addr
	    // retrieve dis data from engine again without source

	    if (stateModel.getPC() != 0) {
		String start = Address.toHexString0x(stateModel.getPC(), true);
		controller.requestDis(start, 100, true);
	    }
	}

	from_address = false;

        gutter.repaint();
        editorPane.repaint();
    }

    public void breakpointModelChanged() {
        gutter.repaint();
        editorPane.repaint();
    }

    JComponent getComponent() {
	return tree;
    }

    void componentActivated() {
	// See 6573346
	editorPane.requestFocusInWindow();
    }


    /**
     * Map document position to contents of whole line at that position.
     * SHOULD be combined with addrFromLine to givew:
     *	Address viewToModel(pos)
     */

    private String getLineAt(int pos) {

	// By default a "paragraph" is a line
	Element pp = styledDoc.getParagraphElement(pos);

	int startPos = pp.getStartOffset();
	int endPos = pp.getEndOffset();
	int length = endPos - startPos;
	String line = null;
	try {
	    line = styledDoc.getText(startPos, length);
	} catch (Exception x) {
	}
	return line;
    }

    /**
     * Extract the address portion from a full line.
     */

    private String addrFromLine(String line) {
	String address;
	String output;
	int i, j;

	if (line == null)
	    return null;

	// /6552624
	if (!line.startsWith("   0x"))	// NOI18N
	    return null;

	// /6552624
	i = line.indexOf("0x");		// NOI18N
	address = line.substring(i);
	i = address.indexOf(":");	// NOI18N
	j = address.indexOf(" ");	// Linux, PLT address does not have : // NOI18N
	if (i > 0)
	    address = address.substring(0, i);
	else if (j > 0)
	    address = address.substring(0, j);

	return address;
    }

    /**
     * Return the contents of the line under the cursor.
     */

    private String getCurrentLine() {
	Caret caret = editorPane.getCaret();
	int pos = caret.getDot();
	return getLineAt(pos);
    }


    /**
     * Return the address corresponding to the line under the cursor.
     */
    public String getCurrentAddress() {
	String address = addrFromLine(getCurrentLine());
	return address;
    }


    void clear() {
	try {
	    styledDoc.remove(0, styledDoc.getLength());
	} catch (BadLocationException ble1) {
	    System.err.println("View.clear(): Couldn't remove initial text from document.");
	}
    }


    private void insert(String text, LineStyle style) {
	assert ! Log.Dis.cndAsm;
	try {
	    styledDoc.insertString(styledDoc.getLength(),
				   text,
				   styledDoc.getStyle(style.toString()));
	} catch (BadLocationException ble2) {
	    System.err.println("View.clear(): Couldn't insert initial text into document.");
	}
    }

    private void setCaretPosition(int carpos) {
	try {
	    Rectangle bubble = editorPane.modelToView(carpos);
	    Rectangle viewRect = textScrollPane.getViewport().getViewRect();

	    if (Log.Dis.bubble) {
		System.out.printf("setCaretPosition(%d)\n", carpos); // NOI18N
		System.out.printf("\tbubble %d[%d]%d\n", // NOI18N
		   bubble.y, bubble.height, bubble.x);
		System.out.printf("\tviewRect %d[%d]%d\n", // NOI18N
		    viewRect.y, viewRect.height, viewRect.y + viewRect.height);
	    }

	    // It's tempting to use Rectangle.contains() to decide if we 
	    // need to scroll, but most of the time the x coordinates are
	    // both 0 and contains() will always retain false.
	    // So we test y's explicitly.

	    boolean shouldScroll = false;
	    if (bubble.y < viewRect.y)
		shouldScroll = true;
	    else if (bubble.y  + bubble.height > viewRect.y + viewRect.height)
		shouldScroll = true;

	    if (shouldScroll) {
		// scroll so the position is near the center of the view.
		// we do this by expanding our bubble

		// How many rectangles high is the view?
		int rectHeight = viewRect.height / bubble.height;

		// subtract one for the current position
		rectHeight --;	

		// half to go above current position, half to go below
		rectHeight /= 2;

		int x     = bubble.x;
		int width = bubble.width;

		int y      = bubble.y - bubble.height *  rectHeight;
		int height =            bubble.height * (rectHeight * 2 + 1);

		Rectangle xBubble = new Rectangle(x, y, width, height);
		if (Log.Dis.bubble) {
		    System.out.printf("\trectHeight %d\n", rectHeight); // NOI18N
		    System.out.printf("\txBubble %d[%d]%d\n", // NOI18N
			xBubble.y, xBubble.height, xBubble.x);
		}

		editorPane.scrollRectToVisible(xBubble);

		// No need to do this
		// textScrollPane.getRowHeader().scrollRectToVisible(xBubble);
		textScrollPane.getRowHeader().repaint();
	    }

	    editorPane.setCaretPosition(carpos);

	} catch (Exception e) {
	    // bad position carpos
	    editorPane.setCaretPosition(0);
	}
    }


    private int getCaretPosition() {
	return editorPane.getCaretPosition();
    }


    private void addStylesToDocument(StyledDocument doc) {

	Color source_line_fg_color  = Color.gray;
	Color code_line_fg_color    = Color.black;
	
	Style def = StyleContext.getDefaultStyleContext().
			getStyle(StyleContext.DEFAULT_STYLE);
	// StyleConstants.setFontFamily(def, "SansSerif");


	// Do not set the background color!
	// The default background color is "transparent" which allows
	// bpt and other stripes to show through


	// Attempt to match default styles from 
	// asm/src/org/netbeans/modules/asm/core/resources/
	//	NetBeans-Asm-fontsColors.xml

	// Source line
	Style s = doc.addStyle(LineStyle.Source_line.toString(), def);
	StyleConstants.setForeground(s, source_line_fg_color);

	// Code line
	s = doc.addStyle(LineStyle.Code_line.toString(), def);
	StyleConstants.setForeground(s, code_line_fg_color);

	// Address
	s = doc.addStyle(LineStyle.address.toString(), def);
	StyleConstants.setForeground(s, Color.red.darker());

	// Label
	// ASM_LABEL
	s = doc.addStyle(LineStyle.label.toString(), def);
	StyleConstants.setForeground(s, code_line_fg_color);
	StyleConstants.setBold(s, true);

	// ASM_INSTRUCTION
	s = doc.addStyle(LineStyle.ins.toString(), def);
	StyleConstants.setForeground(s, new Color(0x00, 0x9B, 0x00));

	// ASM_INSTRUCTION
	s = doc.addStyle(LineStyle.op.toString(), def);
	StyleConstants.setForeground(s, new Color(0x00, 0x9B, 0x00));

	// ASM_REGISTER
	s = doc.addStyle(LineStyle.args.toString(), def);
	StyleConstants.setForeground(s, new Color(0x2E, 0x92, 0xC7));

	// ASM_COMMENT
	s = doc.addStyle(LineStyle.comment.toString(), def);
	StyleConstants.setForeground(s, new Color(0x96, 0x96, 0x96));
    }


    /**
     * Return number of bpts if there are disabled breakpoints 
     * on this line
     */
    private int checkForDisabledBpt(String line) {
	if (breakpointModel == null)
	    return 0;

	String address = addrFromLine(line);
	if (address == null)
	    return 0;

	long addr = 0;
	try {
	    addr = Address.parseAddr(address);
	} catch (NumberFormatException x) {
	    ErrorManager.getDefault().notify(x);
	}
	return breakpointModel.findDisabled(addr);
    }

    /**
     * Return number of bpts if there are breakpoints on this line
     */
    private int checkForBreakpoint(String line) {
	if (breakpointModel == null)
	    return 0;

	String address = addrFromLine(line);
	if (address == null)
	    return 0;

	long addr = 0;
	try {
	    addr = Address.parseAddr(address);
	} catch (NumberFormatException x) {
	    ErrorManager.getDefault().notify(x);
	}
	return breakpointModel.find(addr);
    }

    /**
     * Extract the position index of a register from a line.
     */

    private int regIndexFromLine(String line) {
	String reg = null;
	String output;
	int i, j;

	if (line == null)
	    return 0;

	i = line.indexOf("%");		// NOI18N
	if (i > 0) 
	    return i;
	else
	    return 0;
    }

    /**
     * Return position index of a register pair in the line.
     */
    private int[] checkForRegs(String line) {
	int index[] = new int[2];
	if (stateModel == null)
	    return null;

	index[0] = regIndexFromLine(line);
	if (index[0] > 0) {  
	    // Found the first reg, may have more than one
	    int i = regIndexFromLine(line.substring(index[0]+1));
	    if (i > 0) {
		// Found the second reg, no more
	        index[1] = index[0] + i;
	    }
	} else
	    index[1] = 0;
	return index;
    }

    /**
     * Return true if current PC is on this line.
     */
    private boolean checkForCurrent(String line) {
	if (stateModel == null)
	    return false;

	String address = addrFromLine(line);
	if (address == null)
	    return false;
	long addr = 0;
	try {
	    addr = Address.parseAddr(address);
	} catch (NumberFormatException x) {
	    ErrorManager.getDefault().notify(x);
	}
	return (addr == stateModel.getPC());
    }


    /**
     * Figure what style to render the line in.
     */

    private LineStyle figureStyle(String line) {
	LineStyle style_index;
	if (line.startsWith("   0x")) {		// NOI18N
	    style_index = LineStyle.Code_line;
	} else {
	    style_index = LineStyle.Source_line;
	}
	return style_index;
    }

    public void setModelController(DisFragModel m, Controller c, StateModel s, BreakpointModel b) {
	// out with the old ...
	if (model != null)
	    model.removeListener(fragListener);
	if (breakpointModel != null)
	    breakpointModel.removeListener(breakpointListener);
	if (stateModel != null)
	    stateModel.removeListener(stateListener);

	// ... switch ...
	model = m;
	controller = c;
	breakpointModel = b;
	stateModel = s;

	// ... in with the new
	if (breakpointModel != null)
	    breakpointModel.addListener(breakpointListener);
	if (stateModel != null)
	    stateModel.addListener(stateListener);
	if (model != null)
	    model.addListener(fragListener);

	breakpointModelChanged();
	stateModelChanged();
    }

    public void updateWindow () {
	modelChanged();
	stateModelChanged();
	breakpointModelChanged();
    }

    public void showCurrentStatement() {
	// LATER model.updateStateModel();
	updateWindow();
    }

    /*
     * BreakpointModel stuff
     */
    private final BreakpointModel.Listener breakpointListener =
	new BreakpointModel.Listener() {
            @Override
	    public void bptUpdated() {
		breakpointModelChanged();
	    }
	};

    public void setBreakpointModel(BreakpointModel newBreakpointModel) {

	// out with the old ...
	if (breakpointModel != null)
	    breakpointModel.removeListener(breakpointListener);

	// ... switch ...
	breakpointModel = newBreakpointModel;

	// ... in with the new
	if (breakpointModel != null)
	    breakpointModel.addListener(breakpointListener);

	breakpointModelChanged();
    }

    public BreakpointModel getBreakpointModel() {
      return breakpointModel;
    }

    /*
     * DisFragModel stuff
     */
    private final DisFragModel.Listener fragListener =
	new DisFragModel.Listener() {
            @Override
	    public void fragUpdated() {
		modelChanged();
	    }
	};

    /*
     * StateModel stuff
     */
    private final StateModel.Listener stateListener =
	new StateModel.Listener() {
            @Override
	    public void stateUpdated() {
		stateModelChanged();
	    }
	};

    public void setStateModel(StateModel sm) {
	// out with the old ...
	if (stateModel != null)
	    stateModel.removeListener(stateListener);

	// ... switch ...
	stateModel = sm;

	// ... in with the new
	if (stateModel != null)
	    stateModel.addListener(stateListener);

	stateModelChanged();
    }

    public StateModel getStateModel() {
        return stateModel;
    }
}
