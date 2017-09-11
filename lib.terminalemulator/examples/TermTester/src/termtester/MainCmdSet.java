/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package termtester;

import interp.Cmd;
import interp.CmdSet;
import interp.Interp;
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author ivan
 */
final class MainCmdSet extends CmdSet {
    private final Context context;

    public MainCmdSet(Interp interp, Context context) {
        super(interp);
        this.context = context;

        interp.addCmd(new CmdQuit());

        interp.addCmd(new CmdClear());
        interp.addCmd(new CmdClearEnd());
        interp.addCmd(new CmdCursorMotion());
        interp.addCmd(new CmdDeleteLines());
        interp.addCmd(new CmdFill());
        interp.addCmd(new CmdGo());
        interp.addCmd(new CmdGlyph());
        interp.addCmd(new CmdHome());
        interp.addCmd(new CmdInsertLines());
        interp.addCmd(new CmdMark());
        interp.addCmd(new CmdMargin());
        interp.addCmd(new CmdSend());
        interp.addCmd(new CmdPause());
        interp.addCmd(new CmdTest());
        interp.addCmd(new CmdTerm());
    }

    private class CmdQuit extends Cmd {

        public CmdQuit() {
            super("quit");
        }

        public void run(String[] args) {
            context.finish();
            interp().setDone();
        }

        public void help() {
        }
    }

    private class CmdTest extends Cmd {

        public CmdTest() {
            super("test");
        }

        public void run(String[] args) {
            if (args.length == 0)
                error("Missing test name");
            Test test = Test.find(args[0]);
            if (test == null)
                error("No such test");

            String[] rargs = new String[args.length-1];
            for (int rx = 0; rx < rargs.length; rx++)
                rargs[rx] = args[rx+1];

            test.run(rargs);
        }

        public void help() {
            for (Test test : Test.tests())
                printf("%s\t%s\n", test.name, (test.info() != null)? test.info(): "");

        }
    }

    private class CmdTerm extends Cmd {

        public CmdTerm() {
            super("term");
        }

        public void run(String[] args) {
            if (context.getTerm() == null) {
                printf("No Term\n");
                return;
            }

            if (args.length == 0)
                printf("%s\n", context.getTerm().getEmulation());
            else if (args.length == 1)
                context.getTerm().setEmulation(args[0]);
        }

        public void help() {
            printf("term [ <term> ]\n");
        }
    }

    private class CmdSend extends Cmd {

        public CmdSend() {
            super("send");
        }

        public void run(String[] args) {
            for (String s : args)
                context.send(s);
        }

        public void help() {
        }

    }


    private class CmdClear extends Cmd {

        public CmdClear() {
            super("clear");
        }

        public void run(String[] args) {
            if (args.length == 0) {
                context.send("\\ESC[J");
            } else if (args.length == 1) {
                context.send("\\ESC[%sJ", args[0]);
            } else {
                error("wrong number of arguments");
            }
        }

        public void help() {
            printf("clear    erase down\n");
            printf("clear 0  erase down\n");
            printf("clear 1  erase up\n");
            printf("clear 2  erase all + home cursor\n");
        }
    }

    private class CmdDeleteLines extends Cmd {

        public CmdDeleteLines() {
            super("delete_lines", "DL");
        }

        public void run(String[] args) {
            if (args.length == 0) {
                context.send("\\ESC[M");
            } else if (args.length == 1) {
                context.send("\\ESC[%sM", args[0]);
            } else {
                error("wrong number of arguments");
            }
        }

        public void help() {
            printf("Alias: DL\n");
            printf("delete_lines   (default N=1)\n");
            printf("delete_lines N\n");
        }
    }

    private class CmdMargin extends Cmd {

        public CmdMargin() {
            super("margin");
        }

        public void run(String[] args) {
            if (args.length == 0) {
                if (context.getMargin() == null)
                    printf("no margin\n");
                else
                    printf("margin %d-%d\n", context.getMargin().low, context.getMargin().hi);
            } else if (args.length == 1) {
                if (args[0].equals("reset"))
                    context.setMargin(null);
                else if (args[0].equals("send")) {
                    if (context.getMargin() == null)
                        printf("no margin\n");
                    else
                        context.sendMargin();
                } else
                    error("wrong number of arguments");
            } else if (args.length == 2) {
                context.setMargin(new Context.Margin(Integer.parseInt(args[0]),
                                                     Integer.parseInt(args[1])));
            } else {
                error("wrong number of arguments");
            }
        }

        public void help() {
            printf("margin\n");
            printf("margin reset\n");
            printf("margin send\n");
            printf("margin <low> <hi>\n");
        }
    }

    private class CmdMark extends Cmd {

        public CmdMark() {
            super("mark");
        }

        @Override
        public void run(String[] args) {
            // 'hbvi' addds 48 (ascii '0') to glyph_id and 58 to color_id.
            // color_id's 40-47 give "standard colors"
            // color_id's 58-65 give custom colors which are settable using 
            // Term.setCustomColor()
            //
            // Typical sequence then should be something like this:
            //          term ansi       # not dtterm!
            //          glyph gutter 16 # doesn't automatically refresh
            //          fill X          # refreshes but doesn't resize
            //          glyph reg 1 bpt.png
            //                          # register glyph_id 1 to bpt.png in /resources
            //          mark 1 41       # put a mark using glyph_id 1 and
            //                          # standard color 41 (red)

            if (args.length == 0) {
                error("mising glyph-id");
            } else if (args.length == 1) {
                int glyph_id = Integer.parseInt(args[0]);
                context.send("\\ESC[22;%d;0t", glyph_id);
            } else if (args.length == 2) {
                int glyph_id = Integer.parseInt(args[0]);
                int color_id = Integer.parseInt(args[1]);
                context.send("\\ESC[22;%d;%dt", glyph_id, color_id);
            } else {
                error("wrong number of arguments");
            }
        }

