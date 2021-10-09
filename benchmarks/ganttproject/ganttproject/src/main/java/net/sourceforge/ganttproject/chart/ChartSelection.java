/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2011 GanttProject Team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.chart;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public interface ChartSelection {
  boolean isEmpty();

  IStatus isDeletable();

  void startCopyClipboardTransaction();

  void startMoveClipboardTransaction();

  void cancelClipboardTransaction();

  void commitClipboardTransaction();

  ChartSelection EMPTY = new ChartSelection() {
    @Override
    public void startMoveClipboardTransaction() {
    }

    @Override
    public void startCopyClipboardTransaction() {
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public IStatus isDeletable() {
      return Status.CANCEL_STATUS;
    }

    @Override
    public void commitClipboardTransaction() {
    }

    @Override
    public void cancelClipboardTransaction() {
    }
  };
}
