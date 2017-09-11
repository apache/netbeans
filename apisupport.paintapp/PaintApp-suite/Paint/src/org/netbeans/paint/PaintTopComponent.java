/*
 * Copyright (c) 2010, Oracle. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.netbeans.paint;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.swing.colorchooser.ColorChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

@ConvertAsProperties(dtd = "-//org.netbeans.paint//PaintTopComponent//EN", autostore = false)
@TopComponent.Description(preferredID = "PaintTopComponent", iconBase = "/org/netbeans/paint/new_icon.png", persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "org.netbeans.paint.PaintTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 0),
    @ActionReference(path = "Toolbars/File", position = 0)
})
@TopComponent.OpenActionRegistration(displayName = "#CTL_NewCanvasAction")
public final class PaintTopComponent extends TopComponent implements ActionListener, ChangeListener {
    private static int ct = 0; //A counter you use to provide names for new images
    private final PaintCanvas canvas = new PaintCanvas(); //The component the user draws on
    private final JComponent preview = canvas.getBrushSizeView(); //A component in the toolbar that shows the paintbrush size
    private final JToolBar toolbar = new JToolBar(); //The toolbar
    private final ColorChooser color = new ColorChooser(); //Our color chooser component from the ColorChooser library
    private final JButton clear = new JButton(
            NbBundle.getMessage(PaintTopComponent.class, "LBL_Clear")); //A button to clear the canvas
    private final JLabel label = new JLabel(
            NbBundle.getMessage(PaintTopComponent.class, "LBL_Foreground")); //A label for the color chooser
    private final JLabel brushSizeLabel = new JLabel(
            NbBundle.getMessage(PaintTopComponent.class, "LBL_BrushSize")); //A label for the brush size slider
    private final JSlider brushSizeSlider = new JSlider(1, 24); //A slider to set the brush size
    private InstanceContent content = new InstanceContent(); //The bag of stuff we add/remove the Saver from, and store the last-used file in
    private Saver saver = new Saver();

    public PaintTopComponent() {
        initComponents();
        String displayName = NbBundle.getMessage(
                PaintTopComponent.class,
                "UnsavedImageNameFormat", ct++);
        setDisplayName(displayName);
        //If we don't set the name, Window > Documents will throw an exception
        setName(displayName);
        //Connect our lookup to the rest of the system, so that
        //SaveAction will pay attention to whether or not the Saver is available
        associateLookup(new AbstractLookup(content));
        //Enable the Print action for the canvas:
        putClientProperty("print.printable", true);
        //Disable the Save action by default:
        enableSaveAction(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        //Configure our components, attach listeners
        color.addActionListener(this);
        clear.addActionListener(this);
        brushSizeSlider.setValue(canvas.getBrushDiameter());
        brushSizeSlider.addChangeListener(this);
        color.setColor(canvas.getColor());
        color.setMaximumSize(new Dimension(16, 16));
        //Install the toolbar and the painting component:
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        //Configure the toolbar
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 7, 7));
        toolbar.setFloatable(false);
        //Now populate our toolbar:
        toolbar.add(label);
        toolbar.add(color);
        toolbar.add(brushSizeLabel);
        toolbar.add(brushSizeSlider);
        toolbar.add(preview);
        toolbar.add(clear);
    }

    private void enableSaveAction(boolean canSave) {
        if (canSave) {
            //If the canvas is modified,
            //we add SaveCookie impl to Lookup:
            content.add(saver);
        } else {
            //Otherwise, we remove the SaveCookie impl from the lookup:
            content.remove(saver);
            canvas.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    //Once we can save, we are done listening.
                    //If enableSaveAction(false) is called, we will
                    //start listening again.
                    canvas.removeMouseListener(this);
                    enableSaveAction(true);
                }
            });
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        canvas.setBrushDiameter(brushSizeSlider.getValue());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            canvas.clear();
            enableSaveAction(false);
        } else if (e.getSource() instanceof ColorChooser) {
            ColorChooser cc = (ColorChooser) e.getSource();
            canvas.setColor(cc.getColor());
        }
    }

    private class Saver implements SaveCookie {

        @Override
        public void save() throws IOException {
            DataObject theFile = getLookup().lookup(DataObject.class);
            if (theFile != null) {
                File saveTo = FileUtil.toFile(theFile.getPrimaryFile());
                save(saveTo);
            } else {
                saveAs();
            }
        }

        public void saveAs() throws IOException {
            String title = NbBundle.getMessage(Saver.class, "TTL_SAVE_DIALOG");
            File f = new FileChooserBuilder(Saver.class).setTitle(title).showSaveDialog();
            if (f != null) {
                if (!f.getAbsolutePath().endsWith(".png")) {
                    f = new File(f.getAbsolutePath() + ".png");
                }
                try {
                    if (!f.exists()) {
                        if (!f.createNewFile()) {
                            String failMsg = NbBundle.getMessage(
                                    PaintTopComponent.class,
                                    "MSG_SaveFailed", f.getName());
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(failMsg));
                            return;
                        }
                    } else {
                        String overwriteMessage = NbBundle.getMessage(Saver.class, "MSG_Overwrite", f.getName());
                        Object userChose = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(overwriteMessage));
                        if (NotifyDescriptor.CANCEL_OPTION.equals(userChose)) {
                            return;
                        }
                    }
                    //Need getAbsoluteFile(), or X.png and x.png are different on windows
                    save(f.getAbsoluteFile());
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }

        private void save(File f) throws IOException {
            ImageIO.write(canvas.getImage(), "png", f);
            String savedMessage = NbBundle.getMessage(Saver.class, "MSG_Saved", f.getName());
            StatusDisplayer.getDefault().setStatusText(savedMessage);
            FileObject fob = FileUtil.toFileObject(FileUtil.normalizeFile(f));
            assert fob != null : "MasterFS excluded from suite?";
            //Store the file, so we don't show the Save dialog again
            content.add(DataObject.find(fob));
            setDisplayName(fob.getName());
            enableSaveAction(false);
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // now store the color and size
        p.setProperty("color", "" + color.getColor().getRGB()); 
        p.setProperty("size", "" + brushSizeSlider.getValue());
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        assert "1.0".equals(version);
        final String rgbRef = p.getProperty("color");
        final String sizeRef = p.getProperty("size");
        if (rgbRef != null) {
            int rgb = Integer.parseInt(rgbRef);
            final Color c = new Color(rgb);
            color.setColor(c);
            canvas.setColor(c);
        }
        if (sizeRef != null) {
            int size = Integer.parseInt(sizeRef);
            brushSizeSlider.setValue(size);
            canvas.setBrushDiameter(size);
        }
    }
}
