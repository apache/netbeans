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
package org.netbeans.modules.bugtracking.tasks;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.CharConversionException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.tasks.cache.DashboardStorage;
import org.netbeans.modules.bugtracking.tasks.cache.TaskEntry;
import org.netbeans.modules.bugtracking.tasks.dashboard.CategoryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.DashboardViewer;
import org.netbeans.modules.bugtracking.tasks.dashboard.RepositoryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.tasks.actions.Actions;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.SharedClassObject;
import org.openide.util.actions.Presenter;
import org.openide.xml.XMLUtil;

/**
 *
 * @author jpeska
 */
public class DashboardUtils {

    private static final int VISIBLE_START_CHARS = 5;
    private static final String BOLD_START_SUBSTITUTE = "$$$BOLD_START$$$"; //NOI18
    private static final String BOLD_END_SUBSTITUTE = "$$$BOLD_END$$$"; //NOI18
    private static final String NEW_COLOR = UIUtils.getColorString(UIUtils.getTaskNewColor());
    private static final String MODIFIED_COLOR = UIUtils.getColorString(UIUtils.getTaskModifiedColor());
    private static final String CONFLICT_COLOR = UIUtils.getColorString(UIUtils.getTaskConflictColor());

    private static final Image SCHEDULE_ICON = ImageUtilities.loadImage("org/netbeans/modules/bugtracking/tasks/resources/schedule.png", true); //NOI18
    private static final Image SCHEDULE_WARNING_ICON = ImageUtilities.loadImage("org/netbeans/modules/bugtracking/tasks/resources/schedule_alarm.png", true); //NOI18

    private static final int SCHEDULE_NOT_IN_SCHEDULE = 0;
    private static final int SCHEDULE_IN_SCHEDULE = 1;
    private static final int SCHEDULE_AFTER_DUE = 2;

    public static String getCategoryDisplayText(CategoryNode categoryNode) {
        String categoryName = categoryNode.getCategory().getName();
        boolean containsActiveTask = DashboardViewer.getInstance().containsActiveTask(categoryNode);
        return getTopLvlDisplayText(containsActiveTask, categoryName, categoryNode.isOpened());
    }

    public static String getRepositoryDisplayText(RepositoryNode repositoryNode) {
        String repositoryName = repositoryNode.getRepository().getDisplayName();
        boolean containsActiveTask = DashboardViewer.getInstance().containsActiveTask(repositoryNode);
        return getTopLvlDisplayText(containsActiveTask, repositoryName, repositoryNode.isOpened());
    }

    private static String getTopLvlDisplayText(boolean containsActiveTask, String name, boolean isOpened) {
        String displayName;
        try {
            name = XMLUtil.toElementContent(name);
        } catch (CharConversionException ex) {
        }
        String activeText = containsActiveTask ? "<b>" + name + "</b>" : name; //NOI18N
        if (!isOpened) {
            displayName = "<html><strike>" + activeText + "</strike><html>"; //NOI18N
        } else {
            displayName = "<html>" + activeText + "<html>";
        }
        return displayName;
    }

    public static String getTaskPlainDisplayText(IssueImpl task, JComponent component, int maxWidth) {
        return computeFitText(component, maxWidth, getTaskDisplayName(task), false);
    }

    public static String getTaskDisplayText(IssueImpl task, JComponent component, int maxWidth, boolean active, boolean hasFocus) {
        String fitText = computeFitText(component, maxWidth, getTaskDisplayName(task), active); //NOI18N

        boolean html = false;
        String text = getFilterBoldText(fitText);
        if (text.length() != fitText.length() || active || task.isFinished()) {
            text = escapeXmlChars(text);
        }
        if (text.length() != fitText.length()) {
            html = true;
        }
        if (active) {
            text = BOLD_START_SUBSTITUTE + fitText + BOLD_END_SUBSTITUTE;
            html = true;
        }
        text = replaceSubstitutes(text);

        if (task.isFinished()) {
            text = "<strike>" + text + "</strike>"; //NOI18N
            html = true;
        }
        return getTaskAnotatedText(text, task.getStatus(), hasFocus, html, task.isFinished());
    }

