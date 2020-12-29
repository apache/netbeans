/*
 * FontSelector.java - Font selector
 */

package org.netbeans.modules.python.options;

import java.awt.event.*;
import java.awt.*;
import java.util.Vector;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.*;
import org.openide.util.NbBundle;
/**
 * A font Selector chooser widget.
 */
class FontSelectorDialog extends JDialog
{
	public FontSelectorDialog(Frame parent, Font font)
	{
		super(parent,NbBundle.getMessage(FontSelectorDialog.class, "LBL_FontChooser"),true);
		init(font);
	} 

	public FontSelectorDialog(Dialog parent, Font font)
	{
		super(parent,NbBundle.getMessage(FontSelectorDialog.class, "LBL_FontChooser"),true);
		init(font);
	}

	public FontSelectorDialog(Frame parent, Font font,
		FontSelectorDialog fontSelector)
	{
		super(parent,NbBundle.getMessage(FontSelectorDialog.class, "LBL_FontChooser"),true);
		this.fontSelector = fontSelector;
		init(font);
	} 

	public FontSelectorDialog(Dialog parent, Font font,
		FontSelectorDialog fontSelector)
	{
		super(parent,NbBundle.getMessage(FontSelectorDialog.class, "LBL_FontChooser"),true);
		this.fontSelector = fontSelector;
		init(font);
	} 

	public void ok()
	{
		isOK = true;
		dispose();
	} 

	public void cancel()
	{
		dispose();
	} 

	public Font getSelectedFont()
	{
		if(!isOK)
			return null;

		int size;
		try
		{
			size = Integer.parseInt(sizeField.getText());
		}
		catch(NumberFormatException e)
		{
			size = 12;
		}

		return new Font(familyField.getText(),styleList
			.getSelectedIndex(),size);
	} 


	private FontSelectorDialog fontSelector;
	private boolean isOK;
	private JTextField familyField;
	private JList familyList;
	private JTextField sizeField;
	private JList sizeList;
	private JTextField styleField;
	private JList styleList;
	private JLabel preview;
	private JButton ok;
	private JButton cancel;

	/**
	 * For some reason the default Java fonts show up in the
	 * list with .bold, .bolditalic, and .italic extensions.
	 */
	private static final String[] HIDEFONTS = {
		".bold",
		".italic"
	};

