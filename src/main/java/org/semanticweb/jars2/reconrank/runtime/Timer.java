/*
 * Created on 04-Jul-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.semanticweb.jars2.reconrank.runtime;

/**
 * @author TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
//Timer class

public class Timer {
    long t;

    // constructor

    public Timer() {
        reset();
    }

    // reset timer

    public void reset() {
        t = System.currentTimeMillis();
    }

    // return elapsed time

    public long elapsed() {
        return System.currentTimeMillis() - t;
    }

    // print explanatory string and elapsed time

    public void print(String s) {
        System.out.println(s + ": " + elapsed() + " ms");
    }
}
