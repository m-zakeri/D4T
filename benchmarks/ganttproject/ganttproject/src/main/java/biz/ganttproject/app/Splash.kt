/*
Copyright 2019 BarD Software s.r.o

This file is part of GanttProject, an open-source project management tool.

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
package biz.ganttproject.app

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import net.sourceforge.ganttproject.GanttProject
import java.util.concurrent.CompletableFuture

const val SPLASH_WIDTH = 400
const val SPLASH_HEIGHT = 400

fun showAsync(): CompletableFuture<Runnable> {
  JFXPanel()
  val result = CompletableFuture<Runnable>()
  Platform.runLater {
    val splash1 = ImageView(Image(GanttProject::class.java.getResourceAsStream("/icons/splash.png")))
    val splashLayout = VBox()
    splashLayout.children.addAll(splash1)
    splashLayout.effect = DropShadow()
    val splashScene = Scene(splashLayout)
    splashScene.fill = javafx.scene.paint.Color.TRANSPARENT
    val stage = Stage(StageStyle.TRANSPARENT)
    stage.isAlwaysOnTop = true
    stage.scene = splashScene
    val bounds = Screen.getPrimary().bounds
    stage.x = bounds.minX + bounds.width / 2 - SPLASH_WIDTH / 2
    stage.y = bounds.minY + bounds.height / 2 - SPLASH_HEIGHT / 2
    stage.show()
    result.complete(Runnable {
      Platform.runLater {
        stage.hide()
      }
    })
  }
  return result
}
