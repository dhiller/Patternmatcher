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

import java.awt.BorderLayout;
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

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class PatternMatcher extends JFrame {

    private static final Preferences prefs = Preferences
	    .userNodeForPackage(PatternMatcher.class);
    private final JTextField pattern = new JTextField();
    private final List<JTextArea> testStrings = new ArrayList<JTextArea>();
    private final JTextArea result = new JTextArea();
    private final JSplitPane centerSplitArea = new JSplitPane();
    private final JPanel northPanel = new JPanel();
    private final JPanel resultContainer = new JPanel();
    private final JPanel textAreaContainer = new JPanel();
    private final JPanel upper = new JPanel();
    private final JPanel upperButtons = new JPanel();
    private final JLabel labelTestString = new JLabel("Teststring");

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
	    prefs.put("testStringText" + testStringTexts().size(),
		    "new text here");
	    configureTextFieldsForPatternTest();
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
		result.setRows(estimatedRows(result));
	    } catch (Exception ex) {
		JTextArea textarea = new JTextArea(ex.getMessage());
		textarea.setFont(new Font("Monospaced", Font.PLAIN, 16));
		JOptionPane.showMessageDialog(PatternMatcher.this, textarea, ex
			.getClass().getName(), JOptionPane.ERROR_MESSAGE);
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
	configureTextFieldsAndResultArea();
	configureNorthPanel();
	configureUpperArea();
	configureLowerArea();
	configureTextFieldsForPatternTest();
    }

    private void configureTextFieldsAndResultArea() {
	centerSplitArea.setOrientation(JSplitPane.VERTICAL_SPLIT);
	add(centerSplitArea);
    }

    private void configureLowerArea() {
	resultContainer.setLayout(new GridBagLayout());
	result.setText("\n\n\n");
	resultContainer.add(new JScrollPane(result), new GridBagConstraints(0,
		0, 4, 1, 1.0, 1.0, GridBagConstraints.WEST,
		GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 20));
	centerSplitArea.setRightComponent(resultContainer);
    }

    private void configureUpperArea() {
	upper.setLayout(new GridBagLayout());
	upper.add(labelTestString, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
		GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
		new Insets(2, 2, 2, 2), 0, 0));
	upper.add(new JScrollPane(textAreaContainer), new GridBagConstraints(1,
		0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
		GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
	upperButtons.setLayout(new GridBagLayout());
	addAnotherTestStringFieldButtonOnFirstRow();
	addCheckButton();
	upper.add(upperButtons, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
		GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
		new Insets(2, 2, 2, 2), 0, 0));
	centerSplitArea.setLeftComponent(upper);
    }

    private void configureNorthPanel() {
	northPanel.setLayout(new GridBagLayout());
	add(northPanel, BorderLayout.NORTH);
	addPatternRow();
    }

    private void configureTextFieldsForPatternTest() {
	textAreaContainer.removeAll();
	textAreaContainer.setLayout(new GridBagLayout());
	testStrings.clear();
	for (String testStringText : testStringTexts()) {
	    final JTextArea testString = new JTextArea();
	    testString.setLineWrap(true);
	    testString.setText(testStringText);
	    testString.setColumns(80);
	    testString.setRows(estimatedRows(testString));
	    testString.getDocument().addDocumentListener(
		    new TextFieldSizeAdapter(testString));
	    testStrings.add(testString);
	    textAreaContainer.add(new JScrollPane(testString),
		    new GridBagConstraints(1, testStrings.size() - 1, 1, 1,
			    0.0, 0.0, GridBagConstraints.NORTHWEST,
			    GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2,
				    2), 0, 20));
	}
	textAreaContainer.add(new JPanel(), new GridBagConstraints(1,
		testStrings.size(), 1, 1, 0.0, 1.0,
		GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
		new Insets(2, 2, 2, 2), 0, 0));
	upper.invalidate();
	upper.revalidate();
	upper.repaint();
    }

    private List<String> testStringTexts() {
	int index2 = 0;
	final List<String> testStringTexts = new ArrayList<String>();
	String testStringTextFromPreferences;
	while (!(testStringTextFromPreferences = testStringTextFromPreferences(index2))
		.isEmpty()) {
	    testStringTexts.add(testStringTextFromPreferences);
	    index2++;
	}
	return testStringTexts;
    }

    private void addCheckButton() {
	upperButtons.add(new JButton(new CheckAction()),
		new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(2, 2, 2, 2), 0, 0));
    }

    private int estimatedRows(JTextArea text) {
	return (int) (((text.getText().length() / (text.getColumns() > 0 ? text
		.getColumns() : 80)) + 1 + text.getText()
		.replaceAll("[^\\n]+", "").length()) / 2);
    }

    private String testStringTextFromPreferences(int index) {
	return prefs.get("testStringText" + index, "");
    }

    private void addAnotherTestStringFieldButtonOnFirstRow() {
	upperButtons.add(new JButton(new AddAnotherTestStringField()),
		new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(2, 2, 2, 2), 0, 0));
    }

    private void addPatternRow() {
	northPanel.add(new JLabel("Pattern"), new GridBagConstraints(0, 0, 1,
		1, 0.0, 0.0, GridBagConstraints.WEST,
		GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
	pattern.setText(prefs.get("patternText", ""));
	northPanel.add(pattern, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
		new Insets(2, 2, 2, 2), 50, 0));
	northPanel.add(new JButton(new AddLevelToBackslashes()),
		new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(2, 2, 2, 2), 0, 0));
	northPanel.add(new JButton(new RemoveLevelFromBackslashes()),
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
