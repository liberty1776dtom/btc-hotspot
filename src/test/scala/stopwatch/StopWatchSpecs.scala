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

package stopwatch

import com.typesafe.scalalogging.slf4j.LazyLogging
import commons.Helpers.FutureOption
import iptables.IpTablesInterface
import mocks.IpTablesServiceMock
import registry.{SchedulerRegistry, SessionRepositoryRegistry}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import protocol.SessionRepositoryImpl
import protocol.domain.{Offer, QtyUnit, Session}
import watchdog.{SchedulerImpl, StopWatch, TimebasedStopWatch}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class StopWatchSpecs extends Specification with LazyLogging {
sequential
  
  trait MockStopWatchScope extends Scope {
    val stopWatchDep = new {
      val ipTablesService: IpTablesInterface = new IpTablesServiceMock {}
      val scheduler: SchedulerImpl = new SchedulerImpl
    }
  }
  
  def waitForOfferMillis(waitDuration: Long) = {
    val nop = Future {
      Thread.sleep(waitDuration + 500L)
    }
    Await.result(nop, waitDuration + 1000L millis )
  }
  

  
  "TimeBased stop watch" should {

    "wait the correct time before calling onLimitReach" in new MockStopWatchScope {
      
      val approximation = 500L //0.5s
      
      val session = Session(clientMac = "thisIsMyMac")
      val offer = Offer(
        qty = 2000,
        qtyUnit = QtyUnit.millis,
        price = 950000,
        description =  "1 second"
      )

      val timeStopWatch = new TimebasedStopWatch(stopWatchDep, session, offer.qty)
 
      var t2 = -1L
      val t1 = System.currentTimeMillis
      
      timeStopWatch.start(onLimitReach = {
        logger.info("calling onLimitReach")
        t2 = System.currentTimeMillis
      })

      waitForOfferMillis(offer.qty)

      t2 !== -1L
      t2 - t1 - approximation must beCloseTo(offer.qty within 1.significantFigures)
    }
    
    "isPending should return true if the stopwatch is still running, false otherwise" in new MockStopWatchScope {
  
      val session = Session(clientMac = "thisIsMyMac")
      
      val offer = Offer(
        qty = 2000,
        qtyUnit = QtyUnit.millis,
        price = 950000,
        description =  "1 second"
      )
  
      val timeStopWatch = new TimebasedStopWatch(stopWatchDep, session, offer.qty)
  
      timeStopWatch.start(onLimitReach = {
        logger.info("calling onLimitReach")
      })
      
      timeStopWatch.isPending must beTrue
  
      waitForOfferMillis(offer.qty)
  
      timeStopWatch.isPending must beFalse
    }

  }
  
}
