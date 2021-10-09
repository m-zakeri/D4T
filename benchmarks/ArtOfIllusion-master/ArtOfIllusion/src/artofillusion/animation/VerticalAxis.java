/* Copyright (C) 2001,2004 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.text.*;

/** This draws the vertical axis on graphs of keyframe values. */

public class VerticalAxis extends CustomWidget
{
  private double minValue, maxValue, vscale, tickPos[];
  private NumberFormat nf = NumberFormat.getNumberInstance();
  private boolean lineAtBottom;
  private Rectangle lastBounds;

  public static final int TICK_SIZE = 6;

  public VerticalAxis()
  {
    setGraphRange(0.0, 1.0);
    setPreferredSize(new Dimension(50, 0));
    addEventLink(RepaintEvent.class, this, "paint");
  }

  /** Select the range for the graph. */

  public void setGraphRange(double min, double max)
  {
    Rectangle dim = getBounds();
    minValue = min;
    maxValue = max;
    vscale = dim.height/(max-min);

    // Select the vertical tick positions.


    double height = dim.height;
    if (height <= 0)
      return;
    double increment = Math.pow(10.0, Math.floor(Math.log((max-min))/Math.log(10.0)));
    double pixels = (height*increment)/(max-min);
    if (pixels < 20.0)
    {
      while (pixels < 20.0)
      {
        pixels *= 5.0;
        increment *= 5.0;
      }
    }
    else if ((max-min)/increment < 4.0)
      increment *= 0.5;
    double lowest = Math.ceil(min/increment)*increment;
    int num = (int) Math.ceil((max-lowest)/increment)+1;
    tickPos = new double [num];
    for (int i = 0; i < num; i++)
      tickPos[i] = lowest+i*increment;
  }

  /** Set whether a line should be draw along the bottom edge. */

  public void showLineAtBottom(boolean show)
  {
    lineAtBottom = show;
  }

  private void paint(RepaintEvent ev)
  {
    Graphics2D g = ev.getGraphics();
    FontMetrics fm = g.getFontMetrics(g.getFont());
    int labelOffset = (fm.getMaxAscent()+fm.getMaxDescent())/2;
    Rectangle dim = getBounds();
    if (lastBounds == null || dim.width != lastBounds.width || dim.height != lastBounds.height)
      setGraphRange(minValue, maxValue);

    // Draw the ticks on the vertical axis.

    if (tickPos == null)
      return;
    if (tickPos.length > 1)
      nf.setMaximumFractionDigits((int) Math.max(0, 1-Math.log(tickPos[1]-tickPos[0])/Math.log(10)));
    else
      nf.setMaximumFractionDigits(3);
    for (int i = 0; i < tickPos.length; i++)
    {
      int y = dim.height-(int) Math.round(vscale*(tickPos[i]-minValue));
      g.drawLine(dim.width-TICK_SIZE, y, dim.width, y);
      String s = nf.format(tickPos[i]);
      int x = dim.width-TICK_SIZE-2-fm.stringWidth(s);
      g.drawString(s, x, y+labelOffset);
    }
    if (lineAtBottom)
      g.drawLine(0, dim.height-1, dim.width, dim.height-1);
  }
}
