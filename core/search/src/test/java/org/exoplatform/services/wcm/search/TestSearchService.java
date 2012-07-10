/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.wcm.search;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Date;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.ecms.test.BaseECMSTestCase;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.services.wcm.search.base.AbstractPageList;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jul 14, 2009
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/ecms-test-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/wcm/test-search-configuration.xml") 
  })
public class TestSearchService extends BaseECMSTestCase {
  QueryCriteria queryCriteria = new QueryCriteria();

  private SiteSearchService siteSearchService;

  private WCMPublicationService wcmPublicationService;

  private WebpagePublicationPlugin publicationPlugin ;

  private UserPortalConfigService userPortalConfigService;

  private final String searchKeyword = "This is";

  private SessionProvider sessionProvider;

  private POMSessionManager pomManager;

  private POMSession  pomSession;

  private int seachItemsPerPage = 100;
  
  @Override
  protected void afterContainerStart() {
    super.afterContainerStart();
    siteSearchService = WCMCoreUtils.getService(SiteSearchService.class);
    userPortalConfigService = WCMCoreUtils.getService(UserPortalConfigService.class);
    pomManager = WCMCoreUtils.getService(POMSessionManager.class);
    sessionProvider = WCMCoreUtils.getSystemSessionProvider();
    wcmPublicationService = WCMCoreUtils.getService(WCMPublicationService.class);

    publicationPlugin = new DumpPublicationPlugin();
    publicationPlugin.setName(DumpPublicationPlugin.LIFECYCLE_NAME);
    wcmPublicationService.addPublicationPlugin(publicationPlugin);

  }

  @BeforeMethod
  public void setUp() throws Exception {
    applySystemSession();
    addDocuments();
  }

  private void addDocuments() throws Exception {
    Node classicPortal = (Node)session.getItem("/sites content/live/classic/web contents");
    addChildNodes(classicPortal);

    Node sharedPortal = (Node)session.getItem("/sites content/live/shared/documents");
    addChildNodes(sharedPortal);
  }

  private void addChildNodes(Node parentNode)throws Exception{
    if (pomManager.getSession() == null) pomSession = pomManager.openSession();
    Page page = userPortalConfigService.getPage("portal::classic::testpage");
    if(page == null){
      page = new Page();
      page.setPageId("portal::classic::testpage");
      page.setName("testpage");
      page.setOwnerType("portal");
      page.setOwnerId("classic");
      userPortalConfigService.create(page);
    }

    Node webContentNode = null;
    HashMap<String, String> context = null;
      webContentNode = createWebcontentNode(parentNode, "webcontent0" , null, null, null);
      if(!webContentNode.isNodeType("metadata:siteMetadata"))webContentNode.addMixin("metadata:siteMetadata");
      wcmPublicationService.enrollNodeInLifecycle(webContentNode, DumpPublicationPlugin.LIFECYCLE_NAME);
      wcmPublicationService.publishContentSCV(webContentNode, page, parentNode.getName());
      context = new HashMap<String, String>();
//      context.put(DumpPublicationPlugin.CURRENT_REVISION_NAME, webContentNode.getName());
      publicationPlugin.changeState(webContentNode, PublicationDefaultStates.PUBLISHED, context);
        
      webContentNode = createWebcontentNode(parentNode, "webcontent1", null, null, null);
      if(!webContentNode.isNodeType("metadata:siteMetadata"))webContentNode.addMixin("metadata:siteMetadata");
      wcmPublicationService.enrollNodeInLifecycle(webContentNode, DumpPublicationPlugin.LIFECYCLE_NAME);
      wcmPublicationService.publishContentSCV(webContentNode, page, parentNode.getName());
      context = new HashMap<String, String>();
//      context.put(DumpPublicationPlugin.CURRENT_REVISION_NAME, webContentNode.getName());
      publicationPlugin.changeState(webContentNode, PublicationDefaultStates.DRAFT, context);
    
    session.save();
    pomSession.close();
  }

  private AbstractPageList<ResultNode> getSearchResult() throws Exception{
    return siteSearchService.searchSiteContents(WCMCoreUtils.getSystemSessionProvider(), 
                                                queryCriteria, seachItemsPerPage, false);
  }

  /**
   * Test case 1: search all node (includes have or don't have publication property)
   * in shared portal and not live mode. In this case, set parameters:<br>
   * searchPageChecked = false<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = false<br>
   *
   * @throws Exception the exception
   */
  @Test
  public void testSearchSharedPortalNotLiveMode() throws Exception {
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName("shared");
    queryCriteria.setKeyword(searchKeyword);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(false);
    queryCriteria.setSearchWebpage(false);
    AbstractPageList<ResultNode> pageList = getSearchResult();
    assertEquals(2, pageList.getPage(1).size());
  }

