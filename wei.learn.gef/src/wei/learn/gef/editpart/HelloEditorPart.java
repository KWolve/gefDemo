package wei.learn.gef.editpart;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.CompoundBorder;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.jface.viewers.TextCellEditor;

import wei.learn.gef.model.HelloModel;
import wei.learn.gef.policy.CustomComponentEditPolicy;
import wei.learn.gef.policy.CustomDirectEditPolicy;

public class HelloEditorPart extends EditPartWithListener {

	private CustomDirectEditManager directManager = null;

	@Override
	protected IFigure createFigure() {
		HelloModel model = (HelloModel) getModel();

		Label label = new Label();
		label.setText(model.getText());

		label.setBorder(new CompoundBorder(new LineBorder(),
				new MarginBorder(3)));
		label.setBackgroundColor(ColorConstants.orange);
		label.setOpaque(true);
		return label;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new CustomComponentEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
				new CustomDirectEditPolicy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		if (propertyName.equals(HelloModel.P_CONSTRAINT)) {
			refreshVisuals();
		} else if (propertyName.equals(HelloModel.P_TEXT)) {
			Label label = (Label) getFigure();
			label.setText((String) evt.getNewValue());
		}
	}

	@Override
	protected void refreshVisuals() {
		Rectangle constraint = ((HelloModel) getModel()).getConstraint();
		((GraphicalEditPart) getParent()).setLayoutConstraint(this,
				getFigure(), constraint);
	}

	@Override
	public void performRequest(Request req) {
		// ���Request��REQ_DIRECT_EDIT,��ִ��ֱ�ӱ༭���Եĸ�������performDirectEdit
		if (req.getType().equals(RequestConstants.REQ_DIRECT_EDIT)) {
			performDirectEdit();
			return;
		}
	}

	private void performDirectEdit() {
		if (directManager == null) {
			// �����û��directManager,�򴴽�
			directManager = new CustomDirectEditManager(this,
					TextCellEditor.class, new CustomCellEditorLocator(
							getFigure()));
		}
		directManager.show();
	}
}