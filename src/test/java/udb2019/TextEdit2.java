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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class TextEdit2 extends JFrame implements ActionListener {

    private static final long serialVersionUID = 2054269406974939700L;

    private final JTextArea textArea = new JTextArea();
    private final JMenu fileMenu = new JMenu("File");
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenuItem newItem = new JMenuItem("New");
    private final JMenuItem openItem = new JMenuItem("Open");
    private final JMenuItem saveItem = new JMenuItem("Save");
    private final JMenuItem saveAsItem = new JMenuItem("Save As");
    private final JMenuItem exitItem = new JMenuItem("Exit");
    private String filename = null; // set by "Open" or "Save As"

    public static void main(String args[]) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        new TextEdit2();
    }

    // Constructor: create a text editor with a menu
    public TextEdit2() {
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

        {
            textArea.setLineWrap(true);
            Font font = new Font("Monospaced", Font.BOLD, 14);
            textArea.setFont(font);

        }

        // Create and display rest of GUI
        add(new JScrollPane(textArea));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setSize(300, 300);
        {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle bounds = env.getMaximumWindowBounds();
            // System.out.println("Screen Bounds: " + bounds);
            setSize(new Dimension(bounds.width / 2/* FIXME */, bounds.height / 2/* FIXME */));
        }

        setVisible(true);

        textArea.setEditable(true);

        textArea.setSelectionColor(Color.BLUE);
        textArea.setSelectedTextColor(Color.WHITE);
        textArea.setCaretColor(Color.BLACK);

        loadFile("/home/mhoms/tableman.properties");

        textArea.getCaret().setVisible(true);
        textArea.setCaretPosition(0);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new MyKeyEventDispatcher(textArea));
    }

    static class MyKeyEventDispatcher implements KeyEventDispatcher {

        final JTextArea textArea;

        public MyKeyEventDispatcher(JTextArea textArea) {
            super();
            this.textArea = textArea;
        }

        boolean controlPressed = false;
        boolean altPressed = false;
        boolean shiftPressed = false;

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == textArea) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {

                    int key = e.getKeyCode();

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
                        if (textArea.getCaretPosition() > 0)
                            textArea.setCaretPosition(textArea.getCaretPosition() - 1);
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (textArea.getCaretPosition() < textArea.getText().length())
                            textArea.setCaretPosition(textArea.getCaretPosition() + 1);
                        break;
                    case KeyEvent.VK_HOME:
                        if (this.controlPressed) {
                            textArea.setCaretPosition(0);
                        } else {
                            int c = textArea.getCaretPosition();
                            while (textArea.getText().charAt(c) != '\n') {
                                c--;
                            }
                            textArea.setCaretPosition(c + 1);
                        }
                        break;
                    case KeyEvent.VK_END:
                        if (this.controlPressed) {
                            textArea.setCaretPosition(textArea.getText().length());
                        } else {
                            int c = textArea.getCaretPosition();
                            while (textArea.getText().charAt(c) != '\n') {
                                c++;
                            }
                            textArea.setCaretPosition(c);
                        }
                        break;
                    }
                    e.consume();
                }
                if (e.getID() == KeyEvent.KEY_TYPED)
                    e.consume();
                if (e.getID() == KeyEvent.KEY_RELEASED) {

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
            return false;
        }

    }

    // class Mac

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
