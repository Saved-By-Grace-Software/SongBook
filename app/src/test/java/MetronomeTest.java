package com.sbgsoft.songbook.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sbgsoft.songbook.songs.Metronome;
import com.sbgsoft.songbook.songs.TimeSignature;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MetronomeTest {
    @Test
    public void testCalculateTickTimeFromBPM() {
        // Test setup
        Metronome mTest = new Metronome();
        HashMap testValues = new HashMap();     // Key = BPM, Value = Delay
        TimeSignature tSig = new TimeSignature("4/4");

        // Add the test values for 4/4 time
        testValues.put(120, 500);
        testValues.put(60, 1000);
        testValues.put(130, 462);
        testValues.put(74, 811);
        testValues.put(179, 335);

        // Run the test
        Iterator it = testValues.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            int calcDelayTime = mTest.calculateTickDelayFromBPM((int) pair.getKey(), tSig);
            assertEquals("Failed to calculate delay time correctly for " + pair.getKey() + " BPM", (int) pair.getValue(), calcDelayTime);
        }

        // Change time signature to 3/8
        testValues.clear();
        tSig = new TimeSignature("3/8");

        // Add the test values for 3/8 time
        testValues.put(120, 167);
        testValues.put(60, 333);
        testValues.put(130, 154);
        testValues.put(74, 270);
        testValues.put(179, 112);

        // Run the test
        it = testValues.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            int calcDelayTime = mTest.calculateTickDelayFromBPM((int) pair.getKey(), tSig);
            assertEquals("Failed to calculate delay time correctly for " + pair.getKey() + " BPM", (int) pair.getValue(), calcDelayTime);
        }
    }
}
