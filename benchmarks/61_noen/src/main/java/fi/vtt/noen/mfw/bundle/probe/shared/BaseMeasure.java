/*
 * Copyright (C) 2010-2011 VTT Technical Research Centre of Finland.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package fi.vtt.noen.mfw.bundle.probe.shared;

/**
 * Describes the base measure provided by a probe.
 *
 * @author Teemu Kanstren
 */
public class BaseMeasure {
  //timestamp as provided by System.currentTime() for when this measure was collected
  private final long timestamp;
  //the measurement value, only string supported now. needs to be updated with other types when suitable cases are available.
  private final String measure;

  public BaseMeasure(String measure) {
    this.timestamp = System.currentTimeMillis();
    this.measure = measure;
  }

  public long getTime() {
    return timestamp;
  }

  public String getMeasure() {
    return measure;
  }
}
