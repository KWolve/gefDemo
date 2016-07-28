package wei.learn.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

public class CustomPolylineConnection extends PolylineConnection {
	private static final int OFFSET = 2;
	private Point originStartPoint;
	private Point originEndPoint;

	@Override
	protected void outlineShape(Graphics g) {
		PointList points = getPoints();
		if (points.size() <= 2) {
			super.outlineShape(g);
		} else if (points.size() > 2)// ���ڹյ�����
		{
			PointList shoulderPoints = new PointList();
			shoulderPoints.addPoint(points.getFirstPoint());
			for (int i = 1; i < points.size() - 1; i++)// ���������˵ĵ�,��Ӽ��
			{
				Point forwardPoint = points.getPoint(i - 1);
				Point anglePoint = points.getPoint(i);
				Point nextPoint = points.getPoint(i + 1);
				if (forwardPoint.x == anglePoint.x) // ��ֱ��
				{
					if (forwardPoint.y < anglePoint.y) // ����
					{
						shoulderPoints.addPoint(anglePoint.x, anglePoint.y
								- OFFSET);
					} else if (forwardPoint.y > anglePoint.y) // ����
					{
						shoulderPoints.addPoint(anglePoint.x, anglePoint.y
								+ OFFSET);
					}

					// anglePoint �� nextPoint��ˮƽ��
					if (anglePoint.x < nextPoint.x) { // ����
						shoulderPoints.addPoint(anglePoint.x + OFFSET,
								anglePoint.y);
					} else if (anglePoint.x > nextPoint.x) // ����
					{
						shoulderPoints.addPoint(anglePoint.x - OFFSET,
								anglePoint.y);
					}
				} else if (forwardPoint.y == anglePoint.y) // ˮƽ��
				{
					if (forwardPoint.x < anglePoint.x) // ����
					{
						shoulderPoints.addPoint(anglePoint.x - OFFSET,
								anglePoint.y);
					} else if (forwardPoint.x > anglePoint.x) // ����
					{
						shoulderPoints.addPoint(anglePoint.x + OFFSET,
								anglePoint.y);
					}

					// anglePoint �� nextPoint����ֱ��
					if (anglePoint.y < nextPoint.y) { // ����
						shoulderPoints.addPoint(anglePoint.x, anglePoint.y
								+ OFFSET);
					} else if (anglePoint.y > nextPoint.y) { // ����
						shoulderPoints.addPoint(anglePoint.x, anglePoint.y
								- OFFSET);
					}
				}
			}
			shoulderPoints.addPoint(points.getLastPoint());
			g.drawPolyline(shoulderPoints);
		}
	}

	public Point getOriginStartPoint() {
		return originStartPoint;
	}

	public void setOriginStartPoint(Point originStartPoint) {
		this.originStartPoint = originStartPoint;
	}

	public Point getOriginEndPoint() {
		return originEndPoint;
	}

	public void setOriginEndPoint(Point originEndPoint) {
		this.originEndPoint = originEndPoint;
	}
}
