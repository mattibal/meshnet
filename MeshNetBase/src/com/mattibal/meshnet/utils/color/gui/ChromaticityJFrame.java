package com.mattibal.meshnet.utils.color.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

public class ChromaticityJFrame extends JFrame {
	
	public final static int pointPxSize = 6;

	private ChromaticityPanel contentPane;
	private CieXYPointClickedListener clickedListener = null;

	/**
	 * Create the frame.
	 */
	public ChromaticityJFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 50, 650, 680);
		contentPane = new ChromaticityPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
	}
	
	public ChromaticityJFrame(CieXYPointClickedListener listener){
		this();
		this.clickedListener = listener;
	}
	
	
	protected static final int CIEXY_MARGIN_SX = 46;
	protected static final int CIEXY_MARGIN_TOP = 20;
	protected static final int CIEXY_GRAPH_WIDTH_PX = 471;
	protected static final int CIEXY_GRAPH_HEIGHT_PX = 529;
	protected static final double CIEXY_GRAPH_WIDTH_COORD = 0.8;
	protected static final double CIEXY_GRAPH_HEIGHT_COORD = 0.9;
	
	
	public void addChromaticityPoint(double x, double y){
		int pX, pY; // panel point coordinates
		pX = CIEXY_MARGIN_SX + (int)((CIEXY_GRAPH_WIDTH_PX * x)/CIEXY_GRAPH_WIDTH_COORD);
		pY = CIEXY_MARGIN_TOP + (int)((CIEXY_GRAPH_HEIGHT_PX * (CIEXY_GRAPH_HEIGHT_COORD-y))/CIEXY_GRAPH_HEIGHT_COORD);
		contentPane.addPoint(new Point(pX, pY));
	}
	
	
	public class ChromaticityPanel extends JPanel {
		
		private Image ciexyImage;
		private Point clickedPoint;
		private HashSet<Point> displayedPoints = new HashSet<Point>();
		
		public ChromaticityPanel(){
			
			try {
				ciexyImage = ImageIO.read(new File("ciexy.png"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					
					clickedPoint = e.getPoint();
					
					// TODO do something when I click
					if(clickedListener != null){
						double x, y;
						x = ((e.getX() - CIEXY_MARGIN_SX)*CIEXY_GRAPH_WIDTH_COORD)/CIEXY_GRAPH_WIDTH_PX;
						y = CIEXY_GRAPH_HEIGHT_COORD-(((e.getY() - CIEXY_MARGIN_TOP)*CIEXY_GRAPH_HEIGHT_COORD)/CIEXY_GRAPH_HEIGHT_PX);
						clickedListener.onCieXYPointClicked(x, y);
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
	
	
	public interface CieXYPointClickedListener {
		public void onCieXYPointClicked(double x, double y);
	}

}
