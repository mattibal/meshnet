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

import com.mattibal.meshnet.utils.color.HuslConverter;

public class HuslChooserJFrame extends JFrame {
	
	public final static int CHOOSER_WIDTH_PIXEL = 360;
	public final static int CHOOSER_HEIGHT_PIXEL = 100;
	
	private int l = 50;
	private double h, s;

	private HuslChooserJPanel contentPane;
	
	private final CieXYZColorSelectedListener clickedListener;

	/**
	 * Create the frame.
	 */
	public HuslChooserJFrame(CieXYZColorSelectedListener clickedListener) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new HuslChooserJPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		this.clickedListener = clickedListener;
		
		final JSlider slider = new JSlider(0, 100, 0);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				l = slider.getValue();
				contentPane.drawColorChooser();
				contentPane.repaint();
				updateLedColor();
			}
		});
		
		getContentPane().add(slider, BorderLayout.NORTH);
	}
	
	
	private void updateLedColor(){
		double[] XYZ = HuslConverter.HUSLtoXYZ(h, s, l);
		clickedListener.onCieXYZColorSelected(XYZ[0], XYZ[1], XYZ[2]);
	}
	
	

	private class HuslChooserJPanel extends JPanel {

		private BufferedImage image;



		public HuslChooserJPanel(){
			image = new BufferedImage(CHOOSER_WIDTH_PIXEL,CHOOSER_HEIGHT_PIXEL, BufferedImage.TYPE_INT_ARGB);

			drawColorChooser();

			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					super.mousePressed(e);

					Point clickedPoint = e.getPoint();
					h = clickedPoint.getX();
					s = clickedPoint.getY();
					
					updateLedColor();

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
					double[] rgbdouble = HuslConverter.HUSLtoRGB(x, y, l);
					Color color = new Color((float)rgbdouble[0], (float)rgbdouble[1], (float)rgbdouble[2]);
					image.setRGB(x, y, color.getRGB());
				}
			}
		}

	}
}
