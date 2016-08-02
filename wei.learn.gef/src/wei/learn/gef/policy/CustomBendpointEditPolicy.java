package wei.learn.gef.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.BendpointEditPolicy;
import org.eclipse.gef.handles.BendpointHandle;
import org.eclipse.gef.requests.BendpointRequest;

import wei.learn.gef.command.DeleteBendpointCommand;
import wei.learn.gef.command.MoveBendpointCommand;
import wei.learn.gef.command.MultiCommand;
import wei.learn.gef.handle.CustomBendpointCreationHandle;
import wei.learn.gef.handle.CustomBendpointMoveHandle;

// ide: BendpointPolicy
/**
 * �������е�����ģʽ��ˮƽ��ֱ�� ����Ҫ�����еļ򵥵�bendpoint�Ĵ����ƶ��ķ�ʽ���¹���
 * 
 * @author Administrator
 *
 */
public class CustomBendpointEditPolicy extends BendpointEditPolicy {

	private static final int CheckDeleteLimit = 10;

	private boolean deleteBendpoint = false; // ��ǰ�Ƿ���ɾ��״̬
	private boolean testDelete = false; // ��⵽ɾ��״̬
	private int[] deleteIndexs = null;
	private int changeIndex = -1;
	private Point orginPoint;
	private Bendpoint changePoint;
	// ����������ʱ�洢����. ������ Movefeedback��deletefeedback�����л�
	private Point fPoint;
	private Point nPoint;
	private Point fborderPoint;
	private Point nborderPoint;

	private void resetData() {
		deleteBendpoint = false;
		testDelete = false;
		deleteIndexs = null;
		changeIndex = -1;

	}

	public Command getCommand(Request request) {
		if (REQ_MOVE_BENDPOINT.equals(request.getType())) {
			if (deleteBendpoint)
				return getDeleteBendpointCommand((BendpointRequest) request);
			return getMoveBendpointCommand((BendpointRequest) request);
		}
		if (REQ_CREATE_BENDPOINT.equals(request.getType()))
			return getCreateBendpointCommand((BendpointRequest) request);
		return null;
	}

	@Override
	protected List<BendpointHandle> createSelectionHandles() {
		List<BendpointHandle> list = new ArrayList<BendpointHandle>();
		ConnectionEditPart connEP = (ConnectionEditPart) getHost();
		PointList points = getConnection().getPoints();
		Point fPoint; // forward point
		Point cPoint; // current point
		Point nPoint; // next point
		for (int i = 1; i < points.size() - 1; i++) { // �ӵ�һ���㵽�����ڶ�����ı���
			fPoint = points.getPoint(i - 1);
			cPoint = points.getPoint(i);
			nPoint = points.getPoint(i + 1);
			CustomBendpointCreationHandle cHandle = new CustomBendpointCreationHandle(
					connEP, i, i);
			// �ж���״ ת����Ҫ���Ƿ��� ����һ��8����� ͨ���������x,y�������仯�ķ�ʽ���Կ����ж�
			if ((cPoint.x - fPoint.x + cPoint.y - fPoint.y)
					* ((cPoint.x - nPoint.x + cPoint.y - nPoint.y)) < 0) {
				cHandle.setCursorDirection(CustomBendpointCreationHandle.NORTHEAST);
			} else if ((cPoint.x - fPoint.x + cPoint.y - fPoint.y)
					* ((cPoint.x - nPoint.x + cPoint.y - nPoint.y)) > 0) {
				cHandle.setCursorDirection(CustomBendpointCreationHandle.NORTHWEST);
			}
			list.add(cHandle);

			if (i > 0 && i < points.size() - 2) {
				list.add(new CustomBendpointMoveHandle(connEP, i - 1, i));
			}
		}
		return list;
	}

	/*
	 * �ƶ�bendpoint���˵�bendpoints
	 */
	@Override
	protected Command getMoveBendpointCommand(BendpointRequest request) {
		MoveBendpointCommand moveCom = new MoveBendpointCommand();
		Point mouseLocation = request.getLocation();
		int reqIndex = request.getIndex();
		getConnection().translateToRelative(mouseLocation);

		Map<Integer, Point> changePoints = handlebendpointMove(request);
		// �޸���
		if (!changePoints.isEmpty()) {
			Point[] newLocs = new Point[changePoints.size()];
			int[] indexs = new int[changePoints.size()];
			int j = 0;
			for (Entry<Integer, Point> entry : changePoints.entrySet()) {
				indexs[j] = entry.getKey();
				newLocs[j] = entry.getValue();
				j++;
			}
			moveCom.setConnection(getHost().getModel());
			moveCom.setNewLocation(newLocs);
			moveCom.setIndex(indexs);
		}

		return moveCom;
	}