        @Override
        public void help() {
            printf("mark <glyph-id> [ <color-id> ]\n");
        }
    }

    private class CmdGo extends Cmd {

        public CmdGo() {
            super("go");
        }

        public void run(String[] args) {
            if (args.length == 0) {
                context.send("\\ESC[;H");
            } else if (args.length == 1) {
                String direction = args[0].toUpperCase();
                Util.Direction dir = Util.Direction.valueOf(direction);
                Util.go(context, dir);
            } else if (args.length == 2) {
                Util.go(context, Integer.parseInt(args[0]),
                                 Integer.parseInt(args[1]));
                context.send("\\ESC[%s;%sH", args[0], args[1]);
            } else {
                error("wrong number of arguments");
            }
        }

        public void help() {
            printf("go        # ... home\n");
            printf("go <compass-direction>\n");
            printf("go row col\n");
        }
    }

    private class CmdGlyph extends Cmd {

        public CmdGlyph() {
            super("glyph");
        }

        @Override
        public void run(String[] args) {
            if (args.length < 2) {
                error("wrong number of arguments");
            } else {
                switch (args[0]) {
                    case "gutter":
                        if (args.length != 2)
                            error("wrong number of arguments");
                        int width = Integer.parseInt(args[1]);
                        context.getTerm().setGlyphGutterWidth(width);
                        context.getTerm().revalidate();
                        break;
                    case "reg":
                        if (args.length != 3)
                            error("wrong number of arguments");
                        int id = Integer.parseInt(args[1]);
                        String imageName = args[2];
                        String imagePath = "resources/" + imageName;
                        Image image = null;
                        try {
                            image = ImageIO.read(getClass().getClassLoader().getResource(imagePath));
                        } catch (IOException ex) {
                            error("Couldn't load image as " + imagePath);
                        }
                        context.getTerm().setGlyphImage(id, image);
                        break;
                    default:
                        error("unrecognized sub-cmd '%s'", args[0]);
                        break;
                }
            }
        }

        public void help() {
            printf("glyph gutter <width>\n");
            printf("glyph reg <id> <imgname> # 'bpt.png'\n");
        }
    }

    private class CmdHome extends Cmd {

        public CmdHome() {
            super("home");
        }

        public void run(String[] args) {
            context.send("\\ESC[;H");
        }

        public void help() {
        }
    }

    private class CmdInsertLines extends Cmd {

        public CmdInsertLines() {
            super("insert_lines", "IL");
        }

        public void run(String[] args) {
            if (args.length == 0) {
                context.send("\\ESC[L");
            } else if (args.length == 1) {
                context.send("\\ESC[%sL", args[0]);
            } else {
                error("wrong number of arguments");
            }
        }

        public void help() {
            printf("Alias: IL\n");
            printf("insert   (default N=1)\n");
            printf("insert N\n");
        }
    }

    // public void op_cm(int row, int col); // cursor motion
    // public void op_ce(); // clear to end of line
    private class CmdCursorMotion extends Cmd {

        public CmdCursorMotion() {
            super("cursor_motion", "cm");
        }

        @Override
        public void run(String[] args) {
            if (args.length != 2) {
                error("wrong number of arguments");
            }
            context.send("\\ESC[%s;%sH", args[0], args[1]);
        }

        @Override
        public void help() {
            printf("Alias: cm\n");
            printf("cursor_motion ROW COL\n");
        }
    }

    private class CmdClearEnd extends Cmd {

        public CmdClearEnd() {
            super("clear_end", "ce");
        }

        @Override
        public void run(String[] args) {
            if (args.length == 0) {
                context.send("\\ESC[K");
            } else if (args.length == 1) {
                context.send("\\ESC[%sK", args[0]);
            } else {
                error("wrong number of arguments");
            }
        }

        @Override
        public void help() {
            printf("Alias: ce\n");
            printf("clear_end            (default N = 0)\n");
            printf("clear_end 0          (from cursor to EOL)\n");
            printf("clear_end 1          (from beginning to cursor)\n");
            printf("clear_end 2          (erase all line)\n");
        }
    }

    private class CmdFill extends Cmd {

        public CmdFill() {
            super("fill");
        }

        public void run(String[] args) {
            Util.FillPattern fillPattern = Util.FillPattern.ROWCOL;
            if (args.length == 0) {
            } else if (args.length == 1) {
                fillPattern = Util.FillPattern.valueOf(args[0]);
            } else {
                error("wrong number of arguments");
            }
            Util.fill(context, fillPattern);
        }

        public void help() {
            printf("fill\n");
            printf("fill X\n");
            printf("fill ROWCOL\n");
        }
    }

    private class CmdPause extends Cmd {

        public CmdPause() {
            super("pause");
        }

        public void run(String[] args) {
            context.pause();
        }

        public void help() {
        }
    }
    
}
