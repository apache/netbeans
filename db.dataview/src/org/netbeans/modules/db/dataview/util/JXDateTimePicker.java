/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.

The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):
 */
package org.netbeans.modules.db.dataview.util;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.EventListener;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.text.DefaultFormatterFactory;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.calendar.DatePickerFormatter;
import org.jdesktop.swingx.event.EventListenerMap;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.plaf.DatePickerAddon;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.util.Contract;

/**
 * A component for entering dates with a user interaction similar to a
 * JComboBox. The dates can be typed into a text field or selected from a
 * JXMonthView which opens in a JXPopupMenu on user's request.
 * <p>
 *
 * The date selection is controlled by the JXMonthView's DateSelectionModel.
 * This allows the use of all its functionality in the JXDateTimePicker as well.
 * F.i. restrict the selection to a date in the current or next week:
 * <p>
 * 
 * <pre><code>
 * Appointment appointment = new Appointment(director,
 *         &quot;Be sure to have polished shoes!&quot;);
 * JXDateTimePicker picker = new JXDateTimePicker();
 * Calendar calendar = picker.getMonthView().getCalendar();
 * // starting today if we are in a hurry
 * calendar.setTime(new Date());
 * picker.getMonthView().setLowerBound(calendar.getTime());
 * // end of next week
 * CalendarUtils.endOfWeek(calendar);
 * calendar.add(Calendar.WEEK_OF_YEAR);
 * picker.getMonthView().setUpperBound(calendar.getTime());
 * </code></pre>
 * 
 * Similar to a JXMonthView, the JXDateTimePicker fires an ActionEvent when the user
 * actively commits or cancels a selection. Interested client code can add a
 * ActionListener to be notified by the user action.
 * 
 * <pre><code>
 * JXDateTimePicker picker = new JXDateTimePicker(new Date());
 * ActionListener l = new ActionListener() {
 *     public void actionPerformed(ActionEvent e) {
 *         if (JXDateTimePicker.COMMIT_KEY.equals(e.getActionCommand)) {
 *             saveDate(picker.getDate());
 *         }
 *     }
 * };
 * picker.addActionListener(l);
 * </code></pre>
 * 
 * <p>
 * The DateFormats used in the JXDateTimePicker's are initialized to the default
 * formats of the DatePickerFormatter, as defined by the picker's resourceBundle
 * DatePicker.properties. Application code can overwrite the picker's default
 * 
 * <pre><code>
 * picker.setDateFormats(myCustomFormat, myAlternativeCustomFormat);
 * </code></pre>
 * 
 * PENDING JW: explain what the alternatives are for (after understanding it
 * myself ;-)
 * <p>
 * 
 * The selected Date is a bound property of the JXDateTimePicker. This allows easy
 * binding to a property of a custom bean when using a binding framework.
 * <p>
 * 
 * @author Joshua Outwater
 * @author Jeanette Winzenburg
 * 
 * @see JXMonthView
 * @see org.jdesktop.swingx.calendar.DateSelectionModel
 * @see DatePickerFormatter
 * 
 */
public class JXDateTimePicker extends JComponent {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(JXDateTimePicker.class
            .getName());
    static {
        LookAndFeelAddons.contribute(new DatePickerAddon());
    }

    /**
     * UI Class ID
     */
    public static final String uiClassID = "BasicDateTimePickerUI";

    public static final String EDITOR = "editor";
    public static final String MONTH_VIEW = "monthView";
    /** @deprecated no longer used - all "millis" are replaced by date */ 
    @Deprecated
    public static final String DATE_IN_MILLIS = "dateInMillis";
    public static final String LINK_PANEL = "linkPanel";

    /** action command used for commit actionEvent. */
    public static final String COMMIT_KEY = "datePickerCommit";
    /** action command used for cancel actionEvent. */
    public static final String CANCEL_KEY = "datePickerCancel";
    /** action key for navigate home action */
    public static final String HOME_NAVIGATE_KEY = "navigateHome";
    /** action key for commit home action */
    public static final String HOME_COMMIT_KEY = "commitHome";

    private static final DateFormat[] EMPTY_DATE_FORMATS = new DateFormat[0];

