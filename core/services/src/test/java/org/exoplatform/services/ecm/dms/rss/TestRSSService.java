/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.ecm.dms.rss;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.services.cms.rss.RSSService;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by The eXo Platform SARL
 * June 09, 2009
 */
public class TestRSSService extends BaseWCMTestCase {

  private RSSService rssService;
  
  final static public String MIX_REFERENCEABLE = "mix:referenceable";

  @Override
  protected void afterContainerStart() {
    super.afterContainerStart();
    rssService = (RSSService)container.getComponentInstanceOfType(RSSService.class);
  }
  
  @BeforeMethod
  public void setUp() throws Exception {
    applySystemSession();
  }

  /**
   * Test method: generateFeed()
   * Input: context     Map
   *                    Consist of among information
   * Expect: Create a Feed file (feed type is RSS or Podcast)
   * @throws Exception
   */
  @Test
  public void testGenerateFeed() throws Exception {
    Map<String, String> contextRss = new HashMap<String, String>();
   
  	Node documents = session.getRootNode().addNode("Documents");
  	Node feeds = session.getRootNode().addNode("Feeds");
    Node articleNode = feeds.addNode("testRSS", "exo:article");
    articleNode.setProperty("exo:title", "Test RSS");
    articleNode.setProperty("exo:summary", "Hello RSS");
    if(articleNode.canAddMixin(MIX_REFERENCEABLE)) articleNode.addMixin(MIX_REFERENCEABLE);    
    Node rss = feeds.addNode("rss");
    Node feed = rss.addNode("feedName");
    Node feedContent = feed.addNode("jcr:content");
    feedContent.setProperty("jcr:encoding", "UTF-8");
    feedContent.setProperty("jcr:mimeType", "text/plain");
    feedContent.setProperty("jcr:lastModified", new Date().getTime());
    feedContent.setProperty("jcr:data", "Feed Content");
    session.save();   
    
    contextRss.put("exo:feedType", "rss");
    contextRss.put("repository", "repository");
    contextRss.put("srcWorkspace", COLLABORATION_WS);
    contextRss.put("actionName", "actionName");
    contextRss.put("exo:rssVersion", "rss_2.0");
    contextRss.put("exo:feedTitle", "Hello Feed");
    contextRss.put("exo:summary", "Hello Summary");
    contextRss.put("exo:description", "Hello Description");
    contextRss.put("exo:storePath", "/Feeds");
    contextRss.put("exo:feedName", "feedName");
    contextRss.put("exo:queryPath", "SELECT * FROM exo:article where jcr:path LIKE '/Feeds/%'");
    contextRss.put("exo:title", "Hello Title");
    contextRss.put("exo:url", "http://www.facebook.com");   
    
    rssService.generateFeed(contextRss);
    session.getRootNode().addNode("Feeds");
    Node myFeeds = (Node) session.getItem("/Feeds");
    myFeeds.addNode("rss");
    Node myRSS = (Node) session.getItem("/Feeds/rss");
    myRSS.addNode("feedName");
    Node myFeedName = (Node) session.getItem("/Feeds/rss/feedName");
    if (!myFeedName.hasNode("jcr:content")) {
    	myFeedName.addNode("jcr:content");
    }
    Node myJcrContent = myFeedName.getNode("jcr:content");
    assertEquals("Feeds", myFeeds.getName());
    assertEquals("rss", myRSS.getName());
    assertEquals("feedName", myFeedName.getName());
    assertEquals("/Feeds/rss/feedName/jcr:content", myJcrContent.getPath());
    if (myJcrContent.hasProperty("jcr:data")) {
      String jcrData = myJcrContent.getProperty("jcr:data").getString();
      assertNotNull(myJcrContent.getProperty("jcr:data").getString());
      assertTrue(jcrData.indexOf("Hello Feed") > 0);
      assertTrue(jcrData.indexOf("Hello Description") > 0);
      assertTrue(jcrData.indexOf("<description>Not data</description>") < 0);
      assertEquals("application/rss+xml", myJcrContent.getProperty("jcr:mimeType").getString());
    }
  }

  @Test
  public void testGenerateFeed2() throws Exception {
    Map<String, String> contextPodcast = new HashMap<String, String>();
    
    Node documents = session.getRootNode().addNode("Documents");
    Node podcastNode = documents.addNode("testRSS", "exo:podcast");
    podcastNode.setProperty("exo:title", "Test Podcast");
    podcastNode.setProperty("exo:podcastCategory", "category1, category2");
    podcastNode.setProperty("exo:explicit", "no");
    podcastNode.setProperty("exo:keywords", "keyword1 keyword2");
    Node podcastContent = podcastNode.addNode("jcr:content","nt:resource");
    podcastContent.setProperty("jcr:encoding", "UTF-8");
    podcastContent.setProperty("jcr:mimeType", "text/plain");
    podcastContent.setProperty("jcr:lastModified", new Date().getTime());
    podcastContent.setProperty("jcr:data", "Podcast Data");    
    if(podcastNode.canAddMixin(MIX_REFERENCEABLE)) podcastNode.addMixin(MIX_REFERENCEABLE);
    session.save();    
    
    contextPodcast.put("exo:feedType", "podcast");
    contextPodcast.put("repository", "repository");
    contextPodcast.put("srcWorkspace", COLLABORATION_WS);
    contextPodcast.put("actionName", "actionName");
    contextPodcast.put("exo:rssVersion", "rss_1.0");
    contextPodcast.put("exo:feedTitle", "Hello Feed");
    contextPodcast.put("exo:link", "Testing");
    contextPodcast.put("exo:podcastCategory", "category1, category2");
    contextPodcast.put("exo:keywords", "key1 key2");
    contextPodcast.put("exo:summary", "Hello Summary");
    contextPodcast.put("exo:description", "Hello Description");
    contextPodcast.put("exo:storePath", "/Feeds");
    contextPodcast.put("exo:feedName", "podcastName");
    contextPodcast.put("exo:queryPath", "SELECT * FROM exo:podcast where jcr:path LIKE '/Documents/%'");
    contextPodcast.put("exo:title", "Hello Title");
    contextPodcast.put("exo:url", "http://twitter.com");
    contextPodcast.put("exo:imageURL", "http://twitter.com");
    rssService.generateFeed(contextPodcast);

    session.getRootNode().addNode("Feeds");
    Node myFeeds = (Node) session.getItem("/Feeds");
    myFeeds.addNode("podcast");
    Node myPodcast = (Node) session.getItem("/Feeds/podcast");
    myPodcast.addNode("podcastName");
    Node myPodcastName = (Node) session.getItem("/Feeds/podcast/podcastName");
    assertEquals("Feeds", myFeeds.getName());
    assertEquals("podcast", myPodcast.getName());
    assertEquals("podcastName", myPodcastName.getName());
  }

  /**
   * Clean all node for testing
   */
  @AfterMethod
  public void tearDown() throws Exception {
    Node myRoot = session.getRootNode();
    if (myRoot.hasNode("Feeds")) {
      myRoot.getNode("Feeds").remove();
    }
    if (myRoot.hasNode("Documents")) {
    	myRoot.getNode("Documents").remove();
    }
    session.save();
  }
}
