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
package org.exoplatform.services.ecm.dms.scripts;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.scripts.ScriptService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by The eXo Platform SARL
 * June 09, 2009
 */
public class TestScriptService extends BaseWCMTestCase {

  private ScriptService scriptService;
  private String expectedECMScriptHomePath = "/exo:ecm/scripts/ecm-explorer";
  private String expectedCBScriptHomePath = "/exo:ecm/scripts/content-browser";
  private String expectedBaseScriptPath = "/exo:ecm/scripts";
  private NodeHierarchyCreator nodeHierarchyCreator;
  private String cmsScriptsPath;
  private Session sessionDMS;
  
  @Override
  protected void afterContainerStart() {
    super.afterContainerStart();
    scriptService = (ScriptService)container.getComponentInstanceOfType(ScriptService.class);
    nodeHierarchyCreator = (NodeHierarchyCreator)container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    cmsScriptsPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_SCRIPTS_PATH);
  }
  
  @BeforeMethod
  public void setUp() throws Exception {
    applySystemSession();
    sessionDMS = sessionProviderService_.getSystemSessionProvider(null).getSession(DMSSYSTEM_WS, repository);
  }


  /**
   * Test ScriptServiceImpl.init()
   * Input: repository      String
   *                        The name of repository
   * Expect: Return all data initiated from repository in test-scripts-configuration.xml file
   * @throws Exception
   */
  @Test
  public void testInitRepo() throws Exception {
    scriptService.initRepo(REPO_NAME);
    assertTrue(sessionDMS.itemExists(cmsScriptsPath));
    assertTrue(sessionDMS.itemExists(cmsScriptsPath + "/content-browser"));
    assertTrue(sessionDMS.itemExists(cmsScriptsPath + "/ecm-explorer"));
  }

  /**
   * Test method: ScriptServiceImpl.getECMScriptHome()
   * Input: repository    String
   *                      The name of repository
   *        provider      SessionProvider
   * Expect: Return "/exo:ecm/scripts/ecm-explorer" is path of node for ECM Explorer Scripts
   * @throws Exception
   */
  @Test
  public void testGetECMScriptHome() throws Exception {
    assertNotNull(scriptService.getECMScriptHome(REPO_NAME, sessionProviderService_.getSystemSessionProvider(null)));
    assertEquals(expectedECMScriptHomePath, scriptService.getECMScriptHome(REPO_NAME,
        sessionProviderService_.getSystemSessionProvider(null)).getPath());
  }

  /**
   * Test method: ScriptServiceImpl.getCBScriptHome()
   * Input: repository    String
   *                      The name of repository
   *        provider      SessionProvider
   * Expect: Return "/exo:ecm/scripts/content-browser" is path of node for Content Browser Scripts
   * @throws Exception
   */
  @Test
  public void testGetCBScriptHome() throws Exception {
    assertNotNull(scriptService.getCBScriptHome(REPO_NAME, sessionProviderService_.getSystemSessionProvider(null)));
    assertEquals(expectedCBScriptHomePath, scriptService.getCBScriptHome(REPO_NAME,
        sessionProviderService_.getSystemSessionProvider(null)).getPath());
  }

  /**
   * Test method: ScriptServiceImpl.getECMActionScripts()
   * Input: repository    String
   *                      The name of repository
   *        provider      SessionProvider
   * Expect: Return All node for ECM Action Scripts
   * @throws Exception
   */
  @Test
  public void testGetECMActionScripts() throws Exception {
    List<Node> listECMScripts = scriptService.getECMActionScripts(REPO_NAME, sessionProviderService_.getSystemSessionProvider(null));
    assertTrue(listECMScripts.size() >0);

    List<String> scriptPathList = new ArrayList<String>();
    for (Node ECMScript : listECMScripts) {
      scriptPathList.add(ECMScript.getPath());
    }
    assertTrue(scriptPathList.contains("/exo:ecm/scripts/ecm-explorer/action/AddTaxonomyActionScript.groovy"));
  }

  /**
   * Test method: ScriptServiceImpl.getECMInterceptorScripts()
   * Input: repository    String
   *                      The name of repository
   *        provider      SessionProvider
   * Expect: Return All node for ECM Interceptor Scripts
   * @throws Exception
   */
  @Test
  public void testGetECMInterceptorScripts() throws Exception {
    List<Node> listECMInterceptorcripts = scriptService.getECMInterceptorScripts(REPO_NAME,
        sessionProviderService_.getSystemSessionProvider(null));
    assertTrue(listECMInterceptorcripts.size() >0);

    List<String> scriptPathList = new ArrayList<String>();
    for (Node interceptorScript : listECMInterceptorcripts) {
      scriptPathList.add(interceptorScript.getPath());
    }
    assertTrue(scriptPathList.contains("/exo:ecm/scripts/ecm-explorer/interceptor/PreNodeSaveInterceptor.groovy"));
  }

  /**
   * Test method: ScriptServiceImpl.getECMWidgetScripts()
   * Input: repository    String
   *                      The name of repository
   *        provider      SessionProvider
   * Expect: Return All node for ECM Widget Scripts
   * @throws Exception
   */
  @Test
  public void testGetECMWidgetScripts() throws Exception {
    List<Node> listECMWidgetScripts = scriptService.getECMWidgetScripts(REPO_NAME,
        sessionProviderService_.getSystemSessionProvider(null));
    assertTrue(listECMWidgetScripts.size() >0);

    List<String> scriptPathList = new ArrayList<String>();
    for (Node widgetScript : listECMWidgetScripts) {
      scriptPathList.add(widgetScript.getPath());
    }
    assertTrue(scriptPathList.contains("/exo:ecm/scripts/ecm-explorer/widget/FillSelectBoxWithCalendarCategories.groovy"));
  }

  /**
   * Test method: ScriptServiceImpl.getScript()
   * Input: scriptPath    String
   *                      The path of script
   *        repository    String
   *                      The name of repository
   * Expect: Return script
   * @throws Exception
   */
  @Test
  public void testGetScript() throws Exception {
    assertNotNull(scriptService.getScript("content-browser/GetDocuments.groovy", REPO_NAME));
    try {
      scriptService.getScript("content-browser/GetDocuments1.groovy", REPO_NAME);
      fail();
    } catch (Exception ex) {
    }
  }

  /**
   * Test method: ScriptServiceImpl.getBaseScriptPath()
   * Expect: Return "/exo:ecm/scripts" is base path of script
   * @throws Exception
   */
  @Test
  public void testGetBaseScriptPath() throws Exception {
    assertEquals(expectedBaseScriptPath, scriptService.getBaseScriptPath());
  }

  /**
   * Test method: ScriptServiceImpl.addScript()
   * Input: name          String
   *                      The name of script
   *        text          String
   *        repository    String
   *                      The name of repository
   *        provider      SessionProvider
   * Expect: Insert a new script
   * @throws Exception
   */
  @Test
  public void testAddScript() throws Exception {
    scriptService.addScript("Hello Name", "Hello Text", REPO_NAME, sessionProviderService_.getSystemSessionProvider(null));
    Node hello = (Node)sessionDMS.getItem("/exo:ecm/scripts/Hello Name");
    assertNotNull(hello);
    assertEquals("Hello Text", hello.getNode("jcr:content").getProperty("jcr:data").getString());
  }

  /**
   * Test method: ScriptServiceImpl.getScriptAsText()
   * Input: scriptPath    String
   *                      The path of script
   *        repository    String
   *                      The name of repository
   * Expect: Return "This is my script as text" as text of script
   * @throws Exception
   */
  @Test
  public void testGetScriptAsText() throws Exception {
    scriptService.addScript("My script", "This is my script as text", REPO_NAME, sessionProviderService_.getSystemSessionProvider(null));
    assertEquals("This is my script as text", scriptService.getScriptAsText("My script", REPO_NAME));
  }

  /**
   * Test method: ScriptServiceImpl.getScriptNode()
   * Input: scriptName    String
   *                      The name of script
   *        repository    String
   *                      The name of repository
   *        provider      SessionProvider
   * Expect: Return script node
   * @throws Exception
   */
  @Test
  public void testGetScriptNode() throws Exception {
    scriptService.addScript("My script 2", "This is my script as text 2", REPO_NAME, sessionProviderService_.getSystemSessionProvider(null));

    Node scriptNode = scriptService.getScriptNode("My script 2", REPO_NAME, sessionProviderService_.getSystemSessionProvider(null));
    assertEquals("My script 2", scriptNode.getName());
    assertEquals("This is my script as text 2", scriptNode.getNode("jcr:content").getProperty("jcr:data").getString());
  }

  /**
   * Test method: ScriptServiceImpl.removeScript()
   * Input: scriptPath    String
   *                      The path of script
   *        repository    String
   *                      The name of repository
   *        provider      SessionProvider
   * Expect: remove the script
   * @throws Exception
   */
  @Test
  public void testRemoveScript() throws Exception {
    scriptService.addScript("Hello Name", "Hello Text", REPO_NAME, sessionProviderService_.getSystemSessionProvider(null));

    Node hello = scriptService.getScriptNode("Hello Name", REPO_NAME,
        sessionProviderService_.getSystemSessionProvider(null));
    assertNotNull(hello);
    assertEquals("Hello Text", hello.getNode("jcr:content").getProperty("jcr:data").getString());

    scriptService.removeScript("Hello Name", REPO_NAME, sessionProviderService_.getSystemSessionProvider(null));
    assertNull(scriptService.getScriptNode("Hello Name", REPO_NAME, sessionProviderService_.getSystemSessionProvider(null)));
  }

  /**
   * Clean all scripts for testing
   */
  @AfterMethod
  public void tearDown() throws Exception {
    Node rootScripts = (Node)sessionDMS.getItem(cmsScriptsPath);
    String[] paths = new String[] {"My script", "My script 2", "Hello Name"};
    for (String path : paths) {
      if (rootScripts.hasNode(path)) {
        rootScripts.getNode(path).remove();
      }
    }
    sessionDMS.save();
  }
}
