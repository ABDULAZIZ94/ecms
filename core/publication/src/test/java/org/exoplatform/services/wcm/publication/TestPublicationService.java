/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.wcm.publication;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertNull;

import java.util.HashMap;
import java.util.Locale;

import javax.jcr.Node;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.ecms.test.BaseECMSTestCase;
import org.exoplatform.services.ecm.publication.AlreadyInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 24, 2012  
 */

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/ecms-test-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/wcm/test-publication-configuration.xml")
  })
public class TestPublicationService extends BaseECMSTestCase {
  
  private static final String CURRENT_STATE = "publication:currentState";
  private static final String TEST = "test";
  private static final String ENROLLED = "enrolled";
  private static final String PUBLISHED = "published";
  
  private PublicationService publicationService_;
  private PublicationPlugin plugin_;
  private Node node_;
  
  @Override
  protected void afterContainerStart() {
    super.afterContainerStart();
    publicationService_ = WCMCoreUtils.getService(PublicationService.class);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    applySystemSession();
    node_ = session.getRootNode().addNode(TEST);
    session.save();
    ConversationState c = new ConversationState(new Identity(session.getUserID()));
    ConversationState.setCurrent(c);
    plugin_ = new DumpPublicationPlugin();
    plugin_.setName("Simple");
    plugin_.setDescription("Simple");
    publicationService_.addPublicationPlugin(plugin_);
  }
  
  @AfterMethod
  public void tearDown() throws Exception {
    publicationService_.getPublicationPlugins().clear();
    node_.remove();
    session.save();
  }

  /**
   * tests add publication plugin: 
   * add 1 publication plugins and check if the total plugins number is 1
   */
  @Test
  public void testAddPublicationPlugin() throws Exception {
    assertEquals(1, publicationService_.getPublicationPlugins().size());
  }
  
  /**
   * tests get publication plugin: 
   * add 3 publication plugins, get the total plugins and check if the number is 3 
   */
  @Test
  public void testGetPublicationPlugins() throws Exception {
    assertEquals(1, publicationService_.getPublicationPlugins().size());
  }
  
  /**
   * tests enrolle node in lifecycle:
   * enrolle node in lifecycle and then check if it is enrolled   
   */
  @Test
  public void testEnrollNodeInLifecycle() throws Exception {
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    assertEquals(ENROLLED, node_.getProperty(CURRENT_STATE).getString());
  }
  
  /**
   * tests if node is enrolled in lifecycle:
   * enrolle node in lifecycle and then check if it is enrolled   
   */
  @Test
  public void testIsNodeEnrolledInLifecycle() throws Exception {
    Node node1 = node_.addNode("test1");
    session.save();
    Exception e = null;
    //-------------------------------------
    try {
      publicationService_.enrollNodeInLifecycle(node1, plugin_.getLifecycleName());      
      publicationService_.enrollNodeInLifecycle(node1, plugin_.getLifecycleName());
    } catch (AlreadyInPublicationLifecycleException ex) {
      e = ex;
    }
    assertNotNull(e);    
    //-------------------------------------
//    Node node2 = node_.addNode("test2");
//    node2.addMixin(DumpPublicationPlugin.WCM_PUBLICATION_MIXIN);
//    node2.setProperty("publication:lifecycleName", "test");
//    node2.setProperty("publication:currentState", "test");
//    node2.setProperty("publication:history", new String[]{"test"});
//    session.save();
//    e = null;
//    try {
//      publicationService_.enrollNodeInLifecycle(node2, plugin_.getLifecycleName());
//    } catch (NoSuchNodeTypeException ex) {
//      e = ex;
//    }
//    assertNotNull(e);
    //-------------------------------------
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    assertTrue(publicationService_.isNodeEnrolledInLifecycle(node_));
  }
  
  /**
   * tests changing state for a node 
   */
  @Test
  public void testChangeState() throws Exception {
    HashMap<String, String> context = new HashMap<String, String>();
    context.put("visibility", "true");
    
    Node node1 = node_.addNode("test1");
    session.save();
    Exception e = null;
    try {
      publicationService_.changeState(node1, PUBLISHED, context);
    } catch (NotInPublicationLifecycleException ex) {
      e = ex;
    }
    assertNotNull(e);
        
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    publicationService_.changeState(node_, PUBLISHED, context);
    assertEquals(PUBLISHED, node_.getProperty(CURRENT_STATE).getString());
  }
  
  /**
   * tests getting state image 
   */
  @Test
  public void testGetStateImage() throws Exception {
    Exception e = null;
    try {
      publicationService_.getStateImage(node_, new Locale("en"));
    } catch (NotInPublicationLifecycleException ex) {
      e = ex;
    }
    assertNotNull(e);
    
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    assertNotNull(publicationService_.getStateImage(node_, new Locale("en")));
  }
  
