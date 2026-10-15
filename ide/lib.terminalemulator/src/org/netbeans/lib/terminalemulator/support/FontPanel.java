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

package org.netbeans.lib.terminalemulator.support;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A clone of o.n.core/src/org/netbeans/beaninfo/editors/FontEditor.java
 */
class FontPanel extends JPanel {

//    private final TermOptionsPanel fontPanel;
    static final Color ERROR_COLOR = new Color(235, 0, 0); // IZ#204301
    
    private static final boolean simulateSlowness = false;

    private JDialog containingJDialog;

    private JCheckBox showFixedCheckBox;
    private JTextField tfFont;
    private JTextField tfStyle;
    private JTextField tfSize;
    private JList<FontDescr> lFont;
    private JList<String> lStyle;
    private JList<Integer> lSize;
    private boolean dontSetValue = false;

    private static boolean showFixed = true;

    // List of fonts the user can choose from.
    private static Fonts fonts;

    private String fontFamily;
    private int size = 12;
    private int style;

    // font is a derived value (from new Font(fontFamily, style, size))
    private Font font = null;

    private String errorMsg = null;


    private static class FontDescr {
        private String name;
        private boolean isFixed;

        public FontDescr(String name, boolean isFixed) {
            this.name = name;
            this.isFixed = isFixed;
        }

        @Override
        public String toString() {
            return name;
        }

        public String name() {
            return name;
        }

        public boolean isFixed() {
            return isFixed;
        }
    }

    /**
     * Return true if this font is fixed width.
     * Only the first 256 characters are considered.
     * @param font
     * @return true if this font is fixed width.
     */
    private static boolean isFixedWidth(Component context, Font font) {
	FontMetrics metrics = context.getFontMetrics(font);
	int[] widths = metrics.getWidths();
	int Swidth = widths[0];
	for (int cx = 1; cx < widths.length; cx++) {
	    int width = widths[cx];
	    if (width == 0) {
		continue;
	    } else if (Swidth != width) {
		return false;
	    }
	}
	return true;
    }


    /**
     * Encapsulates the list of fonts the user can choose from.
     */
    private static final class Fonts {
	private final List<FontDescr> fonts = new ArrayList<>();

	public Fonts() {
	    add(new FontDescr("Monospaced", true));	// NOI18N
	}

	public FontDescr[] toArray() {
	    return fonts.toArray(new FontDescr[0]);
	}

	public FontDescr descrByName(String fontName) {
	    for (FontDescr fontDescr : fonts) {
		if (fontDescr.name().equals(fontName))
		    return fontDescr;
	    }
	    return null;
	}

	public void add(FontDescr fontDescr) {
	    fonts.add(fontDescr);
	}

	public FontDescr get(int x) {
	    return fonts.get(x);
	}

	public int size() {
	    return fonts.size();
	}


    }


    private class GetFontsWorker extends SwingWorker<Fonts, Object> {

	public static final String PROP_NFONTS = "nfonts";	// NOI18N

	private final Component comp;
	private final boolean showFixed;
	private final int size;
	private final int style;

	private Fonts result;

	/**
	 *
	 * @param comp Component in which we calculate font metrics
	 * @param showFixed
	 * @param size size for which we calculate font metrics
	 * @param style style for which we calculate font metrics
	 */
	public GetFontsWorker(Component comp, boolean showFixed, int size, int style) {
	    this.comp = comp;
	    this.showFixed = showFixed;
	    this.size = size;
	    this.style = style;
	}

