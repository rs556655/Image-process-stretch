import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class StretchFrame extends JFrame {

	private static final long serialVersionUID = 4390493953307669741L;
	JPanel cotrolPanel = new JPanel();
	ImagePanel leftImagePanel = new ImagePanel();
	ImagePanel rightImagePanel = new ImagePanel();
	JButton btnShow = new JButton("顯示"), 
			btnMinMax = new JButton("Min-max"), 
			btnHistogram = new JButton("Histogram");

	final int[][][] data;
	int height, width;
	BufferedImage img = null;
	
	ActionListener buttonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (e.getSource() == btnMinMax) rightImagePanel.showImage(width, height, processMinMax());
			else if (e.getSource() == btnHistogram) rightImagePanel.showImage(width, height, processHistogram());
			else leftImagePanel.showImage(width, height, data);
			
			refreshImagePanelBounds();
		}
	};
	
	private void refreshImagePanelBounds() {
		int space = StretchFrame.this.getWidth()- 2 * img.getWidth();
		space = space > 0 ? space / 3 : 0;
		
		leftImagePanel.setBounds(
				space,
				((StretchFrame.this.getHeight() - 100 - img.getHeight()) / 2),
				img.getWidth(), img.getHeight());
		
		rightImagePanel.setBounds(
				(space>0)?(2*space+img.getWidth()):(StretchFrame.this.getWidth()-img.getWidth()),
				((StretchFrame.this.getHeight() - 100 - img.getHeight()) / 2),
				img.getWidth(), img.getHeight());
	}
	
	protected StretchFrame(){
		setTitle("影像處理 Stretch by 410275024 陳品豪");
		
		try {
			img = ImageIO.read(new File("file/Munich_gray_dark.png"));
		    //img = ImageIO.read(new File("file/Munich_gray_dark_white_noise.png"));
		} catch (IOException e) {
			System.out.println("IO exception");
		}
		
		height = img.getHeight();
		width = img.getWidth();
		data = new int[height][width][3]; 
		
		this.setSize(width + 15, height + 77);
		
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				int rgb = img.getRGB(x, y);
				data[y][x][0] = Utils.getR(rgb);
				data[y][x][1] = Utils.getG(rgb);
				data[y][x][2] = Utils.getB(rgb);
			}
		
		// 事件監聽
		btnShow.addActionListener(buttonActionListener);
		btnMinMax.addActionListener(buttonActionListener);
		btnHistogram.addActionListener(buttonActionListener);
		this.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
			public void componentMoved(ComponentEvent e) {}
			public void componentResized(ComponentEvent e) {
				refreshImagePanelBounds();
			}
		});
		
		// 控制面板
		cotrolPanel.add(btnShow);
		cotrolPanel.add(new JPanel());
		cotrolPanel.add(btnMinMax);
		cotrolPanel.add(new JPanel());
		cotrolPanel.add(btnHistogram);
		cotrolPanel.add(new JPanel());
		
		JPanel temp = new JPanel();
		temp.setLayout(null);
		temp.add(leftImagePanel);
		temp.add(rightImagePanel);
		
		// 主畫面
		setLayout(new BorderLayout());	 
	    add(cotrolPanel, BorderLayout.PAGE_START);
	    add(temp, BorderLayout.CENTER);
	}
	
	private int [][][] processMinMax() {
		
		int [][][] ndata = new int [data.length][data[0].length][3];
		int [][] table = new int [data.length][data[0].length];
		
		int min = 255, max = 0;
		for (int y = 0; y < data.length; y++) {
			for (int x = 0; x < data[0].length; x++) {
				int [] color = data[y][x];
				int gray = Utils.getGrayScale(color[0], color[1], color[2]);
				if (gray > max) max = gray;
				else if (gray < min) min = gray;
				
				table[y][x] = gray;
			}
		}
		
		for (int y = 0; y < data.length; y++) 
			for (int x = 0; x < data[0].length; x++) 
				ndata[y][x][0] = ndata[y][x][1] = ndata[y][x][2] 
						= (int) Math.round((255.0 * (table[y][x] - min)) / (max - min));
		return ndata;
	}
	
	private int [][][] processHistogram() {
		
		int [][][] ndata = new int [data.length][data[0].length][3];
		int [][] table = new int [data.length][data[0].length];
		
		CDF cdf = new CDF(256);
		
		for (int y = 0; y < data.length; y++) {
			for (int x = 0; x < data[0].length; x++) {
				int [] color = data[y][x];
				int gray = Utils.getGrayScale(color[0], color[1], color[2]);
				table[y][x] = gray;
				cdf.addAt(gray);
			}
		}
		
		for (int y = 0; y < data.length; y++) {
			for (int x = 0; x < data[0].length; x++) {
				ndata[y][x][0] = ndata[y][x][1] = ndata[y][x][2] 
						= (int) Math.round(1.0*(255*cdf.getAt(table[y][x]))/(data.length*data[0].length));
			}
		}
		
		return ndata;
	}
	
	private class CDF {
		int [] record, accumulation;
		boolean isReady = false;
		
		public CDF(int amount) {
			record = new int [amount];
		}
		
		public void addAt(int p) {
			if (p < 0 || p > record.length) return;
			isReady = false;
			record[p]++;
		}
		
		public int getAt(int p) {
			if (!isReady) accumulate();
			if (p < 0 || p > record.length) return 0;
			return accumulation[p];
		}
		
		private void accumulate() {
			accumulation = new int [record.length];
			for (int i = 0; i < record.length; i++) 
				for (int j = i; j < record.length; j++) 
					accumulation[j] += record[i];
			isReady = true;
		}
	}
}
