package supraedit;
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

/**
 * moviment de cursor complet, amb selecció. Accés a clipboard de sistema.
 * Eliminació en ambdós sentits Inserció de tota classe de caràcters regionals
 * (accents, etc)
 *
 * // XXX MACROS
 *
 * // TODO selecció per mouse
 *
 * // TODO undo
 *
 * // XXX pestanyes
 *
 * // XXX find text/regexp in/sensitive wrap forward/backward
 *
 * // TODO replace (amb grouping si regexp=true)
 *
 * // TODO (multi) comandos inline (per input)
 *
 * // TODO (des)tabulació en bloc
 *
 *
 *
 *
 * <h1>Supra Ed <small>the ultimate editor for editing enthusiasts</small></h1>
 *
 * <h2>Navegació bàsica</h2>
 *
 * <p>
 * Els fitxers en edició s'organitzen en tabs: [Alt+left] i [Alt+right] canvia
 * de tab actiu.
 *
 * <p>
 * En cada tab hi ha l'àrea d'edició, i un input text: el cursor conmuta entre
 * aquests amb [Esc].
 *
 *
 * [Control+R] engega/atura la grabació de macro, [Control+P] la playa.
 *
 *
 */
public class TextEdit5 extends JFrame {

    private static final long serialVersionUID = 2054269406974939700L;

    final Color fontColor = Color.BLACK;
    final Color cursorColor = Color.RED;
    final Color backgroundColor = Color.WHITE;
    final Color selectionBackgroundColor = Color.BLUE;
    final Color selectionColor = Color.WHITE;