	@Override
        @SuppressWarnings("SleepWhileInLoop")
	protected Fonts doInBackground() throws Exception {
            String[] fontNames;
            try {
                fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment ().getAvailableFontFamilyNames();
		if (simulateSlowness)
		    Thread.sleep(2500);	// millis
            } catch (RuntimeException e) {
                throw e;
            }

	    firePropertyChange(PROP_NFONTS, null, fontNames.length);

            // It turns out that "Monospaced" is actually not fixed width
            // in bold style. So if we honor style then under certain
            // circumstances Monospaced will become non-fixed.
            // But we depend on it as a surefire fallback/
            // All I can think of to deal with this is to consider fixedness
            // only under PLAIN.
            // Perhaps SHOULD special-case "Monospaced"?
            // TMP style = Font.PLAIN;

	    result = new Fonts();
	    for (int fx = 0; fx < fontNames.length; fx++) {
		if (isCancelled())
		    break;
		Font f = new Font (fontNames[fx], style, size);
		boolean isFixedWidth = isFixedWidth(comp, f);
		if (showFixed) {
		    if (isFixedWidth)
			result.add(new FontDescr(fontNames[fx], isFixedWidth));
		} else {
		    result.add(new FontDescr(fontNames[fx], isFixedWidth));
		}
		if (simulateSlowness) {
		    Thread.sleep(100);	// millis
                }
		setProgress((100 * fx) / fontNames.length);
	    }

	    return result;
	}

	@Override
	protected void done() {
	    Fonts fonts = null;
	    try {
		fonts = get();
	    } catch (InterruptedException | ExecutionException ex) {
		Logger.getLogger(FontPanel.class.getName()).log(Level.SEVERE, null, ex);
	    } catch (CancellationException ex) {
		// Logger.getLogger(FontPanel.class.getName()).log(Level.WARNING, null, ex);
	    }
	    gotFonts(fonts);
	}
    }