	@Override
	protected Command getDeleteBendpointCommand(BendpointRequest request) {
		MultiCommand multiCmd = new MultiCommand();
		List<Command> cmds = new ArrayList<Command>();
		multiCmd.setCommands(cmds);
		// �ƶ���ص�
		MoveBendpointCommand moveCmd = new MoveBendpointCommand();
		moveCmd.setConnection(getHost().getModel());
		moveCmd.setIndex(new int[] { changeIndex });
		moveCmd.setNewLocation(new Point[] { (Point) changePoint });
		cmds.add(moveCmd);
		// ɾ���غϵ�
		DeleteBendpointCommand delCmd = new DeleteBendpointCommand();
		delCmd.setConnection(getHost().getModel());
		delCmd.setIndex(deleteIndexs);
		cmds.add(delCmd);
		return multiCmd;
	}

	@Override
	protected void eraseConnectionFeedback(BendpointRequest request) {
		super.eraseConnectionFeedback(request);
		resetData();
	}

	@Override
	protected void showMoveBendpointFeedback(BendpointRequest request) {
		Point mouseLocation = new Point(request.getLocation());
		int reqIndex = request.getIndex();
		List<Point> bendpoints = getBendpoints();
		testDelete = false;
		// ����׼��
		if (!deleteBendpoint) {
			// ׼������
			fPoint = bendpoints.get(reqIndex);
			nPoint = bendpoints.get(reqIndex + 1);
			fborderPoint = null; // ǰ���߽�
			nborderPoint = null; // �󷽱߽�
			deleteIndexs = new int[2];
			// �����߶����,��Ҫ�޸ĵ�����
			if (reqIndex > 0) {
				fborderPoint = bendpoints.get(reqIndex - 1);
			}
			if (reqIndex < bendpoints.size() - 2) {
				nborderPoint = bendpoints.get(reqIndex + 2);
			}
		}

		orginPoint = null;
		changePoint = null;
		if (fPoint.y == nPoint.y) // ��ֱ�ƶ�
		{
			if (fborderPoint != null) {// ����ǰ���߽�
				if (Math.abs(mouseLocation.y - fborderPoint.y) < CheckDeleteLimit) { // ����delete
					testDelete = true;
					if (!deleteBendpoint) {
						deleteIndexs[0] = bendpoints.indexOf(fborderPoint);
						deleteIndexs[1] = bendpoints.indexOf(fPoint);
					}
					orginPoint = nPoint;
					changePoint = new AbsoluteBendpoint(nPoint.x,
							fborderPoint.y);
				}
			}
			if (nborderPoint != null) {
				if (Math.abs(mouseLocation.y - nborderPoint.y) < CheckDeleteLimit) { // ����delete
					testDelete = true;
					if (!deleteBendpoint) {
						deleteIndexs[0] = bendpoints.indexOf(nPoint);
						deleteIndexs[1] = bendpoints.indexOf(nborderPoint);
					}
					orginPoint = fPoint;
					changePoint = new AbsoluteBendpoint(fPoint.x,
							nborderPoint.y);
				}
			}
		} else if (fPoint.x == nPoint.x) { // ˮƽ�ƶ�
			if (fborderPoint != null) {// ����ǰ���߽�
				if (Math.abs(mouseLocation.x - fborderPoint.x) < CheckDeleteLimit) { // ����delete
					testDelete = true;
					if (!deleteBendpoint) {
						deleteIndexs[0] = bendpoints.indexOf(fborderPoint);
						deleteIndexs[1] = bendpoints.indexOf(fPoint);
					}
					orginPoint = nPoint;
					changePoint = new AbsoluteBendpoint(fborderPoint.x,
							nPoint.y);
				}
			}
			if (nborderPoint != null) {
				if (Math.abs(mouseLocation.x - nborderPoint.x) < CheckDeleteLimit) { // ����delete
					testDelete = true;
					if (!deleteBendpoint) {
						deleteIndexs[0] = bendpoints.indexOf(nPoint);
						deleteIndexs[1] = bendpoints.indexOf(nborderPoint);
					}
					orginPoint = fPoint;
					changePoint = new AbsoluteBendpoint(nborderPoint.x,
							fPoint.y);
				}
			}
		}
		if (testDelete) {
			System.err.println("test delete");
			if (!deleteBendpoint) {
				System.err.println("deleteBendpoint is true");
				deleteBendpoint = true;
				saveOriginalConstraint();
				changeIndex = bendpoints.indexOf(orginPoint);
				List<Point> newBendpoints = getBendpoints();
				newBendpoints.set(changeIndex, (AbsoluteBendpoint) changePoint);
				for (int i = 0; i < deleteIndexs.length; i++) {
					newBendpoints.remove(deleteIndexs[i] - i);
				}
				getConnection().setRoutingConstraint(newBendpoints);
			}
			return;
		}
		if (deleteBendpoint) {
			System.err.println("deleteBendpoint is false");
			deleteBendpoint = false;
			restoreOriginalConstraint();
		}
		getConnection().translateToRelative(mouseLocation);
		saveOriginalConstraint();
		List<Point> originBendpoints = getBendpoints();
		Map<Integer, Point> changePoints = handlebendpointMove(mouseLocation,
				fPoint, nPoint, reqIndex);
		for (Entry<Integer, Point> entry : changePoints.entrySet()) {
			originBendpoints.set(entry.getKey(),
					new AbsoluteBendpoint(entry.getValue()));
		}
		getConnection().setRoutingConstraint(originBendpoints);
	}

