/*
 *  Jajuk
 *  Copyright (C) 2007 bflorat
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
 *  $Revision$
 */

package org.jajuk.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;

import com.vlsolutions.swing.toolbars.BackgroundPainter;

/**
 *  Type description
 *
 * @author     Bertrand Florat
 * @created    27 févr. 07
 */
public class JajukBackgroundPainter extends BasicGradientPainter implements BackgroundPainter {

	/* (non-Javadoc)
	 * @see com.vlsolutions.swing.toolbars.BackgroundPainter#paintBackground(javax.swing.JComponent, java.awt.Graphics)
	 */
	public void paintBackground(JComponent component, Graphics g) {
		super.paintBackground((Graphics2D)g, component);
	}
	
	public JajukBackgroundPainter(){
		super(BasicGradientPainter.GRAY);
	}

}
