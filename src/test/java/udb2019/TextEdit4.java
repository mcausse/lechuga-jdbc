
package udb2019;
/* TextEdit.java - a simple text editor - Matt Mahoney

This program demonstrates simple text editing, menus, and load/save
operations.  The program displays a menu bar with one menu (File) with
5 menu items:

New  - clears the screen.
Open - prompts the user to enter a file name and loads it.
Save - saves the file using the name specified in the last "Open" or
       "Save As" command.  If there is no such name, then prompt for
       a name as in "Save As".
Save As - Prompts the user to enter a file name and saves the file.
Exit - quits the program.

"Open" and "Save As" use a dialog box that allows the user to navigate
directories (a JFileChooser) to select a file.  The user has the option
to cancel, in which case the screen and disk are not modified and the
current name of the file is unchanged.  (The program is not exited).

If the user specifies a nonexistent or unreadable file in "Open", then
the operation is cancelled.  If the user specifies an invalid file name
or tries to write to a file without write permission in "Save" or
"Save As", then the operation is cancelled.  In both cases, an error
dialog appears and the current name of the file (if any) is unchanged.

Unlike most text editors, the program does not bother to ask the user
if he/she wishes to save the screen content before "New", "Open" or "Exit"
if it has been modified since the last "Save" or "Save As" operation.
*/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

public class TextEdit4 extends JFrame implements ActionListener {

    private static final long serialVersionUID = 2054269406974939700L;

    final Color fontColor = Color.BLACK;
    final Color cursorColor = Color.RED;
    final Color backgroundColor = Color.WHITE;

    private final JTextArea textArea = new JTextArea();
    private final JMenu fileMenu = new JMenu("File");
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenuItem newItem = new JMenuItem("New");
    private final JMenuItem openItem = new JMenuItem("Open");
    private final JMenuItem saveItem = new JMenuItem("Save");
    private final JMenuItem saveAsItem = new JMenuItem("Save As");
    private final JMenuItem exitItem = new JMenuItem("Exit");

    JButton lineWrapButton;

    private String filename = null; // set by "Open" or "Save As"