	@SuppressWarnings("unchecked")
	private List<Point> getBendpoints() {
		return (List<Point>) getConnection().getRoutingConstraint();
	}

	@Override
	protected void showDeleteBendpointFeedback(BendpointRequest request) {
		saveOriginalConstraint();
	}

	private Map<Integer, Point> handlebendpointMove(Point mouseLocation,
			Point forwardPoint, Point nextPoint, int fPointIndex) {
		Map<Integer, Point> changePoints = new HashMap<Integer, Point>();
		// ��Ҫ�����ֵ
		Point nForwardPoint = new Point();
		Point nNextPoint = new Point();
		if (forwardPoint.x == nextPoint.x) // ����Ƿ��������϶�
		{
			// y���� ����x
			nForwardPoint.y = forwardPoint.y;
			nForwardPoint.x = mouseLocation.x;
			nNextPoint.y = nextPoint.y;
			nNextPoint.x = mouseLocation.x;

		}

		if (forwardPoint.y == nextPoint.y)// ����Ƿ��������϶�
		{
			// x���� ����y
			nForwardPoint.x = forwardPoint.x;
			nForwardPoint.y = mouseLocation.y;
			nNextPoint.x = nextPoint.x;
			nNextPoint.y = mouseLocation.y;
		}
		changePoints.put(fPointIndex, nForwardPoint);
		changePoints.put(fPointIndex + 1, nNextPoint);
		return changePoints;
	}

	private Map<Integer, Point> handlebendpointMove(BendpointRequest request) {
		int reqIndex = request.getIndex();
		Point mouseLocation = request.getLocation();
		List<Point> constraint = getBendpoints();
		// ׼������
		Point fPoint = constraint.get(reqIndex);
		Point nPoint = constraint.get(reqIndex + 1);

		Map<Integer, Point> changePoints = new HashMap<Integer, Point>();
		// ��Ҫ�����ֵ
		Point nForwardPoint = new Point();
		Point nNextPoint = new Point();
		if (fPoint.x == nPoint.x) // ����Ƿ��������϶�
		{
			// y���� ����x
			nForwardPoint.y = fPoint.y;
			nForwardPoint.x = mouseLocation.x;
			nNextPoint.y = nPoint.y;
			nNextPoint.x = mouseLocation.x;

		}
		if (fPoint.y == nPoint.y)// ����Ƿ��������϶�
		{
			// x���� ����y
			nForwardPoint.x = fPoint.x;
			nForwardPoint.y = mouseLocation.y;
			nNextPoint.x = nPoint.x;
			nNextPoint.y = mouseLocation.y;
		}
		changePoints.put(reqIndex, nForwardPoint);
		changePoints.put(reqIndex + 1, nNextPoint);
		return changePoints;
	}

	@Override
	protected Command getCreateBendpointCommand(BendpointRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void showCreateBendpointFeedback(BendpointRequest request) {
		Point mouseLocation = new Point(request.getLocation());
		int reqIndex = request.getIndex();
		getConnection().translateToRelative(mouseLocation);
		saveOriginalConstraint();
		List<Point> constraint = getBendpoints();
		// ����׼�� �����������ڵ��λ��
		Point fPoint = null; // forward
		Point cPoint;
		Point nPoint = null;
		fPoint = constraint.get(reqIndex - 1);
		cPoint = constraint.get(reqIndex);
		nPoint = constraint.get(reqIndex + 1);
		Map<Integer, Point> changePoints = bendpointCreationRelatePoints(
				mouseLocation, fPoint, cPoint, nPoint, reqIndex);
		// ��ȡ����϶�������

		getConnection().setRoutingConstraint(constraint);
	}

	private Map<Integer, Point> bendpointCreationRelatePoints(Point mousePoint,
			Point fPoint, Point cPoint, Point nPoint, int fPointIndex) {
		boolean addLine = false; // true �����������ߵ���Ӧ; false �ƶ��ߵ���Ӧ
		// ȷ���Լ�����״
		if ((cPoint.x - fPoint.x + cPoint.y - fPoint.y)
				* ((cPoint.x - nPoint.x + cPoint.y - nPoint.y)) < 0) {// Northeast
			if (cPoint.x == fPoint.x && cPoint.y > fPoint.y) // ��һ����
			{
				if (cPoint.x < mousePoint.x && cPoint.y > mousePoint.y) {
					addLine = true;
				}
			} else { // ��������
				if (cPoint.x > mousePoint.x && cPoint.y < mousePoint.y) {
					addLine = true;
				}
			}
		} else if ((cPoint.x - fPoint.x + cPoint.y - fPoint.y)
				* ((cPoint.x - nPoint.x + cPoint.y - nPoint.y)) > 0) { // Northwest
			if (cPoint.x == fPoint.x && cPoint.y > fPoint.y) // �ڵڶ�����
			{
				if (cPoint.x > mousePoint.x && cPoint.y > mousePoint.y) {
					addLine = true;
				}
			} else { // ��������
				if (cPoint.x < mousePoint.x && cPoint.y < mousePoint.y) {
					addLine = true;
				}
			}
		}
		
		

		return null;
	}
}
