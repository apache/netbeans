/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.platform.models;

import java.util.List;
import javax.swing.AbstractListModel;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;

public class PythonPlatformListModel extends AbstractListModel {
    private PythonPlatformManager manager = PythonPlatformManager.getInstance();
    private List<PythonPlatform> model = manager.getPlatforms();

    @Override
    public int getSize() {
        return model.size();
    }

    @Override
    public Object getElementAt(int index) {
        if (index >= 0 && index < model.size()) {
            return model.get(index);
        } else {
            return null;
        }
    }
    
    public void refresh(){
        model = manager.getPlatforms();
        fireContentsChanged(this, 0, model.size() -1);
    }
}
