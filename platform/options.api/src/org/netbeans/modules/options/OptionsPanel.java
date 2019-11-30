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

package org.netbeans.modules.options;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.options.CategoryModel.Category;
import org.netbeans.modules.options.advanced.AdvancedPanel;
import org.netbeans.modules.options.ui.VariableBorder;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.awt.QuickSearch;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

public class OptionsPanel extends JPanel {
    private JPanel pCategories;
    private JPanel pCategories2;
    private JScrollPane categoriesScrollPane;
    private JPanel pOptions;
    private JPanel quickSearch;
    private Color origForeground;
    private String hintText;
    private boolean hintVisible = false;
    private JTextComponent searchTC;
    private String text2search = "";
    private boolean clearSearch = false;
    private CardLayout cLayout;
    
    private final HashMap<String, JTabbedPane> categoryid2tabbedpane = new HashMap<String, JTabbedPane>();
    private final HashMap<String, ArrayList<String>> categoryid2words = new HashMap<String, ArrayList<String>>();
    private HashMap<String, HashMap<Integer, TabInfo>> categoryid2tabs = new HashMap<String, HashMap<Integer, TabInfo>>();
    private final ArrayList<String> disabledCategories = new ArrayList<String>();

    //private final ArrayList<FileObject> advancedFOs = new ArrayList<FileObject>();
    //private final HashMap<String, Integer> dublicateKeywordsFOs = new HashMap<String, Integer>();
    //private final HashMap<FileObject, Integer> fo2index = new HashMap<FileObject, Integer>();

    private Map<String, CategoryButton> buttons = new LinkedHashMap<String, CategoryButton>();    
    private final boolean isMac = UIManager.getLookAndFeel ().getID ().equals ("Aqua");
    private final boolean isNimbus = UIManager.getLookAndFeel ().getID ().equals ("Nimbus");
    private final boolean isMetal = UIManager.getLookAndFeel() instanceof MetalLookAndFeel;
    private final boolean isFlatLaf = UIManager.getLookAndFeel().getID().startsWith("FlatLaf");
    private final boolean isGTK = UIManager.getLookAndFeel ().getID ().equals ("GTK");
    private final Color selected = isMac ? new Color(221, 221, 221) : getSelectionBackground();
    private final Color selectedB = isMac ? new Color(183, 183, 183) : (isFlatLaf ? selected : new Color (149, 106, 197));
    private final Color highlighted = isMac ? new Color(221, 221, 221) : getHighlightBackground();
    private final Color highlightedB = isFlatLaf ? highlighted : new Color (152, 180, 226);
    //private final Color iconViewBorder = new Color (127, 157, 185);
    private final ControllerListener controllerListener = new ControllerListener ();
    
    private final Color borderMac = new Color(141, 141, 141);
    private final Font labelFontMac = new Font("Lucida Grande", 0, 10);            
    private CategoryModel categoryModel;
    private boolean applyPressed = false;
    
    private static String loc (String key) {
        return NbBundle.getMessage (OptionsPanel.class, key);
    }
    
    /** Creates new form OptionsPanel */
    public OptionsPanel (CategoryModel categoryModel) {
        this(null, categoryModel);
    }
    
