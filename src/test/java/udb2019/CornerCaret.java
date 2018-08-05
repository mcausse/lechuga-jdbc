package udb2019;
/*
Java Swing, 2nd Edition
By Marc Loy, Robert Eckstein, Dave Wood, James Elliott, Brian Cole
ISBN: 0-596-00408-7
Publisher: O'Reilly 
*/

// CornerCaret.java
//A custom caret class.
//

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

public class CornerCaret extends DefaultCaret {

    public CornerCaret() {
        setBlinkRate(500); // half a second
    }

    protected synchronized void damage(Rectangle r) {
        if (r == null)
            return;
        // give values to x,y,width,height (inherited from java.awt.Rectangle)
        x = r.x;
        y = r.y + (r.height * 4 / 5 - 3);
        width = 5;
        height = 5;
        repaint(); // calls getComponent().repaint(x, y, width, height)
    }

    public void paint(Graphics g) {
        JTextComponent comp = getComponent();
        if (comp == null)
            return;

        int dot = getDot();
        Rectangle r = null;
        try {
            r = comp.modelToView(dot);
        } catch (BadLocationException e) {
            return;
        }
        if (r == null)
            return;

//        int caretWidth=textArea.getFontMetrics(textArea.getFont()).charWidth(' ');
        
        int dist = r.height * 4 / 5 - 3; // will be distance from r.y to top

        if ((x != r.x) || (y != r.y + dist)) {
            // paint() has been called directly, without a previous call to
            // damage(), so do some cleanup. (This happens, for example, when
            // the
            // text component is resized.)
            repaint(); // erase previous location of caret
            x = r.x; // set new values for x,y,width,height
            y = r.y + dist;
            width = 5;
            height = 5;
        }

        if (isVisible()) {
            g.setColor(comp.getCaretColor());
            g.drawLine(r.x, r.y + dist, r.x, r.y + dist + 4); // 5 vertical
            // pixels
            g.drawLine(r.x, r.y + dist + 4, r.x + 4, r.y + dist + 4); // 5 horiz
            // px
            
            g.setXORMode(comp.getBackground());
            g.fillRect(r.x, r.y + dist, r.x + 4, r.y + dist + 4);
        }
    }

    public static void main(String args[]) {
        JFrame frame = new JFrame("CornerCaret demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextArea area = new JTextArea(8, 32);
        area.setCaret(new CornerCaret());
        area.setText("This is the story\nof the hare who\nlost his spectacles.");
        frame.getContentPane().add(new JScrollPane(area), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}