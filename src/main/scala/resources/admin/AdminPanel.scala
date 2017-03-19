/*
 * btc-hotspot
 * Copyright (C) 2016  Andrea Raspitzu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package resources.admin

import akka.http.scaladsl.server.Route
import commons.Configuration.AdminPanelConfig._
import registry.{MiniPortalRegistry, Registry}
import resources.CaptiveResource

object AdminPanelRegistry extends Registry with AdminPanel {
  
  MiniPortalRegistry.bindOrFail(adminPanelRoute, adminPanelHost, adminPanelPort, "Admin Panel")
  
}

trait AdminPanel
  extends CaptiveResource
  with AdminAPI {
  
  
  def staticFilesRoute:Route = getFromDirectory(adminPanelStaticFilesDir)
  
  val adminPanelRoute:Route =
    adminRoute ~
    staticFilesRoute ~
    catchAllRedirect(adminPanelIndex)
  
}