    public static String computeFitText(JComponent component, int maxWidth, String text, boolean bold) {
        if (text == null) {
            text = ""; // NOI18N
        }
        if (text.length() <= VISIBLE_START_CHARS + 3) {
            return text;
        }
        FontMetrics fm;
        if (bold) {
            fm = component.getFontMetrics(component.getFont().deriveFont(Font.BOLD));
        } else {
            fm = component.getFontMetrics(component.getFont());
        }
        int width = maxWidth;

        String sufix = "..."; // NOI18N
        int sufixLength = fm.stringWidth(sufix + " "); //NOI18N
        int desired = width - sufixLength;
        if (desired <= 0) {
            return text;
        }

        for (int i = 0; i <= text.length() - 1; i++) {
            String prefix = text.substring(0, i);
            int swidth = fm.stringWidth(prefix);
            if (swidth >= desired) {
                if (fm.stringWidth(text.substring(i + 1)) <= fm.stringWidth(sufix)) {
                    return text;
                }
                return prefix.length() > 0 ? prefix + sufix : text;
            }
        }
        return text;
    }

    public static String getTaskAnotatedText(IssueImpl task) {
        return getTaskAnotatedText(getTaskDisplayName(task), task.getStatus(), false, false, task.isFinished());
    }

    private static String getTaskAnotatedText(String text, IssueStatusProvider.Status status, boolean hasFocus, boolean isHTML, boolean isFinished) {
        if (status == IssueStatusProvider.Status.INCOMING_NEW && !hasFocus) {
            text = "<html><font color=\"" + NEW_COLOR + "\">" + text + "</font></html>"; //NOI18N
        } else if (status == IssueStatusProvider.Status.INCOMING_MODIFIED && !hasFocus) {
            text = "<html><font color=\"" + MODIFIED_COLOR + "\">" + text + "</font></html>"; //NOI18N
        } else if (status == IssueStatusProvider.Status.CONFLICT && !hasFocus) {
            text = "<html><font color=\"" + CONFLICT_COLOR + "\">" + text + "</font></html>"; //NOI18N
        } else if (isHTML) {
            text = "<html>" + text + "</html>"; //NOI18N
        }
        return text;
    }

    private static String escapeXmlChars(String text) {
        String result = text;
        try {
            result = XMLUtil.toElementContent(text);
        } catch (CharConversionException ex) {
        }
        return result;
    }

    static String getTaskDisplayName(IssueImpl task) {
        String displayName = task.getDisplayName();
        if (displayName.startsWith("#")) {
            displayName = displayName.replaceFirst("#", "");
        }
        return displayName;
    }

    private static String getFilterBoldText(String fitText) {
        String filterText = FilterPanel.getInstance().getFilterText();
        if (!filterText.equals("")) { //NOI18N
            int searchIndex = 0;
            StringBuilder sb = new StringBuilder(fitText);

            int index = sb.toString().toLowerCase().indexOf(filterText.toLowerCase(), searchIndex);
            while (index != -1) {
                sb.insert(index, BOLD_START_SUBSTITUTE);
                index = index + BOLD_START_SUBSTITUTE.length() + filterText.length();
                sb.insert(index, BOLD_END_SUBSTITUTE);
                searchIndex = index + BOLD_END_SUBSTITUTE.length();
                index = sb.toString().toLowerCase().indexOf(filterText.toLowerCase(), searchIndex);
            }
            return sb.toString();
        } else {
            return fitText;
        }
    }

    public static String getFindActionMapKey() {
        return SharedClassObject.findObject(FindAction.class, true).getActionMapKey().toString();
    }

    private static String replaceSubstitutes(String text) {
        text = text.replace(BOLD_START_SUBSTITUTE, "<b>"); //NOI18N
        return text.replace(BOLD_END_SUBSTITUTE, "</b>"); //NOI18N
    }

