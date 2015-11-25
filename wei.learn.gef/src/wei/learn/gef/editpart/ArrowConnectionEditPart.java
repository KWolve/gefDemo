package wei.learn.gef.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;

public class ArrowConnectionEditPart extends CustomAbstractConnectionEditPart
{
    protected IFigure createFigure()
    {
        // ���Ƕ���������
        PolylineConnection connection = (PolylineConnection) super.createFigure();
        // �����������������
        connection.setTargetDecoration(new PolygonDecoration());
        return connection;
    }

}
