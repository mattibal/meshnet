package com.mattibal.meshnet.utils.color.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JSlider;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import com.mattibal.meshnet.utils.color.PlanckianLocus;

public class PlanckianLocusJFrame extends JFrame {
	
	private static final int MIN_TEMP = 1667;
	private static final int MAX_TEMP = 10000;
	public final static int MAX_Y_LUMINANCE = 2000;

	private JPanel contentPane;
	private JSlider tempSlider;
	private JLabel tempLabel;
	private JSlider lumenSlider;
	
	private CiexyYColorSelectedListener listener;
	private PlanckianLocus locus;
	
	
	public PlanckianLocusJFrame(CiexyYColorSelectedListener listener) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		this.listener = listener;
		locus = new PlanckianLocus();
		
		tempSlider = new JSlider(MIN_TEMP, MAX_TEMP, MIN_TEMP);
		contentPane.add(tempSlider);
		
		tempLabel = new JLabel("select temperature above");
		contentPane.add(tempLabel);
		
		lumenSlider = new JSlider(0, MAX_Y_LUMINANCE, 0);
		contentPane.add(lumenSlider);
		
		
		tempSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				onUpdated();
			}
		});
		lumenSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				onUpdated();
			}
		});
	}
	
	
	private void onUpdated(){
		int temp = tempSlider.getValue();
		tempLabel.setText(temp+" °K");
		locus.calculate(temp);
		listener.onCiexyYColorSelected(locus.getx(), locus.gety(), lumenSlider.getValue());
	}
	

}
