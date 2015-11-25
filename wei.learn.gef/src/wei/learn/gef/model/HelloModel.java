package wei.learn.gef.model;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class HelloModel extends AbstractModel {
	private String text = "Hello world";
	private Rectangle constraint;
	public static final String P_CONSTRAINT = "_constraint";
	// ����ַ�����ID �����ı�ͼ�ε��ı�ʱ��֪ͨ��Editpart
	public static final String P_TEXT = "_text";
	// connection

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		firePropertyChange(P_TEXT, null, text);
	}

	public Rectangle getConstraint() {
		return constraint;
	}

	public void setConstraint(Rectangle constraint) {
		this.constraint = constraint;
		firePropertyChange(P_CONSTRAINT, null, constraint);
	}

	// ��ʵ������ͼ����tableview����ʾ����,��һ������������,��2��������ֵ.
	// IPropertyDescriptor[]�������˼��������������������Ƶ�.��������ֻ�ṩһ������
	// ������Ϊgreeeting
	public IPropertyDescriptor[] getPropertyDescriptors() {
		IPropertyDescriptor[] descriptors = new IPropertyDescriptor[] { new TextPropertyDescriptor(
				P_TEXT, "Greeting") };
		return descriptors;
	}

	// ʹ�����Ե�ID����ø�������������ͼ��ֵ
	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals(P_TEXT)) {
			return text;
		}
		return null;
	}

	// �ж�������ͼ�е�����ֵ�Ƿ�ı�,���û��ָ������ֵ�򷵻�false
	@Override
	public boolean isPropertySet(Object id) {
		if (id.equals(P_TEXT)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		if (id.equals(P_TEXT)) {
			setText((String) value);
		}
	}
}
