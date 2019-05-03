package prc_classes;

import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

public class PRC_FileChooser extends JFileChooser {
    protected JDialog createDialog(Component parent)
            throws HeadlessException {
        JDialog dlg = super.createDialog(parent);
        dlg.setLocationRelativeTo(null);
        dlg.setAlwaysOnTop (true);
        return dlg;
    }
}