    /**
     * The editable date field that displays the date
     */
    private JFormattedTextField _dateField;
    protected JFormattedTextField _timeField;

    /**
     * Popup that displays the month view with controls for
     * traversing/selecting dates.
     */
    private JPanel _linkPanel;
    private MessageFormat _linkFormat;
    private java.util.Date linkDate;
    
    private JXMonthView _monthView;
    private boolean editable = true;
    private EventListenerMap listenerMap;
    protected boolean lightWeightPopupEnabled = JPopupMenu.getDefaultLightWeightPopupEnabled();

    private Timestamp date;

    private PropertyChangeListener monthViewListener;


    /**
     * Intantiates a date picker with no selection and the default 
     * <code>DatePickerFormatter</code>.
     * <p/>
     * The date picker is configured with the default time zone and locale
     *
     * @see #setTimeZone
     * @see #getTimeZone
     */
    public JXDateTimePicker() {
        this(null, null);
    }

    

    /**
     * Intantiates a date picker using the specified time as the initial
     * selection and the default 
     * <code>DatePickerFormatter</code>.
     * <p/>
     * The date picker is configured with the default time zone and locale
     *
     * @param selected the initially selected date
     * @see #setTimeZone
     * @see #getTimeZone
     */
    public JXDateTimePicker(Timestamp selected) {
        this(selected, null);
    }
    
    /**
     * Intantiates a date picker with no selection and the default 
     * <code>DatePickerFormatter</code>.
     * <p/>
     * The date picker is configured with the default time zone and specified 
     * locale
     *
     * @param locale    initial Locale
     * @see #setTimeZone
     * @see #getTimeZone
     */
    public JXDateTimePicker(Locale locale) {
        this(null, locale);
    }

    /**
     * Intantiates a date picker using the specified time as the initial
     * selection and the default 
     * <code>DatePickerFormatter</code>.
     * <p/>
     * The date picker is configured with the default time zone and specified locale
     *
     * @param selection initially selected Date
     * @param locale initial Locale
     * @see #setTimeZone
     * @see #getTimeZone
     * 
     */
    public JXDateTimePicker(Timestamp selection, Locale locale) {
        init();
        if (locale != null) {
            setLocale(locale);
        }
        // install the controller before setting the date
        updateUI();
        setDateTime(selection);
    }

    /**
     * Sets the date property. <p>
     * 
     * Does nothing if the ui vetos the new date - as might happen if
     * the code tries to set a date which is unselectable in the 
     * monthView's context. The actual value of the new Date is controlled
     * by the JXMonthView's DateSelectionModel. The default implementation
     * normalizes the date to the start of the day in the model's calendar's 
     * coordinates, that is all time fields are zeroed. To keep the time fields,
     * configure the monthView with a SingleDaySelectionModel.
     * <p>
     * 
     * At all "stable" (= not editing in date input field nor 
     * in the monthView) times the date is the same in the 
     * JXMonthView, this JXDateTimePicker and the editor. If a new Date
     * is set, this invariant is enforced by the DatePickerUI.
     * <p>
     * 
     * This is a bound property. 
     * 
     *  
     * @param date the new date to set.
     * @see #getDate()
     * @see org.jdesktop.swingx.calendar.DateSelectionModel
     * @see org.jdesktop.swingx.calendar.SingleDaySelectionModel
     */
    public void setDateTime(Timestamp date) {
        if(date == null) {
            return;
        }
        /*
         * JW: 
         * this is a poor woman's constraint property.
         * Introduces explicit coupling to the ui. 
         * Which is unusual at this place in code.
         * 
         * If needed the date can be made a formal
         * constraint property and let the ui add a
         * VetoablePropertyListener.
         */
        try {
            date = getUI().getSelectableDate(date);
        } catch (PropertyVetoException e) {
            return;
        }
        
        Timestamp old = getDateTime();
        this.date = date;
        firePropertyChange("date", old, getDateTime());
    }

    protected boolean isSameDate(Calendar cal1, Calendar cal2) {
        Comparator<Calendar> c = new SelectedValuesComparator();
        if (c.compare(cal1, cal2) == 0) {
            return true;
        }
        return false;
    }

