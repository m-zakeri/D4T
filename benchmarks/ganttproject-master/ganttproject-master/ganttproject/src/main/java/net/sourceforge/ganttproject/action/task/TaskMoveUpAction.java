/*
GanttProject is an opensource project management tool.
Copyright (C) 2002-2011 Dmitry Barashev, GanttProject Team

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
package net.sourceforge.ganttproject.action.task;

import biz.ganttproject.ganttview.TaskTableActionConnector;
import kotlin.jvm.functions.Function0;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.UIUtil;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskContainmentHierarchyFacade;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskSelectionManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Move selected tasks up
 */
public class TaskMoveUpAction extends TaskActionBase {

  private final Function0<TaskTableActionConnector> myTableConnector;

  public TaskMoveUpAction(TaskManager taskManager, TaskSelectionManager selectionManager, UIFacade uiFacade,
                          Function0<TaskTableActionConnector> tableConnector) {
    super("task.move.up", taskManager, selectionManager, uiFacade);
    myTableConnector = tableConnector;
  }

  @Override
  protected String getIconFilePrefix() {
    return "up_";
  }

  @Override
  protected boolean isEnabled(List<Task> selection) {
    if (selection.size() == 0) {
      return false;
    }
    TaskContainmentHierarchyFacade taskHierarchy = getTaskManager().getTaskHierarchy();
    for (Task task : selection) {
      if (taskHierarchy.getPreviousSibling(task) == null) {
        // task is the first child of the parent
        return false;
      }
    }
    return true;
  }

  @Override
  protected void run(List<Task> selection) throws Exception {
    myTableConnector.invoke().getCommitEdit().invoke();
    final TaskContainmentHierarchyFacade taskHierarchy = getTaskManager().getTaskHierarchy();
    for (Task task : selection) {
      final Task parent = taskHierarchy.getContainer(task);
      final int index = taskHierarchy.getTaskIndex(task) - 1;
      myTableConnector.invoke().getRunKeepingExpansion().invoke(task, t -> {
        taskHierarchy.move(t, parent, index);
        return null;
      });
    }
    myTableConnector.invoke().getScrollTo().invoke(selection.get(0));
  }

  public TaskMoveUpAction asToolbarAction() {
    final TaskMoveUpAction result = new TaskMoveUpAction(getTaskManager(), getSelectionManager(), getUIFacade(), myTableConnector);
    result.setFontAwesomeLabel(UIUtil.getFontawesomeLabel(result));
    this.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("enabled".equals(evt.getPropertyName())) {
          result.setEnabled((Boolean)evt.getNewValue());
        }
      }
    });
    result.setEnabled(this.isEnabled());
    return result;
  }
}