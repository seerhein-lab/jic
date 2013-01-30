package playground;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class PropConstTestClass {
	public PropConstTestClass(JButton button) {
		ActionListener listener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

			}
		};
		button.addActionListener(listener);
	}
}