    public static void quickSearchTask(RepositoryImpl repositoryImpl) {
        JButton open = new JButton(NbBundle.getMessage(DashboardTopComponent.class, "OPTION_Open"));
        open.setEnabled(false);
        JButton cancel = new JButton(NbBundle.getMessage(DashboardTopComponent.class, "OPTION_Cancel"));

        QuickSearchPanel quickSearchPanel = new QuickSearchPanel(repositoryImpl);
        NotifyDescriptor quickSearchDialog = new NotifyDescriptor(
                quickSearchPanel,
                NbBundle.getMessage(DashboardTopComponent.class, "LBL_QuickTitle", repositoryImpl.getDisplayName()), //NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                new Object[]{open, cancel},
                open);
        quickSearchDialog.setValid(false);
        QuickSearchListener quickSearchListener = new QuickSearchListener(quickSearchPanel, open);
        quickSearchPanel.addQuickSearchListener(quickSearchListener);
        Object result = DialogDisplayer.getDefault().notify(quickSearchDialog);
        if (result == open) {
            IssueImpl issueImpl = quickSearchPanel.getSelectedTask();
            IssueAction.openIssue(issueImpl.getRepositoryImpl(), issueImpl.getID());
            Category selectedCategory = quickSearchPanel.getSelectedCategory();
            if (selectedCategory != null) {
                DashboardViewer.getInstance().addTaskToCategory(selectedCategory, new TaskNode(issueImpl, null));
            }
        }
        quickSearchPanel.removeQuickSearchListener(quickSearchListener);
    }

    private static class QuickSearchListener implements ChangeListener {

        private QuickSearchPanel quickSearchPanel;
        private JButton open;

        public QuickSearchListener(QuickSearchPanel quickSearchPanel, JButton open) {
            this.quickSearchPanel = quickSearchPanel;
            this.open = open;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            IssueImpl selectedTask = quickSearchPanel.getSelectedTask();
            open.setEnabled(selectedTask != null);
        }
    }

    public static boolean isRepositoryOpened(String repositoryId) {
        List<String> closedIds = DashboardStorage.getInstance().readClosedRepositories();
        return !closedIds.contains(repositoryId);
    }

    public static void loadCategory(Category category) {
        DashboardStorage storage = DashboardStorage.getInstance();
        List<TaskEntry> taskEntries = storage.readCategory(category.getName());
        category.setTasks(loadTasks(taskEntries));
    }

    public static Collection<RepositoryImpl> getRepositories() {
        return RepositoryRegistry.getInstance().getKnownRepositories(false, true);
    }

    private static List<IssueImpl> loadTasks(List<TaskEntry> taskEntries) {
        List<IssueImpl> tasks = new ArrayList<IssueImpl>(taskEntries.size());
        Map<String, List<String>> repository2Ids = new HashMap<String, List<String>>();

        for (TaskEntry taskEntry : taskEntries) {
            List<String> idList = repository2Ids.get(taskEntry.getRepositoryId());
            if (idList == null) {
                idList = new LinkedList<String>();
                repository2Ids.put(taskEntry.getRepositoryId(), idList);
            }
            idList.add(taskEntry.getIssueId());
        }
        for (Entry<String, List<String>> e : repository2Ids.entrySet()) {
            RepositoryImpl repository = getRepository(e.getKey());
            if (repository != null) {
                List<String> l = e.getValue();
                Collection<IssueImpl> issues = repository.getIssueImpls(l.toArray(new String[0]));
                if (issues != null) {
                    tasks.addAll(issues);
                }
            }
        }
        return tasks;
    }

    private static RepositoryImpl getRepository(String repositoryId) {
        Collection<RepositoryImpl> repositories = getRepositories();
        for (RepositoryImpl repository : repositories) {
            if (repository.getId().equals(repositoryId)) {
                return repository;
            }
        }
        return null;
    }

