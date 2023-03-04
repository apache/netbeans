/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * "Ops.java"
 * Ops.java 1.14 01/07/23
 * The abstract operations the terminal can perform.
 */

package org.netbeans.lib.terminalemulator;

/**
 * The mnemonics for the ops are _roughly_ based on termcap entries.
 */

public
interface Ops {
    public void op_pause();
    public void op_char(char c);
    public void op_carriage_return();
    public void op_line_feed();         // == op_ind(1)
    public void op_back_space();
    public void op_tab();
    public void op_bel();

    public void op_soft_reset();
    public void op_full_reset();

    public void op_as();            // LS1, SO, map G1 into GL (~ switch to graphic font)
    public void op_ae();            // LS0, SI, map G0 into GL (~ switch to normal font)
    public void op_al(int count); // add new blank line
    public void op_bc(int count); // back cursor/column
    public void op_cm(int row, int col); // cursor motion
    public void op_cl(); // clear screen and home cursor
    public void op_ce(); // clear to end of line
    public void op_cd(); // clear to end of screen
    public void op_dc(int count);	// delete character
    public void op_dl(int count); // delete line & scroll everything under it up
    public void op_do(int count); // down 1 line
    public void op_ho(); // cursor home (upper left of the screen)
    public void op_ic(int count); // insert character
    public void op_nd(int count); // cursor right (non-destructive space)
    public void op_up(int count); // == op_ri
    public void op_sc();	// save cursor position
    public void op_rc();	// restore saved cursor position
    public void op_margin(int from, int to);	// set vertical scroll margins

    public void op_el(int code);        // Erase in Line

    public void op_attr(int mode);	// set ANSI attributes
    public void op_set_mode(int mode);
    public void op_reset_mode(int mode);

    // These cause data to be sent back:
    public void op_status_report(int code);


    // ops mimicing certain DtTerm features
    public void op_glyph(int glyph, int rendition);	// assign glyph
							// to current row
    public void op_reverse(boolean reverse);
    public void op_cursor_visible(boolean cursor);
    
    public void op_icon_name(String iconName);
    public void op_win_title(String winTitle);
    public void op_cwd(String currentWorkingDirectory);
    public void op_ext(String command);

    // font mgmt
    public void op_setG(int gx, int font);
    public void op_selectGL(int gx);


    
    // querying operations
    public int op_get_width();
    public int op_get_column();	// ... cursor is currently located on (0-origin)
					    

    // ops unique to Term
    public void op_time(boolean refresh);	// dump time into output &
						// control refreshEnabled prop
    public void op_hyperlink(String url, String text);

    public void logUnrecognizedSequence(String toString);
    public void logCompletedSequence(String toString);

    public void op_send_chars(String sequence);

    public void op_cha(int i);          // Cursor Horizontal Absolute
    public void op_ech(int i);          // Erase CHaracters
    public void op_vpa(int i);          // Vertical Position Absolute     
    public void op_ed(int i);           // Erase in Display
    public void op_cbt(int n);          // Cursor Backward Tabulation
    public void op_cht(int n);          // Cursor Horizontal Tabulation

    public void op_ri(int n);           // Reverse Index (scrolls)
    public void op_cuu(int n);          // Cursor Up (doesn't scroll)

    public void op_ind(int n);          // Index (~= \LF) (scrolls)
    public void op_cud(int n);          // Cursor Down (doesn't scroll)
}
