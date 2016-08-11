package galaxy.ide.configurable.editor.gef.router;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class HelloTopAnchor extends AbstractConnectionAnchor
{
    IFigure figure;

    public HelloTopAnchor(IFigure figure)
    {
        this.figure = figure;
        setOwner(figure);
    }

    @Override
    public Point getLocation(Point reference)
    {
        Rectangle r = Rectangle.SINGLETON;
        r.setBounds(getBox());
        r.translate(-1, -1);
        r.resize(1, 1);
        getOwner().translateToAbsolute(r);
        int x = r.x + r.width / 2;
        int y = r.y+3;
        return new Point(x, y);
    }

    protected Rectangle getBox()
    {
        return getOwner().getBounds();
    }
}
