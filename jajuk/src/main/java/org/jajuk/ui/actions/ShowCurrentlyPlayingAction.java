/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
 *  http://jajuk.info
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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * A simple action to allow to display the currently playing track via the
 * Notification System. This is used to have a keyboard shortcut which displays
 * information about the current song.
 *
 */
public class ShowCurrentlyPlayingAction extends SelectionAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  ShowCurrentlyPlayingAction() {
    super(Messages.getString("ShowCurrentlyPlayingAction.0"), IconLoader.getIcon(JajukIcons.PLAY_16X16), "ctrl alt N", true, true);
    setShortDescription(Messages.getString("ShowCurrentlyPlayingAction.1"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.SelectionAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent e) throws Exception {
    // simply invoke the necessary event via the observer mechanism
    ObservationManager.notify(new JajukEvent(JajukEvents.SHOW_CURRENTLY_PLAYING));
  }
}