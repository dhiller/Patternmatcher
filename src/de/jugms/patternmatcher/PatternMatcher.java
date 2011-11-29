package de.jugms.patternmatcher;


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

public class PatternMatcher extends JFrame {

    private static final Preferences prefs = Preferences.userNodeForPackage(PatternMatcher.class);
    private final JTextField pattern = new JTextField();
    private final List<JTextArea> testStrings = new ArrayList<JTextArea>();
    private final JTextArea result = new JTextArea();

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
                pack();
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
                new JButton(new AbstractAction("\\\\") {

            @Override
            public void actionPerformed(ActionEvent e) {
                pattern.setText(pattern.getText().replace("\\", "\\\\"));
            }
        }),
                new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        getContentPane().add(
                new JButton(new AbstractAction("\\") {

            @Override
            public void actionPerformed(ActionEvent e) {
                pattern.setText(pattern.getText().replace("\\\\", "\\"));
            }
        }),
                new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        getContentPane().add(
                new JLabel("Teststring"),
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHEAST,
                GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2),
                0, 0));
        int index = 0;
        String testStringText = "";
        testStrings.clear();
        do {
            testStringText = prefs.get("testStringText" + index, "");
			JTextArea testString = new JTextArea();
			testString.setLineWrap(true);
			testString.setText(testStringText);
            testStrings.add(testString);
            getContentPane().add(
                    new JScrollPane(testString),
                    new GridBagConstraints(1, 1 + index, 3, 1, 0.1, 0.1,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH,
                    new Insets(2, 2, 2, 2), 0, 0));
            if (index == 0) {
                getContentPane().add(
                        new JButton(new AbstractAction("+") {

                    {
                        putValue(AbstractAction.LONG_DESCRIPTION,
                                "Add another text area");
                    }

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        prefs.put(
                                "testStringText" + testStrings.size(),
                                "new text here");
                        PatternMatcher.this.getContentPane().removeAll();
                        populateFrame();
                    }
                }),
                        new GridBagConstraints(4, 1 + index, 3, 1, 0.0, 0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));
            }
            index++;
        } while (testStringText != null && !testStringText.trim().isEmpty());
        getContentPane().add(
                new JButton(new CheckAction()),
                new GridBagConstraints(2, 2 + index, 2, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        getContentPane().add(
                new JScrollPane(result),
                new GridBagConstraints(0, 3 + index, 4, 1, 0.0, 0.5,
                GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 250, 100));
        pack();
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
