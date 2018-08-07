
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
    final Color selectionBackgroundColor = Color.BLUE;
    final Color selectionColor = Color.WHITE;

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

        textArea.setSelectionColor(selectionBackgroundColor);
        textArea.setSelectedTextColor(selectionColor);

        textArea.setBackground(backgroundColor);

        // loadFile("/home/mhoms/tableman.properties");
        // loadFile("d:/c.properties");
        loadFile("/home/mhoms/dbman.script");

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

    static class JTextAreaActionsManager {

        final JTextArea textArea;

        boolean shift = false;
        boolean selection = false;

        Integer selectionStart = null;
        Integer selectionEnd = null;

        public JTextAreaActionsManager(JTextArea textArea) {
            super();
            this.textArea = textArea;
        }

        /////////////////

        public void shiftPressed() {
            shift = true;
        }

        public void shiftReleased() {
            shift = false;
        }

        public void beforeCursorMoves() {
            if (shift) {
                if (!selection) {
                    selection = true;
                    selectionStart = textArea.getCaretPosition();
                    selectionEnd = textArea.getCaretPosition();

                    textArea.setCaretPosition(selectionStart);
                    textArea.moveCaretPosition(selectionEnd);
                }
            } else {
                selection = false;
            }
        }

        public void afterCursorMoves() {
            if (selection) {
                selectionEnd = textArea.getCaretPosition();

                textArea.setCaretPosition(selectionStart);
                textArea.moveCaretPosition(selectionEnd);
            }
        }

        /////////////////

        private int getLineNum() {
            try {
                return textArea.getLineOfOffset(textArea.getCaretPosition());
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        private int getColumnNum() {
            try {
                return textArea.getCaretPosition() - textArea.getLineStartOffset(getLineNum());
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        private int getLineLength() {
            try {
                return textArea.getLineEndOffset(getLineNum()) - 1 - textArea.getLineStartOffset(getLineNum());
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        private void gotoLineBegin() {
            try {
                textArea.setCaretPosition(textArea.getLineStartOffset(getLineNum()));
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        private void gotoLineEnd() {
            try {
                int pos = textArea.getLineEndOffset(getLineNum()) - 1;
                if (pos > 0) {
                    textArea.setCaretPosition(pos);
                }
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        private void dec() {
            if (textArea.getCaretPosition() - 1 >= 0) {
                textArea.setCaretPosition(textArea.getCaretPosition() - 1);
            }
        }

        private void inc() {
            if (textArea.getCaretPosition() + 1 < textArea.getText().length()) {
                textArea.setCaretPosition(textArea.getCaretPosition() + 1);
            }
        }

        private void inc(int n) {
            for (int i = 0; i < n; i++) {
                inc();
            }
        }

        private int getLineCount() {
            // -1 pq és 1-based
            return textArea.getLineCount() - 1;
        }

        /////////////////

        public void moveLeft(int n) {
            beforeCursorMoves();
            for (int i = 0; i < n; i++) {
                if (textArea.getCaretPosition() > 0) {
                    dec();
                }
            }
            afterCursorMoves();
        }

        public void moveRight(int n) {
            beforeCursorMoves();
            for (int i = 0; i < n; i++) {
                if (textArea.getCaretPosition() < textArea.getText().length()) {
                    inc();
                }
            }
            afterCursorMoves();
        }

        public void moveLeftWords(int n) {
            beforeCursorMoves();
            for (int i = 0; i < n; i++) {
                if (textArea.getCaretPosition() > 0) {
                    dec();
                    while (textArea.getCaretPosition() > 0
                            && !Character.isWhitespace(textArea.getText().charAt(textArea.getCaretPosition()))) {
                        dec();
                    }
                    while (textArea.getCaretPosition() > 0
                            && Character.isWhitespace(textArea.getText().charAt(textArea.getCaretPosition()))) {
                        dec();
                    }
                    inc();
                }
            }
            afterCursorMoves();
        }

        public void moveRightWords(int n) {
            beforeCursorMoves();
            for (int i = 0; i < n; i++) {
                if (textArea.getCaretPosition() < textArea.getText().length()) {
                    while (textArea.getCaretPosition() < textArea.getText().length() - 1
                            && !Character.isWhitespace(textArea.getText().charAt(textArea.getCaretPosition()))) {
                        inc();
                    }
                    while (textArea.getCaretPosition() < textArea.getText().length() - 1
                            && Character.isWhitespace(textArea.getText().charAt(textArea.getCaretPosition()))) {
                        inc();
                    }
                }
            }
            afterCursorMoves();
        }

        public void moveDown() {
            beforeCursorMoves();
            if (getLineNum() >= getLineCount() - 1) {
                gotoLineEnd();
                inc();
                return;
            }
            int cols = getColumnNum();
            gotoLineEnd();
            inc();
            inc(Math.min(cols, getLineLength()));
            afterCursorMoves();
        }

        public void moveUp() {
            beforeCursorMoves();
            if (getLineNum() == 0) {
                return;
            }
            int cols = getColumnNum();
            gotoLineBegin();
            dec();
            gotoLineBegin();
            inc(Math.min(cols, getLineLength()));
            afterCursorMoves();
        }

        public void pageUp(int n) {
            beforeCursorMoves();
            for (int i = 0; i < n; i++) {
                moveUp();
            }
            afterCursorMoves();
        }

        public void pageDown(int n) {
            beforeCursorMoves();
            for (int i = 0; i < n; i++) {
                moveDown();
            }
            afterCursorMoves();
        }

        public void bufferHome() {
            beforeCursorMoves();
            textArea.setCaretPosition(0);
            afterCursorMoves();
        }

        public void bufferEnd() {
            beforeCursorMoves();
            textArea.setCaretPosition(textArea.getText().length());
            afterCursorMoves();
        }

        public void lineHome() {
            beforeCursorMoves();
            gotoLineBegin();
            afterCursorMoves();
        }

        public void lineEnd() {
            beforeCursorMoves();
            gotoLineEnd();
            afterCursorMoves();
        }

        public void delete() {
            if (selection) {
                int min = Math.min(selectionStart, selectionEnd);
                int max = Math.max(selectionStart, selectionEnd);
                textArea.replaceRange("", min, max);
                textArea.setCaretPosition(min);
            } else {
                int pos = textArea.getCaretPosition();
                if (pos >= textArea.getText().length()) {
                    return;
                }
                textArea.replaceRange("", pos, pos + 1);
            }
        }

        public void backSpace() {
            if (selection) {
                int min = Math.min(selectionStart, selectionEnd);
                int max = Math.max(selectionStart, selectionEnd);
                textArea.replaceRange("", min, max);
                textArea.setCaretPosition(min);
            } else {
                int pos = textArea.getCaretPosition();
                if (pos <= 0) {
                    return;
                }
                textArea.replaceRange("", pos - 1, pos);
            }
        }

        public void insert(String text, int atPosition) {
            if (selection) {
                int min = Math.min(selectionStart, selectionEnd);
                int max = Math.max(selectionStart, selectionEnd);
                textArea.replaceRange(text, min, max);
                textArea.setCaretPosition(min + text.length());
            } else {
                textArea.insert(text, atPosition);
            }
        }

    }

    static class MyKeyEventDispatcher implements KeyEventDispatcher {

        final JTextArea textArea;
        final JTextAreaActionsManager utils;

        public MyKeyEventDispatcher(JTextArea textArea) {
            super();
            this.textArea = textArea;
            this.utils = new JTextAreaActionsManager(textArea);
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
                        if (!shiftPressed) {
                            utils.shiftPressed();
                            System.out.println("+sel");
                        }
                        this.shiftPressed = true;
                        break;

                    case KeyEvent.VK_LEFT:
                        if (this.controlPressed) {
                            utils.moveLeftWords(1);
                        } else {
                            utils.moveLeft(1);
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (this.controlPressed) {
                            utils.moveRightWords(1);
                        } else {
                            utils.moveRight(1);
                        }
                        break;
                    case KeyEvent.VK_DOWN: {
                        utils.moveDown();
                        break;
                    }
                    case KeyEvent.VK_UP: {
                        utils.moveUp();
                        break;
                    }

                    case KeyEvent.VK_PAGE_UP: {
                        utils.pageUp(20);
                        break;
                    }
                    case KeyEvent.VK_PAGE_DOWN: {
                        utils.pageDown(20);
                        break;
                    }
                    case KeyEvent.VK_HOME:
                        if (this.controlPressed) {
                            utils.bufferHome();
                        } else {
                            utils.lineHome();
                        }
                        break;
                    case KeyEvent.VK_END:
                        if (this.controlPressed) {
                            utils.bufferEnd();
                        } else {
                            utils.lineEnd();
                        }
                        break;
                    case KeyEvent.VK_DELETE: {
                        utils.delete();
                        break;
                    }
                    case KeyEvent.VK_BACK_SPACE: {
                        utils.backSpace();
                        break;
                    }
                    default:
                    }

                    // if(key!=KeyEvent.VK_SHIFT)
                    e.consume();

                } else if (e.getID() == KeyEvent.KEY_TYPED) {
                    // System.out.println((int) e.getKeyChar());
                    if (e.getKeyChar() != KeyEvent.VK_DELETE && e.getKeyChar() != KeyEvent.VK_BACK_SPACE
                            && !controlPressed) {
                        // textArea.insert(String.valueOf(e.getKeyChar()), textArea.getCaretPosition());
                        utils.insert(String.valueOf(e.getKeyChar()), textArea.getCaretPosition());
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
                        if (shiftPressed) {
                            utils.shiftReleased();
                            System.out.println("-sel");
                        }
                        this.shiftPressed = false;
                        break;
                    }

                    // if(key!=KeyEvent.VK_SHIFT)
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
