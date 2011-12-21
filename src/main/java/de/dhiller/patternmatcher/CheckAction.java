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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

final class CheckAction extends AbstractAction {

    final PatternMatcher patternMatcher;
    final StringBuilder result = new StringBuilder();

    CheckAction(PatternMatcher patternMatcher) {
	super("Check match");
	this.patternMatcher = patternMatcher;
    }

    public void actionPerformed(ActionEvent e) {
	try {
	    String patternText = patternMatcher.patternText();
	    MatcherPreferences.storePatternText(patternText);
	    final List<String> testStringTexts = patternMatcher
		    .testStringTexts();
	    for (int i = 0, n = testStringTexts.size(); i < n; i++) {
		String testStringText = testStringTexts.get(i);
		if (testStringText.trim().isEmpty()) {
		    MatcherPreferences.removeTestStringText(i);
		    continue;
		}
		MatcherPreferences.storeTestStringText(i, testStringText);
		Pattern compiledPattern = Pattern.compile(patternText);
		Matcher matcher = compiledPattern.matcher(testStringText);
		String resultLabel = "Field "
			+ (i + 1)
			+ ": "
			+ testStringText.substring(0,
				Math.min(testStringText.length(), 20))
			+ (testStringText.length() >= 20 ? "..." : "");
		result.append("--- matches() for " + resultLabel + " ---\n");
		if (!matcher.matches()) {
		    result.append("string does not match regex\n");
		} else {
		    appendMatchedGroups(matcher);
		}
		Matcher matcher2 = compiledPattern.matcher(testStringText);
		result.append("\n--- find() for " + resultLabel + " ---\n");
		boolean find = matcher2.find();
		if (!find) {
		    result.append("no matches found\n");
		}
		int iteration = 1;
		do {
		    if (find) {
			result.append("Iteration " + iteration++ + ":\n");
			appendMatchedGroups(matcher2);
			result.append("\n");
		    }
		} while (find = matcher2.find());
		result.append("\n");
	    }
	    PatternMatcher r = this.patternMatcher;
	    this.patternMatcher.result.setRows(TextComponentUtilities
		    .estimatedRows(this.patternMatcher.result));
	    showResult();
	} catch (Exception ex) {
	    JTextArea textarea = new JTextArea(ex.getMessage());
	    textarea.setFont(new Font("Monospaced", Font.PLAIN, 16));
	    JOptionPane.showMessageDialog(this.patternMatcher, textarea, ex
		    .getClass().getName(), JOptionPane.ERROR_MESSAGE);
	}
    }

    void showResult() {
	patternMatcher.showResult(result.toString());
    }

    private void appendMatchedGroups(Matcher matcher) {
	result.append("Capturing group " + 0 + ": " + matcher.group() + "\n");
	for (int i = 1, n = matcher.groupCount() + 1; i < n; i++) {
	    result.append("Capturing group " + i + ": " + matcher.group(i)
		    + "\n");
	}
    }
}