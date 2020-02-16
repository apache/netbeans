/*
 * Action added only for development propurses, it will be to removed before merge.
 */
package org.netbeans.modules.notifications.linux;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Help",
        id = "org.netbeans.modules.notifications.linux.LinuxNotificationChecker"
)
@ActionRegistration(
        displayName = "#CTL_LinuxNotificationChecker"
)
@ActionReference(path = "Menu/Help", position = 1250)
@Messages("CTL_LinuxNotificationChecker=Check linux notifcations")
public final class LinuxNotificationChecker implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Image image = ImageUtilities.loadImage("org/netbeans/modules/notifications/resources/notifications.png");
        NotificationDisplayer.getDefault().notify("Netbeans test notification", ImageUtilities.image2Icon(image), "Netbeans test notification body", null);
    }
}
