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

package termtester;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import tests.*;

/**
 *
 * @author ivan
 */
public abstract class Test {
    private static final Map<String, Test> tests = new HashMap<String, Test>();

    protected final String name;
    protected final Context context;
    protected final int minArg;
    protected final int maxArg;
    protected final boolean compass;
    protected final Util.FillPattern fill;

    private String info;

    @SuppressWarnings("LeakingThisInConstructor")
    protected Test(String name, Context context, int minArg, int maxArg, boolean compass, Util.FillPattern fill) {
        this.name = name;
        this.context = context;
        this.minArg = minArg;
        this.maxArg = maxArg;
        this.compass = compass;
        this.fill = fill;
        tests.put(name, this);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void init(Context context) {

        new Test_acs(context);
        new Test_al(context);
        new Test_attr(context);
        new Test_bc(context);
        new Test_cd(context);
        new Test_ce(context);
        new Test_cha(context);
        new Test_cm(context);
        new Test_cud(context);
        new Test_cuu(context);
        new Test_cv(context);
        new Test_ech(context);
        new Test_ed(context);
        new Test_ed(context);
        new Test_el(context);
        new Test_el2(context);
        new Test_font(context);
        new Test_dc(context);
        new Test_dl(context);
        new Test_do(context);
        new Test_ic(context);
        new Test_im(context);
        new Test_key(context);
        new Test_lf(context);
        new Test_misc(context);
        new Test_nd(context);
        new Test_om(context);
        new Test_overstrike(context);
        new Test_reverse(context);
        new Test_ri(context);
        new Test_scrc(context);
        new Test_tab(context);
        new Test_txtprm(context);
        new Test_up(context);
        new Test_vpa(context);
    }

    public static Collection<Test> tests() {
        return tests.values();
    }

    public static Test find(String name) {
        return tests.get(name);
    }

    public abstract void runBasic(String[] args);

    public void runPrefix() {
        // no-op
    }

    public String info() {
        return info;
    }

    protected final void info(String info) {
        this.info = info;
    }

    public void run(String[] args) {
        if (compass)
            runCompass(args);
        else
            runSimple(args);
    }

    public void runCompass(String[] args) {
        context.interp.printf("Running test '%s' on all compass points ...\n", name);

        Context.Margin margin = context.getMargin();

        if (true) {
            for (Util.Direction dir : Util.Direction.values()) {
                context.sendQuiet("\\ESCc");            // full reset
                runPrefix();
                Util.fill(context, fill);
                if (margin != null)
                    context.sendQuiet("\\ESC[%d;%dr", margin.low, margin.hi);
                Util.go(context, dir);
                runBasic(args);
                context.pause();
            }
        }
        if (margin != null) {
            for (Util.MarginDirection dir : Util.MarginDirection.values()) {
                context.sendQuiet("\\ESCc");            // full reset
                runPrefix();
                Util.fill(context, fill);
                if (margin != null)
                    context.sendQuiet("\\ESC[%d;%dr", margin.low, margin.hi);
                if (!Util.go(context, dir))
                    continue;
                runBasic(args);
                context.pause();
            }
        }

        context.interp.printf("Done\n");
    }

    public void runSimple(String[] args) {
        context.interp.printf("Running test '%s' ...\n", name);
        context.sendQuiet("\\ESCc");            // full reset
        runPrefix();
        Util.fill(context, fill);
        runPrefix();
        runBasic(args);
        context.interp.printf("Done\n");
    }

    protected static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
