package udb2019;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class Testing extends JFrame {
    JTextArea ta = new JTextArea(5, 10);
    boolean arrowKey;

    public Testing() {
        setLocation(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JScrollPane sp = new JScrollPane(ta);
        JScrollBar sb = sp.getVerticalScrollBar();
        JPanel panel = new JPanel();
        panel.add(sp);
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(new JComboBox(new String[] { "a", "b", "c" }), BorderLayout.SOUTH);
        pack();
        for (int x = 0; x < 25; x++)
            ta.append(x + "\n");
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent ke) {
                if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof JTextArea) {
                    if (ke.getID() == KeyEvent.KEY_PRESSED) {
                        int key = ke.getKeyCode();
                        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
                            arrowKey = true;
                            ke.consume();
                        }
                    }
                    if (ke.getID() == KeyEvent.KEY_TYPED && arrowKey)
                        ke.consume();
                    if (ke.getID() == KeyEvent.KEY_RELEASED)
                        arrowKey = false;
                }
                return false;
            }
        });
    }

    public static void main(String[] args) {
        new Testing().setVisible(true);
    }
}