	private void init(Font font)
	{
		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12,12,12,12));
		setContentPane(content);

		JPanel listPanel = new JPanel(new GridLayout(1,3,6,6));

		String[] fonts;
		try
		{
			fonts = getFontList();
		}
		catch(Exception e)
		{
			fonts = new String[] { "Broken Java implementation!" };
			e.printStackTrace() ;
		}

		JPanel familyPanel = createTextFieldAndListPanel(
			"Font:",
			familyField = new JTextField(),
			familyList = new JList(fonts));
		listPanel.add(familyPanel);

		String[] sizes = { "9", "10", "12", "14", "16", "18", "24" };
		JPanel sizePanel = createTextFieldAndListPanel(
			"Size:",
			sizeField = new JTextField(),
			sizeList = new JList(sizes));
		listPanel.add(sizePanel);

		String[] styles = {
			"plain",
			"bold",
			"italic",
			"bolditalic"
		};

		JPanel stylePanel = createTextFieldAndListPanel(
			"Font Style :",
			styleField = new JTextField(),
			styleList = new JList(styles));
		styleField.setEditable(false);
		listPanel.add(stylePanel);

		familyList.setSelectedValue(font.getFamily(),true);
		familyField.setText(font.getFamily());
		sizeList.setSelectedValue(String.valueOf(font.getSize()),true);
		sizeField.setText(String.valueOf(font.getSize()));
		styleList.setSelectedIndex(font.getStyle());
		styleField.setText((String)styleList.getSelectedValue());

		ListHandler listHandler = new ListHandler();
		familyList.addListSelectionListener(listHandler);
		sizeList.addListSelectionListener(listHandler);
		styleList.addListSelectionListener(listHandler);

		content.add(BorderLayout.NORTH,listPanel);

		preview = new JLabel("The quick brown fox jumped over the lazy dog") {
            @Override
			public void paintComponent(Graphics g)
			{
				//if(fontSelector != null)
				//	fontSelector.setAntiAliasEnabled(g);
				super.paintComponent(g);
			}
		};
		preview.setBorder(new TitledBorder(
			"Preview"));

		updatePreview();

		Dimension prefSize = preview.getPreferredSize();
		prefSize.height = 50;
		preview.setPreferredSize(prefSize);

		content.add(BorderLayout.CENTER,preview);

		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
		buttons.setBorder(new EmptyBorder(12,0,0,0));
		buttons.add(Box.createGlue());

		ok = new JButton(NbBundle.getMessage(FontSelectorDialog.class, "LBL_OK"));
		ok.addActionListener(new ActionHandler());
		getRootPane().setDefaultButton(ok);
		buttons.add(ok);

		buttons.add(Box.createHorizontalStrut(6));

		cancel = new JButton(NbBundle.getMessage(FontSelectorDialog.class, "LBL_Cancel"));
		cancel.addActionListener(new ActionHandler());
		buttons.add(cancel);

		buttons.add(Box.createGlue());

		content.add(BorderLayout.SOUTH,buttons);

		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
	} 

	private String[] getFontList()
	{
		String[] nameArray = GraphicsEnvironment
			.getLocalGraphicsEnvironment()
			.getAvailableFontFamilyNames();
		Vector nameVector = new Vector(nameArray.length);

		for(int i = 0, j; i < nameArray.length; i++)
		{
			for(j = 0; j < HIDEFONTS.length; j++)
			{
				if(nameArray[i].contains(HIDEFONTS[j]))
					break;
			}

			if(j == HIDEFONTS.length)
				nameVector.addElement(nameArray[i]);
		}

		String[] _array = new String[nameVector.size()];
		nameVector.copyInto(_array);
		return _array;
	} 

	private JPanel createTextFieldAndListPanel(String label,
		JTextField textField, JList list)
	{
		GridBagLayout layout = new GridBagLayout();
		JPanel panel = new JPanel(layout);

		GridBagConstraints cons = new GridBagConstraints();
		cons.gridx = cons.gridy = 0;
		cons.gridwidth = cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;

		JLabel _label = new JLabel(label);
		layout.setConstraints(_label,cons);
		panel.add(_label);

		cons.gridy = 1;
		Component vs = Box.createVerticalStrut(6);
		layout.setConstraints(vs,cons);
		panel.add(vs);

		cons.gridy = 2;
		layout.setConstraints(textField,cons);
		panel.add(textField);

		cons.gridy = 3;
		vs = Box.createVerticalStrut(6);
		layout.setConstraints(vs,cons);
		panel.add(vs);

		cons.gridy = 4;
		cons.gridheight = GridBagConstraints.REMAINDER;
		cons.weighty = 1.0f;
		JScrollPane scroller = new JScrollPane(list);
		layout.setConstraints(scroller,cons);
		panel.add(scroller);

		return panel;
	} 

	private void updatePreview()
	{
		String family = familyField.getText();
		int size;
		try
		{
			size = Integer.parseInt(sizeField.getText());
		}
		catch(NumberFormatException e)
		{
			size = 12;
		}
		int style = styleList.getSelectedIndex();

		preview.setFont(new Font(family,style,size));
	} 


	class ActionHandler implements ActionListener
	{
                @Override
		public void actionPerformed(ActionEvent evt)
		{
			if(evt.getSource() == ok)
				ok();
			else if(evt.getSource() == cancel)
				cancel();
		}
	} 

	class ListHandler implements ListSelectionListener
	{
                @Override
		public void valueChanged(ListSelectionEvent evt)
		{
			Object source = evt.getSource();
			if(source == familyList)
			{
				String family = (String)familyList.getSelectedValue();
				if(family != null)
					familyField.setText(family);
			}
			else if(source == sizeList)
			{
				String size = (String)sizeList.getSelectedValue();
				if(size != null)
					sizeField.setText(size);
			}
			else if(source == styleList)
			{
				String style = (String)styleList.getSelectedValue();
				if(style != null)
					styleField.setText(style);
			}

			updatePreview();
		}
	} 
}
