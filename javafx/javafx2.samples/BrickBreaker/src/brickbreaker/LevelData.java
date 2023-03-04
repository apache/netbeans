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
package brickbreaker;

import java.util.Arrays;

import javafx.collections.ObservableList;

public class LevelData {

    private static final String NEXT_LEVEL = "---";

    private static final String[] LEVELS_DATA = new String[] {
        "",
        "",
        "",
        "",
        "WWWWWWWWWWWWWWW",
        "WYYWYYWYWYYWYYW",
        "WWWWWWWWWWWWWWW",
        "WWWWWWWWWWWWWWW",
        "RWRWRWRWRWRWRWR",
        "WRWRWRWRWRWRWRW",
        "WWWWWWWWWWWWWWW",
        "LLLLLLLLLLLLLLL",

        NEXT_LEVEL,

        "",
        "",
        "",
        "",
        "",
        "W              ",
        "WO             ",
        "WOG            ",
        "WOGR           ",
        "WOGRB          ",
        "WOGRBC         ",
        "WOGRBCL        ",
        "WOGRBCLV       ",
        "WOGRBCLVY      ",
        "WOGRBCLVYM     ",
        "WOGRBCLVYMW    ",
        "WOGRBCLVYMWO   ",
        "WOGRBCLVYMWOG  ",
        "WOGRBCLVYMWOGR ",
        "22222222222222B",

        NEXT_LEVEL,

        "",
        "",
        "",
        "00    000000000",
        "",
        "    222 222 222",
        "    2G2 2G2 2G2",
        "    222 222 222",
        "",
        "  222 222 222  ",
        "  2R2 2R2 2R2  ",
        "  222 222 222  ",
        "",
        "222 222 222    ",
        "2L2 2L2 2L2    ",
        "222 222 222    ",

        NEXT_LEVEL,

        "RRRRRRRRRRRRRRR",
        "RWWWWWWWWWWWWWR",
        "RWRRRRRRRRRRRWR",
        "RWRWWWWWWWWWRWR",
        "RWRWRRRRRRRWRWR",
        "RWRWR     RWRWR",
        "RWRWR     RWRWR",
        "RWRWR     RWRWR",
        "RWRWR     RWRWR",
        "RWRWR     RWRWR",
        "RWRW2222222WRWR",
        "",
        "",
        "222222222222222",

        NEXT_LEVEL,

        "",
        "    Y     Y    ",
        "    Y     Y    ",
        "     Y   Y     ",
        "     Y   Y     ",
        "    2222222    ",
        "   222222222   ",
        "   22R222R22   ",
        "  222R222R222  ",
        " 2222222222222 ",
        " 2222222222222 ",
        " 2222222222222 ",
        " 2 222222222 2 ",
        " 2 2       2 2 ",
        " 2 2       2 2 ",
        "    222 222    ",
        "    222 222    ",

        NEXT_LEVEL,

        "OOOOOOOOOOOOOOO",
        "OOOOOOOOOOOOOOO",
        "OOOOOOOOOOOOOOO",
        "",
        "",
        "GGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGG",
        "GGGGGGGGGGGGGGG",
        "",
        "",
        "YYYYYYWWWYYYYYY",
        "222222WWW222222",
        "YYYYYYWWWYYYYYY",
        "YYY0       0YYY",
        "YY           YY",
        "Y             Y",

        NEXT_LEVEL,

        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "R O Y W G B C M",
        "2 2 2 2 2 2 2 2",

        NEXT_LEVEL,

        "",
        "",
        "RRR YYY G G RRR",
        "  R Y Y G G R R",
        "  R Y Y G G R R",
        "  R YYY G G RRR",
        "  R Y Y G G R R",
        "  R Y Y G G R R",
        "RR  Y Y  G  R R",
        "               ",
        "    222 2 2    ",
        "    2   2 2    ",
        "    2   2 2    ",
        "    222  2     ",
        "    2   2 2    ",
        "    2   2 2    ",
        "    2   2 2    ",
    };

    private static ObservableList<Integer> levelsOffsets;

    public static int getLevelsCount() {
        initLevelsOffsets();
        return levelsOffsets.size() - 1;
    }

    public static String[] getLevelData(int level) {
        initLevelsOffsets();
        if (level < 1 || level > getLevelsCount()) {
            return null;
        } else {
            return Arrays.copyOfRange(LEVELS_DATA, levelsOffsets.get(level - 1) + 1, levelsOffsets.get(level));
        }
    }

    private static void initLevelsOffsets() {
        if (levelsOffsets == null) {
            levelsOffsets = javafx.collections.FXCollections.<Integer>observableArrayList();
            levelsOffsets.add(-1);
            for (int i = 0; i < LEVELS_DATA.length; i++) {
                if (LEVELS_DATA[i].equals(NEXT_LEVEL)) {
                    levelsOffsets.add(i);
                }
            }
            levelsOffsets.add(LEVELS_DATA.length + 1);
        }
    }

}

