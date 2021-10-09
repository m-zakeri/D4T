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

import static biz.ganttproject.task.TreeAlgorithmsKt.documentOrdered;
import static biz.ganttproject.task.TreeAlgorithmsKt.retainRoots;

/**
 * Indent several nodes that are selected
 */
public class TaskIndentAction extends TaskActionBase {

  private final Function0<TaskTableActionConnector> myTableConnector;

  public TaskIndentAction(TaskManager taskManager, TaskSelectionManager selectionManager, UIFacade uiFacade, Function0<TaskTableActionConnector> tableConnector) {
    super("task.indent", taskManager, selectionManager, uiFacade);
    myTableConnector = tableConnector;
  }

  @Override
  protected String getIconFilePrefix() {
    return "indent_";
  }

  @Override
  protected boolean isEnabled(List<Task> selection) {
    TaskMoveEnabledPredicate predicate = new TaskMoveEnabledPredicate(getTaskManager(), new IndentTargetFunctionFactory(getTaskManager()));
    return predicate.apply(selection);
  }

  @Override
  protected void run(List<Task> selection) {
    getTaskManager().getAlgorithmCollection().getScheduler().setEnabled(false);
    try {
      final TaskContainmentHierarchyFacade taskHierarchy = getTaskManager().getTaskHierarchy();
      indent(selection, taskHierarchy, (task, newParent) -> myTableConnector.invoke().getRunKeepingExpansion().invoke(task, t ->  {
        taskHierarchy.move(t, newParent);
        return null;
      }));
    } finally {
      getTaskManager().getAlgorithmCollection().getScheduler().setEnabled(true);
    }
  }

  public TaskIndentAction asToolbarAction() {
    final TaskIndentAction result = new TaskIndentAction(getTaskManager(), getSelectionManager(), getUIFacade(), myTableConnector);
    result.setFontAwesomeLabel(UIUtil.getFontawesomeLabel(result));
    this.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("enabled".equals(evt.getPropertyName())) {
          result.setEnabled((Boolean) evt.getNewValue());
        }
      }
    });
    result.setEnabled(this.isEnabled());
    return result;
  }

  public interface IndentApplyFxn {
    void apply(Task task, Task newParent);
  }

  public static void indent(List<Task> selectedTasks, TaskContainmentHierarchyFacade taskHierarchy, IndentApplyFxn fxn) {
    documentOrdered(retainRoots(selectedTasks), taskHierarchy).forEach(task -> {
      final Task newParent = taskHierarchy.getPreviousSibling(task);
      fxn.apply(task, newParent);
    });
  }
}