    /**
     * Effectively control whether font chooser dialog (the one containing us)
     * is modal.
     */
    private void setModal(boolean modal) {
	assert containingJDialog() != null;
	if (modal) {
	    originalCursor = containingJDialog().getCursor();
	    containingJDialog().setCursor(waitCursor);
	    containingJDialog().getGlassPane().setVisible(true);
	    containingJDialog().getGlassPane().addMouseListener(new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
		    e.consume();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		    e.consume();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		    e.consume();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		    e.consume();
		}

		@Override
		public void mouseExited(MouseEvent e) {
		    e.consume();
		}
	    });

	} else {
	    containingJDialog().setCursor(originalCursor);
	    containingJDialog().getGlassPane().setVisible(false);
	}
    }

    // State to help us restore stuff after Worker is done.
    // HACK ... should use a trampoline runnable.
    private Runnable continuation;
    private ProgressMonitor progressMonitor;
    private Cursor originalCursor;
    private Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

    private void gotFonts(Fonts fonts) {
	assert SwingUtilities.isEventDispatchThread();

	// If 'fonts' is null it means worker was cancelled 
	// or some other trouble so don't switch.
	if (fonts != null) {
	    FontPanel.fonts = fonts;
        }

	progressMonitor.close();
	progressMonitor = null;
	setModal(false);

	if (continuation != null)
	    continuation.run();
    }

    static final Integer[] sizes = new Integer [] {
                                       Integer.valueOf (3),
                                       Integer.valueOf (5),
                                       Integer.valueOf (8),
                                       Integer.valueOf (10),
                                       Integer.valueOf (12),
                                       Integer.valueOf (14),
                                       Integer.valueOf (18),
                                       Integer.valueOf (24),
                                       Integer.valueOf (36),
                                       Integer.valueOf (48)
                                   };

    static final String[] styles = new String [] {
                                       Catalog.get("CTL_Plain"),	// NOI18N
                                       Catalog.get("CTL_Bold"),		// NOI18N
                                       Catalog.get("CTL_Italic"),	// NOI18N
                                       Catalog.get("CTL_BoldItalic")	// NOI18N
                                   };

    /**
     * Help render FontDescr's.
     * This renderer does two things:
     * 1) The lFont JList is a list of FontDescrs's and JLIst doesn't know how
     *    to render them by default, so this renderer bridges them.
     * 2) When we have fonts that are not fixed width we'd like to render
     *    them in grey.
     */
    static class MyListCellRenderer implements ListCellRenderer<FontDescr> {
        private final ListCellRenderer<? super FontDescr> delegate;

        MyListCellRenderer(ListCellRenderer<? super FontDescr> delegate) {
            this.delegate = delegate;
        }

	@Override
        public Component getListCellRendererComponent(JList<? extends FontDescr> list, FontDescr value, int index, boolean isSelected, boolean cellHasFocus) {
            FontDescr fd = value;
            Component c = delegate.getListCellRendererComponent(list, fd, index, isSelected, cellHasFocus);
            if (fd.isFixed()) {
                c.setForeground(Color.BLACK);
            } else {
                c.setForeground(Color.GRAY);
            }
            return c;
        }

    }

    /*
     * Handle font family selection changes (lFont).
     */
    private final ListSelectionListener lFontListener = new ListSelectionListener() {
	@Override
        public void valueChanged(ListSelectionEvent e) {
            if (!lFont.isSelectionEmpty()) {
                if (fonts.size() > 0) {
                    //Mac bug workaround
                    int i = lFont.getSelectedIndex();
                    String newFontName = fonts.get(i).name();
                    if (! newFontName.equals(fontFamily)) {
                        tfFont.setText(newFontName);
                        setValue(null);
                    }
                }
            }
        }
    };

    private void lFontListen(boolean listen) {
        if (listen)
            lFont.addListSelectionListener(lFontListener);
        else
            lFont.removeListSelectionListener(lFontListener);
    }

    FontPanel(Font font, TermOptionsPanel parent) {
        super();
//        this.fontPanel = parent;
        this.font = font;
        dontSetValue = false;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 0, 11));
        if (font == null) {
            if (fonts != null && fonts.size() > 0) {
                font = new Font(fonts.get(0).name(), Font.PLAIN, 10);
            } else {
                font = UIManager.getFont("Label.font");		// NOI18N
            }
        }

	this.size = font.getSize();
	this.style = font.getStyle();
	this.fontFamily = font.getFamily();

	if  (fonts == null || fonts.size() == 1) {
	    // First time ever or if called again after a worker canellation

	    // Initial list made up of just the passed-in font
	    Fonts new_fonts = new Fonts();
	    if (!font.getFamily().equals("Monospaced")) {	// NOI18N
		new_fonts.add(new FontDescr(font.getFamily(), isFixedWidth(parent, font)));
            }

            fonts = new_fonts;
            
	    SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    updateFontList();
		}
	    });
	} else {
	    SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    trackFont();
		}
	    });
	}

        lFont = new JList<>(fonts.toArray());
        lFont.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lFont.getAccessibleContext().setAccessibleDescription("ACSD_CTL_Font");	// NOI18N
        lFont.setCellRenderer(new MyListCellRenderer(lFont.getCellRenderer()));

        lStyle = new JList<>(styles);
        lStyle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lStyle.getAccessibleContext().setAccessibleDescription("ACSD_CTL_FontStyle");	// NOI18N
        lSize = new JList<>(sizes);
        lSize.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lSize.getAccessibleContext().setAccessibleDescription("ACSD_CTL_Size");	// NOI18N
        tfSize = new JTextField(String.valueOf(font.getSize()));
        tfSize.getAccessibleContext().setAccessibleDescription(lSize.getAccessibleContext().getAccessibleDescription());
        getAccessibleContext().setAccessibleDescription("ACSD_FontCustomEditor");	// NOI18N


        GridBagLayout la = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(la);

        c.gridwidth = 1;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;

        c.insets = new Insets(0, 0, 10, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        showFixedCheckBox = new JCheckBox();
        showFixedCheckBox.setSelected(showFixed);
        showFixedCheckBox.setText(Catalog.get("LBL_FixedOnly"));	// NOI18N
        showFixedCheckBox.setMnemonic(Catalog.get("MNM_FixedOnly").charAt(0));	// NOI18N
        showFixedCheckBox.addActionListener(new java.awt.event.ActionListener() {
	    @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // showFixedActionPerformed(evt);
                showFixed = showFixedCheckBox.isSelected();
                updateFontList();
            }
        });
        add(showFixedCheckBox, c);

        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        JLabel l = new JLabel();
        l.setText(Catalog.get("LBL_Font"));	// NOI18N
        l.setDisplayedMnemonic(Catalog.mnemonic("MNM_Font"));	// NOI18N
        l.setLabelFor(lFont);
        la.setConstraints(l, c);
        add(l);


        c.insets = new Insets(0, 5, 0, 0);
        l = new JLabel();
        l.setText(Catalog.get("LBL_FontStyle"));	// NOI18N
        l.setDisplayedMnemonic(Catalog.mnemonic("MNM_FontStyle"));	// NOI18N
        l.setLabelFor(lStyle);
        la.setConstraints(l, c);
        add(l);


        c.insets = new Insets(0, 5, 0, 0);
        c.gridwidth = GridBagConstraints.REMAINDER;
        l = new JLabel();
        l.setText(Catalog.get("LBL_Size"));	// NOI18N
        l.setDisplayedMnemonic(Catalog.mnemonic("MNM_Size"));	// NOI18N
        l.setLabelFor(tfSize);
        la.setConstraints(l, c);
        add(l);


        c.insets = new Insets(5, 0, 0, 0);
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        tfFont = new JTextField(font.getFamily());
        tfFont.setEnabled(false);
        la.setConstraints(tfFont, c);
        add(tfFont);
        c.insets = new Insets(5, 5, 0, 0);
        tfStyle = new JTextField(Catalog.get(parent.getStyleName(font.getStyle())));
        tfStyle.setEnabled(false);
        la.setConstraints(tfStyle, c);
        add(tfStyle);
        c.insets = new Insets(5, 5, 0, 0);
        c.gridwidth = GridBagConstraints.REMAINDER;

        tfSize.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    setValue(null);
                }
            }
        });

        tfSize.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evt) {
                if (dontSetValue) {
                    return;
                } else {
                    dontSetValue = true;
                }
                Component c = evt.getOppositeComponent();
                if (c != null) {
                    if (c instanceof JButton) {
                        if (((JButton) c).getText().equals("CTL_OK")) {	// NOI18N
                            setValue(null);
                        }
                    } else {
                        setValue(null);
                    }
                }
            }

            @Override
            public void focusGained(FocusEvent evt) {
                dontSetValue = false;
            }
        });

        la.setConstraints(tfSize, c);
        add(tfSize);
        c.gridwidth = 1;
        c.insets = new Insets(5, 0, 0, 0);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        lFont.setVisibleRowCount(5);

        lFontListen(true);
        JScrollPane sp = new JScrollPane(lFont);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        la.setConstraints(sp, c);
        add(sp);
        style = font.getStyle();
        lStyle.setVisibleRowCount(5);
        lStyle.setSelectedValue(parent.getStyleName(style), true);

        lStyle.addListSelectionListener(new ListSelectionListener() {
	    @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!lStyle.isSelectionEmpty()) {
                    int i = lStyle.getSelectedIndex();
                    String newStyleName = styles[i];
                    if (! newStyleName.equals(tfStyle.getText())) {
                        tfStyle.setText(styles[i]);
                        setValue(null);
                    }
                }
            }
        });

        sp = new JScrollPane(lStyle);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        c.insets = new Insets(5, 5, 0, 0);
        la.setConstraints(sp, c);
        add(sp);
        c.gridwidth = GridBagConstraints.REMAINDER;
        lSize.getAccessibleContext().setAccessibleName(tfSize.getAccessibleContext().getAccessibleName());
        lSize.setVisibleRowCount(5);
        updateSizeList(font.getSize());

        lSize.addListSelectionListener(new ListSelectionListener() {
	    @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!lSize.isSelectionEmpty()) {
                    int i = lSize.getSelectedIndex();
                    tfSize.setText(String.valueOf(sizes[i]));
                    setValue(null);
                }
            }
        });

        sp = new JScrollPane(lSize);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        c.insets = new Insets(5, 5, 0, 0);
        la.setConstraints(sp, c);
        add(sp);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 2.0;
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder(" " + Catalog.get("CTL_Preview") + " "));	// NOI18N
        JPanel pp = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(150, 60);
            }

            @Override
            public void paint(Graphics g) {
                //          super.paint (g);
                paintValue(g, new Rectangle(0, 0, getSize().width - 1, getSize().height - 1),
                           font(),
			   Catalog.get("MSG_Sample"),	// NOI18N
                           errorMsg);
            }
        };

        p.add("Center", pp);                    // NOI18N
        c.insets = new Insets(12, 0, 0, 0);
        la.setConstraints(p, c);
        add(p);
    }

    private JDialog containingJDialog() {
	if (containingJDialog == null) {
	    Component dialogCandidate = this;
	    while (dialogCandidate != null && !(dialogCandidate instanceof JDialog))
		dialogCandidate = dialogCandidate.getParent();
	    if (dialogCandidate == null)
		containingJDialog = null;
	    else
		containingJDialog = (JDialog) dialogCandidate;
	}
	return containingJDialog;
    }


    /*
     * Called from constructor or when showFixed, style or size changes.
     */
    private void updateFontList() {
	Runnable whenDone = new Runnable() {
	    @Override
	    public void run() {
		fontListUpdated();
	    }
	};
	this.continuation = whenDone;

	setModal(true);

	final GetFontsWorker worker = new GetFontsWorker(this, showFixed, size, style);
	progressMonitor = new ProgressMonitor(this,
				  Catalog.get("MSG_TakingInventory"),// NOI18N
				  " ",				// NOI18N
				  0, 2);
	// TMP progressMonitor.setMillisToDecideToPopup(0);
	// TMP progressMonitor.setMillisToPopup(0);
	// kick-start it so the progress dialog becomes visible.
	progressMonitor.setProgress(1);

	// Track notifications from worker and update pogressMonitor

	worker.addPropertyChangeListener(new PropertyChangeListener() {

	    private boolean ckCancel() {
		// SwingWorker queues up "progress" notificatons so
		// it's possible that we receive some after SwingWorker.done()
		// is called! If we then call setProgress() it "resurrects"
		// a closed ProgresMonitor.
		if (worker.isCancelled()) {
		    return true;
		} else if (progressMonitor == null || progressMonitor.isCanceled()) {
		    boolean withInterrupts = true;
		    worker.cancel(withInterrupts);
		    return true;
		} else {
		    return false;
		}
	    }
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
                switch (evt.getPropertyName()) {
                    case "progress":// NOI18N
                        if (ckCancel())
                            return;
                        progressMonitor.setProgress((Integer) evt.getNewValue());
                        break;
                    case GetFontsWorker.PROP_NFONTS:
                        if (ckCancel())
                            return;
                        progressMonitor.setNote(Catalog.get("MSG_CheckingFixedWidth")); // NOI18N
                        progressMonitor.setMaximum((Integer) evt.getNewValue());
                        break;
                }
	    }
	});

	worker.execute();
    }

    /**
     * Called when we're done with updateFontList.
     */
    private void fontListUpdated() {
	assert SwingUtilities.isEventDispatchThread();
        try {
	    lFontListen(false);
            lFont.setListData(fonts.toArray());
	    trackFont();
        } finally {
            lFontListen(true);
        }
    }

    /**
     * Ensure that the current 'font' is selected in the font list.
     * If current font is not in the font list post an error message
     * and fall back on the first font in the list.
     */
    private void trackFont() {
	// Now done in fontListUpdated()
	fontFamily = font.getFamily();
	lFont.setSelectedValue(fonts.descrByName(fontFamily), true);
	int is = lFont.getSelectedIndex();
	String err;
	if (is == -1) {
	    String fallbackFontFamily = fonts.get(0).name();
	    err = Catalog.format("FMT_FontUnavailable", fontFamily, fallbackFontFamily);	// NOI18N

	    fontFamily = fallbackFontFamily;
	    lFont.setSelectedValue(fonts.get(0), true);
	    tfFont.setText(fontFamily);
	    font = new Font(fontFamily, style, size);
	} else {
	    err = null;
	}
	setValue(err);
    }

    public Font font() {
        return font;
    }

    public void paintValue(Graphics g, Rectangle rectangle, Font font, String sample, String errorMsg) {
        Font originalFont = g.getFont();
        // Fix of 21713, set default value
        // LATER if ( font == null ) setValue( null );
        Font paintFont = font == null ? originalFont : font;
        // NOI18N
        assert paintFont != null : "paintFont must exist.";
        FontMetrics fm = g.getFontMetrics(paintFont);
        if (fm.getHeight() > rectangle.height) {
            /* LATER
            if (Utilities.isMac()) {
            // don't use deriveFont() - see #49973 for details
            paintFont = new Font(paintFont.getName(), paintFont.getStyle(), 12);
            } else
             */
            {
                paintFont = paintFont.deriveFont(12.0F);
            }
            fm = g.getFontMetrics(paintFont);
        }
        g.setFont(paintFont);
        int height = (rectangle.height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(sample == null ? "null" : sample,	// NOI18N
                     rectangle.x, rectangle.y + height);
        if (errorMsg != null) {
            Color originalColor = g.getColor();
            g.setColor(ERROR_COLOR);
            g.drawString(errorMsg,
                         rectangle.x, rectangle.y + height + fm.getAscent());
            g.setColor(originalColor);

        }
        g.setFont(originalFont);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 350);
    }

    private void updateSizeList(int size) {
        if (Arrays.asList(sizes).contains(Integer.valueOf(size))) {
            lSize.setSelectedValue(Integer.valueOf(size), true);
        } else {
            lSize.clearSelection();
        }
    }

    /**
     * Called whenever any of the controls on the panel gets an event
     */
    private void setValue(String errorMsg) {

        boolean sizeChanged;
        boolean styleChanged;

        int oldSize = size;
        size = 12;
        try {
            size = Integer.parseInt(tfSize.getText());
            if (size <= 0) {
                IllegalArgumentException iae = new IllegalArgumentException();
                /* LATER
                UIExceptions.annotateUser (iae, null,
                size == 0 ? "CTL_InvalidValueWithParam", tfSize.getText () : // NOI18N
                "CTL_NegativeSize", // NOI18N
                null, null);
                tfSize.setText (String.valueOf (font.getSize ()));
                 */
                throw iae;
            }
            updateSizeList(size);
        } catch (NumberFormatException e) {
            /* LATER
            UIExceptions.annotateUser (e, null,
            "CTL_InvalidValueWithExc", // NOI18N
            null, null);
            tfSize.setText (String.valueOf (font.getSize ()));
             */
            throw e;
        }
        sizeChanged = size != oldSize;

        int oldStyle = style;
        int i = lStyle.getSelectedIndex();
        style = Font.PLAIN;
        switch (i) {
            case 0:
                style = Font.PLAIN;
                break;
            case 1:
                style = Font.BOLD;
                break;
            case 2:
                style = Font.ITALIC;
                break;
            case 3:
                style = Font.BOLD | Font.ITALIC;
                break;
        }
        styleChanged = style != oldStyle;


        fontFamily = tfFont.getText();
        if (sizeChanged || styleChanged) {
            this.errorMsg = errorMsg;
            updateFontList();
        } else {
            this.errorMsg = errorMsg;
        }
        // TMP FontEditor.this.setValue (new Font (tfFont.getText (), ii, size));
        font = new Font(fontFamily, style, size);
        invalidate();
        Component p = getParent();
        if (p != null) {
            p.validate();
        }
        repaint();
    }
}
