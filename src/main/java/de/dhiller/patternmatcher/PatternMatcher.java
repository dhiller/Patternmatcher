/*
 * Copyright (c) 2006-2011, dhiller, http://www.dhiller.de
 * Daniel Hiller, Warendorfer Str. 47, 48231 Warendorf, NRW, Germany, 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this 
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * - Neither the name of dhiller nor the names of its
 *   contributors may  
 *   be used to endorse or promote products derived from this software without 
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.dhiller.patternmatcher;


import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class PatternMatcher extends JFrame {

    private static final Preferences prefs = Preferences.userNodeForPackage(PatternMatcher.class);
    private final JTextField pattern = new JTextField();
    private final List<JTextArea> testStrings = new ArrayList<JTextArea>();
    private final JTextArea result = new JTextArea();

    private final class TextFieldSizeAdapter implements DocumentListener {
	private final JTextArea testString;

	private TextFieldSizeAdapter(JTextArea testString) {
	    this.testString = testString;
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	    updateRows(testString);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		    updateRows(testString);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		    updateRows(testString);
	}

	private void updateRows(JTextArea testString) {
	    testString.setRows(estimatedRows(testString));
		    pack();
	}
    }

    private final class AddAnotherTestStringField extends AbstractAction {

	private AddAnotherTestStringField() {
	    super("+");
	    putValue(AbstractAction.LONG_DESCRIPTION, "Add another text area");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    prefs.put(
	            "testStringText" + testStrings.size(),
	            "new text here");
	    PatternMatcher.this.getContentPane().removeAll();
	    populateFrame();
	}
    }

    private final class RemoveLevelFromBackslashes extends AbstractAction {
	private RemoveLevelFromBackslashes() {
	    super("\\");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    pattern.setText(pattern.getText().replace("\\\\", "\\"));
	}
    }

    private final class AddLevelToBackslashes extends AbstractAction {
	private AddLevelToBackslashes() {
	    super("\\\\");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    pattern.setText(pattern.getText().replace("\\", "\\\\"));
	}
    }

    private final class CheckAction extends AbstractAction {

        private CheckAction() {
            super("Check match");
        }

        public void actionPerformed(ActionEvent e) {
			result.setLineWrap(true);
            result.setText("");
            try {
                String patternText = pattern.getText();
                prefs.put("patternText", patternText);
                final List<String> testStringTexts = new ArrayList<String>();
                for (int i = 0, n = testStrings.size(); i < n; i++) {
                    final JTextArea t = testStrings.get(i);
                    String testStringText = t.getText();
                    if (testStringText.trim().isEmpty()) {
                        prefs.put("testStringText" + i, "");
                        continue;
                    }
                    testStringTexts.add(testStringText);
                    prefs.put("testStringText" + i, testStringText);
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
                    result.append("\n--- find() for " + resultLabel
                            + " ---\n");
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
		result.setRows(estimatedLines(result.getText()));
		// pack();
            } catch (Exception ex) {
                JTextArea textarea = new JTextArea(ex.getMessage());
                textarea.setFont(new Font("Monospaced", Font.PLAIN, 16));
                JOptionPane.showMessageDialog(PatternMatcher.this, textarea, ex.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            }
        }

        private void appendMatchedGroups(Matcher matcher) {
            result.append("Capturing group " + 0 + ": " + matcher.group()
                    + "\n");
            for (int i = 1, n = matcher.groupCount() + 1; i < n; i++) {
                result.append("Capturing group " + i + ": " + matcher.group(i)
                        + "\n");
            }
        }
    }

    private PatternMatcher() {
        super("Regex Matcher");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        populateFrame();
    }

    private void populateFrame() {
        addPatternRow();
        addTestStringsLabel();
        int index = 0;
        String testStringText = "";
        testStrings.clear();
        do {
            testStringText = testStringTextFromPreferences(index);
			final JTextArea testString = new JTextArea();
			testString.setLineWrap(true);
			testString.setText(testStringText);
	    testString.setColumns(80);
	    testString.setRows(estimatedRows(testString));
	    testString.getDocument().addDocumentListener(new TextFieldSizeAdapter(testString));
            testStrings.add(testString);
            getContentPane().add(
                    new JScrollPane(testString),
		    new GridBagConstraints(1, 1 + index, 3, 1, 1.0, 0.0,
			    GridBagConstraints.WEST,
			    GridBagConstraints.HORIZONTAL,
			    new Insets(2, 2, 2, 2), 0, 20));
            if (index == 0) {
                addAnotherTestStringFieldButtonOnFirstRow();
            }
            index++;
	} while (!testStringText.trim().isEmpty());
        getContentPane().add(
                new JButton(new CheckAction()),
                new GridBagConstraints(2, 2 + index, 2, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
	result.setText("\n\n\n");
	result.setRows(estimatedLines(result.getText()));
        getContentPane().add(
                new JScrollPane(result),
		new GridBagConstraints(0, 3 + index, 4, 1, 1.0, 1.0,
			GridBagConstraints.WEST, GridBagConstraints.BOTH,
			new Insets(2, 2, 2, 2), 0, 20));
        pack();
    }

    private int estimatedLines(String text) {
	return (int) (((text.length() / 80) + 1 + text
		.replaceAll("[^\\n]+", "").length()) / 2);
    }

    private int estimatedRows(JTextArea text) {
	return (int) (((text.getText().length() / text.getColumns()) + 1 + text
		.getText().replaceAll("[^\\n]+", "").length()) / 2);
    }

    private String testStringTextFromPreferences(int index) {
	return prefs.get("testStringText" + index, "");
    }

    private void addAnotherTestStringFieldButtonOnFirstRow() {
	getContentPane().add(
	        new JButton(new AddAnotherTestStringField()),
		new GridBagConstraints(4, 1, 3, 1, 0.0, 0.0,
	        GridBagConstraints.WEST,
	        GridBagConstraints.NONE,
	        new Insets(2, 2, 2, 2), 0, 0));
    }

    private void addTestStringsLabel() {
	getContentPane().add(
                new JLabel("Teststring"),
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHEAST,
                GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2),
                0, 0));
    }

    private void addPatternRow() {
	getContentPane().add(
                new JLabel("Pattern"),
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        pattern.setText(prefs.get("patternText", ""));
        getContentPane().add(
                pattern,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 50, 0));
        getContentPane().add(
                new JButton(new AddLevelToBackslashes()),
                new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        getContentPane().add(
		new JButton(new RemoveLevelFromBackslashes()),
                new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                PatternMatcher patternMatcher = new PatternMatcher();
				patternMatcher.setVisible(true);
				patternMatcher.pack();
            }
        });
    }
}
