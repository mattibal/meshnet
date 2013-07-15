package com.mattibal.meshnet.utils.color.gui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.mattibal.meshnet.utils.color.gui.ChromaticityJFrame.ChromaticityPanel;
import com.sun.org.apache.xml.internal.utils.NSInfo;

public class ChromaticityUCSJFrame extends JFrame{

	public final static int MAX_Y_LUMINANCE = 2000;
	public final static int pointPxSize = 6;

	private ChromaticityUCSPanel contentPane;
	private CiexyYColorSelectedListener clickedListener = null;

	private double curru;
	private double currv;
	private double currY;
	
	private double currx, curry;
	
	private FadingThread fading = null;

	/**
	 * Create the frame.
	 */
	private ChromaticityUCSJFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 50, 650, 680);
		contentPane = new ChromaticityUCSPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		final JSlider slider = new JSlider(0, MAX_Y_LUMINANCE, 0);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				currY = slider.getValue();
				clickedListener.onCiexyYColorSelected(currx, curry, currY);
			}
		});
		getContentPane().add(slider, BorderLayout.NORTH);
	}

	public ChromaticityUCSJFrame(CiexyYColorSelectedListener listener){
		this();
		this.clickedListener = listener;
	}


	protected static final int CIEXY_MARGIN_SX = 31;
	protected static final int CIEXY_MARGIN_TOP = 31;
	protected static final int CIEXY_GRAPH_WIDTH_PX = 541;
	protected static final int CIEXY_GRAPH_HEIGHT_PX = 541;
	protected static final double CIEXY_GRAPH_WIDTH_COORD = 0.6;
	protected static final double CIEXY_GRAPH_HEIGHT_COORD = 0.6;


	public void addChromaticityPoint(double x, double y){
		double u, v;
		u = (4*x)/(12*y-2*x+3);
		v = (9*y)/(12*y-2*x+3);
		int pX, pY; // panel point coordinates
		pX = CIEXY_MARGIN_SX + (int)((CIEXY_GRAPH_WIDTH_PX * u)/CIEXY_GRAPH_WIDTH_COORD);
		pY = CIEXY_MARGIN_TOP + (int)((CIEXY_GRAPH_HEIGHT_PX * (CIEXY_GRAPH_HEIGHT_COORD-v))/CIEXY_GRAPH_HEIGHT_COORD);
		contentPane.addPoint(new Point(pX, pY));
	}


	public class ChromaticityUCSPanel extends JPanel {

		private Image ciexyImage;
		private Point clickedPoint;
		private HashSet<Point> displayedPoints = new HashSet<Point>();

		public ChromaticityUCSPanel(){

			try {
				ciexyImage = ImageIO.read(new File("cie1976ucs.png"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					super.mousePressed(e);

					clickedPoint = e.getPoint();
					
					if(fading != null){
						fading.interrupt();
					}
					
					double u = ((e.getX() - CIEXY_MARGIN_SX)*CIEXY_GRAPH_WIDTH_COORD)/CIEXY_GRAPH_WIDTH_PX;
					double v = CIEXY_GRAPH_HEIGHT_COORD-(((e.getY() - CIEXY_MARGIN_TOP)*CIEXY_GRAPH_HEIGHT_COORD)/CIEXY_GRAPH_HEIGHT_PX);

					if(!SwingUtilities.isRightMouseButton(e)){
						if(clickedListener != null){
							
							curru=u;
							currv=v;
							updatexy();

							clickedListener.onCiexyYColorSelected(currx, curry, currY);
						}
					} else {
						// Start color fading
						fading = new FadingThread(curru, currv, u, v);
						fading.start();
					}
				}
				
				
			});
		}


		protected void addPoint(Point point){
			displayedPoints.add(point);
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			// Draw background image
			g.drawImage(ciexyImage, 0, 0, null);

			// Draw the points
			for(Point point : displayedPoints){
				g.fillRect(point.x, point.y, pointPxSize, pointPxSize);
			}
		}
	}
	
	private void updatexy(){
		currx = (9*curru)/(6*curru-16*currv+12);
		curry = (4*currv)/(6*curru-16*currv+12);
	}
	
	
	
	private class FadingThread extends Thread {
		
		double u1, v1, u2, v2;
		
		long fadeDurationMs = 10000;
		long msStep = 50;
		double uincstep;
		double vincstep;
		
		
		public FadingThread(double u1, double v1, double u2, double v2){
			this.u1=u1;
			this.v1=v1;
			this.u2=u2;
			this.v2=v2;
			
			double nStep = fadeDurationMs/msStep;
			uincstep = (Math.max(u1, u2) - Math.min(u1, u2)) / nStep;
			vincstep = (Math.max(v1, v2) - Math.min(v1, v2)) / nStep;
		}
		
		@Override
		public void run() {
			super.run();

			try {
				while(!Thread.interrupted()){

					Thread.sleep(msStep);

					curru += uincstep;
					currv += vincstep;

					if(curru > Math.max(u1, u2) || curru < Math.min(u1, u2)){
						uincstep = 0-uincstep;
					}
					if(currv > Math.max(v1, v2) || currv < Math.min(v1, v2)){
						vincstep = 0-vincstep;
					}

					updatexy();
					clickedListener.onCiexyYColorSelected(currx, curry, currY);


				}
			} catch (InterruptedException e) {
			}
		}
	}
	

}
