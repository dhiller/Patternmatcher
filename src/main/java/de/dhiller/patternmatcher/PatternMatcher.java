/*
 * Copyright (c) 2011, dhiller, http://www.dhiller.de
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.text.JTextComponent;

public class PatternMatcher extends JFrame {

    final JTextField pattern = new JTextField();
    final List<JTextArea> testStrings = new ArrayList<JTextArea>();
    final JTextArea result = new JTextArea();
    private final JSplitPane centerSplitArea = new JSplitPane();
    private final JPanel northPanel = new JPanel();
    private final JPanel resultContainer = new JPanel();
    private final JPanel textAreaContainer = new JPanel();
    private final JPanel upper = new JPanel();
    private final JPanel upperButtons = new JPanel();
    private final JLabel labelTestString = new JLabel("Teststring");

    private PatternMatcher() {
	super("Regex Matcher");
	setDefaultCloseOperation(EXIT_ON_CLOSE);
	configureTextFieldsAndResultArea();
	configurePatternInputArea();
	configureTestStringsInputArea();
	configureLowerArea();
	reconfigureTextFieldsForPatternTest();
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

    void reconfigureTextFieldsForPatternTest() {
	textAreaContainer.removeAll();
	textAreaContainer.setLayout(new GridBagLayout());
	testStrings.clear();
	for (String testStringText : MatcherPreferences.retrieveTestStringTexts()) {
	    final JTextArea testString = newTestStringArea(testStringText);
	    testStrings.add(testString);
	    textAreaContainer.add(new JScrollPane(testString),
		    new GridBagConstraints(1, testStrings.size() - 1, 1, 1,
			    1.0, 0.0, GridBagConstraints.NORTHWEST,
			    GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2,
				    2), 0, 0));
	}
	addFillingPanel();
	upper.invalidate();
	upper.revalidate();
	upper.repaint();
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

    private void configureTestStringsInputArea() {
	upper.setLayout(new GridBagLayout());
	upper.add(labelTestString, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
		GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
		new Insets(2, 2, 2, 2), 0, 0));
	upper.add(new JScrollPane(textAreaContainer), new GridBagConstraints(1,
		0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
		GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
	upperButtons.setLayout(new GridBagLayout());
	addAnotherTestStringFieldButtonOnFirstRow();
	addRemoveTestStringFieldButtonOnFirstRow();
	addCheckButton();
	upper.add(upperButtons, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
		GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
		new Insets(2, 2, 2, 2), 0, 0));
	centerSplitArea.setLeftComponent(upper);
    }

    private void configurePatternInputArea() {
	northPanel.setLayout(new GridBagLayout());
	add(northPanel, BorderLayout.NORTH);
	northPanel.add(new JLabel("Pattern"), new GridBagConstraints(0, 0, 1,
		1, 0.0, 0.0, GridBagConstraints.WEST,
		GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
	pattern.setText(MatcherPreferences.retrievePatternText());
	northPanel.add(pattern, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
		GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
		new Insets(2, 2, 2, 2), 50, 0));
	northPanel.add(new JButton(new AddLevelToBackslashes(this.pattern)),
		new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(2, 2, 2, 2), 0, 0));
	northPanel.add(new JButton(new RemoveLevelFromBackslashes(this.pattern)),
		new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(2, 2, 2, 2), 0, 0));
    }

    private JTextArea newTestStringArea(String testStringText) {
	final JTextArea testString = new JTextArea();
	testString.setLineWrap(true);
	testString.setText(testStringText);
	testString.setColumns(80);
	testString.setRows(TextComponentUtilities.estimatedRows(testString));
	testString.getDocument().addDocumentListener(
		new TextFieldSizeAdapter(this, testString));
	return testString;
    }

    private void addFillingPanel() {
	textAreaContainer.add(new JPanel(), new GridBagConstraints(1,
		testStrings.size(), 1, 1, 0.0, 1.0,
		GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
		new Insets(2, 2, 2, 2), 0, 0));
    }

    private void addCheckButton() {
	upperButtons.add(new JButton(new CheckAction(this)),
		new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(2, 2, 2, 2), 0, 0));
    }

    private void addAnotherTestStringFieldButtonOnFirstRow() {
	upperButtons.add(new JButton(new AddAnotherTestStringField(this)),
		new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(2, 2, 2, 2), 0, 0));
    }

    private void addRemoveTestStringFieldButtonOnFirstRow() {
	final RemoveTestStringField a = new RemoveTestStringField(this);
	upperButtons.add(new JButton(a), new GridBagConstraints(1, 0, 1, 1,
		0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
		new Insets(2, 2, 2, 2), 0, 0));
    }

}
