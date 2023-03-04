/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.core.network.proxy.pac;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.core.network.proxy.pac.datetime.PacUtilsDateTime;

/**
 *
 */
public class PacUtilsDateTimeTest {
    
    public PacUtilsDateTimeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isInWeekdayRange method, of class PacUtilsDateTime.
     */
    @Test
    public void testIsInWeekdayRange() throws Exception {
        System.out.println("isInWeekdayRange");
        Calendar cal = Calendar.getInstance();
        int weekdayNum = cal.get(Calendar.DAY_OF_WEEK);
        Date[] dates = new Date[7];
        dates[weekdayNum-1] = cal.getTime();
        for(int i=weekdayNum+1; i <= 7; i++) {
            cal.add(Calendar.DATE, 1);
            dates[i-1] = cal.getTime();
        }
        cal = Calendar.getInstance();
        for(int i=weekdayNum-1; i >=1; i--) {
            cal.add(Calendar.DATE, -1);
            dates[i-1] = cal.getTime();
        }
        
        Date nowSUN = dates[0];
        Date nowMON = dates[1];
        Date nowTUE = dates[2];
        Date nowWED = dates[3];
        Date nowTHU = dates[4];
        Date nowFRI = dates[5];
        Date nowSAT = dates[6];

        assertTrue(PacUtilsDateTime.isInWeekdayRange(nowMON, "MON"));
        assertFalse(PacUtilsDateTime.isInWeekdayRange(nowMON, "SUN"));
        assertTrue(PacUtilsDateTime.isInWeekdayRange(nowMON, "MON", "SUN"));
        assertTrue(PacUtilsDateTime.isInWeekdayRange(nowSAT, "FRI", "TUE"));
        assertTrue(PacUtilsDateTime.isInWeekdayRange(nowMON, "FRI", "TUE"));
        assertFalse(PacUtilsDateTime.isInWeekdayRange(nowTHU, "FRI", "TUE"));
        assertFalse(PacUtilsDateTime.isInWeekdayRange(nowTHU, "SAT", "MON"));
        assertTrue(PacUtilsDateTime.isInWeekdayRange(nowSUN, "SAT", "MON", "GMT"));
        assertFalse(PacUtilsDateTime.isInWeekdayRange(nowSUN, "MON", "TUE", "GMT"));
        
    }

    /**
     * Test of isInTimeRange method, of class PacUtilsDateTime.
     */
    @Test
    public void testIsInTimeRange() throws Exception {
        System.out.println("isInTimeRange");
        
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        
        assertTrue(PacUtilsDateTime.isInTimeRange(df.parse("13:00:00"), 13));
        assertFalse(PacUtilsDateTime.isInTimeRange(df.parse("14:00:00"), 13));
        assertTrue(PacUtilsDateTime.isInTimeRange(df.parse("14:00:00"), 13, 14));
        assertFalse(PacUtilsDateTime.isInTimeRange(df.parse("14:01:00"), 13, 14));
        assertTrue(PacUtilsDateTime.isInTimeRange(df.parse("14:00:00"), 13, 30, 14, 30));
        assertFalse(PacUtilsDateTime.isInTimeRange(df.parse("14:00:00"), 13, 30, 13, 59));
        assertTrue(PacUtilsDateTime.isInTimeRange(df.parse("14:00:00"), 13, 30, 14, 0));
        assertTrue(PacUtilsDateTime.isInTimeRange(df.parse("14:00:20"), 13, 30, 23, 14, 0, 21));
        assertTrue(PacUtilsDateTime.isInTimeRange(df.parse("14:00:20"), 13, 30, 23, 14, 0, 20));
        assertFalse(PacUtilsDateTime.isInTimeRange(df.parse("14:00:20"), 13, 30, 23, 14, 0, 19));
        assertFalse(PacUtilsDateTime.isInTimeRange(df.parse("14:00:20"), 13, 30, 23, 14, 0, 19, "GMT"));
        
        assertTrue(PacUtilsDateTime.isInTimeRange(df.parse("23:12:20"), 22, 4));
        
    }

    /**
     * Test of isInDateRange method, of class PacUtilsDateTime.
     */
    @Test
    public void testIsInDateRange() throws Exception {
        System.out.println("isInDateRange");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-02-28"), "FEB"));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-02-28"), "MAR", "JUN"));
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-02-28"), "DEC", "MAR"));
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-12-28"), "DEC", "MAR"));
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-03-28"), "DEC", "MAR"));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-11-28"), "DEC", "MAR"));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-04-28"), "DEC", "MAR"));

        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-04-28"), 20, 30));
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-04-28"), 28));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-04-28"), 27));
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-04-28"), 27, 28));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-04-28"), 10, 12));

        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-04-28"), 2016));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-04-28"), 2017));
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-04-28"), 2016, 2020));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-04-28"), 2012, 2015));
        
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-04-28"), 15, "FEB", 31, "MAR"));
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-03-28"), 15, "FEB", 31, "MAR"));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-03-28"), 15, "NOV", 31, "JAN"));
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-12-28"), 15, "NOV", 31, "JAN"));
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-01-28"), 15, "NOV", 31, "JAN"));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-02-01"), 15, "NOV", 31, "JAN"));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-11-01"), 15, "NOV", 31, "JAN"));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-09-16"), 15, "NOV", 31, "JAN"));
        
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2016-11-01"), "NOV", 2016, "JAN", 2017));
        assertTrue(PacUtilsDateTime.isInDateRange(df.parse("2017-01-26"), "NOV", 2016, "JAN", 2017));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2016-10-30"), "NOV", 2016, "JAN", 2017));
        assertFalse(PacUtilsDateTime.isInDateRange(df.parse("2017-02-01"), "NOV", 2016, "JAN", 2017));
    }


    
}
