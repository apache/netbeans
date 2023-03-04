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
 * AveragedNameValueLogElement.java
 *
 * Created on October 15, 2002, 4:59 PM
 */

package org.netbeans.performance.spi;
import org.netbeans.performance.spi.html.*;
/** A NameValueLogElement subclass which can take an array of
 * <code>ValueLogElement</code> or <code>NameValueLogElement</code>
 * values, and represent the average of the value and other statistical
 * information.  Assumes that the passed elements have value properties
 * that resolve to <code>Number</code> or one of its subclasses.
 * @author  Tim Boudreau
 */
public class AveragedNameValueLogElement extends NameValueLogElement implements Average {

    /** Creates a new instance of AveragedNameValueLogElement, using
     * the specified name.  The value returned by this instance
     * will be the average of the values provided by the array of
     * ValueLogElements passed to this constructor.<P>The default
     * implementation assumes that the values of all of the passed
     * ValueLogElements will be of the class Number.  If this is not
     * the case, subclass this class and provide an appropriate 
     * implementation of averageElements().<P>Note that no consistency
     * check is performed as to whether subclasses of ValueLogElement 
     * passed to it are of the same subclass or represent the same thing.
     */
    public AveragedNameValueLogElement(String name, ValueLogElement[] toAverage) {
        super (name);
        parsed = true;
        averageElements (toAverage);
    }
    
    /**A convenience constructor that creates a new instance of AveragedNameLogElement,
     * assigning the resulting instance's name the name of the passed 
     * <code>toAverage[0]</code>.  Since it is usually useless to average
     * apples and oranges, this should suffice. */
    public AveragedNameValueLogElement(NameValuePairLogElement[] toAverage) {
        this (toAverage[0].getName(), toAverage);
    }
 
    float[] samples;
    /**Averages the array of elements passed to the constructor and sets
     * fields accordingly. */
    protected void averageElements (ValueLogElement[] el) {
        float val=0;
        float variance=0;
        samples = new float[el.length];
        for (int i=0; i < el.length; i++) {
            samples[i] = ((Number) el[i].getValue()).floatValue();
            val+= samples[i];
        }
        java.util.Arrays.sort (samples);
        val = val/el.length;
        value = new Float (val);
    }
    
    /** Returns the lowest value in the set of values
     * used to create this element.
     * the set of values used to create this element. */
    public Float getMin() {
        return samples[0];
    }
    
    /** Returns the highest value in the set of values
     * used to create this element.
     * the set of values used to create this element. */
    public Float getMax() {
        return samples[samples.length-1];
    }
    
    /** Returns the mean, or arithmetic average of 
     * the set of values used to create this element. */
    public Float getMean() {
        return (Float) getValue();
    }
    
    /** Returns the median value of the entry set used to create
     * this element */
    public Float getMedian() {
        float result;
        //if it's not even, average the two middle elements
        if ((samples.length % 2) == 0) {
            result = samples[(samples.length/2)-1];
            result = (result + samples[(samples.length/2)]) / 2 ;
        } else {
            result = samples[samples.length/2];
        }
        return result;
    }
    
    /** Returns the values used to create this element.     */
    public float[] getSamples() {
        return samples;
    }
    
    /**Returns the standard deviation of the statistics used to
     * create this element. */
    public Double getStandardDeviation() {
        float mean = ((Float) getValue()).floatValue();
        double result=0;
        for (int i=0; i < samples.length; i++) {
            result += (samples[i] - mean) * (samples[i] - mean);
        }
        result = Math.sqrt(result / (samples.length - 1));
        return new Double (result);
    }
    
    /**Returns the standard deviation as a percentage of 
     * the mean. */
    public Float getVariance() {
        double sd = getStandardDeviation().doubleValue();
        double result = (sd / getMean().floatValue()) * 100;
        return new Float (result);
    }
    
    /** Returns the maximum percentage of the mean this 
     * value varies by, calculated as<P>
     * <code>((Math.abs (mean - (Math.max (min, max)))/mean) * 100
     *</code> */
    public Float getMaxVariance() {
        float biggest = Math.max (samples[0], samples[samples.length-1]);
        float mean = ((Float) value).floatValue();
        float result = ((Math.abs (mean - biggest))/mean) * 100; 
        return new Float (result);
    }

    public String toString() {
        StringBuffer result = new StringBuffer (name);
        result.append ("=");
        result.append (value);
        result.append (", Median=");
        result.append (getMedian());
        result.append (", Standard Deviation=");
        result.append (getStandardDeviation());
        result.append (", min=");
        result.append (getMin());
        result.append (", max=");
        result.append (getMax());
        result.append (", Variance=");
        result.append (getVariance());
        result.append ("%");
        result.append (", MaxVariance=");
        result.append (getMaxVariance());
        result.append ("%");
        result.append (", samples: [");
        for (int i=0; i < samples.length; i++) {
            result.append (Float.toString(samples[i]));
            if (i+1!=samples.length) result.append (",");
        }
        result.append ("]");
        return result.toString();
    }
    
    /** Create an HTML representation of this element's data (creates
     * a table with all of the properties).
     */
    public HTML toHTML () {
        HTMLTable result = new HTMLTable(name, 2);
        result.add ("Average (mean)");
        result.add (getMean().toString());
        result.add ("Median");
        result.add (getMedian().toString());
        result.add ("Standard deviation");
        result.add (getStandardDeviation().toString());
        result.add ("Variance (standard deviation as a percentage of mean)");
        result.add (getVariance().toString());
        result.add ("Max variance from mean");
        result.add (getMaxVariance().toString() + "%");
        result.add ("Minimum/Maximum value");
        result.add (getMin() + "/" + getMax());
        return (HTML) result;
    }
    
    /** Test execution method for debugging */
    public static void main (String args[]) {
        NameValueLogElement[] data = new NameValueLogElement[] {
            /*
            new NameValueLogElement ("MyAverage", new Float (95)),
            new NameValueLogElement ("1", new Float (93)),
            new NameValueLogElement ("1", new Float (88)),
            new NameValueLogElement ("1.2", new Float (91)),
            new NameValueLogElement ("1.2", new Float (89)),
             */
            
//            new NameValueLogElement ("1000", new Float (6)),
            new NameValueLogElement ("MyAverage", new Float (1)),
            new NameValueLogElement ("1", new Float (2)),
            new NameValueLogElement ("1", new Float (3)),
            new NameValueLogElement ("1.2", new Float (4)),
            new NameValueLogElement ("1.2", new Float (5)),
            new NameValueLogElement ("1000", new Float (6)),
            new NameValueLogElement ("1000", new Float (7)),

        };
        System.out.println(new AveragedNameValueLogElement(data).toString());
    }
    
    /**Determine if two averaged elements have a statistically insignificant
     * difference in mean value. */
    public static boolean equalWithinStandardDeviation (AveragedNameValueLogElement a, AveragedNameValueLogElement b) {
        AveragedNameValueLogElement greater = a.getMean().floatValue() > b.getMean().floatValue() ? a : b;
        AveragedNameValueLogElement lesser = greater == a ? b : a;
        float greaterMinValue = greater.getMean().floatValue() + greater.getStandardDeviation().floatValue();
        float lesserMaxValue = lesser.getMean().floatValue() - lesser.getStandardDeviation().floatValue();
        return lesserMaxValue > greaterMinValue;
    }
    
}
