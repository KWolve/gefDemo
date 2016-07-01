package galaxy.ide.configurable.editor.gef.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

@SuppressWarnings("serial")
// ��CheckedPointList2��ȫһ�� �����������ΪAStar�����ṩ�� δ�������и��Ի��޸� ���Ե�������һ��
public class AStarCheckedPointList extends PointList {

	// ���Ϊ�غϵľ���
	private int CHECK_DISTANCE = 10;
	// X�����غ� X����ƫת�ľ���
	private int LINE_OFFSET_X = 1;
	// Y�����غϺ�ƫת����
	private int LINE_OFFSET_Y = 1;
	// ���ƿ���
	private boolean on_off = true;
	// ˳·����
	private boolean on_the_way = false;

	/****** shoulder *******************************************************/
	// ��򿪹�
	private boolean shoulder_on_off = false;
	// shoulder xƫ����
	private int SHOULDER_OFFSET_X = 3;
	// shoulder yƫ����
	private int SHOULDER_OFFSET_Y = 3;
	/****** bridge *******************************************************/
	// bridge ����
	private Connection conn;
	// ��������
	private List<Connection> othersConnections = new ArrayList<Connection>();
	// ��¼�յ� Ϊ˳·�ṩ���� ˳·���عرվ������ע��
	private Point newEndPoint;
	// ��ѡ��ӵĵ�
	private Point caditatePoint;
	// �Ѿ���ӵ�List�е����ĵ� ������ǿ��Ըı��
	private Point lastPoint;

	// ����ÿ���ߵ��յ�͹��յ����˳·����
	// ����Ϊ�˽�����ı��յ�ͼԴλ��ʱ,���ߵ��¾��߻��໥���ų��ִ����ƽ����
	private HashMap<Connection, Point> connectionEndPoint = new HashMap<Connection, Point>();
	private HashMap<Point, ArrayList<Connection>> connectionsWithSameEndPoint = new HashMap<Point, ArrayList<Connection>>();
	private boolean changeEndPoints = false;

	public void setConn(Connection conn) {
		this.conn = conn;
		refreshConnections();
	}

	public void setOnOffMergeLine(boolean on_off_MergeLine) {
		on_the_way = on_off_MergeLine;
	}

	public void setEndPoint(Point endPoint) {
		// ֻ�Ǹ��µ�ȫ�ֱ��� ��û�б��浽connectionEndPoint��
		this.newEndPoint = endPoint;
		// ���֮ǰ�Ѿ������
		if (connectionEndPoint.get(conn) != null) {
			// ����֮ǰ���յ�
			Point oldEndPoint = connectionEndPoint.get(conn);
			if (oldEndPoint.equals(endPoint)) // �����еĴ洢��ͬ,������º��޸�.
			{
				// ���Ǹı��յ�Ĳ���
				return;
			} else {
				changeEndPoints = true;
			}

		} else // ���֮ǰû�б����,������һ�ε�����.
		{
			connectionEndPoint.put(conn, endPoint);
		}

		// �����Ƿ�ı����յ���ֱ��� ���othersConnections
		othersConnections.clear();
		if (changeEndPoints == false) // ����ִ��
		{
			refreshConnections();
		} else // �����յ�ı�����
		{
			// ��ȡԭ���յ��ConnectionList
			Point oldEndPoint = connectionEndPoint.get(conn);
			ArrayList<Connection> connectionList = connectionsWithSameEndPoint
					.get(oldEndPoint);
			refreshConnectionsWithoutSameEndPointConnection(connectionList);
			// Ϊ���������غ�����׼�� ��Ϊ �Լ����޸�������»�������� �͵����������غ��߼�⵽�Լ�.
			if (connectionList != null) {
				connectionList.remove(conn);
			}
			// �Ƴ����õ��յ�
			if (connectionList == null) {
				connectionsWithSameEndPoint.remove(oldEndPoint);
			}
			// �����µ��յ�
			connectionEndPoint.put(conn, endPoint);
			changeEndPoints = false;
		}
	}