  /**
   * Test case 2: search all node (includes have or don't have publication property)
   * in shared portal. In this case, set parameters:<br>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = true<br>
   *
   * @throws Exception the exception
   */
  @Test
  public void testSearchSharedPortalLiveMode() throws Exception {
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName("shared");
    queryCriteria.setKeyword(searchKeyword);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(true);
    queryCriteria.setSearchWebpage(false);
    AbstractPageList<ResultNode> pageList = getSearchResult();
    assertEquals(1, pageList.getPage(1).size());
    assertEquals(2, pageList.getTotalNodes());
  }

  /**
   * Test case 3: search all node (includes have or don't have publication property) in all portals.
   * In this case, set parameters:<br>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = false<br>
   *
   * @throws Exception the exception
   */
  @Test
  public void testSearchAllPortalNotLiveMode() throws Exception {    
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName(null);
    queryCriteria.setKeyword(searchKeyword);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(false);
    queryCriteria.setSearchWebpage(false);
    AbstractPageList<ResultNode> pageList = getSearchResult();
    assertEquals(4, pageList.getTotalNodes());
    assertEquals(4, pageList.getPage(1).size());
  }

  /**
   * Test case 4: search nodes which are live mode in all portals.
   * In this case, set parameters:<br>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   *
   * @throws Exception the exception
   */
  @Test
  public void testSearchAllPortalLiveMode() throws Exception {
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName(null);
    queryCriteria.setKeyword(searchKeyword);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(true);
    queryCriteria.setSearchWebpage(false);
    AbstractPageList<ResultNode> pageList = getSearchResult();
    assertEquals(2, pageList.getPage(1).size());
    assertEquals(4, pageList.getTotalNodes());
  }

  //---------------------------------------------- Test search document -----------------------------------------------------------
  /**
   * Test case 5: Test search document.
   * Search all documents in system (all portals) which are live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
  @Test
  public void testSearchDocumentLiveMode() throws Exception {    
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName(null);
    queryCriteria.setKeyword(searchKeyword);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(true);
    queryCriteria.setSearchWebpage(false);
    AbstractPageList<ResultNode> pageList = getSearchResult();
    assertEquals(2, pageList.getPage(1).size());
  }

  /**
   * Test case 6:Test search document.
   * Search all documents in system (all portals). With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = false<br>
   */
  @Test
  public void testSearchDocumentNotLiveMode() throws Exception {
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName(null);
    queryCriteria.setKeyword(searchKeyword);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(false);
    queryCriteria.setSearchWebpage(false);
    AbstractPageList<ResultNode> pageList = getSearchResult();
    assertEquals(4, pageList.getTotalNodes());
  }

  /**
   * Test case 7:Test search document.
   * Search all documents in shared portal. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = false<br>
   */
  @Test
  public void testSearchDocumentOfSharedPortal() throws Exception {
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName("shared");
    queryCriteria.setKeyword(searchKeyword);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(false);
    queryCriteria.setSearchWebpage(false);
    AbstractPageList<ResultNode> pageList = getSearchResult();
    assertEquals(2, pageList.getTotalNodes());
  }

  /**
   * Test case 8:Test search document.
   * Search all documents in shared portal. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = true<br>
   */
  @Test
  public void testSearchDocumentOfSharedPortalLiveMode() throws Exception {
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName("shared");
    queryCriteria.setKeyword(searchKeyword);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(true);
    queryCriteria.setSearchWebpage(false); 
    AbstractPageList<ResultNode> pageList = getSearchResult();
    assertEquals(1, pageList.getPage(1).size());
    assertEquals(2, pageList.getTotalNodes());
  }

  //------------------------------------------- Test search pages ------------------------------------------------------------------
  /**
   * Test case 9:Test search pages.
   * Search all pages in all portals. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
//  public void testSearchPagesLiveMode() throws Exception {
//    this.searchDocumentChecked = false;
//    this.searchIsLiveMode = true;
//    this.searchSelectedPortal = null;
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(5, pageList.getPage(1).size());
//  }

  /**
   * Test case 10:Test search pages.
   * Search all pages in all portals and not live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = false<br>
   */
//  public void testSearchPages() throws Exception {
//    this.searchDocumentChecked = false;
//    this.searchSelectedPortal = null;
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(10, pageList.getTotalNodes());
//  }

  /**
   * Test case 11:Test search pages.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = true<br>
   */
//  public void testSearchPagesSharedLiveMode() throws Exception {
//    this.searchDocumentChecked = false;
//    this.searchIsLiveMode = true;
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(5, pageList.getPage(1).size());
//    assertEquals(10, pageList.getTotalNodes());
//  }