    public static void main(String args[]) {
        // JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TextEdit5();
            }
        });
    }

    interface Closure {
        void execute();
    }

    class EditorPane extends JPanel {

        private static final long serialVersionUID = -3040324544220338224L;

        String filename; // set by "Open" or "Save As"
        final JTextArea textArea;

        final JMenu fileMenu = new JMenu("File");
        final JMenuBar menuBar = new JMenuBar();
        final JMenuItem newItem = new JMenuItem("New");
        final JMenuItem openItem = new JMenuItem("Open");
        final JMenuItem saveItem = new JMenuItem("Save");
        final JMenuItem saveAsItem = new JMenuItem("Save As");
        final JMenuItem exitItem = new JMenuItem("Exit");

        final JButton lineWrapButton;
        final JButton recordMacroButton;
        final JButton playMacroButton;
        final JTextField cmdTextField;

        public EditorPane(String filename) {
            super(new BorderLayout());
            this.filename = filename;

            this.textArea = new JTextArea();
            MacroRecording macroRecording = new MacroRecording(textArea);

            fileMenu.add(newItem);
            fileMenu.add(openItem);
            fileMenu.add(saveItem);
            fileMenu.add(saveAsItem);
            fileMenu.add(exitItem);

            menuBar.add(fileMenu);
            add(menuBar, BorderLayout.NORTH);

            lineWrapButton = new JButton("wrap");
            lineWrapButton.setMargin(new Insets(0, 0, 0, 0));
            menuBar.add(lineWrapButton);

            recordMacroButton = new JButton("Rec");
            recordMacroButton.setMargin(new Insets(0, 0, 0, 0));
            menuBar.add(recordMacroButton);

            playMacroButton = new JButton("Play");
            playMacroButton.setMargin(new Insets(0, 0, 0, 0));
            menuBar.add(playMacroButton);

            {
                cmdTextField = new JTextField(50);
                cmdTextField.addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                            textArea.requestFocus();
                            e.consume();
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                    }
                });
                menuBar.add(cmdTextField);
            }

            ActionListener actionListener = new ActionListener() {

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
                        textArea.setWrapStyleWord(textArea.getLineWrap());
                        textArea.requestFocus();
                    } else if (e.getSource() == recordMacroButton) {
                        if (macroRecording.isRecording()) {
                            macroRecording.recordMacroStop();
                            recordMacroButton.setText("Rec");
                            playMacroButton.setEnabled(true);
                        } else {
                            macroRecording.recordMacroStart();
                            recordMacroButton.setText("Recording");
                            playMacroButton.setEnabled(false);
                        }
                        textArea.requestFocus();
                    } else if (e.getSource() == playMacroButton) {
                        macroRecording.playMacro();
                        textArea.requestFocus();
                    } else if (e.getSource() == cmdTextField) {
                        // TODO això és important: aquí es processa una línia de comanda des de l'input
                        // System.out.println(cmdTextField.getText());
                        // macroRecording.interpret(cmdTextField.getText());
                        // textArea.requestFocus();
                    }
                }
            };

            newItem.addActionListener(actionListener);
            openItem.addActionListener(actionListener);
            saveItem.addActionListener(actionListener);
            saveAsItem.addActionListener(actionListener);
            exitItem.addActionListener(actionListener);
            lineWrapButton.addActionListener(actionListener);
            recordMacroButton.addActionListener(actionListener);
            playMacroButton.addActionListener(actionListener);
            cmdTextField.addActionListener(actionListener);

            JScrollPane scrollPane = new JScrollPane(textArea);
            add(scrollPane, BorderLayout.CENTER);

            {
                TextLineNumber tln = new TextLineNumber(textArea);
                tln.setMinimumDisplayDigits(3);
                scrollPane.setRowHeaderView(tln);
            }

            loadFile(filename);

            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(textArea.getLineWrap());
            Font font = new Font("Monospaced", Font.BOLD, 12);
            textArea.setFont(font);
            textArea.setEditable(true);
            textArea.setSelectionColor(selectionBackgroundColor);
            textArea.setSelectedTextColor(selectionColor);
            textArea.setBackground(backgroundColor);
            textArea.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, textArea.getBackground()));
            DefaultCaret c = new DefaultCaret();
            textArea.setCaret(c);
            textArea.getCaret().setBlinkRate(500);
            textArea.setCaretColor(cursorColor);
            textArea.setCaretPosition(0);
            textArea.getCaret().setVisible(true);

            Closure onDoRecord = () -> {
                recordMacroButton.doClick();
            };
            Closure onDoPlay = () -> {
                playMacroButton.doClick();
            };

            // KeyEvent e;
            // KeyboardFocusManager.getCurrentKeyboardFocusManager().dispatchKeyEvent(e);

            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
                    new MyKeyEventDispatcher(macroRecording, cmdTextField, onDoRecord, onDoPlay));

            textArea.requestFocus();

        }

        public String getFilenameShort() {
            return "(short)";
        }

        public String getFilenameFull() {
            return filename;
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

    // Constructor: create a text editor with a menu
    public TextEdit5() {
        super("Supra Ed");

        // https://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
        JTabbedPane tabbedPane = new JTabbedPane();
        // ImageIcon icon = createImageIcon("images/middle.gif");
        add(tabbedPane);

        EditorPane p1 = new EditorPane("/home/mhoms/tableman.properties");
        tabbedPane.addTab(p1.getFilenameShort(), null, p1, p1.getFilenameFull());
        tabbedPane.setSelectedComponent(p1);

        // loadFile("/home/mhoms/tableman.properties");
        // loadFile("d:/c.properties");
        // loadFile("/home/mhoms/dbman.script");
        // loadFile("C:\\Users\\mhoms.LINECOM\\git\\moncheta\\src\\test\\java\\supraedit\\TextEdit5.java");

        EditorPane p2 = new EditorPane("/home/mhoms/java/workospace/moncheta-2018-java8/PURITOS.TXT");
        tabbedPane.addTab(p2.getFilenameShort(), null, p2, p2.getFilenameFull());
        tabbedPane.setSelectedComponent(p2);

        int tabSelected = tabbedPane.getSelectedIndex();
        ((EditorPane) tabbedPane.getComponent(tabSelected)).textArea.requestFocus();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle bounds = env.getMaximumWindowBounds();
            setSize(new Dimension(bounds.width / 2/* FIXME */, bounds.height / 2/* FIXME */));
        }

        tabbedPane.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                JTabbedPane tabs = (JTabbedPane) e.getSource();
                ((EditorPane) tabs.getSelectedComponent()).textArea.requestFocus();
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });

        UIManager.put("Caret.width", 3);
        setVisible(true);
    }

    static class MacroRecording {

        final JTextArea textArea;

        boolean isRecording = false;
        final List<KeyEvent> eventsRecorded = new ArrayList<>();

        public MacroRecording(JTextArea textArea) {
            super();
            this.textArea = textArea;
        }

        public boolean isRecording() {
            return isRecording;
        }

        public void recordMacroStart() {
            isRecording = true;
            eventsRecorded.clear();
        }

        public void recordMacroStop() {
            isRecording = false;
        }

        public void record(KeyEvent ke) {
            if (isRecording) {
                this.eventsRecorded.add(ke);
            }
        }

        public JTextArea getTextArea() {
            return textArea;
        }

        public void playMacro() {
            isRecording = false;
            for (KeyEvent e : eventsRecorded) {
                textArea.dispatchEvent(new KeyEvent((Component) e.getSource(), e.getID(), System.currentTimeMillis(),
                        e.getModifiers(), e.getKeyCode(), e.getKeyChar()));
            }
        }

    }

    class MyKeyEventDispatcher implements KeyEventDispatcher {

        final MacroRecording macroRecording;
        final JTextField cmdTextField;

        final Closure onDoRecord;
        final Closure onDoPlay;

        public MyKeyEventDispatcher(MacroRecording macroRecording, JTextField cmdTextField, Closure onDoRecord,
                Closure onDoPlay) {
            super();
            this.macroRecording = macroRecording;
            this.cmdTextField = cmdTextField;
            this.onDoRecord = onDoRecord;
            this.onDoPlay = onDoPlay;
        }

        boolean controlPressed = false;
        boolean altPressed = false;

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {

            /*
             * la gestió de la tecla [Alt] és comuna entre tots els tabs: això permet
             * conmutar fluidament entre ells sense deixar anar el [Alt] (fent [Alt+left] i
             * [Alt+right]).
             */
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ALT) {
                    this.altPressed = true;
                    e.consume();
                }
            }
            if (e.getID() == KeyEvent.KEY_RELEASED) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ALT) {
                    this.altPressed = false;
                    e.consume();
                }
            }

            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == macroRecording.getTextArea()) {

                if (e.getID() == KeyEvent.KEY_PRESSED) {

                    int key = e.getKeyCode();

                    switch (key) {
                    case KeyEvent.VK_CONTROL:
                        this.controlPressed = true;
                        // System.out.println(controlPressed);
                        break;
                    case KeyEvent.VK_LEFT: {

                        if (altPressed) {
                            JTabbedPane tabs = (JTabbedPane) cmdTextField.getParent().getParent().getParent();
                            int selected = tabs.getSelectedIndex();
                            if (selected > 0) {
                                tabs.setSelectedIndex(selected - 1);
                            }
                            e.consume();
                        }
                        break;
                    }
                    case KeyEvent.VK_RIGHT: {

                        if (altPressed) {
                            JTabbedPane tabs = (JTabbedPane) cmdTextField.getParent().getParent().getParent();
                            int selected = tabs.getSelectedIndex();
                            if (selected < tabs.getTabCount() - 1) {
                                tabs.setSelectedIndex(selected + 1);
                            }
                            e.consume();
                        }
                        break;
                    }
                    case KeyEvent.VK_ESCAPE: {
                        cmdTextField.requestFocus();
                        e.consume();
                        break;
                    }
                    case KeyEvent.VK_R: {
                        if (controlPressed) {
                            onDoRecord.execute();
                            e.consume();
                        }
                        break;
                    }
                    case KeyEvent.VK_P: {
                        if (controlPressed) {
                            onDoPlay.execute();
                            e.consume();
                        }
                        break;
                    }
                    }

                } else if (e.getID() == KeyEvent.KEY_TYPED) {

                } else if (e.getID() == KeyEvent.KEY_RELEASED) {

                    int key = e.getKeyCode();

                    switch (key) {
                    case KeyEvent.VK_CONTROL:
                        this.controlPressed = false;
                        break;

                    }
                }

                /**
                 * IMPORTANTISSIM: REGISTRA TOT LO NO CONSUMIT!!!!!
                 */
                if (!e.isConsumed()) {
                    macroRecording.record(e);
                }

            }

            return false;
        }

    }

}
