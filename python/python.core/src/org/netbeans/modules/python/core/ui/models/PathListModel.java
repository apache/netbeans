/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.core.ui.models;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;

public class PathListModel extends AbstractListModel{
    private List<String> model = new ArrayList<>();

    @Override
    public int getSize() {
        return model.size();
    }

    @Override
    public Object getElementAt(int index) {
        return model.get(index);
    }

    public List<String> getModel() {
        return model;
    }

    public void setModel(List<String> model) {
        this.model = model;
        fireContentsChanged(this, 0, model.size() -1);
    }
    
    public void add(String element){
        model.add(element);
        fireContentsChanged(this, 0, model.size() -1);
    }
    public void remove(int index){
        if(model.size() > 0 && index < model.size()){
            model.remove(index);
            fireContentsChanged(this, 0, model.size() -1);
        }
    }
    public void moveUp(int index){
        if (index > 0){
            String temp = model.remove(index);
            model.add(index-1, temp);
            fireContentsChanged(this, 0, model.size() -1);
        }
    }
    public void moveDown(int index){
        if (index +1 < model.size()){ // +1 because you can't move 'down' from the end position!
            String temp = model.remove(index);
            model.add(index+1, temp);
            fireContentsChanged(this, 0, model.size() -1);
        }
    }
}
