package galaxy.ide.configurable.editor.gef.router;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.geometry.Geometry;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.ui.IEditorPart;

import wei.learn.gef.figure.CustomPolylineConnection;
import wei.learn.gef.helper.Utility;
import wei.learn.gef.ui.DiagramEditor;

/**
 * �����ͼԤ�����ܣ��Բ�ͬ�Ľڵ����ֲ�ͬ��Gֵ
 * 
 * @author caiyu
 * @date 2014-5-15
 */
public class AStarConnectionRouter2 extends AbstractRouter implements
		RouterStyle {

	private DiagramEditor editor;
	/**
	 * �ڵ�Ԥ������
	 */
	private ANodePreReader preReader = new ANodePreReader();

	private final int D;
	final private int style;

	private LayerManager manager;

	private FreeformLayer layer;

	// �̳����е�PointList�� ����ӵ�֮ǰ ���м�����(�����ص�)
	private AStarCheckedPointList points = new AStarCheckedPointList();
	// ֧�ֺϲ����ߵ��л�
	public boolean on_off_MergeLine = true;

	/**
	 * 
	 * @param editPart
	 * @param gridLength
	 *            ���ӳ���
	 * @param style
	 *            �ο�{@link RouterStyle} : FLOYD,FLOYD_FLAT,FOUR_DIR,SHOWPOOL
	 */
	public AStarConnectionRouter2(DiagramEditor diaEditor, int gridLength,
			int style) {
		this.editor = diaEditor;
		manager = (LayerManager) editor.getGraphicalViewer()
				.getEditPartRegistry().get(LayerManager.ID);
		layer = (FreeformLayer) manager
				.getLayer(org.eclipse.gef.LayerConstants.HANDLE_LAYER);
		D = gridLength;
		this.style = style;
	}

	@Override
	public void route(Connection conn) {
		// AStar�Զ����߲���
		long time = System.currentTimeMillis();
		// ׼��AStar����
		List<ANode> openList = new LinkedList<ANode>();
		List<ANode> closedList = new LinkedList<ANode>();
		Point startPoint = getStartPoint(conn).getCopy();
		Point endPoint = getEndPoint(conn).getCopy();

		// ׼��AStarCheckedPointList
		points.removeAllPoints();
		points.setConn(conn);
		points.setOnOffMergeLine(on_off_MergeLine);
		points.setEndPoint(endPoint);

		preReader.setStartPoint(startPoint);
		preReader.setEndPoint(endPoint);

		conn.translateToRelative(startPoint);
		conn.translateToRelative(endPoint);

		ANode startNode = new ANode();
		ANode endNode = new ANode(endPoint, startPoint, D);

		// ���һ��startNode �� endNode
		// showStartEndNode(startPoint, startNode, endNode);

		// ���յ�����ͼԴ�����
		if ((this.style & CONSOLE_INFO) == CONSOLE_INFO) {
			System.err.println("�յ�������ϵ�е�λ�ã�" + "xIndex:" + endNode.xIndex
					+ "yIndex:" + endNode.yIndex);
			System.out
					.println("------------------------search List-----------------------------------------");
		}
		preReadingNodes();

		openList.add(startNode);

		ANode minFNode = null;
		int i = 0;
		while (true) {
			i++;
			minFNode = findMinNode(openList);
			openList.remove(minFNode);
			closedList.add(minFNode);
			if (minFNode == null || minFNode.equals(endNode))
				break;
			if ((this.style & CONSOLE_INFO) == CONSOLE_INFO) {
				System.out.println("NO: " + i + "    position:("
						+ minFNode.xIndex + "," + minFNode.yIndex + ")   f():"
						+ minFNode.f() + " g:" + minFNode.g + " h:"
						+ minFNode.h);
			}
			search(minFNode, openList, closedList, startNode, endNode);
		}
		if ((this.style & CONSOLE_INFO) == CONSOLE_INFO) {
			System.out.println("�������Եĵ�ĸ�����" + i);
		}
		test(openList, closedList, startPoint);
		showPool();
		PointList aPathList = new PointList();
		if (minFNode != null) {
			// ƽ������
			floyd(minFNode, startPoint, D);

			ANode tempNode = minFNode;
			aPathList.addPoint(tempNode.getPoint(startPoint, D));
			// ���·��
			if ((this.style & CONSOLE_INFO) == CONSOLE_INFO) {
				System.err
						.println("---------------------------route----------------------------------");
				System.err.println("����");
				System.err.println("xIndex:" + tempNode.xIndex + "  yIndex:"
						+ tempNode.yIndex);
			}
			while (true) {
				tempNode = tempNode.getParent();
				if (tempNode == null)
					break;
				aPathList.insertPoint(tempNode.getPoint(startPoint, D), 0);
				if ((this.style & CONSOLE_INFO) == CONSOLE_INFO) {
					System.err.println("xIndex:" + tempNode.xIndex
							+ "  yIndex:" + tempNode.yIndex);
				}
			}
		}
		if (aPathList.size() == 0) {
			aPathList.addPoint(startPoint);
		}

		pathExtraControl(aPathList, endPoint);
		aPathList.addPoint(endPoint);
		// ��a*�㷨�����ļ��Ϸֱ���뵽AStarCheckedPointList �յ�ֱ����������� ���豸ѡ
		for (int j = 0; j < aPathList.size() - 1; j++) {
			// System.out.println("con:"+connection.hashCode()+"NO:"+j+" points:"+aPathList.getPoint(j));
			points.addCandidatePoint(aPathList.getPoint(j));
		}
		points.addCandidatePoint(endPoint);
		conn.setPoints(points.getCopy());

		points.removeAllPoints();
		openList.clear();
		closedList.clear();
		preReader.reset();
		if ((this.style & CONSOLE_INFO) == CONSOLE_INFO) {
			System.out.println("��ʱ�� " + (System.currentTimeMillis() - time));
		}
	}

	private PointList calculateNewStartManhattanPath(Point basicPoint,
			Point startPoint) {
		PointList points = new PointList();
		if (basicPoint.y > startPoint.y) { // ˳������
			points.addPoint(startPoint.x, basicPoint.y); // ֱ������յ��Ϸ��ĵ㼴��
		} else if (basicPoint.y < startPoint.y) // ��������
		{
			// ����Z�͵����� �Ծ�ֵΪ�۵�
			int middleX = startPoint.x + (basicPoint.x - startPoint.x) / 2;
			points.addPoint(startPoint.x, startPoint.y + D);
			points.addPoint(middleX, startPoint.y + D);
			points.addPoint(middleX, basicPoint.y);
		}
		return points;
	}

	private PointList calculateNewEndManhattanPath(Point basicPoint,
			Point endPoint) {
		PointList points = new PointList();
		if (basicPoint.y < endPoint.y) { // ˳������
			points.addPoint(endPoint.x, basicPoint.y); // ֱ������յ��Ϸ��ĵ㼴��
		} else if (basicPoint.y > endPoint.y) // ��������
		{
			// ����Z�͵����� �Ծ�ֵΪ�۵�
			int middleX = basicPoint.x + (endPoint.x - basicPoint.x) / 2;
			points.addPoint(middleX, basicPoint.y);
			points.addPoint(middleX, endPoint.y - D);
			points.addPoint(endPoint.x, endPoint.y - D);
		}
		return points;
	}

	private void search(ANode node, List<ANode> openList,
			List<ANode> closedList, ANode startNode, ANode endNode) {
		ANode[] nodes = findAroundNode(node);
		for (int i = 0, len = nodes.length; i < len; i++) {
			if (nodes[i].getLevel() == ANodeLevel.DEAD)
				continue;
			nodes[i].g = (i <= 3 ? nodes[i].getLevel().RE
					: nodes[i].getLevel().BE) + node.g;
			nodes[i].h = caculateH(nodes[i], endNode);
			if (closedList.contains(nodes[i]))
				continue;
			if (!openList.contains(nodes[i])) {
				openList.add(nodes[i]);
				nodes[i].setParent(node);
			}
			// else if (openList.contains(nodes[i]))
			else {
				int idx = openList.indexOf(nodes[i]);
				ANode n = openList.get(idx);
				if (nodes[i].g < n.g) {
					openList.remove(idx);
					closedList.add(n);
					nodes[i].setParent(n.getParent());
					openList.add(idx, nodes[i]);
				}
			}
		}
	}

	private int caculateH(ANode p, ANode endNode) {
		return (Math.abs(endNode.xIndex - p.xIndex) + Math.abs(endNode.yIndex
				- p.yIndex))
				* p.getLevel().RE;
	}

	// private void showStartEndNode(Point startPoint, ANode startNode,
	// ANode endNode) {
	// removeOldHandles(layer, SpecialHandler.class);
	// SpecialHandler sh = new SpecialHandler();
	// Point p = startNode.getPoint(startPoint, D);
	// sh.setBounds(new Rectangle(p.x, p.y, 10, 10));
	// sh.setOwner((GraphicalEditPart) editor.getGraphicalViewer()
	// .getContents());
	//
	// SpecialHandler sh2 = new SpecialHandler();
	// p = endNode.getPoint(startPoint, D);
	// sh2.setBounds(new Rectangle(p.x, p.y, 10, 10));
	// sh2.setOwner((GraphicalEditPart) editor.getGraphicalViewer()
	// .getContents());
	// layer.add(sh);
	// layer.add(sh2);
	// }

	private void pathExtraControl(PointList points, Point endPoint) {
		// �����յ�û���ڷ����� ����б�ߵĴ���
		int size = points.size();
		// ��ֱ����ʱ
		if (size == 2) {
			Point pointN1 = points.getLastPoint();
			points.addPoint(new Point(endPoint.x, pointN1.y));
			return;
		}
		if (size < 3)
			return;
		// ˮƽб��
		// _________
		// .
		Point pointN1 = points.getLastPoint();
		points.setPoint(new Point(endPoint.x, pointN1.y), size - 1);
		// ��������ʱ
		// _________
		// |
		// |
		// .
		//
		Point pointN2 = points.getPoint(size - 2);
		if (pointN2.x == pointN1.x) {
			points.setPoint(new Point(endPoint.x, pointN2.y), size - 2);
			points.removePoint(size - 1);
		}

	}

	private void showPool() {
		if ((this.style & SHOW_POOL) != SHOW_POOL)
			return;
		Map<Integer, Map<Integer, ANodeLevel>> pool = preReader.getPool();
		// �Ƴ�֮ǰ��oldHandlers
		removeOldHandles(layer, PoolHandler.class);
		List<ANode> poolList = new ArrayList<ANode>();
		for (Map.Entry<Integer, Map<Integer, ANodeLevel>> entrySet : pool
				.entrySet()) {
			int x = entrySet.getKey();
			Map<Integer, ANodeLevel> valueMap = entrySet.getValue();
			for (Map.Entry<Integer, ANodeLevel> valueEntry : valueMap
					.entrySet()) {
				int y = valueEntry.getKey();
				ANodeLevel level = valueEntry.getValue();
				ANode node = new ANode(x, y);
				node.setLevel(level);
				poolList.add(node);
			}
		}
		for (ANode poolNode : poolList) {
			Point p = poolNode.getPoint(preReader.getStartPoint(), D);
			PoolHandler h = new PoolHandler();
			h.level = poolNode.getLevel();
			h.setBounds(new Rectangle(p.x, p.y, 5, 5));
			h.setOwner((GraphicalEditPart) editor.getGraphicalViewer()
					.getContents());
			layer.add(h);
		}

	}

	/**
	 * �Խڵ���Ԥ����Ԥ��ڵ�LEVEL
	 */
	private void preReadingNodes() {
		Rectangle r;
		for (Object c : editor.getGraphicalViewer().getContents().getChildren()) {
			if (c instanceof GraphicalEditPart) {
				r = ((GraphicalEditPart) c).getFigure().getBounds();
				preReader.read(r, D);
			}
		}
	}

	private ANode[] findAroundNode(ANode node) {
		ANode[] nodes = new ANode[((this.style & FOUR_DIR) == FOUR_DIR) ? 4 : 8];
		nodes[0] = preReader.getNode(node.xIndex, node.yIndex + 1);
		nodes[1] = preReader.getNode(node.xIndex, node.yIndex - 1);
		nodes[2] = preReader.getNode(node.xIndex + 1, node.yIndex);
		nodes[3] = preReader.getNode(node.xIndex - 1, node.yIndex);

		if ((this.style & FOUR_DIR) != FOUR_DIR) {
			nodes[4] = preReader.getNode(node.xIndex - 1, node.yIndex + 1);
			nodes[5] = preReader.getNode(node.xIndex + 1, node.yIndex - 1);
			nodes[6] = preReader.getNode(node.xIndex + 1, node.yIndex + 1);
			nodes[7] = preReader.getNode(node.xIndex - 1, node.yIndex - 1);
		}
		return nodes;
	}

	/**
	 * ����
	 * 
	 * @param openList
	 * @param closedList
	 * @param startPoint
	 */
	private void test(List<ANode> openList, List<ANode> closedList,
			Point startPoint) {
		if ((this.style & TEST) != TEST)
			return;
		removeOldHandles(layer, TestHandler.class);

		for (ANode openNode : openList) {
			Point p = openNode.getPoint(startPoint, D);
			TestHandler h = new TestHandler();
			h.open = true;
			h.setBounds(new Rectangle(p.x, p.y, D, D));
			h.setOwner((GraphicalEditPart) editor.getGraphicalViewer()
					.getContents());
			layer.add(h);
		}
		for (ANode openNode : closedList) {
			if (openNode == null)
				continue;
			Point p = openNode.getPoint(startPoint, D);
			TestHandler h = new TestHandler();
			h.open = false;
			h.setBounds(new Rectangle(p.x, p.y, D, D));
			h.setOwner((GraphicalEditPart) editor.getGraphicalViewer()
					.getContents());
			layer.add(h);
		}
	}

	@SuppressWarnings("unchecked")
	private void removeOldHandles(FreeformLayer layer, Class<?> clazz) {
		List<Figure> removeFigures = new ArrayList<Figure>();
		List<Object> layerFigure = layer.getChildren();
		for (Iterator<Object> iterator = layerFigure.iterator(); iterator
				.hasNext();) {
			Object object = (Object) iterator.next();
			if (clazz == TestHandler.class) {
				if (object instanceof TestHandler) {
					TestHandler testHandler = (TestHandler) object;
					removeFigures.add(testHandler);
				}
			} else if (clazz == PoolHandler.class) {
				if (object instanceof PoolHandler) {
					PoolHandler poolHandler = (PoolHandler) object;
					removeFigures.add(poolHandler);
				}
			} else {
				// do nothing
			}
		}
		for (Iterator<Figure> iterator = removeFigures.iterator(); iterator
				.hasNext();) {
			Figure object = (Figure) iterator.next();
			layer.remove(object);
		}
	}

	/**
	 * ��������ƽ������
	 * 
	 * @param D
	 * @param startPoint
	 * 
	 * @param points
	 */
	public void floyd(ANode node, Point startPoint, int D) {
		if ((this.style & FLOYD_SIMPLIFY) != FLOYD_SIMPLIFY
				&& (this.style & FLOYD) != FLOYD)
			return;
		ANode fatherNode, currentNode = node, grandNode;
		// ȥ������
		while (true) {
			fatherNode = currentNode.getParent();
			if (fatherNode == null)
				break;
			grandNode = fatherNode.getParent();
			if (grandNode == null)
				break;
			// ɾ�������յ� �������������� x1/y1=x2/y2 ���ݣ� x1y2=x2y1 �ж�
			// /|
			// / |
			// / |
			// / |y2
			// /| |
			// / |y1 |
			// / | |
			// /___|___|
			// x1
			// |--x2---|
			if ((fatherNode.xIndex - currentNode.xIndex)
					* (grandNode.yIndex - currentNode.yIndex) == (grandNode.xIndex - currentNode.xIndex)
					* (fatherNode.yIndex - currentNode.yIndex)) {
				currentNode.setParent(grandNode);
			} else
				currentNode = fatherNode;
		}
		currentNode = node;

		if ((this.style & FLOYD) != FLOYD)
			return;
		// ȥ���յ�
		while (true) {
			fatherNode = currentNode.getParent();
			if (fatherNode == null)
				break;
			while (true) {
				grandNode = fatherNode.getParent();
				if (grandNode == null)
					break;
				if (linkable(currentNode, grandNode, startPoint, D)) {
					currentNode.setParent(grandNode);
				}
				fatherNode = grandNode;
			}
			currentNode = currentNode.getParent();
			if (currentNode == null)
				break;
		}
	}

	private boolean linkable(final ANode firstNode, final ANode lastNode,
			final Point startPoint, final int D) {
		Point firstPoint = firstNode.getPoint(startPoint, D);
		Point lastPoint = lastNode.getPoint(startPoint, D);
		for (Object c : editor.getGraphicalViewer().getContents().getChildren()) {
			if (c instanceof GraphicalEditPart) {
				if (intersects(((GraphicalEditPart) c).getFigure().getBounds(),
						firstPoint, lastPoint)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * ָ������������Ƿ��ָ���������ཻ
	 * 
	 * @param rect
	 * @param fp
	 * @param lp
	 * @return
	 */
	private boolean intersects(Rectangle r, Point fp, Point lp) {
		if (r.contains(fp) || r.contains(lp))
			return true;
		int diagonal1x1 = r.x, diagonal1y1 = r.y, diagonal1x2 = r.x + r.width, diagonal1y2 = r.y
				+ r.height, diagonal2x1 = r.x + r.width, diagonal2y1 = r.y, diagonal2x2 = r.x, diagonal2y2 = r.y
				+ r.height;

		if (Geometry.linesIntersect(diagonal1x1, diagonal1y1, diagonal1x2,
				diagonal1y2, fp.x, fp.y, lp.x, lp.y)
				|| Geometry.linesIntersect(diagonal2x1, diagonal2y1,
						diagonal2x2, diagonal2y2, fp.x, fp.y, lp.x, lp.y))
			return true;
		return false;
	}

	/*
	 * private boolean getOnOffMergeLine() { // �������ж�ȡ���õ�ֵ Ĭ��ֵΪfalse Preferences
	 * onOffMergeLinePregerences = ConfigurationScope.INSTANCE
	 * .getNode("cn.com.agree.ide.commons.floweditor.onoffmergeline"); boolean
	 * onOff =
	 * onOffMergeLinePregerences.getBoolean(IFlowConstants.KEY_ONOFF_MERGELINE,
	 * false); return onOff; }
	 */

	/**
	 * �ӿ����б����ҵ���СFֵ
	 * 
	 * @return
	 */
	private ANode findMinNode(List<ANode> openList) {
		if (openList.size() == 0)
			return null;
		Collections.sort(openList);
		return openList.get(0);
	}

	class ComparatorANode implements Comparator<ANode> {

		@Override
		public int compare(ANode p1, ANode p2) {
			return p1.f() < p2.f() ? -1 : 1;
		}
	}

	// ��дbendpointCOnnnectionRouter�ķ���
	private Map<Connection, Object> constraints = new HashMap<Connection, Object>(
			11);

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

	public void setConstraint(Connection connection, Object constraint) {
		constraints.put(connection, constraint);
	}
}
