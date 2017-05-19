import javax.swing.JFrame;


public class MainGUI {

	public static void main(String[] args) {
		JFrame frame = new StretchFrame(); 
		frame.setSize(1300, 600);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
