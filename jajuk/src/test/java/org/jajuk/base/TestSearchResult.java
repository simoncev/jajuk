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
 *  $Revision: 3132 $
 */
package org.jajuk.base;

import junit.framework.TestCase;

import org.jajuk.JUnitHelpers;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.services.webradio.WebRadio;

/**
 * 
 */
public class TestSearchResult extends TestCase {

  /**
   * Test method for {@link org.jajuk.base.SearchResult#hashCode()}.
   * @throws Exception 
   */
  public void testHashCode() throws Exception {
    // TODO: this fails currently because there is no equals in SearchResult, should we add one? For now we just cover hashCode()

    // hashcode only looks at "sResu" parameter
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "");
    SearchResult equ = new SearchResult(JUnitHelpers.getFile("file2", true), "");

    assertEquals(res.hashCode(), equ.hashCode());

    //JUnitHelpers.HashCodeTest(res, equ);

    //    res = new SearchResult(new WebRadio("web", "url"), "webradio");
    //    equ = new SearchResult(new WebRadio("web", "url"), "webradio");
    //    JUnitHelpers.HashCodeTest(res, equ);
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#SearchResult(org.jajuk.base.File)}.
   * @throws Exception 
   */
  public void testSearchResultFile() throws Exception {
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "");
    assertNotNull(res);
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#SearchResult(org.jajuk.base.File, java.lang.String)}.
   * @throws Exception 
   */
  public void testSearchResultFileString() throws Exception {
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "testresult");
    assertNotNull(res);

  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#SearchResult(org.jajuk.services.webradio.WebRadio, java.lang.String)}.
   */
  public void testSearchResultWebRadioString() {
    SearchResult res = new SearchResult(new WebRadio("web", "testurl"), "testresult");
    assertNotNull(res);
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#compareTo(org.jajuk.base.SearchResult)}.
   * @throws Exception 
   */
  public void testCompareTo() throws Exception {
    // compareTo only looks at sResu-parameter
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "testresu");
    SearchResult equ = new SearchResult(JUnitHelpers.getFile("file2", true), "testresu");
    SearchResult notequ = new SearchResult(JUnitHelpers.getFile("file2", true), "testresu1");
    JUnitHelpers.CompareToTest(res, equ, notequ);
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#getFile()}.
   * @throws Exception 
   */
  public void testGetFile() throws Exception {
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "testresu");
    assertEquals("file2", res.getFile().getName());
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#getType()}.
   * @throws Exception 
   */
  public void testGetType() throws Exception {
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "testresu");
    assertEquals(SearchResultType.FILE, res.getType());

    res = new SearchResult(new WebRadio("web", "testurl"), "testresu");
    assertEquals(SearchResultType.WEBRADIO, res.getType());
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#getWebradio()}.
   */
  public void testGetWebradio() {
    SearchResult res = new SearchResult(new WebRadio("web1", "testurl"), "testresu");
    assertEquals(SearchResultType.WEBRADIO, res.getType());
    assertEquals("web1", res.getWebradio().getName());
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#getResu()}.
   */
  public void testGetResu() {
    SearchResult res = new SearchResult(new WebRadio("web", "testurl"), "testresu1");
    assertEquals("testresu1", res.getResu());
  }
}
