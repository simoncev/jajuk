/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * $Log$
 * Revision 1.1  2003/10/07 21:02:22  bflorat
 * Initial commit
 *
 */
package org.jajuk.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.jajuk.base.*;
import org.jajuk.i18n.*;

/**
 *  Command panel
 *
 * @author     bflorat
 * @created    3 oct. 2003
 */
public class CommandJPanel extends JPanel implements TechnicalStrings{
	//widgets declaration
		JToolBar jtbSearch;
			JTextField jtfSearch;
		JToolBar jtbHistory;
			JComboBox jcbHistory;
		JToolBar jtbMode;
			JButton jbRepeat;
			JButton jbRandom;
			JButton jbContinue;
			JButton jbIntro;
		JToolBar jtbSpecial;
			JButton jbGlobalRandom;
			JButton jbBestof;
			JButton jbMute;
		JToolBar jtbPlay;
			JButton jbUp;
			JButton jbDown;
			JButton jbRew;
			JButton jbPlayPause;
			JButton jbStop;
			JButton jbFwd;
		JToolBar jtbVolume;
			JLabel jlVolume;
			JSlider jsVolume;
		JToolBar jtbPosition;
			JLabel jlPosition;
			JSlider jsPosition;
		
	public CommandJPanel(){
		//set default layout and size
		setLayout(new BoxLayout(this,BoxLayout.X_AXIS)); //we use a BoxLayout and not a FlowLayout to allow resizing
		
		//dimensions
		int height1 = 25;  //buttons, components
		int height2 = 36; //slider ( at least this height in the gtk+ l&f ) 
		Dimension d25_1 = new Dimension(25,height1);
		Dimension d50_1 = new Dimension(50,height1);
		Dimension d75_1 = new Dimension(75,height1);
		Dimension d90_1 = new Dimension(90,height1);
		Dimension d100_1 = new Dimension(100,height1);
		Dimension d125_1 = new Dimension(125,height1);
		Dimension d160_1 = new Dimension(160,height1);
		Dimension d180_1 = new Dimension(180,height1);
		Dimension d200_1 = new Dimension(200,height1);
		Dimension d225_1 = new Dimension(225,height1);
		Dimension d230_1 = new Dimension(230,height1);
		Dimension d250_1 = new Dimension(250,height1);
		
		Dimension d50_2 = new Dimension(50,height2);
		Dimension d125_2 = new Dimension(125,height2);
		Dimension d160_2 = new Dimension(160,height2);
		Dimension d225_2 = new Dimension(225,height2);
		Dimension d200_2 = new Dimension(200,height2);
		
				
		//search toolbar
		jtbSearch = new JToolBar();
		jtbSearch.setMinimumSize(d50_2); //We set the same sizes to the toolbar and the text field to avoid getting blanks
		jtbSearch.setPreferredSize(d200_2);
		jtbSearch.setMaximumSize(d200_2);
		jtfSearch = new JTextField();
		jtfSearch.setToolTipText(Messages.getString("CommandJPanel.search_1")); //$NON-NLS-1$
		jtfSearch.setMinimumSize(d50_1);
		jtfSearch.setPreferredSize(d200_1);
		jtfSearch.setMaximumSize(d200_1);
		jtbSearch.add(jtfSearch);
		
		//history toolbar
		jtbHistory = new JToolBar();
		jtbHistory.setMinimumSize(d50_2);
		jtbHistory.setPreferredSize(d225_2);
		jtbHistory.setMaximumSize(d225_2);
		jcbHistory = new JComboBox();
		jcbHistory.setMinimumSize(d25_1);
		jcbHistory.setPreferredSize(d200_1);
		jcbHistory.setMaximumSize(d200_1);
		jcbHistory.setToolTipText(Messages.getString("CommandJPanel.play_history_1")); //$NON-NLS-1$
		jtbHistory.add(jcbHistory);
		
		//Mode toolbar
		jtbMode = new JToolBar();
		jtbMode.setMinimumSize(d160_2);
		jtbMode.setPreferredSize(d160_2);
		jtbMode.setMaximumSize(d160_2);
		jbRepeat = new JButton(new ImageIcon(ICON_REPEAT)); 
		jbRepeat.setToolTipText(Messages.getString("CommandJPanel.repeat_mode___play_selection_in_a_loop_1")); //$NON-NLS-1$
		jtbMode.add(jbRepeat);
		jtbMode.addSeparator();
		jbRandom = new JButton(new ImageIcon(ICON_SHUFFLE));
		jbRandom.setToolTipText(Messages.getString("CommandJPanel.shuffle_mode___play_a_random_track_from_the_selection_2")); //$NON-NLS-1$
		jtbMode.add(jbRandom);
		jtbMode.addSeparator();
		jbContinue = new JButton(new ImageIcon(ICON_CONTINUE)); 
		jbContinue.setToolTipText(Messages.getString("CommandJPanel.continue_mode___continue_to_play_next_tracks_when_finished_3")); //$NON-NLS-1$
		jtbMode.add(jbContinue);
		jtbMode.addSeparator();
		jbIntro = new JButton(new ImageIcon(ICON_FILTER)); 
		jbIntro.setToolTipText(Messages.getString("CommandJPanel.intro_mode___play_just_a_part_of_each_track_offset_and_time_can_be_set_in_the_parameters_view_4")); //$NON-NLS-1$
		jtbMode.add(jbIntro);
		
		//Special functions toolbar
		jtbSpecial = new JToolBar();
		jtbSpecial.setMinimumSize(d125_2);
		jtbSpecial.setPreferredSize(d125_2);
		jtbSpecial.setMaximumSize(d125_2);
		jbGlobalRandom = new JButton(new ImageIcon(ICON_ROLL)); 
		jbGlobalRandom.setToolTipText(Messages.getString("CommandJPanel.Play_a_shuffle_selection_from_the_entire_collection_1")); //$NON-NLS-1$
		jtbSpecial.add(jbGlobalRandom);
		jtbSpecial.addSeparator();
		jbBestof = new JButton(new ImageIcon(ICON_BESTOF)); 
		jbBestof.setToolTipText(Messages.getString("CommandJPanel.Play_your_own_favorite_tracks_2")); //$NON-NLS-1$
		jtbSpecial.add(jbBestof);
		jtbSpecial.addSeparator();
		jbMute = new JButton(new ImageIcon(ICON_MUTE)); 
		jbMute.setToolTipText(Messages.getString("CommandJPanel.Turn_sound_off_3")); //$NON-NLS-1$
		jtbSpecial.add(jbMute);
		
		//Play toolbar
		jtbPlay = new JToolBar();
		jtbPlay.setMinimumSize(d200_2);
		jtbPlay.setPreferredSize(d200_2);
		jtbPlay.setMaximumSize(d200_2);
		jbUp = new JButton(new ImageIcon(ICON_UP)); 
		jbUp.setToolTipText(Messages.getString("CommandJPanel.Play_previous_track_in_current_selection_4")); //$NON-NLS-1$
		jtbPlay.add(jbUp);
		jbDown = new JButton(new ImageIcon(ICON_DOWN)); 
		jbDown.setToolTipText(Messages.getString("CommandJPanel.Play_next_track_in_current_selection_5")); //$NON-NLS-1$
		jtbPlay.add(jbDown);
		jtbPlay.addSeparator();
		jbRew = new JButton(new ImageIcon(ICON_REW)); 
		jbRew.setToolTipText(Messages.getString("CommandJPanel.Fast_rewind_in_current_track_6")); //$NON-NLS-1$
		jtbPlay.add(jbRew);
		jbPlayPause = new JButton(new ImageIcon(ICON_PLAY)); 
		jbPlayPause.setToolTipText(Messages.getString("CommandJPanel.Play/pause_current_track_7")); //$NON-NLS-1$
		jtbPlay.add(jbPlayPause);
		jbStop = new JButton(new ImageIcon(ICON_STOP)); 
		jbStop.setToolTipText(Messages.getString("CommandJPanel.Stop_current_track_8")); //$NON-NLS-1$
		jtbPlay.add(jbStop);
		jbFwd = new JButton(new ImageIcon(ICON_FWD)); 
		jbFwd.setToolTipText(Messages.getString("CommandJPanel.Fast_forward_in_current_track_9")); //$NON-NLS-1$
		jtbPlay.add(jbFwd);
		
		//TODO integration seb
		
		//Volume toolbar
		jtbVolume = new JToolBar();
		jtbVolume.setMinimumSize(d50_1);
		jtbVolume.setPreferredSize(d200_2);
		jtbVolume.setMaximumSize(d200_2);
		jlVolume = new JLabel(new ImageIcon(ICON_VOLUME)); 
		jtbVolume.add(jlVolume);
		jtbVolume.addSeparator();
		jsVolume = new JSlider(0,100,50);
		jsVolume.setToolTipText(Messages.getString("CommandJPanel.Volume_1")); //$NON-NLS-1$
		jsVolume.setMinimumSize(d50_2);
		jsVolume.setPreferredSize(d200_2);
		jsVolume.setMaximumSize(d200_2);
		jtbVolume.add(jsVolume);
		
		//Position toolbar
		jtbPosition = new JToolBar();
		jtbPosition.setMinimumSize(d50_2);
		jtbPosition.setPreferredSize(d200_2);
		jtbPosition.setMaximumSize(d200_2);
		jlPosition = new JLabel(new ImageIcon(ICON_POSITION)); 
		jtbPosition.add(jlPosition);
		jtbPosition.addSeparator();
		jsPosition = new JSlider(0,100,50);
		jsPosition.setToolTipText(Messages.getString("CommandJPanel.Go_to_this_position_in_the_played_track_2")); //$NON-NLS-1$
		jsPosition.setMinimumSize(d50_2);
		jsPosition.setPreferredSize(d200_2);
		jsPosition.setMaximumSize(d200_2);
		jtbPosition.add(jsPosition);
				
		//add toolbars to main panel
		add(jtbSearch);
		add(jtbHistory);
		add(jtbMode);
		add(jtbSpecial);
		add(jtbPlay);
		add(jtbVolume);
		add(jtbPosition);
		
		
	}

	
	
	
}
