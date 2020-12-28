/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.options;

import javax.swing.Icon;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.ImageUtilities;

public final class OptionsOptionsCategory extends OptionsCategory {

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/python/options/py_25_32.png", false);
    }

    @Override
    public String getCategoryName() {
        return NbBundle.getMessage(OptionsOptionsCategory.class, "OptionsCategory_Name_Options");
    }

    @Override
    public String getTitle() {
        return NbBundle.getMessage(OptionsOptionsCategory.class, "OptionsCategory_Title_Options");
    }

    @Override
    public OptionsPanelController create() {
        return new OptionsOptionsPanelController();
    }
}