  /**
   * Test case 12:Test search pages.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = shared<br>
   * searchIsLiveMode = true<br>
   */
//  public void testSearchPagesShared() throws Exception {
//    this.searchDocumentChecked = false;
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(5, pageList.getPage(1).size());
//    assertEquals(10, pageList.getTotalNodes());
//  }

  //------------------------------------- test with not document or page --------------------------------------------------------------------

  /**
   * Test case 13:Test search contents are not document or page in all portal.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
//  public void testSearchNotPagesDocument_AllPortalLiveMode() throws Exception {
//    this.searchDocumentChecked = false;
//    this.searchPageChecked = false;
//    this.searchIsLiveMode = true;
//    this.searchSelectedPortal = null;
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(5, pageList.getPage(1).size());
//    assertEquals(10, pageList.getTotalNodes());
//  }

  /**
   * Test case 14:Test search contents are not document or page in all portal.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
//  public void testSearchNotPagesDocument_AllPortalNotLiveMode() throws Exception {
//    this.searchDocumentChecked = false;
//    this.searchPageChecked = false;
//    this.searchSelectedPortal = null;
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(5, pageList.getPage(1).size());
//    assertEquals(10, pageList.getTotalNodes());
//  }

  /**
   * Test case 15:Test search contents are not document or page in all portal.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
//  public void testSearchNotPagesDocument_SharedLiveMode() throws Exception {
//    this.searchDocumentChecked = false;
//    this.searchPageChecked = false;
//    this.searchIsLiveMode = true;
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(5, pageList.getPage(1).size());
//    assertEquals(10, pageList.getTotalNodes());
//  }

  /**
   * Test case 16:Test search contents are not document or page in all portal.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = false<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
//  public void testSearchNotPagesDocument_SharedNoLiveMode() throws Exception {
//    this.searchDocumentChecked = false;
//    this.searchPageChecked = false;
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(5, pageList.getPage(1).size());
//    assertEquals(10, pageList.getTotalNodes());
//  }

  /**
   * Test case 17:Test search contents is document or page in all portal. And search
   * with created date and modified Date.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = false<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
//  @SuppressWarnings("deprecation")
//  public void testSearchPagesDocument_Date() throws Exception {
//    this.searchIsLiveMode = true;
//    this.searchSelectedPortal = null;
//    Date date = new Date(2009, 05, 05);
//    GregorianCalendar calFrom = new GregorianCalendar() ;
//    calFrom.setTime(date);
//    date = new Date();
//    GregorianCalendar calTo = new GregorianCalendar() ;
//    calTo.setTime(date);
//    DatetimeRange datetimeRange = new DatetimeRange(calFrom, calTo);
//    queryCriteria.setCreatedDateRange(datetimeRange);
//    queryCriteria.setSearchWebpage(false);
//    queryCriteria.setSiteName(null);
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(5, pageList.getPage(1).size());
//    assertEquals(10, pageList.getTotalNodes());
//  }

  /**
   * Test case 18:Test search contents is document or page in all portal. And search
   * with created date and modified Date.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   */
//  public void testSearchPagesDocument_NotFultextSearch() throws Exception {
//    this.searchIsLiveMode = true;
//    this.searchSelectedPortal = null;
//    queryCriteria.setFulltextSearch(false);
//    queryCriteria.setSearchWebpage(false);
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(0, pageList.getPage(1).size());
//  }

  /**
   * Test case 19:Test search contents is document or page in all portal. And search
   * with created date and modified Date.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   * keyWord = null;
   */
//  public void testSearchPagesDocument_ContentType() throws Exception {
//    this.searchIsLiveMode = true;
//    this.searchSelectedPortal = null;
//    queryCriteria.setSearchWebpage(false);
//    queryCriteria.setContentTypes(new String[]{"exo:webContent", "exo:htmlFile"});
//    queryCriteria.setKeyword(null);
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(5, pageList.getPage(1).size());
//  }

  /**
   * Test case 20:Test search contents is document or page in all portal. And search
   * with created date and modified Date.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   * keyWord = null;
   */
//  public void testSearchPagesDocument_Property() throws Exception {
//    this.searchIsLiveMode = true;
//    this.searchSelectedPortal = null;
//    this.searchKeyword = "This is*";
//    QueryProperty queryProperty1 = queryCriteria.new QueryProperty();
//    queryProperty1.setName("jcr:data");
//    queryProperty1.setValue("This is the");
//    QueryProperty queryProperty2 = queryCriteria.new QueryProperty();
//    queryProperty2.setName("jcr:data");
//    queryProperty2.setValue("the default.css file");
//    queryCriteria.setQueryMetadatas(new QueryProperty[]{queryProperty1, queryProperty2});
//    queryCriteria.setSearchWebpage(false);
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(0, pageList.getPage(1).size());
//  }

