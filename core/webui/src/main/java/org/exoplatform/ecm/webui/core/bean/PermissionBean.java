/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.ecm.webui.core.bean;

import org.exoplatform.services.jcr.access.PermissionType;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          vuna@exoplatform.com
 * Mar 23, 2013  
 */
public class PermissionBean {

  private String usersOrGroups ;
  private boolean read ;
  private boolean addNode ;
  private boolean remove ;

  public String getUsersOrGroups() { return usersOrGroups ; }
  public void setUsersOrGroups(String s) { usersOrGroups = s ; }

  public boolean isAddNode() { return addNode ; }
  public void setAddNode(boolean b) { addNode = b ; }

  public boolean isRead() { return read ; }
  public void setRead(boolean b) { read = b ; }

  public boolean isRemove() { return remove ; }
  public void setRemove(boolean b) { remove = b ; }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    // TODO Auto-generated method stub
    return this.usersOrGroups.equals(((PermissionBean)obj).usersOrGroups);
  }
  
  public void setPermissions(String[] permArray) {
    if (permArray == null) return;
    for (String entry : permArray) {
      if (PermissionType.READ.equals(entry)) this.setRead(true);
      if (PermissionType.REMOVE.equals(entry)) this.setRemove(true);
      if (PermissionType.ADD_NODE.equals(entry)) this.setAddNode(true);
    }
  }
  
  @Override
  public int hashCode() {
    return this.usersOrGroups.hashCode();
  }
}
