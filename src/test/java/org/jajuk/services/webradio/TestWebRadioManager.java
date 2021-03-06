/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.services.webradio;

import java.util.List;

import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;

public class TestWebRadioManager extends JajukTestCase {
  private WebRadioManager man = WebRadioManager.getInstance();
  WebRadio radio1;
  WebRadio radio2;
  WebRadio radio3;
  WebRadio radio4;

  @Override
  public void specificSetUp() throws Exception {
    man.cleanup();
    // Fill few radio
    radio1 = TestHelpers.getWebRadio("Preset1", "http://preset1", WebRadioOrigin.PRESET);
    radio2 = TestHelpers.getWebRadio("Preset2", "http://preset2", WebRadioOrigin.PRESET);
    radio3 = TestHelpers.getWebRadio("Custom1", "http://custom1", WebRadioOrigin.CUSTOM);
    radio4 = TestHelpers.getWebRadio("Custom2", "http://custom2", WebRadioOrigin.CUSTOM);
  }

  public void testGetWebRadiosByOrigin() throws Exception {
    List<WebRadio> shouldBeCustom = man.getWebRadiosByOrigin(WebRadioOrigin.CUSTOM);
    List<WebRadio> shouldBePreset = man.getWebRadiosByOrigin(WebRadioOrigin.PRESET);
    assertTrue(shouldBeCustom.size() == 2 && shouldBeCustom.contains(radio3));
    assertTrue(shouldBePreset.size() == 2 && shouldBePreset.contains(radio2));
  }
}
