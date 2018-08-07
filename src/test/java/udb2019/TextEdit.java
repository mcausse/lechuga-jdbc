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
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

public class TextEdit extends JFrame implements ActionListener {

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
        new TextEdit();
    }

    // Constructor: create a text editor with a menu
    public TextEdit() {
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

        MacroListener listener = new MacroListener();
        textArea.addKeyListener(listener);
        textArea.addCaretListener(listener);

        // Caret caret = new DefaultCaret();
        // caret.setBlinkRate(100);

        // caret.setBlinkRate( UIManager.getInt("TextField.caretBlinkRate") );
        // textArea.getCaret().setBlinkRate(100);
        // textArea.getCaret().set

        // textArea.getCaret().addChangeListener();
        // textArea.append("aló\n");
        // loadFile("/home/mhoms/tableman.properties");
        loadFile("/home/mhoms/dbman.script");

        textArea.getCaret().setVisible(true);
        textArea.setCaretPosition(0);

        // Keymap kMap = this.getKeymap();

        // int caretWidth=textArea.getFontMetrics(textArea.getFont()).charWidth(' ');
        // textArea.putClientProperty("caretWidth", caretWidth);

        // textArea.addCaretListener(new CaretListener() {
        // public void caretUpdate(CaretEvent e) {
        // try {
        // int pos = textArea.getCaretPosition();
        // Rectangle rPos = textArea.modelToView(pos) != null ?
        // textArea.modelToView(pos).getBounds() : new Rectangle();
        // int caretX = rPos.x;
        // int caretEndX = rPos.x;
        // if (pos < textArea.getDocument().getLength()) {
        // Rectangle rNextPos = textArea.modelToView(pos + 1) != null ?
        // textArea.modelToView(pos + 1).getBounds() : new Rectangle();
        //
        // if (rPos.y == rNextPos.y) {
        // caretEndX = rNextPos.x;
        // }
        // }
        // System.out.println(caretEndX - caretX + 1);
        // textArea.putClientProperty("caretWidth", Math.max(1, caretEndX - caretX +
        // 1));
        // } catch (BadLocationException ex) {
        // ex.printStackTrace();
        // }
        // }
        // });
        // textArea.setCaret(new MyCaret());

    }

    // https://www.javalobby.org/java/forums/t19898.html
    static class MyCaret extends DefaultCaret {

        /**
         *
         */
        private static final long serialVersionUID = -6947016439837399863L;

        @Override
        public void paint(Graphics g) {
            // if(mode == TypingMode.INSERT) {
            // super.paint(g);
            // return;
            // }
            JTextComponent comp = getComponent();

            int dot = getDot();
            Rectangle r = null;
            char c;
            try {
                r = comp.modelToView(dot);
                if (r == null) {
                    return;
                }
                c = comp.getText(dot, 1).charAt(0);
            } catch (BadLocationException e) {
                return;
            }

            // erase provious caret
            if (x != r.x || y != r.y) {
                repaint();
                x = r.x;
                y = r.y;
                height = r.height;
            }

            g.setColor(comp.getCaretColor());
            g.setXORMode(comp.getBackground());

            width = g.getFontMetrics().charWidth(c);
            if (c == '\t' || c == '\n') {
                width = g.getFontMetrics().charWidth(' ');
            }
            System.out.println(width);
            if (isVisible()) {
                g.fillRect(r.x, r.y, width, r.height);
            }
        }
    };

    // class MacroEvent {
    //
    // boolean cntrlPressed;
    // char keyChar;
    // int caretPos;
    //
    // @Override
    // public String toString() {
    // return "MacroEvent [cntrlPressed=" + cntrlPressed + ", keyChar=" + keyChar +
    // ", caretPos=" + caretPos + "]";
    // }
    // }

    class MacroListener implements KeyListener, CaretListener {

        // final List<MacroEvent> record = new ArrayList<>();
        // final MacroEvent current = new MacroEvent();

        int linenum;
        int columnnum;
        int caretpos;

        boolean cntrlPressed;
        boolean shiftPressed;
        boolean altPressed;
        char keyChar;

        @Override
        public String toString() {
            return "MacroListener [linenum=" + linenum + ", columnnum=" + columnnum + ", cntrlPressed=" + cntrlPressed
                    + ", shiftPressed=" + shiftPressed + ", altPressed=" + altPressed + ", keyChar=" + keyChar + "]";
        }

        @Override
        public void keyTyped(KeyEvent e) {
            keyChar = e.getKeyChar();
            System.out.println(e.getKeyCode());
            System.out.println(this);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                shiftPressed = true;
            } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                cntrlPressed = true;
            } else if (e.getKeyCode() == KeyEvent.VK_ALT) {
                altPressed = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                shiftPressed = false;
            } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                cntrlPressed = false;
            } else if (e.getKeyCode() == KeyEvent.VK_ALT) {
                altPressed = false;
            }
        }

        // Each time the caret is moved, it will trigger the listener and its method
        // caretUpdate.
        // It will then pass the event to the update method including the source of the
        // event (which is our textarea control)
        @Override
        public void caretUpdate(CaretEvent e) {
            JTextArea textArea = (JTextArea) e.getSource();

            // We create a try catch to catch any exceptions. We will simply ignore such an
            // error for our demonstration.
            try {
                // First we find the position of the caret. This is the number of where the
                // caret is in relation to the start of the JTextArea
                // in the upper left corner. We use this position to find offset values (eg what
                // line we are on for the given position as well as
                // what position that line starts on.
                caretpos = textArea.getCaretPosition();
                linenum = textArea.getLineOfOffset(caretpos);

                // We subtract the offset of where our line starts from the overall caret
                // position.
                // So lets say that we are on line 5 and that line starts at caret position 100,
                // if our caret position is currently 106
                // we know that we must be on column 6 of line 5.
                columnnum = caretpos - textArea.getLineStartOffset(linenum);

                // We have to add one here because line numbers start at 0 for getLineOfOffset
                // and we want it to start at 1 for display.
                linenum += 1;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            // Once we know the position of the line and the column, pass it to a helper
            // function for updating the status bar.
            // updateStatus(linenum, columnnum);
            System.out.println(this);
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