  /**
   * Test case 21:Test search contents is document or page in all portal. And search
   * with created date and modified Date.
   * Search all pages in share and live mode. With this case, values of parameters are:<br/>
   * searchPageChecked = true<br>
   * searchDocumentChecked = true<br>
   * searchSelectedPortal = null<br>
   * searchIsLiveMode = true<br>
   * keyWord = null;
   */
//  public void testSearchPagesDocument_CategoryUUIDS() throws Exception{
//    this.searchIsLiveMode = true;
//    this.searchSelectedPortal = null;
//    Node documentNode = ((Node)session.getItem("/sites content/live/shared/documents")).getNode("webcontent0");
//    Node livenode = ((Node)session.getItem("/sites content/live/classic/web contents")).getNode("webcontent0");
//    queryCriteria.setSearchWebpage(false);
//    queryCriteria.setCategoryUUIDs(new String[]{documentNode.getUUID(), livenode.getUUID()});
//    AbstractPageList<ResultNode> pageList = getSearchResult();
//    assertEquals(0, pageList.getAvailable());
//  }

  @Test
  public void testSearchByProperty()throws Exception{
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName("shared");
    queryCriteria.setKeyword(searchKeyword);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(true);
    queryCriteria.setSearchWebpage(false);
    queryCriteria.setFulltextSearch(true);
    queryCriteria.setFulltextSearchProperty(new String[] {"dc:description"});
    assertEquals(0, siteSearchService.searchSiteContents(sessionProvider, queryCriteria, 10, true).getTotalNodes());
  }

  @Test
  public void testSearchByDocumentType()throws Exception{
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName("shared");
    queryCriteria.setKeyword(null);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(true);
    queryCriteria.setSearchWebpage(false);
    queryCriteria.setFulltextSearch(true);    
    String documentType = "exo:webContent";    
    queryCriteria.setFulltextSearchProperty(null);
    queryCriteria.setContentTypes(documentType.split(","));
    assertEquals(2, siteSearchService.searchSiteContents(sessionProvider, queryCriteria, 10, true).getTotalNodes());
  }

  @Test
  public void testSearchByDocumentAuthor()throws Exception{
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName("shared");
    queryCriteria.setKeyword(null);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(true);
    queryCriteria.setSearchWebpage(false);
    queryCriteria.setFulltextSearch(true);    
    queryCriteria.setFulltextSearchProperty(null);    
    String author = "root";
    queryCriteria.setAuthors(new String[]{author});
    assertEquals(4, siteSearchService.searchSiteContents(sessionProvider, queryCriteria, 10, true).getTotalNodes());
  }

  @Test
  public void testSearchByMimeTypes()throws Exception{
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName("shared");
    queryCriteria.setKeyword(null);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(true);
    queryCriteria.setSearchWebpage(false);
    queryCriteria.setFulltextSearch(true);    
    queryCriteria.setFulltextSearchProperty(null);    
    queryCriteria.setMimeTypes(new String[]{"exo:webContent", " exo:siteBreadcrumb"});
    assertEquals(4, siteSearchService.searchSiteContents(sessionProvider, queryCriteria, 10, true).getTotalNodes());
  }

  @Test
  public void testSearchByTagUUID() throws Exception{
    queryCriteria = new QueryCriteria();
    queryCriteria.setSiteName("shared");
    queryCriteria.setKeyword(null);
    queryCriteria.setSearchDocument(true);
    queryCriteria.setSearchWebContent(true);
    queryCriteria.setLiveMode(true);
    queryCriteria.setSearchWebpage(false);
    queryCriteria.setFulltextSearch(true);    
    queryCriteria.setFulltextSearchProperty(null);    
    queryCriteria.setMimeTypes(new String[]{"exo:webContent", " exo:siteBreadcrumb"});    
    Node node = (Node)session.getItem("/sites content/live/classic/web contents/webcontent0");
    String uuid = node.getUUID();
    queryCriteria.setTagUUIDs(new String[]{uuid});   
    assertEquals(4, siteSearchService.searchSiteContents(sessionProvider, queryCriteria, 10, true).getTotalNodes());
  }

