package galaxy.ide.configurable.editor.gef.router;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * �ڵ�Ԥ����
 * 
 * @author caiyu
 * @date 2014-5-15
 */
public class ANodePreReader {

	private Map<Integer, Map<Integer, ANodeLevel>> pool;
	private Point startPoint;
	private Point endPoint;

	public Point getStartPoint() {
		return startPoint;
	}

	public Point getEndPoint() {
		return endPoint;
	}

	public ANodePreReader() {
		pool = new HashMap<Integer, Map<Integer, ANodeLevel>>();
	}

	public Map<Integer, Map<Integer, ANodeLevel>> getPool() {
		return pool;
	}

	/**
	 * 
	 * @param r
	 * @param startPoint
	 * @param D
	 */
	public void read(Rectangle r, final int D) {
		int xS = ANodePreReader.calculateIndex(r.x, startPoint.x, D);
		int xE = ANodePreReader
				.calculateIndex(r.x + r.width(), startPoint.x, D);
		int yS = ANodePreReader.calculateIndex(r.y, startPoint.y, D);
		int yE = ANodePreReader.calculateIndex(r.y + r.height(), startPoint.y,
				D);
		Map<Integer, ANodeLevel> map;
		Map<Integer, ANodeLevel> mapXE;
		// 1 ΪͼԴ�ϵĵ� �����Ѷ� hard
		for (int x = xS; x < xE; x++) {
			for (int y = yS; y < yE; y++) {
				map = pool.get(x);
				if (map == null) {
					map = new HashMap<Integer, ANodeLevel>();
				}
				map.put(y, ANodeLevel.HARD);
				pool.put(x, map);
			}
		}

		// 2 ΪͼԴ���ܵĵ������Ѷ� normal
		// 2.1 ���±߽� �����ĸ�����
		for (int x = xS - 1; x < xE + 1; x++) {
			map = pool.get(x);
			if (map == null) {
				map = new HashMap<Integer, ANodeLevel>();
			}
			// ���ys�Ѿ��ܿ���ͼԴ���ϱ߽��� �������������һ��normal��
			int offsetYs = (r.y - startPoint.y) % D;
			if (4 < Math.abs(offsetYs)) {
				map.put(yS - 1, ANodeLevel.NORMAL);
			}
			// �±߽�ͬ��
			int offsetYe = (r.y + r.height() - startPoint.y) % D;
			if (4 < Math.abs(offsetYe)) {
				map.put(yE, ANodeLevel.NORMAL);
			}
			pool.put(x, map);
		}

		// ��,�Ҳ�����
		map = pool.get(xS - 1);
		if (map == null) {
			map = new HashMap<Integer, ANodeLevel>();
		}
		mapXE = pool.get(xE);
		if (mapXE == null) {
			mapXE = new HashMap<Integer, ANodeLevel>();
		}
		for (int y = yS; y < yE; y++) {
			map.put(y, ANodeLevel.NORMAL);
			mapXE.put(y, ANodeLevel.NORMAL);
		}
		pool.put(xS - 1, map);
		pool.put(xE, mapXE);

		// 3 �������յ���ѶȽ������⴦�� ����ʼ����յ� �� �Ѿ����ڽ���ͼԴ��ĵ� ���Ѷ�����Ϊeasy
		// ��������ǵ����յ���ͼԴ�ڲ�����Ч��
		if (r.contains(startPoint)) // ��������²��ٽ���
		{
			map = pool.get(0);
			if (map == null) {
				map = new HashMap<Integer, ANodeLevel>();
			}
			map.put(0, ANodeLevel.EASY);
			map.put(1, ANodeLevel.EASY);
		}
		if (r.contains(endPoint)) // �յ���յ��ϲ��ٽ���
		{
			ANode endNode = new ANode(endPoint, startPoint, D);
			map = pool.get(endNode.xIndex);
			if (map == null) {
				map = new HashMap<Integer, ANodeLevel>();
			}
			map.put(endNode.yIndex, ANodeLevel.EASY);
			map.put(endNode.yIndex - 1, ANodeLevel.EASY);
		}
	}

	public ANode getNode(int x, int y) {
		ANode node = new ANode(x, y);
		Map<Integer, ANodeLevel> map = pool.get(x);
		node.setLevel(map == null ? ANodeLevel.EASY // ���pool��û�д����Ѷ� Ĭ��Ϊ��򵥵��Ѷ�
				: map.get(y) == null ? ANodeLevel.EASY : map.get(y));
		return node;
	}

	public void reset() {
		this.startPoint = null;
		this.endPoint = null;
		pool.clear();
	}

	public static int calculateIndex(int v1, int v2, int distance) {
		int offset = (v1 - v2) % distance;
		return offset > 2 ? (v1 - v2) / distance + 1 : (v1 - v2) / distance;
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}

	public void setEndPoint(Point endPoint) {
		this.endPoint = endPoint;
	}
}