	@SuppressWarnings("unchecked")
	private void refreshConnectionsWithoutSameEndPointConnection(
			ArrayList<Connection> connectionList) {
		List<Connection> allConnections = conn.getParent().getChildren();
		for (int i = 0; i < allConnections.size(); i++) {
			// ������ı��յ�ͼԴλ��ʱ,���ߵ��¾��߻��໥���Ŵ������ƽ��������
			// ����˼·��,������һ���غ�����ͼ�ı��յ�ʱ,�������غ��߾ʹ�otherConnections��ȥ��
			if (connectionList == null) // connectionList==null
			{
				if (allConnections.get(i) != conn) {
					othersConnections.add(allConnections.get(i));
				}
			}
			if (connectionList != null
					&& !connectionList.contains(allConnections.get(i))) {
				othersConnections.add(allConnections.get(i));
			}

		}
	}
	@SuppressWarnings("unchecked")
	// ˢ�������ߵ�����
	private void refreshConnections() {
		othersConnections.clear();
		List<Connection> allConnections = conn.getParent().getChildren();
		for (int i = 0; i < allConnections.size(); i++) {
			// ���ڲ��ܸ��Լ��Ƚ�λ�� �����Ƴ��Լ�
			if (allConnections.get(i) != conn) {
				othersConnections.add(allConnections.get(i));
			}
		}
	}

	// ��Ӻ�ѡ��
	public void addCandidatePoint(Point point) {
		addCandidatePoint(point.x, point.y);
	}

	public void addCandidatePoint(int x, int y) {
		caditatePoint = new Point(x, y);
		// ����������ӵĵ�һ���ߵڶ�����,�Ͳ��ü����
		// ����������ӵĵ�һ���߾Ͳ�����
		if (othersConnections.size() == 0 || size() < 2) {
			addPoint(caditatePoint);
		} else
		// ��ҪУ��
		{
			// ȡ�����һ��
			lastPoint = getLastPoint();
			removePoint(size() - 1);
			if (on_off) {
				if (lastPoint.x == caditatePoint.x) {
					checkCaditateX();
				} else if (lastPoint.y == caditatePoint.y) {
					checkCaditateY();
				}
			}
			addCheckedPoints();
		}
	}

	// ����ͺ�ѡ��ͨ���˼��� ��ӵ�list��
	private void addCheckedPoints() {
		addPoint(lastPoint);
		addPoint(caditatePoint);
	}

	// ���׼����ӵ��ߵĵ��Ƿ���������ص� X����
	private void checkCaditateX() {
		// ������ֻ��ˮƽ��ֱ������� ���Կ�����������ߵ�����
		// �������е���
		for (int i = 0; i < othersConnections.size(); i++) {
			Connection conn = othersConnections.get(i);
			// ��ȡ�ߵ����еĵ�
			PointList pointList = conn.getPoints();
			// �����ߵ����еĵ�
			// -1��ԭ���Ǳ������ ��������ˮƽ��ֱ�� �������ڵ��Ȼ��X��ͬ,����Y��ͬ ���ǹ�ע�����������X�ͼ��ĵ��X��ͬ
			// һ�������������� �������������һ��
			for (int j = 0; j < pointList.size() - 1; j++) {
				// ����ֻ��x��ͬ�ſ��ܴ����ص� ��׼ȷ��˵���������ڵĵ��x��ͬ
				if (Math.abs(pointList.getPoint(j).x - lastPoint.x) <= CHECK_DISTANCE
						&& pointList.getPoint(j).x == pointList.getPoint(j + 1).x)// ����ж���Ϊ�˵��������غ�ʱ,�������ж�Ϊ�غ�
				{
					// ������ϸ���ж�
					// ����
					int y1 = Math.min(pointList.getPoint(j).y,
							pointList.getPoint(j + 1).y);
					int y2 = Math.max(pointList.getPoint(j).y,
							pointList.getPoint(j + 1).y);
					int y3 = Math.min(lastPoint.y, caditatePoint.y);
					int y4 = Math.max(lastPoint.y, caditatePoint.y);
					// ��ֱλ����ͬ�����ص�
					if (y1 > y4 || y2 < y3) {
						break;
					} else
					// �ص�����λ��
					{
						// ����Ƿ�˳· ���˳·��ֹͣ���� ���ҵ���������·�ߵ�λ��
						if (on_the_way) {
							if (newEndPoint.equals(pointList.getLastPoint().x,
									pointList.getLastPoint().y)) {
								lastPoint.x = pointList.getPoint(j).x;
								caditatePoint.x = pointList.getPoint(j).x;
								recondSameEndConnections(conn);
								return;
							}
						}

						// �������еķ��� ƫ��
						if (!caditatePoint.equals(newEndPoint)) {
							Point frontLastPoint = getLastPoint();
							// x���������
							if (frontLastPoint.x < lastPoint.x) {
								// ����
								lastPoint.x += LINE_OFFSET_X;
								caditatePoint.x += LINE_OFFSET_X;
							} else {
								// ����
								lastPoint.x -= LINE_OFFSET_X;
								caditatePoint.x -= LINE_OFFSET_X;
							}
							// �ݹ�����λ�� �Ƿ����ص�
							checkCaditateX();
						}else
						{
							return;
						}
					}

				}
			}
		}
	}