  /**
   * tests getting current state
   */
  @Test
  public void testGetCurrentState() throws Exception {
    Exception e = null;
    try {
      publicationService_.getCurrentState(node_);
    } catch (NotInPublicationLifecycleException ex) {
      e = ex;
    }
    assertNotNull(e);
    
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    assertEquals(ENROLLED, publicationService_.getCurrentState(node_));
  }
  
  /**
   * tests getting user info
   */
  @Test
  public void testGetUserInfo() throws Exception {
    Exception e = null;
    try {
      publicationService_.getUserInfo(node_, new Locale("en"));
    } catch (NotInPublicationLifecycleException ex) {
      e = ex;
    }
    assertNotNull(e);
    
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    assertEquals(null, publicationService_.getUserInfo(node_, new Locale("en")));
  }
  
  /**
   * tests adding log
   */
  @Test
  public void testAddLog() throws Exception {
    Exception e = null;
    try {
      publicationService_.addLog(node_, new String[] {"log1"});
    } catch (NotInPublicationLifecycleException ex) {
      e = ex;
    }
    assertNotNull(e);
    
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    publicationService_.addLog(node_, new String[] {"log1"});
    publicationService_.addLog(node_, new String[] {"log2"});
    assertEquals(3, node_.getProperty(DumpPublicationPlugin.HISTORY).getValues().length);
  }
  
  /**
   * tests getting logs
   */
  @Test
  public void testGetLog() throws Exception {
    Exception e = null;
    try {
      publicationService_.getLog(node_);
    } catch (NotInPublicationLifecycleException ex) {
      e = ex;
    }
    assertNotNull(e);
    
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    publicationService_.addLog(node_, new String[] {"log1"});
    publicationService_.addLog(node_, new String[] {"log2"});
    assertEquals(3, publicationService_.getLog(node_).length);
  }
  
  /**
   * tests getting node life cycle name 
   */
  @Test
  public void testGetNodeLifecycleName() throws Exception {
    Exception e = null;
    try {
      publicationService_.getNodeLifecycleName(node_);
    } catch (NotInPublicationLifecycleException ex) {
      e = ex;
    }
    assertNotNull(e);
    
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    assertEquals(plugin_.getLifecycleName(), publicationService_.getNodeLifecycleName(node_));
  }

  /**
   * tests getting node life cycle Description 
   */
  @Test
  public void testGetNodeLifecycleDescription() throws Exception {
    Exception e = null;
    try {
      publicationService_.getNodeLifecycleDesc(node_);
    } catch (NotInPublicationLifecycleException ex) {
      e = ex;
    }
    assertNotNull(e);
    
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    assertEquals(plugin_.getDescription(), publicationService_.getNodeLifecycleDesc(node_));
  }
  
  /**
   * tests unsubscribing life cycle 
   */
  @Test
  public void testUnsubscribeLifecycle() throws Exception {
    Exception e = null;
    try {
      publicationService_.unsubcribeLifecycle(node_);
    } catch (NotInPublicationLifecycleException ex) {
      e = ex;
    }
    assertNotNull(e);
    
    node_.addMixin("exo:sortable");
    session.save();
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    publicationService_.unsubcribeLifecycle(node_);
    assertFalse(node_.isNodeType(DumpPublicationPlugin.PUBLICATION));
  }
  
  /**
   * tests isunsubscribe life cycle 
   */
  @Test
  public void testIsUnsubscribeLifecycle() throws Exception {
    Exception e = null;
    assertTrue(publicationService_.isUnsubcribeLifecycle(node_));
    
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    assertFalse(publicationService_.isUnsubcribeLifecycle(node_));
  }

  /**
   * tests get node publish
   * @throws Exception
   */
  @Test
  public void testGetNodePublish() throws Exception {
    assertNull(publicationService_.getNodePublish(node_, null));
    
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    assertEquals(node_, publicationService_.getNodePublish(node_, null));
    assertEquals(node_, publicationService_.getNodePublish(node_, plugin_.getLifecycleName()));
  }

  /**
   * tests getLocalizedAndSubstituteLog
   */
  @Test
  public void testGetLocalizedAndSubstituteLog() throws Exception {
    assertEquals("Test EN", publicationService_.getLocalizedAndSubstituteLog(
         new Locale("en"), "PublicationService.test.test", new String[]{}));
  }
  
  /**
   * tests getLocalizedAndSubstituteLog
   */
  @Test
  public void testGetLocalizedAndSubstituteLog2() throws Exception {
    publicationService_.enrollNodeInLifecycle(node_, plugin_.getLifecycleName());
    assertEquals("PublicationService.test.test", publicationService_.getLocalizedAndSubstituteLog(
         node_, new Locale("en"), "PublicationService.test.test", new String[]{}));
  }
  
}
