package supraedit;

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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
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
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultCaret;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * moviment de cursor complet, amb selecció. Accés a clipboard de sistema.
 * Eliminació en ambdós sentits Inserció de tota classe de caràcters regionals
 * (accents, etc)
 *
 * // XXX MACROS: el recording de macros registra tot lo de textarea i del
 * cmdInputText! Ho reprodeueix tot dins la mateixa pestanya!
 *
 * // XXX selecció per mouse
 *
 * // XXX undo:
 * https://stackoverflow.com/questions/2547404/using-undo-and-redo-for-jtextarea
 * https://web.archive.org/web/20100114122417/http://exampledepot.com/egs/javax.swing.undo/UndoText.html
 *
 *
 * // XXX pestanyes
 *
 * // XXX find text/regexp in/sensitive wrap forward/backward
 *
 * // TODO replace (amb grouping si regexp=true)
 *
 * // XXX (multi) comandos inline (per input)
 *
 * // TODO (des)tabulació en bloc
 *
 * // TODO autoindent: en enter, segueix la tabulació de l'anterior fila.
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
 * [Control+R] engega/atura la grabació de macro, [Control+P] la playa.
 *
 * [Control+Z] undo, [Control+Y] redo.
 *
 *
 *
 *
 */
public class TextEdit5 extends JFrame {

    private static final long serialVersionUID = 2054269406974939700L;

    final Charset CHARSET = TextFileUtils.UTF8;
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

            cmdTextField = new JTextField();
            menuBar.add(cmdTextField);

            this.textArea = new JTextArea();
            MacroRecording macroRecording = new MacroRecording(textArea, cmdTextField);

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