    public static Icon getTaskIcon(IssueImpl issue) {
        Image priorityIcon = issue.getPriorityIcon();
        Image scheduleIcon = getScheduleIcon(issue);
        if (scheduleIcon != null) {
            return ImageUtilities.image2Icon(ImageUtilities.mergeImages(priorityIcon, scheduleIcon, 0, 0));
        }
        return ImageUtilities.image2Icon(priorityIcon);
    }

    public static int getScheduleIndex(IssueImpl issue) {
        boolean afterDue = isAfterDue(issue);
        boolean scheduleNow = isInSchedule(issue);
        if (afterDue) {
            return SCHEDULE_AFTER_DUE;
        } else if (scheduleNow) {
            return SCHEDULE_IN_SCHEDULE;
        }
        return SCHEDULE_NOT_IN_SCHEDULE;
    }

    private static Image getScheduleIcon(IssueImpl issue) {
        boolean afterDue = isAfterDue(issue);
        boolean scheduleNow = isInSchedule(issue);
        if (afterDue) {
            return SCHEDULE_WARNING_ICON;
        } else if (scheduleNow) {
            return SCHEDULE_ICON;
        }
        return null;
    }

    private static boolean isAfterDue(IssueImpl issue) {
        Calendar now = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        IssueScheduleInfo schedule = issue.getSchedule();

        if (issue.getDueDate() != null) {
            dueDate.setTime(issue.getDueDate());
        } else if (schedule != null) {
            dueDate.setTime(schedule.getDate());
            dueDate.add(Calendar.DATE, schedule.getInterval());
        } else {
            return false;
        }
        return now.get(Calendar.YEAR) >= dueDate.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) >= dueDate.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isInSchedule(IssueImpl issue) {
        Calendar now = Calendar.getInstance();
        IssueScheduleInfo scheduleInfo = issue.getSchedule();
        if (scheduleInfo == null) {
            return false;
        }
        Calendar scheduleStart = Calendar.getInstance();
        scheduleStart.setTime(scheduleInfo.getDate());

        Calendar scheduleEnd = Calendar.getInstance();
        scheduleEnd.setTime(scheduleInfo.getDate());
        scheduleEnd.add(Calendar.DATE, scheduleInfo.getInterval());

        if (now.getTimeInMillis() >= scheduleStart.getTimeInMillis() && now.getTimeInMillis() <= scheduleEnd.getTimeInMillis()) {
            return true;
        }
        return false;
    }

    public static int compareTaskIds(String id1, String id2) {
        int id = 0;
        boolean isIdNumeric = true;
        try {
            id = Integer.parseInt(id1);
        } catch (NumberFormatException numberFormatException) {
            isIdNumeric = false;
        }
        int idOther = 0;
        boolean isIdOtherNumberic = true;
        try {
            idOther = Integer.parseInt(id2);
        } catch (NumberFormatException numberFormatException) {
            isIdOtherNumberic = false;
        }
        if (isIdNumeric && isIdOtherNumberic) {
            return compareNumericId(id, idOther);
        } else if (isIdNumeric) {
            return 1;
        } else if (isIdOtherNumberic) {
            return -1;
        } else {
            return compareComplexId(id1, id2);
        }
    }