    public static void main(String args[]) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        new TextEdit4();
    }

    // Constructor: create a text editor with a menu
    public TextEdit4() {
        super("Text Editor");

        // Create menu and add listeners
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(exitItem);
        newItem.addActionListener(this);
        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        saveAsItem.addActionListener(this);
        exitItem.addActionListener(this);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        lineWrapButton = new JButton("Line wrap");
        lineWrapButton.setMargin(new Insets(0, 0, 0, 0));
        lineWrapButton.addActionListener(this);
        menuBar.add(lineWrapButton);

        {
            textArea.setLineWrap(true);
            Font font = new Font("Monospaced", Font.BOLD, 12);
            textArea.setFont(font);
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane);

        TextLineNumber tln = new TextLineNumber(textArea);
        scrollPane.setRowHeaderView(tln);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle bounds = env.getMaximumWindowBounds();
            // System.out.println("Screen Bounds: " + bounds);
            setSize(new Dimension(bounds.width / 2/* FIXME */, bounds.height / 2/* FIXME */));
        }

        textArea.setEditable(true);

        // textArea.setSelectionColor(Color.BLUE);
        // textArea.setSelectedTextColor(Color.GREEN);
        textArea.setBackground(backgroundColor);

        // loadFile("/home/mhoms/tableman.properties");
        loadFile("/home/mhoms/dbman.script");
        // loadFile("d:/c.properties");

        UIManager.put("Caret.width", 3);
        DefaultCaret c = new DefaultCaret();
        textArea.setCaret(c);

        textArea.getCaret().setBlinkRate(500);

        textArea.setCaretColor(cursorColor);
        textArea.setCaretPosition(0);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new MyKeyEventDispatcher(textArea));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
                textArea.getCaret().setVisible(true);
                textArea.requestFocus();
            }
        });
    }

    static class MyKeyEventDispatcher implements KeyEventDispatcher {

        final JTextArea textArea;
        final JTextAreaCursorUtils utils;

        static class JTextAreaCursorUtils {

            final JTextArea textArea;

            public JTextAreaCursorUtils(JTextArea textArea) {
                super();
                this.textArea = textArea;
            }

            public int getLineNum() {
                try {
                    return textArea.getLineOfOffset(textArea.getCaretPosition());
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }

            public int getColumnNum() {
                try {
                    return textArea.getCaretPosition() - textArea.getLineStartOffset(getLineNum());
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }

            public int getLineLength() {
                try {
                    return textArea.getLineEndOffset(getLineNum()) - 1 - textArea.getLineStartOffset(getLineNum());
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }

            public void gotoLineBegin() {
                try {
                    textArea.setCaretPosition(textArea.getLineStartOffset(getLineNum()));
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }

            public void gotoLineEnd() {
                try {
                    textArea.setCaretPosition(textArea.getLineEndOffset(getLineNum()) - 1);
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
            }

            public void dec() {
                textArea.setCaretPosition(textArea.getCaretPosition() - 1);
            }

            public void inc() {
                textArea.setCaretPosition(textArea.getCaretPosition() + 1);
            }

            public void inc(int n) {
                textArea.setCaretPosition(textArea.getCaretPosition() + n);
            }

            public int getLineCount() {
                // -1 pq és 1-based
                return textArea.getLineCount() - 1;
            }
        }

        public MyKeyEventDispatcher(JTextArea textArea) {
            super();
            this.textArea = textArea;
            this.utils = new JTextAreaCursorUtils(textArea);
        }

        boolean controlPressed = false;
        boolean altPressed = false;
        boolean shiftPressed = false;

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == textArea) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {

                    int key = e.getKeyCode();

                    // XXX https://docs.oracle.com/javase/tutorial/uiswing/components/textarea.html

                    // System.err.println("OOO " +key);

                    switch (key) {
                    case KeyEvent.VK_CONTROL:
                        this.controlPressed = true;
                        break;
                    case KeyEvent.VK_ALT:
                        this.altPressed = true;
                        break;
                    case KeyEvent.VK_SHIFT:
                        this.shiftPressed = true;
                        break;

                    case KeyEvent.VK_LEFT:
                        if (textArea.getCaretPosition() > 0) {
                            if (this.controlPressed) {
                                if (textArea.getCaretPosition() > 0) {
                                    utils.dec();
                                }
                                while (textArea.getCaretPosition() > 0 && !Character
                                        .isWhitespace(textArea.getText().charAt(textArea.getCaretPosition()))) {
                                    utils.dec();
                                }
                                while (textArea.getCaretPosition() > 0 && Character
                                        .isWhitespace(textArea.getText().charAt(textArea.getCaretPosition()))) {
                                    utils.dec();
                                }
                            } else {
                                utils.dec();
                            }
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (textArea.getCaretPosition() < textArea.getText().length()) {

                            if (this.controlPressed) {
                                while (textArea.getCaretPosition() < textArea.getText().length() - 1 && !Character
                                        .isWhitespace(textArea.getText().charAt(textArea.getCaretPosition()))) {
                                    utils.inc();
                                }
                                while (textArea.getCaretPosition() < textArea.getText().length() - 1 && Character
                                        .isWhitespace(textArea.getText().charAt(textArea.getCaretPosition()))) {
                                    utils.inc();
                                }
                            } else {
                                utils.inc();
                            }
                        }
                        break;
                    case KeyEvent.VK_DOWN: {

                        if (utils.getLineNum() >= utils.getLineCount() - 1) {
                            utils.gotoLineEnd();
                            utils.inc();
                            break;
                        }

                        int cols = utils.getColumnNum();
                        utils.gotoLineEnd();
                        utils.inc();
                        utils.inc(Math.min(cols, utils.getLineLength()));
                        break;
                    }
                    case KeyEvent.VK_UP: {
                        if (utils.getLineNum() == 0) {
                            break;
                        }

                        int cols = utils.getColumnNum();
                        utils.gotoLineBegin();
                        utils.dec();
                        utils.gotoLineBegin();
                        utils.inc(Math.min(cols, utils.getLineLength()));
                        break;
                    }

                    case KeyEvent.VK_PAGE_UP: {
                        for (int i = 0; i < 20; i++) {

                            // TODO
                            if (utils.getLineNum() == 0) {
                                break;
                            }

                            int cols = utils.getColumnNum();
                            utils.gotoLineBegin();
                            utils.dec();
                            utils.gotoLineBegin();
                            utils.inc(Math.min(cols, utils.getLineLength()));
                        }
                        break;
                    }
                    case KeyEvent.VK_PAGE_DOWN: {
                        for (int i = 0; i < 20; i++) {

                            // TODO
                            if (utils.getLineNum() >= utils.getLineCount() - 1) {
                                utils.gotoLineEnd();
                                utils.inc();
                                break;
                            }

                            int cols = utils.getColumnNum();
                            utils.gotoLineEnd();
                            utils.inc();
                            utils.inc(Math.min(cols, utils.getLineLength()));
                        }
                        break;
                    }
                    case KeyEvent.VK_HOME:
                        if (this.controlPressed) {
                            textArea.setCaretPosition(0);
                        } else {
                            utils.gotoLineBegin();
                        }
                        break;
                    case KeyEvent.VK_END:
                        if (this.controlPressed) {
                            textArea.setCaretPosition(textArea.getText().length());
                        } else {
                            utils.gotoLineEnd();
                        }
                        break;
                    case KeyEvent.VK_DELETE: {
                        int pos = textArea.getCaretPosition();
                        if (pos >= textArea.getText().length()) {
                            break;
                        }
                        textArea.replaceRange("", pos, pos + 1);
                        break;
                    }
                    case KeyEvent.VK_BACK_SPACE: {
                        int pos = textArea.getCaretPosition();
                        if (pos <= 0) {
                            break;
                        }
                        textArea.replaceRange("", pos - 1, pos);
                        break;
                    }
                    default:
                    }
                    e.consume();
                } else if (e.getID() == KeyEvent.KEY_TYPED) {
                    // System.out.println((int) e.getKeyChar());
                    if (e.getKeyChar() != KeyEvent.VK_DELETE && e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
                        textArea.insert(String.valueOf(e.getKeyChar()), textArea.getCaretPosition());
                    }
                    e.consume();
                } else if (e.getID() == KeyEvent.KEY_RELEASED) {

                    int key = e.getKeyCode();

                    switch (key) {
                    case KeyEvent.VK_CONTROL:
                        this.controlPressed = false;
                        break;
                    case KeyEvent.VK_ALT:
                        this.altPressed = false;
                        break;
                    case KeyEvent.VK_SHIFT:
                        this.shiftPressed = false;
                        break;
                    }
                    e.consume();
                }
            }

            // System.out.println(this.controlPressed + "-" + this.altPressed + "-" +
            // this.shiftPressed);
            return false;
        }

    }

    // Handle menu events
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == newItem) {
            textArea.setText("");
        } else if (e.getSource() == openItem) {
            loadFile();
        } else if (e.getSource() == saveItem) {
            saveFile(filename);
        } else if (e.getSource() == saveAsItem) {
            saveFile(null);
        } else if (e.getSource() == exitItem) {
            System.exit(0);
        } else if (e.getSource() == lineWrapButton) {
            textArea.setLineWrap(!textArea.getLineWrap());
            textArea.requestFocus();
        }
    }

    // Prompt user to enter filename and load file. Allow user to cancel.
    // If file is not found, pop up an error and leave screen contents
    // and filename unchanged.
    private void loadFile() {
        JFileChooser fc = new JFileChooser();
        String name = null;
        if (fc.showOpenDialog(null) != JFileChooser.CANCEL_OPTION) {
            name = fc.getSelectedFile().getAbsolutePath();
        } else {
            return; // user cancelled
        }
        loadFile(name);
    }

    private void loadFile(String name) {
        try {
            File f = new File(name);
            Scanner in = new Scanner(f); // might fail
            filename = name;
            textArea.setText("");
            while (in.hasNext()) {
                textArea.append(in.nextLine() + "\n");
            }
            in.close();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "File not found: " + name, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Save named file. If name is null, prompt user and assign to filename.
    // Allow user to cancel, leaving filename null. Tell user if save is
    // successful.
    private void saveFile(String name) {
        if (name == null) { // get filename from user
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(null) != JFileChooser.CANCEL_OPTION) {
                name = fc.getSelectedFile().getAbsolutePath();
            }
        }
        if (name != null) { // else user cancelled
            try {
                Formatter out = new Formatter(new File(name)); // might fail
                filename = name;
                out.format("%s", textArea.getText());
                out.close();
                JOptionPane.showMessageDialog(null, "Saved to " + filename, "Save File", JOptionPane.PLAIN_MESSAGE);
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Cannot write to file: " + name, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
