/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA. 
 * $Revision$
 */

package org.jajuk.ui.views;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;

/**
 * Logical table view
 * 
 * @author bflorat 
 * @created 13 dec. 2003
 */
public class LogicalTableView extends AbstractTableView implements Observer{

	/** Self instance */
	private static LogicalTableView ltv;

		
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Logical table view";
	}
	

	/** Return singleton */
	public static LogicalTableView getInstance() {
		if (ltv == null) {
			ltv = new LogicalTableView();
		}
		return ltv;
	}

	/** Constructor */
	public LogicalTableView(){
		super();
		ltv = this;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void display(){
		super.display();
		populate();
		//Register on the list for subject we are interrested in
		ObservationManager.register(EVENT_DEVICE_MOUNT,this);
		ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
		ObservationManager.register(EVENT_DEVICE_REFRESH,this);
		update(EVENT_DEVICE_REFRESH);  //force the first refresh

	}
	
	/**Fill the tree */
	public void populate(){
		//col number
		iColNum = 6;
		//Columns names
		sColName = new String[]{"Track","Album","Author","Length","Style","Rate"};
		//Values
		ArrayList alTracks = TrackManager.getSortedTracks();
		int iSize = alTracks.size();
		Iterator it = alTracks.iterator();
		oValues = new Object[iSize][iColNum];
		//Track | Album | Author | Length | Style | Rate	
		for (int i = 0;it.hasNext();i++){
			Track track = (Track)it.next(); 
			oValues[i][0] = track.getName();
			oValues[i][1] = track.getAlbum().getName2();
			oValues[i][2] = track.getAuthor().getName2();
			oValues[i][3] = Long.toString(track.getLength());
			oValues[i][4] = track.getStyle().getName2();
			oValues[i][5] = Long.toString(track.getRate());
		}
		//row num
		iRowNum = iSize;
		//edtiable table  and class 
		bCellEditable = new boolean[8][iSize];
		for (int i =0;i<8;i++){
			for (int j=0;j<iSize;j++){
				bCellEditable[i][j]=false;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.LogicalTableView";
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		//let super class to test common ( physical/logical ) events 
		super.actionPerformed(e);
		//then specifics
	}
	
	
}