    public OptionsPanel (String categoryID, CategoryModel categoryModel) {
	this.categoryModel = categoryModel;
        // init UI components, layout and actions, and add some default values
        initUI(categoryID);        
        if (getActionMap().get("SEARCH_OPTIONS") == null) {//NOI18N
            InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

            if(Utilities.isMac()) {
                inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_MASK), "SEARCH_OPTIONS");//NOI18N
                // Mac cloverleaf symbol
                hintText = Bundle.Filter_Textfield_Hint("\u2318+F");
            } else {
                inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), "SEARCH_OPTIONS");//NOI18N
                hintText = Bundle.Filter_Textfield_Hint("Ctrl+F");
            }
            getActionMap().put("SEARCH_OPTIONS", new SearchAction());//NOI18N
        }
    }
    
    private String getCategoryID(String categoryID) {
        return categoryID == null ? categoryModel.getCurrentCategoryID() : categoryID;
    }

    void initCurrentCategory (final String categoryID, final String subpath) {
        //generalpanel should be moved to core/options and then could be implemented better
        //generalpanel doesn't need lookup
        boolean isGeneralPanel = "General".equals(getCategoryID(categoryID));//NOI18N
        if (categoryModel.isLookupInitialized() || isGeneralPanel) {
            setCurrentCategory(categoryModel.getCategory(getCategoryID(categoryID)), subpath);
            initActions();                        
        } else {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // change cursor                            
                            Frame[] all = Frame.getFrames();
                            if (all == null || all.length == 0) {
                                return;
                            }
                            final Frame frame = all[0];
                            final Cursor cursor = frame.getCursor();
                            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            setCurrentCategory(categoryModel.getCategory(getCategoryID(categoryID)), subpath);
                            initActions();
                            // reset cursor
                            frame.setCursor(cursor);
                            setCursor(cursor);
                        }
                    });
                }
            }, 500);                            
        }
    }
    
    private void setCurrentCategory (final CategoryModel.Category category, String subpath) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if(category == null) {
                JComponent component = new JPanel(new BorderLayout());
                JLabel label = new JLabel(loc("CTL_Options_Search_Nothing_Found"));//NOI18N
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setHorizontalTextPosition(JLabel.CENTER);
                component.add(label, BorderLayout.CENTER);
                component.setSize(pOptions.getSize());
                component.setPreferredSize(pOptions.getPreferredSize());
                final Dimension size = component.getSize();
                if (component.getParent() == null || !pOptions.equals(component.getParent())) {
                    pOptions.add(component, label.getText());
                }
                cLayout.show(pOptions, label.getText());
                checkSize(size);
                firePropertyChange("buran" + OptionsPanelController.PROP_HELP_CTX, null, null);
            } else {
                CategoryModel.Category oldCategory = categoryModel.getCurrent();
                if (oldCategory != null) {
                    (buttons.get(oldCategory.getID())).setNormal();
                }
                if (category != null) {
                    (buttons.get(category.getID())).setSelected();
                }

                categoryModel.setCurrent(category);
                JComponent component = category.getComponent();                
                category.update(controllerListener, false);
                final Dimension size = component.getSize();
                if (component.getParent() == null || !pOptions.equals(component.getParent())) {
                    pOptions.add(component, category.getCategoryName());
                }
                cLayout.show(pOptions, category.getCategoryName());
                checkSize(size);
                /*if (CategoryModel.getInstance().getCurrent() != null) {
                 ((CategoryButton) buttons.get (CategoryModel.getInstance().getCurrentCategoryID())).requestFocus();
                 } */
                firePropertyChange("buran" + OptionsPanelController.PROP_HELP_CTX, null, null);
                if (subpath != null) {
                    category.setCurrentSubcategory(subpath);
                }
            }
        } finally {
            setCursor(null);
        }
    }

    public void setCategoryInstance(CategoryModel categoryInstance) {
	this.categoryModel = categoryInstance;
    }
        
    HelpCtx getHelpCtx () {
	if(categoryModel == null) {
	    return null;
	}
        return categoryModel.getHelpCtx ();
    }
    
    void update () {
	if(categoryModel == null) {
	    return;
	}
        categoryModel.update(controllerListener, true);
    }

    void save(boolean applyButtonPressed) {
	if(categoryModel == null) {
	    return;
	}
	applyPressed = applyButtonPressed;
        save();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (applyPressed) {
                    categoryModel.update(controllerListener, false);
                }
                applyPressed = false;
            }
        });
    }
    
    void save () {
	if(categoryModel == null) {
	    return;
	}
        categoryModel.save();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (categoryModel == null) {
                    return;
                }
                clearSearchField();
                if (!applyPressed) {
                    categoryModel = null;
                }
            }
        });
    }
    
    void cancel () {
	if(categoryModel == null) {
	    return;
	}
        clearSearchField();
        categoryModel.cancel();
	categoryModel = null;
    }
    
    boolean dataValid () {
	if(categoryModel == null) {
	    return false;
	}
        return categoryModel.dataValid();
    }
    
    boolean isChanged () {
	if(categoryModel == null) {
	    return false;
	}
	return categoryModel.isChanged();
    }
    
    boolean needsReinit() {
	if(categoryModel == null) {
	    return false;
	}
        return categoryModel.needsReinit();
    }
    
    // private methods .........................................................

    @NbBundle.Messages({"Filter_Textfield_Tooltip=Press Esc or Enter with empty text to clear the filter",
        "# {0} - shortcut to access the search text field",
        "Filter_Textfield_Hint=Filter ({0})"})
    private void initUI(String categoryName) {
        this.getAccessibleContext().setAccessibleDescription(loc("ACS_OptionsPanel"));//NOI18N
        // central panel
        pOptions = new JPanel ();
        cLayout = new CardLayout();
        pOptions.setLayout (cLayout);
        pOptions.setPreferredSize (getUserSize());
        JLabel label = new JLabel (loc ("CTL_Loading_Options"));
        label.setHorizontalAlignment (JLabel.CENTER);
        pOptions.add (label, label.getText());//NOI18N

        // icon view
        pCategories2 = new JPanel (new GridBagLayout());        
        pCategories2.setBackground (getTabPanelBackground());
        pCategories2.setBorder (null);
        addCategoryButtons();        

        quickSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        quickSearch.setBackground(getTabPanelBackground());
        QuickSearch qs = QuickSearch.attach(quickSearch, null, new OptionsQSCallback());
        qs.setAlwaysShown(true);
        
        JComponent searchPanel = (JComponent) quickSearch.getComponent(0);
        searchPanel.setToolTipText(Bundle.Filter_Textfield_Tooltip());
        searchTC = (JTextComponent) searchPanel.getComponent(searchPanel.getComponentCount() - 1);
        searchTC.setToolTipText(Bundle.Filter_Textfield_Tooltip());
        searchTC.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                showHint(false);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (text2search.trim().isEmpty()) {
                    showHint(true);
                } else {
                    showHint(false);
                }
                if(e.getOppositeComponent() != null && e.getOppositeComponent().equals(quickSearch) && !clearSearch) {
                    searchTC.requestFocusInWindow();
                } else {
                    clearSearch = false;
		    if(e.getOppositeComponent() != null && e.getOppositeComponent().equals(quickSearch)) {
			pOptions.requestFocusInWindow();
		    }
                }
            }
        });
        showHint(true);
        
        pCategories = new JPanel (new BorderLayout ());
        pCategories.setBorder (BorderFactory.createMatteBorder(0,0,1,0,isFlatLaf ? UIManager.getColor("Separator.foreground"): Color.lightGray)); //NOI18N
        pCategories.setBackground (getTabPanelBackground());
        categoriesScrollPane = new JScrollPane(pCategories2, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        categoriesScrollPane.setBorder(null);
        categoriesScrollPane.getHorizontalScrollBar().setUnitIncrement(Utils.ScrollBarUnitIncrement);
        pCategories.add ("Center", categoriesScrollPane);
        pCategories.add ("East", quickSearch);
        pCategories.setPreferredSize(new Dimension(pCategories.getPreferredSize().width, 
                pCategories.getPreferredSize().height + categoriesScrollPane.getHorizontalScrollBar().getPreferredSize().height));
        
        // layout
        setLayout (new BorderLayout (10, 10));

        pOptions.setBorder(new CompoundBorder(
                new VariableBorder(null, null, borderMac, null),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)
                ));
        add(pCategories, BorderLayout.NORTH);
        add(pOptions, BorderLayout.CENTER);
     
        categoryName = getCategoryID(categoryName);
        if (categoryName != null) {
            CategoryModel.Category c = categoryModel.getCategory(getCategoryID(categoryName));
            
            CategoryButton b = buttons.get(categoryName);
            if (b != null) {
                b.setSelected();
            }
        }
    }
    
    private void clearSearchField() {
        if (text2search.trim().isEmpty()) { 
            // do nothing if options quick search was not used
            return;
        }
        searchTC.setText("");
        clearAllinQS();
    }
    
    private void showHint (boolean showHint) {
        if (hintVisible == showHint) {
            return ;
        }
        // remember orig color on first invocation
        if (origForeground == null) {
            origForeground = searchTC.getForeground();
        }
        if (showHint) {
            searchTC.setForeground(searchTC.getDisabledTextColor());
            searchTC.setText(hintText);
        } else {
            searchTC.setForeground(origForeground);
            searchTC.setText(text2search);
        }
        hintVisible = showHint;
    }
        
    private void computeOptionsWords() {
        Set<Map.Entry<String, CategoryModel.Category>> categories = categoryModel.getCategories();
        categoryid2tabs = new HashMap<String, HashMap<Integer, TabInfo>>();
        for (Map.Entry<String, CategoryModel.Category> set : categories) {
            JComponent jcomp = set.getValue().getComponent();
            String id = set.getValue().getID();
            if(jcomp instanceof JTabbedPane) {
                categoryid2tabbedpane.put(id, (JTabbedPane)jcomp);
            } else if(jcomp instanceof AdvancedPanel) {
                categoryid2tabbedpane.put(id, (JTabbedPane)jcomp.getComponent(0));
            } else if (jcomp instanceof Container) {
                handleAllComponents((Container) jcomp, id, null, -1);
            }
        }

        FileObject keywordsFOs = FileUtil.getConfigRoot().getFileObject(CategoryModel.OD_LAYER_KEYWORDS_FOLDER_NAME);

        for(FileObject keywordsFO : keywordsFOs.getChildren()) {
            handlePanel(keywordsFO);
        }
    }

    private void handlePanel(FileObject keywordsFO) {
        String location = keywordsFO.getAttribute("location").toString(); //NOI18N
        String tabTitle = keywordsFO.getAttribute("tabTitle").toString(); //NOI18N
        JTabbedPane pane = categoryid2tabbedpane.get(location);
        int tabIndex = pane == null ? -1 : pane.indexOfTab(tabTitle);

        Set<String> keywords = new HashSet<String>();
	keywords.add(location.toUpperCase());
        keywords.add(tabTitle.toUpperCase());
	Enumeration<String> attributes = keywordsFO.getAttributes();
	while(attributes.hasMoreElements()) {
	    String attribute = attributes.nextElement();
	    if(attribute.startsWith("keywords")) {
		String word = keywordsFO.getAttribute(attribute).toString();
		keywords.add(word.toUpperCase());
	    }
	}

        ArrayList<String> words = categoryid2words.get(location);
        if (words == null) {
            words = new ArrayList<String>();
        }

        Set<String> newWords = new HashSet<String>();
        for (String keyword : keywords) {
            if (!words.contains(keyword)) {
                newWords.add(keyword);
             }
         }

        words.addAll(newWords);
        categoryid2words.put(location, words);

        if (!categoryid2tabs.containsKey(location)) {
            categoryid2tabs.put(location, new HashMap<Integer, TabInfo>());
        }
        HashMap<Integer, TabInfo> categoryTabs = categoryid2tabs.get(location);
        TabInfo tabInfo;
        if (!categoryTabs.containsKey(tabIndex)) {
            tabInfo = new TabInfo();
        } else {
            tabInfo = categoryTabs.get(tabIndex);
        }
        tabInfo.addWords(keywords);
        categoryTabs.put(tabIndex, tabInfo);
        categoryid2tabs.put(location, categoryTabs);
     }

    private void handleAllComponents(Container container, String categoryID, JTabbedPane tabbedPane, int index) {
        Component[] components = container.getComponents();
        Component component;
        for (int i = 0; i < components.length; i++) {
            component = components[i];
            String text;
            
            if(component instanceof JTabbedPane) {
                if(categoryid2tabbedpane.get(categoryID) == null) {
                    categoryid2tabbedpane.put(categoryID, (JTabbedPane)component);
                }
            } else {
                handleAllComponents((Container)component, categoryID, tabbedPane, index);
            }
        }
        
    }
    
    private class TabInfo {

        private ArrayList<String> words;

        public TabInfo() {
            this.words = new ArrayList<String>();
        }

        public ArrayList<String> getWords() {
            return words;
        }

        public void addWord(String word) {
            words.add(word.toUpperCase());
        }

        public void addWords(Set<String> words) {
            for (String word : words) {
                addWord(word);
            }
        }
    }

    final class OptionsQSCallback implements QuickSearch.Callback {

        private boolean initialized = false;

        @Override
        public void quickSearchUpdate(String searchText) {
            if (!searchText.equalsIgnoreCase(hintText)) {
                text2search = searchText.trim();
            }
        }
        
        private void showWaitCursor() {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                    mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    mainWindow.getGlassPane().setVisible(true);
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(OptionsDisplayerImpl.class, "CTL_Searching_Options"));
                }
            });
        }

        private void hideWaitCursor() {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    StatusDisplayer.getDefault().setStatusText("");  //NOI18N
                    JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                    mainWindow.getGlassPane().setVisible(false);
                    mainWindow.getGlassPane().setCursor(null);
                }
            });
        }

        private int getNextEnabledTabIndex(JTabbedPane pane, int currentIndex) {
            for (int i = currentIndex + 1; i < pane.getTabCount(); i++) {
                if(pane.isEnabledAt(i)) {
                    return i;
                }
            }
            for (int i = 0; i < currentIndex; i++) {
                if(pane.isEnabledAt(i)) {
                    return i;
                }
            }
            return -1;
        }

        private boolean containsAllSearchWords(ArrayList<String> keywords, Collection<String> stWords) {
            Iterator<String> e = stWords.iterator();
            while (e.hasNext()) {
                if (!containsSearchWord(keywords, e.next())) {
                    return false;
                }
            }
            return true;
        }

        private boolean containsSearchWord(ArrayList<String> keywords, String stWord) {
            Iterator<String> e = keywords.iterator();
            while (e.hasNext()) {
                if (e.next().contains(stWord)) {
                    return true;
                }
            }
            return false;
        }

	private ArrayList<String> getAllMatchedKeywords(ArrayList<String> keywords, Collection<String> stWords) {
	    ArrayList<String> allMatched = new ArrayList<String>();
	    Iterator<String> e = stWords.iterator();
            while (e.hasNext()) {
		allMatched.addAll(getMatchedKeywords(keywords, e.next(), allMatched));
            }
	    return allMatched;
	}

	private ArrayList<String> getMatchedKeywords(ArrayList<String> keywords, String stWord, ArrayList<String> allMatched) {
	    ArrayList<String> matched = new ArrayList<String>();
	    Iterator<String> e = keywords.iterator();
            while (e.hasNext()) {
		String next = e.next();
		for (String s : next.split(",")) {
		    s = s.trim();
		    if (s.contains(stWord) && !allMatched.contains(s) && !matched.contains(s)) {
			matched.add(s);
		    }
		}
            }
	    return matched;
	}
        
        private void handleSearch(String searchText) {
            List<String> stWords = Arrays.asList(searchText.toUpperCase().split(" "));
            String exactCategory = null;
	    String exactTabTitle = null;
            int exactTabIndex = -1;
            for (String id : categoryModel.getCategoryIDs()) {
		exactTabIndex = -1;
                ArrayList<String> entry = categoryid2words.get(id);
		List<String> matchedKeywords;
                if (entry != null) {
                    boolean found = containsAllSearchWords(entry, stWords);
                    for (String stWord : stWords) {
                        if (id.toUpperCase().contains(stWord)) {
                            exactCategory = id;
                        }
                    }

                    if (found) {
                        disabledCategories.remove(id);
                        buttons.get(id).setEnabled(true);
                        JTabbedPane pane = categoryid2tabbedpane.get(id);
                        if (categoryid2tabs.get(id) != null) {
                            HashMap<Integer, TabInfo> tabsInfo = categoryid2tabs.get(id);
                            boolean foundInNoTab = true;
                            for (Integer tabIndex : tabsInfo.keySet()) {
                                if (tabIndex != -1) {
                                    ArrayList<String> tabWords = tabsInfo.get(tabIndex).getWords();
                                    boolean foundInTab = false;
                                    if (containsAllSearchWords(tabWords, stWords)) {
                                        foundInTab = true;
                                        foundInNoTab = false;
                                        exactTabIndex = tabIndex;
					if ((exactCategory == null && exactTabTitle == null) || (exactCategory != null && exactCategory.equals(id))) {
					    setCurrentCategory(categoryModel.getCategory(id), null);
					}
                                    }
                                    if (foundInTab) {
					for (String stWord : stWords) {
					    if (pane.getTitleAt(tabIndex).toUpperCase().contains(stWord)) {
						exactTabTitle = pane.getTitleAt(tabIndex);
					    }
					}
                                        pane.setEnabledAt(tabIndex, true);
                                        if (exactTabIndex == tabIndex
						&& (exactTabTitle == null || (exactTabTitle != null && pane.getTitleAt(tabIndex).equals(exactTabTitle)))) {
                                            pane.setSelectedIndex(tabIndex);
                                            setCurrentCategory(categoryModel.getCategory(id), null);
                                        }
					matchedKeywords = getAllMatchedKeywords(tabWords, stWords);
					categoryModel.getCurrent().handleSuccessfulSearchInController(searchText, matchedKeywords);
                                    } else {
                                        pane.setEnabledAt(tabIndex, false);
                                        if(exactTabIndex == -1) {
                                            pane.setSelectedIndex(getNextEnabledTabIndex(pane, tabIndex));
                                        }
                                    }
                                } else {
				    if ((exactCategory == null && exactTabTitle == null) || (exactCategory != null && exactCategory.equals(id))) {
					setCurrentCategory(categoryModel.getCategory(id), null);
				    }
                                    if(tabsInfo.size() == 1) {
                                        foundInNoTab = false;
					matchedKeywords = getAllMatchedKeywords(entry, stWords);
					categoryModel.getCurrent().handleSuccessfulSearchInController(searchText, matchedKeywords);
                                    }
                                }
                            }
                            // above we tried to find an exact match and were conservative about selecting a specific tab, so if
                            // the search term is found and no tab is yet selected make sure at least last one found gets selected
                            if (!foundInNoTab && pane != null && pane.getSelectedIndex() == -1) {
                                pane.setSelectedIndex(exactTabIndex);
                            }
                            if(foundInNoTab) {
                                handleNotFound(id, exactCategory, exactTabTitle);
                            }
                        } else {
			    if ((exactCategory == null && exactTabTitle == null) || (exactCategory != null && exactCategory.equals(id))) {
				setCurrentCategory(categoryModel.getCategory(id), null);
			    }
			    matchedKeywords = getAllMatchedKeywords(entry, stWords);
			    categoryModel.getCurrent().handleSuccessfulSearchInController(searchText, matchedKeywords);
                        }
                    } else {
                        handleNotFound(id, exactCategory, exactTabTitle);
                    }
                } else {
                    handleNotFound(id, exactCategory, exactTabTitle);
                }
            }
        }

        private void handleNotFound(String id, String exactCategory, String exactTabTitle) {
            if (!disabledCategories.contains(id)) {
                disabledCategories.add(id);
            }
            JTabbedPane pane = categoryid2tabbedpane.get(id);
            if (categoryid2tabs.get(id) != null && pane != null) {
                for (int i = 0; i < pane.getTabCount(); i++) {
                    pane.setEnabledAt(i, false);
                }
            }
            buttons.get(id).setEnabled(false);
            if (disabledCategories.size() == buttons.size()) {
                setCurrentCategory(null, null);
            } else {
                for (String id3 : categoryModel.getCategoryIDs()) {
                    if (buttons.get(id3).isEnabled() && ((exactCategory != null && exactCategory.equals(id3)) || (exactCategory == null && exactTabTitle == null))) {
                        setCurrentCategory(categoryModel.getCategory(id3), null);
                        break;
                    }
                }
            }
        }

        @Override
        public void showNextSelection(boolean forward) {
        }

        @Override
        public String findMaxPrefix(String prefix) {
            return prefix;
        }

        @Override
        public void quickSearchConfirmed() {
            if (text2search.length() == 0) {
                clearAllinQS();
                showHint(true);
                return;
            }
            showWaitCursor();
            try {
                if (!initialized) {
                    final String sText = text2search;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            computeOptionsWords();
                            initialized = true;
                            handleSearch(sText);
                            showHint(false);
                        }
                    });
                } else {
                    handleSearch(text2search);
                    showHint(false);
                }
            } finally {
                hideWaitCursor();
            }
        }

        @Override
        public void quickSearchCanceled() {
            if(!text2search.trim().isEmpty()) {
                // we got a call from GuickSearch.SearchFieldListener.searchForNode(),
                // so call quickSearchUpdate ourselves.
                quickSearchUpdate("");
                return;
            }
            clearAllinQS();
            if (searchTC.hasFocus()) {
                showHint(false);
            } else {
                // Show the hint in the next EQ round to prevent from mutation in notification
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        showHint(true);
                    }
                });
            }
        }
    }

    private void clearAllinQS() {
	clearSearch = true;
	for (String id : categoryModel.getCategoryIDs()) {
	    JTabbedPane pane = categoryid2tabbedpane.get(id);
	    if (categoryid2tabs.get(id) != null && pane != null) {
		for (int i = 0; i < pane.getTabCount(); i++) {
		    pane.setEnabledAt(i, true);
		}
	    }
	    buttons.get(id).setEnabled(true);
	}
	setCurrentCategory(categoryModel.getCurrent(), null);
	disabledCategories.clear();
	categoryModel.getCurrent().handleSuccessfulSearchInController(null, null);
    }
    
    private void initActions () {
        if (getActionMap ().get("PREVIOUS") == null) {//NOI18N
            InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            
            inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "PREVIOUS");//NOI18N
            getActionMap ().put ("PREVIOUS", new PreviousAction ());//NOI18N
            
            inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK),"NEXT");//NOI18N
            getActionMap ().put ("NEXT", new NextAction ());//NOI18N
        }
    }
    
    private void addCategoryButtons () {
        // remove old buttons
        Iterator<CategoryButton> it = buttons.values().iterator ();
        while (it.hasNext ()) {
            removeButton(it.next());
        }
        pCategories2.removeAll ();
        buttons = new LinkedHashMap<String, CategoryButton>();
        
        // add new buttons
        String[] names = categoryModel.getCategoryIDs();
        for (int i = 0; i < names.length; i++) {
            CategoryModel.Category category = categoryModel.getCategory(names[i]);
            addButton (category);
        }
        
        addFakeButton ();
    }
                
    private CategoryButton addButton (CategoryModel.Category category) {
        int index = buttons.size ();
        CategoryButton button = isNimbus || isGTK 
                ? new NimbusCategoryButton(category)
                : new CategoryButton(category);

        // add shortcut
	if (!isMac) {
	    KeyStroke keyStroke = KeyStroke.getKeyStroke(button.getDisplayedMnemonic(), KeyEvent.ALT_MASK);
	    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, button);
	    getActionMap().put(button, new SelectAction(category));
	}

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridx = index;
        gbc.gridy = 0;
        pCategories2.add(button, gbc);
        buttons.put (category.getID(), button);
        return button;
    }
    
    private void removeButton (CategoryButton button) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke 
            (button.getDisplayedMnemonic (), KeyEvent.ALT_MASK);
        getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW).remove (keyStroke);
        getActionMap ().remove (button);
    }
    
    private void addFakeButton () {
        /* i don't know a better workaround */
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy = 0;
        pCategories2.add (new javax.swing.JLabel (""), gbc);
    }
        
    private Dimension getInitSize() {
        //if necessary init size could be chosen for individual resolutions differently
        //DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();         
        return new Dimension(750, 500);
    }
    
    Dimension getUserSize() {
        int w = NbPreferences.forModule(OptionsPanel.class).getInt("OptionsWidth",getInitSize().width);//NOI18N
        int h = NbPreferences.forModule(OptionsPanel.class).getInt("OptionsHeight",getInitSize().height);//NOI18N
        return new Dimension (w, h);
    }

    @Override
    public Dimension getPreferredSize() {
        //#108865 Scrollbars appear on Options dialog - preferredSize mustn't exceed screenBounds.? - 100 
        //else NbPresenter will show up scrollbars                            
        Dimension d = super.getPreferredSize();
        final Rectangle screenBounds = Utilities.getUsableScreenBounds();
        return new Dimension(Math.min(d.width, screenBounds.width - 101), Math.min(d.height, screenBounds.height - 101));
    }
        
    void storeUserSize() {
        Dimension d = pOptions.getSize();
        NbPreferences.forModule(OptionsPanel.class).putInt("OptionsWidth",d.width);//NOI18N
        NbPreferences.forModule(OptionsPanel.class).putInt("OptionsHeight",d.height);//NOI18N
        pOptions.setPreferredSize(d);
    }
    
    private boolean checkSize(Dimension componentSize) {
        boolean retval = false;
        Dimension prefSize = pOptions.getPreferredSize();
        Dimension userSize = getUserSize();
        componentSize = new Dimension(Math.max(componentSize.width, userSize.width),Math.max(componentSize.height, userSize.height));
        if (prefSize.width < componentSize.width || prefSize.height < componentSize.height) {
            Dimension newSize = new Dimension(Math.max(prefSize.width, componentSize.width),Math.max(prefSize.height, componentSize.height));
            pOptions.setPreferredSize(newSize);
            Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, this);
            invalidate();
            if (w != null) {
                w.pack();
            }
            retval = true;            
        }        
        return retval;
    }

    private Color getTabPanelBackground() {
        if( useUIDefaultsColors() ) {
            Color res = UIManager.getColor( "Tree.background" ); //NOI18N
            if( null == res )
                res = Color.white;
            return new Color( res.getRGB() );
        }
        return Color.white;
    }

    private Color getTabPanelForeground() {
        if( useUIDefaultsColors() ) {
            Color res = UIManager.getColor( "Tree.foreground" ); //NOI18N
            if( null == res )
                res = Color.black;
            return new Color( res.getRGB() );
        }
        return Color.black;
    }

    private Color getSelectionBackground() {
        if( useUIDefaultsColors() ) {
            if( !Color.white.equals( getTabPanelBackground() ) ) {
                Color res = UIManager.getColor( "Tree.selectionBackground" ); //NOI18N
                if( null == res )
                    res = Color.blue;
                return new Color( res.getRGB() );
            }
        }
        return new Color (193, 210, 238);
    }

    private Color getHighlightBackground() {
        if( useUIDefaultsColors() ) {
            if( !Color.white.equals( getTabPanelBackground() ) ) {
                Color res = UIManager.getColor( "Tree.selectionBackground" ); //NOI18N
                if( null == res )
                    res = Color.blue;
                return new Color( res.getRGB() );
            }
        }
        return new Color (224, 232, 246);
    }

    private boolean useUIDefaultsColors() {
        return isMetal || isNimbus || isFlatLaf;
    }

    // innerclasses ............................................................
    
    private class SelectAction extends AbstractAction {
        private CategoryModel.Category category;
        
        SelectAction (CategoryModel.Category category) {
            this.category = category;
        }
        public void actionPerformed (ActionEvent e) {
            setCurrentCategory (category, null);
        }
    }
        
    private class SelectCurrentAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            CategoryModel.Category highlightedB = categoryModel.getCategory(categoryModel.getHighlitedCategoryID());
            if (highlightedB != null) {
                setCurrentCategory(highlightedB, null);
            }
        }
    }
    
    private class SearchAction extends AbstractAction {
        @Override
        public void actionPerformed (ActionEvent e) {
            showHint(false);
            searchTC.requestFocusInWindow();
        }
    }
    
    private class PreviousAction extends AbstractAction {
        public void actionPerformed (ActionEvent e) {
            Category previous = categoryModel.getPreviousCategory();
            if(buttons.get(previous.getID()).isEnabled()) {
                setCurrentCategory (previous, null);
            } else {
                String currentID = categoryModel.getCurrentCategoryID();
                String[] ids = categoryModel.getCategoryIDs();
                int idx = Arrays.asList(ids).indexOf(currentID);
                if(idx - 1 > -1) {
                    if (doPreviousNextAction(ids, idx - 1, -1, false)) {
                        doPreviousNextAction(ids, ids.length - 1, idx, false);
                    }
                } else {
                    doPreviousNextAction(ids, ids.length - 1, idx, false);
                }
            }
        }
    }
    
    private class NextAction extends AbstractAction {
        public void actionPerformed (ActionEvent e) {            
            Category next = categoryModel.getNextCategory();
            if(buttons.get(next.getID()).isEnabled()) {
                setCurrentCategory (next, null);
            } else {
                String currentID = categoryModel.getCurrentCategoryID();
                String[] ids = categoryModel.getCategoryIDs();
                int idx = Arrays.asList(ids).indexOf(currentID);
                if(idx + 1 < ids.length) {
                    if(doPreviousNextAction(ids, idx + 1, ids.length, true)) {
                        doPreviousNextAction(ids, 0, idx, true);
                    }
                } else {
                    doPreviousNextAction(ids, 0, idx, true);
                }
            }
        }
    }

    private boolean doPreviousNextAction(String[] ids, int start, int end, boolean nextAction) {
        if (nextAction) {
            for (int i = start; i < end; i++) {
                String id = ids[i];
                if (buttons.get(id).isEnabled()) {
                    setCurrentCategory(categoryModel.getCategory(id), null);
                    return false;
                }
            }
        } else {
            for (int i = start; i > end; i--) {
                String id = ids[i];
                if (buttons.get(id).isEnabled()) {
                    setCurrentCategory(categoryModel.getCategory(id), null);
                    return false;
                }
            }
        }
        return true;
    }
    
    class ControllerListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            OptionsPanel.this.firePropertyChange 
                ("buran" + evt.getPropertyName (), null, null);
        }
    }

    @NbBundle.Messages({"# {0} - name of the category button",
        "CategoryButton_AccessibleDescription={0} category button. Use the arrow keys to move between top level categories."})
    class CategoryButton extends JLabel implements MouseListener {
        private final CategoryModel.Category category;                
        CategoryButton (final CategoryModel.Category category) {
            super (category.getIcon());
            this.category = category;
            Mnemonics.setLocalizedText (this, category.getCategoryName());
	    getAccessibleContext().setAccessibleName(category.getCategoryName());
	    getAccessibleContext().setAccessibleDescription(Bundle.CategoryButton_AccessibleDescription(category.getCategoryName()));
            setDisplayedMnemonic(0);            
            setOpaque (true);
            setVerticalTextPosition (BOTTOM);
            setHorizontalTextPosition (CENTER);
            setHorizontalAlignment (CENTER);
            addMouseListener (this);
            setFocusable (false);
            setFocusTraversalKeysEnabled (false);
            setForeground (getTabPanelForeground());
            
            if (isMac) {
                setFont(labelFontMac);
                setIconTextGap(2);
            }
            
            setNormal ();
        }
            
        void setNormal () {
            if (isMac) {
                setBorder (new EmptyBorder (5, 6, 3, 6));
            } else {
                setBorder (new EmptyBorder (2, 4, 2, 4));
            }
            setBackground (getTabPanelBackground());
        }
        
        void setSelected () {
            if (isMac) {
                setBorder(new CompoundBorder (
                        new VariableBorder(null, selectedB, null, selectedB),
                        BorderFactory.createEmptyBorder(5, 5, 3, 5)
                        ));
            } else {
                setBorder (new CompoundBorder (
                    new CompoundBorder (
                        new LineBorder (getTabPanelBackground()),
                        new LineBorder (selectedB)
                    ),
                    new EmptyBorder (0, 2, 0, 2)
                ));
            }
            setBackground (selected);            
        }
        
        void setHighlighted() {
            if (!isMac) {
                setBorder(new CompoundBorder(
                        new CompoundBorder(
                        new LineBorder(getTabPanelBackground()),
                        new LineBorder(highlightedB)
                        ),
                        new EmptyBorder(0, 2, 0, 2)
                        ));
                setBackground(highlighted);
            }
            if (!category.isHighlited()) {
                if (categoryModel.getHighlitedCategoryID() != null) {
                    CategoryButton b = buttons.get(categoryModel.getHighlitedCategoryID());
                    if (b != null && !b.category.isCurrent()) {
                        b.setNormal();
                    }
                }
                categoryModel.setHighlited(category,true);
            }
        }
        
        public void mouseClicked (MouseEvent e) {            
        }

        public void mousePressed (MouseEvent e) {
            if (buttons.get(category.getID()).isEnabled() && !isMac && categoryModel.getCurrent() != null) {
                setSelected ();
            }
        }

        public void mouseReleased (MouseEvent e) {
            if (buttons.get(category.getID()).isEnabled() && !category.isCurrent() && category.isHighlited() && categoryModel.getCurrent() != null) {
                setCurrentCategory(category, null);
            }
        }

        public void mouseEntered (MouseEvent e) {
            CategoryButton button = buttons.get(category.getID());
            if (button != null && button.isEnabled() && !category.isCurrent() && categoryModel != null && categoryModel.getCurrent() != null) {
                setHighlighted();
            } else if (categoryModel != null) {
                categoryModel.setHighlited(categoryModel.getCategory(categoryModel.getHighlitedCategoryID()), false);
            }
        }

        public void mouseExited (MouseEvent e) {
            if (!category.isCurrent() && !isMac && categoryModel.getCurrent() != null) {
                setNormal ();
            }
        }
    }


    private static final int BORDER_WIDTH = 4;
    private static final Border selBorder = new CompoundBorder( 
            new CompoundBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH),
                new NimbusBorder() ),
                BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
    private static final Border normalBorder = BorderFactory.createEmptyBorder(2*BORDER_WIDTH+1, 2*BORDER_WIDTH+1, 2*BORDER_WIDTH+3, 2*BORDER_WIDTH+3);

    private static final short STATUS_NORMAL = 0;
    private static final short STATUS_SELECTED = 1;
    private static final short STATUS_HIGHLIGHTED = 2;

    private static final Color COL_GRADIENT1 = new Color(244,245,249);
    private static final Color COL_GRADIENT2 = new Color(163,184,203);
    private static final Color COL_GRADIENT3 = new Color(206,227,246);

    private static final Color COL_OVER_GRADIENT1 = new Color(244,245,249,128);
    private static final Color COL_OVER_GRADIENT2 = new Color(163,184,203,128);
    private static final Color COL_OVER_GRADIENT3 = new Color(206,227,246,128);

    private final boolean isDefaultTabBackground = Color.white.equals( getTabPanelBackground() );

    private class NimbusCategoryButton extends CategoryButton {

        private short status = STATUS_NORMAL;

        public NimbusCategoryButton( final CategoryModel.Category category ) {
            super( category );
            setOpaque(false);
            setBorder( normalBorder );
        }

        @Override
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if( isDefaultTabBackground && (status == STATUS_SELECTED || status == STATUS_HIGHLIGHTED) ) {
                Insets in = getInsets();
                in.top -= BORDER_WIDTH;
                in.left -= BORDER_WIDTH;
                in.bottom -= BORDER_WIDTH;
                in.right -= BORDER_WIDTH;
                Graphics2D g2d = (Graphics2D) g.create();

                int width = getWidth()-in.left-in.right+1;
                int height = getHeight()-in.top-in.bottom+1;
                int topGradient = (int)(0.7*height);
                int bottomGradient = height-topGradient;
                Color c1 = (status == STATUS_HIGHLIGHTED ? COL_OVER_GRADIENT1 : COL_GRADIENT1);
                Color c2 = (status == STATUS_HIGHLIGHTED ? COL_OVER_GRADIENT2 : COL_GRADIENT2);
                Color c3 = (status == STATUS_HIGHLIGHTED ? COL_OVER_GRADIENT3 : COL_GRADIENT3);
                g2d.setPaint( new GradientPaint(in.left, in.top, c1, in.left, in.top+topGradient, c2));
                g2d.fillRect(in.left,in.top, width, topGradient );

                g2d.setPaint( new GradientPaint(in.left, in.top+topGradient, c2, in.left, in.top+topGradient+bottomGradient, c3));
                g2d.fillRect(in.left,in.top+topGradient, width, bottomGradient  );

                g2d.dispose();
            }
            super.paintComponent(g);
        }

        @Override
        void setHighlighted() {
            super.setHighlighted();
            status = STATUS_HIGHLIGHTED;
            setBorder(selBorder);
            repaint();
        }


        @Override
        void setNormal() {
            setBorder(normalBorder);
            status = STATUS_NORMAL;
            repaint();
        }

        @Override
        void setSelected() {
            setBorder(selBorder);
            status = STATUS_SELECTED;
            repaint();
        }
    }

    private static class NimbusBorder implements Border {

        private static final Color COLOR_BORDER = new Color(72,93,112, 255);
        private static final Color COLOR_SHADOW1 = new Color(72,93,112, 100);
        private static final Color COLOR_SHADOW2 = new Color(72,93,112, 60) ;

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D)g;

            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            Area rect = new Area(new RoundRectangle2D.Float(x, y, width-3, height-2, 4, 4));
            g2d.setColor( COLOR_BORDER );
            g2d.draw( rect );

            Area shadow = new Area( rect );
            AffineTransform tx = new AffineTransform();
            tx.translate(1, 1);
            shadow.transform(tx);
            shadow.subtract(rect);
            g2d.setColor( COLOR_SHADOW1 );
            g2d.draw( shadow );

            shadow = new Area( rect );
            tx = new AffineTransform();
            tx.translate(2, 2);
            shadow.transform(tx);
            shadow.subtract(rect);
            g2d.setColor( COLOR_SHADOW2 );
            g2d.draw( shadow );
        }

        public Insets getBorderInsets(Component c) {
            return new Insets( 1,1,3,3 );
        }

        public boolean isBorderOpaque() {
            return false;
        }
    }
}
