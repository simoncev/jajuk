/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.ui.thumbnails;

import ext.SwingWorker;
import ext.services.lastfm.AudioScrobblerAlbum;
import ext.services.lastfm.AudioScrobblerService;
import ext.services.lastfm.AudioScrobblerTrack;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Item;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.border.DropShadowBorder;

/**
 * Last.FM Album thumb represented as album cover + (optionally) others text
 * information display...
 */
public class AudioScrobblerAlbumThumbnail extends AbstractThumbnail {

  private static final long serialVersionUID = -804471264407148566L;

  /** Associated album */
  private AudioScrobblerAlbum album;

  /** Popup thumbnail cache */
  private File fThumb;

  /** Is this author known in collection ? */
  private boolean bKnown;

  /**
   * @param album :
   *          associated album
   */
  public AudioScrobblerAlbumThumbnail(AudioScrobblerAlbum album) {
    super(100);
    this.album = album;
    bKnown = (AlbumManager.getInstance().getAlbumByName(album.getTitle()) != null);
  }

  @Override
  public void populate() throws Exception {
    jlIcon = new JLabel();

    SwingWorker sw = new ext.SwingWorker() {

      ImageIcon ii;

      @Override
      public Object construct() {
        try {
          // Check if album image is null
          String albumUrl = album.getCoverURL();
          if (UtilString.isVoid(albumUrl)) {
            return null;
          }
          // Download thumb
          URL remote = new URL(albumUrl);
          // Download image and store file reference (to generate the
          // popup thumb for ie)
          fCover = DownloadManager.downloadToCache(remote);
          fThumb = UtilSystem.getConfFileByPath(Const.FILE_CACHE + "/" + System.currentTimeMillis()
              + '.' + UtilSystem.getExtension(fCover));
          BufferedImage image = ImageIO.read(fCover);
          ImageIcon downloadedImage = new ImageIcon(image);
          ii = UtilGUI.getScaledImage(downloadedImage, 100);
          // Free images memory
          downloadedImage.getImage().flush();
          image.flush();
        } catch (Exception e) {
          Log.error(e);
        }
        return null;
      }

      @Override
      public void finished() {
        // Check if author is null
        if (ii == null) {
          return;
        }
        super.finished();
        postPopulate();
        jlIcon.setIcon(ii);
        setLayout(new VerticalLayout(2));
        // Use a panel to allow text to be bigger than image under it
        add(UtilGUI.getCentredPanel(jlIcon));
        JLabel jlTitle;
        if (bKnown) {
          // Album known in collection, display its name in bold
          jlTitle = new JLabel(UtilString.getLimitedString(album.getTitle(), 15), IconLoader
              .getIcon(JajukIcons.ALBUM), JLabel.CENTER);
          jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
        } else {
          jlTitle = new JLabel(UtilString.getLimitedString(album.getTitle(), 15));
          jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
        }
        jlTitle.setToolTipText(album.getTitle());
        add(jlTitle);
        jlIcon.setBorder(new DropShadowBorder(Color.BLACK, 5, 0.5f, 5, false, true, false, true));
        // disable inadequate menu items
        jmiCDDBWizard.setEnabled(false);
        jmiGetCovers.setEnabled(false);
        if (getItem() == null) {
          jmiDelete.setEnabled(false);
          jmiPlay.setEnabled(false);
          jmiPlayRepeat.setEnabled(false);
          jmiPlayShuffle.setEnabled(false);
          jmiPush.setEnabled(false);
          jmiProperties.setEnabled(false);
        }
        // Set URL to open
        jmiOpenLastFMSite.putClientProperty(Const.DETAIL_CONTENT, album.getUrl());

      }

    };
    sw.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#getItem()
   */
  @Override
  public Item getItem() {
    Album item = AlbumManager.getInstance().getAlbumByName(album.getTitle());
    if (item != null) {
      return item;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#getDescription()
   */
  @Override
  public String getDescription() {
    // populate album detail
    if (album.getTracks() == null) {
      AudioScrobblerAlbum lAlbum = AudioScrobblerService.getInstance().getAlbum(
          this.album.getArtist(), this.album.getTitle());
      if (lAlbum != null) {
        this.album = lAlbum;
      }
    }
    Color bgcolor = UtilGUI.getUltraLightColor();
    Color fgcolor = UtilGUI.getForegroundColor();
    String sOut = "<html bgcolor='#" + UtilGUI.getHTMLColor(bgcolor) + "'><TABLE color='"
        + UtilGUI.getHTMLColor(fgcolor) + "'><TR><TD VALIGN='TOP'> <b>" + "<a href='file://"
        + Const.XML_URL + '?' + album.getUrl() + "'>" + album.getTitle() + "</a>" + "</b><br><br>";
    // display cover
    sOut += "<img src='" + album.getCoverURL() + "'><br>";
    // Display author as global value only if it is a single author album
    // We use file://<item type>?<item id> as HTML hyperlink format
    sOut += "<br>" + Messages.getString("Property_author") + " : " + "<a href='file://"
        + Const.XML_URL + '?' + album.getArtistUrl() + "'>" + album.getArtist() + "</a>";
    // Display year if available
    String year = album.getYear();
    if (!UtilString.isVoid(year)) {
      sOut += "<br>" + Messages.getString("Property_year") + " : " + year;
    }
    sOut += "</TD><TD>";
    // Show each track detail if available
    if (album.getTracks() != null) {
      for (AudioScrobblerTrack track : album.getTracks()) {
        sOut += "<b>" + "<a href='file://" + Const.XML_URL + '?' + track.getUrl() + "'>"
            + track.getTitle() + "</a></b><br>";
      }
    }
    sOut += "</TD></TR></TABLE></html>";
    return sOut;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#launch()
   */
  @Override
  public void launch() {
    if (getItem() != null) {
      // play the album
      jmiPlay.doClick();
    } else {
      // Open the last.FM page
      jmiOpenLastFMSite.doClick();
    }
  }

}
