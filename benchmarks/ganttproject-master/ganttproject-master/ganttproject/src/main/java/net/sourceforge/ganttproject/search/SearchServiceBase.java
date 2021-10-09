/*
Copyright 2003-2012 GanttProject Team

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.ganttproject.search;

import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.gui.TreeUiFacade;
import net.sourceforge.ganttproject.gui.UIFacade;

/**
 * Base class for task and resource search services.
 *
 * @author dbarashev (Dmitry Barashev)
 *
 * @param <SR>
 *          search result object type
 * @param <SO>
 *          target search object type
 */
abstract class SearchServiceBase<SR extends SearchResult<SO>, SO> implements SearchService<SR, SO> {
  protected final int myViewIndex;
  private IGanttProject myProject;
  protected TreeUiFacade<SO> myTreeUiFacade;
  protected UIFacade myUiFacade;

  protected SearchServiceBase(int viewIndex) {
    myViewIndex = viewIndex;
  }

  protected void init(IGanttProject project, TreeUiFacade<SO> treeUiFacade, UIFacade uiFacade) {
    myProject = project;
    myTreeUiFacade = treeUiFacade;
    myUiFacade = uiFacade;
  }

  protected static boolean isNotEmptyAndContains(String doc, String query) {
    return doc != null && doc.toLowerCase().contains(query);
  }

  protected IGanttProject getProject() {
    return myProject;
  }

  protected UIFacade getUiFacade() { return myUiFacade; }
}
