package set.hyrts.org.apache.log4j.lf5.viewer;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class LogFactor5LoadingDialog extends LogFactor5Dialog {
   public LogFactor5LoadingDialog(JFrame jframe, String message) {
      super(jframe, "LogFactor5", false);
      JPanel bottom = new JPanel();
      bottom.setLayout(new FlowLayout());
      JPanel main = new JPanel();
      main.setLayout(new GridBagLayout());
      this.wrapStringOnPanel(message, main);
      this.getContentPane().add(main, "Center");
      this.getContentPane().add(bottom, "South");
      this.show();
   }
}