    protected class SelectedValuesComparator implements Comparator<Calendar> {
        @Override
        public int compare(Calendar cal1, Calendar cal2) {

            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE)) {
                return 0;
            }
            if (cal1.after(cal2)) {
                return 1;
            } else {
                return -1;
            }
        }
    }


    /**
     * Returns the currently selected date.
     *
     * @return Date
     */
    public Timestamp getDateTime() {
        return date; 
    }
    
    /**
     * 
     */
    private void init() {
        listenerMap = new EventListenerMap();
        initMonthView();

        updateLinkFormat();
        linkDate = _monthView.getToday();
        _linkPanel = new TodayPanel();
    }

    private void initMonthView() {
        _monthView = new JXMonthView();
//        _monthView.setSelectionModel(new SingleDaySelectionModel());
        _monthView.setTraversable(true);

        _monthView.setBackground(ColorHelper.getJxdatetimepickerBackground());
        _monthView.setForeground(ColorHelper.getJxdatetimepickerForeground());
        _monthView.setSelectionBackground(ColorHelper.getJxdatetimepickerSelectedBackground()); //NOI18N
        _monthView.setSelectionForeground(ColorHelper.getJxdatetimepickerSelectedForeground()); //NOI18N
        _monthView.setMonthStringBackground(ColorHelper.getJxdatetimepickerMonthStringBackground()); //NOI18N
        _monthView.setMonthStringForeground(ColorHelper.getJxdatetimepickerMonthStringForeground()); //NOI18N
        _monthView.setDaysOfTheWeekForeground(ColorHelper.getJxdatetimepickerDaysOfTheWeekForeground()); //NOI18N
        _monthView.setTodayBackground(ColorHelper.getJxdatetimepickerTodayBackground()); //NOI18N

        _monthView.addPropertyChangeListener(getMonthViewListener());
    }

    /**
     * Lazily creates and returns the PropertyChangeListener which listens
     * for model's calendar properties.
     * 
     * @return a PropertyChangeListener for monthView's property change notification.
     */
    private PropertyChangeListener getMonthViewListener() {
        if (monthViewListener == null) {
            monthViewListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("timeZone".equals(evt.getPropertyName())) {
                        updateTimeZone((TimeZone) evt.getOldValue(), (TimeZone) evt.getNewValue());
                    }
                    
                }
                
            };
        }
        return monthViewListener;
    }

    /**
     * Callback from monthView timezone changes. <p>
     * 
     * NOTE: as timeZone is a bound property of this class we need to 
     * guarantee the propertyChangeNotification. As this class doesn't 
     * own this property it must listen to the owner (monthView) and 
     * re-fire the change.
     * 
     * @param oldValue the old timezone.
     * @param newValue the new timezone.
     */
    protected void updateTimeZone(TimeZone oldValue, TimeZone newValue) {
        firePropertyChange("timeZone", oldValue, newValue);
    }

    /**
     * Returns the look and feel (L&F) object that renders this component.
     *
     * @return the DatePickerUI object that renders this component
     */
    public BasicDateTimePickerUI getUI() {
        return (BasicDateTimePickerUI) ui;
    }

    /**
     * Sets the L&F object that renders this component.
     *
     * @param ui UI to use for this {@code JXDateTimePicker}
     */
    public void setUI(BasicDateTimePickerUI ui) {
        super.setUI(ui);
    }

    /**
     * Resets the UI property with the value from the current look and feel.
     *
     * @see UIManager#getUI
     */
    @Override
    public void updateUI() {
        setUI(new BasicDateTimePickerUI());
        // JW: quick hack around #706-swingx - monthView not updated
        // not sure if this here is the correct place nor 
        // the correct method - SwingUtilities.updateComponentTree might be better
        getMonthView().updateUI();
        invalidate();
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * Replaces the currently installed formatter and factory used by the
     * editor. These string formats are defined by the
     * <code>java.text.SimpleDateFormat</code> class. <p>
     * 
     * Note: The given formats are internally synched to the picker's current
     *    TimeZone. 
     * 
     * @param formats zero or more not null string formats to use. Note that a 
     *    null array is allowed and resets the formatter to use the 
     *    localized default formats.
     * @throws NullPointerException any array element is null.
     * @see java.text.SimpleDateFormat
     */
    public void setFormats(String... formats) {
        DateFormat[] dateFormats = null;
        if (formats !=  null) {
            Contract.asNotNull(formats,
                    "the array of format strings must not "
                            + "must not contain null elements");
            dateFormats = new DateFormat[formats.length];
            for (int counter = formats.length - 1; counter >= 0; counter--) {
                dateFormats[counter] = new SimpleDateFormat(formats[counter], getLocale());
            }
        }
        setFormats(dateFormats);
    }

    /**
     * Replaces the currently installed formatter and factory used by the
     * editor.<p>
     *
     * Note: The given formats are internally synched to the picker's current
     *    TimeZone. 
     * 
     * @param formats zero or more not null formats to use. Note that a 
     *    null array is allowed and resets the formatter to use the 
     *    localized default formats.
     * @throws NullPointerException any of its elements is null.
     */
    public void setFormats(DateFormat... formats) {
        if (formats != null) {
            Contract.asNotNull(formats, "the array of formats " + "must not contain null elements");
        }
        
        DateFormat[] old = getFormats();
        _dateField.setFormatterFactory(new DefaultFormatterFactory(
                new DatePickerFormatter(formats, getLocale())));
        firePropertyChange("formats", old, getFormats());
    }

    /**
     * Returns an array of the formats used by the installed formatter
     * if it is a subclass of <code>JXDatePickerFormatter<code>.
     * <code>javax.swing.JFormattedTextField.AbstractFormatter</code>
     * and <code>javax.swing.text.DefaultFormatter</code> do not have
     * support for accessing the formats used.
     *
     * @return array of formats guaranteed to be not null, but might be empty.
     */
    public DateFormat[] getFormats() {
        // Dig this out from the factory, if possible, otherwise return null.
        AbstractFormatterFactory factory = _dateField.getFormatterFactory();
        if (factory != null) {
            AbstractFormatter formatter = factory.getFormatter(_dateField);
            if (formatter instanceof DatePickerFormatter) {
                return ((DatePickerFormatter) formatter).getFormats();
            }
        }
        return EMPTY_DATE_FORMATS;
    }

    /**
     * Return the <code>JXMonthView</code> used in the popup to
     * select dates from.
     *
     * @return the month view component
     */
    public JXMonthView getMonthView() {
        return _monthView;
    }

    /**
     * Set the component to use the specified JXMonthView.  If the new JXMonthView
     * is configured to a different time zone it will affect the time zone of this
     * component.
     *
     * @param monthView month view comopnent.
     * @throws NullPointerException if view component is null
     * 
     * @see #setTimeZone
     * @see #getTimeZone
     */
    public void setMonthView(JXMonthView monthView) {
        Contract.asNotNull(monthView, "monthView must not be null");
        JXMonthView oldMonthView = getMonthView();
        TimeZone oldTZ = getTimeZone();
        oldMonthView.removePropertyChangeListener(getMonthViewListener());
        _monthView = monthView;
        getMonthView().addPropertyChangeListener(getMonthViewListener());
        firePropertyChange(MONTH_VIEW, oldMonthView, getMonthView());
        firePropertyChange("timeZone", oldTZ, getTimeZone());
    }

    /**
     * Gets the time zone.  This is a convenience method which returns the time zone
     * of the JXMonthView being used.
     *
     * @return The <code>TimeZone</code> used by the <code>JXMonthView</code>.
     */
    public TimeZone getTimeZone() {
        return _monthView.getTimeZone();
    }

    /**
     * Sets the time zone with the given time zone value.    This is a convenience
     * method which returns the time zone of the JXMonthView being used.<p>
     * 
     * PENDING JW: currently this property is the only property of the monthView 
     * which is exposed in this api. Not sure why it is here at all.
     * It's asymetric (to the other properties) and as such should be either removed
     * or the others which might be relevant to a datePicker exposed as well (probably 
     * hiding the monthView itself as an implementation detail of the ui delegate). 
     *
     * @param tz The <code>TimeZone</code>.
     */
    public void setTimeZone(TimeZone tz) {
        _monthView.setTimeZone(tz);
    }


    /**
     * Returns the date shown in the LinkPanel.
     * <p>
     * PENDING JW: the property should be named linkDate - but that's held by the 
     * deprecated long returning method. Maybe revisit if we actually remove the other.
     * 
     * @return the date shown in the LinkPanel.
     */
    public java.util.Date getLinkDay() {
        return linkDate;
    }

    /**
     * Set the date the link will use and the string defining a MessageFormat
     * to format the link.  If no valid date is in the editor when the popup
     * is displayed the popup will focus on the month the linkDate is in.  Calling
     * this method will replace the currently installed linkPanel and install
     * a new one with the requested date and format.
     * 
     * 
     * @param linkDay     the Date to set on the LinkPanel 
     * @param linkFormatString String used to format the link
     * @see java.text.MessageFormat
     */
    public void setLinkDay(java.util.Date linkDay, String linkFormatString) {
        setLinkFormat(new MessageFormat(linkFormatString));
        setLinkDay(linkDay);
    }
    
    /**
     * Sets the date shown in the TodayPanel. 
     * 
     * PENDING JW ... quick api hack for testing. Don't recreate the panel if
     * it had been used 
     * 
     * @param linkDay the date used in the TodayPanel
     */
    public void setLinkDay(java.util.Date linkDay) {
        this.linkDate = linkDay;
        Format[] formats = getLinkFormat().getFormatsByArgumentIndex();
        for (Format format : formats) {
            if (format instanceof DateFormat) {
                ((DateFormat) format).setTimeZone(getTimeZone());
            }
        }
        setLinkPanel(new TodayPanel());
    }
    

    /**
     * @param _linkFormat the _linkFormat to set
     */
    protected void setLinkFormat(MessageFormat _linkFormat) {
        this._linkFormat = _linkFormat;
    }

    /**
     * @return the _linkFormat
     */
    protected MessageFormat getLinkFormat() {
        return _linkFormat;
    }

    /**
     * Update text on the link panel.
     * 
     */
    private void updateLinkFormat() {
        // PENDING JW: move to ui
        String linkFormat = UIManagerExt.getString(
                "JXDatePicker.linkFormat", getLocale());
        
        if (linkFormat != null) {
            setLinkFormat(new MessageFormat(linkFormat));
        } else {
            setLinkFormat(new MessageFormat("{0,date, dd MMMM yyyy}"));
        }
    }

    /**
     * Return the panel that is used at the bottom of the popup.  The default
     * implementation shows a link that displays the current month.
     *
     * @return The currently installed link panel
     */
    public JPanel getLinkPanel() {
        return _linkPanel;
    }

    /**
     * Set the panel that will be used at the bottom of the popup.
     * PENDING JW: why insist on JPanel? JComponent would be enough?
     *  
     * @param linkPanel The new panel to install in the popup
     */
    public void setLinkPanel(JPanel linkPanel) {
        JPanel oldLinkPanel = _linkPanel;
        _linkPanel = linkPanel;
        firePropertyChange(LINK_PANEL, oldLinkPanel, _linkPanel);
    }

    /**
     * Returns the formatted text field used to edit the date selection.
     * <p>
     * Clients should NOT use this method. It is provided to temporarily support
     * the PLAF code.
     * 
     * @return the formatted text field
     */
//    @Deprecated
    public JFormattedTextField getEditor() {
        return _dateField;
    }

    /**
     * Sets the editor. <p>
     * 
     * The default is created and set by the UI delegate.
     * <p>
     * Clients should NOT use this method. It is provided to temporarily support
     * the PLAF code.
     * 
     * @param editor the formatted input.
     * @throws NullPointerException if editor is null.
     * 
     * @see #getEditor
     */
//    @Deprecated
    public void setEditor(JFormattedTextField editor) {
        Contract.asNotNull(editor, "editor must not be null");
        JFormattedTextField oldEditor = _dateField;
        _dateField = editor;
        firePropertyChange(EDITOR, oldEditor, _dateField);
    }

    @Override
    public void setComponentOrientation(ComponentOrientation orientation) {
        super.setComponentOrientation(orientation);
        _monthView.setComponentOrientation(orientation);
    }

    /**
     * Returns true if the current value being edited is valid.
     *
     * @return true if the current value being edited is valid.
     */
    public boolean isEditValid() {
        return _dateField.isEditValid();
    }

    /**
     * Commits the editor's changes and notifies ActionListeners.
     * 
     * Forces the current value to be taken from the AbstractFormatter and
     * set as the current value. This has no effect if there is no current
     * AbstractFormatter installed.
     *
     * @throws java.text.ParseException Throws parse exception if the date
     *                                  can not be parsed.
     */
    public void commitEdit() throws ParseException {
        try {
            _dateField.commitEdit();
            fireActionPerformed(COMMIT_KEY);
        } catch (ParseException e) {
            // re-throw
            throw e;
        } 
    }

    /**
     * Cancels the editor's changes and notifies ActionListeners.
     * 
     */
    public void cancelEdit() {
        // hmmm... no direct api?
         _dateField.setValue(_dateField.getValue());
         fireActionPerformed(CANCEL_KEY);
    }

    /**
     * Sets the editable property. If false, ...?
     * 
     * The default value is true.
     * 
     * @param value
     * @see #isEditable()
     */
    public void setEditable(boolean value) {
        boolean oldEditable = isEditable();
        editable = value;
        firePropertyChange("editable", oldEditable, editable);
        if (editable != oldEditable) {
            repaint();
        }
    }

    /**
     * Returns the editable property. 
     * 
     * @return {@code true} if the picker is editable; {@code false} otherwise
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Returns the font that is associated with the editor of this date picker.
     */
    @Override
    public Font getFont() {
        return getEditor().getFont();
    }

    /**
     * Set the font for the editor associated with this date picker.
     */
    @Override
    public void setFont(final Font font) {
        getEditor().setFont(font);
    }

    /**
     * Sets the <code>lightWeightPopupEnabled</code> property, which
     * provides a hint as to whether or not a lightweight
     * <code>Component</code> should be used to contain the
     * <code>JXDateTimePicker</code>, versus a heavyweight
     * <code>Component</code> such as a <code>Panel</code>
     * or a <code>Window</code>.  The decision of lightweight
     * versus heavyweight is ultimately up to the
     * <code>JXDateTimePicker</code>.  Lightweight windows are more
     * efficient than heavyweight windows, but lightweight
     * and heavyweight components do not mix well in a GUI.
     * If your application mixes lightweight and heavyweight
     * components, you should disable lightweight popups.
     * The default value for the <code>lightWeightPopupEnabled</code>
     * property is <code>true</code>, unless otherwise specified
     * by the look and feel.  Some look and feels always use
     * heavyweight popups, no matter what the value of this property.
     * <p/>
     * See the article <a href="http://java.sun.com/products/jfc/tsc/articles/mixing/index.html">Mixing Heavy and Light Components</a>
     * on <a href="http://java.sun.com/products/jfc/tsc">
     * <em>The Swing Connection</em></a>
     * This method fires a property changed event.
     *
     * @param aFlag if <code>true</code>, lightweight popups are desired
     * @beaninfo bound: true
     * expert: true
     * description: Set to <code>false</code> to require heavyweight popups.
     */
    public void setLightWeightPopupEnabled(boolean aFlag) {
        boolean oldFlag = lightWeightPopupEnabled;
        lightWeightPopupEnabled = aFlag;
        firePropertyChange("lightWeightPopupEnabled", oldFlag, lightWeightPopupEnabled);
    }

    /**
     * Gets the value of the <code>lightWeightPopupEnabled</code>
     * property.
     *
     * @return the value of the <code>lightWeightPopupEnabled</code>
     *         property
     * @see #setLightWeightPopupEnabled
     */
    public boolean isLightWeightPopupEnabled() {
        return lightWeightPopupEnabled;
    }

    /**
     * Get the baseline for the specified component, or a value less
     * than 0 if the baseline can not be determined.  The baseline is measured
     * from the top of the component.
     *
     * @param width  Width of the component to determine baseline for.
     * @param height Height of the component to determine baseline for.
     * @return baseline for the specified component
     */
    @Override
    public int getBaseline(int width, int height) {
        return ((BasicDateTimePickerUI) ui).getBaseline(width, height);
    }


    /**
     * Adds an ActionListener.
     * <p/>
     * The ActionListener will receive an ActionEvent when a selection has
     * been made.
     *
     * @param l The ActionListener that is to be notified
     */
    public void addActionListener(ActionListener l) {
        listenerMap.add(ActionListener.class, l);
    }

    /**
     * Removes an ActionListener.
     *
     * @param l The action listener to remove.
     */
    public void removeActionListener(ActionListener l) {
        listenerMap.remove(ActionListener.class, l);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        java.util.List<T> listeners = listenerMap.getListeners(listenerType);
        T[] result;
        if (!listeners.isEmpty()) {
            //noinspection unchecked
            result = (T[]) java.lang.reflect.Array.newInstance(listenerType, listeners.size());
            result = listeners.toArray(result);
        } else {
            result = super.getListeners(listenerType);
        }
        return result;
    }


    /**
     * Fires an ActionEvent with the given actionCommand
     * to all listeners.
     */
    protected void fireActionPerformed(String actionCommand) {
        ActionListener[] listeners = getListeners(ActionListener.class);
        ActionEvent e = null;

        for (ActionListener listener : listeners) {
            if (e == null) {
                e = new ActionEvent(this,
                        ActionEvent.ACTION_PERFORMED,
                        actionCommand);
            }
            listener.actionPerformed(e);
        }
    }
 
    /**
     * Pes: added setLocale method to refresh link text on locale changes
     */
    private final class TodayPanel extends JXPanel {
        private TodayAction todayAction;
        private JXHyperlink todayLink;

        @SuppressWarnings("rawtypes")
        TodayPanel() {
            super(new FlowLayout());
            Color gradientStart = ColorHelper.getJxdatetimepickerTodayPanelBackgroundGradientStart();
            Color gradientEnd = ColorHelper.getJxdatetimepickerTodayPanelBackgroundGradientEnd();
            setBackgroundPainter(new MattePainter(new GradientPaint(0, 0, gradientStart, 0, 1, gradientEnd)));
            todayAction = new TodayAction();
            todayLink = new JXHyperlink(todayAction);
            todayLink.addMouseListener(createDoubleClickListener());
            Color textColor = ColorHelper.getJxdatetimepickerTodayPanelLinkForeground();
            todayLink.setUnclickedColor(textColor);
            todayLink.setClickedColor(textColor);
            add(todayLink);
        }

        /**
         * @return
         */
        private MouseListener createDoubleClickListener() {
            MouseAdapter adapter = new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getClickCount() != 2) {
                        return;
                    }
                    todayAction.select = true;
                }
                
            };
            return adapter;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(187, 187, 187));
            g.drawLine(0, 0, getWidth(), 0);
            g.setColor(new Color(221, 221, 221));
            g.drawLine(0, 1, getWidth(), 1);
        }

        /**
         * {@inheritDoc} <p>
         *  Overridden to update the link format and hyperlink text.
         */
        @Override
        public void setLocale(Locale l) {
            super.setLocale(l);
            updateLinkFormat();
            todayLink.setText(getLinkFormat().format(new Object[]{getLinkDay()}));
        }
        
        private final class TodayAction extends AbstractAction {
            boolean select;
            TodayAction() {
                super(getLinkFormat().format(new Object[]{getLinkDay()}));
                Calendar cal = _monthView.getCalendar();
                cal.setTime(getLinkDay());
                putValue(NAME, getLinkFormat().format(new Object[] {cal.getTime()}));
            }

            @Override
            public void actionPerformed(ActionEvent ae) {
                String key = select ? JXDateTimePicker.HOME_COMMIT_KEY : JXDateTimePicker.HOME_NAVIGATE_KEY;
                select = false;
                Action delegate = getActionMap().get(key);
                /*
                 * PatrykRy: Commit today date only when commit action is enabled.
                 * Home navigate is always enabled.
                 */
                if (delegate !=  null && delegate.isEnabled()) {  
                    delegate.actionPerformed(null);
                }
                
            }
        }
    }


}    
 
