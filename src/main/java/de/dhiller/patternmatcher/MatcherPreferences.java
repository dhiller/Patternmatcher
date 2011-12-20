/*
 * Copyright (c) 2011, dhiller, http://www.dhiller.de Daniel Hiller, Warendorfer Str. 47, 48231 Warendorf, NRW,
 * Germany, All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided with the distribution. - Neither the
 * name of dhiller nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.dhiller.patternmatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

class MatcherPreferences {

    static final String TEST_STRING_TEXT_PREFERENCES_KEY = "testStringText"; //$NON-NLS-1$
    static final String PATTERN_TEXT_PREFERENCES_KEY = "patternText"; //$NON-NLS-1$

    private static final Preferences preferences = Preferences
	    .userNodeForPackage(PatternMatcher.class);

    static void storeTestStringText(final int index, final String newText) {
        preferences().put(MatcherPreferences.TEST_STRING_TEXT_PREFERENCES_KEY + index,
    	    newText);
    }

    static List<String> retrieveTestStringTexts() {
        int index = 0;
        final List<String> testStringTexts = new ArrayList<String>();
        String testStringTextFromPreferences;
        while (!(testStringTextFromPreferences = preferences().get(
		TEST_STRING_TEXT_PREFERENCES_KEY + index, "")).isEmpty()) {
            testStringTexts.add(testStringTextFromPreferences);
            index++;
        }
        return testStringTexts;
    }

    static void storePatternText(String patternText) {
	preferences().put(PATTERN_TEXT_PREFERENCES_KEY, patternText);
    }

    static String retrievePatternText() {
	return preferences().get(PATTERN_TEXT_PREFERENCES_KEY, "");
    }

    private static Preferences preferences() {
	return preferences;
    }

    static void removeTestStringText(int i) {
	storeTestStringText(i, "");
    }

    static void addTestString() {
	storeTestStringText(retrieveTestStringTexts().size(),
		"<your text goes here>");
    }

    static void removeTestString() {
	storeTestStringText(retrieveTestStringTexts().size() - 1, "");
    }

}