    private static int compareNumericId(int id, int idOther) {
        if (id < idOther) {
            return -1;
        } else if (id > idOther) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int compareComplexId(String id1, String id2) {
        int dividerIndex1 = id1.lastIndexOf("-"); //NOI18
        int dividerIndex2 = id2.lastIndexOf("-"); //NOI18
        if (dividerIndex1 == -1 || dividerIndex2 == -1) {
            DashboardViewer.LOG.log(Level.WARNING, "Unsupported ID format - id1: {0}, id2: {1}", new Object[]{id1, id2});
            return id1.compareTo(id2);
        }
        String prefix1 = id1.subSequence(0, dividerIndex1).toString();
        String suffix1 = id1.substring(dividerIndex1 + 1);

        String prefix2 = id2.subSequence(0, dividerIndex2).toString();
        String suffix2 = id2.substring(dividerIndex2 + 1);

        //compare prefix, alphabetically
        int comparePrefix = prefix1.compareTo(prefix2);
        if (comparePrefix != 0) {
            return comparePrefix;
        }
        //compare number suffix
        int suffixInt1;
        int suffixInt2;
        try {
            suffixInt1 = Integer.parseInt(suffix1);
            suffixInt2 = Integer.parseInt(suffix2);
        } catch (NumberFormatException nfe) {
            //compare suffix alphabetically if it is not convertable to number
            DashboardViewer.LOG.log(Level.WARNING, "Unsupported ID format - id1: {0}, id2: {1}", new Object[]{id1, id2});
            return suffix1.compareTo(suffix2);
        }
        return compareNumericId(suffixInt1, suffixInt2);
    }

    public static boolean confirmDelete(String title, String message) {
        NotifyDescriptor nd = new NotifyDescriptor(
                message,
                title,
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                null,
                NotifyDescriptor.YES_OPTION);
        return DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION;
    }

    public static SchedulingMenu createScheduleMenu(IssueScheduleInfo previousSchedule) {
        return new SchedulingMenu(previousSchedule);
    }

    public static IssueScheduleInfo getToday() {
        Calendar calendar = getTodayCalendar();
        return new IssueScheduleInfo(calendar.getTime());
    }

    public static IssueScheduleInfo getThisWeek() {
        Calendar calendar = getTodayCalendar();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        return new IssueScheduleInfo(calendar.getTime(), 7);
    }

    public static IssueScheduleInfo getNextWeek() {
        Calendar calendar = getTodayCalendar();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.add(Calendar.DATE, 7);
        return new IssueScheduleInfo(calendar.getTime(), 7);
    }

    public static IssueScheduleInfo getAll() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        return new IssueScheduleInfo(calendar.getTime(), Integer.MAX_VALUE);
    }

    public static Calendar getTodayCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static int getMillisToTomorrow() {
        Calendar now = Calendar.getInstance();
        Calendar tomorrow = getTodayCalendar();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Long millisToTomorrow = (tomorrow.getTimeInMillis() - now.getTimeInMillis()) + 60 * 1000; // plus one minute to be sure
        return millisToTomorrow.intValue();
    }

    public static void setQueryAutoRefresh(QueryImpl q, boolean b) {
        NbPreferences.forModule(Actions.class).putBoolean(getQueryRefreshKey(q), b);
    }

    public static boolean isQueryAutoRefresh(QueryImpl q) {
        return NbPreferences.forModule(Actions.class).getBoolean(getQueryRefreshKey(q), true);
    }
    
    private static String getQueryRefreshKey(QueryImpl q) {
        RepositoryImpl r = q.getRepositoryImpl();
        StringBuilder sb = new StringBuilder();
        sb.append("query.auto.refresh.");
        sb.append(r.getConnectorId());
        sb.append("<=>");
        sb.append(r.getId());
        sb.append("<=>");
        sb.append(q.getDisplayName());
        return sb.toString();
    }
        
    public static final class SchedulingMenu {

        private final JMenu menu;
        private final List<JMenuItem> menuItems;
        private final ChangeSupport support;
        private IssueScheduleInfo scheduleInfo;