  @AfterMethod
  protected void tearDown() throws Exception {

    NodeIterator iterator = null;
    Node classicPortal = (Node)session.getItem("/sites content/live/classic/web contents");
    iterator = classicPortal.getNodes();
    while (iterator.hasNext()) {
      iterator.nextNode().remove();
    }

    Node sharedPortal = (Node)session.getItem("/sites content/live/shared/documents");
    iterator = sharedPortal.getNodes();
    while (iterator.hasNext()) {
      iterator.nextNode().remove();
    }

    session.save();
  }
  
  protected Node createWebcontentNode(Node parentNode,
                                      String nodeName,
                                      String htmlData,
                                      String cssData,
                                      String jsData) throws Exception {
    Node webcontent = parentNode.addNode(nodeName, "exo:webContent");
    webcontent.setProperty("exo:title", nodeName);
    Node htmlNode;
    try {
      htmlNode = webcontent.getNode("default.html");
    } catch (Exception ex) {
      htmlNode = webcontent.addNode("default.html", "nt:file");
    }
    Node articleNode = webcontent.addNode("article", "exo:article");
    articleNode.setProperty("exo:title", "This is");
    articleNode.setProperty("exo:text", "This is text");
    if (!htmlNode.isNodeType("exo:htmlFile"))
      htmlNode.addMixin("exo:htmlFile");
    Node htmlContent;
    try {
      htmlContent = htmlNode.getNode("jcr:content");
    } catch (Exception ex) {
      htmlContent = htmlNode.addNode("jcr:content", "nt:resource");
    }
    htmlContent.setProperty("jcr:encoding", "UTF-8");
    htmlContent.setProperty("jcr:mimeType", "text/html");
    htmlContent.setProperty("jcr:lastModified", new Date().getTime());
    if (htmlData == null)
      htmlData = "This is the default.html file.";
    htmlContent.setProperty("jcr:data", htmlData);

    Node jsFolder;
    try {
      jsFolder = webcontent.getNode("js");
    } catch (Exception ex) {
      jsFolder = webcontent.addNode("js", "exo:jsFolder");
    }
    Node jsNode;
    try {
      jsNode = jsFolder.getNode("default.js");
    } catch (Exception ex) {
      jsNode = jsFolder.addNode("default.js", "nt:file");
    }
    if (!jsNode.isNodeType("exo:jsFile"))
      jsNode.addMixin("exo:jsFile");
    jsNode.setProperty("exo:active", true);
    jsNode.setProperty("exo:priority", 1);
    jsNode.setProperty("exo:sharedJS", true);

    Node jsContent;
    try {
      jsContent = jsNode.getNode("jcr:content");
    } catch (Exception ex) {
      jsContent = jsNode.addNode("jcr:content", "nt:resource");
    }
    jsContent.setProperty("jcr:encoding", "UTF-8");
    jsContent.setProperty("jcr:mimeType", "text/javascript");
    jsContent.setProperty("jcr:lastModified", new Date().getTime());
    if (jsData == null)
      jsData = "This is the default.js file.";
    jsContent.setProperty("jcr:data", jsData);

    Node cssFolder;
    try {
      cssFolder = webcontent.getNode("css");
    } catch (Exception ex) {
      cssFolder = webcontent.addNode("css", "exo:cssFolder");
    }
    Node cssNode;
    try {
      cssNode = cssFolder.getNode("default.css");
    } catch (Exception ex) {
      cssNode = cssFolder.addNode("default.css", "nt:file");
    }
    if (!cssNode.isNodeType("exo:cssFile"))
      cssNode.addMixin("exo:cssFile");
    cssNode.setProperty("exo:active", true);
    cssNode.setProperty("exo:priority", 1);
    cssNode.setProperty("exo:sharedCSS", true);

    Node cssContent;
    try {
      cssContent = cssNode.getNode("jcr:content");
    } catch (Exception ex) {
      cssContent = cssNode.addNode("jcr:content", "nt:resource");
    }
    cssContent.setProperty("jcr:encoding", "UTF-8");
    cssContent.setProperty("jcr:mimeType", "text/css");
    cssContent.setProperty("jcr:lastModified", new Date().getTime());
    if (cssData == null)
      cssData = "This is the default.css file.";
    cssContent.setProperty("jcr:data", cssData);

    Node mediaFolder;
    try {
      mediaFolder = webcontent.getNode("medias");
    } catch (Exception ex) {
      mediaFolder = webcontent.addNode("medias");
    }
    if (!mediaFolder.hasNode("images"))
      mediaFolder.addNode("images", "nt:folder");
    if (!mediaFolder.hasNode("videos"))
      mediaFolder.addNode("videos", "nt:folder");
    if (!mediaFolder.hasNode("audio"))
      mediaFolder.addNode("audio", "nt:folder");
    session.save();
    return webcontent;
  }
}
