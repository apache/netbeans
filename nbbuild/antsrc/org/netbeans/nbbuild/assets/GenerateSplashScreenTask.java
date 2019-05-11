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
package org.netbeans.nbbuild.assets;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Generates an Apache NetBeans splash screen with the given text.
 *
 * @see
 * <a href="https://issues.apache.org/jira/browse/NETBEANS-2455">NETBEANS-2455</a>
 */
public class GenerateSplashScreenTask extends Task {

    /**
     * The TrueType font to use.
     */
    private File font;
    /**
     * The resulting output file.
     */
    private File output;
    /**
     * The text to set on the splash screen.
     */
    private StringBuilder text;
    /**
     * Whether we want a splash screen (640x396) or an about (529x327) image.
     */
    private boolean about = false;
    
    @Override
    public void execute() throws BuildException {
        if (!font.exists() || !font.canRead() || !font.isFile()) {
            throw new BuildException("True type font '%s' not found", font.getAbsolutePath());
        }
        if (text == null) {
            throw new BuildException("Please specify a text to imprint in the splash screen.");
        }
        String imprintText = getProject().replaceProperties(text.toString());
        if (imprintText.startsWith("IDE dev-")) {
            String [] parts = imprintText.split("-");
            imprintText = String.format("Development build %s", parts[1]);
        }
        generateSplashScreen(font, imprintText, output);
    }

    private void generateSplashScreen(File ttfFontFile, String imprintText, File outputFile) {
        BufferedImage splashScreenTemplateImage = null;
        String templateName = about ? "apache-netbeans-about.png" : "apache-netbeans-splash.png";
        try (InputStream imageInput = GenerateSplashScreenTask.class.getResourceAsStream(templateName)) {
            if (imageInput == null) {
                throw new BuildException("Cannot find image 'apache-netbeans-splash-png'");
            }
            splashScreenTemplateImage = ImageIO.read(imageInput);
        } catch (IOException ex) {
            throw new BuildException(ex.getMessage(), ex);
        }

        Font font = null;

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, ttfFontFile);
        } catch (FontFormatException ex) {
            throw new BuildException(String.format("Invalid font format in file %s: %s",
                    ttfFontFile.getAbsolutePath(), ex.getMessage()), ex);
        } catch (IOException ex) {
            throw new BuildException(String.format("Error reading font file %s: %s",
                    ttfFontFile.getAbsolutePath(), ex.getMessage()), ex);
        }

        imprintImage(splashScreenTemplateImage, font, imprintText);

        try {
            ImageIO.write(splashScreenTemplateImage, "PNG", output);
        } catch (IOException ex) {
            throw new BuildException("Cannot write splash image to '%s': %s", output.getAbsolutePath(), ex.getMessage());
        }

        log("Generated " + (about ? "about" : "splash") + " screen in " + output.getAbsolutePath());
    }

    /**
     * The width of the SVG file.
     */
    private static final double SVG_WIDTH = 1242;

    /**
     * The height of the SVG file.
     */
    private static final double SVG_HEIGHT = 768;

    /**
     * The X coordinate of the text position in the SVG file.
     */
    private static final double SVG_TEXT_X = 474;

    /**
     * The Y coordinate of the bottom-left corner of the text in the SVG file.
     * (Note, Y coordinates start from bottom in SVG).
     */
    private static final double SVG_TEXT_Y = 200;

    /**
     * The Y coordinate of the bottom-left corner of the text in the SVG file
     * (measured from top).
     */
    private static final double SVG_TEXT_Y_FROM_TOP = SVG_HEIGHT - SVG_TEXT_Y;

    /**
     * The font size as in the SVG file. This is used to resize the font in the
     * PNG file.
     */
    private static final double SVG_FONT_SIZE = 32;
    
    
    private static final Color INFO_TEXT_COLOR = new Color(0x1b, 0x6a, 0xc6);

    private void imprintImage(BufferedImage splashScreenTemplateImage, Font font, String imprintText) {
        int imageWidth = splashScreenTemplateImage.getWidth();
        int imageHeight = splashScreenTemplateImage.getHeight();

        double xtext = SVG_TEXT_X * imageWidth / SVG_WIDTH;
        double ytext = SVG_TEXT_Y_FROM_TOP * imageHeight / SVG_HEIGHT;

        // Prepare everything for painting
        Graphics2D g = (Graphics2D) splashScreenTemplateImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Derive an appropriate font size
        double fontSize = 1.5 * SVG_FONT_SIZE * imageHeight / SVG_HEIGHT;
        Font derivedFont = font.deriveFont((float) fontSize);
        g.setFont(derivedFont);

        // The color of the text
        g.setColor(INFO_TEXT_COLOR);
        
        

        g.drawString(imprintText, (float) xtext, (float) ytext);
    }

    public File getFont() {
        return font;
    }

    public void setFont(File font) {
        this.font = font;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public boolean isAbout() {
        return about;
    }

    public void setAbout(boolean about) {
        this.about = about;
    }

    public void addText(String someText) {
        if (text == null) {
            text = new StringBuilder();
        }
        text.append(someText);
    }
    
}
