package udb2019;

import java.awt.EventQueue;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

public class Test {
    public static void main(String[] args) throws IOException {
        JTextArea textArea = new JTextArea(20, 40);
        textArea.read(new FileReader("/home/mhoms/tableman.properties"), null); // for some text
        JScrollPane scrollPane = new JScrollPane(textArea);
        // disableArrowKeys(textArea.getInputMap());
        disableArrowKeys(scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));

        JFrame.setDefaultLookAndFeelDecorated(true);
        final JFrame f = new JFrame("Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(scrollPane);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                f.pack();
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            }
        });
    }

    static void disableArrowKeys(InputMap im) {
        String[] keystrokeNames = { "UP", "DOWN", "LEFT", "RIGHT" };
        for (String keystrokeName : keystrokeNames) {
            im.put(KeyStroke.getKeyStroke(keystrokeName), "none");
        }
    }
}