            {
                final UndoManager undo = new UndoManager();
                undo.discardAllEdits();
                undo.setLimit(200);

                textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
                    @Override
                    public void undoableEditHappened(UndoableEditEvent evt) {
                        undo.addEdit(evt.getEdit());
                    }
                });

                // Create an undo action and add it to the text component
                textArea.getActionMap().put("Undo", new AbstractAction("Undo") {

                    private static final long serialVersionUID = 6230534001961891325L;

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            if (undo.canUndo()) {
                                undo.undo();
                            }
                        } catch (CannotUndoException e) {
                        }
                    }
                });

                textArea.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

                textArea.getActionMap().put("Redo", new AbstractAction("Redo") {

                    private static final long serialVersionUID = 3505672699323520092L;

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            if (undo.canRedo()) {
                                undo.redo();
                            }
                        } catch (CannotRedoException e) {
                        }
                    }
                });
                textArea.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
            }

            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(textArea.getLineWrap());
            Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
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

            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
                    new MyKeyEventDispatcher(macroRecording, cmdTextField, onDoRecord, onDoPlay));

            textArea.requestFocus();

        }

        public String getFilenameShort() {
            return new File(filename).getName();
        }

        public String getFilenameFull() {
            return filename;
        }

        private void loadFile() {
            JFileChooser fc = new JFileChooser();
            String name = null;
            if (fc.showOpenDialog(null) != JFileChooser.CANCEL_OPTION) {
                name = fc.getSelectedFile().getAbsolutePath();
            } else {
                return; // user cancelled
            }
            loadFile(name);
            this.filename = name;
        }

        private void loadFile(String name) {
            try {
                File f = new File(name);
                textArea.setText(TextFileUtils.read(f, CHARSET));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Cannot load file: " + name, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void saveFile(String name) {
            if (name == null) { // get filename from user
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(null) != JFileChooser.CANCEL_OPTION) {
                    name = fc.getSelectedFile().getAbsolutePath();
                }
            }
            if (name != null) { // else user cancelled
                try {
                    File f = new File(name);
                    TextFileUtils.write(f, CHARSET, textArea.getText());
                    JOptionPane.showMessageDialog(null, "Saved to " + filename, "Save File", JOptionPane.PLAIN_MESSAGE);
                } catch (Exception e) {
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
        // EditorPane p1 = new
        // EditorPane("C:\\Users\\mhoms.LINECOM\\git\\moncheta\\src\\test\\java\\supraedit\\TextEdit5.java");
        tabbedPane.addTab(p1.getFilenameShort(), null, p1, p1.getFilenameFull());
        tabbedPane.setSelectedComponent(p1);

        // loadFile("/home/mhoms/tableman.properties");
        // loadFile("d:/c.properties");
        // loadFile("/home/mhoms/dbman.script");
        // loadFile("C:\\Users\\mhoms.LINECOM\\git\\moncheta\\src\\test\\java\\supraedit\\TextEdit5.java");

        EditorPane p2 = new EditorPane("/home/mhoms/java/workospace/moncheta-2018-java8/PURITOS.TXT");
        // EditorPane p2 = new EditorPane("d:/a.txt");
        tabbedPane.addTab(p2.getFilenameShort(), null, p2, p2.getFilenameFull());
        tabbedPane.setSelectedComponent(p2);

        int tabSelected = tabbedPane.getSelectedIndex();
        ((EditorPane) tabbedPane.getComponent(tabSelected)).textArea.requestFocus();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle bounds = env.getMaximumWindowBounds();
            // setSize(new Dimension(bounds.width / 2/* FIXME */, bounds.height / 2/* FIXME
            // */));
            setSize(new Dimension(bounds.width, bounds.height));
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
        final JTextField cmdTextField;

        boolean isRecording = false;
        final List<KeyEvent> eventsRecorded = new ArrayList<>();

        public MacroRecording(JTextArea textArea, JTextField cmdTextField) {
            super();
            this.textArea = textArea;
            this.cmdTextField = cmdTextField;
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
                // System.out.println("recording: " + ke);
            }
        }

        public JTextArea getTextArea() {
            return textArea;
        }

        public void playMacro() {
            isRecording = false;
            for (KeyEvent e : eventsRecorded) {
                if (e.getSource() == textArea) {
                    cmdTextField.requestFocus();// TODO ?
                    textArea.dispatchEvent(new KeyEvent((Component) e.getSource(), e.getID(),
                            System.currentTimeMillis(), e.getModifiers(), e.getKeyCode(), e.getKeyChar()));
                } else if (e.getSource() == cmdTextField) {
                    cmdTextField.requestFocus();// TODO ?
                    cmdTextField.dispatchEvent(new KeyEvent((Component) e.getSource(), e.getID(),
                            System.currentTimeMillis(), e.getModifiers(), e.getKeyCode(), e.getKeyChar()));
                }
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

            if (e.getSource() == cmdTextField) {

                if (e.getID() == KeyEvent.KEY_TYPED) {
                    if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                        macroRecording.getTextArea().requestFocus();
                    }
                }

                if (e.getID() == KeyEvent.KEY_TYPED && e.getKeyChar() == KeyEvent.VK_ENTER) {
                    // System.out.println("enter");

                    JTextArea textArea = macroRecording.getTextArea();

                    String cmd = cmdTextField.getText();
                    // System.out.println("cmd: " + cmd);
                    if (cmd.startsWith("f")) {
                        String cmdVal = cmd.substring(1);
                        int textLength = macroRecording.getTextArea().getDocument().getLength();

                        int findPos = textArea.getCaretPosition() + 1;
                        if (findPos >= textLength) {
                            textArea.setCaretPosition(textLength);
                            textArea.requestFocus();
                        } else {
                            int pos = textArea.getText().indexOf(cmdVal, findPos);
                            if (pos < 0) {
                                // no troba més: deixa el cursor a final de fitxer
                                textArea.setCaretPosition(textLength);
                            } else {
                                textArea.setCaretPosition(pos);
                            }
                        }

                        textArea.requestFocus();

                        // // TODO
                        // try {
                        // int pos = textArea.getCaretPosition();
                        // Highlighter highlighter = textArea.getHighlighter();
                        // HighlightPainter painter = new
                        // DefaultHighlighter.DefaultHighlightPainter(Color.pink);
                        // highlighter.addHighlight(pos, pos + textLength, painter);
                        // } catch (BadLocationException ex) {
                        // throw new RuntimeException(ex);
                        // }

                        // TODO
                        // JOptionPane.showMessageDialog(null, new JTePane(textArea));
                    } else if (cmd.startsWith("F")) {

                        String cmdVal = cmd.substring(1);
                        // int textLength = textArea.getDocument().getLength();

                        int findPos = textArea.getCaretPosition() - 1;
                        if (findPos <= 0) {
                        } else {
                            int pos = textArea.getText().lastIndexOf(cmdVal, findPos);
                            if (pos < 0) {
                                // no troba més: deixa el cursor a inici de fitxer
                                textArea.setCaretPosition(0);
                            } else {
                                textArea.setCaretPosition(pos);
                            }
                        }

                        textArea.requestFocus();

                        // TODO
                        // try {
                        // Highlighter highlighter = textArea.getHighlighter();
                        // HighlightPainter painter = new
                        // DefaultHighlighter.DefaultHighlightPainter(Color.pink);
                        // highlighter.addHighlight(pos, pos + text.length(), painter);
                        // } catch (BadLocationException e) {
                        // throw new RuntimeException(e);
                        // }

                        // TODO
                        // JOptionPane.showMessageDialog(null, new JTePane(textArea));
                    } else if (cmd.startsWith("@f")) {
                        String regexp = cmd.substring(2);
                        int textLength = textArea.getDocument().getLength();

                        Pattern p = Pattern.compile(regexp);

                        int findPos = textArea.getCaretPosition() + 1;
                        if (findPos >= textLength) {
                            textArea.setCaretPosition(textLength);
                        } else {
                            Matcher m = p.matcher(textArea.getText());
                            if (m.find(findPos)) {
                                textArea.setCaretPosition(m.start());
                            } else {
                                // no troba més: deixa el cursor a final de fitxer
                                textArea.setCaretPosition(textLength);
                            }
                        }

                        textArea.requestFocus();
                    }
                }

                /**
                 * IMPORTANTISSIM: REGISTRA TOT LO TECLEJAT EN L'INPUT-TEXT DE COMANDES
                 */
                macroRecording.record(e);
            }

            if (e.getSource() == macroRecording.getTextArea()) {

                if (e.getID() == KeyEvent.KEY_PRESSED) {

                    int key = e.getKeyCode();

                    switch (key) {
                    case KeyEvent.VK_CONTROL:
                        this.controlPressed = true;
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

class TextFileUtils {

    public static final Charset ISO88591 = Charset.forName("ISO-8859-1");
    public static final Charset UTF8 = Charset.forName("UTF8");
    public static final Charset Cp1252 = Charset.forName("Cp1252");

    public static String read(File f, Charset charset) {

        BufferedReader b = null;
        try {
            StringBuilder r = new StringBuilder();

            b = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));
            String line;
            while ((line = b.readLine()) != null) {
                r.append(line);
                r.append('\n');
            }
            return r.toString();

        } catch (IOException e) {
            throw new RuntimeException("error reading file: " + f, e);
        } finally {
            if (b != null) {
                try {
                    b.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    public static void write(File f, Charset charset, String text) {
        Writer w = null;
        try {
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), charset));
            w.write(text);
        } catch (IOException e) {
            throw new RuntimeException("error writing file: " + f, e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

}
