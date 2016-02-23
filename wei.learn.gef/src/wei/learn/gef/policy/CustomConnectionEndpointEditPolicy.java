package wei.learn.gef.policy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.handles.ConnectionHandle;

import wei.learn.gef.handle.HomunculeConnectionEndHandle;
import wei.learn.gef.handle.HomunculeConnectionStartHandle;
//��ӦABIDE ��FlowWireEndpointPolicy
public class CustomConnectionEndpointEditPolicy extends
		ConnectionEndpointEditPolicy {
	//ѡ����֮�� ��ʼ�˵��handle��ʽ
	@Override
	protected List createSelectionHandles() {
		List<ConnectionHandle> list = new ArrayList<ConnectionHandle>();
		list.add(new HomunculeConnectionEndHandle(
				(ConnectionEditPart) getHost()));
		list.add(new HomunculeConnectionStartHandle(
				(ConnectionEditPart) getHost()));
		return list;
	}
}