	public void recondSameEndConnections(Connection conn) {
		ArrayList<Connection> connectionList;
		if (connectionsWithSameEndPoint.get(newEndPoint) == null) {
			connectionList = new ArrayList<Connection>();
			connectionList.add(this.conn);
			connectionList.add(conn);
		} else {
			connectionList = connectionsWithSameEndPoint.get(newEndPoint);
			if (!connectionList.contains(this.conn)) {
				connectionList.add(this.conn);
			}
			if (!connectionList.contains(conn)) {
				connectionList.add(conn);
			}
		}
		connectionsWithSameEndPoint.put(newEndPoint, connectionList);
	}

	// ���׼����ӵ��ߵĵ��Ƿ���������ص� Y����
	private void checkCaditateY() {
		// �������е���
		for (int i = 0; i < othersConnections.size(); i++) {
			Connection conn = othersConnections.get(i);
			// �ж����е����Ƿ�����ص�
			PointList pointList = conn.getPoints();
			// �����ߵ����еĵ�
			for (int j = 0; j < pointList.size() - 1; j++) {
				// ����ֻ��x��ͬ�ſ��ܴ����ص� ��׼ȷ��˵���������ڵĵ��x��ͬ
				if (Math.abs(pointList.getPoint(j).y - lastPoint.y) <= CHECK_DISTANCE
						&& pointList.getPoint(j).y == pointList.getPoint(j + 1).y) {
					// ������ϸ���ж�
					// ����
					int x1 = Math.min(pointList.getPoint(j).x,
							pointList.getPoint(j + 1).x);
					int x2 = Math.max(pointList.getPoint(j).x,
							pointList.getPoint(j + 1).x);
					int x3 = Math.min(lastPoint.x, caditatePoint.x);
					int x4 = Math.max(lastPoint.x, caditatePoint.x);
					// ��ֱλ����ͬ�����ص�
					if (x1 > x4 || x2 < x3) {
						break;
					} else
					// �ص�����λ��
					{
						// ����Ƿ�˳· ���˳·��ֹͣ���� ���ҵ���������·�ߵ�λ��
						if (on_the_way) {
							if (newEndPoint.equals(pointList.getLastPoint().x,
									pointList.getLastPoint().y)) {
								lastPoint.y = pointList.getPoint(j).y;
								caditatePoint.y = pointList.getPoint(j).y;
								recondSameEndConnections(conn);
								return;
							}
						}
						// �������еķ��� ƫ��
						Point frontLastPoint = getLastPoint();
						if (frontLastPoint.y < lastPoint.y) {
							// ����
							lastPoint.y += LINE_OFFSET_Y;
							caditatePoint.y += LINE_OFFSET_Y;
						} else {
							// ����
							lastPoint.y -= LINE_OFFSET_Y;
							caditatePoint.y -= LINE_OFFSET_Y;
						}
						checkCaditateY();
					}

				}
			}
		}
	}

	// shoulder����˼����󵽴��յ�ǰ����ֱ���� �����ṩһ������
	public void addShoulder() {
		if (shoulder_on_off == false) {
			return;
		}
		// �����ֱ��ֱ������
		if (size() > 2) {
			// ȡ���������� ɾ����������
			int size = size();
			Point lastPoint = getPoint(size - 1);
			Point middlePoint = getPoint(size - 2);
			Point frontPoint = getPoint(size - 3);
			// ֻ����ֱ��������
			if (lastPoint.x == middlePoint.x
					&& Math.abs(frontPoint.x - middlePoint.x) >= SHOULDER_OFFSET_X) // ��ȫ���
			{
				Point middleFrontPoint = middlePoint.getCopy();
				if (frontPoint.x < middlePoint.x) // "7"��״
				{
					// ���ƶ���������
					middleFrontPoint.x = middlePoint.x - SHOULDER_OFFSET_X;
				} else {
					// ���ƶ���������
					middleFrontPoint.x = middlePoint.x + SHOULDER_OFFSET_X;
				}
				// �Ƴ�ԭ�йյ�
				removePoint(size - 2);
				// �����ǰ��
				insertPoint(middleFrontPoint, size - 2);
				Point middleNextPoint = middlePoint.getCopy();
				middleNextPoint.y = middlePoint.y + SHOULDER_OFFSET_Y;
				// ����к��
				insertPoint(middleNextPoint, size - 1);
			}
		}
	}

}
