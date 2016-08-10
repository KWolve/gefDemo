package wei.learn.gef.router;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;

public class MyBendpointConnectionRouter extends AbstractRouter {
	private Map<Connection, Object> constraints = new HashMap<Connection, Object>(
			11);
	private static final PrecisionPoint A_POINT = new PrecisionPoint();

	/**
	 * Gets the constraint for the given {@link Connection}.
	 * 
	 * @param connection
	 *            The connection whose constraint we are retrieving
	 * @return The constraint
	 */
	public Object getConstraint(Connection connection) {
		return constraints.get(connection);
	}

	/**
	 * Removes the given connection from the map of constraints.
	 * 
	 * @param connection
	 *            The connection to remove
	 */
	public void remove(Connection connection) {
		constraints.remove(connection);
	}

	/**
	 * Routes the {@link Connection}. Expects the constraint to be a List of
	 * {@link org.eclipse.draw2d.Bendpoint Bendpoints}.
	 * 
	 * @param conn
	 *            The connection to route
	 */
	@SuppressWarnings("unchecked")
	public void route(Connection conn) {
		PointList points = conn.getPoints();
		Point oldStart = null;
		Point oldEnd = null;
		for (int i = 0; i < points.size(); i++) {
			System.err.println("point " + i + ":" + points.getPoint(i));

		}
		if (!(points.getFirstPoint().equals(new Point(0, 0)) && points
				.getLastPoint().equals(new Point(100, 100)))) {
			oldStart = points.getFirstPoint();
			oldEnd = points.getLastPoint();
		}
		points.removeAllPoints();

		List<Point> bendpoints = (List<Point>) getConstraint(conn);
		if (bendpoints == null)
			bendpoints = Collections.EMPTY_LIST;

		Point ref1, ref2;

		if (bendpoints.isEmpty()) {
			ref1 = conn.getTargetAnchor().getReferencePoint();
			ref2 = conn.getSourceAnchor().getReferencePoint();
		} else {
			ref1 = new Point((Point) (bendpoints.get(0)));
			conn.translateToAbsolute(ref1);
			ref2 = new Point((Point) (bendpoints.get(bendpoints.size() - 1)));
			conn.translateToAbsolute(ref2);
		}
		// ׼�����յ�
		Point newStart = new Point();
		newStart.setLocation(conn.getSourceAnchor().getLocation(ref1));
		conn.translateToRelative(newStart);
		Point newEnd = new Point();
		newEnd.setLocation(conn.getTargetAnchor().getLocation(ref2));
		conn.translateToRelative(newEnd);

		// �����߼�
		points.addPoint(newStart);
		if (oldStart != null && !oldStart.equals(newStart)) // ���ָı���ʼ������ ��Ҫ���⴦��
		{
			if (bendpoints.isEmpty()) // ��ǰ����ֻ�������� ��ֱһ��ֱ��
			{
				// �Լ���ƽ���߶ȴ����յ�
				int averageY = (newStart.y + newEnd.y) / 2;
				points.addPoint(newStart.x, averageY);
				points.addPoint(newEnd.x, averageY);
			} else { // ���ڹյ�����
				// �޸Ĺյ�λ��
				Point nextPoint = (Point) bendpoints.get(0);
				bendpoints.set(0, new AbsoluteBendpoint(new Point(newStart.x,
						nextPoint.y)));
			}
		}

		for (int i = 0; i < bendpoints.size(); i++) {
			Bendpoint bp = (Bendpoint) bendpoints.get(i);
			points.addPoint(bp.getLocation());
		}
		// Ϊ�˴��� ��ֱ���϶� ��ʼͼԴ�����  �ܶ��� ����ûɶ�����İ취
		if (oldStart != null && !oldStart.equals(newStart)
				&& bendpoints.isEmpty()) {
			int averageY = (newStart.y + newEnd.y) / 2;
			bendpoints.add(new AbsoluteBendpoint(newStart.x, averageY));
			bendpoints.add(new AbsoluteBendpoint(newEnd.x, averageY));
		}
		if (oldEnd != null && !oldEnd.equals(newEnd)) // ���ָı��յ����� ��Ҫ���⴦��
		{
			if (bendpoints.isEmpty()) // ��ǰ����ֻ�������� ��ֱһ��ֱ��
			{
				// �Լ���ƽ���߶ȴ����յ�
				int averageY = (newStart.y + newEnd.y) / 2;
				points.addPoint(newStart.x, averageY);
				points.addPoint(newEnd.x, averageY);
				bendpoints.add(new AbsoluteBendpoint(newStart.x, averageY));
				bendpoints.add(new AbsoluteBendpoint(newEnd.x, averageY));
			} else { // ���ڹյ�����
				// �޸Ĺյ�λ��
				Point fPoint = (Point) bendpoints.get(bendpoints.size() - 1);
				Point newfPoint = new AbsoluteBendpoint(newEnd.x, fPoint.y);
				bendpoints.set(bendpoints.size() - 1, newfPoint);
				points.setPoint(newfPoint, points.size() - 1);
			}
		}
		points.addPoint(newEnd);
		conn.setPoints(points);
	}

	/**
	 * Sets the constraint for the given {@link Connection}.
	 * 
	 * @param connection
	 *            The connection whose constraint we are setting
	 * @param constraint
	 *            The constraint
	 */
	public void setConstraint(Connection connection, Object constraint) {
		constraints.put(connection, constraint);
	}

}
