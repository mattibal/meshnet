package com.mattibal.meshnet.utils.color.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.mattibal.meshnet.utils.color.CIELab;
import com.mattibal.meshnet.utils.color.ColourConverter;
import com.mattibal.meshnet.utils.color.ColourConverter.WhitePoint;

public class LabChooserJFrame extends JFrame {
	
	public final static int CHOOSER_WIDTH_PIXEL = 200;
	public final static int CHOOSER_HEIGHT_PIXEL = 200;
	
	private int currL = 50;

	private LabChooserJPanel contentPane;
	
	private final CieXYZColorSelectedListener clickedListener;


	/**
	 * Create the frame.
	 */
	public LabChooserJFrame(CieXYZColorSelectedListener clickedListener) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new LabChooserJPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		this.clickedListener = clickedListener;
		
		final JSlider slider = new JSlider(0, 100, 0);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				currL = slider.getValue();
				contentPane.drawColorChooser();
				contentPane.repaint();
			}
		});
		getContentPane().add(slider, BorderLayout.NORTH);
	}
	
	
	private class LabChooserJPanel extends JPanel {
		
		private BufferedImage image;
		private CIELab cielab = new CIELab();
		
		
		
		public LabChooserJPanel(){
			image = new BufferedImage(CHOOSER_WIDTH_PIXEL,CHOOSER_HEIGHT_PIXEL, BufferedImage.TYPE_INT_ARGB);
			
			drawColorChooser();
			
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					super.mousePressed(e);
					
					Point clickedPoint = e.getPoint();
					double L = currL;
					double a = clickedPoint.getX()-100;
					double b = clickedPoint.getY()-100;
					double[] Lab = {L, a, b};
					double[] XYZ = ColourConverter.LABtoXYZ(Lab, WhitePoint.D65);
					clickedListener.onCieXYZColorSelected(XYZ[0], XYZ[1], XYZ[2]);
					
				}
			});
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			// TODO Auto-generated method stub
			super.paintComponent(g);
			
			g.drawImage(image, 0, 0, null);
		}
		
		
		private void drawColorChooser(){
			for(int x=0; x<CHOOSER_WIDTH_PIXEL; x++){
				for(int y=0; y<CHOOSER_HEIGHT_PIXEL; y++){
					Color color = cielab.getColour(currL, x-100, y-100, false);
					if(color==null){
						color = Color.GRAY;
					}
					image.setRGB(x, y, color.getRGB());
				}
			}
		}
		
	}

}
