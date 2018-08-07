package udb2019;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Keymap;

public class CaretShape extends JEditorPane {

    /**
     *
     */
    private static final long serialVersionUID = -4433998522177633990L;
    private boolean isInsertMode = false;
    Color oldCaretColor;
    Color insertCaretColor = new Color(254, 254, 254);

    public CaretShape() {
        setText("Press INSERT key to enable/disable replace mode.");

        MyCaret c = new MyCaret();
        c.setBlinkRate(getCaret().getBlinkRate());
        setCaret(c);
        oldCaretColor = getCaretColor();
        addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (isInsertMode()) {
                    processCaretWidth();
                }
            }
        });

        Keymap kMap = this.getKeymap();
        Action a = new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -2384700975480276882L;

            @Override
            public void actionPerformed(ActionEvent e) {
                setInsertMode(!isInsertMode());
            }

        };
        kMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), a);
    }

    public static void main(String[] args) {
        JFrame fr = new JFrame("Custom caret shape for Overwrite mode JEditorPane");
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setSize(450, 270);
        final CaretShape cs = new CaretShape();
        fr.getContentPane().add(new JScrollPane(cs));
        fr.setLocationRelativeTo(null);
        fr.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                cs.setInsertMode(true);
            }
        });

    }

    public boolean isInsertMode() {
        return isInsertMode;
    }

    public void setInsertMode(boolean insertMode) {
        isInsertMode = insertMode;
        processMode();
    }

    private void processMode() {
        if (isInsertMode()) {
            processCaretWidth();
            setCaretColor(insertCaretColor);
        }

        else {
            setCaretColor(oldCaretColor);
            putClientProperty("caretWidth", 1);
        }
    }

    private void processCaretWidth() {
        try {
            int pos = getCaretPosition();
            Rectangle rPos = modelToView(pos) != null ? modelToView(pos).getBounds() : new Rectangle();
            int caretX = rPos.x;
            int caretEndX = rPos.x;
            if (pos < getDocument().getLength()) {
                Rectangle rNextPos = modelToView(pos + 1) != null ? modelToView(pos + 1).getBounds() : new Rectangle();

                if (rPos.y == rNextPos.y) {
                    caretEndX = rNextPos.x;
                }
            }
            putClientProperty("caretWidth", Math.max(1, caretEndX - caretX + 1));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void replaceSelection(String content) {
        if (isEditable() && isInsertMode() && getSelectionStart() == getSelectionEnd()) {
            int pos = getCaretPosition();
            int lastPos = Math.min(getDocument().getLength(), pos + content.length());
            select(pos, lastPos);
        }
        super.replaceSelection(content);
    }

    class MyCaret extends DefaultCaret {

        /**
         *
         */
        private static final long serialVersionUID = -2663986207122182832L;

        @Override
        public void paint(Graphics g) {
            if (isInsertMode()) {
                // we should shift to half width because of DefaultCaret rendering algorithm

                AffineTransform old = ((Graphics2D) g).getTransform();
                int w = (Integer) getClientProperty("caretWidth");
                g.setXORMode(Color.black);
                g.translate(w / 2, 0);
                super.paint(g);
                ((Graphics2D) g).setTransform(old);
            } else {
                super.paint(g);
            }

        }

        @Override
        protected synchronized void damage(Rectangle r) {
            if (isInsertMode()) {
                if (r != null) {
                    int damageWidth = (Integer) getClientProperty("caretWidth");
                    x = r.x - 4 - damageWidth / 2;
                    y = r.y;
                    width = 9 + 3 * damageWidth / 2;
                    height = r.height;
                    repaint();
                }
            } else {
                super.damage(r);
            }
        }
    }
}