        @NbBundle.Messages({
            "CTL_ChooseDate=Choose Date...",
            "# {0} - date", "CTL_CustomDateChosen=Custom: {0}",
            "CTL_ThisWeek=This Week",
            "CTL_NextWeek=Next Week",
            "# {0} - today's name", "CTL_Today={0} - Today",
            "CTL_NotScheduled=Not Scheduled"
        })
        public SchedulingMenu(final IssueScheduleInfo previousSchedule) {
            this.support = new ChangeSupport(this);
            this.menu = new JMenu(NbBundle.getMessage(DashboardUtils.class, "LBL_ScheduleFor"));
            this.menuItems = new ArrayList<JMenuItem>();

            for (int i = 0; i < 7; i++) {
                final Calendar calendar = getTodayCalendar();
                calendar.add(Calendar.DATE, i);
                String itemName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
                if (i == 0) {
                    itemName = Bundle.CTL_Today(itemName);
                }
                JMenuItem item = new JCheckBoxMenuItem(new ScheduleItemAction(itemName, new IssueScheduleInfo(calendar.getTime())));
                menu.add(item);
                menuItems.add(item);
            }
            menu.addSeparator();
            menuItems.add(null);

            JMenuItem thisWeek = new JCheckBoxMenuItem(new ScheduleItemAction(
                    Bundle.CTL_ThisWeek(),
                    getThisWeek()));

            menu.add(thisWeek);
            menuItems.add(thisWeek);

            JMenuItem nextWeek = new JCheckBoxMenuItem(new ScheduleItemAction(
                    Bundle.CTL_NextWeek(),
                    getNextWeek()));

            menu.add(nextWeek);
            menuItems.add(nextWeek);
            menu.addSeparator();
            menuItems.add(null);

            final IDEServices.DatePickerComponent picker = UIUtils.createDatePickerComponent();
            JMenuItem chooseDate = null;
            if (picker.allowsOpeningDaySelector()) {
                chooseDate = new JCheckBoxMenuItem(new ScheduleItemAction(Bundle.CTL_ChooseDate(),
                        null) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                picker.setDate(previousSchedule == null ? new Date() : previousSchedule.getDate());
                                if (picker.openDaySelector()) {
                                    Date date = picker.getDate();
                                    scheduleInfo = date == null ? null : new IssueScheduleInfo(date);
                                    support.fireChange();
                                }
                            }
                        });
            } else if (previousSchedule != null) {
                chooseDate = new JCheckBoxMenuItem(Bundle.CTL_CustomDateChosen(
                        DateFormat.getDateInstance(DateFormat.MEDIUM).format(previousSchedule.getDate())));
                chooseDate.setEnabled(false);
            }
            if (chooseDate != null) {
                menu.add(chooseDate);
                menuItems.add(chooseDate);
            }

            JMenuItem notScheduled = new JCheckBoxMenuItem(new ScheduleItemAction(
                    Bundle.CTL_NotScheduled(),
                    null));
            menu.add(notScheduled);
            menuItems.add(notScheduled);

            // select already schedule item
            if (previousSchedule == null) {
                notScheduled.setSelected(true);
                return;
            }
            for (JMenuItem item : menuItems) {
                if (item != null && item.getAction() instanceof ScheduleItemAction) {
                    IssueScheduleInfo assignedSchedule = ((ScheduleItemAction) item.getAction()).getAssignedSchedule();
                    if (assignedSchedule != null && assignedSchedule.equals(previousSchedule)) {
                        item.setSelected(true);
                        return;
                    }
                }
            }
            if (chooseDate != null) {
                chooseDate.setSelected(true);
            }
        }

        public Action getMenuAction() {
            return new ScheduleMenuAction(menu);
        }

        public List<JMenuItem> getMenuItems() {
            return menuItems;
        }

        public IssueScheduleInfo getScheduleInfo() {
            return scheduleInfo;
        }

        public void addChangeListener(ChangeListener listener) {
            support.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            support.removeChangeListener(listener);
        }

        private class ScheduleItemAction extends AbstractAction {

            private final IssueScheduleInfo assignedSchedule;

            public ScheduleItemAction(String name, IssueScheduleInfo assignedSchedule) {
                super(name);
                this.assignedSchedule = assignedSchedule;
            }

            public IssueScheduleInfo getAssignedSchedule() {
                return assignedSchedule;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                scheduleInfo = assignedSchedule;
                support.fireChange();
            }
        }
    }

    private static class ScheduleMenuAction extends AbstractAction implements Presenter.Popup {

        private final JMenu menu;

        public ScheduleMenuAction(JMenu menu) {
            super();
            this.menu = menu;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            menu.getAction().actionPerformed(e);
        }

        @Override
        public void setEnabled(boolean newValue) {
            super.setEnabled(newValue);
            menu.setEnabled(newValue);
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return menu;
        }
    }
}
